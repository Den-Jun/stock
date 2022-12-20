package com.junbaobao.stock.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.junbaobao.stock.mapper.RatioDataMapper;
import com.junbaobao.stock.mapper.ShareDateMapper;
import com.junbaobao.stock.model.dto.Ban;
import com.junbaobao.stock.model.po.RatioData;
import com.junbaobao.stock.model.po.ShareDate;
import com.junbaobao.stock.util.DataUtil;
import com.sun.org.apache.regexp.internal.RE;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 127.0.0.1:8089/select/productionData
 * 127.0.0.1:8089/select/productionRatio
 * 127.0.0.1:8089/select/getRatioDataListByDayStr
 * 127.0.0.1:8089/select/getShareDateListByDayStr
 */
@RestController
@RequestMapping("/select")
public class SelectController {

    @Resource
    ShareDateMapper shareDateMapper;
    @Resource
    RatioDataMapper ratioDataMapper;

    /**
     * 生成数据
     *
     * @param dayStr
     * @return
     */


//     127.0.0.1:8089/select/productionData
    @GetMapping("/productionData")
    public int productionData(String dayStr) {
        List<String> banCodeByDay = getBanCodeByDay(dayStr);
        boolean shareInfo = getShareInfo(banCodeByDay, dayStr);
        return banCodeByDay.size();
    }

    @GetMapping("/getStockRatioData")
    public  Map<String,Object> getStockRatioData(String stockId){
        Map<String,Object> map = new HashMap<String,Object>();
        RatioData ratioData = new RatioData();
        ratioData.setCode("1213");
        ratioData.setShareName("1213");
        map.put("ratioData",ratioData);
        return map;
    }


    public List<String> getBanCodeByDay(String dayStr) {
        if (dayStr == null) {
            dayStr = DateUtil.format(new Date(), "yyyymmdd");
        }
        String url = "https://push2ex.eastmoney.com/getTopicZTPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt&Pageindex=0&pagesize=100&sort=fbt%3Aasc&date=" + dayStr + "&_=1670832933186";
        String body = HttpRequest.get(url).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray pool = data.getJSONArray("pool");
        List<Ban> bans = JSONObject.parseArray(pool.toString(), Ban.class);
        List<String> collect = bans.stream().map(Ban::getC).collect(Collectors.toList());
        return collect;
    }


