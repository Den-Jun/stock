package com.junbaobao.stock.controller;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.junbaobao.stock.job.SelectJob;
import com.junbaobao.stock.mapper.JyDateMapper;
import com.junbaobao.stock.model.po.JyDate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/jyDate")
@Slf4j
public class JyDateController {
    @Resource
    JyDateMapper jyDateMapper;
    @Resource
    SelectJob selectJob;

    @GetMapping("/test")
    public void  test(){
        selectJob.start();
    }

    /**
     * 保存下个月数据
     * @return
     */
    @GetMapping("/savePreMonthJyDate")
    public Boolean savePreMonthJyDate() {
        String month = getPreMonth();
        return saveJyDate(month);
    }
    /**
     * 保存当前月数据
     * @return
     */
    @GetMapping("/saveNowMonthJyDate")
    public Boolean saveNowMonthJyDate() {
        String month = getNowMonth();
        return saveJyDate(month);
    }

    public boolean saveJyDate(String month){
        int i = jyDateMapper.deleteByMonth(month);
        String thisUrl = "http://www.szse.cn/api/report/exchange/onepersistenthour/monthList?month=" + month;
        String body = HttpRequest.get(thisUrl).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        JSONArray data = jsonObject.getJSONArray("data");
        List<JyDate> list = JSONObject.parseArray(data.toString(), JyDate.class);
        for (JyDate jyDate : list) {
            jyDateMapper.insert(jyDate);
        }
        return true;
    }


    public String getPreMonth() {
        Calendar cal = Calendar.getInstance();
        cal.add(cal.MONTH, 1);
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
        String preMonth = dft.format(cal.getTime());
        return preMonth;
    }

    public String getNowMonth() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM");
        String preMonth = dft.format(cal.getTime());
        return preMonth;
    }

}
