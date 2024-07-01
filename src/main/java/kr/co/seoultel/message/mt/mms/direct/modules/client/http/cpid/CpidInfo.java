package kr.co.seoultel.message.mt.mms.direct.modules.client.http.cpid;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class CpidInfo {

    private String cpid;
    private int tps;

    public CpidInfo(String cpid, Integer tps) {
        this.cpid = cpid;
        this.tps = Objects.requireNonNull(tps);
    }

    @Override
    public String toString() {
        return "CpidInfo{" +
                "cpid='" + cpid + '\'' +
                ", tps=" + tps +
                '}';
    }
}
