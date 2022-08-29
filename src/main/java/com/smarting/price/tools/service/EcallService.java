package com.smarting.price.tools.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smarting.price.tools.entity.EcallCost;
import com.smarting.price.tools.mapper.EcallMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EcallService {

    @Autowired
    private EcallMapper ecallMapper;

    public JSONObject getEcallShow(String params) {
        JSONObject paramsObject = JSONObject.parseObject(params);
        String region = String.valueOf(paramsObject.getOrDefault("region", ""));
        String countryCode = String.valueOf(paramsObject.getOrDefault("countryCode", ""));
        String carrier = String.valueOf(paramsObject.getOrDefault("carrier", ""));
        int gp = Integer.parseInt(String.valueOf(paramsObject.getOrDefault("gp", 0)));
        JSONObject returnObject = new JSONObject();
        returnObject.put("status", "failed");
        returnObject.put("message", "query result is empty");
        returnObject.put("result", new JSONArray());
        if (StringUtils.isNotBlank(region)) {
            String join = String.join("", "%", region, "%");
            List<EcallCost> ecallCostList = ecallMapper.getEcallCostListByRegion(join, carrier);
            getJsonObject(region, "欧洲", gp, ecallCostList, returnObject, carrier);
            return returnObject;
        } else if (StringUtils.isNotBlank(countryCode)) {
            List<EcallCost> ecallCostList = ecallMapper.getEcallCostListByCountryCode(countryCode, carrier);
            getJsonObject("", "", gp, ecallCostList, returnObject, carrier);
            return returnObject;
        } else {
            return returnObject;
        }
    }

    private void getJsonObject(String region, String regionCn, int gp, List<EcallCost> ecallCostList,
                               JSONObject returnObject, String carrier) {
        List<EcallCost> ecallCostCollect = ecallCostList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(ecallCostCollect)) {
            List<JSONObject> countryCollect = ecallCostList.stream().map(ecallCost -> {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("countryCode", ecallCost.getCountryCode());
                jsonObject.put("countryCn", ecallCost.getCountryCn());
                return jsonObject;
            }).collect(Collectors.toList());
            Optional<EcallCost> ecallCostOptional = ecallCostList.stream().filter(Objects::nonNull).findAny();
            if (ecallCostOptional.isPresent()) {
                EcallCost ecallCost = ecallCostOptional.get();
                BigDecimal costEcallRmb = ecallCost.getCostEcallRmb();
                // 计算公式 eCall单价 = 对应成本价/（1-gp%）
                double divisor = (double) (100 - gp) / 100;
                BigDecimal divide = costEcallRmb.divide(new BigDecimal(divisor), 2, RoundingMode.HALF_UP);
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("costEcall", divide);
                jsonBody.put("carrier", carrier);
                jsonBody.put("region", region);
                jsonBody.put("regionCn", regionCn);
                jsonBody.put("network", ecallCost.getNetwork());
                jsonBody.put("contractStatus", ecallCost.getContractStatus());
                jsonBody.put("ecallFlag", ecallCost.getEcallFlag());
                jsonBody.put("csEcallFlag", ecallCost.getCsEcallFlag());
                jsonBody.put("ngEcallFlag", ecallCost.getNgEcallFlag());
                jsonBody.put("deliveryCycle", ecallCost.getDeliveryCycle());
                jsonBody.put("usePeriod",ecallCost.getUsePeriod());
                jsonBody.put("countryCollect", countryCollect);

                JSONArray jsonArray = new JSONArray();
                jsonArray.add(jsonBody);
                returnObject.put("status", "success");
                returnObject.put("message", "");
                returnObject.put("result", jsonArray);
            }
        }
    }
}