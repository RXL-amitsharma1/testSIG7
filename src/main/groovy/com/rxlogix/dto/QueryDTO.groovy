package com.rxlogix.dto

import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum

class QueryDTO {
    Long id
    String name
    String description
    boolean hasBlanks = false
    String JSONQuery
    QueryTypeEnum queryType
    String customSQLQuery
    ReassessListednessEnum reassessListedness

    QueryDTO(Map query){
        id = query.id
        name = query.name
        description = query.description
        queryType = QueryTypeEnum.valueOf(query.queryType)
        JSONQuery = query.JSONQuery
        hasBlanks = query.hasBlanks
        customSQLQuery = query.customSQLQuery
        reassessListedness = query.reassessListedness ? ReassessListednessEnum.valueOf(query.reassessListedness) : null
    }
}
