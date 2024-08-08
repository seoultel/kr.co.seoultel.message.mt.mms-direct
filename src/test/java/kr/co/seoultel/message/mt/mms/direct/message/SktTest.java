package kr.co.seoultel.message.mt.mms.direct.message;

import kr.co.seoultel.message.mt.mms.core.common.exceptions.message.soap.MCMPSoapRenderException;
import kr.co.seoultel.message.mt.mms.core.messages.direct.skt.SktSubmitResMessage;

public class SktTest {
    public static void main(String[] args) throws MCMPSoapRenderException {
        String xml = "<?xml version=\"1.0\" encoding=\"euc-kr\"?><env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\"><env:Header><mm7:TransactionID xmlns:mm7=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2\" env:mustUnderstand=\"1\">tid </mm7:TransactionID></env:Header><env:Body><mm7:RSErrorRsp xmlns:mm7=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-2\"><StatusText>SUBS is ported (SKT)</StatusText><MM7Version>5.3.0</MM7Version><StatusCode>6102</StatusCode></mm7:RSErrorRsp></env:Body></env:Envelope>";
        SktSubmitResMessage sktSubmitResMessage = new SktSubmitResMessage();
        sktSubmitResMessage.fromXml(xml);
    }
}
