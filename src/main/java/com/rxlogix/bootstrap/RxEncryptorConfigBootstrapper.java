package com.rxlogix.bootstrap;


import com.rxlogix.crypto.RxTextEncryptor;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class RxEncryptorConfigBootstrapper implements BootstrapRegistryInitializer {

    @Override
    public void initialize(BootstrapRegistry registry) {
        registry.registerIfAbsent(TextEncryptor.class, context -> {
            return new RxTextEncryptor();
        });
    }
}