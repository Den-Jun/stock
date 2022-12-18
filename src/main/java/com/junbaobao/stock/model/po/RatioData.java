package com.junbaobao.stock.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
@TableName("RATIO_DATA")
public class RatioData {

    @TableId(value = "id", type = IdType.ASSIGN_UUID)
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

    @TableField("CREATE_TIME")
    private Date createTime;
    /**
     * 数据日期
     */
    @TableField("DATA_TIME")
    private String dataTime;

    /**
     * 未竞成交比（未匹配量/竞价量）
     */
    @TableField("UNSUCCESSFUL_BIDDING")
    private BigDecimal unsuccessfulBidding;

    /**
     * '竞昨成交比(竞价量/昨日成交量)'
     */
    @TableField("BIDDING_YESTERDAY")
    private BigDecimal biddingYesterday;


    /**
     * '竞价分钟比(竞价十分钟的平均每一分钟交易量/过去五天平均每分钟交易量)'
     */
    @TableField("BIDDING_MINTER")
    private BigDecimal biddingMinter;


    /**
     * '竞价比(今日竞价量/昨日竞价量)'
     */
    @TableField("BIDDING")
    private BigDecimal bidding;

    /**
     * '爆量系数（竞价量/昨日分时最大量）'
     */
    @TableField("EXPLOSIVE_QUANTITY")
    private BigDecimal explosiveQuantity;

    /**
     * '昨日上板系数（昨日最大分时/昨日成交量）'
     */
    @TableField("YESTERDAY_BAN")
    private BigDecimal yesterdayBan;

    /**
     * '竞封比（竞价量/昨日封单量）'
     */
    @TableField("BIDDING_SEALED")
    private BigDecimal biddingSealed;

    /**
     * '昨前比（昨日成交量/前日成交量）'
     */
    @TableField("YESTERDAY_FRONT")
    private BigDecimal yesterdayFront;

    /**
     * '昨竞成交比（昨天竞价量/昨天成交量）'
     */
    @TableField("YESTERDAY_BIDDING")
    private BigDecimal yesterdayBidding;

}
