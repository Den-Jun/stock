package com.junbaobao.stock.entity;

import lombok.Data;

/**
 * @Classname StockInfoDo
 * @Description
 * @Date: Created in 2022/12/16 17:13
 * @Author Name:
 */
@Data
public class StockInfo {

    private String stockId;

    private String stockName;


    /**
     * 比值1 今日竞价  > 昨日分时最大*0.5    1 大于 0 等于  -1 小于
     */
    private String ratioOne;

    /**
     * 比值2 今日成交量 / 365天最大成交量>0.5
     */
    private String ratioTwo;

    /**
     * 比值数 昨日最大的成交额 /今日竞价
     */
    private String ratioNumber;

    /**
     * 昨日竞价金额
     */
    private String lastBiddingPrice;

    /**
     * 今日竞价金额
     */
    private String thisBiddingPrice;

    /**
     * 昨日封单大小
     */
    private String lastSealSize;


    /**
     * 昨日分时最大量
     */
    private String lastTimeMax;


    /**
     * 昨日总成交量
     */
    private String lastTotalDeal;



    /**
     * 今天为成交量
     */
    private String thisNoDeal;


    /**
     * 今天第一分钟成交量
     */
    private String thisOneDeal;

    /**
     * 竞价十分钟的平均每一分钟交易量
     */
    private String thisBiddingTenAvgDeal;


    /**
     * 过去五天平均每一分钟交易量
     */
    private String lastFiveAvgOneMinuteDeal;


    /**
     * 前日交易量
     */
    private String lastDeal;


}
