package com.junbaobao.stock.model.dto;

import lombok.Data;

@Data
public class Ban {

    /**
     * 代码
     */
    private String c;

    /**
     * 名称
     */
    private String n;

    /**
     * 次数
     */
    private String m;

    /**
     * 行业板块
     */
    private String hybk;

}
