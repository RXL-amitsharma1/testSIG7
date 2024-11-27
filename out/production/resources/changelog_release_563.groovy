import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_5.6.1.groovy'
    }
    include file: '2023-03-22-signal-alert-association-table-changes.groovy'
    include file: '2023-04-24-adding-event-id-column.groovy'
    include file: '2022-11-15-alert-deletion-changes.groovy'
    include file: '2022-04-11-create-pvs-app-configuration-table.groovy'
    include file: '2023-01-05-justification-length-issue.groovy'
}
