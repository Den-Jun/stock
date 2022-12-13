package com.junbaobao.stock.service;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * @Classname StockService
 * @Description
 * @Date: Created in 2022/12/5 10:36
 * @Author Name:
 */
@Component
public  class StockService1  {

    /**
     * 根据页数获取所有股票list
     */
    public void getStock() {
//        String url = "https://push2.eastmoney.com/api/qt/clist/get";
//
//        HashMap<String, Object> paramMap = new HashMap<>();
//
//        HashMap<String, String> headerMap = new HashMap<>();
//        //显示的栏
//        paramMap.put("fields", "f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152");
//        //每页多少
//        paramMap.put("pz", 1000);
//        //当前页数
//        paramMap.put("pn", 1);
//        //
//        paramMap.put("fs", "m:0%20t:6,m:0%20t:80,m:1%20t:2,m:1%20t:23,m:0%20t:81%20s:2048");
//
//        headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
//
//        headerMap.put("Accept-Language","zh-CN,zh;q=0.9,zh-TW;q=0.8,en-US;q=0.7,en;q=0.6");

        String url = "https://push2.eastmoney.com/api/qt/clist/get?fields=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152&pz=1000&pn=1&fs=m:0%20t:6,m:0%20t:80,m:1%20t:2,m:1%20t:23,m:0%20t:81%20s:2048";

        String body =  HttpRequest.get(url).execute().body();

        JSONObject jsonObject = JSONUtil.parseObj(body);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject diff = data.getJSONObject("diff");

        System.out.println(jsonObject);
    }


}
