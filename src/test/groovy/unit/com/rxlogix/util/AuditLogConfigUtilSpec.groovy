package unit.com.rxlogix.util

import com.rxlogix.signal.SystemConfig
import com.rxlogix.util.AuditLogConfigUtil
import grails.util.Holders
import spock.lang.Specification

class AuditLogConfigUtilSpec extends Specification {

    def setup() {
        // Mocking Holders and SystemConfig
        Holders.config = [:]

        SystemConfig systemConfig = new SystemConfig()
        SystemConfig.metaClass.'static'.first = { ->
            systemConfig
        }    }

    def "should populate label config for aggregate audit labels when changedProperty is null"() {
        given:
        // Mocking populateAggregateAuditLabelMap method to return dummy data
        AuditLogConfigUtil.metaClass.static.populateAggregateAuditLabelMap = { ->
            return [field1: "Label 1", field2: "Label 2"]
        }

        when:
        AuditLogConfigUtil.populateLabelConfigForAudit(null)

        then:
        Holders.config.aggregateAuditLabels == [field1: "Label 1", field2: "Label 2"]
    }

    def "should populate signalUDLabels for LIST_OF_MAP type"() {
        given:
        Holders.config.signal.summary.dynamic.fields = [
                [fieldName: "field1", label: "Label 1"],
                [fieldName: "field2", label: "Label 2"]
        ]

        when:
        AuditLogConfigUtil.populateLabelConfigForAudit("signalUDLabels")

        then:
        Holders.config.signalUDLabels == [field1: "Label 1", field2: "Label 2"]
    }

    def "should handle BOOLEAN type and set statusHistoryStatusLabel correctly"() {
        given:
        SystemConfig.metaClass.static.first = { ->
            return [enableSignalWorkflow: true] // Mocking SystemConfig.first() method
        }

        when:
        AuditLogConfigUtil.populateLabelConfigForAudit("statusHistoryStatusLabel")

        then:
        Holders.config.statusHistoryStatusLabel == "To State"
    }

    def "should log warning and return when config is invalid"() {
        given:
        def invalidConfigData = [["type": "UNKNOWN", "propertyName": "unknownProperty"]]

        when:
        AuditLogConfigUtil.populateLabelConfigForAudit("unknownProperty")

        then:
        notThrown(Exception) // Ensuring no exceptions are thrown
    }

    def "should not fail if no changedProperty is provided"() {
        when:
        AuditLogConfigUtil.populateLabelConfigForAudit()

        then:
        noExceptionThrown()
    }

    def "should handle null values gracefully"() {
        given:
        Holders.config = null

        when:
        AuditLogConfigUtil.populateLabelConfigForAudit()

        then:
        noExceptionThrown()
    }
}
