package kr.co.seoultel.message.mt.mms.direct.util.ktf;

import kr.co.seoultel.message.mt.mms.core.common.protocol.KtfProtocol;
import kr.co.seoultel.message.mt.mms.core.util.DateUtil;
import kr.co.seoultel.message.mt.mms.core_module.common.exceptions.rabbitMq.NAckType;
import kr.co.seoultel.message.mt.mms.direct.config.SenderConfig;

public class KtfUtil {

    public static String getRandomTransactionalId(String umsMsgId) {
        return String.join(":", DateUtil.getDate(), SenderConfig.TELECOM.toUpperCase(), umsMsgId);
    }

    public static NAckType getNAckTypeByStatusCode(String statusCode) {
        switch (statusCode) {
            case KtfProtocol.KTF_SUBMIT_ACK_SUCCESS_RESULT:                            //  성공 // "1000"
            case KtfProtocol.KTF_SUBMIT_ACK_CLIENT_ERROR_RESULT:                       //  클라이언트 오류 // "2000"
            case KtfProtocol.KTF_SUBMIT_ACK_MESSAGE_FORMAT_ERROR_RESULT:               //  메시지 포맷 오류 // "2100"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_ERROR_RESULT:                         //  SOAP Part 오류 // "2101"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_PARSING_ERROR_RESULT:                 //  SOAP 포맷 오류 // "2102"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_NOTSUPPORTED_METHOD_RESULT:           //  SOAP에서 미지원 method(MessageType) 오류 // "2103"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_VERSION_ERROR_RESULT:                 //  SOAP Element 중 Version 정보 오류 // "2104"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_MESSAGECLASS_ERROR_RESULT:            //  SOAP Element 중 Message Class 정보 오류 // "2105"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SENDINFO_ERROR_RESULT:                //  SOAP Element인 SenderInfo 정보 오류 // "2106"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_RCPTINFO_ERROR_RESULT:                //  SOAP Element인 Recipients 정보 오류 // "2107"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_HUBID_ERROR_RESULT:                   //  SOAP Element인 VASPID(HUBSP) 정보 오류 // "2108"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_VASID_ERROR_RESULT:                   //  SOAP Element인 VASID(SUBCP) 정보 오류 // "2109"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_CALLBACK_ERROR_RESULT:                //  SOAP Element인 CallbackAddress 정보 오류 // "2110"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SENDER_ERROR_RESULT:                  //  SOAP Element인 Sender 정보 오류 // "2111"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_TIMESTAMP_ERROR_RESULT:               //  SOAP Element인 StampTime 정보 오류 // "2112"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SUBJECT_ERROR_RESULT:                 //  SOAP Element인 Subject 정보 오류 // "2113"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_TRANSACTIONID_ERROR_RESULT:           //  SOAP Element 중 Transaction ID 정보 오류 // "2114"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_MESSAGEID_ERRO_RESULT:                //  SOAP Element 중 Message ID 정보 오류 // "2115"
            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SERVICETYPE_ERROR_RESULT:             //  SOAP Element 중 MessageType 정보 오류 // "2116"
            case KtfProtocol.KTF_SUBMIT_ACK_MIME_ERROR_RESULT:                         //  Mime 메시지 포맷 오류 // "2150"
            case KtfProtocol.KTF_SUBMIT_ACK_BOUNDARY_ERROR_RESULT:                     //  Bounday 오류 // "2151"
            case KtfProtocol.KTF_SUBMIT_ACK_CONTENTTYPE_ERROR_RESULT:                  //  Content-Type 오류 // "2152"
            case KtfProtocol.KTF_SUBMIT_ACK_CONTENT_EMPTY_RESULT:                      //  Content 오류 // "2154"
            case KtfProtocol.KTF_SUBMIT_ACK_ENCODING_ERROR_RESULT:                     //  Content Encoding 오류 // "2155"
            case KtfProtocol.KTF_SUBMIT_ACK_CONTENT_NOTSUPPORTED_RESULT:               //  지원하지 않는 Content 오류 // "2160"
            case KtfProtocol.KTF_SUBMIT_ACK_CONTENT_CONVERSION_NOTSUPPORTED_RESULT:    //  변환을 지원하지 않는 Content 오류 수신단말이 지원할 수 없는  Content 포함이 된 경우 발생 // "2161"
            case KtfProtocol.KTF_SUBMIT_ACK_CONTENTID_ERROR_RESULT:                    //  Content ID 오류 // "2162"
            case KtfProtocol.KTF_SUBMIT_ACK_TRANID_DUPLICATE_ERROR_RESULT:             //  TRANSACTION ID가 중복 // "2163"
            case KtfProtocol.KTF_SUBMIT_ACK_RCPTCNT_OVER_RESULT:                       //  동보 전송 건수 초과 오류 // "4201"
                return NAckType.ACK;

            // 서버 오류
            case KtfProtocol.KTF_SUBMIT_ACK_SERVER_ERROR_RESULT:                       //  서버 오류 // "3000"
            case KtfProtocol.KTF_SUBMIT_ACK_SERVICE_ERROR_RESULT:                      //  서비스 오류 // "4000"
            case KtfProtocol.KTF_SUBMIT_ACK_SERVICE_DENIED_RESULT:                     //  서비스 거부 // "4001"

            // HUBSP 오류
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_AUTH_ERROR_RESULT:                     //  HUBSP 인증 오류 // "4100"
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_NOTFOUND_RESULT:                       //  HUBSP 없음 오류 // "4101"
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_BLOCK_RESULT:                          //  HUBSP 정지 오류 // "4102"
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_EXPIRED_RESULT:                        //  HUBSP 폐기 오류 // "4103"
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_IP_INVALID_RESULT:                     //  HUBSP IP 오류 // "4104"
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_CALLBACK_INVALID_RESULT:               //  HUBSP 회신번호 오류 (안심메시지) // "4105"

            // TPS 초과
            case KtfProtocol.KTF_SUBMIT_ACK_HUB_OVER_INTRAFFIC_RESULT:                 //  허용 트래픽 초과 오류 // "4202"
                return NAckType.NACK;

            case KtfProtocol.KTF_SUBMIT_ACK_HUB_OVER_MESSAGE_RESULT:                   //  SIZE 허용 메시지 SIZE 초과 오류 // "4203"
            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_ERROR_RESULT:                         //  스팸 처리 오류 // "4400"
            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_SUBJECT_RESULT:                       //  제목 스팸 처리 오류 // "4401"
            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_FILENAME_RESULT:                      //  파일명 스팸 처리 오류 // "4402"
            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_SUBCP_RESULT:                         //  SUB CP 스팸 처리 오류 // "4403"
            case KtfProtocol.KTF_SUBMIT_ACK_UNDEFINED_RESULT:                          //  알 수 없는 에러 // "9999"
            default:
                return NAckType.ACK;
        }
    }
//
//
//    public static NAckType getNAckTypeBySubmitAckStatusCode(String statusCode) {
//        switch (statusCode) {
//            // 정상건
//            case KtfProtocol.KTF_SUBMIT_ACK_SUCCESS_RESULT:
//                return NAckType.ACK;
//
//            // TID 중복 인입건
//            case KtfProtocol.KTF_SUBMIT_ACK_TRANID_DUPLICATE_ERROR_RESULT:
//                return NAckType.ACK;
//
//            // 메세지 포맷 에러
//            case KtfProtocol.KTF_SUBMIT_ACK_CLIENT_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_MESSAGE_FORMAT_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_PARSING_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_NOTSUPPORTED_METHOD_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_VERSION_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_MESSAGECLASS_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SENDINFO_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_RCPTINFO_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_HUBID_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_VASID_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_CALLBACK_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SENDER_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_TIMESTAMP_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SUBJECT_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_TRANSACTIONID_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_MESSAGEID_ERRO_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SOAP_SERVICETYPE_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_MIME_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_BOUNDARY_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_CONTENTTYPE_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_CONTENT_EMPTY_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_ENCODING_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_CONTENT_NOTSUPPORTED_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_CONTENT_CONVERSION_NOTSUPPORTED_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_CONTENTID_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_OVER_MESSAGE_RESULT:
//                return NAckType.ACK;
//
//            // 서버 오류로 인한 재처리
//            case KtfProtocol.KTF_SUBMIT_ACK_SERVER_ERROR_RESULT:
//                return NAckType.NACK;
//
//            // 서비스 오류로 인한 재처리
//            case KtfProtocol.KTF_SUBMIT_ACK_SERVICE_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SERVICE_DENIED_RESULT:
//                return NAckType.NACK;
//
//            // HUBSP 인증관련 오류로 인한 재처리
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_AUTH_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_NOTFOUND_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_BLOCK_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_EXPIRED_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_IP_INVALID_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_CALLBACK_INVALID_RESULT:
//                return NAckType.NACK;
//
//            // 동보 전송 건수 초과
//            case KtfProtocol.KTF_SUBMIT_ACK_RCPTCNT_OVER_RESULT:
//                return NAckType.ACK;
//
//            // 전송가능 TPS 초과로 인한 재처리
//            case KtfProtocol.KTF_SUBMIT_ACK_HUB_OVER_INTRAFFIC_RESULT:
//                return NAckType.NACK;
//
//            // 스팸으로 인한 실패
//            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_ERROR_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_SUBJECT_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_FILENAME_RESULT:
//            case KtfProtocol.KTF_SUBMIT_ACK_SPAM_SUBCP_RESULT:
//                return NAckType.ACK;
//
//            // 실패 원인을 알 수 없어 실패 처리
//            case KtfProtocol.KTF_SUBMIT_ACK_UNDEFINED_RESULT:
//            default:
//                return NAckType.ACK;
//        }
//    }

