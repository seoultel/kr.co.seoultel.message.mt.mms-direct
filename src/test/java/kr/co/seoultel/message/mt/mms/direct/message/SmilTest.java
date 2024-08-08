package kr.co.seoultel.message.mt.mms.direct.message;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;

public class SmilTest {
    public static void main(String[] args) throws MessagingException {
        String test = "<smil xmlns=\"http://www.w3.org/2001/SMIL20/Language\"><head><meta content=\"4.0\" name=\"mms_skt_version\"/><layout><root-layout background-color=\"#FFFFFF\" height=\"377\" width=\"220\"><region height=\"21%\" id=\"text\" left=\"0%\" top=\"79%\" width=\"100%\" z-index=\"0\"/></root-layout></layout></head><body><par repeatCount=\"indefinite\"/><color1 region=\"text\" repeatCount=\"indefinite\" src=\"cid:20240704153446_8393159\"><param name=\"style\" value=\"scroll\" valuetype=\"data\"/></color1></body></smil>";
        String test2 = test.replaceAll("><", ">\r\n<");
        System.out.println(test2);


    }
}
