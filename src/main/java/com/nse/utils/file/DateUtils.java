package com.nse.utils.file;

import com.nse.constants.NSEConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DateUtils {
    final private static Logger LOGGER = LoggerFactory.getLogger(DateUtils.class);

    public static List<String> getLastMonthDatesTillGivenDate(LocalDate fromDate, LocalDate tillDate, String dateFormat){
        List<String> dates = new ArrayList<>();
        while (fromDate.isBefore(tillDate) || fromDate.isEqual(tillDate)) {
            dates.add(fromDate.format(DateTimeFormatter.ofPattern(dateFormat)));
            fromDate = fromDate.plusDays(1);
        }
        LOGGER.info("Final  dates are: {}", dates);
        return dates;
    }


    public static LocalDate converStringToDate(String originalDate, String format) {
        try {
            DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
            builder.parseCaseInsensitive();
            builder.appendPattern(format);
            DateTimeFormatter dateFormat = builder.toFormatter();
            return LocalDate.parse(originalDate, dateFormat);
        } catch (Exception e) {
           // e.printStackTrace();

            if(originalDate.contains("Sep")){
                originalDate = originalDate.replaceAll("Sep", "Sept");
            }
            DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder();
            builder.parseCaseInsensitive();
            builder.appendPattern(format);
            DateTimeFormatter dateFormat = builder.toFormatter();
            return LocalDate.parse(originalDate, dateFormat);
        }
    }

    public static String getDateStringForGivenFormat(String date, String fromFormat, String toFormat) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(fromFormat));
        return localDate.format(DateTimeFormatter.ofPattern(toFormat));
    }

    public static Date getDateFromGivenFormat(String date, String fromFormat) {
        return  Date.from(LocalDate.parse(date, DateTimeFormatter.ofPattern(fromFormat)).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static String getDateStringLocaDate(LocalDate date, String toFormat) {
        return date.format(DateTimeFormatter.ofPattern(toFormat));
    }

    public static List<String> getDatesBetweenDate(String fromDate, String toDate, String toFormat){
        List<String> dates = new ArrayList<>();
        LocalDate from = DateUtils.converStringToDate(fromDate, NSEConstant.DATE_FORMAT);
        LocalDate to = DateUtils.converStringToDate(toDate, NSEConstant.DATE_FORMAT);
        while (from.isBefore(to) || from.isEqual(to)) {
            dates.add(DateUtils.getDateStringLocaDate(from, toFormat));
           // System.out.println(DateUtils.getDateStringLocaDate(from, toFormat));
            from = from.plusDays(1);
        }
        return dates;
    }

    public static List<String> getDatesBetweenDate(LocalDate fromDate, LocalDate toDate, String toFormat){
        List<String> dates = new ArrayList<>();
        LocalDate from = fromDate;
        LocalDate to = toDate;
        while (from.isBefore(to) || from.isEqual(to)) {
            dates.add(DateUtils.getDateStringLocaDate(from, toFormat));
            // System.out.println(DateUtils.getDateStringLocaDate(from, toFormat));
            from = from.plusDays(1);
        }
        return dates;
    }

    public static List<String> getDatesBetweenDate(String fromDate, String toDate, String fromFormat, String toFormat){
        List<String> dates = new ArrayList<>();
        LocalDate from = DateUtils.converStringToDate(fromDate, fromFormat);
        LocalDate to = DateUtils.converStringToDate(toDate, fromFormat);
        while (from.isBefore(to) || from.isEqual(to)) {
            dates.add(DateUtils.getDateStringLocaDate(from, toFormat));
            //System.out.println(DateUtils.getDateStringLocaDate(from, toFormat));
            from = from.plusDays(1);
        }
        return dates;
    }



    public static void main(String[] args) {

    }
    public static void main1(String[] args) {
        String date = "16042021";
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(NSEConstant.DATE_FORMAT));
        System.out.println("Day : " + localDate.getDayOfMonth());
        System.out.println("Month : " + localDate.getMonth());
        System.out.println("Year : " + localDate.getYear());
        //https://www1.nseindia.com/content/historical/DERIVATIVES/2021/FEB/fo01FEB2021bhav.csv.zip
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        String text = localDate.format(formatter);
        LocalDate parsedDate = LocalDate.parse(text, formatter);
        String year = getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "yyyy");
        String month = getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "MMM").toUpperCase();
        String fullDate = getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "ddMMMyyyy").toUpperCase();
        String res = String.format(NSEConstant.OPTIONS_DATA_URL, year, month, fullDate);
        System.out.println(res);
    }
}