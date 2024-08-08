package kr.co.seoultel.message.mt.mms.direct.modules;

import com.rabbitmq.client.*;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.TpsOverExeption;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.FormatException;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.MessageDeserializationException;
import kr.co.seoultel.message.mt.mms.core.entity.DeliveryType;
import kr.co.seoultel.message.mt.mms.core.util.*;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerDisconnectionException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerTokenException;
import kr.co.seoultel.message.mt.mms.core_module.distributor.Distributable;
import kr.co.seoultel.message.mt.mms.core_module.distributor.WeightNode;
import kr.co.seoultel.message.mt.mms.core_module.distributor.WeightedRoundRobinDistributor;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.core_module.utils.MMSReportUtil;
import kr.co.seoultel.message.mt.mms.direct.config.FileServerConfig;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClient;
import kr.co.seoultel.message.mt.mms.core_module.modules.consumer.AbstractConsumer;
import kr.co.seoultel.message.mt.mms.core_module.modules.heartBeat.HeartBeatProtocol;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.Application;
import kr.co.seoultel.message.mt.mms.direct.config.RabbitMQConfig;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;
import lombok.NonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Map;

@Slf4j
@Component
public class MessageConsumer extends AbstractConsumer {

    private final int totalTps;

    private final CachingConnectionFactory cachingConnectionFactory;
    private final Distributable<WeightNode<HttpClient>> distributor;


    public MessageConsumer(RabbitMQConfig rabbitMQConfig, CachingConnectionFactory cachingConnectionFactory,
                           Integer totalTps,
                           WeightedRoundRobinDistributor<WeightNode<HttpClient>> weightNodeWeightedRoundRobinDistributor,
                           QueueStorage<MessageDelivery> republishQueueStorage, QueueStorage<MrReport> reportQueueStorage) {
        super(rabbitMQConfig, republishQueueStorage, reportQueueStorage);

        this.cachingConnectionFactory = cachingConnectionFactory;

        this.totalTps = totalTps;
        this.distributor = weightNodeWeightedRoundRobinDistributor;
    }

    public void init() {
        createChannel();

        Map<String, Object> arguments = Map.of();
        declareExchange(BuiltinExchangeType.DIRECT);
        declareQueue(arguments);
        bindQueueByExchange(totalTps, true);

        doConsume();
        HeartBeatClient.setHStatus(HeartBeatProtocol.HEART_SUCCESS);
    }

    @Override
    public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
        if (!Application.isStarted()) {
            log.info("[CONSUMER] Successfully closed consumer's channel");
            return;
        }

