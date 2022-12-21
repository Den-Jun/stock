package com.junbaobao.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.junbaobao.stock.model.po.ShareDayData;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShareDayDataMapper extends BaseMapper<ShareDayData> {


    @Select("select * from SHARE_DAY_DATA where DATA_TIME =#{yesterdayStr}")
    List<ShareDayData> getShareDayData(@Param("yesterdayStr") String yesterdayStr);

    @Delete("delete from SHARE_DAY_DATA where DATA_TIME =#{dayStr} ")
    int deleteByDataTime(String dayStr);
}
