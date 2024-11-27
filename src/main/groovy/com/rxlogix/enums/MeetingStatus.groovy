package com.rxlogix.enums

enum MeetingStatus {

    SCHEDULED("Scheduled"),
    CANCELLED("Cancelled"),
    CLOSED("Closed")

    def id

    MeetingStatus(id) {this.id = id}
}