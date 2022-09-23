package com.spintly.base.utilities;


import com.spintly.base.enums.EnumCollection.CharacterTypeSets;
import com.google.common.base.CharMatcher;
import com.spintly.base.support.logger.LogUtility;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public final class RandomDataGenerator {
    private static LogUtility logger = new LogUtility(RandomDataGenerator.class);

    static Integer RANDOM_LENGTH = 6;

    public static String getData(String inputString, Integer... limit) {
        Object output = null;
        String strOutput;
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
        Integer RandomSize = limit.length > 0 ? limit[0] : RANDOM_LENGTH;

        switch (inputString) {
            case "{RANDOM_STRING}":
                strOutput = RandomDataGenerator.random(RandomSize, CharacterTypeSets.ALPHABETS);
                break;
            case "{RANDOM_APLHANUM}":
                strOutput = RandomDataGenerator.random(RandomSize, CharacterTypeSets.ALPHANUMERIC);
                break;
            case "{RANDOM_EMAIL}":
                strOutput = RandomDataGenerator.randomEmailAddress(RandomSize);
                break;
            case "{RANDOM_PASTDATE}":
                strOutput = fmt.print(RandomDataGenerator.randomDOB());
                break;
            case "{RANDOM_NUM}":
                strOutput = RandomDataGenerator.random(RandomSize, CharacterTypeSets.NUMERIC);
                break;
            case "{RANDOM_PHONE_NUM}":
                double a = (((Math.random() * (9999 - 7000)) + 7000)) * 1000;
                String number = String.valueOf(a);
                String aa = number.substring(0, 7);
                String bb = number.substring(8, 11);
                strOutput = aa.concat(bb);
                break;
            case "{RANDOM_NUM_IN_Range}":
                strOutput = RandomDataGenerator.randomIntegerInRange(1, RandomSize).toString();
                break;
            default:
                strOutput = inputString;
        }
        return strOutput;
    }

    private static String random(Integer length, CharacterTypeSets permittedCharacters) {
        String randomString = null;
        if (CharacterTypeSets.ALPHABETS.equals(permittedCharacters)) {
            randomString = randomString(length);
        } else if (CharacterTypeSets.NUMERIC.equals(permittedCharacters)) {
            randomString = randomInteger(length);
        } else if (CharacterTypeSets.ALPHANUMERIC.equals(permittedCharacters)) {
            randomString = randomAlphanumeric(length);
        } else if (CharacterTypeSets.ANY_CHARACTERS.equals(permittedCharacters)) {
            randomString = randomAsciiCharacters(length);
        } else if (CharacterTypeSets.ANY_CHARACTERS_SUPPORTS_MULTILINGUAL.equals(permittedCharacters)) {
            randomString = randomAsciiCharacters(length);
        }
        return randomString;
    }

    private static String randomInteger(Integer length) {
        return RandomStringUtils.randomNumeric(length);
    }

    public static Integer randomIntegerInRange(Integer startRange, Integer endRange) {
        return RandomUtils.nextInt(startRange, startRange);
    }

    private static String randomString(Integer length) {
        return RandomStringUtils.random(length, true, false);
    }

    private static String randomAlphanumeric(Integer length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    public static String randomAlphabetic(Integer length) {
        return RandomStringUtils.randomAlphabetic(length);
    }

    public static String randomEmailAddress(Integer length) {
        String email = randomAlphanumeric(length);
        return email.toLowerCase();
    }

    public static DateTime randomDOB() {
        DateTime dateTime = new DateTime();
        dateTime = dateTime.minusYears((int) (18 + (Math.random() * ((50 - 18) + 1))));
        return dateTime.minusYears((int) (18 + (Math.random() * ((50 - 18) + 1))));
    }

    public static String dateWithNoLeadingZero(String dateWithLeadingZero) {
        String dateWithNoLeadingZero;
        dateWithNoLeadingZero = CharMatcher.is('0').trimLeadingFrom(dateWithLeadingZero);
        return dateWithNoLeadingZero;
    }

    private static String randomAsciiCharacters(Integer characterAmount) {
        return RandomStringUtils.random(characterAmount, 32, 127, false, false);
    }

}