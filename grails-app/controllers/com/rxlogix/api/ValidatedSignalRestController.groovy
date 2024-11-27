package com.rxlogix.api

import com.rxlogix.controllers.SignalController
import com.rxlogix.security.Authorize
import com.rxlogix.signal.SignalOutcome
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON

@Authorize
class ValidatedSignalRestController implements SignalController {

    def messageSource

    def signalOutcomes() {
        def resp = [:]
        def signalId = params.id
        def validatedSignal = ValidatedSignal.get(signalId)

        if (!validatedSignal) {
            RestApiResponse.invalidParametersResponse(resp, "Validated signal with id: ${signalId} not found")
            render  resp as JSON
            return
        }
        def data = [:]
        data << [allSignalOutcomes: SignalOutcome.list().findAll { !it.isDeleted }.collect { it.name },
                 existingSignals: validatedSignal?.signalOutcomes?.findAll { !it.isDeleted }.collect { it.name },
                 signalOutcomes   : SignalOutcome.findAllByDispositionId(validatedSignal?.dispositionId).findAll {!it.isDeleted}.collect { it.name }]

        RestApiResponse.successResponseWithData(resp, null, data)
        render resp as JSON
    }

    def verifySignalOutcomeMapping() {
        def resp = [:]
        def status = true
        def msg = null

        def targetDispositionId = params.targetDispositionId?.toLong()
        SignalOutcome mappedSignal = targetDispositionId ? SignalOutcome.findByDispositionId(targetDispositionId) : null

        if (!mappedSignal) {
            status = false
            msg = "Signal Outcome not found"
        } else {
            def outcomes = params.list('signalOutComes')
            outcomes.each { outcomeName ->
                SignalOutcome signalOutcome = SignalOutcome.findByName(outcomeName)
                if (signalOutcome?.dispositionId != targetDispositionId) {
                    status = false
                    msg = "Single Outcome Name not correspond to checked targetDispositionId"
                    return
                }
            }
        }

        if (status) {
            def data = [messageSource.getMessage("app.label.signal.information.outcome.mapping.error", null, Locale.default)]
            RestApiResponse.successResponseWithData(resp, null, data)
        } else {
            RestApiResponse.failureResponse(resp, msg)
        }

        render resp as JSON
    }
}
