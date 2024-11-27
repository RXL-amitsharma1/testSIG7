package com.rxlogix.commandObjects

import com.rxlogix.signal.ProductGroupData

class DictionaryGroupStatusCO {
    Integer id;
    String uniqueIdentifier;
    Integer count;
    String apiUsername
    String apiTocken
    List<ProductGroupData> productGroups;
    List<ProductGroupCO> productGroupList;
    List<DictionaryGroupCO> dictionaryGroups;
    String productGroupsString;
    String pvrHttpError

}
