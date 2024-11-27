import grails.util.Holders

databaseChangeLog = {
    if(!Holders.config.signal.legacy.migrations) {
        include file: '2020-12-22-signal-release-5.0.groovy'
    }
    include file : '2021-03-30-signal-release_5.0-5.1.groovy'
    include file : '2021-05-10-temp-column-seq-change.groovy'
}