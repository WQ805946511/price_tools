package com.smarting.price.tools.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.smarting.price.tools.service.UerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(originPatterns = "*", maxAge = 3600, allowCredentials = "true")
public class UerController {
    @Autowired
    private UerService uerService;

    @PostMapping("/api/authenticate")
    public String getAuthenticate(@RequestBody String params) {
        JSONObject jsonObject = uerService.getAuthenticate(params);
        return JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }

    @GetMapping("/api/currentUser")
    public String getCurrentUer(@RequestParam("email") String email) {
        JSONObject jsonObject = uerService.getCurrentUer(email);
        return JSONObject.toJSONString(jsonObject, SerializerFeature.WriteMapNullValue);
    }
}
