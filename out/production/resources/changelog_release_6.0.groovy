import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_5.6.1.groovy'
    }
    include file: 'changelog_release_5.6.2.2.groovy'
    include file: 'changelog_release_563.groovy'
    include file: '2023-01-19-spotfire-job-status-table.groovy'
    include file: '2023-01-16-system-precheck-table.groovy'
    include file: '2022-02-03-validated-signal-table-new-product-and-event-column-added-for-sorting.groovy'
    include file: '2023-01-17-alert-progress-resume.groovy'
    include file: '2023-06-14-db-add-last-login.groovy'
    include file: '2023-02-06-non-ldap-user-login.groovy'
    include file: '2023-02-01-product-type-configuration-table.groovy'
    include file: '2023-02-13-product-type-configuration-migration.groovy'
    include file: '2023-02-17-add-constraint-emerging-issue.groovy'
    include file: '2023-02-21-signal-alert-association-table-changes.groovy'
    include file: '2023-03-16-db-column-length-increased.groovy'
    include file: '2023-05-04-make-description-column-4000-characters-business-configuration.groovy'

}
