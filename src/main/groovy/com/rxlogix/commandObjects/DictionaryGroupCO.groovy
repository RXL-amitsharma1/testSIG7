package com.rxlogix.commandObjects

import com.rxlogix.pvdictionary.DictionaryGroupCmd
import com.rxlogix.signal.ProductGroupData

class DictionaryGroupCO {
    Long id
    Long productGroupStatusId
    Long version
    String uniqueIdentifier
    String groupName
    String groupOldName
    Integer type
    String description
    String copyGroups
    String sharedWith
    String owner
    Integer tenantId
    String includeSources
    String data
    String validationError
    String status
    Set<String> includeSourcesToAdd;

    DictionaryGroupCO() { }

    DictionaryGroupCO(Long id, Long productGroupStatusId, Long version, String uniqueIdentifier, String groupName,
                      String groupOldName, Integer type, String description, String copyGroups, String sharedWith,
                      String owner, Integer tenantId, String includeSources, String data, String validationError, String status) {
        this.id = id
        this.productGroupStatusId = productGroupStatusId
        this.version = version
        this.uniqueIdentifier = uniqueIdentifier
        this.groupName = groupName
        this.groupOldName = groupOldName
        this.type = type
        this.description = description
        this.copyGroups = copyGroups
        this.sharedWith = sharedWith
        this.owner = owner
        this.tenantId = tenantId
        this.includeSources = includeSources
        this.data = data
        this.validationError = validationError
        this.status = status
    }

    ProductGroupData getProductGroupData() {
        ProductGroupData prodGroupData = new ProductGroupData()
        prodGroupData.id = this.id
        prodGroupData.productGroupStatusId = this.productGroupStatusId
        prodGroupData.version = this.version
        prodGroupData.uniqueIdentifier = this.uniqueIdentifier
        prodGroupData.groupName = this.groupName
        prodGroupData.type = this.type
        prodGroupData.description = this.description
        prodGroupData.copyGroups = this.copyGroups
        prodGroupData.sharedWith = this.sharedWith
        prodGroupData.owner = this.owner
        prodGroupData.tenantId = this.tenantId
        prodGroupData.includeSources = this.includeSources
        prodGroupData.data = this.data
        prodGroupData.validationError = this.validationError
        prodGroupData.status = this.status
        prodGroupData
    }
    DictionaryGroupCmd getDictionaryGroupCmd() {
        DictionaryGroupCmd dictionaryGroupCmd = new DictionaryGroupCmd()
        dictionaryGroupCmd.id = this.id
        dictionaryGroupCmd.groupName = this.groupName
        dictionaryGroupCmd.type = this.type
        dictionaryGroupCmd.description = this.description
        dictionaryGroupCmd.copyGroups = this.copyGroups
        dictionaryGroupCmd.sharedWith = this.sharedWith
        dictionaryGroupCmd.owner = this.owner
        dictionaryGroupCmd.tenantId = this.tenantId
        dictionaryGroupCmd.data = this.data
        dictionaryGroupCmd
    }
}
