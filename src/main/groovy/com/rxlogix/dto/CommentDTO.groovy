package com.rxlogix.dto

import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.GlobalCaseCommentMapping

public class CommentDTO {

    private Long tenantId;
    private Integer versionNum;
    private Integer followUpNum;
    private Long caseId;
    private String oldCommentTxt;
    private String newCommentTxt;
    private String module;
    private String subModule;
    private String alertName;
    private Long configId;
    private Long exConfigId;

    private String createdBy;
    private String createdDate;
    private String updatedBy;
    private String updatedDate;

    private Integer udNumber1;
    private Integer udNumber2;
    private Integer udNumber3;
    private String udText1;
    private String udText2;
    private String udText3;
    private String udDate1;
    private String udDate2;
    private Long commentId;
    private String dataSource

    private String productFamily
    private String productName
    private String EventName
    private BigInteger productId
    private Integer ptCode
    private String caseNumber

    String getCaseNumber() {
        return caseNumber
    }

    void setCaseNumber(String caseNumber) {
        this.caseNumber = caseNumber
    }

    String getProductFamily() {
        return productFamily
    }

    void setProductFamily(String productFamily) {
        this.productFamily = productFamily
    }

    String getProductName() {
        return productName
    }

    void setProductName(String productName) {
        this.productName = productName
    }

    String getEventName() {
        return EventName
    }

    void setEventName(String eventName) {
        EventName = eventName
    }

    BigInteger getProductId() {
        return productId
    }

    void setProductId(BigInteger productId) {
        this.productId = productId
    }

    Integer getPtCode() {
        return ptCode
    }

    void setPtCode(Integer ptCode) {
        this.ptCode = ptCode
    }

    String getDataSource() {
        return dataSource
    }

    void setDataSource(String dataSource) {
        this.dataSource = dataSource
    }

    Long getCommentId() {
        return commentId
    }

    void setCommentId(Long commentId) {
        this.commentId = commentId
    }


    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }

    public Integer getFollowUpNum() {
        return followUpNum;
    }

    public void setFollowUpNum(Integer followUpNum) {
        this.followUpNum = followUpNum;
    }

    public Long getCaseId() {
        return caseId;
    }

    public void setCaseId(Long caseId) {
        this.caseId = caseId;
    }

    public String getOldCommentTxt() {
        return oldCommentTxt;
    }

    public void setOldCommentTxt(String oldCommentTxt) {
        this.oldCommentTxt = oldCommentTxt;
    }

    public String getNewCommentTxt() {
        return newCommentTxt;
    }

    public void setNewCommentTxt(String newCommentTxt) {
        this.newCommentTxt = newCommentTxt;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getSubModule() {
        return subModule;
    }

    public void setSubModule(String subModule) {
        this.subModule = subModule;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getExConfigId() {
        return exConfigId;
    }

    public void setExConfigId(Long exConfigId) {
        this.exConfigId = exConfigId;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public Integer getUdNumber1() {
        return udNumber1;
    }

    public void setUdNumber1(Integer udNumber1) {
        this.udNumber1 = udNumber1;
    }

    public Integer getUdNumber2() {
        return udNumber2;
    }

    public void setUdNumber2(Integer udNumber2) {
        this.udNumber2 = udNumber2;
    }

    public Integer getUdNumber3() {
        return udNumber3;
    }

    public void setUdNumber3(Integer udNumber3) {
        this.udNumber3 = udNumber3;
    }

    public String getUdText1() {
        return udText1;
    }

    public void setUdText1(String udText1) {
        this.udText1 = udText1;
    }

    public String getUdText2() {
        return udText2;
    }

    public void setUdText2(String udText2) {
        this.udText2 = udText2;
    }

    public String getUdText3() {
        return udText3;
    }

    public void setUdText3(String udText3) {
        this.udText3 = udText3;
    }

    public String getUdDate1() {
        return udDate1;
    }

    public void setUdDate1(String udDate1) {
        this.udDate1 = udDate1;
    }

    public String getUdDate2() {
        return udDate2;
    }

    public void setUdDate2(String udDate2) {
        this.udDate2 = udDate2;
    }

    CommentDTO getCommentDtoObject(CommentDTO globalComment, AlertComment alertComment, def commentInput){
        globalComment.versionNum = commentInput.getAt('caseVersion') as Integer
        globalComment.createdBy = alertComment.createdBy
        globalComment.configId =  alertComment.configId
        globalComment.caseId = commentInput.getAt('caseId') as Long
        globalComment.alertName = commentInput.getAt('alertName')?.replaceAll("'", "''")
        globalComment.createdDate = alertComment.dateCreated
        globalComment.exConfigId = commentInput.getAt('executedConfigId') as Long
        globalComment.followUpNum = commentInput.getAt('followUpNumber') as Integer
        globalComment.module = "PVS"
        globalComment.newCommentTxt = commentInput.getAt('comments')?.replaceAll("'", "''")
        globalComment.subModule = "PVS"
        globalComment.tenantId = 1
        globalComment.productFamily = alertComment.productFamily?.replaceAll("'", "''")
        globalComment.productName = alertComment.productName?.replaceAll("'", "''")
        globalComment.eventName = alertComment.eventName?.replaceAll("'", "''")
        globalComment.productId = alertComment.productId
        globalComment.ptCode = alertComment.ptCode
        globalComment.caseNumber = alertComment.caseNumber
        globalComment
    }

    CommentDTO getUpdateCommentDtoObject(CommentDTO globalComment, GlobalCaseCommentMapping alertComment, def params){
        globalComment.versionNum = params.int("caseVersion")
        globalComment.createdBy = alertComment.createdBy
        globalComment.configId =  params.long("configId")
        globalComment.caseId = alertComment.caseId
        globalComment.alertName = params.alertName?.replaceAll("'", "''")
        globalComment.exConfigId = params.long("executedConfigId")
        globalComment.followUpNum = params.int("followUpNumber")
        globalComment.module = "PVS"
        globalComment.newCommentTxt = alertComment.comments.replaceAll("'","''")
        globalComment.subModule = "PVS"
        globalComment.tenantId = 1
        globalComment.productFamily = alertComment.productFamily?.replaceAll("'", "''")
        globalComment.productName = alertComment.productName?.replaceAll("'", "''")
        globalComment.eventName = alertComment.eventName?.replaceAll("'", "''")
        globalComment.productId = alertComment.productId
        globalComment.ptCode = alertComment.ptCode
        globalComment.caseNumber = alertComment.caseNumber
        globalComment
    }

}

