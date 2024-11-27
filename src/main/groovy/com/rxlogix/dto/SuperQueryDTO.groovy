package com.rxlogix.dto

import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum

class SuperQueryDTO {
    Long id
    String name
    String description
    boolean hasBlanks = false
    String JSONQuery
    QueryTypeEnum queryType
    String customSQLQuery
    ReassessListednessEnum reassessListedness
    List<QueryDTO> queries = []

    SuperQueryDTO(Map superQuery) {
        id = superQuery.id
        name = superQuery.name
        description = superQuery.description
        queryType = QueryTypeEnum.valueOf(superQuery.queryType)
        JSONQuery = superQuery.JSONQuery
        hasBlanks = superQuery.hasBlanks
        customSQLQuery = superQuery.customSQLQuery
        reassessListedness = superQuery.reassessListedness ? ReassessListednessEnum.valueOf(superQuery.reassessListedness) : null
        if (superQuery.queryType == 'SET_BUILDER') {
            superQuery.queries.each { Map query ->
                QueryDTO queryDTO = new QueryDTO(query)
                queries.add(queryDTO)
            }
        }
    }

    SuperQueryDTO(String ruleJSON, QueryTypeEnum queryTypeEnum){
        this.JSONQuery = ruleJSON
        this.queryType = queryTypeEnum
    }
}
