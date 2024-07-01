package kr.co.seoultel.message.mt.mms.direct.modules.client.http;

import io.github.resilience4j.ratelimiter.RateLimiter;
import kr.co.seoultel.message.mt.mms.core.util.CommonUtil;
import kr.co.seoultel.message.mt.mms.direct.modules.client.http.cpid.CpidInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Getter @Setter
public class HttpClientProperty {

    protected String bpid;
    protected CpidInfo cpidInfo;

    protected String vasId;
    protected String vaspId;

    protected String ip;
    protected int port;

    protected final RateLimiter rateLimiter;

    @Builder
    public HttpClientProperty(String bpid, CpidInfo cpidInfo, String vasId, String vaspId, String ip, int port) {
        this.bpid = bpid;
        this.cpidInfo = cpidInfo;
        this.vasId = vasId;
        this.vaspId = vaspId;
        this.ip = ip;
        this.port = port;
        this.rateLimiter = CommonUtil.getRateLimiter(cpidInfo.getTps(), String.format("%s-tps-limiter", cpidInfo.getCpid()));;
    }

    @Override
    public String toString() {
        return "HttpClientProperty{" +
                "cpidInfo=" + cpidInfo +
                ", bpid='" + bpid + '\'' +
                ", vasId='" + vasId + '\'' +
                ", vaspId='" + vaspId + '\'' +
                '}';
    }
}
