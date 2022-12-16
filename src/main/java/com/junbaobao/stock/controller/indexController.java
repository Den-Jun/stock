package com.junbaobao.stock.controller;

import com.alibaba.fastjson.JSONArray;
import com.junbaobao.stock.entity.NoticeWebsocketResp;
import com.junbaobao.stock.service.StockService1;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


    @GetMapping("/a")
    public void getStock(@RequestParam("cookie")String cookie) {
        //JSONArray stock = noticeWebsocket.getStockInfo("002362", "汉王科技", "1");
        //noticeWebsocket.zhangtingban();

        List<String> stockList = noticeWebsocket.getOptional(cookie);
        for (String stockId : stockList){
            JSONArray stock = noticeWebsocket.getStockInfo(stockId);
        }
    }


}
