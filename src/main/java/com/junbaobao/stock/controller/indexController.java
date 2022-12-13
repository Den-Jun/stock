package com.junbaobao.stock.controller;

import com.alibaba.fastjson.JSONArray;
import com.junbaobao.stock.entity.NoticeWebsocketResp;
import com.junbaobao.stock.service.StockService1;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    public void getStock() {
        JSONArray stock = noticeWebsocket.getStockInfo("002362", "汉王科技", "1");
        //noticeWebsocket.zhangtingban();
    }


}
