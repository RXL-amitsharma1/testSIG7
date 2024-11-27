package pvsignal

import asset.pipeline.AssetFile
import asset.pipeline.AssetSpecLoader
import com.rxlogix.session.PVHttpSessionServletListener
import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration
import groovy.util.logging.Slf4j
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration
import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.EnvironmentAware
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.env.Environment

import javax.servlet.ServletContext
@ServletComponentScan
@ComponentScan
@EnableAutoConfiguration(exclude = [SecurityFilterAutoConfiguration])
@Slf4j
class Application extends GrailsAutoConfiguration implements EnvironmentAware {
    PVHttpSessionServletListener pvSessionListener
    ServletContext servletContext

    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    @Override
    void setEnvironment(Environment environment) {
        patch()
        log.debug "Executing Assets Loader - Time: ${System.currentTimeMillis()}"
        AssetSpecLoader.loadSpecifications()
        log.debug "Executed Assets Loader - Time: ${System.currentTimeMillis()}"
    }

    def patch() {
        AssetSpecLoader.metaClass.static.loadSpecifications = { ClassLoader classLoader ->
            if (classLoader == null)
                classLoader = Thread.currentThread().contextClassLoader

            if (specifications == null) {
                def resources = classLoader.getResources(FACTORIES_RESOURCE_LOCATION)
                specifications = []

                resources.each { URL res ->
                    def classNames = null

                    try {
                        classNames = res.getText('UTF-8').split(/\r?\n/).collect() { String str -> str.trim() }

                        if (classNames) {
                            for (className in classNames) {
                                try {
                                    def cls = classLoader.loadClass(className)
                                    if (AssetFile.isAssignableFrom(cls)) {
                                        if (!specifications.contains(cls))
                                            specifications << (Class<AssetFile>) cls
                                    } else {
                                        log.warn("Asset specification $className not registered because it does not implement the AssetFile interface")
                                    }
                                } catch (Throwable e) {
                                    log.error("Error loading asset specification $className: $e.message", e)
                                }
                            }
                        }
                    } catch (all) {
                        log.error("failed load the assets.specs")
                    }
                }
                applyProcessors(classLoader)
            }
            return specifications
        }
    }

    @Override
    void onStartup(Map<String, Object> event) {
        super.onStartup(event)
    }

    void onShutdown(Map<String, Object> event) {
        super.onShutdown(event)
    }
}