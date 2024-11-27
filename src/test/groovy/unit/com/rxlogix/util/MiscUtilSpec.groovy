package com.rxlogix.util

import spock.lang.Ignore
import spock.lang.Specification

class MiscUtilSpec extends Specification {
    @Ignore
    void "md5ChecksumForFile should return the checksum of a file as a String" () {
        // This needs to be operating system independent. This test can fail on a Windows environment if it has line breaks:
        // http://stackoverflow.com/questions/5940514/is-a-md5-hash-of-a-file-unique-on-every-system
        expect:
            "46190f059ea1c0af37a772c920c1eb53" == MiscUtil.md5ChecksumForFile("test/unit/data/md5checksum_test.txt")
    }

    def "test getProductDrugValues with isPva true"() {
        given:
        def config = [productSelection: '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[{"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"}]}' ]

        when:
        def result = MiscUtil.getProductDrugValues(config.productSelection, true)

        then:
        result.size() == 1
        result == [['id':'000200', 'name':'PARACETAMOL']]
    }

    def "test getProductDrugValues with isPva false"() {
        given:
        def config = [productSelection: '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[]}' ]

        when:
        def result = MiscUtil.getProductDrugValues(config.productSelection, false)

        then:
        result.size() == 0
    }

    def "test parseDictionaryDrug with empty dictionarySelection"() {
        given:
        def config = [productSelection: ""]

        when:
        def result = MiscUtil.parseDictionaryDrug(config.productSelection)

        then:
        result.size() == 0
    }

    def "test parseDictionaryDrug with valid dictionarySelection"() {
        given:
        def config = [productSelection: '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[{"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"}]}' ]

        when:
        def result = MiscUtil.parseDictionaryDrug(config.productSelection)

        then:
        result == [['id':'000200', 'name':'PARACETAMOL']]
    }

    void "test generateSpotFireFileName method"() {
        given:
        String signalName = "Signal-UNICODE-1-022OQ-UNICODE signal-unicode-¡ÐÊßãÆæØøÅåß\$^(){}1234567890-Formal Run Purpose ¡ÐÊßãÆæØøÅåß\$^(){}1234567890-ÐÊßãÆæØøÅåß\$^(){}1234567890-Formal Run Purpose¡ÐÊßãÆæØøÅåß\$^-Formal Run Purpose-Formal Run Purpose(){}12377777777-Formal Run12345"
        int spotFireFileNameLength = 234
        String expectedFileName = "Signal-UNICODE-1-022OQ-UNICODE_signal-unicode-__________________1234567890-Formal_Run_Purpose___________________1234567890-_________________1234567890-Formal_Run_Purpose______________-Formal_Run_Purpose-Formal_Run_Purpose____123777777"  // Replace with your expected file name

        when:
        String fileName = MiscUtil.generateSpotFireFileName(signalName, spotFireFileNameLength)

        then:
        fileName == expectedFileName
    }

    def "test sanitizeString with null input"() {
        expect:
        MiscUtil.sanitizeString(null) == ""
    }

    def "test sanitizeString with empty string"() {
        expect:
        MiscUtil.sanitizeString("") == ""
    }

    def "test sanitizeString with string containing special characters"() {
        given:
        String input = "Hello <World>! This is a test | string / with special # characters \\"

        when:
        String result = MiscUtil.sanitizeString(input)

        then:
        result == "Hello World This is a test  string  with special  characters "
    }

    def "test sanitizeString with input length exceeding 100 characters"() {
        given:
        String input = "This string is way too long and contains more than one hundred characters. " +
                "Let's see if it gets trimmed down correctly! Extra characters to exceed limit."

        when:
        String result = MiscUtil.sanitizeString(input)

        then:
        result.length() <= 100
    }
}
