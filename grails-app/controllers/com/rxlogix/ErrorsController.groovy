package com.rxlogix

import grails.util.Environment
import grails.util.Holders
import groovy.sql.Sql
import grails.plugin.springsecurity.annotation.Secured
import java.sql.SQLException

@Secured('permitAll')
class ErrorsController {
    def signalDataSourceService

    def forbidden = {
        render view: '/errors/error403'
    }

    def notFound = {
        render view: '/errors/error404'
    }

    def configurationNotFound = {
        render view: '/errors/configurationError406'
    }

    def notAllowed = {
        render view: '/errors/error405'
    }

    def serverError = {
        Boolean isProductionEnv = Environment.current == Environment.PRODUCTION
        boolean dbStatus = checkDataBaseConnection()
        if (!dbStatus) {
            databaseError()
        } else {
            render view: '/errors/error500', model: [isProductionEnv: isProductionEnv]
        }
    }

    def permissionsError = {
        render view: '/errors/errorPermissions'
    }

    def databaseError(){
        render(view: 'dbError')
    }

    def alertInProgressError(){
        render(view: 'alertInProgressError')
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
        } catch (SQLException sqe) {
            sqe.printStackTrace()
            return false
        } catch (Exception ex){
            ex.printStackTrace()
            return false
        } finally {
            sql?.close()
            sql1?.close()
        }
    }
}
