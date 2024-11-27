package com.rxlogix.crypto

import groovy.transform.CompileStatic
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.encrypt.TextEncryptor

@Configuration
@CompileStatic
class RxEncryptionBootstrapConfiguration {

    @Configuration
    @CompileStatic
    protected static class RxCodecConfiguration {
        @Bean
        @ConditionalOnClass(RxTextEncryptor.class)
        public TextEncryptor rxEncryptor() {
            return new RxTextEncryptor()
        }
    }
}