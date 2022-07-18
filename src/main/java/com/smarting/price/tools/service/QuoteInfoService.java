package com.smarting.price.tools.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.smarting.price.tools.entity.Constant;
import com.smarting.price.tools.entity.Country;
import com.smarting.price.tools.entity.QuoteInfo;
import com.smarting.price.tools.mapper.CountryMapper;
import com.smarting.price.tools.mapper.QuoteInfoMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuoteInfoService {
    @Autowired
    private QuoteInfoMapper quoteInfoMapper;
    @Autowired
    private CountryMapper countryMapper;

    public JSONObject getReturnObject(String params) {
        JSONObject jsonParams = JSONObject.parseObject(params);
        String quotationMethod = jsonParams.getString("quotationMethod");
        String flowSpecifications = jsonParams.getString("flowSpecifications");
        JSONArray countryArray = jsonParams.getJSONArray("countrys");
        // 根据城市查询
        List<QuoteInfo> quoteInfoAllList = getQuoteInfoList(jsonParams, flowSpecifications, countryArray);

        // 区域查询后根据大区分组
        List<Country> countryList = countryMapper.queryAreasCn();
        Map<String, List<Country>> countryMap = countryList.stream().collect(Collectors.groupingBy(Country::getCountryCode));

        if (CollectionUtils.isEmpty(quoteInfoAllList)) {
            return getFailObject();
        }
        return getShowObject(jsonParams, quotationMethod, quoteInfoAllList, countryMap);
    }

    private List<QuoteInfo> getQuoteInfoList(JSONObject jsonParams, String flowSpecifications, JSONArray countryArray) {
        List<QuoteInfo> quoteInfoAllList = quoteInfoMapper.getQuoteInfoList(flowSpecifications, countryArray);
        String serviceType = jsonParams.getString("serviceType");
        return quoteInfoAllList.stream().filter(quoteInfo -> {
            String flag = StringUtils.equalsIgnoreCase(serviceType, Constant.PREMIUM_SERVICE) ?
                    quoteInfo.getPremiumFlag() : quoteInfo.getEssentialFlag();
            return StringUtils.equalsIgnoreCase(flag, "yes");
        }).collect(Collectors.toList());
    }

    private JSONObject getFailObject() {
        JSONObject jsonObject = new JSONObject();
        // 查询结果为空的情况直接返回失败
        jsonObject.put("status", "failed");
        jsonObject.put("message", "result is empty");
        return jsonObject;
    }

    private JSONObject getShowObject(JSONObject jsonParams, String quotationMethod, List<QuoteInfo> quoteInfoAllList,
                                     Map<String, List<Country>> countryMap) {
        JSONArray jsonArray = new JSONArray();
        if (StringUtils.equalsIgnoreCase(quotationMethod, Constant.SINGLE_COUNTRY)) {
            // 单个国家报价
            singleCountry(jsonParams, quoteInfoAllList, jsonArray, countryMap);
        } else if (StringUtils.equalsIgnoreCase(quotationMethod, Constant.MANY_COUNTRY)) {
            // 多个国家组合报价(多个多加组合取交集，就是大家都有的carrier)
            List<QuoteInfo> quoteInfoSharedList = getQuoteInfoSharedList(jsonParams, quoteInfoAllList);
            getJsonArray(jsonParams, jsonArray, quoteInfoSharedList, countryMap);
        }
        if (CollectionUtils.isEmpty(jsonArray)) {
            return getFailObject();
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", "success");
        jsonObject.put("message", "");
        jsonObject.put("result", jsonArray);
        return jsonObject;
    }

    private List<QuoteInfo> getQuoteInfoSharedList(JSONObject jsonParams, List<QuoteInfo> quoteInfoAllList) {
        JSONArray countryArray = jsonParams.getJSONArray("countrys");
        Map<String, List<QuoteInfo>> carrierMap =
                quoteInfoAllList.stream().collect(Collectors.groupingBy(QuoteInfo::getCarrier));
        List<QuoteInfo> quoteInfoSharedList = new ArrayList<>();
        carrierMap.forEach((carrier, quoteInfoList) -> {
            Map<String, List<QuoteInfo>> countryCodeMap =
                    quoteInfoList.stream().collect(Collectors.groupingBy(QuoteInfo::getCountryCode));
            if (countryCodeMap.size() == countryArray.size()) {
                // 筛选出每个国家都支持的carrier
                quoteInfoSharedList.addAll(quoteInfoList);
            }
        });
        return quoteInfoSharedList;
    }

    private void singleCountry(JSONObject jsonParams, List<QuoteInfo> quoteInfoAllList, JSONArray jsonArray,
                               Map<String, List<Country>> countryMap) {
        // 按照国家分组
        Map<String, List<QuoteInfo>> groupByCountryMap =
                quoteInfoAllList.stream().collect(Collectors.groupingBy(QuoteInfo::getCountryCode));
        // 由于country表和cost_table表中城市名称不统一，现改为country成功中文统一显示
        List<Country> countryList = countryMapper.queryAllCountrys();
        Map<String, List<Country>> groupByCountryCodeMap =
                countryList.stream().collect(Collectors.groupingBy(Country::getCountryCode));
        groupByCountryMap.forEach((countryCode, quoteInfoByCountryList) -> {
            quoteInfoByCountryList = getQuoteInfoByCountryList(groupByCountryCodeMap, countryCode, quoteInfoByCountryList);
            getJsonArray(jsonParams, jsonArray, quoteInfoByCountryList, countryMap);
        });
    }

    private List<QuoteInfo> getQuoteInfoByCountryList(Map<String, List<Country>> groupByCountryCodeMap, String countryCode,
                                          List<QuoteInfo> quoteInfoByCountryList) {
        List<Country> countryCodeList = groupByCountryCodeMap.get(countryCode);
        if (CollectionUtils.isNotEmpty(countryCodeList)) {
            Country country = countryCodeList.get(0);
            quoteInfoByCountryList =
                    quoteInfoByCountryList.stream().peek(quoteInfo -> quoteInfo.setCountryCn(country.getCountry())).collect(Collectors.toList());
        }
        return quoteInfoByCountryList;
    }

    private void getJsonArray(JSONObject jsonParams, JSONArray jsonArray, List<QuoteInfo> quoteInfoByCountryList,
                              Map<String, List<Country>> countryMap) {
        // 分组找到Carrier中最大的价格（相同的Carrier取最大的报价）
        List<QuoteInfo> quoteInfoMaxList = getQuoteInfoMaxList(quoteInfoByCountryList, jsonParams);
        if (CollectionUtils.isNotEmpty(quoteInfoMaxList)) {
            // 按照查询出每个carrier最大值的集合再按照规格价格从低到高排序（前台页面排序显示）
            List<QuoteInfo> sortedQuoteInfoMaxList =
                    quoteInfoMaxList.stream().filter(quoteInfo -> quoteInfo.getSpecificationPrice() != null)
                            .sorted(Comparator.comparing(QuoteInfo::getSpecificationPrice)).collect(Collectors.toList());
            JSONObject jsonCountry = new JSONObject();
            String flowSpecifications = jsonParams.getString("flowSpecifications");
            jsonCountry.put("flowSpecifications", flowSpecifications);

            // 设置tab下拉框，以上排序的结果展示
            JSONObject jsonShowTab = getJsonShowTab(jsonParams, quoteInfoByCountryList, sortedQuoteInfoMaxList);
            jsonCountry.put("showTab", jsonShowTab);

            // 第一个详细信息展示或查询的对于carrier的详细信息展示
            QuoteInfo quoteInfoMin = getQuoteInfoMin(quoteInfoByCountryList, sortedQuoteInfoMaxList, jsonParams,
                    countryMap);
            if (quoteInfoMin != null) {
                jsonCountry.put("data", quoteInfoMin);
                jsonArray.add(jsonCountry);
            }
        }
    }

    private QuoteInfo getQuoteInfoMin(List<QuoteInfo> quoteInfoByCountryList, List<QuoteInfo> sortedQuoteInfoMaxList,
                                      JSONObject jsonParams, Map<String, List<Country>> countryMap) {
        // 排序后直接选择第一个值，作为国家前台的展示数据，并且讲满足的Carrier的network进行组合
        String carrierQuery = jsonParams.getString("carrier");
        // 这边在默认情况的使用排序的第一个，在选择Carrier的时候优先
        QuoteInfo quoteInfoMin = StringUtils.isEmpty(carrierQuery) ? sortedQuoteInfoMaxList.get(0) :
                sortedQuoteInfoMaxList.stream().filter(quoteInfo -> StringUtils.equals(quoteInfo.getCarrier(),
                        carrierQuery)).findFirst().orElse(null);
        if (quoteInfoMin == null) {
            // 如果等于null代表未能找到需要的Carrier,则空白显示
            QuoteInfo quoteInfo = sortedQuoteInfoMaxList.get(0);
            quoteInfoMin = new QuoteInfo();
            quoteInfoMin.setRegion(quoteInfo.getRegion());
            String regionCn = getRegionCn(countryMap, quoteInfoMin);
            quoteInfoMin.setRegionCn(regionCn);
            quoteInfoMin.setCountryCode(quoteInfo.getCountryCode());
            // 这边也用

            quoteInfoMin.setCountryCn(quoteInfo.getCountryCn());
            quoteInfoMin.setCountry(quoteInfo.getCountry());
            // 多个国家地区展示信息设置
            setManyCountryInfo(jsonParams, quoteInfoMin);
        } else {
            Set<String> networkSet = getNetworkSet(quoteInfoByCountryList, quoteInfoMin);
            // 同一个Carrier的networks整合用，链接展示
            String networks = String.join(",", networkSet);
            quoteInfoMin.setNetwork(networks);
            // 匹配设置下大区的中文展示
            String regionCn = getRegionCn(countryMap, quoteInfoMin);
            quoteInfoMin.setRegionCn(regionCn);
            // 多个国家地区展示信息设置
            setManyCountryInfo(jsonParams, quoteInfoMin);
        }
        return quoteInfoMin;
    }

    private Set<String> getNetworkSet(List<QuoteInfo> quoteInfoByCountryList, QuoteInfo quoteInfoMin) {
        return quoteInfoByCountryList.stream().filter(quoteInfo -> StringUtils.equals(quoteInfo.getCarrier(),
                quoteInfoMin.getCarrier())).map(quoteInfo -> StringUtils.isNotEmpty(quoteInfo.getNetwork()) ?
                quoteInfo.getNetwork() : "").collect(Collectors.toSet());
    }

    private void setManyCountryInfo(JSONObject jsonParams, QuoteInfo quoteInfoMin) {
        String quotationMethod = jsonParams.getString("quotationMethod");
        if (StringUtils.equalsIgnoreCase(quotationMethod, Constant.MANY_COUNTRY)) {
            // 多个国家网络不展示
            quoteInfoMin.setNetwork("");
            // 多个国家地区不显示
            quoteInfoMin.setRegion("");
            quoteInfoMin.setRegionCn("");
            // 多个国家地区的CountryCode整合用，链接展示
            String countrys = getCountrys(jsonParams, "countrys");
            quoteInfoMin.setCountryCode(countrys);
            // 多个国家地区的CountryCn整合用，链接展示
            String countryCns = getCountrys(jsonParams, "countryCN");
            quoteInfoMin.setCountryCn(countryCns);
        }
    }

    private String getRegionCn(Map<String, List<Country>> countryMap, QuoteInfo quoteInfoMin) {
       /* String region = quoteInfoMin.getRegion();*/
        String countryCode = quoteInfoMin.getCountryCode();
        List<Country> countryList = countryMap.get(countryCode);
        return CollectionUtils.isNotEmpty(countryList) ? countryList.get(0).getRegionCn() : "";
    }

    private String getCountrys(JSONObject jsonParams, String key) {
        JSONArray countryCnArray = jsonParams.getJSONArray(key);
        Set<String> countryCnSet = countryCnArray.stream().map(String::valueOf).collect(Collectors.toSet());
        return String.join(",", countryCnSet);
    }

    private JSONObject getJsonShowTab(JSONObject jsonParams, List<QuoteInfo> quoteInfoByCountryList,
                                      List<QuoteInfo> sortedQuoteInfoMaxList) {
        String serviceType = jsonParams.getString("serviceType");
        // showTab为每个Carrier按照价格从低到高展示
        JSONObject jsonShowTab = new JSONObject(new LinkedHashMap<>());
        sortedQuoteInfoMaxList.forEach(sortedQuoteInfo -> {
            jsonShowTab.put(sortedQuoteInfo.getCarrier(), false);
            // Premium服务是找到两个以上的网络进行标记，Essential不用标记
            setPremiumShowTab(quoteInfoByCountryList, serviceType, jsonShowTab, sortedQuoteInfo);
        });
        return jsonShowTab;
    }

    private void setPremiumShowTab(List<QuoteInfo> quoteInfoByCountryList, String serviceType, JSONObject jsonShowTab
            , QuoteInfo sortedQuoteInfo) {
        if (StringUtils.equalsIgnoreCase(serviceType, Constant.PREMIUM_SERVICE)) {
            // 这边是拿到所有满足的Carrier（资源方的网络展示）
            Set<String> networkSet =
                    quoteInfoByCountryList.stream().filter(quoteInfo -> StringUtils.equals(quoteInfo.getCarrier(),
                            sortedQuoteInfo.getCarrier())).map(QuoteInfo::getNetwork).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(networkSet) && networkSet.size() > 1) {
                jsonShowTab.put(sortedQuoteInfo.getCarrier(), true);
            }
        }
    }

    private List<QuoteInfo> getQuoteInfoMaxList(List<QuoteInfo> quoteInfoAllList, JSONObject jsonParams) {
        // 按照Carrier进行分组
        Map<String, List<QuoteInfo>> groupByCarrierMap =
                quoteInfoAllList.stream().collect(Collectors.groupingBy(QuoteInfo::getCarrier));

        List<QuoteInfo> quoteInfoMaxList = new ArrayList<>();
        String serviceType = jsonParams.getString("serviceType");
        groupByCarrierMap.forEach((carrier, quoteInfoByCarrierList) -> {
            // premium与essential在相同的carrier选择最大值的逻辑相同
            Optional<QuoteInfo> max = getQuoteInfoOptional(serviceType, quoteInfoByCarrierList);
            if (max.isPresent()) {
                QuoteInfo quoteInfoMax = max.get();
                quoteInfoMaxList.add(quoteInfoMax);
            }
        });
        return quoteInfoMaxList;
    }

    private Optional<QuoteInfo> getQuoteInfoOptional(String serviceType, List<QuoteInfo> quoteInfoByCarrierList) {
        return quoteInfoByCarrierList.stream().filter(quoteInfo -> {
            String flag = StringUtils.equalsIgnoreCase(serviceType, Constant.PREMIUM_SERVICE) ?
                    quoteInfo.getPremiumFlag() : quoteInfo.getEssentialFlag();
            return StringUtils.equalsIgnoreCase(flag, "yes");
        }).max(Comparator.comparing(QuoteInfo::getSpecificationPrice));
    }
}
