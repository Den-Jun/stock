package com.junbaobao.stock.model.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ShareDayData {

    /**
     * id
     */
    private String id;

    /**
     * 股票代码
     */
    private String code;

    /**
     * 股票名称
     */
    private String shareName;

    /**
     * 行业板块
     */
    private String hybk;

    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 数据日期
     */
    private String dataTime;

    private String secId;


    /**
     * '连板天数'
     */
    private int continuityDay;

    /**
     * '未匹配量'
     */
    private BigDecimal unmatchedVolume;
    /**
     * '竞价量'
     */
    private BigDecimal biddingVolume;

    /**
     * '第一分钟成交量'
     */
    private BigDecimal firstMinuteVolume;

    /**
     * '今日分时最大量'
     */
    private BigDecimal todayMinterMax;

    /**
     * '总成交量'
     */
    private BigDecimal totalVolume;
    /**
     * '封单大小'
     */
    private BigDecimal sealedVolume;
    /**
     * '竞价十分钟的平均每一分钟交易量'
     */
    private BigDecimal biddingMinuteAverage;
    /**
     * '五天平均每一分钟交易量'
     */
    private BigDecimal fiveDayAverageMinutes;
    /**
     * ''昨日总成交量''
     */
    private BigDecimal yesterdayTotalVolume;
    /**
     * '一年最大量'
     */
    private BigDecimal oneYearMax;

}
