package com.nse.hello;

import com.nse.constants.NSEConstant;
import com.nse.utils.file.DateUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReactiveTest {

    public static void main(String[] args) {
        //normalReactive();

        double input = 596.0;
        double strikeGap = 5;
        System.out.println(input%strikeGap);
        System.out.println(input - (input%strikeGap));
        System.out.println("Hello");
    }

    public static void normalWhile(){//600 to 780
        long start = System.currentTimeMillis();
        String fromDate = "01022022";
        LocalDate from = DateUtils.converStringToDate(fromDate, NSEConstant.DATE_FORMAT);
        String toDate = "01022023";
        LocalDate to = DateUtils.converStringToDate(toDate, NSEConstant.DATE_FORMAT);
        while (from.isBefore(to) || from.isEqual(to)) {
            String stringDate = DateUtils.getDateStringLocaDate(from, NSEConstant.DATE_FORMAT);
            System.out.println(from);
            from = from.plusDays(1);
        }
        System.out.println("Program finished in " + (System.currentTimeMillis() - start));
    }


    public static void normalLoop(){ // 600 680
        long start = System.currentTimeMillis();
        String fromDate = "01022022";
        LocalDate from = DateUtils.converStringToDate(fromDate, NSEConstant.DATE_FORMAT);
        String toDate = "01022023";
        LocalDate to = DateUtils.converStringToDate(toDate, NSEConstant.DATE_FORMAT);
        List<String> dates = new ArrayList<>();
        while (from.isBefore(to) || from.isEqual(to)) {
            String stringDate = DateUtils.getDateStringLocaDate(from, NSEConstant.DATE_FORMAT);
            dates.add(stringDate);
            from = from.plusDays(1);
        }
        dates.stream().forEach(System.out::println);
        System.out.println("Program finished in " + (System.currentTimeMillis() - start));
    }

    public static void normalReactive(){ // 600 680
        long start = System.currentTimeMillis();
        String fromDate = "01022022";
        LocalDate from = DateUtils.converStringToDate(fromDate, NSEConstant.DATE_FORMAT);
        String toDate = "01022023";
        LocalDate to = DateUtils.converStringToDate(toDate, NSEConstant.DATE_FORMAT);
        List<String> dates = new ArrayList<>();
        while (from.isBefore(to) || from.isEqual(to)) {
            String stringDate = DateUtils.getDateStringLocaDate(from, NSEConstant.DATE_FORMAT);
            dates.add(stringDate);
            from = from.plusDays(1);
        }
        Flux.fromIterable(dates).collectList().block().forEach(System.out::println);


    //dates.stream().forEach(System.out::println);
        System.out.println("Program finished in " + (System.currentTimeMillis() - start));
    }

}
