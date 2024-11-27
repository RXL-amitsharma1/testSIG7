package com.rxlogix.dto

import com.rxlogix.config.ActivityType
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.user.Group
import com.rxlogix.user.User


class ActivityDTO {

    def executedConfiguration
    ActivityType type
    User loggedInUser
    String details
    String justification
    Map attributes
    String product
    String event
    User assignedToUser
    String caseNumber
    Group assignedToGroup
    String guestAttendeeEmail

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof ActivityDTO)) return false

        ActivityDTO that = (ActivityDTO) o
        if (caseNumber != that.caseNumber) return false
        if (type != that.type) return false
        if (event != that.event) return false
        if (product != that.product) return false
        if (details != that.details) return false
        if (executedConfiguration != that.executedConfiguration) return false
        if (loggedInUser != that.loggedInUser) return false

        return true
    }

    @Override
    int hashCode() {
        int result
        result = event? event.hashCode() : 0
        result = type? type.hashCode() : 0
        result = 31 * result + (product ? product.hashCode() : 0)
        result = 31 * result + (details ? details.hashCode() : 0)
        result = 31 * result + (executedConfiguration ? executedConfiguration.hashCode() : 0)
        result = 31 * result + (loggedInUser ? loggedInUser.hashCode() : 0)
        return result
    }

}
