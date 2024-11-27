import com.rxlogix.Constants
import com.rxlogix.ReportExecutorService
import com.rxlogix.SignalDataSourceService
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.json.JsonOutput
import groovy.sql.Sql
import oracle.jdbc.Const

databaseChangeLog = {

    changeSet(author: "bhupender (generated)", id: "202308081524-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'CRITERIA_COUNTS')
            }
        }
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "CRITERIA_COUNTS", type: "varchar2(8000 CHAR)") {
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "202308081524-03"){
        grailsChange{
            change{
                try {
                    List<ExecutedConfiguration> ecList = ExecutedConfiguration.getAll()
                    ReportExecutorService reportExecutorService = ctx.getBean("reportExecutorService")
                    String criteriaCountJson
                    List<Long> pvaList = []
                    List<Long> faersList = []
                    List<Long> vaersList = []
                    List<Long> vigibaseList = []
                    ecList?.each { ExecutedConfiguration executedConfiguration ->
                        if(executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                            if(executedConfiguration.selectedDatasource.contains(Constants.DataSource.PVA)){
                                pvaList.add(executedConfiguration.id)
                                if(pvaList.size() == 1000){
                                    Map criteriaCountMap = reportExecutorService.fetchCriteriaCountChangeLogPVA(pvaList,Constants.DataSource.PVA)
                                    criteriaCountMap.each { executedConfigurationId , criteriaMap ->
                                        ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                                        ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                                        ec.save(flush:true)
                                    }
                                    pvaList.clear()
                                }
                            }
                            if(executedConfiguration.selectedDatasource.contains(Constants.DataSource.FAERS) && executedConfiguration.selectedDatasource.startsWith(Constants.DataSource.FAERS)){
                                faersList.add(executedConfiguration.id)
                                if(faersList.size() == 1000){
                                    Map criteriaCountMap = reportExecutorService.fetchCriteriaCountChangelogFaers(faersList,Constants.DataSource.FAERS)
                                    criteriaCountMap.each { executedConfigurationId , criteriaMap ->
                                        ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                                        ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                                        ec.save(flush:true)
                                    }
                                    faersList.clear()
                                }
                            }
                            if(executedConfiguration.selectedDatasource.contains(Constants.DataSource.VAERS) && executedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VAERS)){
                                vaersList.add(executedConfiguration.id)
                                if(vaersList.size() == 1000){
                                    Map criteriaCountMap = reportExecutorService.fetchCriteriaCountVaersChangelog(vaersList,Constants.DataSource.VAERS)
                                    criteriaCountMap.each { executedConfigurationId , criteriaMap ->
                                        ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                                        ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                                        ec.save(flush:true)
                                    }
                                    vaersList.clear()
                                }
                            }
                            if(executedConfiguration.selectedDatasource.contains(Constants.DataSource.VIGIBASE) && executedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE)){
                                vigibaseList.add(executedConfiguration.id)
                                if(vigibaseList.size() == 1000){
                                    Map criteriaCountMap = reportExecutorService.fetchCriteriaCountVigiBaseChangelog(vigibaseList,Constants.DataSource.VIGIBASE)
                                    criteriaCountMap.each { executedConfigurationId , criteriaMap ->
                                        ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                                        ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                                        ec.save(flush:true)
                                    }
                                    vigibaseList.clear()
                                }
                            }
                        }
                    }
                    if(pvaList){
                        Map criteriaCountMap = reportExecutorService.fetchCriteriaCountChangeLogPVA(pvaList,Constants.DataSource.PVA)
                        criteriaCountMap.each { executedConfigurationId , criteriaMap ->
                            ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                            ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                            ec.save(flush:true)
                        }
                        pvaList.clear()
                    }
                    if (faersList) {
                        Map criteriaCountMap = reportExecutorService.fetchCriteriaCountChangelogFaers(faersList, Constants.DataSource.FAERS)
                        criteriaCountMap.each { executedConfigurationId, criteriaMap ->
                            ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                            ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                            ec.save(flush: true)
                        }
                        faersList.clear()
                    }
                    if(vaersList){
                        Map criteriaCountMap = reportExecutorService.fetchCriteriaCountVaersChangelog(vaersList,Constants.DataSource.VAERS)
                        criteriaCountMap.each { executedConfigurationId , criteriaMap ->
                            ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                            ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                            ec.save(flush:true)
                        }
                        vaersList.clear()
                    }
                    if(vigibaseList){
                        Map criteriaCountMap = reportExecutorService.fetchCriteriaCountVigiBaseChangelog(vigibaseList,Constants.DataSource.VIGIBASE)
                        criteriaCountMap.each { executedConfigurationId , criteriaMap ->
                            ExecutedConfiguration ec = ExecutedConfiguration.get(executedConfigurationId as Long)
                            ec.criteriaCounts = JsonOutput.toJson(criteriaMap)
                            ec.save(flush:true)
                        }
                        vigibaseList.clear()
                    }
                } catch (Exception ex){
                    println "#### Error occurred while updating criteriaCount column in ex_rconfig"
                    ex.printStackTrace()
                }
            }
        }
    }
}