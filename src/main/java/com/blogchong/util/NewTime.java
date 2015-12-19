package com.blogchong.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Author:  blogchong
 * Blog:    www.blogchong.com
 * Mailbox: blogchong@163.com
 * QQGroup: 191321336
 * Weixin:  blogchong
 * Data:    2015/7/29
 * Describe:时间转换工具
 */
public class NewTime {

    public static String type = "yyyy-MM-dd-HH-mm-ss";

    public static void main(String[] args) throws Exception {

        long interval = 32;

        Date dateDate = new Date();

        String strDate = dateToString(dateDate, type);

        Long longDate = stringToLong(strDate, type);

        Long longDate2 = longDate - (interval * 24 * 60 * 60 * 1000);
        String strDate2 = longToString(longDate2, type);


        System.out.println("初始时间1：" + strDate);
        System.out.println("初始时间2：" + strDate2);
        //System.out.println("差：" + (stringToLong(strDate, type) - stringToLong(testDate, type)));

//        System.out.println("初始时间1：" + stringToLong(strDate, type));
//        System.out.println("初始时间2：" + stringToLong(testDate, type));
//        System.out.println("差：" + (stringToLong(strDate, type) - stringToLong(testDate, type)));


//        System.out.println(nowTime);
//        SimpleDateFormat time=new SimpleDateFormat("yyyy-MM-dd");
//        System.out.println(time.format(nowTime));
    }

    // date类型转换为String类型
    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    // long类型转换为String类型
    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    public static String longToString(long currentTime, String formatType)
            throws ParseException {
        Date date = longToDate(currentTime, formatType); // long类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // string类型转换为date类型
    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    // long转换为Date类型
    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    // string类型转换为long类型
    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    // date类型转换为long类型
    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }

}
