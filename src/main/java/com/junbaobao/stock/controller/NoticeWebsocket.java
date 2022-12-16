package com.junbaobao.stock.controller;

/**
 * @Classname NoticeWebsocket
 * @Description
 * @Date: Created in 2022/12/5 9:57
 * @Author Name:
 */

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpRequest;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.junbaobao.stock.entity.NoticeWebsocketResp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ServerEndpoint("/stock")
@Component
@Slf4j
public class NoticeWebsocket {


    //记录连接的客户端
    public static Map<String, Session> clients = new ConcurrentHashMap<>();

    public static Map<String, Set<String>> conns = new ConcurrentHashMap<>();


    private String sid = null;

    private String userId = "junbaobao";

    @OnOpen
    public void onOpen(Session session) {
        this.sid = UUID.randomUUID().toString();
        clients.put(this.sid, session);

        Set<String> clientSet = conns.get(userId);

        if (clientSet == null) {
            clientSet = new HashSet<>();
            conns.put(userId, clientSet);
        }
        clientSet.add(this.sid);


        JSONArray stock = getStock();
        NoticeWebsocketResp noticeWebsocketResp = new NoticeWebsocketResp();
        noticeWebsocketResp.setNoticeInfo(stock);
        sendMessage(noticeWebsocketResp);


//        getStockInfo();
        System.out.println("连接开启");
    }

