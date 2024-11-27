package com.rxlogix.commandObjects

class BatchLotDataCO {

    Long id
    String apiUsername;
    String apiTocken;
    String batchId;
    Date batchDate;
    Integer count;

    //List clientDatas;
    //List<Map<String,Object>> clientDatas;
    def clientDatas;
    String clientDatasString;

    static constraints = {
        id nullable: true
        apiUsername nullable: false, blank: false
        apiTocken nullable: true, blank: false
        clientDatas nullable: true, blank: false
        batchId nullable: true, blank: false
        batchDate nullable: true, blank: false
        clientDatas nullable: true
    }

}
