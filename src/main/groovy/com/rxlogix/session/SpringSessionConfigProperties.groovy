package com.rxlogix.session

class SpringSessionConfigProperties {
    int maxInactiveInterval
    String mapName
    Boolean allowPersistMutable

    private static SpringSessionConfigProperties configProperties;

    SpringSessionConfigProperties(ConfigObject springSessionConfig) {
        maxInactiveInterval = springSessionConfig.timeout.interval ?: 1800
        mapName = springSessionConfig.map.name ?: 'spring:session'
        allowPersistMutable = springSessionConfig.allow.persist.mutable ?: false
    }
}
