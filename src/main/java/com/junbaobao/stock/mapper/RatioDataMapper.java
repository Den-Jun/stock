package com.junbaobao.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.junbaobao.stock.model.po.RatioData;
import com.junbaobao.stock.model.vo.RatioDataVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RatioDataMapper extends BaseMapper<RatioData> {


    @Select("select t.*,d.HYBK from RATIO_DATA t " +
            "left join " +
            "(select HYBK,code,DATA_TIME from SHARE_DAY_DATA where DATA_TIME=( select date_format( max(jyrq), '%Y%m%d') from jy_date where  date_format(jyrq, '%Y%m%d')< #{dateStr} and jybz = 1)) d " +
            "on  d.`CODE` = t.code  where t.DATA_TIME = #{dateStr} " +
            "order by t.continuity_Day desc,t.pressure_Money desc ,t.EXPLOSIVE_QUANTITY desc")
    List<RatioDataVO> getRatioDataByDateStr(@Param("dateStr") String dateStr);

    @Delete("delete from RATIO_DATA where data_time = #{dateStr}")
    int deleteByDayStr(String dayStr);
}
