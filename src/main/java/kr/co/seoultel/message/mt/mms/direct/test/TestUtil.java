package kr.co.seoultel.message.mt.mms.direct.test;


import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TestUtil {

    public static String generateMsgId() {    // 20자리 msgId 생성
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmssSSS");
        String dateStr = sdf.format(new Date());

        Random random = new Random();
        int randomInt = random.nextInt(99999); // Generate a random number between 0 and 99999

        return dateStr + String.format("%05d", randomInt); // Concatenate the two strings
    }

    private static final String NUMBERS = "0123456789";

    public static String generateRandomNumberString(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(NUMBERS.length());
            char randomChar = NUMBERS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }

    public static String generateUniqueNumberString() {
        Set<String> existingStrings = new HashSet<>();
        while (true) {
            String randomNumberString = generateRandomNumberString(18 + new Random().nextInt(3)); // 18~20자리
            if (!existingStrings.contains(randomNumberString)) {
                existingStrings.add(randomNumberString);
                return randomNumberString;
            }
        }
    }

    public static String getDate(int plusSecond) {
        LocalDateTime future = LocalDateTime.now().plusSeconds(plusSecond);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

        return future.format(formatter);
    }

}
