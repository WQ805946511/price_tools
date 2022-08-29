package com.smarting.price.tools.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EcallCost {
    private String carrier;

    private String region;

    private String countryCode;

    private String country;

    private String countryCn;

    private String network;

    private String contractStatus;

    private String ecallFlag;

    private BigDecimal costEcallRmb;

    private String csEcallFlag;

    private String ngEcallFlag;

    private String deliveryCycle;

    private String usePeriod;
}
