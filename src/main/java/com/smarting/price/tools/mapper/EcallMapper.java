package com.smarting.price.tools.mapper;

import com.smarting.price.tools.entity.EcallCost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface EcallMapper {
    List<EcallCost> getEcallCostListByRegion(String region, String carrier);

    List<EcallCost> getEcallCostListByCountryCode(String countryCode, String carrier);
}
