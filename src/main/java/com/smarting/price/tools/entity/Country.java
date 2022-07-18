package com.smarting.price.tools.entity;

import lombok.Data;

@Data
public class Country {
    // id
    private Integer id;
    // 国家
    private String country;
    // 国家简称
    private String countryCode;
     // 地区
    private String region;
    // 地区中文
    private String regionCn;
}