    public boolean getShareInfo(List<String> stockList, String dateStr) {
        for (String stockId : stockList) {
            String stockName = null;
            try {
                //这里判断当前股票属于那个板块
                String secId = stockId;
                if (stockId.startsWith("6")) {
                    secId = 1 + "." + stockId;
                } else {
                    secId = 0 + "." + stockId;
                }

                //获取走势  可以取都是天的 最多5天
                String thisUrl = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f20&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f20&ut=fa5fd1943c7b386f172d6893dbfba10b&secid=" + secId + "&ndays=2&iscr=1&iscca=0";
                String body = HttpRequest.get(thisUrl).execute().body();
                JSONObject jsonObject = JSON.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray trendsList = data.getJSONArray("trends");

                //根据时间进行一个分组
                Map<String, List<Object>> thisMap = trendsList.stream().collect(Collectors.groupingBy(d -> {
                    String[] split = d.toString().split(",");
                    String date = split[0];
                    return DataUtil.toDate(date, "yyyy-MM-dd");
                }));
                Object[] objects = thisMap.keySet().toArray();
                //昨日09：30数据
                Object yesterdayBiddingData = thisMap.get(objects[1]).get(0);
                BigDecimal yesterdayBiddingVolume = new BigDecimal(yesterdayBiddingData.toString().split(",")[5]);

                //昨日09：31数据
                Object yesterdayFirstMinuteData = thisMap.get(objects[1]).get(1);
                BigDecimal yesterdayFirstMinute = new BigDecimal(yesterdayFirstMinuteData.toString().split(",")[5]);

                //今天09：30
                Object todayBiddingData = thisMap.get(objects[0]).get(0);
                BigDecimal todayBiddingVolume = new BigDecimal(todayBiddingData.toString().split(",")[5]);

                //今天09：31
                BigDecimal todayFirstMinute = new BigDecimal(0);
                List<Object> todayData = thisMap.get(objects[0]);
                int size = todayData.size();
                if (size > 1) {
                    todayFirstMinute = new BigDecimal(todayData.get(1).toString().split(",")[5]);
                }

                //获取昨日分时最大量
                List<Object> yesterdayDateList = thisMap.get(objects[1]).stream().sorted(Comparator.comparing(sort ->
                        Double.parseDouble(sort.toString().split(",")[6])
                ).reversed()).collect(Collectors.toList());
                String s = yesterdayDateList.get(0).toString();
                BigDecimal yesterdayMinterMax = new BigDecimal(s.split(",")[5]);

                //获取以往的走势
                String pastUrl = "https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=" + secId + "&klt=101&fqt=1";

                String body1 = HttpRequest.get(pastUrl).execute().body();
                JSONObject pastJsonObject = JSON.parseObject(body1);
                JSONObject pastData1 = pastJsonObject.getJSONObject("data");
                stockName = data.getString("name");
                JSONArray klinesList = pastData1.getJSONArray("klines");

                //取365天的数据
                List<Object> pastList = klinesList.stream().filter(a -> {
                    try {
                        //获取1年前的时间
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                        Calendar c = Calendar.getInstance();
                        c.setTime(new Date());
                        c.add(Calendar.YEAR, -1);
                        Date y = c.getTime();
                        String[] split = a.toString().split(",");
                        //时间
                        String date = split[0];

                        //当前时间是否是存在365中的时间
                        //检测时间       开始时间   结束时间
                        return DateUtil.isIn(format.parse(date), new Date(), y);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return false;
                }).collect(Collectors.toList());
                //获取365天内最大的成交量
                List<Object> data365List = pastList.stream().sorted(Comparator.comparing(sort -> {
                    return Integer.parseInt(sort.toString().split(",")[5]);
                }).reversed()).collect(Collectors.toList());
                //最大365天内的最大的成交量
                String maxiTurnover = data365List.get(0).toString();


                //
                List<Object> oneYearDateData = pastList.stream().sorted(Comparator.comparing(sort -> {
                    return String.valueOf(sort.toString().split(",")[0]);
                }).reversed()).collect(Collectors.toList());
                //昨天交易量
                Object yesterdayData = oneYearDateData.get(1);
                BigDecimal yesterdayTotal = new BigDecimal(yesterdayData.toString().split(",")[5]);
                //前天交易量
                Object beforeYesterdayData = oneYearDateData.get(2);
                BigDecimal beforeYesterday = new BigDecimal(beforeYesterdayData.toString().split(",")[5]);

                List<Object> sixDayData = oneYearDateData.stream().limit(6).collect(Collectors.toList());
                List<Object> fiveDayData = sixDayData.stream().skip(1).collect(Collectors.toList());

                //前五日总成交量
                int beforeFiveSum = fiveDayData.stream().mapToInt(i -> {
                    return Integer.parseInt(i.toString().split(",")[5]);
                }).sum();

//                //当天档口行情报价
//                String hqbjUrl = "http://63.push2.eastmoney.com/api/qt/stock/sse?ut=fa5fd1943c7b386f172d6893dbfba10b&fltt=2&invt=2&volt=2&fields=f152,f288,f43,f57,f58,f169,f170,f46,f44,f51,f168,f47,f164,f116,f60,f45,f52,f50,f48,f167,f117,f71,f161,f49,f530,f135,f136,f137,f138,f139,f141,f142,f144,f145,f147,f148,f140,f143,f146,f149,f55,f62,f162,f92,f173,f104,f105,f84,f85,f183,f184,f185,f186,f187,f188,f189,f190,f191,f192,f107,f111,f86,f177,f78,f110,f262,f263,f264,f267,f268,f250,f251,f252,f253,f254,f255,f256,f257,f258,f266,f269,f270,f271,f273,f274,f275,f127,f199,f128,f198,f259,f260,f261,f171,f277,f278,f279,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f20,f19,f18,f17,f16,f15,f14,f13,f12,f11,f531&secid="+secId;
//
//                String hqbjBody = HttpRequest.get(hqbjUrl).execute().body();
//                hqbjBody = hqbjBody.replace("data: ", "");
//                JSONObject hqbjJsonObject = JSON.parseObject(hqbjBody);
//                JSONObject hqbjData = pastJsonObject.getJSONObject("data");
//                String mai1 = data.getString("f20");
//
                ShareDate shareDate = new ShareDate();
                shareDate.setId(UUID.randomUUID().toString());
                shareDate.setCode(stockId);
                shareDate.setDataTime(dateStr);
                shareDate.setCreateTime(new Date());
                shareDate.setShareName(stockName);
                // 昨日竞价量
                shareDate.setYesterdayBiddingVolume(yesterdayBiddingVolume);
                //今天竞价金量
                shareDate.setTodayBiddingVolume(todayBiddingVolume);
                //昨日封单大小
                // shareDate.setYesterdaySealedVolume();
                //昨日分时最大量
                shareDate.setYesterdayMinterMax(yesterdayMinterMax);
                //昨日总成交量
                shareDate.setYesterdayTotal(yesterdayTotal);
                //今天未匹配量
                //shareDate.setTodayUnmatched();
                //今天第一分钟成交量
                shareDate.setTodayFirstMinute(todayFirstMinute);
                //竞价十分钟的平均每一分钟交易量
                shareDate.setTodayBiddingMinuteAverage(todayBiddingVolume.divide(new BigDecimal(10), 5, BigDecimal.ROUND_FLOOR));
                //过去五天平均每一分钟交易量
                shareDate.setFiveDayAverageMinutes(new BigDecimal(beforeFiveSum).divide(new BigDecimal(6 * 60), 5, BigDecimal.ROUND_FLOOR));
                //前日交易量
                shareDate.setBeforeYesterday(beforeYesterday);
                shareDateMapper.insert(shareDate);
            } catch (Exception e) {
                System.out.println(" 股票ID: " + stockId + " 股票名称: " + stockName + "报错;e:" + e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 生成比值数据
     *
     * @param dateStr
     * @return
     */
    //     127.0.0.1:8089/select/productionRatio
    @GetMapping("/productionRatio")
    public boolean productionRatio(String dateStr) {
        List<ShareDate> shareDateByDateStr = shareDateMapper.getShareDateByDateStr(dateStr);
        for (ShareDate shareDate : shareDateByDateStr) {
            RatioData ratioData = new RatioData();
            ratioData.setId(UUID.randomUUID().toString());
            ratioData.setDataTime(dateStr);
            ratioData.setCreateTime(new Date());
            ratioData.setShareName(shareDate.getShareName());
            ratioData.setCode(shareDate.getCode());
            //'未竞成交比（未匹配量/竞价量）',
//            ratioData.setUnsuccessfulBidding();
            //'竞价分钟比(竞价十分钟的平均每一分钟交易量/过去五天平均每分钟交易量)'
            ratioData.setBiddingYesterday(shareDate.getTodayBiddingMinuteAverage().divide(shareDate.getFiveDayAverageMinutes(), 5, BigDecimal.ROUND_FLOOR));
            //'竞价比(今日竞价量/昨日竞价量)'
            ratioData.setBiddingMinter(shareDate.getTodayBiddingVolume().divide(shareDate.getYesterdayBiddingVolume(), 5, BigDecimal.ROUND_FLOOR));
            //'爆量系数（竞价量/昨日分时最大量）'
            ratioData.setExplosiveQuantity(shareDate.getTodayBiddingVolume().divide(shareDate.getYesterdayMinterMax(), 5, BigDecimal.ROUND_FLOOR));
            //'昨日上板系数（昨日最大分时/昨日成交量）'
            ratioData.setYesterdayBan(shareDate.getYesterdayMinterMax().divide(shareDate.getYesterdayTotal(), 5, BigDecimal.ROUND_FLOOR));
//            //'竞封比（竞价量/昨日封单量）'
//            ratioData.setBiddingSealed();
            //'昨前比（昨日成交量/前日成交量）'
            ratioData.setYesterdayFront(shareDate.getYesterdayTotal().divide(shareDate.getBeforeYesterday(), 5, BigDecimal.ROUND_FLOOR));
            // '昨竞成交比（昨天竞价量/昨天成交量）'
            ratioData.setYesterdayBidding(shareDate.getYesterdayBiddingVolume().divide(shareDate.getYesterdayTotal(), 5, BigDecimal.ROUND_FLOOR));
            ratioDataMapper.insert(ratioData);
        }
        return true;
    }

    /**
     * 获取比值数据
     *
     * @param dayStr
     * @return
     */
    //     127.0.0.1:8089/select/getRatioDataListByDayStr
    @GetMapping("getRatioDataListByDayStr")
    public List<RatioData> getRatioDataListByDayStr(String dayStr) {
        if (dayStr == null) {
            dayStr = DateUtil.format(new Date(), "yyyymmdd");
        }
        return ratioDataMapper.getRatioDataByDateStr(dayStr);
    }

    /**
     * 获取比值数据
     *
     * @param dayStr
     * @return
     */
    //     127.0.0.1:8089/select/getShareDateListByDayStr
    @GetMapping("getShareDateListByDayStr")
    public List<ShareDate> getShareDateListByDayStr(String dayStr) {
        if (dayStr == null) {
            dayStr = DateUtil.format(new Date(), "yyyymmdd");
        }
        return shareDateMapper.getShareDateByDateStr(dayStr);
    }


}
