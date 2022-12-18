package com.junbaobao.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.junbaobao.stock.model.po.ShareDate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShareDateMapper extends BaseMapper<ShareDate> {

    @Select("select * from Share_Date where date_time= #{dateStr}")
    List<ShareDate> getShareDateByDateStr(@Param("dateStr") String dateStr);

}
