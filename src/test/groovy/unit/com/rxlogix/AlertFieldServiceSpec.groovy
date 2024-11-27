package com.rxlogix
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.dto.CategoryDTO
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.hibernate.Session
import org.slf4j.Logger
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@Mock([AggregateCaseAlert, AlertService, CacheService])
@TestFor(AlertFieldService)
@TestMixin(GrailsUnitTestMixin)
@ConfineMetaClassChanges([ExecutedConfiguration])
class AlertFieldServiceSpec extends Specification {
    def setup() {
    }

    def cleanup() {
    }

    void "test alertFields"() {
        given:
        def mockCacheService = Mock(CacheService)

        String dbType = "SAFETY"
        String alertType = "AGGREGATE_CASE_ALERT"
        def alertFieldsData = [
                [
                        enabled              : true,
                        alertType            : "AGGREGATE_CASE_ALERT",
                        isAdvancedFilterField: true,
                        oldDisplay           : "EB95(Asia)",
                        secondaryName        : null,
                        isAutocomplete       : false,
                        dbKey                : null,
                        isSmqVisible         : true,
                        keyId                : "EB95_ASIA",
                        isBusinessRuleField  : true,
                        name                 : "eb95Asia",
                        seq                  : 0,
                        isHyperLink          : false,
                        isVisible            : true,
                        flagEnabled          : true,
                        cssClass             : null,
                        type                 : "subGroup",
                        dbType               : "SAFETY",
                        isAdhocVisible       : false,
                        listOrder            : 0,
                        isFilter             : true,
                        containerView        : 2,
                        dataType             : "java.lang.Number",
                        isNewColumn          : false,
                        display              : "EB95(Asia)",
                        optional             : false
                ],
                [
                        enabled              : false,
                        alertType            : "AGGREGATE_CASE_ALERT",
                        isAdvancedFilterField: true,
                        secondaryName        : null,
                        isAutocomplete       : false,
                        dbKey                : null,
                        isSmqVisible         : true,
                        oldDisplay           : "EB95(Rest of the World)",
                        isBusinessRuleField  : true,
                        seq                  : 0,
                        isHyperLink          : false,
                        name                 : "eb95Rest of the World",
                        isVisible            : true,
                        flagEnabled          : true,
                        cssClass             : null,
                        type                 : "subGroup",
                        dbType               : "SAFETY",
                        display              : "EB95(Rest of the World)",
                        keyId                : "EB95_REST OF THE WORLD",
                        isAdhocVisible       : false,
                        listOrder            : 0,
                        isFilter             : true,
                        containerView        : 2,
                        dataType             : "java.lang.Number",
                        isNewColumn          : false,
                        optional             : false
                ]
        ]

        // Mocking the cacheService behavior
        mockCacheService.getAlertFields(alertType) >> alertFieldsData
        service.cacheService = mockCacheService
        when:
        service.alertFields(dbType, alertType, true, true,true, true)

        then:
        noExceptionThrown()
    }
}