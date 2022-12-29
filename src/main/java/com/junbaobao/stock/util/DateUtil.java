package com.junbaobao.stock.util;

import cn.hutool.http.HttpRequest;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String toDate(String dateStr, String format) {
        SimpleDateFormat  dateFormat = new SimpleDateFormat (format);
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.format(date);
    }

    public static String toDate(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String str = sdf.format(date);
        return str;
    }



}
