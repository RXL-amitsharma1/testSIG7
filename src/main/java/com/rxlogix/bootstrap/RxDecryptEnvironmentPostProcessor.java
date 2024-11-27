package com.rxlogix.bootstrap;

import com.rxlogix.crypto.RxTextEncryptor;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.bootstrap.encrypt.DecryptEnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import java.util.Map;


public class RxDecryptEnvironmentPostProcessor extends DecryptEnvironmentPostProcessor {

    //!!Important overridden method to override DecryptEnvironmentPostProcessor due to custom encryptor.
    // Use spring.cloud.decrypt-environment-post-processor.enabled =false to disable.
    // https://github.com/spring-cloud/spring-cloud-commons/issues/897
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources propertySources = environment.getPropertySources();
        environment.getPropertySources().remove(DECRYPTED_PROPERTY_SOURCE_NAME);
        Map<String, Object> map = decrypt(getTextEncryptor(environment), propertySources);
        if (!map.isEmpty()) {
            // We have some decrypted properties
            propertySources.addFirst(new SystemEnvironmentPropertySource(DECRYPTED_PROPERTY_SOURCE_NAME, map));
        }
    }

    protected TextEncryptor getTextEncryptor(ConfigurableEnvironment environment) {
        return new RxTextEncryptor();
    }
}
