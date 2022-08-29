package com.smarting.price.tools.controller;

import com.smarting.price.tools.service.EcallService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class EcallController {

    @Autowired
    private EcallService ecallService;

    @PostMapping("/api/getEcallShow")
    public String getEcallShow(@RequestBody String params) {
        long startTime = System.currentTimeMillis();
        log.info("getEcallShow params = {}", params);
        String result = ecallService.getEcallShow(params).toJSONString();
        long endTime = System.currentTimeMillis();
        log.info("getEcallShow execution time = {}", endTime - startTime);
        return result;
    }
}
