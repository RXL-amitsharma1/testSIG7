package com.rxlogix.config


class MasterEvdasExecutedConfig {

    String name
    Date dateCreated
    Date lastUpdated
    Long masterConfigId

    static constraints = {
        masterConfigId nullable: true
    }

    static mapping = {
        id generator:'sequence', params:[sequence:'evdas_exec_config_sequence']
    }

}
