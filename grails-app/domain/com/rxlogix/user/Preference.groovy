package com.rxlogix.user

import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.ChildModuleAudit
import grails.plugins.orm.auditable.JSONAudit
import org.joda.time.DateTimeZone
class Preference implements Serializable {

    static auditable = [ignore:['isCumulativeAlertEnabled','apiToken','tokenUpdateDate','lastUpdated','modifiedBy','dateCreated','user','createdBy','isEmailEnabled']]

    Locale locale
    String timeZone
    Boolean isEmailEnabled = true
    Boolean isCumulativeAlertEnabled = false
    String dashboardConfig
    String apiToken

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    Date tokenUpdateDate
    boolean isLiteratureAbstractView = false

    static belongsTo = [user: User]

    static mapping = {
        table name: "PREFERENCE"
        dashboardConfig column: "DASHBOARD_CONFIG_JSON", type: "text", sqlType: "clob"
        locale column: "LOCALE"
        timeZone column: "TIME_ZONE"
        apiToken sqlType: "varchar2(4000)"
    }

    static constraints = {
        locale(nullable: false)
        timeZone(nullable: true)
        dashboardConfig(nullable: true)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        apiToken nullable: true, blank: true, validator: { val, obj ->
            if (!val || (obj.getId() && !obj.isDirty('apiToken'))) {
                return true
            }
            return !Preference.countByIdNotEqualAndApiToken(obj.id, val)
        }
        tokenUpdateDate(nullable: true)
        isLiteratureAbstractView(nullable: true)
    }

    String getTimeZone() {
        if (timeZone) {
            return timeZone
        } else {
            return DateTimeZone.UTC.ID
        }
    }

    @Override
    public String toString() {
        return this.user.fullName
    }

    def getInstanceIdentifierForAuditLog() {
        return this.user?.fullName
    }


    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        Boolean isHealthChecker = this.user.isHealthStatusReviewer()
        if (newValues && (oldValues == null)){
            //case of meeting create done to put meeting time manually as it is derived from meeting date
            if (newValues.containsKey("dashboardConfig")) {
                newValues.put('dashboardConfig', MiscUtil.getDashboardVisibleWidgets(newValues.get('dashboardConfig'), isHealthChecker))
            }
        }
        if (newValues && oldValues && this.dirtyPropertyNames?.contains("dashboardConfig")) {
            newValues.put('dashboardConfig', MiscUtil.getDashboardVisibleWidgets(newValues.get('dashboardConfig'), isHealthChecker))
            oldValues.put("dashboardConfig", MiscUtil.getDashboardVisibleWidgets(this.getPersistentValue("dashboardConfig"), isHealthChecker))
        }

        return [newValues: newValues, oldValues: oldValues]
    }


}