package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.core.dto.MessageDelivery;
import kr.co.seoultel.message.mt.mms.core.common.constant.Constants;
import kr.co.seoultel.message.mt.mms.core.common.interfaces.Checkable;
import kr.co.seoultel.message.mt.mms.core_module.distributor.RoundRobinDistributor;
import kr.co.seoultel.message.mt.mms.core_module.distributor.WeightNode;
import kr.co.seoultel.message.mt.mms.core_module.distributor.WeightedRoundRobinDistributor;
import kr.co.seoultel.message.mt.mms.core_module.modules.ExpirerService;
import kr.co.seoultel.message.mt.mms.core_module.modules.report.MrReport;
import kr.co.seoultel.message.mt.mms.core_module.storage.HashMapStorage;
import kr.co.seoultel.message.mt.mms.core_module.storage.QueueStorage;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClient;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientHandler;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.HttpClientProperty;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.cpid.CpidInfo;
import kr.co.seoultel.message.mt.mms.direct.ktf.KtfClientHandler;
import kr.co.seoultel.message.mt.mms.direct.lgt.LgtClientHandler;
import kr.co.seoultel.message.mt.mms.direct.skt.SktClientHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Setter
@Configuration
@RequiredArgsConstructor
@ConfigurationProperties("sender.http")
public class DirectConfig implements Checkable {

    protected String bpid;
    protected String vasId;
    protected String vaspId;

    @Value("${sender.http.endpoint.ip}")
    protected String ip;

    @Value("${sender.http.endpoint.port}")
    protected int port;

    private List<CpidInfo> cpids;

    private final ExpirerService expirerService;



    @Override
    @PostConstruct
    public void check() {
        log.info("DirectConfig : {}", this);
    }




    @Bean
    public Integer totalTps() {
        return cpids.stream().mapToInt(CpidInfo::getTps).sum();
    }

    /*
     * HttpClient 클래스를 상속받는 클라이언트 핸들러 객체를 빈으로 등록하여 주입받아서 사용해도 될 것 같으나,
     * 우선 각 클라이언트 객체마다 클라이언트 핸들러를 가지고 있는 구조로 작성하였음.
     */
    @Bean
    public List<HttpClient> httpClients(HashMapStorage<String, MessageDelivery> deliveryStorage, QueueStorage<MrReport> reportQueueStorage) {
        return cpids.stream().map(
                (cpidInfo) -> HttpClientProperty.builder()
                                                .cpidInfo(cpidInfo)
                                                .bpid(bpid)
                                                .vasId(vasId)
                                                .vaspId(vaspId)
                                                .ip(ip)
                                                .port(port)
                                                .build()
        ).map((property) -> {
            HttpClientHandler handler;
            switch (SenderConfig.TELECOM.toUpperCase()) {
                case Constants.SKT:
                    handler = new SktClientHandler(property, deliveryStorage, reportQueueStorage);
                    return new HttpClient(property.getCpidInfo().getTps(), handler, expirerService);

                case Constants.KTF:
                    handler = new KtfClientHandler(property, deliveryStorage, reportQueueStorage);
                    return new HttpClient(property.getCpidInfo().getTps(), handler, expirerService);

                case Constants.LGT:
                default:
                    handler = new LgtClientHandler(property, deliveryStorage, reportQueueStorage);
                    return new HttpClient(property.getCpidInfo().getTps(), handler, expirerService);
            }
        }).collect(Collectors.toList());
    }


    @Bean
    public WeightedRoundRobinDistributor<WeightNode<HttpClient>> weightNodeWeightedRoundRobinDistributor(List<HttpClient> httpClients) {
        List<WeightNode<HttpClient>> weightNodes = httpClients.stream()
                .map((httpClient) -> new WeightNode<HttpClient>(httpClient.getTps(), httpClient))
                .collect(Collectors.toList());

        return new WeightedRoundRobinDistributor<WeightNode<HttpClient>>(weightNodes);
    }


    @Bean
    public RoundRobinDistributor<HttpClient> roundRobinDistributor(List<HttpClient> httpClients) {
        return new RoundRobinDistributor<HttpClient>(httpClients);
    }


    @Override
    public String toString() {
        return "DirectConfig{" +
                "bpid='" + bpid + '\'' +
                ", vasId='" + vasId + '\'' +
                ", vaspId='" + vaspId + '\'' +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", cpids=" + cpids +
                '}';
    }
}
