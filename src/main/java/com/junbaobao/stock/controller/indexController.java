package com.junbaobao.stock.controller;

import com.alibaba.fastjson.JSONArray;
import com.junbaobao.stock.entity.NoticeWebsocketResp;
import com.junbaobao.stock.entity.StockInfo;
import com.junbaobao.stock.service.StockService1;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname indexController
 * @Description
 * @Date: Created in 2022/12/5 11:55
 * @Author Name:
 */
@RestController
@RequestMapping("/index")
public class indexController {

    @Resource
    NoticeWebsocket noticeWebsocket;


    @Resource
    StockService1 stockService1;


    @GetMapping("/a")
    public void getStock(@RequestParam("cookie")String cookie) {
        //JSONArray stock = noticeWebsocket.getStockInfo("002362", "汉王科技", "1");
        //noticeWebsocket.zhangtingban();

        List<String> stockList = noticeWebsocket.getOptional(cookie);
        List<StockInfo> stockInfo = noticeWebsocket.getStockInfo(stockList);
    }

    @GetMapping("/add")
    public void addStock(@RequestParam("stockcode") String stockcode, @RequestHeader("Cookie")String cookie){
        stockService1.addStock(cookie,stockcode);
    }

}
