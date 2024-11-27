package com.rxlogix.config


class ExecutedConfigurationActivityMapping implements Serializable {

    ExecutedConfiguration executedConfiguration
    Activity activity

    static mapping = {
        table("ex_rconfig_activities")
        activity column: "ACTIVITY_ID"
        executedConfiguration column: "EX_CONFIG_ACTIVITIES_ID"
        version false
        id composite:["executedConfiguration","activity"]
    }

}