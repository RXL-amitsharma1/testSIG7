package com.rxlogix.commandObjects

import com.rxlogix.signal.BatchLotData

class BatchLotCO {
    Long id;
    String apiUsername;
    String apiTocken;
    String batchId;
    Date batchDate;
    Integer count;

    List<BatchLotData> clientDatas;

    static constraints = {
        apiUsername nullable: false, blank: false
        apiTocken nullable: true, blank: false
        clientDatas nullable: true, blank: false
        batchId nullable: true, blank: false
        batchDate nullable: true, blank: false
        id nullable: true
    }

}
