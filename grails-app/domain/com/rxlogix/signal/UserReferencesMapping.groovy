package com.rxlogix.signal

import com.rxlogix.user.User

class UserReferencesMapping {

    Long userId
    Long referenceId
    Integer priority
    boolean isPinned=false
    boolean isDeleted=false


    static mapping = {
        table("USER_REFERENCES_MAPPING")
        version false
    }
}
