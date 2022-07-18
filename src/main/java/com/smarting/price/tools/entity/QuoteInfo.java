package com.smarting.price.tools.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class QuoteInfo implements Serializable {
    // 资源方
    private String carrier;
    // 大区
    private String region;
    // 国家简称
    private String countryCode;
    // 国家
    private String country;
    // 国家中文
    private String countryCn;
    // essential服务
    private String essentialFlag;
    // premium服务
    private String premiumFlag;
    // 运营商
    private String network;
    // 规格价格
    private BigDecimal specificationPrice;
    // 是否可以2g
    private String twoG;
    // 是否可以3g
    private String threeG;
    // 是否可以4g
    private String lte;
    // 是否可以LTE-M
    private String ltem;
    // 是否永久漫游
    private String perRoaming;
    // 短信价格
    private BigDecimal priceSms;
    // 资源占用价格
    private BigDecimal pricePaygMrc;
    // 超出套餐1m价格
    private BigDecimal overstepMealOneMb;
    // 地区中文
    private String regionCn;
    // 是否可以多apn
    private String apn;
    // 交付周期
    private String times;
}
