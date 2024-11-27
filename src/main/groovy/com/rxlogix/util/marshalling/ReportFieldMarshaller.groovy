package com.rxlogix.util.marshalling


import com.rxlogix.config.ReportField
import grails.converters.JSON

class ReportFieldMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ReportField) { ReportField rf ->
            def map = [:]
            map['id'] = rf.id
            map['name'] = rf.name
            map['description'] = rf.description
            map['dataType'] = rf.dataType
            map['fieldGroup'] = rf.fieldGroup.name
            map['listDomainClass'] = rf.listDomainClass
            map['transform'] = rf.transform
            map['isText'] = rf.isText
            map['isAutocomplete'] = rf.isAutocomplete
            map['isNonCacheSelectable'] = rf.nonCacheSelectable
            return map
        }
    }
}
