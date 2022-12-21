package com.junbaobao.stock.util;

import cn.hutool.http.HttpRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static String toDate(String dateStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.format(date);
    }

    public static void main(String[] args) {
        //获取走势  可以取都是天的 最多5天
        String thisUrl = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f20&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f20&ut=fa5fd1943c7b386f172d6893dbfba10b&secid=1.600185&ndays=2&iscr=1&iscca=0";
        String body = HttpRequest.get(thisUrl).execute().body();
        System.out.println(body);
    }
}
