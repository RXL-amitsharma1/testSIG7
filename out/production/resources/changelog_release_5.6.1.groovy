import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_5.6.groovy'
    }
    include file: '2023-01-05-justification-length-issue.groovy'
    include file: '2023-03-22-signal-alert-association-table-changes.groovy'
    include file: '2023-05-26-change-validated-signal-topic-column-type.groovy'
    include file: '2023-06-21-evdas_history_undo.groovy'
    include file: '2023-04-24-adding-event-id-column.groovy'
    include file: '2023-09-18-update-fields-to-8k-characters.groovy'
    include file: '2023-09-05-executed-config-master-child.groovy'
    include file: '2023-04-12-signal-scim-script.groovy'
}