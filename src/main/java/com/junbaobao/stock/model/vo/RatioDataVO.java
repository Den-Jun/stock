package com.junbaobao.stock.model.vo;

import com.junbaobao.stock.model.po.RatioData;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RatioDataVO extends RatioData {

    /**
     * 成交量
     */
    private BigDecimal nowMoney;

    /**
     * 涨幅
     */
    private BigDecimal increase;
}
