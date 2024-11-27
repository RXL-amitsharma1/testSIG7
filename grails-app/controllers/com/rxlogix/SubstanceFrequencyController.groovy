package com.rxlogix

import com.rxlogix.config.DateRangeInformation
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.util.DateUtil
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders

@Secured(["isAuthenticated()"])
class SubstanceFrequencyController {

    DateRangeInformation dateRangeInformation  = new DateRangeInformation()
    List<String> frequencyList = ['15 Days', '1 Month', '3 Months', '6 Months', '12 Months']
    List<String> alertTypeList = [Constants.AlertConfigType.EVDAS_ALERT, Constants.AlertType.AGGREGATE_ALERT, Constants.AlertType.AGGREGATE_ALERT_FAERS]
    @Secured(['ROLE_ADMIN','ROLE_CONFIGURATION_VIEW','ROLE_CONFIGURATION_CRUD'])
    def index() {
        def substanceFrequencyList = SubstanceFrequency.list(sort: "id", order: "desc")
        def resultList = substanceFrequencyList?.collect { it.toDto() }
        [substanceFrequencyList : resultList]
    }

    def create(){
        SubstanceFrequency subfeq = new SubstanceFrequency()
        render(view: "create", model: [subfeq: subfeq, frequencyList: frequencyList, alertTypeList: alertTypeList])
    }


    def save() {

        SubstanceFrequency subfeq = new SubstanceFrequency(params)
        try {
            if (SubstanceFrequency.findByAlertTypeAndNameIlike(params.alertType,params.name)) {
                flash.error = message(code: 'app.warning.unique.frequency.product', args: [params.name])
                render(view:"create", model: [subfeq: subfeq, frequencyList: frequencyList, alertTypeList: alertTypeList])
                return
            } else {
                bindData(subfeq, params, [exclude: ['startDate', 'endDate']])
                def startDateAbsolute = params.startDate
                def endDateAbsolute = params.endDate
                def timezone = Holders.config.server.timezone
                subfeq.startDate = DateUtil.stringToDate(startDateAbsolute, 'MM/dd/yyyy', timezone)
                subfeq.endDate = DateUtil.stringToDate(endDateAbsolute, 'MM/dd/yyyy', timezone)
                SubstanceFrequency existingFreq = SubstanceFrequency.findByFrequencyName(params.frequencyName)
                if (existingFreq) {
                    if (subfeq.startDate != existingFreq.startDate || subfeq.endDate != existingFreq.endDate || subfeq.miningFrequency != existingFreq.miningFrequency) {
                        flash.error = message(code: "app.warning.invalid.frequency.name")
                        throw new Exception()
                    }
                }
            }
            subfeq.save(flush: true)
            flash.message = message(code: "app.substance.frequency.created")
            redirect(view: 'index')
        }
        catch(Exception e) {
            flash.error = message(code: "app.warning.all.frequency.fields.required")
            log.error(e.getMessage())
            e.printStackTrace()
            render(view:"create", model: [subfeq: subfeq, frequencyList: frequencyList, alertTypeList: alertTypeList])
        }


    }

    def edit() {
       SubstanceFrequency substanceInstance = SubstanceFrequency.get(params.id)
       render (view: 'edit', model: [instance: substanceInstance, frequencyList: frequencyList, alertTypeList: alertTypeList])
    }

    def update(){
        SubstanceFrequency substanceInstance = SubstanceFrequency.get(params.id)
        try{
            substanceInstance.properties = params;
            substanceInstance.save(flush : true, failOnError: true)
            redirect view: 'index'
        }
        catch(Exception e){
            flash.error = message(code: "app.warning.all.frequency.fields.required")
            log.error("Updation of Substance Frequency Failed")
            render view: 'edit', model: [instance: substanceInstance, frequencyList: frequencyList, alertTypeList: alertTypeList]
        }

    }

    def delete(){
        SubstanceFrequency substanceInstance = SubstanceFrequency.get(params.id)
        substanceInstance.delete(flush: true)
        flash.message = message(code: "app.substance.frequency.deleted")
        redirect view : 'index'

    }


}
