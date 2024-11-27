package com.rxlogix.config

class TopicCategory {
    static auditable = true

    String name

    static mapping = {
        table("TOPIC_CATEGORY")
        sort "name"
    }
    @Override
    String toString(){
        "$name"
    }

    def getInstanceIdentifierForAuditLog() {
        return name
    }
}
