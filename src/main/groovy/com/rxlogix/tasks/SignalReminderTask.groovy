package com.rxlogix.tasks

import com.rxlogix.signal.AdHocAlert

class SignalReminderTask implements Task {

    void executeTask(){
        def alerts = AdHocAlert.findAll();
        System.out.println("Hello")

    }
}
