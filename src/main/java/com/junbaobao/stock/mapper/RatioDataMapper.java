package com.junbaobao.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.junbaobao.stock.model.po.RatioData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RatioDataMapper extends BaseMapper<RatioData> {


    @Select("select * from RATIO_DATA where data_time = #{dateStr}")
    List<RatioData> getRatioDataByDateStr(@Param("dateStr") String dateStr);

    @Delete("delete from RATIO_DATA where data_time = #{dateStr}")
    int deleteByDayStr(String dayStr);
}
