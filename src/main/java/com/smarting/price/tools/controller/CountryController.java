package com.smarting.price.tools.controller;

import com.smarting.price.tools.entity.Country;
import com.smarting.price.tools.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class CountryController {
    @Autowired
    private CountryService countryService;

    @GetMapping("/api/queryAllCountrys")
    public List<Country> queryAllCountrys() {
        return countryService.queryAllCountrys();
    }

    @GetMapping("/api/queryAreas")
    public Set<String> queryAreas() {
        return countryService.queryAreas();
    }
}
