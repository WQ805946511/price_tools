package com.smarting.price.tools.service;

import com.smarting.price.tools.entity.Country;
import com.smarting.price.tools.mapper.CountryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CountryService {
    @Autowired
    private CountryMapper countryMapper;

    public List<Country> queryAllCountrys() {
        return countryMapper.queryAllCountrys();
    }

    public Set<String> queryAreas() {
        List<Country> countryList = countryMapper.queryAreas();
        return countryList.stream().map(Country::getRegionCn).sorted(Comparator.comparing(regionCn -> regionCn)).collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
