package com.junbaobao.stock.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.junbaobao.stock.mapper.RatioDataMapper;
import com.junbaobao.stock.mapper.ShareDateMapper;
import com.junbaobao.stock.mapper.ShareDayDataMapper;
import com.junbaobao.stock.model.dto.Ban;
import com.junbaobao.stock.model.po.RatioData;
import com.junbaobao.stock.model.po.ShareDate;
import com.junbaobao.stock.model.po.ShareDayData;
import com.junbaobao.stock.model.vo.RatioDataVO;
import com.junbaobao.stock.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import net.sf.jxls.transformer.XLSTransformer;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 不要测试执行这两个避免打乱数据
 * #生成复盘数据 ---每天收盘后执行
 * http://127.0.0.1:8089/select/productionBanShareDayDateByDayStr?dayStr=收盘日期
 * #生成比值数据 -- 开盘09：26 后执行  yyyymmdd
 * http://127.0.0.1:8089/select/productionRatio?yesterdayStr=上一个交易日&todayStr=今天
 * #收盘后更新当天比值结果是否涨停
 * http://127.0.0.1:8089/select/updateRatioResult?todayStr=今天
 * <p>
 * #查看比值结果
 * http://127.0.0.1:8089/select/getRatioDataListByDayStr?dayStr=比值日期
 * #导出比值结果
 * http://127.0.0.1:8089/select/exportRatioData?dayStrs=比值日期
 */
@RestController
@RequestMapping("/select")
@Slf4j
public class SelectController {

    @Resource
    ShareDateMapper shareDateMapper;
    @Resource
    RatioDataMapper ratioDataMapper;
    @Resource
    ShareDayDataMapper shareDayDataMapper;


    /**
     * 26分后取值
     *
     * @param secId
     * @return
     */
    @GetMapping("/getBiddingVolume")
    public String getBiddingVolume(String secId) {
        try {
            String thisUrl = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f20&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f20&ut=fa5fd1943c7b386f172d6893dbfba10b&secid=" + secId + "&ndays=1&iscr=1&iscca=0";
            String body = HttpRequest.get(thisUrl).execute().body();
            JSONObject jsonObject = JSON.parseObject(body);
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray trendsList = data.getJSONArray("trends");
            return trendsList.get(11).toString().split(",")[5] + "-" + trendsList.get(11).toString().split(",")[1];
        } catch (Exception e) {
            log.error("获取分时走势错误:" + secId);
            return null;
        }
    }

    /**
     * 生成比值数据
     *
     * @param
     * @return
     */
    //     127.0.0.1:8089/select/productionRatio
    @GetMapping("/productionRatio")
    public boolean productionRatio(Integer yesterdayStr, Integer todayStr) {
        int i = ratioDataMapper.deleteByDayStr(todayStr.toString());
        List<ShareDayData> shareDateByDateStr = shareDayDataMapper.getShareDayData(yesterdayStr.toString());

        for (ShareDayData shareDate : shareDateByDateStr) {
            String biddingVolumeStr = getBiddingVolume(shareDate.getSecId());
            String money = biddingVolumeStr.substring(biddingVolumeStr.indexOf("-") + 1);
            biddingVolumeStr = biddingVolumeStr.substring(0, biddingVolumeStr.indexOf("-"));

            BigDecimal biddingVolume = new BigDecimal(biddingVolumeStr);
            RatioData ratioData = new RatioData();

            ratioData.setId(shareDate.getId());
            ratioData.setDataTime(todayStr.toString());
            ratioData.setSecId(shareDate.getSecId());
            ratioData.setCreateTime(new Date());
            ratioData.setContinuityDay(shareDate.getContinuityDay());
            ratioData.setShareName(shareDate.getShareName());
            ratioData.setCode(shareDate.getCode());
            //压力金额
            ratioData.setPressureMoney(shareDate.getOneYearMax().multiply(new BigDecimal(money)).divide(new BigDecimal(1000000), 2, RoundingMode.FLOOR));
            //'未竞成交比（未匹配量/竞价量）',
//            ratioData.setUnsuccessfulBidding();
            //'竞昨成交比(竞价量/昨日成交量)'
            ratioData.setBiddingYesterday(biddingVolume.divide(shareDate.getTotalVolume(), 5, RoundingMode.FLOOR));
            //'竞价比(今日竞价量/昨日竞价量)'
            ratioData.setBidding(biddingVolume.divide(shareDate.getBiddingVolume(), 5, RoundingMode.FLOOR));
            //竞价分钟比(竞价十分钟的平均每一分钟交易量/过去五天平均每分钟交易量)'
            ratioData.setBiddingMinter(biddingVolume.divide(new BigDecimal(10)).divide(shareDate.getFiveDayAverageMinutes(), 5, RoundingMode.FLOOR));
            //'爆量系数（竞价量/昨日分时最大量）'
            ratioData.setExplosiveQuantity(biddingVolume.divide(shareDate.getTodayMinterMax(), 5, RoundingMode.FLOOR));
            //'昨日上板系数（昨日最大分时/昨日成交量）'
            ratioData.setYesterdayBan(shareDate.getTodayMinterMax().divide(shareDate.getTotalVolume(), 5, RoundingMode.FLOOR));
//            //'竞封比（竞价量/昨日封单量）'
            ratioData.setBiddingSealed(biddingVolume.divide(shareDate.getSealedVolume(), 5, RoundingMode.FLOOR));
            //'昨前比（昨日成交量/前日成交量）'
            ratioData.setYesterdayFront(shareDate.getTotalVolume().divide(shareDate.getYesterdayTotalVolume(), 5, RoundingMode.FLOOR));
            // '昨竞成交比（昨天竞价量/昨天成交量）'
            ratioData.setYesterdayBidding(shareDate.getBiddingVolume().divide(biddingVolume, 5, RoundingMode.FLOOR));
            //竞年比（今天竞价量/一年最大量）
            ratioData.setYearBidding(biddingVolume.divide(shareDate.getOneYearMax(), 5, RoundingMode.FLOOR));
            ratioDataMapper.insert(ratioData);
            log.info(ratioData.toString());
        }
        return true;
    }

