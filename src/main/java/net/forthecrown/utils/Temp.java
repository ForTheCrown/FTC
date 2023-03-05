package net.forthecrown.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Temp {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d LLL yyyy");
    private static final ZoneId zoneId = ZoneId.systemDefault();

    public static String localDateToString(LocalDate date) {
        return date.format(dateFormatter);
    }

    public static LocalDate localDateFromString(String date) {
        return LocalDate.parse(date, dateFormatter);
    }

    public static LocalDate localDateFromLong(long timeStamp) {
        return Instant.ofEpochSecond(timeStamp).atZone(zoneId).toLocalDate();
    }

    public static long localDateToLong(LocalDate date) {
        return date.atStartOfDay(zoneId).toEpochSecond();
    }

    public static void main(String[] args) {
        LocalDate now = LocalDate.now();
        System.out.println(localDateToString(now));
        System.out.println(localDateFromString(localDateToString(now)));
    }
}
