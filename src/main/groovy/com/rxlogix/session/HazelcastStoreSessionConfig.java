package com.rxlogix.session;

import grails.core.GrailsApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.config.annotation.web.http.HazelcastHttpSessionConfiguration;

@Configuration
public class HazelcastStoreSessionConfig extends HazelcastHttpSessionConfiguration {

    private GrailsApplication grailsApplication;

    public HazelcastStoreSessionConfig(GrailsApplication grailsApplication, SpringSessionConfigProperties configProperties) {
        this.grailsApplication = grailsApplication;
        this.setMaxInactiveIntervalInSeconds(configProperties.getMaxInactiveInterval());
        this.setSessionMapName(configProperties.getMapName());
    }
}
