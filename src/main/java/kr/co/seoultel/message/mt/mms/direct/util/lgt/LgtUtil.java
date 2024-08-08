package kr.co.seoultel.message.mt.mms.direct.util.lgt;

import kr.co.seoultel.message.mt.mms.core.common.protocol.LgtProtocol;
import kr.co.seoultel.message.mt.mms.core.common.protocol.SktProtocol;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckType;

public class LgtUtil {

    public static NAckType getNAckTypeByStatusCode(String statusCode) {
        switch (statusCode) {
            case LgtProtocol.SUCCESS:                             // 메시지가 성공적으로 처리 되었음 / "1000"
            case LgtProtocol.PARTIAL_SUCCESS:                     // 메시지가 부분적으로 실행 되었으나 일부는 처리되지 못했음. / "1100"
            case LgtProtocol.CLIENT_ERROR:                        // Client 가 잘못된 응답을 보냄 / "2000"
            case LgtProtocol.OPERATION_RESTRICTED:                // 허용되지 않은 command 실행에 의해 메시지가 거부됨 / "2001"
            case LgtProtocol.ADDRESS_ERROR:                       // 메시지에 있는 주소가 잘못된 형식이거나 유효하지 않음. 메시지 수신자가 다수일 경우 적어도 한 개의 주소가 잘못되어도 응답을 줌 / "2002"
            case LgtProtocol.ADDRESS_NOT_FOUND:                   // 메시지에 있는 주소를 MMS Relay/Server 가 찾을 수 없음. 이 코드는 메시지가 전송될 주소를 찾을 수 없을 때 리턴 됨 / "2003"
            case LgtProtocol.MULTI_MEDIA_CONTENT_REFUSED:         // 1) SOAP 메시지에 포함된 MIME content의 요소나 크기, 타입이 불분명하거나 2) 이미지 크기가 허용된 크기보다 초과하여 전송하는 경우에 리턴 / "2004"
            case LgtProtocol.MESSAGE_ID_NOT_FOUND:                // MMS Relay/Server가 이전에 전송된 메시지에 대한 message ID를 찾을 수 없거나 VASP로 부터 받은 응답에서 message ID를 찾을 수 없음. / "2005"
            case LgtProtocol.LINKED_ID_NOT_FOUND:                 // MMS Relay/Server가 메시지에 있는 LinkedID 를 찾을 수 없음. / "2006"
            case LgtProtocol.MESSAGE_FORMAT_CORRUPT:              // 메시지가 규격에 맞지 않거나 부적당함 / "2007"
            case LgtProtocol.SERVER_ERROR:                        // 서버에서 올바른 요청에 대한 처리를 실패함 / "3000"
            case LgtProtocol.NOT_POSSIBLE:                        // 메시지 처리가 불가능함. 이 코드는 메시지 가 더 이상 유효하지 않거나 취소된 것에 대한 결과임. 메시지가 이미 처리 되었거나 더 이상 유효하지 않아서 MMS Relay/Serve가 처리할 수 없음. / "3001"
            case LgtProtocol.MESSAGE_REJECTED:                    // 서버에서 메시지를 받아들일 수 없음 / "3002"
            case LgtProtocol.MULTIPLE_ADDRESSED_NOT_SUPPORTED:    // MMS Relay/Server가 multiple recipients를 지원하지 않음 / "3003"
            case LgtProtocol.GENERAL_SERVICE_ERROR:               // 요구된 서비스가 실행될 수 없음 / "4000"
            case LgtProtocol.IMPROPER_IDENTIFICATION:             // 메시지의 Identification header가 client를 확인할 수 없음 (VASP나 MMS Relay/Server) / "4001"
            case LgtProtocol.UNSUPPORTED_VERSION:                 // 메시지에 있는 MM7 version 이 지원되지 않는 version임 / "4002"
            case LgtProtocol.UNSUPPORTED_OPERATION:               // 메시지 헤더에 있는 Message Type 이 서버에서 지원되지 않음 / "4003"
            case LgtProtocol.VALIDATION_ERROR:                    // 필수적인 field가 빠졌거나 message-format 이 맞지 않아 XML로 된 SOAP 메시지를 parsing할 수 없음 / "4004"
            case LgtProtocol.SERVICE_ERROR:                       // 서버(MMS Relay/Server 나 VASP)에서 메시지 처리에 실패하여 재전송 할 수 없음. / "4005"
            case LgtProtocol.KISACODE_ERROR:                      // 최초 발신 사업자 코드가 규격에 맞지 않아 문자 전송 불가능 / "4032"
            case LgtProtocol.SUBS_REJECT:                         // 수신자가 착신거절 신청자임 / "6014"
            case LgtProtocol.INVALID_AUTH_PASSWORD:               // CP 보안 인증 실패 / "6024"
            case LgtProtocol.EXPIRED_PASSWORD:                    // CP 인증 Password 유효 기간 만료 / "6025"
            case LgtProtocol.MMS_DISABLE_SUBS:                    // MMS 비가용 단말 / "6072"
            case LgtProtocol.SUBS_IS_PORTED:                      // PORT-OUT
                return NAckType.ACK;

            case LgtProtocol.SERVICE_UNAVAILBALE:                 // 서비스가 일시적으로 되지 않음. 서버에서 응답이 없음 / "4006"
            case LgtProtocol.SERVICE_DENIED:                      // Client에게 요청된 작업에 대한 허가가 나있지 않음 / "4007"
            case LgtProtocol.SYSTEM_ERROR:                        // 서버 과부하로 인해 일시적으로 메시지 수신불가, 해당 VASID로 문자 전송을 중지하고 1분 이후 재전송 해야 함 / "4030"
                return NAckType.NACK;

            case LgtProtocol.TRAFFIC_IS_OVER:                     // 1:1 메시지 전송 시 허용된 트래픽을 초과 하여 전송 하는 경우 / "7103"
                return NAckType.NACK;

            default:
                return NAckType.ACK;
        }
    }

//    public static NAckType getNAckTypeSubmitAckStatusCode(String statusCode) {
//        switch (statusCode) {
//            // 성공건
//            case LgtProtocol.SUCCESS:
//            case LgtProtocol.PARTIAL_SUCCESS:
//                return NAckType.ACK;
//
//            // 전송가능 TPS 초과로 인한 재처리
//            case LgtProtocol.TRAFFIC_IS_OVER:
//                return NAckType.NACK;
//
//            // 메세지 형식 오류
//            case LgtProtocol.ADDRESS_ERROR:
//            case LgtProtocol.ADDRESS_NOT_FOUND:
//            case LgtProtocol.MULTI_MEDIA_CONTENT_REFUSED:
//            case LgtProtocol.MESSAGE_ID_NOT_FOUND:
//            case LgtProtocol.LINKED_ID_NOT_FOUND:
//            case LgtProtocol.MESSAGE_FORMAT_CORRUPT:
//            case LgtProtocol.IMPROPER_IDENTIFICATION:
//            case LgtProtocol.UNSUPPORTED_VERSION:
//            case LgtProtocol.UNSUPPORTED_OPERATION:
//            case LgtProtocol.VALIDATION_ERROR:
//            case LgtProtocol.KISACODE_ERROR:
//                return NAckType.ACK;
//
//            // CID 또는 발송 IP 설정 실수로 인한 재처리
//            case LgtProtocol.INVALID_AUTH_PASSWORD:
//            case LgtProtocol.EXPIRED_PASSWORD:
//                return NAckType.NACK;
//
//            // 수신자 착신 거절 신청자 또는 MMS 비가용 단말
//            case LgtProtocol.SUBS_REJECT:
//            case LgtProtocol.MMS_DISABLE_SUBS:
//                return NAckType.ACK;
//
//            // 이통사 서버 문제 / 재전송
//            case LgtProtocol.SERVICE_UNAVAILBALE:
//            case LgtProtocol.SYSTEM_ERROR:
//                return NAckType.NACK;
//
//            // 이통사 서버 문제 / 싪패처리
//            case LgtProtocol.CLIENT_ERROR:
//            case LgtProtocol.NOT_POSSIBLE:
//            case LgtProtocol.MESSAGE_REJECTED:
//            case LgtProtocol.MULTIPLE_ADDRESSED_NOT_SUPPORTED:
//            case LgtProtocol.GENERAL_SERVICE_ERROR:
//            case LgtProtocol.SERVICE_DENIED:
//            case LgtProtocol.OPERATION_RESTRICTED:
//            case LgtProtocol.SERVER_ERROR:
//            default:
//                return NAckType.ACK;
//        }
//    }

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
            case "6102":
                return LgtProtocol.SUBS_IS_PORTED;
            case "7103":
                return LgtProtocol.TRAFFIC_IS_OVER;

            default:
            case "3000":
                return LgtProtocol.SERVER_ERROR;
        }
    }


}
