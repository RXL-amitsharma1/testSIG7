package com.rxlogix

import com.rxlogix.mart.MartTags
import com.rxlogix.signal.GlobalArticle
import com.rxlogix.signal.GlobalCase
import com.rxlogix.signal.GlobalProductEvent
import com.rxlogix.signal.SingleCaseAlert
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification

@Transactional
class AlertTagService {
    def sessionFactory
    def signalDataSourceService
    def dataSource_pva

    List<Map> getAlertTagNameList(List<Long> alertIdList,def domain){
        List<Map> results = domain.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                'alertTags' {
                    property("name", "name")
                }

            }
            or {
                alertIdList.collate(1000).each {
                    'in'('id', it)
                }
            }
        } as List<Map>
        results
    }

    List getMartTagsName() {
        List martTagsNameList = []
            MartTags.createCriteria().list {
                projections {
                    property("name")
                }
                eq("isCaseSeriesTag", true)
            }.each { tagName ->
                martTagsNameList.add(tagName)
            }
        return martTagsNameList
    }

    Long fetchGlobalId(String domain , String globalId) {
        Long globalAlertId
        switch(domain) {
            case Constants.AlertConfigType.SINGLE_CASE_ALERT :
                Long id = GlobalCase.createCriteria().get {
                    projections {
                        property('id')
                    }
                    eq('caseId' , globalId?.toLong())
                }
                if(!id) {
                    GlobalCase globalCase = new GlobalCase()
                    globalCase.caseId = globalId?.toLong()
                    globalCase.save()
                    globalAlertId = globalCase?.id
                }
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                Long producutId = globalId?.split('-')[0]
                Long eventId = globalId?.split('-')[1]
                Long smqCode = globalId?.split('-').size() > 2 ? globalId?.split('-')[1] : null
                Long id = GlobalProductEvent.createCriteria().get {
                    projections {
                        property('id')
                    }
                    eq('productId' , producutId?.toLong())
                    eq('eventId' , eventId?.toLong())
                    eq('smqCode' , smqCode)
                }
                if(!id) {
                   GlobalProductEvent globalProductEvent = new GlobalProductEvent()
                   globalProductEvent?.productId = producutId.toLong()
                   globalProductEvent?.eventId = eventId?.toLong()
                   globalProductEvent.save()
                    globalAlertId = globalProductEvent?.id

                }
                break
            case Constants.AlertType.LITERATURE_ALERT :
                Long id = GlobalArticle.createCriteria().get {
                    projections {
                        property('id')
                    }
                    eq('articleId' , globalId?.toLong())
                }
                if(!id) {
                    GlobalArticle globalArticle = new GlobalArticle()
                    globalArticle.articleId = globalId?.toLong()
                    globalArticle.save()
                    globalAlertId = globalArticle?.id
                }
                break

        }
        return globalAlertId
    }

    void batchPersistGlobalCase(List newCaseIds) {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        GlobalCase.withTransaction {
            List batch = []
            newCaseIds.eachWithIndex { def caseId, Integer index ->
                GlobalCase globalCase = new GlobalCase()
                globalCase.caseId = caseId
                batch += globalCase
                globalCase.save(validate: false)
                if (index.mod(batchSize) == 0) {
                    Session session = sessionFactory.currentSession
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }
        log.info("GlobalCases are saved")
    }

    List<Map> fetchTagsfromMart(List<Map> codeValues) {
        List <Map> tags = codeValues.findAll{
            it.parentId == '0' || it.parentId == null
        }
        return tags

    }

    List<Map> fetchSubTagsFromMart(List<Map> codeValues) {
        List <Map> subTags = codeValues.findAll{
            it.parentId != '0' && it.parentId != null
        }
        return subTags
    }

    List<Map> fetchTagsAndSubtags() {
        Sql sql = null
        List <Map> tags = []
        try {
            sql = new Sql(signalDataSourceService.getDataSource('pva'))
            sql.eachRow("select * from code_value" , []) { row ->
                Map rowData = ['id' : row.id , 'text' : row.value   , 'parentId' : row.parent_id]
                tags << rowData
            }
            sql.close()
        } catch (Throwable t) {
            log.error("Error on fetching Tags ")
        } finally {
            try {
                sql?.close()
            } catch (Throwable notableToHandle) {
                log.error("Failed to close the Sql", notableToHandle)
            }
        }
        return tags
    }

    List<Map> getAllSingleCaseAlertTags(List<SingleCaseAlert> list) {
        List<Map> tags = []
        list.each {sca->
            List<Map> caseSeriesTags = sca.pvsAlertTag.collect {
                ['tagText': it.tagText, 'subTagText': it.subTagText, 'privateUser': it.privateUser ? '(P)' : '', 'priority': it.priority, 'tagType': '(A)' , 'alertId' : it.alertId]
            }
            tags << caseSeriesTags
        }
        return tags
    }

    List<Map> getAllSingleCaseGlobalTags(List<SingleCaseAlert> list) {
        List<Map> tags = []
        list.each {sca->
            List<Map> globalTags = sca?.globalCase?.pvsGlobalTag.collect {
                ['tagText' : it.tagText , 'subTagText' : it.subTagText ,'privateUser' : it.privateUser ? '(P)' : ''  , 'priority' : it.priority , 'tagType' : '(G)' , 'globalId' : it.globalId]
            }
            tags << globalTags
        }
        return tags
    }
}