    public static String getSubmitAckStatusCodeKor(String statusCode) {
        switch (statusCode) {
            case "1000":
                return "SUCCESS_RESULT";
            case "2000":
                return "CLIENT_ERROR_RESULT";
            case "2100":
                return "MESSAGE_FORMAT_ERROR_RESULT";
            case "2101":
                return "SOAP_ERROR_RESULT";
            case "2102":
                return "SOAP_PARSING_ERROR_RESULT";
            case "2103":
                return "SOAP_NOTSUPPORTED_METHOD_RESULT";
            case "2104":
                return "SOAP_VERSION_ERROR_RESULT";
            case "2105":
                return "SOAP_MESSAGECLASS_ERROR_RESULT";
            case "2106":
                return "SOAP_SENDINFO_ERROR_RESULT";
            case "2107":
                return "SOAP_RCPTINFO_ERROR_RESULT";
            case "2108":
                return "SOAP_HUBID_ERROR_RESULT";
            case "2109":
                return "SOAP_VASID_ERROR_RESULT";
            case "2110":
                return "SOAP_CALLBACK_ERROR_RESULT";
            case "2111":
                return "SOAP_SENDER_ERROR_RESULT";
            case "2112":
                return "SOAP_TIMESTAMP_ERROR_RESULT";
            case "2113":
                return "SOAP_SUBJECT_ERROR_RESULT";
            case "2114":
                return "SOAP_TRANSACTIONID_ERROR_RESULT";
            case "2115":
                return "SOAP_MESSAGEID_ERRO_RESULT";
            case "2116":
                return "SOAP_SERVICETYPE_ERROR_RESULT";
            case "2150":
                return "MIME_ERROR_RESULT";
            case "2151":
                return "BOUNDARY_ERROR_RESULT";
            case "2152":
                return "CONTENTTYPE_ERROR_RESULT";
            case "2154":
                return "CONTENT_EMPTY_RESULT";
            case "2155":
                return "ENCODING_ERROR_RESULT";
            case "2160":
                return "CONTENT_NOTSUPPORTED_RESULT";
            case "2161":
                return "CONTENT_CONVERSION_NOTSUPPORTED_RESULT";
            case "2162":
                return "CONTENTID_ERROR_RESULT";
            case "2163":
                return "TRANID_DUPLICATE_ERROR_RESULT";
            case "3000":
                return "SERVER_ERROR_RESULT";
            case "4000":
                return "SERVICE_ERROR_RESULT";
            case "4001":
                return "SERVICE_DENIED_RESULT";
            case "4100":
                return "HUB_AUTH_ERROR_RESULT";
            case "4101":
                return "HUB_NOTFOUND_RESULT";
            case "4102":
                return "HUB_BLOCK_RESULT";
            case "4103":
                return "HUB_EXPIRED_RESULT";
            case "4104":
                return "HUB_IP_INVALID_RESULT";
            case "4105":
                return "HUB_CALLBACK_INVALID_RESULT";
            case "4201":
                return "RCPTCNT_OVER_RESULT";
            case "4202":
                return "HUB_OVER_INTRAFFIC_RESULT";
            case "4203":
                return "HUB_OVER_MESSAGE_RESULT";
            case "4400":
                return "SPAM_ERROR_RESULT";
            case "4401":
                return "SPAM_SUBJECT_RESULT";
            case "4402":
                return "SPAM_FILENAME_RESULT";
            case "4403":
                return "SPAM_SUBCP_RESULT";
            case "9999":
            default:
                return "UNDEFINED_RESULT";
        }
    }

