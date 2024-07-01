package kr.co.seoultel.message.mt.mms.direct.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Objects;


@Getter
@Builder
@ToString
@AllArgsConstructor
public class TestMessage {



    private String groupCode = "A000010";
    // phoneNumber
    private String sender = "01056610926";
    private String callback = "01056610926";
    private String receiver = "01056610926";

    // Message
    private String subject = "새해 복 많이 받으세요";
    private String msg;

    private List<String> mediaFiles = List.of();

    private String originCode = "123456789";

    private String reportTo = "http://192.168.50.24:8001";

    private String telecom = "KT";

    public void setTelecom(String telecom) {
        if (telecom == null) {
            this.telecom = "KT";
        } else {
            this.telecom = telecom;
        }
    }

    public void setGroupCode(String groupCode) {
        if (groupCode == null) {
            this.groupCode = "A000010";
        } else {
            this.groupCode = groupCode;
        }
    }

    public void setSender(String sender) {
        if (sender == null) {
            this.sender = "01056610926";
        } else {
            this.sender = sender;
        }
    }


    public void setCallback(String callback) {
        if (callback == null) {
            this.callback = "01056610926";
        } else {
            this.callback = callback;
        }
    }

    public void setReceiver(String receiver) {
        if (callback == null) {
            this.receiver = "01056610926";
        } else {
            this.receiver = receiver;
        }
    }

    public void setSubject(String subject) {
        if (subject == null) {
            this.subject = "새해 복 많이 받으세요";
        } else {
            this.subject = subject;
        }
    }

    public void setMsg(String msg) {
        this.msg = Objects.requireNonNullElse(msg, "행복한 한 해 되시기를 기원합니다.");
    }

    public void setMediaFiles(List<String> mediaFiles) {
        if (mediaFiles == null || mediaFiles.isEmpty()) {
            this.mediaFiles = List.of();
        } else {
            this.mediaFiles = mediaFiles;
        }
    }

    public void setOriginCode(String originCode) {
        if (originCode == null) {
            this.originCode = "123456789";
        } else {
            this.originCode = originCode;
        }
    }

    public void setReportTo(String reportTo) {
        if (reportTo == null) {
            this.reportTo = "http://192.168.50.24:8001";
        } else {
            this.reportTo = reportTo;
        }
    }
}
