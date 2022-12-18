package com.junbaobao.stock.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("SHARE_DATA")
public class ShareDate {

    /**
     * id
     */
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * '股票编码'
     */
    @TableField("CODE")
    private String code;

    /**
     * '股票名称'
     */
    @TableField("SHARE_NAME")
    private String shareName;

    /**
     * 创建日期
     */
    @TableField("CREATE_TIME")
    private Date createTime;

    /**
     * 数据日期
     */
    @TableField("DATA_TIME")
    private String dataTime;


    /**
     * '连板天数'
     */
    @TableField("CONTINUITY_DAY")
    private int continuityDay;

    /**
     * '昨日竞价量'
     */
    @TableField("YESTERDAY_BIDDING_VOLUME")
    private BigDecimal yesterdayBiddingVolume;

    /**
     * '今天竞价量'
     */
    @TableField("TODAY_BIDDING_VOLUME")
    private BigDecimal todayBiddingVolume;

    /**
     * '昨日封单大小'
     */
    @TableField("YESTERDAY_SEALED_VOLUME")
    private BigDecimal yesterdaySealedVolume;

    /**
     * '昨日分时最大量',
     */
    @TableField("YESTERDAY_MINTER_MAX")
    private BigDecimal yesterdayMinterMax;

    /**
     * '昨日总成交量''
     */
    @TableField("YESTERDAY_TOTAL")
    private BigDecimal yesterdayTotal;

    /**
     * '今天未匹配量'
     */
    @TableField("TODAY_UNMATCHED")
    private BigDecimal todayUnmatched;

    /**
     * '今天第一分钟成交量'
     */
    @TableField("TODAY_FIRST_MINUTE")
    private BigDecimal todayFirstMinute;

    /**
     * '昨天第一分钟成交量'
     */
    @TableField("YESTERDAY_FIRST_MINUTE")
    private BigDecimal yesterdayFirstMinute;

    /**
     * '过去五天平均每一分钟交易量'
     */
    @TableField("FIVE_DAY_AVERAGE_MINUTES")
    private BigDecimal fiveDayAverageMinutes;

    /**
     * '竞价十分钟的平均每一分钟交易量'
     */
    @TableField("TODAY_BIDDING_MINUTE_AVERAGE")
    private BigDecimal todayBiddingMinuteAverage;

    /**
     * 前天成交量
     */
    @TableField("BEFORE_YESTERDAY")
    private BigDecimal beforeYesterday;


}
