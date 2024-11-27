package com.rxlogix.security

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * This annotation is intended to secure an endpoint or all endpoints of a controller.
 * To access the secured resource, the user must have at least one of the authorities listed in the "value" array.
 * Using the annotation with an empty "value" array means that the user must be authenticated (not anonymous) to access the resource.
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.METHOD])
@interface Authorize {
    String[] value() default []
}