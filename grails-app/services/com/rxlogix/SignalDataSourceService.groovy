package com.rxlogix

import grails.gorm.transactions.Transactional
import grails.util.GrailsNameUtils
import grails.util.Holders
import groovy.sql.Sql
import org.springframework.jdbc.datasource.SimpleDriverDataSource

import java.sql.Connection

@Transactional
class SignalDataSourceService {
    def dataSource
    def dataSource_pva
    def dataSource_eudra
    def dataSource_faers
    def dataSource_vigibase
    def dataSource_jader

    /**
     * This service method gives the datasource bean instance based on the selected datasource value.
     * @param selectedDatasource
     * @return
     */
    def getDataSource(def selectedDatasource) {
        String beanName = "dataSource_"+GrailsNameUtils.getPropertyName(selectedDatasource)
        if ("dataSource".equals(selectedDatasource)) {
            beanName = "dataSource"
        }
        def ctx = Holders.grailsApplication.mainContext
        ctx.getBean(beanName)
    }

    Connection getReportConnection(selectedDataSource) {
        def classNameOfDs = Holders.config.dataSources."$selectedDataSource".driverClassName
        def userNameOfDs = Holders.config.dataSources."$selectedDataSource".username
//        tODO
//        def passWordOfDs = RxCodec.decode(Holders.config.dataSources."$selectedDataSource".password)
        def passWordOfDs = Holders.config.dataSources."$selectedDataSource".password
        def urlOfDs = Holders.config.dataSources."$selectedDataSource".url

        SimpleDriverDataSource reportDataSource = new SimpleDriverDataSource()
        reportDataSource.driverClass = Class.forName(classNameOfDs)
        reportDataSource.username = userNameOfDs
        reportDataSource.password = passWordOfDs
        reportDataSource.url = urlOfDs

        return reportDataSource.getConnection()
    }
    Connection getReportConnectionWithDisabledAutoCommit(selectedDataSource) {
        def classNameOfDs = Holders.config.dataSources."$selectedDataSource".driverClassName
        def userNameOfDs = Holders.config.dataSources."$selectedDataSource".username
//        tODO
//        def passWordOfDs = RxCodec.decode(Holders.config.dataSources."$selectedDataSource".password)
        def passWordOfDs = Holders.config.dataSources."$selectedDataSource".password
        def urlOfDs = Holders.config.dataSources."$selectedDataSource".url

        SimpleDriverDataSource reportDataSource = new SimpleDriverDataSource()
        reportDataSource.driverClass = Class.forName(classNameOfDs)
        reportDataSource.username = userNameOfDs
        reportDataSource.password = passWordOfDs
        reportDataSource.url = urlOfDs
        Connection connection = reportDataSource.getConnection()
        connection.autoCommit = false
        return connection
    }

    Connection getDefaultDatasourceConnection() {
        def classNameOfDs = Holders.config.dataSources.dataSource.driverClassName
        def userNameOfDs = Holders.config.dataSources.dataSource.username
//        tODO
//        def passWordOfDs = RxCodec.decode(Holders.config.dataSources.dataSource.password)
        def passWordOfDs = Holders.config.dataSources.dataSource.password
        def urlOfDs = Holders.config.dataSources.dataSource.url

        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++")
        log.info("url : " + urlOfDs)
        log.info("driverClassName : " + classNameOfDs)
        log.info("username : " + userNameOfDs)
        log.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++++")

        SimpleDriverDataSource reportDataSource = new SimpleDriverDataSource()
        reportDataSource.driverClass = Class.forName(classNameOfDs)
        reportDataSource.username = userNameOfDs
        reportDataSource.password = passWordOfDs
        reportDataSource.url = urlOfDs

        return reportDataSource.getConnection()
    }

    boolean checkFreshConnection() {
        boolean isConnectionWorking = false
        Sql sql = null
        Connection connection = null
        try {
            connection = getReportConnection("dataSource")
            sql = new Sql(connection)
            String valQuery = 'SELECT 1'
            sql.execute(valQuery)
            isConnectionWorking = true
        } catch (Exception ex) {
            log.error("Fresh connection is also failing", ex.printStackTrace())
            isConnectionWorking = false
        } finally {
            sql?.close()
            connection?.close()
        }
        return isConnectionWorking
    }
}