    @GetMapping("/getStockRatioData")
    public Map<String, Object> getStockRatioData(String stockId) {
        Map<String, Object> map = new HashMap<String, Object>();
        RatioData ratioData = new RatioData();
        ratioData.setCode("1213");
        ratioData.setShareName("1213");
        map.put("ratioData", ratioData);
        return map;
    }


    /**
     * 获取比值数据
     *
     * @param dayStr
     * @return
     */
    //     127.0.0.1:8089/select/getRatioDataListByDayStr
    @GetMapping("getRatioDataListByDayStr")
    public List<RatioDataVO> getRatioDataListByDayStr(String dayStr) {
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        if (StringUtils.isBlank(dayStr)) {
            dayStr = cn.hutool.core.date.DateUtil.format(new Date(), "yyyyMMdd");
        }
        List<Ban> ban = getBan(dayStr);
        List<String> collect = ban.stream().map(Ban::getC).collect(Collectors.toList());
        List<RatioDataVO> ratioDataByDateStr = ratioDataMapper.getRatioDataByDateStr(dayStr);
        ratioDataByDateStr.parallelStream().forEach(ratioData -> {
            ratioData.setNowMoney(getNowMoney(ratioData.getSecId()));
            ratioData.setBan(collect.contains(ratioData.getCode()));
        });
        return ratioDataByDateStr;
    }


    /**
     * 每天收盘生成收盘数据
     *
     * @param dayStr
     * @return
     */
    @GetMapping("/productionBanShareDayDateByDayStr")
    public int productionBanShareDayDateByDayStr(Integer dayStr) {

        int i = shareDayDataMapper.deleteByDataTime(dayStr.toString());
        List<ShareDayData> shareDayDataList = saveBanData(dayStr.toString());
        boolean b = productionShareDayData(shareDayDataList);
        return shareDayDataList.size();
    }


    /**
     * 保存涨停板数据
     *
     * @param dayStr
     * @return
     */
    public List<ShareDayData> saveBanData(String dayStr) {

        if (dayStr == null) {
            dayStr = DateUtil.format(new Date(), "yyyyMMdd");
        }
        List<Ban> bans = getBan(dayStr);
        List<ShareDayData> shareDayDataList = new ArrayList<>();
        for (Ban ban : bans) {
            ShareDayData shareDayData = new ShareDayData();
            shareDayData.setId(dayStr + "-" + ban.getC());
            shareDayData.setCode(ban.getC());
            shareDayData.setDataTime(dayStr);
            shareDayData.setCreateTime(new Date());
            shareDayData.setShareName(ban.getN());
            shareDayData.setHybk(ban.getHybk());
            shareDayData.setContinuityDay(ban.getLbc());
            shareDayDataMapper.insert(shareDayData);
            shareDayDataList.add(shareDayData);
        }
        return shareDayDataList;
    }

