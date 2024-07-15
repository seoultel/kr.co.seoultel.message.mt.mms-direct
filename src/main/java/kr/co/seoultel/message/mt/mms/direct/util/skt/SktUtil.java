package kr.co.seoultel.message.mt.mms.direct.util.skt;

import kr.co.seoultel.message.mt.mms.core.common.protocol.SktProtocol;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckType;

public class SktUtil {
    public static NAckType getNAckTypeSubmitAckStatusCode(String statusCode) {
        switch (statusCode) {
            case SktProtocol.SUCCESS:
                return NAckType.ACK;

            // CID 또는 발송 IP 설정 실수로 인한 재처리
            case SktProtocol.ADDRESSS_ERROR:
            case SktProtocol.SERVICE_DENIED:
                return NAckType.NACK;

            // 메세지 형식 오류
            case SktProtocol.ORIG_ADDRESS_ERROR:
            case SktProtocol.MULTIMEDIA_CONTENT_REFUSED:
            case SktProtocol.MESSAGE_ID_NOT_FOUND:
            case SktProtocol.MESSAGE_FORMAT_CORRUPT:
            case SktProtocol.CONTENT_ERROR:
            case SktProtocol.CONTENT_SIZE_ERROR:
                return NAckType.ACK;

            // 단말 문제로 인한 재처리
            case SktProtocol.CLIENT_ERROR:
                return NAckType.NACK;

            // 미지원 단말
            case SktProtocol.CLIENT_UNAVAILABLE:
                return NAckType.ACK;

            // SKT 측 서비스 문제로 인한 재처리
            case SktProtocol.SERVICE_ERROR:
            case SktProtocol.SERVICE_UNAVAILABLE:
            case SktProtocol.SERVICE_CHECK:
                return NAckType.NACK;

            // 전송가능 TPS 초과로 인한 재처리
            case SktProtocol.EXCEED_MAX_TRANS:
                return NAckType.NACK;

            default:
                return NAckType.ACK;
        }
    }

    public static String getStatusCodeKor(String statusCode) {
        switch (statusCode) {
            case SktProtocol.SUCCESS:
                return "SUCCESS";
            case SktProtocol.ADDRESSS_ERROR:
                return "ADDRESSS_ERROR";
            case SktProtocol.ORIG_ADDRESS_ERROR:
                return "ORIG_ADDRESS_ERROR";
            case SktProtocol.MULTIMEDIA_CONTENT_REFUSED:
                return "MULTIMEDIA_CONTENT_REFUSED";
            case SktProtocol.MESSAGE_ID_NOT_FOUND:
                return "MESSAGE_ID_NOT_FOUND";
            case SktProtocol.MESSAGE_FORMAT_CORRUPT:
                return "MESSAGE_FORMAT_CORRUPT";
            case SktProtocol.CONTENT_ERROR:
                return "CONTENT_ERROR";
            case SktProtocol.CLIENT_ERROR:
                return "CLIENT_ERROR";
            case SktProtocol.CLIENT_UNAVAILABLE:
                return "CLIENT_UNAVAILABLE";
            case SktProtocol.CONTENT_SIZE_ERROR:
                return "CONTENT_SIZE_ERROR";
            case SktProtocol.SERVICE_ERROR:
                return "SERVICE_ERROR";
            case SktProtocol.SERVICE_UNAVAILABLE:
                return "SERVICE_UNAVAILABLE";
            case SktProtocol.SERVICE_DENIED:
                return "SERVICE_DENIED";
            case SktProtocol.EXCEED_MAX_TRANS:
                return "EXCEED_MAX_TRANS";

            case SktProtocol.SERVICE_CHECK:
            default:
                return "SERVICE_CHECK";
        }
    }
}
