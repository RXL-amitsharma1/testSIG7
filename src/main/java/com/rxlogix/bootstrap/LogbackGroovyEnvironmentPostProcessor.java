package com.rxlogix.bootstrap;

import ch.qos.logback.classic.LoggerContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.impl.StaticLoggerBinder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;


// Custom hook to disable springboot logging initialization after shutting down logback initialized context. It's a temp fix till we get fix from https://github.com/virtualdogbert/logback-groovy-config
public class LogbackGroovyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        LoggerContext loggerContext = getLoggerContext();
        // We are disabling springboot logger init process as logback-config already initialized.
        if (loggerContext.getObject(LoggingSystem.class.getName()) == null) {
            loggerContext.putObject(LoggingSystem.class.getName(), new Object());
            //Added to remove LogbackLoggingSystem.FILTER
            loggerContext.getTurboFilterList().stream().filter(x -> x.getClass().isAnonymousClass()).findAny().ifPresent(turboFilter -> loggerContext.getTurboFilterList().remove(turboFilter));
        }
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    private LoggerContext getLoggerContext() {
        ILoggerFactory factory = StaticLoggerBinder.getSingleton().getLoggerFactory();
        if (!(factory instanceof LoggerContext)) {
            throw new RuntimeException("Startup failure as StaticLoggerBinder factory is not of type LoggerContext. Please check if logback-config configured correctly.");
        }
        return (LoggerContext) factory;
    }
}