    public static String getReportStatusCodeKor(String statusCode) {
        switch (statusCode) {
            case "1000":
                return "SUCCESS";
            case "1001":
                return "SUCCESS_SPAM";
            case "1002":
                return "SUCCESS_ANSIM";
            case "2000":
                return "CLIENT_ERROR";
            case "2100":
                return "MESSAGE_FORMAT_ERROR";
            case "2101":
                return "SOAP_ERROR";
            case "2102":
                return "SOAP_PARSING_ERROR";
            case "2103":
                return "SOAP_NOTSUPPORTED_METHOD";
            case "2104":
                return "SOAP_VERSION_ERROR";
            case "2105":
                return "SOAP_MESSAGECLASS_ERROR";
            case "2106":
                return "SOAP_SENDINFO_ERROR";
            case "2107":
                return "SOAP_RCPTINFO_ERROR";
            case "2108":
                return "SOAP_HUBID_ERROR";
            case "2109":
                return "SOAP_VASID_ERROR";
            case "2110":
                return "SOAP_CALLBACK_ERROR";
            case "2111":
                return "SOAP_SENDER_ERROR";
            case "2112":
                return "SOAP_TIMESTAMP_ERROR";
            case "2113":
                return "SOAP_SUBJECT_ERROR";
            case "2114":
                return "SOAP_TRANSACTIONID_ERROR";
            case "2115":
                return "SOAP_MESSAGEID_ERROR";
            case "2116":
                return "SOAP_SERVICETYPE_ERROR";
            case "2150":
                return "MIME_ERROR";
            case "2151":
                return "BOUNDARY_ERROR";
            case "2152":
                return "CONTENTTYPE_ERROR";
            case "2154":
                return "CONTENT_EMPTY";
            case "2155":
                return "ENCODING_ERROR";
            case "2160":
                return "CONTENT_NOTSUPPORTED";
            case "2161":
                return "CONTENT_CONVERSION_NOT";
            case "2162":
                return "CONTENTID_ERROR";
            case "2163":
                return "TRANID_DUPLICATE_ERROR";
            case "2200":
                return "RESELLERCODE_ERROR";
            case "3000":
                return "SERVER_ERROR";
            case "3400":
                return "LMSC_NETWORK_PROBLEM";
            case "3505":
                return "LMSC_NO_HEADER";
            case "4000":
                return "SERVICE_ERROR";
            case "4001":
                return "SERVICE_DENIED";
            case "4100":
                return "HUB_AUTH_ERROR";
            case "4101":
                return "HUB_NOTFOUND";
            case "4102":
                return "HUB_BLOCK";
            case "4103":
                return "HUB_EXPIRED";
            case "4104":
                return "HUB_IP_INVALID";
            case "4200":
                return "SERVICE_LIMIT";
            case "4201":
                return "RCPTCNT_OVER";
            case "4202":
                return "HUB_OVER_INTRAFFIC";
            case "4203":
                return "HUB_OVER_MESSAGESIZE";
            case "4300":
                return "SUBS_ERROR";
            case "4301":
                return "SUBS_INVALID";
            case "4302":
                return "SUBS_PORTED_SKT";
            case "4303":
                return "SUBS_PORTED_LGT";
            case "4304":
                return "SUBS_ADULT_AUTH_FAIL";
            case "4305":
                return "SUBS_MMS_NOTSUPPORTED";
            case "4306":
                return "SUBS_DUPLICATION";
            case "4307":
                return "SUBS_BLOCK";
            case "4400":
                return "SPAM_ERROR";
            case "4401":
                return "SPAM_SUBJECT";
            case "4402":
                return "SPAM_FILENAME";
            case "4403":
                return "SPAM_SUBCP";
            case "4404":
                return "SPAM_CALLBACK";
            case "5200":
                return "LMSCDR_UNDEFINED";
            case "5300":
                return "LMCSDR_TERMINAL_ERROR";
            case "5310":
                return "LMSCDR_TERMINAL_OUT_OF_MESSAGE_SIZE";
            case "5320":
                return "LMSCDR_TERMINAL_OUT_OF_MEMORY";
            case "5330":
                return "LMSCDR_PULL_OUTOFTIME";
            case "5401":
                return "LMSCDR_SEND_AUTH_FAIL";
            case "5403":
                return "LMSCDR_SEND_OUT_OF_MAX_RETRY_MAX";
            case "5409":
                return "LMSCDR_SEND_SENDER_BLOCK";
            case "3430":
                return "LMSC_MESSAGE_FORMAT_ERROR";
            case "7400":
                return "MMSC_NETWORK_PROBLEM";
            case "7505":
                return "MMSC_NO_HEADER";
            case "8200":
                return "MMSCDR_UNDEFINED_MMSC";
            case "8300":
                return "MMCSDR_TERMINAL_ERROR";
            case "8310":
                return "MMSCDR_TERMINAL_OUT_OF_MESSAGE_SIZE";
            case "8320":
                return "MMSCDR_TERMINAL_OUT_OF_MEMORY";
            case "8330":
                return "MMSCDR_PULL_OUTOFTIME";
            case "8401":
                return "MMSCDR_SEND_AUTH_FAIL";
            case "8403":
                return "MMSCDR_SEND_OUT_OF_MAX_RETRY_MAX";
            case "8408":
                return "MMSCDR_SEND_SENDER_BLOCK";
            case "9999":
            default:
                return "UNDEFINED";
        }
    }
}
