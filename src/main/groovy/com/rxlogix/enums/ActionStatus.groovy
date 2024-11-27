package com.rxlogix.enums
import grails.util.Holders
enum ActionStatus {
    InProgress("In Progress"),
    Deleted("Deleted"),
    New("New"),
    Closed('Closed'),
    Completed('Completed'),
    ReOpened('Re-opened')

    String id

    ActionStatus(id) { this.id = id }

    static List allValues() {
        List<ActionStatus> allStatus = []

        Holders.config.actionStatus.labels.each {
            switch (it) {
                case "In Progress":
                    allStatus.add(InProgress)
                    break
                case "Deleted":
                    allStatus.add(Deleted)
                    break
                case "New":
                    allStatus.add(New)
                    break
                case "Closed":
                    allStatus.add(Closed)
                    break
                case "Re-opened":
                    allStatus.add(ReOpened)
                    break
                case "Completed":
                    allStatus.add(Completed)
                    break
            }
        }
        allStatus
    }

    static ActionStatus findByValue(String value) {
        values().find { it.id == value }
    }

    String getKey() { name() }
}