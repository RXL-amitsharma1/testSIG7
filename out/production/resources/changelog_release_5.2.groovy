import grails.util.Holders

databaseChangeLog = {
    if(!Holders.config.signal.legacy.migrations) {
        include file: '2020-12-22-signal-release-5.0.groovy'
        include file : '2021-03-30-signal-release_5.0-5.1.groovy'
    }
    include file : 'changelog_release_5.1.2.groovy'
    include file : '2021-06-21-signal-release_5.1-5.2.groovy'
    // all grails changes should run after migrations have been completed
    if(!Holders.config.signal.legacy.migrations) {
        include file: '2021-08-09-signal-release_5.0-5.1_grails-changes.groovy'
    }
    include file: '2021-08-09-signal-release_5.1-5.2_grails-changes.groovy'
    include file: '2021-12-20-change-log_5.2.2-5.2.3.groovy'
    include file: '2021-09-15-Agg-DSS-Columns-Add.groovy'
}