        switch (HeartBeatClient.getHStatus()) {
            // Channel close
            case HeartBeatProtocol.DST_CONNECTION_ERROR:
                log.info("[CONSUMER] CLOSED CONSUMER, CAUSED BY DISCONNECTED TO {}", SenderConfig.GROUP);
                break;

            // 연결 재복구
            default:
                log.error("[RABBIT EXCEPTION] SHUTDOWN CONSUMER -> ", sig);

                do {
                    CommonUtil.doThreadSleep(2000L);
                    createChannel();
                } while (Application.isStarted() && (channel == null | (channel != null && !channel.isOpen())));

                doConsume();
                break;
        }
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException{
        long deliveryTag = envelope.getDeliveryTag();

        try {

            MessageDelivery messageDelivery = ConvertorUtil.convertBytesToObject(bytes, MessageDelivery.class);
            log.info("[Consume] Successfully consume message[{}] from rabbitMQ", messageDelivery.getUmsMsgId());

            boolean isFallback = FallbackUtil.isFallback(messageDelivery);
            if (isFallback) {
                ValidateUtil.validateFallbackMessageDelivery(messageDelivery);
                ValidateUtil.assignDefaultFallbackSubjectIfNullOrEmpty(messageDelivery);
            } else {
                ValidateUtil.validateMessageDelivery(messageDelivery);
                ValidateUtil.assignDefaultSubjectIfNullOrEmpty(messageDelivery);
            }

            log.info("[Validate] Successfully message[{}] validated [{}]", messageDelivery.getUmsMsgId(), messageDelivery);

            InboundMessage inboundMessage = new InboundMessage(deliveryTag, messageDelivery, this);
            WeightNode<HttpClient> weightNode = distributor.get();
            weightNode.getElement().doSubmit(inboundMessage);
        } catch (FormatException e) {
            log.warn(e.getMessage());

            if (e instanceof MessageDeserializationException) {
                channel.basicAck(deliveryTag, false);
                return;
            }

            MessageDelivery messageDelivery = e.getMessageDelivery();
            MMSReportUtil.handleSenderException(messageDelivery, e.getReportMessage(), e.getMnoResult(), e.getDeliveryType());

            // Validation 이 끝난 경우, MessageDelivery 객체는 null 이 아님.
            reportQueueStorage.add(new MrReport(DeliveryType.SUBMIT_ACK, messageDelivery));

            channel.basicAck(deliveryTag, false);
        } catch (FileServerException e) {
            if (e instanceof FileServerTokenException) {
                log.warn("[SYSTEM] INVALID FILE-SERVER TOKEN[{}]", FileServerConfig.TOKEN);
                channel.basicNack(deliveryTag, false, true);
                return;
            }

            if (e instanceof FileServerDisconnectionException) {
                log.warn("[SYSTEM] TEMPORARY DISCONNECTED TO FILE-SERVER");
                channel.basicNack(deliveryTag, false, true);
                return;
            }

            /*
             * 1. AttatchedImageFormatException : number of images in message is excess 3
             * 2. ImageNotFoundException : groypCode is not corresponed to imageId of image attached in message
             * 3. ImageExpiredException : expired images attached in message images
             * 4. FileServerException : unknown exception
             */
            log.warn(e.getMessage());
            MMSReportUtil.handleSenderException(e.getMessageDelivery(), e.getReportMessage(), e.getMnoResult(), e.getDeliveryType());
            channel.basicAck(deliveryTag, false);

            reportQueueStorage.add(new MrReport(DeliveryType.SUBMIT_ACK, e.getMessageDelivery()));
        } catch (TpsOverExeption e) {
            long sleepTime = DateUtil.getTimeGapUntilNextSecond();
            CommonUtil.doThreadSleep(sleepTime);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[Exception] : {}", e.getMessage(), e);
            channel.basicAck(deliveryTag, false);
        }
    }

    @Override
    public void createChannel() {
        try {
            Connection clientCon;
            org.springframework.amqp.rabbit.connection.Connection amqpCon;

            Channel channel;

            if (!rabbitMqConfig.isPrimaryCluster()) {
                clientCon = cachingConnectionFactory.getRabbitConnectionFactory().newConnection();
                channel = clientCon.createChannel();
            } else {
                amqpCon = cachingConnectionFactory.getPublisherConnectionFactory().createConnection();
                channel = amqpCon.createChannel(false);
            }

            if (channel != null && channel.isOpen()) {
                this.channel = channel;
            }
        } catch (AlreadyClosedException | AmqpIOException | ConnectException e) {
            log.error("[DISCONNECTED TO RABBIT] FAILED TO GETTING RABBIT CHANNEL");
        } catch(Exception e) {
            log.error("Exception occurs in creating client Connection", e);
        }
    }


    @Override
    protected void closeChannel() {
        try {
            if (channel != null && channel.isOpen()) {
                this.channel.close();
                this.channel = null;
                log.info("[CONSUMER] SUCCESSFULLY CLOSED RABBIT CHANNEL");
            }
        } catch (Exception e) {
            log.error("Exception occured during rabbit-mq channel closing");
        }
    }


    public void basicCancle() {
        try {
            channel.basicCancel(consumerTag);
        } catch (Exception e) {
            log.error("??? ", e);
        }
    }

    public void basicRecover() {
        try {
            channel.basicRecover();
        } catch (Exception e) {
            log.error("[CONSUMER] Failed to recover consumer", e);
        }
    }

    @Override
    public void handleCancel(String consumerTag) {
        if (consumerTag != null) {
            try {
                channel.basicCancel(consumerTag);
            } catch (Exception e) {
                log.error("[CONSUMER] Failed to cancle consumer of tag[{}]", consumerTag, e);
            }
        }
    }

    public boolean isCreatableChannel() throws IOException {
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(rabbitMqConfig.getMtQueue());
        return declareOk.getConsumerCount() == 0;
    }

    public int getConsumerCount() throws IOException {
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(rabbitMqConfig.getMtQueue());
        return declareOk.getConsumerCount();
    }


    public void destroy() {
        closeChannel();
        log.info("[SHUTDOWN] ReportProcessor is gracefully shutdowned");
    }
}
