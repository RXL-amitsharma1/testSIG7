package com.rxlogix.signal

import com.rxlogix.user.User

class UserViewOrder {

    User user
    ViewInstance viewInstance
    Integer viewOrder

    static belongsTo = [viewInstance : ViewInstance]

    static constraints = {
    }
}