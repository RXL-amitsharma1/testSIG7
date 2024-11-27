import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: '2020-12-22-signal-release-5.0.groovy'
        include file: '2021-03-30-signal-release_5.0-5.1.groovy'
        include file: '2021-06-21-signal-release_5.1-5.2.groovy'
        include file: 'changelog_release_5.4.groovy'
        include file: 'changelog_release_5.5.groovy'
        include file: 'changelog_release_5.6.groovy'
        include file: 'changelog_release_5.6.1.groovy'
        include file: 'changelog_release_5.6.2.2.groovy'
        include file: 'changelog_release_563.groovy'
        include file: 'changelog_release_6.0.groovy'
    }
    include file: '2023-06-26-Migration-for-Audit-log-columns.groovy'
    include file: '2023-07-13-update-fields-to-8k-characters.groovy'
    include file: '2023-08-14-evdas-and-aggregate-history-modification.groovy'
    include file: '2023-08-16-remove-unique-constraint-on-name-field-of-disposition-rule.groovy'
}