package com.rxlogix.jobs

class MoveFaersConfigDataToMartJob {

    def moveFaersConfigDataToMartService


    static triggers = {
        simple startDelay: 480000l, repeatInterval: 120000l // execute job after each 2 minutes with delay of 8 min
    }

    def execute() {
        moveFaersConfigDataToMartService.migrateDataToMart()
    }
}
