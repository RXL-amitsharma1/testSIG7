package com.rxlogix

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.*
import spock.lang.Unroll
import com.rxlogix.signal.SubstanceFrequency

class SubstanceFrequencyControllerSpec extends Specification implements ControllerUnitTest<SubstanceFrequencyController> {

    def setup() {
        Date dateSart = new Date() - 1
        Date endDate =new Date()
        SubstanceFrequency substanceFrequency = new SubstanceFrequency(name: 'test', startDate: dateSart, endDate: endDate, uploadFrequency: '1',
                miningFrequency: '2', alertType: 'testAlertType', frequencyName: 'testFrequency').save(failOnError: true)
        SubstanceFrequency substanceFrequency2 = new SubstanceFrequency(name: 'test2', startDate: dateSart, endDate: endDate, uploadFrequency: '11',
                miningFrequency: '21', alertType: 'testAlertType2', frequencyName: 'testFrequency2').save(failOnError: true)
    }

    def cleanup() {
    }
    void "test index action"(){
        when:
            def model=controller.index()
        then:
            response.status==200
            model.substanceFrequencyList[0].name=='test2'
            model.substanceFrequencyList[0].alertType=='testAlertType2'
            model.substanceFrequencyList[0].frequencyName=='testFrequency2'
    }

    void "test create action"(){
        when:
            controller.create()
        then:
            response.status==200
            view=='/substanceFrequency/create'
            model.subfeq!=null
            model.frequencyList[0]=='15 Days'
            model.alertTypeList[0]==Constants.AlertConfigType.EVDAS_ALERT

    }
    void "test edit action"(){
        when:
            params.id='1'
            controller.edit()
        then:
            response.status==200
            view=='/substanceFrequency/edit'
            model.instance.name=='test'
            model.frequencyList==['15 Days', '1 Month', '3 Months', '6 Months', '12 Months']
            model.alertTypeList==[Constants.AlertConfigType.EVDAS_ALERT, Constants.AlertType.AGGREGATE_ALERT, Constants.AlertType.AGGREGATE_ALERT_FAERS]
    }
    @Unroll
    void "test save -- Failed "(){
        when:
            params.name=nameValue
            params.startDate="02/02/2020"
            params.endDate="05/02/2020"
            params.uploadFrequency= '1'
            params.miningFrequency='2'
            params.alertType=alertTypeValue
            params.frequencyName=frequencyNameValue
            controller.save()
        then:
            response.status==200
            view=='/substanceFrequency/create'
            model.subfeq.name==nameValue
            model.frequencyList==['15 Days', '1 Month', '3 Months', '6 Months', '12 Months']
            model.alertTypeList==[Constants.AlertConfigType.EVDAS_ALERT, Constants.AlertType.AGGREGATE_ALERT, Constants.AlertType.AGGREGATE_ALERT_FAERS]

        where:
        nameValue | alertTypeValue   | frequencyNameValue
        'test'    | 'testAlertType'  | 'testFrequency1'
        'test2'   | 'testAlertType'  | 'testFrequency2'
        'test3'   | 'testAlertType2' | 'testFrequency'

    }

    void "test save -- Success "(){
        when:
            params.name='newName'
            params.startDate="02/02/2020"
            params.endDate="05/03/2020"
            params.uploadFrequency= '1'
            params.miningFrequency='2'
            params.alertType='newAlertType'
            params.frequencyName='newFrequencyName'
            controller.save()
        then:
            response.status==302
            response.redirectedUrl=='/substanceFrequency/index'

    }
    void "test update -- Success "(){
        when:
            params.id=2
            params.name='testNew'
            params.startDate="02/02/2020"
            params.endDate="05/02/2020"
            params.uploadFrequency= '1'
            params.miningFrequency='2'
            params.alertType='testAlertType'
            params.frequencyName='testFrequency4'
            controller.update()
        then:
            response.status==302
            response.redirectedUrl=='/substanceFrequency/index'


    }
    void "test update -- Failed "(){
        when:
            params.id=null
            params.name='testNew'
            params.startDate="02/02/2020"
            params.endDate="05/02/2020"
            params.uploadFrequency= '1'
            params.miningFrequency='2'
            params.alertType='testAlertType'
            params.frequencyName='testFrequencyName'
            controller.update()
        then:
            response.status==200
            view=='/substanceFrequency/edit'
            model.instance==null
            model.frequencyList==['15 Days', '1 Month', '3 Months', '6 Months', '12 Months']
            model.alertTypeList==[Constants.AlertConfigType.EVDAS_ALERT, Constants.AlertType.AGGREGATE_ALERT, Constants.AlertType.AGGREGATE_ALERT_FAERS]

    }

        void "test delete action" (){
        when:
            params.id='2'
            controller.delete()
        then:
            response.status==302
            response.redirectedUrl=='/substanceFrequency/index'
    }


}