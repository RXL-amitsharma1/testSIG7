package com.rxlogix

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
class QueryServiceSpec extends Specification implements ServiceUnitTest<QueryService> {
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()
    public static final user = "unitTest"
    def JSONQuery = """{ "all": { "containerGroups": [   { "expressions": [  
            { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""

    void "test for getQueryListForBusinessConfiguration()" () {
        setup:
        ReportIntegrationService reportIntegrationService = Mock(ReportIntegrationService)
        service.reportIntegrationService = reportIntegrationService
        Map queryOutput = [queryList:[[id:1 , name:'Date Filter' , owner:'Signaldev' , name:'Date Filter'] , [id:1 , name:'Date Filter' , owner:'Signaldev' , name:'Date Filter']]]
        reportIntegrationService.getQueryList("", 0, 9999, true) >> queryOutput

        when:
        List queryList = service.getQueryListForBusinessConfiguration()

        then:
        queryList.size() == 1
    }
}
