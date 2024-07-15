package kr.co.seoultel.message.mt.mms.direct.modules;

import com.rabbitmq.client.*;
import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.TpsOverExeption;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.FormatException;
import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.MessageDeserializationException;

import kr.co.seoultel.message.mt.mms.core.util.*;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.fileServer.FileServerException;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckException;
import kr.co.seoultel.message.mt.mms.core_module.dto.InboundMessage;
import kr.co.seoultel.message.mt.mms.core_module.distributor.RoundRobinDistributor;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClient;
import kr.co.seoultel.message.mt.mms.core_module.modules.consumer.AbstractConsumer;
import kr.co.seoultel.message.mt.mms.core_module.modules.heartBeat.HeartBeatProtocol;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.direct.Application;
import kr.co.seoultel.message.mt.mms.direct.config.RabbitMQConfig;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class MessageConsumer extends AbstractConsumer {

    private final RoundRobinDistributor<HttpClient> distributor;

    private final RabbitMQConfig rabbitMQConfig;
    private final CachingConnectionFactory cachingConnectionFactory;


    public MessageConsumer(RabbitMQConfig rabbitMQConfig, CachingConnectionFactory cachingConnectionFactory,
                        ConcurrentLinkedQueue<MrReport> reportQueue, ConcurrentLinkedQueue<MessageDelivery> republishQueue, List<HttpClient> httpClients) {
        super(reportQueue, republishQueue);

        this.rabbitMQConfig = rabbitMQConfig;
        this.cachingConnectionFactory = cachingConnectionFactory;
        this.distributor = new RoundRobinDistributor<HttpClient>(httpClients);
    }

    public void init() {
        createChannel();
        doConsume();
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
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bytes) throws IOException {
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
            HttpClient httpClient = distributor.get();
            httpClient.doSubmit(inboundMessage);
        } catch(FormatException e) {
            log.error(e.getMessage());

            if (e instanceof MessageDeserializationException) {
                channel.basicAck(deliveryTag, false);
                return;
            }
        } catch (FileServerException e) {
            throw new RuntimeException(e);
        } catch (NAckException e) {
            throw new RuntimeException(e);
        } catch (TpsOverExeption e) {
            long sleepTime = DateUtil.getTimeGapUntilNextSecond();
            CommonUtil.doThreadSleep(sleepTime);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("e.getMessage() : {}", e.getMessage(), e);
            log.error("HERER? ??E R");
        }
    }


    @Override
    public void createChannel() {
        try {
            Connection clientCon;
            org.springframework.amqp.rabbit.connection.Connection amqpCon;

            Channel channel;

            if (!rabbitMQConfig.isPrimaryCluster()) {
                clientCon = cachingConnectionFactory.getRabbitConnectionFactory().newConnection();
                channel = clientCon.createChannel();
            } else {
                amqpCon = cachingConnectionFactory.getPublisherConnectionFactory().createConnection();
                channel = amqpCon.createChannel(false);
            }

            if (channel != null && channel.isOpen()) {
                HeartBeatClient.setHStatus(HeartBeatProtocol.HEART_SUCCESS);
                setChannel(channel);
            }
        } catch (AlreadyClosedException | AmqpIOException | java.net.ConnectException e) {
            log.error("[DISCONNECTED TO RABBIT] FAILED TO GETTING RABBIT CHANNEL");
        } catch(Exception e) {
            log.error("Exception occurs in creating client Connection", e);
        }
    }


    public void setChannel(@NonNull Channel channel) {
        try {
            channel.basicQos(distributor.getSize());
            channel.basicRecover(true);
            channel.queueBind(mtQueueName, mtExchangeName, mtQueueName);

            this.channel = channel;

            log.info("[CONSUMER] Consumer's channel[channel-number : {}] is activated", channel.getChannelNumber());
        } catch (IOException e) {
            log.error("[IOException] Failed to create channel from RabbitMQ");
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
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(mtQueueName);
        return declareOk.getConsumerCount() == 0;
    }

    public int getConsumerCount() throws IOException {
        AMQP.Queue.DeclareOk declareOk = channel.queueDeclarePassive(mtQueueName);
        return declareOk.getConsumerCount();
    }


    public void destroy() {
        closeChannel();
        republishQueueDataVault.destroy(republishQueue);
        log.info("[SHUTDOWN] ReportProcessor is gracefully shutdowned");
    }
}
