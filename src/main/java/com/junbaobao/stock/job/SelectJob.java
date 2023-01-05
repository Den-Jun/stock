package com.junbaobao.stock.job;

import com.junbaobao.stock.controller.JyDateController;
import com.junbaobao.stock.controller.SelectController;
import com.junbaobao.stock.mapper.JyDateMapper;
import com.junbaobao.stock.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

@Component
@Slf4j
public class SelectJob {

    @Resource
    JyDateMapper jyDateMapper;
    @Resource
    SelectController selectController;
    @Resource
    JyDateController jyDateController;

    /**
     * 收盘后更新,并复盘涨停数据
     */
    @Scheduled(cron = "1 0 15 * * *")
    public void end() {
        int jybz = jyDateMapper.jybz(DateUtil.toDate(new Date(), "yyyy-MM-dd"));
        if (jybz == 0) {
            return;
        }
        String today = DateUtil.toDate(new Date(), "yyyyMMdd");
        selectController.productionBanShareDayDateByDayStr(Integer.valueOf(today));
        selectController.updateRatioResult(Integer.valueOf(today));
    }

    /**
     * 开盘计算比值并且推送到自选
     */
    @Scheduled(cron = "1 26 9 * * *")
    public void start() {
        int jybz = jyDateMapper.jybz(DateUtil.toDate(new Date(), "yyyy-MM-dd"));
        if (jybz == 0) {
            return;
        }
        String today = DateUtil.toDate(new Date(), "yyyyMMdd");
        String upJyrq = jyDateMapper.getUpJyrq(DateUtil.toDate(new Date(), "yyyy-MM-dd"));
        if (StringUtils.isBlank(upJyrq)) {
            jyDateController.saveNowMonthJyDate();
            upJyrq = jyDateMapper.getUpJyrq(DateUtil.toDate(new Date(), "yyyy-MM-dd"));
        }
        String upJyrqStr = DateUtil.toDate(upJyrq, "yyyy-MM-dd");
        selectController.productionRatio(Integer.valueOf(upJyrqStr.replace("-","")), Integer.valueOf(today));
        selectController.baoLiangOptionalAdd();
    }

    @Scheduled(cron = "1 23 18 * * *")
    public void zhangTingFuPanJob() {
        int jybz = jyDateMapper.jybz(DateUtil.toDate(new Date(), "yyyy-MM-dd"));
        if (jybz == 0) {
            return;
        }
        selectController.zhangTingFuPan();
    }

}