    public List<Ban> getBan(String dayStr) {
        String url = "https://push2ex.eastmoney.com/getTopicZTPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt&Pageindex=0&pagesize=100&sort=fbt%3Aasc&date=" + dayStr + "&_=1670832933186";
        String body = HttpRequest.get(url).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray pool = data.getJSONArray("pool");
        return JSONObject.parseArray(pool.toString(), Ban.class);
    }

    /**
     * 填充成交数据
     *
     * @param shareDayDataList
     * @return
     */
    public boolean productionShareDayData(List<ShareDayData> shareDayDataList) {
        for (ShareDayData shareDayData : shareDayDataList) {
            String code = shareDayData.getCode();
            String stockName = shareDayData.getShareName();
            try {
                //这里判断当前股票属于那个板块
                String secId = code;
                if (code.startsWith("6")) {
                    secId = 1 + "." + code;
                } else {
                    secId = 0 + "." + code;
                }

                //获取走势  可以取都是天的 最多5天
                String thisUrl = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13,f20&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f20&ut=fa5fd1943c7b386f172d6893dbfba10b&secid=" + secId + "&ndays=1&iscr=1&iscca=0";
                String body = HttpRequest.get(thisUrl).execute().body();
                JSONObject jsonObject = JSON.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray trendsList = data.getJSONArray("trends");
                //09：30数据
                Object todayBiddingData = trendsList.get(11);
                //今天竞价金额
                BigDecimal biddingVolume = new BigDecimal(todayBiddingData.toString().split(",")[5]);
                //今天09：31
                Object firstMinuteData = trendsList.get(16);
                //今天第一分钟量
                BigDecimal firstMinuteVolume = new BigDecimal(firstMinuteData.toString().split(",")[5]);

                //今天数据按照成交量大小排序
                List<Object> todayDateList = trendsList.stream().sorted(Comparator.comparing(sort ->
                        Double.parseDouble(sort.toString().split(",")[6])
                ).reversed()).collect(Collectors.toList());
                //获取第一个数据成交量最大
                String max = todayDateList.get(0).toString();
                BigDecimal todayMinterMax = new BigDecimal(max.split(",")[5]);

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
                        return cn.hutool.core.date.DateUtil.isIn(format.parse(date), new Date(), y);
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
                BigDecimal oneYearMax = new BigDecimal(data365List.get(0).toString().split(",")[5]);

                //一年的数据按照时间排序
                List<Object> oneYearDateData = pastList.stream().sorted(Comparator.comparing(sort -> {
                    return String.valueOf(sort.toString().split(",")[0]);
                }).reversed()).collect(Collectors.toList());
                //今天成交量
                Object todayData = oneYearDateData.get(0);
                BigDecimal totalVolume = new BigDecimal(todayData.toString().split(",")[5]);
                //昨天交易量
                Object yesterdayData = oneYearDateData.get(1);
                BigDecimal yesterdayTotalVolume = new BigDecimal(yesterdayData.toString().split(",")[5]);

                //竞价十分钟平均成交量
                BigDecimal biddingMinuteAverage = biddingVolume.divide(new BigDecimal(10), 5, RoundingMode.FLOOR);

                List<Object> fiveDayData = oneYearDateData.stream().limit(5).collect(Collectors.toList());
                //前五日总成交量
                int beforeFiveSum = fiveDayData.stream().mapToInt(i -> {
                    return Integer.parseInt(i.toString().split(",")[5]);
                }).sum();
                BigDecimal fiveDayAverageMinutes = new BigDecimal(beforeFiveSum).divide(new BigDecimal(6 * 60), 5, RoundingMode.FLOOR);

                //当天档口行情报价
                String hqbjUrl = "http://63.push2.eastmoney.com/api/qt/stock/get?ut=fa5fd1943c7b386f172d6893dbfba10b&fltt=2&invt=2&volt=2&fields=f152,f288,f43,f57,f58,f169,f170,f46,f44,f51,f168,f47,f164,f116,f60,f45,f52,f50,f48,f167,f117,f71,f161,f49,f530,f135,f136,f137,f138,f139,f141,f142,f144,f145,f147,f148,f140,f143,f146,f149,f55,f62,f162,f92,f173,f104,f105,f84,f85,f183,f184,f185,f186,f187,f188,f189,f190,f191,f192,f107,f111,f86,f177,f78,f110,f262,f263,f264,f267,f268,f250,f251,f252,f253,f254,f255,f256,f257,f258,f266,f269,f270,f271,f273,f274,f275,f127,f199,f128,f198,f259,f260,f261,f171,f277,f278,f279,f31,f32,f33,f34,f35,f36,f37,f38,f39,f40,f20,f19,f18,f17,f16,f15,f14,f13,f12,f11,f531&secid=" + secId;

                String hqbjBody = HttpRequest.get(hqbjUrl).execute().body();
                hqbjBody = hqbjBody.replace("data: ", "");
                JSONObject hqbjJsonObject = JSON.parseObject(hqbjBody);
                JSONObject hqbjData = hqbjJsonObject.getJSONObject("data");
                String mai1 = hqbjData.getString("f20");
                BigDecimal sealedVolume = new BigDecimal(mai1);
//                shareDayData.setUnmatchedVolume();
                shareDayData.setBiddingVolume(biddingVolume);
                shareDayData.setSecId(secId);
                shareDayData.setFirstMinuteVolume(firstMinuteVolume);
                shareDayData.setTodayMinterMax(todayMinterMax);
                shareDayData.setTotalVolume(totalVolume);
                shareDayData.setSealedVolume(sealedVolume);
                shareDayData.setBiddingMinuteAverage(biddingMinuteAverage);
                shareDayData.setFiveDayAverageMinutes(fiveDayAverageMinutes);
                shareDayData.setYesterdayTotalVolume(yesterdayTotalVolume);
                shareDayData.setOneYearMax(oneYearMax);
                shareDayDataMapper.updateById(shareDayData);
                log.info(shareDayData.toString());
            } catch (Exception e) {
                shareDayDataMapper.deleteById(shareDayData.getId());
                System.out.println(" 股票ID: " + code + " 股票名称: " + stockName + "报错;e:" + e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 更新比值结果 是否涨停
     */
    @GetMapping("/updateRatioResult")
    public int updateRatioResult(Integer todayStr) {
        List<RatioDataVO> ratioDataByDateStr = ratioDataMapper.getRatioDataByDateStr(todayStr.toString());
        String url = "https://push2ex.eastmoney.com/getTopicZTPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt&Pageindex=0&pagesize=100&sort=fbt%3Aasc&date=" + todayStr + "&_=1670832933186";
        String body = HttpRequest.get(url).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray pool = data.getJSONArray("pool");
        List<Ban> bans = JSONObject.parseArray(pool.toString(), Ban.class);
        List<String> collect = bans.stream().map(Ban::getC).collect(Collectors.toList());
        Map<String, List<Ban>> listMap = bans.stream().collect(Collectors.groupingBy(Ban::getC));
        int i = 0;
        for (RatioData ratioData : ratioDataByDateStr) {
            if (collect.contains(ratioData.getCode())) {
                ratioData.setBan(true);
                ratioData.setContinuityDay(listMap.get(ratioData.getCode()).get(0).getLbc());
                log.info(ratioData.toString());
                i++;
            } else {
                ratioData.setBan(false);
            }
            ratioDataMapper.updateById(ratioData);
        }
        return i;
    }


    @GetMapping("/exportRatioData")
    public void exportRatioData(Integer[] dayStrs) throws IOException, InvalidFormatException {
        QueryWrapper<RatioData> q = new QueryWrapper<>();
        q.in("DATA_TIME", dayStrs);
        q.orderByDesc("DATA_TIME");
        List<RatioData> ratioData = ratioDataMapper.selectList(q);
        export("temp/比值结果.xls", ratioData);
    }

    @Resource
    HttpServletRequest request;
    @Resource
    HttpServletResponse httpResponse;


    public void export(String filePath, List<RatioData> list) throws InvalidFormatException, IOException {
        String exportFileName = DateUtil.format(new Date(), "yyyy-MM-dd");
        InputStream resourceFile = FileUtil.getResourceFile(filePath);
        XLSTransformer xlsTransformer = new XLSTransformer();
        Map<Object, Object> beans = new HashMap<>();
        beans.put("list", list);
        Workbook workbook = xlsTransformer.transformXLS(resourceFile, beans);

        String userAgent = request.getHeader("User_Agent");
        if (StringUtils.isNotBlank(userAgent) && (userAgent.contains("MSIE") || userAgent.contains("Trident"))) {
            exportFileName = URLEncoder.encode(exportFileName, "UTF-8");
        } else {
            exportFileName = new String(exportFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        }

        httpResponse.setContentType("application/octet-stream");
        httpResponse.setHeader("Content-Disposition", "attachment; filename=\"" + exportFileName + "\".xls");
        workbook.write(httpResponse.getOutputStream());
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


    /**
     * 爆量股票添加自选
     *
     * @return
     */
    @GetMapping("/baoLiangOptionalAdd")
    public int baoLiangOptionalAdd() {
        String co = "v=A9bHpaa8qQ2kc52pShOoZk1WJ43ddxqxbLtOFUA_wrlUA3g5qAdqwTxLniET; Hm_lvt_78c58f01938e4d85eaf619eae71b4ed1=1670893758,1672230984; __bid_n=1850908397e32802cc4207; FEID=v10-42ce2c89652de70a093242fa17b6df01f7ed6511; __xaf_fpstarttimer__=1672231050911; __xaf_thstime__=1672231051315; FPTOKEN=cDKhKXn0aVBX2gtiXtDUahP23Uty6EGH2oFF2EVNRlUQ89mwCxdUKDbWM6Bf8QZ7DZYxyL0yJrkmreaszpMdiC2ASsoM+oyUp/lLofp05tKMtxPbpDyxvFrHRfUUxiEQns7ItmYzbl7Navv2Oein7TgBrp8ye6yIG9IXYkwHLOGKUJhxmomhBQGr7RrcAYMvTo8TJ9nHi7h3VJXBvybrnlZGemQXwxjJ2Wh…156575163.2.10.1672231052; __utmc=156575163; __utmt=1; user=MDpBbHdheXNZZTo6Tm9uZTo1MDA6NTM4ODcyMDE5OjcsMTExMTExMTExMTEsNDA7NDQsMTEsNDA7NiwxLDQwOzUsMSw0MDsxLDEwMSw0MDsyLDEsNDA7MywxLDQwOzUsMSw0MDs4LDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAxLDQwOzEwMiwxLDQwOjI0Ojo6NTI4ODcyMDE5OjE2NzIyMzE2MTI6OjoxNTkyOTA4NzQwOjQ4Njc4ODowOjFlOGFmOTE4NDlkNmRhMzY3ZjFkNTk2Yzc5MzQyMzk1MDpkZWZhdWx0XzQ6MQ%3D%3D; userid=528872019; u_name=AlwaysYe; escapename=AlwaysYe; ticket=e5e0570ecc09d4961739c56eff29a289; utk=907d0faa52ca5a52733ec31b820a6079";
        List<RatioDataVO> ratioDataListByDayStr = baoLiang();
        int i = 0;
        for (RatioData ratioData : ratioDataListByDayStr) {
            addStock(co, ratioData.getCode());
            i++;
        }
        return i;
    }

    @GetMapping("/baoLiang")
    public List<RatioDataVO> baoLiang() {
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        List<RatioDataVO> ratioDataListByDayStr = getRatioDataListByDayStr(null);
        List<RatioDataVO> re = new ArrayList<>();
        for (RatioDataVO ratioData : ratioDataListByDayStr) {
            if (
                    ratioData.getBiddingYesterday().compareTo(new BigDecimal("0.1")) > 0 //竞昨天大于0.1
                            && ratioData.getExplosiveQuantity().compareTo(new BigDecimal("0.5")) > 0//爆量大于0.5
                            && ratioData.getBiddingYesterday().compareTo(new BigDecimal("0.03")) > 0 //竞年大于0.03
//                    && ratioData.getYesterdayBidding().compareTo(new BigDecimal("1")) > 0
//                            && ratioData.getBiddingSealed().compareTo(new BigDecimal("0.15")) > 0  //竞封大于0.15
//                            && ratioData.getBiddingMinter().compareTo(new BigDecimal("1")) > 0  //竞价分钟五日比大于1
            ) {
                re.add(ratioData);
            }
        }
        return re;
    }

    /**
     * 添加自选
     */
    public void addStock(String cookie, String stockId) {
        String url = "Https://t.10jqka.com.cn/newcircle/group/modifySelfStock/?op=add&stockcode=" + stockId;
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", cookie);
        String body = HttpRequest.get(url).addHeaders(headers).execute().body();
        log.info(body);
    }


    /**
     * 获取成交额
     *
     * @param secId
     * @return
     */
    public BigDecimal getNowMoney(String secId) {
        //获取以往的走势
        String pastUrl = "https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=" + secId + "&klt=101&fqt=1";
        String body1 = HttpRequest.get(pastUrl).execute().body();
        JSONObject pastJsonObject = JSON.parseObject(body1);
        JSONObject pastData1 = pastJsonObject.getJSONObject("data");
        JSONArray klinesList = pastData1.getJSONArray("klines");
        String today = klinesList.get(klinesList.size() - 1).toString();
        String[] todaySp = today.split(",");
        String xianJia = todaySp[2];
        String chengJiaoLiang = todaySp[5];
        return new BigDecimal(xianJia).multiply(new BigDecimal(chengJiaoLiang)).divide(new BigDecimal("1000000"), 2, BigDecimal.ROUND_FLOOR);
    }


}
