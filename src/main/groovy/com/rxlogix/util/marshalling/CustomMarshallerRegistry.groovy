package com.rxlogix.util.marshalling

import groovy.util.logging.Slf4j


/**
 * For use as a spring bean to auto register Marshallers in the util.marshalling package. Drop your
 * marshaller ending w/ *Marshaller* into this package and it will be automatically registered during
 * bootstrap execution.
 */
@Slf4j
class CustomMarshallerRegistry {

    def register() {
        def marshallers = getMarshallerClassFiles()

        marshallers.each { Class marshallerClass ->
            def marshaller = marshallerClass.newInstance()
            marshaller.register()
            log.info("${marshaller.class.getSimpleName()} was registered")
        }
    }

    /**
     *
     * @return Class[] for the Marshallers in the com.rxlogix.util.marshalling package
     */
    private Class[] getMarshallerClassFiles() {
        def marshallerPackage = "com.rxlogix.util.marshalling"
        def urls = getClass().classLoader.getResources(marshallerPackage.replace(".", "/"))
        def marshallersClassFiles = urls.collect {
            def file = new File(it.toURI())
            return file.listFiles().findAll { it.name.endsWith("Marshaller.class") }
        }.flatten()

        marshallersClassFiles.collect { File f ->
            def className = f.name.split("\\.")[0]
            getClass().classLoader.loadClass("${marshallerPackage}.${className}")
        }
    }
}
