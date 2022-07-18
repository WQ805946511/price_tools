package com.smarting.price.tools.mapper;

import com.smarting.price.tools.entity.Country;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CountryMapper {
    List<Country> queryAllCountrys();

    List<Country> queryAreas();

    List<Country> queryAreasCn();
}
