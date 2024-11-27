package com.rxlogix

import grails.testing.services.ServiceUnitTest
import org.grails.datastore.gorm.bootstrap.support.InstanceFactoryBean
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

import javax.sql.DataSource

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Ignore
class ReportFieldServiceSpec extends Specification implements ServiceUnitTest<ReportFieldService> {
    @Shared def orgTmp

    static doWithSpring = {
        dataSource_pva(InstanceFactoryBean, [:] as DataSource, DataSource)
    }

    def setupSpec() {
        orgTmp = System.getProperty("java.io.tmpdir")
        System.setProperty("java.io.tmpdir", "./")
    }

    def cleanupSpec() {
        File f = new File(System.getProperty("java.io.tmpdir"), "selectable_list_file.dat")
        f.delete()
        System.setProperty("java.io.tmpdir", orgTmp)
    }

    def "serializeValues test "() {
        setup:
            def values = [key_a: "a", key_b: "b"]

        when:
            service.serializeValues(values)
        then:
            new File(System.getProperty("java.io.tmpdir"), "selectable_list_file.dat").exists() == true
    }

    def "deserialize Value test"() {
        setup:
            def values = [key_a: "a", key_b: "b"]
            service.serializeValues(values)
        when:
            def readValues= service.readValues()
        then:
            readValues.size() == 2
            readValues["key_a"] == "a"
    }
}
