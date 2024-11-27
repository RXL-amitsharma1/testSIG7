package com.rxlogix.signal

import com.rxlogix.config.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.JSONAudit
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.Duration

@DirtyCheck
class Alert implements GroovyInterceptable, Serializable {
    static auditable = true

    def grailsApplication

    def static dataSource

    static attachmentable = true

    def static TABLE_NAME = "ALERTS"

    Long id
    Long alertId
    String caseNumber
    Long alertVersion
    Disposition disposition
    Date reviewDate
    User assignedTo
    Group assignedToGroup
    Date detectedDate
    Date dueDate
    String notes
    Priority priority
    @JSONAudit
    String attributes
    List activities
    Map<String, Object> attributesMap

    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    String eventSelection

    boolean flagged = false
    Boolean isMultiIngredient = false

    ExecutedConfiguration executedAlertConfiguration

    static String overdue_dates_sql =
            """
                select case when DATEDIFF(due_date, CURDATE())<=0 Then 'Overdue'
                            when DATEDIFF(due_date, CURDATE())<=1 Then '1 day'
                            when DATEDIFF(due_date, CURDATE())>=2  AND DATEDIFF(due_date, CURDATE())<=7 Then '2 to 7 days'
                            when DATEDIFF(due_date, CURDATE())>7 Then '> 7 days'
                            else NULL End AS Review_Timeframe, p.value as priority,
                count(alerts.id) as alert_count from $TABLE_NAME alerts, PVS_STATE ps, PRIORITY p where alerts.alert_type=:type AND alerts.workflow_state_id=ps.id
                and ps.final_state=0 and alerts.priority_id = p.id group by case when DATEDIFF(due_date, CURDATE())<=0 Then 'Overdue'
                            when DATEDIFF(due_date, CURDATE())<=1 Then '1 day'
                            when DATEDIFF(due_date, CURDATE())>=2  AND DATEDIFF(due_date, CURDATE())<=7 Then '2 to 7 days'
                            when DATEDIFF(due_date, CURDATE())>7 Then '> 7 days'
                            else NULL End, p.value;
            """

    static String alert_ids_by_type = "SELECT id FROM $TABLE_NAME WHERE alert_type=:alert_type"

    static mapping = {
        table("ALERTS")
        discriminator column: [name: "alert_type", length: 20]
        attributes type: 'text', sqlType: 'clob'
        executedAlertConfiguration(column: 'exec_config_id')
        actions joinTable: [name: "ADHOC_ALERT_ACTIONS",column: "ACTION_ID",key: "ALERT_ID"]
        notes nullable: true, type: "text", sqlType: "clob"
    }

    static constraints = {
        alertId nullable: true
        caseNumber nullable: true
        alertVersion nullable: true
        reviewDate nullable: true
        assignedTo nullable: true
        assignedToGroup nullable: true
        detectedDate(validator: { val, obj ->
            if (obj.getClass().getSimpleName() == 'AdHocAlert' && !val)
                return "com.rxlogix.signal.AdHocAlert.detectedDate.nullable"
        }, nullable: true)
        dueDate nullable: true
        notes nullable: true
        alertConfiguration nullable: true
        disposition nullable: true
        attributes nullable: true
        createdBy nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        modifiedBy nullable: true
        executedAlertConfiguration nullable: true
        eventSelection nullable: true, maxSize: 8000
        isMultiIngredient nullable: true
    }

    static belongsTo = [alertConfiguration        : Configuration,
                        assignedTo                : User,
                        disposition               : Disposition,
                        executedAlertConfiguration: ExecutedConfiguration]

    static hasMany = [activities: Activity, actions: Action]

    static transients = [
            'attributesMap'
    ]

    static overdueGroups(type) {
        def data = []
        Sql sql = new Sql(getDataSource())
        try {
            sql.eachRow(overdue_dates_sql, [type: type]) { row ->
                data << [row.Review_Timeframe, row.priority, row.alert_count]
            }
        }catch (Exception e){
            e.printStackTrace()
        } finally {
            sql?.close()
        }
        data
    }

    def getAttr(attrName) {
        if (!attributesMap) {
            attributesMap = populateAttributesMap()
        }

        if (attributesMap)
            attributesMap[attrName]
        else
            null
    }

    def populateAttributesMap() {
        def jsonSlurper = new JsonSlurper()
        attributes ? jsonSlurper.parseText(attributes) ?: [:] : [:]
    }

    def dueIn() {
        def theDueDate = new DateTime(dueDate).withTimeAtStartOfDay()
        def now = DateTime.now().withTimeAtStartOfDay()
        def dur = new Duration(now, theDueDate)
        dur.getStandardDays()
    }

    def static idByAlertType(String type) {
        Sql sql = new Sql(getDataSource())
        def data = []
        try {
            sql.eachRow(alert_ids_by_type, [alert_type: type]) { row ->
                data << (row.id as Long)
            }
        }catch (Exception e){
            e.printStackTrace()
        }finally {
            sql?.close()
        }
        data
    }

    def propertyMissing(name) {
        if (!attributesMap)
            attributesMap = populateAttributesMap()

        if (name && attributesMap.containsKey(name))
            return attributesMap[name]
        else
            null
    }

    def propertyMissing(String name, value) {
        if (!attributesMap) {
            attributesMap = populateAttributesMap()
        }

        attributesMap[name] = value
    }

    def beforeValidate() {
        if (attributesMap) {
            attributes = JsonOutput.toJson(attributesMap)
        }
    }

    def afterLoad() {
        attributesMap = populateAttributesMap()
    }

    def getSharedUsers() {
        if (sharedWithGroups) {
            sharedWithGroups.collect { g -> g.members }.flatten()
        }
    }

    static def getDataSource() {
        if (!dataSource) {
            def app = Holders.getGrailsApplication()
            dataSource = app.getMainContext().getBean('dataSource')
        }

        dataSource
    }
}
