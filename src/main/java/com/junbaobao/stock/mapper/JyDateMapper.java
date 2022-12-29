package com.junbaobao.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.junbaobao.stock.model.po.JyDate;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JyDateMapper extends BaseMapper<JyDate> {

    @Select("select jybz from jy_date where jyrq=#{dataStr}")
    int jybz(String dateStr);

    @Delete("delete from jy_date where jyrq like '${month}%'")
    int deleteByMonth(String month);

    @Select("select max(jyrq) from jy_date where jyrq<#{dataStr} and jybz = 1")
    String getUpJyrq(String dateStr);

}
