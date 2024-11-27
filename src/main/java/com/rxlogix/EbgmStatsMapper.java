package com.rxlogix;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;

import java.math.BigInteger;

@JsonDeserialize(as = EbgmStatsMapper.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EbgmStatsMapper {

    public BigInteger product_Id;

    public String pt_Id;

    public double ebgm;

    public double eb05;

    public double eb95;

    public EbgmSubGroup ebgm_age;

    public EbgmSubGroup eb05_age;

    public EbgmSubGroup eb95_age;

    public EbgmSubGroup ebgm_gender;

    public EbgmSubGroup eb05_gender;

    public EbgmSubGroup eb95_gender;

    public BigInteger getProduct_Id() {
        return product_Id;
    }

    public void setProduct_Id(BigInteger product_Id) {
        this.product_Id = product_Id;
    }

    public String getPt_Id() {
        return pt_Id;
    }

    public void setPt_Id(String pt_Id) {
        this.pt_Id = pt_Id;
    }

    public double getEbgm() {
        return ebgm;
    }

    public void setEbgm(double ebgm) {
        this.ebgm = ebgm;
    }

    public double getEb05() {
        return eb05;
    }

    public void setEb05(double eb05) {
        this.eb05 = eb05;
    }

    public double getEb95() {
        return eb95;
    }

    public void setEb95(double eb95) {
        this.eb95 = eb95;
    }

    public EbgmSubGroup getEbgm_age() {
        return ebgm_age;
    }

    public void setEbgm_age(EbgmSubGroup ebgm_age) {
        this.ebgm_age = ebgm_age;
    }

    public EbgmSubGroup getEb05_age() {
        return eb05_age;
    }

    public void setEb05_age(EbgmSubGroup eb05_age) {
        this.eb05_age = eb05_age;
    }

    public EbgmSubGroup getEb95_age() {
        return eb95_age;
    }

    public void setEb95_age(EbgmSubGroup eb95_age) {
        this.eb95_age = eb95_age;
    }

    public EbgmSubGroup getEbgm_gender() {
        return ebgm_gender;
    }

    public void setEbgm_gender(EbgmSubGroup ebgm_gender) {
        this.ebgm_gender = ebgm_gender;
    }

    public EbgmSubGroup getEb05_gender() {
        return eb05_gender;
    }

    public void setEb05_gender(EbgmSubGroup eb05_gender) {
        this.eb05_gender = eb05_gender;
    }

    public EbgmSubGroup getEb95_gender() {
        return eb95_gender;
    }

    public void setEb95_gender(EbgmSubGroup eb95_gender) {
        this.eb95_gender = eb95_gender;
    }

    public static <T> T fromJSON(String json, Class<T> clazz) {
        return new Gson().fromJson(json, clazz);
    }

}
