package com.rxlogix.util

import spock.lang.Specification
import com.rxlogix.enums.DictionaryTypeEnum

class ViewHelperSpec extends Specification {

    def "test getProductDictionaryValues with null obj"() {
        given:
        def obj = null
        def dictionaryType = DictionaryTypeEnum.PRODUCT

        when:
        def result = ViewHelper.getProductDictionaryValues(obj, dictionaryType)

        then:
        result == ""
    }

    def "test getProductDictionaryValues with productSelection without drug data"() {
        given:
        def obj = [productSelection: '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[]}' ]
        def dictionaryType = DictionaryTypeEnum.PRODUCT

        ViewHelper.metaClass.static.getHiearchyValues = { ->
            return ['Substance', 'Product Name', 'Product - Dosage Forms', 'Trade Name']
        }

        when:
        def result = ViewHelper.getProductDictionaryValues(obj, dictionaryType)

        then:
        result == "PARACETAMOL (Substance)"
    }

    def "test getProductDictionaryValues with productSelection with drug data"() {
        given:
        def obj = [productSelection: '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[{"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"}]}' ]
        def dictionaryType = DictionaryTypeEnum.PRODUCT

        ViewHelper.metaClass.static.getHiearchyValues = { ->
            return ['Substance', 'Product Name', 'Product - Dosage Forms', 'Trade Name']
        }

        when:
        def result = ViewHelper.getProductDictionaryValues(obj, dictionaryType)

        then:
        result == "PARACETAMOL (Substance), 000200 (PARACETAMOL) (Drug Record Number)"
    }

    def "test getProductDictionaryValues with productGroupSelection data"() {
        given:
        def obj = [productGroupSelection: '[{"name":"test WHO PG 1145 (380630)","id":380630,"isMultiIngredient":true,"includeWHODrugs":false}]' ]
        def dictionaryType = DictionaryTypeEnum.PRODUCT_GROUP

        ViewHelper.metaClass.static.getHiearchyValues = { ->
            return ['Substance', 'Product Name', 'Product - Dosage Forms', 'Trade Name']
        }

        when:
        def result = ViewHelper.getProductDictionaryValues(obj, dictionaryType)

        then:
        result == ""
    }

}
