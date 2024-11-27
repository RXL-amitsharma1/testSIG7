package com.rxlogix
import grails.util.Holders

import groovy.sql.Sql

class DataBaseConnectionCheckInterceptor {
    def signalDataSourceService
    DataBaseConnectionCheckInterceptor() {
        if (Holders.getGrailsApplication().getConfig().signal.dataBaseInterceptor.check) {
            match(controller: '*', action: 'create')
            match(controller: '*', action: 'details')
            match(controller: '*', action: 'index')
            match(controller: '*', action: 'changeDisposition')
            match(controller: '*', action: 'save')
            match(controller: '*', action: 'review')
            match(controller: '*', action: 'adhocReview')
            match(controller: '*', action: 'executionStatus')
            match(controller: '*', action: 'importScreen')
            match(controller: '*', action: 'view')
            match(controller: '*', action: 'changeAssignedToGroup')
            match(controller: '*', action: 'signalWorkflowRule')
            match(controller: '*', action: 'list')
            match(controller: '*', action: 'createSignalWorkflowRule')
            match(controller: '*', action: 'signalWorkflowList')
            match(controller: '*', action: 'edit')
            match(controller: '*', action: 'caseDetail')
            match(controller: '*', action: 'saveAlertCategories')
            match(controller: '*', action: 'upload')
            match(controller: '*', action: 'saveComment')
        }
    }

    boolean before() {
        boolean dbStatus = checkDataBaseConnection()
        if (!dbStatus) {
            forward(controller: 'errors', action: 'databaseError')
            return false
        }
        return true
    }

    boolean after() { true }

    void afterView() {
    }
    boolean checkDataBaseConnection(){
        Sql sql
        Sql sql1
        try {
            def grailsApplication = Holders.getGrailsApplication()
            def dataSource = grailsApplication.mainContext.getBean('dataSource')
            sql = new Sql(dataSource)
            sql1 = new Sql(signalDataSourceService.getReportConnection("dataSource"))
            String valQuery = 'SELECT 1'
            sql.execute(valQuery)
            sql1.execute(valQuery)
            return true
        } catch (Exception ex){
            ex.printStackTrace()
            return false
        } finally {
            sql?.close()
            sql1?.close()
        }
    }
}
