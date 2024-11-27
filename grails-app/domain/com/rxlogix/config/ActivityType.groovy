package com.rxlogix.config

class ActivityType implements Serializable {

    static auditable = false
    ActivityTypeValue value

    static constraints = {
    }

    static mapping = {
        table("ACTIVITY_TYPE")
    }

    @Override
    String toString() {
        this.value
    }
}