    /**
     * 发送给所有用户
     *
     * @param
     */
    public void sendMessage(NoticeWebsocketResp noticeWebsocketResp) {
        String message = JSONObject.toJSONString(noticeWebsocketResp);
        for (Session session1 : NoticeWebsocket.clients.values()) {
            try {
                session1.getBasicRemote().sendText(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        log.info("连接断开！");
        clients.remove(this.sid);
    }


    public void onMsg(Session session, String message) throws InterruptedException {
        while (true) {
            JSONArray stock = getStock();
            if (ObjectUtils.isEmpty(stock)) {
                return;
            }
            //这里休眠是2S 是因为页面刷新的太快了
            Thread.sleep(2000);
            NoticeWebsocketResp noticeWebsocketResp = new NoticeWebsocketResp();
            noticeWebsocketResp.setNoticeInfo(stock);
            sendMessage(noticeWebsocketResp);
        }

    }

    /**
     * 根据页数获取所有股票list
     */
    public JSONArray getStock() {
        String url = "https://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=500&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&wbp2u=|0|0|0|web&fid=f3&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23,m:0+t:81+s:2048&fields=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f12,f13,f14,f15,f16,f17,f18,f20,f21,f23,f24,f25,f22,f11,f62,f128,f136,f115,f152&_=1670309547456";

        String body = HttpRequest.get(url).execute().body();

        JSONObject jsonObject = JSON.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray diff = data.getJSONArray("diff");

        System.out.println(diff);
        return diff;
    }

    /**
     * 获取当前的现错
     * 20628
     *
     * @return
     */
    public JSONArray getStockInfo(String stockId) {
        //这里判断当前股票属于那个板块
        int secId = 0;
        if (stockId.startsWith("6")) {
            secId = 1;
        }

        Map<String, String> map = new HashMap<String, String>();
        //获取走势  可以取都是天的 最多5天
        String thisUrl = "https://push2his.eastmoney.com/api/qt/stock/trends2/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58&ut=fa5fd1943c7b386f172d6893dbfba10b&secid=" + secId + "." + stockId + "&ndays=2&iscr=1&iscca=0";


        String body = HttpRequest.get(thisUrl).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray trendsList = data.getJSONArray("trends");

        //根据时间进行一个分组
        Map<String, List<Object>> thisMap = trendsList.stream().collect(Collectors.groupingBy(d -> {
            String[] split = d.toString().split(",");
            String date = split[0];
            return toDate(date, "yyyy-MM-dd");
        }));


        //获取以往的走势
        String pastUrl = "https://push2his.eastmoney.com/api/qt/stock/kline/get?fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11,f12,f13&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&beg=0&end=20500101&ut=fa5fd1943c7b386f172d6893dbfba10b&rtntype=6&secid=" + secId + "." + stockId + "&klt=101&fqt=1";

        String body1 = HttpRequest.get(pastUrl).execute().body();
        JSONObject pastJsonObject = JSON.parseObject(body1);
        JSONObject pastData1 = pastJsonObject.getJSONObject("data");
        String stockName = data.getString("name");
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


        //Turnover

        int index = 0;
        //昨日最大的成交额
        BigDecimal yesterdayTurnover = null;
        //今日竞价
        BigDecimal thisBidding = null;
        //今日竞价量
        Integer thisBiddingVolume = null;

        //循环今日数据
        for (String thisData : thisMap.keySet()) {
            List<Object> list = thisMap.get(thisData);
            //0为今天 1为昨日
            if (index == 0) {
                //分时数据
                String date = list.get(0).toString();
                thisBidding = new BigDecimal(date.split(",")[6]);
                thisBiddingVolume = Integer.parseInt(date.split(",")[5]);
            } else {
                //昨日
                //第一个循环位昨日的竞价 获取昨日最大额  根据最大成交额排序
                List<Object> yesterdayDateList = list.stream().sorted(Comparator.comparing(sort -> {
                    return Double.parseDouble(sort.toString().split(",")[6]);
                }).reversed()).collect(Collectors.toList());

                String s = yesterdayDateList.get(0).toString();
                yesterdayTurnover = new BigDecimal(s.split(",")[6]);
            }

            index++;
        }
        //获取365天内最大的成交量
        List<Object> data365List = pastList.stream().sorted(Comparator.comparing(sort -> {
            return Integer.parseInt(sort.toString().split(",")[5]);
        }).reversed()).collect(Collectors.toList());
        //最大365天内的最大的成交量
        String maxiTurnover = data365List.get(0).toString();


        //比值2
        Boolean bizhi2 = thisBiddingVolume / Integer.parseInt(maxiTurnover.split(",")[5]) > 0.05;


        //今日竞价  > 昨日分时最大*0.5    1 大于 0 等于  -1 小于
        int flag = thisBidding.compareTo(yesterdayTurnover.multiply(new BigDecimal(0.5)));

        System.out.println("比值1：今日竞价  > 昨日分时最大*0.5    1 大于 0 等于  -1 小于");
        System.out.println(" 股票ID: " + stockId + " 股票名称: " + stockName + " 比值1条件结果：" + flag + "比值数是:" + yesterdayTurnover.divide(thisBidding, BigDecimal.ROUND_CEILING));

//        System.out.println(yesterdayTurnover.divide(thisBidding, BigDecimal.ROUND_CEILING).multiply(new BigDecimal(100)));


        //今日成交量 / 365天最大成交量>0.5 并且 今日竞价 > 昨日分时最大*0.5  并且 今日竞价成交额>10000000
        if (bizhi2 && flag == 1 && thisBidding.compareTo(new BigDecimal(10000000)) == 1) {
            System.out.println("当前标底可以进行一个观察股票ID: " + stockId + " 股票名称: " + stockName + "比值数是:" + yesterdayTurnover.divide(thisBidding, BigDecimal.ROUND_CEILING));
        }


        return null;
    }


    public void zhangtingban() {
        String url = "https://push2ex.eastmoney.com/getTopicZTPool?ut=7eea3edcaed734bea9cbfc24409ed989&dpt=wz.ztzt&Pageindex=0&pagesize=100&sort=fbt%3Aasc&date=20221212&_=1670832933186";

        String body = HttpRequest.get(url).execute().body();

        JSONObject jsonObject = JSON.parseObject(body);
        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray pool = data.getJSONArray("pool");

        for (int i = 0; i < pool.size(); i++) {
            JSONObject jsonObject1 = pool.getJSONObject(i);

            //股票ID
            String stockId = jsonObject1.getString("c");
            String stockName = jsonObject1.getString("n");
            String m = jsonObject1.getString("m");
            getStockInfo(stockId);
        }

    }

    public String toDate(String dateStr, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = dateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateFormat.format(date);
    }

    public static void main(String[] args) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.YEAR, -1);
        Date y = c.getTime();
        boolean in = DateUtil.isIn(new Date(), new Date(), y);

//        String year = format.format(y);
//        System.out.println("过去一年："+year);
    }

    public static boolean isBetween(LocalDateTime beginTime, LocalDateTime endTime) {
        //获取当前时间
        LocalDateTime now = LocalDateTime.now();
        boolean flag = false;
        if (now.isAfter(beginTime) && now.isBefore(endTime)) {
            flag = true;
        }
        return flag;
    }


    /**
     * 获取user自选
     *
     * @param cookie 登录cookie
     */
    public List<String> getOptional(String cookie) {
        List<String> list = new ArrayList<String>();
        //同花顺api _时间搓
        String url = "https://t.10jqka.com.cn/newcircle/group/getSelfStockWithMarket/?_=1670982946607";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Cookie", cookie);

        String body = HttpRequest.get(url).addHeaders(headers).execute().body();
        JSONObject jsonObject = JSON.parseObject(body);
        JSONArray resultList = jsonObject.getJSONArray("result");

        for (int i = 0; i < resultList.size(); i++) {
            JSONObject jsonObject1 = resultList.getJSONObject(i);

            list.add(jsonObject1.getString("code"));
        }
        //只返回stock Code
        return list;

    }
}
