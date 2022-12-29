package com.junbaobao.stock.job;

import com.junbaobao.stock.controller.JyDateController;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class JyDateJob {


    @Resource
    JyDateController jyDateController;

    /**
     * 每月初初始下个月数据
     */
    @Scheduled(cron = "0 1 1 * * *")
    public void initJyDate() {
        jyDateController.savePreMonthJyDate();
    }


}
