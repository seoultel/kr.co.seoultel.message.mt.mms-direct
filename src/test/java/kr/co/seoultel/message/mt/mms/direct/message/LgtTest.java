package kr.co.seoultel.message.mt.mms.direct.message;

import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.messages.direct.lgt.LgtDeliveryReportReqMessage;

public class LgtTest {
    public static void main(String[] args) throws MCMPSoapRenderException {
        String xml = "<?xml version=\"1.0\" encoding=\"euc-kr\"?><env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\"><env:Header><mm7:TransactionID xmlns:mm7=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2\" env:mustUnderstand=\"1\">-675086d4:191221270e3:-25b6</mm7:TransactionID></env:Header><env:Body><mm7:DeliveryReportReq xmlns:mm7=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2\"><MM7Version>5.3.0</MM7Version><MMSRelayServerID>LGT-MMS-Relay</MMSRelayServerID><Sender><Number>01083025800</Number></Sender><MessageID>65_20240805222251_B_412_346</MessageID><Recipients><Number>01054702550</Number></Recipients><TimeStamp>2024-08-05T22:22:54+09:00</TimeStamp><MMStatus>Retrieved</MMStatus><StatusText>OK</StatusText></mm7:DeliveryReportReq></env:Body></env:Envelope>";
        LgtDeliveryReportReqMessage lgtDeliveryReportReqMessage = new LgtDeliveryReportReqMessage();
        lgtDeliveryReportReqMessage.fromXml(xml);

    }
}
