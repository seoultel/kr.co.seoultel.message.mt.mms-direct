package kr.co.seoultel.message.mt.mms.direct.util.lgt;

import kr.co.seoultel.message.mt.mms.core.common.protocol.LgtProtocol;
import kr.co.seoultel.message.mt.mms.core.common.protocol.SktProtocol;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckType;

public class LgtUtil {

    public static NAckType getNAckTypeSubmitAckStatusCode(String statusCode) {
        switch (statusCode) {
            case LgtProtocol.SUCCESS:
            case LgtProtocol.PARTIAL_SUCCESS:
                return NAckType.ACK;

            // 전송가능 TPS 초과로 인한 재처리
            case LgtProtocol.TRAFFIC_IS_OVER:
                return NAckType.NACK;

            // 메세지 형식 오류
            case LgtProtocol.ADDRESS_ERROR:
            case LgtProtocol.ADDRESS_NOT_FOUND:
            case LgtProtocol.MULTI_MEDIA_CONTENT_REFUSED:
            case LgtProtocol.MESSAGE_ID_NOT_FOUND:
            case LgtProtocol.LINKED_ID_NOT_FOUND:
            case LgtProtocol.MESSAGE_FORMAT_CORRUPT:
            case LgtProtocol.IMPROPER_IDENTIFICATION:
            case LgtProtocol.UNSUPPORTED_VERSION:
            case LgtProtocol.UNSUPPORTED_OPERATION:
            case LgtProtocol.VALIDATION_ERROR:
            case LgtProtocol.KISACODE_ERROR:
                return NAckType.ACK;

            // CID 또는 발송 IP 설정 실수로 인한 재처리
            case LgtProtocol.INVALID_AUTH_PASSWORD:
            case LgtProtocol.EXPIRED_PASSWORD:
                return NAckType.NACK;

            // 수신자 착신 거절 신청자 또는 MMS 비가용 단말
            case LgtProtocol.SUBS_REJECT:
            case LgtProtocol.MMS_DISABLE_SUBS:
                return NAckType.ACK;

            // 이통사 서버 문제 / 재전송
            case LgtProtocol.SERVICE_UNAVAILBALE:
            case LgtProtocol.SYSTEM_ERROR:
                return NAckType.NACK;

            // 이통사 서버 문제 / 싪패처리
            case LgtProtocol.CLIENT_ERROR:
            case LgtProtocol.NOT_POSSIBLE:
            case LgtProtocol.MESSAGE_REJECTED:
            case LgtProtocol.MULTIPLE_ADDRESSED_NOT_SUPPORTED:
            case LgtProtocol.GENERAL_SERVICE_ERROR:
            case LgtProtocol.SERVICE_DENIED:
            case LgtProtocol.OPERATION_RESTRICTED:
            case LgtProtocol.SERVER_ERROR:
            default:
                return NAckType.ACK;
        }
    }

    public static String getLgtResultMessageKor(String statusCode) {
        switch (statusCode) {
            case "1000":
                return LgtProtocol.SUCCESS;
            case "1100":
                return LgtProtocol.PARTIAL_SUCCESS;
            case "2000":
                return LgtProtocol.CLIENT_ERROR;
            case "2001":
                return LgtProtocol.OPERATION_RESTRICTED;
            case "2002":
                return LgtProtocol.ADDRESS_ERROR;
            case "2003":
                return LgtProtocol.ADDRESS_NOT_FOUND;
            case "2004":
                return LgtProtocol.MULTI_MEDIA_CONTENT_REFUSED;
            case "2005":
                return LgtProtocol.MESSAGE_ID_NOT_FOUND;
            case "2006":
                return LgtProtocol.LINKED_ID_NOT_FOUND;
            case "2007":
                return LgtProtocol.MESSAGE_FORMAT_CORRUPT;
            case "3001":
                return LgtProtocol.NOT_POSSIBLE;
            case "3002":
                return LgtProtocol.MESSAGE_REJECTED;
            case "3003":
                return LgtProtocol.MULTIPLE_ADDRESSED_NOT_SUPPORTED;
            case "4000":
                return LgtProtocol.GENERAL_SERVICE_ERROR;
            case "4001":
                return LgtProtocol.IMPROPER_IDENTIFICATION;
            case "4002":
                return LgtProtocol.UNSUPPORTED_VERSION;
            case "4003":
                return LgtProtocol.UNSUPPORTED_OPERATION;
            case "4004":
                return LgtProtocol.VALIDATION_ERROR;
            case "4005":
                return LgtProtocol.SERVICE_ERROR;
            case "4006":
                return LgtProtocol.SERVICE_UNAVAILBALE;
            case "4007":
                return LgtProtocol.SERVICE_DENIED;
            case "4030":
                return LgtProtocol.SYSTEM_ERROR;
            case "4032":
                return LgtProtocol.KISACODE_ERROR;
            case "6014":
                return LgtProtocol.SUBS_REJECT;
            case "6024":
                return LgtProtocol.INVALID_AUTH_PASSWORD;
            case "6025":
                return LgtProtocol.EXPIRED_PASSWORD;
            case "6072":
                return LgtProtocol.MMS_DISABLE_SUBS;
            case "7103":
                return LgtProtocol.TRAFFIC_IS_OVER;

            default:
            case "3000":
                return LgtProtocol.SERVER_ERROR;
        }
    }


}
