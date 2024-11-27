package com.rxlogix.config

import grails.gorm.transactions.Transactional

@Transactional
class ActionTypeService {

    def saveActionType(ActionType at) {
        at.validate()
         at.save()
    }

    def deleteActionType(ActionType at) {
        at.delete()
    }
}
