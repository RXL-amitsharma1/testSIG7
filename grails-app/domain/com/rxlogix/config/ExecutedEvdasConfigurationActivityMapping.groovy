package com.rxlogix.config


class ExecutedEvdasConfigurationActivityMapping implements Serializable {

    ExecutedEvdasConfiguration executedEvdasConfiguration
    Activity activity

    static mapping = {
        table("EX_EVDAS_CONFIG_ACTIVITIES")
        activity column: "ACTIVITY_ID"
        executedEvdasConfiguration column: "EX_EVDAS_CONFIG_ID"
        version false
        id composite: ["executedEvdasConfiguration", "activity"]
    }

}