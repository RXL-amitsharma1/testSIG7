package com.rxlogix.config

class GuestAttendee {
    static auditable = false

    String guestAttendeeEmail

    static mapping = {
        table name: "GUEST_ATTENDEE"
        guestAttendeeEmail column: "EMAIL"
    }

    static constraints = {
        guestAttendeeEmail email: true
    }

    @Override
    String toString(){
        return this.guestAttendeeEmail
    }

}
