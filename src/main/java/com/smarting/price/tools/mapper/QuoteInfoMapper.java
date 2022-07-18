package com.smarting.price.tools.mapper;

import com.alibaba.fastjson.JSONArray;
import com.smarting.price.tools.entity.QuoteInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface QuoteInfoMapper {
    List<QuoteInfo> getQuoteInfoList(@Param("flowSpecifications") String flowSpecifications,
                                     @Param("list") JSONArray countryArray);

    List<QuoteInfo> getQuoteInfoTwoList();
}
