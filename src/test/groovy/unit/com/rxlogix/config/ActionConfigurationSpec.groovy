package com.rxlogix.config

import grails.testing.gorm.DomainUnitTest
import unit.utils.ConstraintUnitSpec

class ActionConfigurationSpec extends ConstraintUnitSpec implements DomainUnitTest<ActionConfiguration> {

    def setup() {
        mockForConstraintsTests(ActionConfiguration, [ActionConfiguration.build()])
    }
    /*
    @Unroll("test ActionConfiguration all constraints #field is #error")
    void "test ActionConfiguration all constraints"() {
        when:
        def actionConfiguration = new ActionConfiguration("$field": val)

        then:
        validateConstraints(actionConfiguration, field, error)

        where:
        error      | field               | val
        'nullable' | 'value'             | null
        'nullable' | 'displayName'       | null
        'valid'    | 'displayName_local' | null
        'valid'    | 'description_local' | null
    }
    */
}
