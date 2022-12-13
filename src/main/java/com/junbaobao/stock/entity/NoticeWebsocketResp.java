package com.junbaobao.stock.entity;

import lombok.Data;

/**
 * @Classname NoticeWebsocketResp
 * @Description
 * @Date: Created in 2022/12/5 14:33
 * @Author Name:
 */
@Data
public class NoticeWebsocketResp<T> {

    private String noticeType;

    private T noticeInfo;

}