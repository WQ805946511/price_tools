package com.smarting.price.tools.service;

import com.alibaba.fastjson.JSONObject;
import com.smarting.price.tools.entity.User;
import com.smarting.price.tools.mapper.UserMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UerService {
    @Autowired
    private UserMapper userMapper;

    public JSONObject getAuthenticate(String params) {
        JSONObject jsonParams = JSONObject.parseObject(params);
        String userName = jsonParams.getString("userName");
        String password = jsonParams.getString("password");
        List<User> authenticateList = userMapper.getAuthenticate(userName, password);
        return getJsonObject(authenticateList);
    }

    private JSONObject getJsonObject(List<User> userList) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 400);
        if (CollectionUtils.isNotEmpty(userList)) {
            User user = userList.get(0);
            jsonObject.put("code", 200);
            jsonObject.put("email", user.getEmail());
            jsonObject.put("name", user.getName());
            jsonObject.put("nick", user.getNick());
            jsonObject.put("roleCode", user.getRoleCode());
        }
        return jsonObject;
    }

    public JSONObject getCurrentUer(String email) {
        List<User> currentUerList = userMapper.getCurrentUer(email);
        return getJsonObject(currentUerList);
    }
}
