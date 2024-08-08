package kr.co.seoultel.message.mt.mms.direct.util.skt;

import kr.co.seoultel.message.mt.mms.core.common.protocol.SktProtocol;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckType;

public class SktUtil {

    public static NAckType getNAckTypeByStatusCode(String statusCode) {
        switch (statusCode) {
            case SktProtocol.SUCCESS: // 성공 / "1000"
            case SktProtocol.ORIG_ADDRESS_ERROR: // 최초발신사업자 코드 인증 실패 / "2003"
            case SktProtocol.MULTIMEDIA_CONTENT_REFUSED: // 지원하지 않는 미디어 타입이거나 MIME 형식 오류 / "2004"
            case SktProtocol.MESSAGE_ID_NOT_FOUND: // Message ID가 없음 / "2005"
            case SktProtocol.MESSAGE_FORMAT_CORRUPT: // 메시지 포맷 혹은 값 오류 / "2007"
            case SktProtocol.CONTENT_ERROR: // 올바른 컨텐츠가 아님 / "2101"
            case SktProtocol.CLIENT_ERROR: // 일시적인 단말 문제 / "2102"
            case SktProtocol.CLIENT_UNAVAILABLE: // 미지원 단말 / "2103"
            case SktProtocol.CONTENT_SIZE_ERROR: // 컨텐츠 크기가 커서 처리할 수 없음 / "2104"
            case SktProtocol.SERVICE_ERROR: // 일반적인 서비스 에러 / "4005"
                return NAckType.ACK;

            // HUBSP 오류
            case SktProtocol.ADDRESSS_ERROR: // CID 및 발송IP 인증 실패 / "2002"
            case SktProtocol.SERVICE_UNAVAILABLE: // 사용자가 많아 일시적인 서비스 불가 (Timeout) / "4006"
            case SktProtocol.SERVICE_DENIED: // 서비스를 요청한 client가 permission이 없는 경우 / "4007"
            case SktProtocol.EXCEED_MAX_TRANS: // CID에 등록 된 전송량 초과로 실패 처리 (전송되지 않음) / "4008"
            case SktProtocol.SERVICE_CHECK: // 서비스 점검중인 경우 / "4101"
                return NAckType.NACK;

            default:
                return NAckType.ACK;
        }
    }
    public static NAckType getNAckTypeSubmitAckStatusCode(String statusCode) {
        switch (statusCode) {
            // 성공건
            case SktProtocol.SUCCESS:   // SUCCESS
                return NAckType.ACK;

            // 메세지 형식 오류
            case SktProtocol.ORIG_ADDRESS_ERROR:
            case SktProtocol.MULTIMEDIA_CONTENT_REFUSED:
            case SktProtocol.MESSAGE_ID_NOT_FOUND:
            case SktProtocol.MESSAGE_FORMAT_CORRUPT:
            case SktProtocol.CONTENT_ERROR:
            case SktProtocol.CONTENT_SIZE_ERROR:
                return NAckType.ACK;

            // 미지원 단말
            case SktProtocol.CLIENT_UNAVAILABLE:
                return NAckType.ACK;

            case SktProtocol.CLIENT_ERROR: // 클라이언트 오류
                return NAckType.ACK;

            // SKT 측 서비스 문제로 인한 재처리
            case SktProtocol.SERVICE_ERROR:
            case SktProtocol.SERVICE_UNAVAILABLE:
            case SktProtocol.SERVICE_CHECK:
                return NAckType.NACK;

            // CID 또는 발송 IP 설정 실수로 인한 재처리
            case SktProtocol.ADDRESSS_ERROR:
            case SktProtocol.SERVICE_DENIED:
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
