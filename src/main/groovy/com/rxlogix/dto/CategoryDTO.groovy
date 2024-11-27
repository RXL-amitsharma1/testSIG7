package com.rxlogix.dto

class CategoryDTO implements Cloneable {
    Long martId;
    Boolean alertLevel;
    Integer factGrpId;
    Long catId;
    Long subCatId;
    String catName;
    String subCatName;
    String dmlType;
    String module;
    String dataSource;
    Long privateUserId;
    Integer priority;
    String createdBy;
    String createdDate;
    String updatedBy;
    String updatedDate;
    Integer isAutoTagged;
    Integer isRetained;
    Long udNumber1;
    Long udNumber2;
    Long udNumber3;
    String udText1;
    String udText2;
    String udText3;
    String udText4;
    String udDate1;
    String udDate2;
    String factGrpCol1;
    String factGrpCol2;
    String factGrpCol3;
    String factGrpCol4;
    String factGrpCol5;
    String factGrpCol6;
    String factGrpCol7;
    String factGrpCol8;
    String factGrpCol9;
    String factGrpCol10;
    String alertId;
    Boolean isAdhoc = false

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "alertLevel=" + alertLevel +
                ", factGrpId=" + factGrpId +
                ", catId=" + catId +
                ", subCatId=" + subCatId +
                ", catName='" + catName + '\'' +
                ", subCatName='" + subCatName + '\'' +
                ", dmlType='" + dmlType + '\'' +
                ", privateUserId=" + privateUserId +
                '}';
    }
}
