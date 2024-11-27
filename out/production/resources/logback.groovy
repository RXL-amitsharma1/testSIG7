import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

String userHome  = System.properties.'user.home'
String hostname =  InetAddress.getLocalHost().getHostName()

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

// See http://logback.qos.ch/manual/groovy.html for details on configuration
if (!Environment.isDevelopmentMode()) {
//
//    appender('FILE', FileAppender) {
//        file = "${userHome}/.signal/pvsignal.log"
//        append = true
//        encoder(PatternLayoutEncoder) {
//            charset = Charset.forName('UTF-8')
//
//            pattern =
//                    '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
//                            '%clr(%5p) ' + // Log level
//                            '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
//                            '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
//                            '%m%n%wex' // Message
//        }
//    }

    appender("ROLLING", RollingFileAppender) {
        encoder(PatternLayoutEncoder) {
            pattern =
                    '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                            '%clr(%5p) ' + // Log level
                            '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                            '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                            '%m%n%wex' // Message
        }
        rollingPolicy(TimeBasedRollingPolicy) {
            fileNamePattern = "${userHome}/.signal/pvsignal-${hostname}-%d{dd-MMM-yyyy}.log"
            maxHistory = 30
        }
    }

//    logger("grails.app", INFO, ['STDOUT'], false)
//    logger("grails.app", ALL, ['FILE'], false)
//    logger("grails.app", ALL, ['ROLLING', 'STDOUT'], false)
    logger("com.rxlogix", INFO, ['ROLLING', 'STDOUT'], false)
    logger("org.hibernate", ERROR, ['ROLLING', 'STDOUT'], false)
    logger("pvsignal.BootStrap", INFO, ['ROLLING', 'STDOUT'], false)
    logger("liquibase", INFO, ['ROLLING', 'STDOUT'], false)
    logger('org.grails', ERROR, ['ROLLING', 'STDOUT'], false)
    logger('org.springframework', ERROR, ['ROLLING', 'STDOUT'], false)
//    root(INFO, ['STDOUT'])
}
else{
    logger('org.opensaml', DEBUG, ['STDOUT'], false)
    logger('org.springframework.security.provider', DEBUG, ['STDOUT'], false)
    logger('org.springframework.security.saml', DEBUG, ['STDOUT'], false)
    logger("grails.app", INFO, ['STDOUT'], false)
    root(INFO, ['STDOUT'])
}