package com.smarting.price.tools.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import com.smarting.price.tools.service.QuoteInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class QuoteInfoController {
    @Autowired
    private QuoteInfoService quoteInfoService;

    @PostMapping("/api/getTabInfoShow")
    public String getTabInfoShow(@RequestBody String params) {
        long startTime = System.currentTimeMillis();
        log.info("getTabInfoShow params = {}", params);
        JSONObject jsonObject = quoteInfoService.getReturnObject(params);
        String result = JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
        long endTime = System.currentTimeMillis();
        log.info("getTabInfoShow execution time = {}", endTime - startTime);
        return result;
    }

    @PostMapping("/api/getInfoShow")
    public String getInfoShow(@RequestBody String params) {
        long startTime = System.currentTimeMillis();
        log.info("getInfoShow params = {}", params);
        JSONObject jsonObject = quoteInfoService.getReturnObject(params);
        String result = JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
        long endTime = System.currentTimeMillis();
        log.info("getInfoShow execution time = {}", endTime - startTime);
        return result;
    }
}
