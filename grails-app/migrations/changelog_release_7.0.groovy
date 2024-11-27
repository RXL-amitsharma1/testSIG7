import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_6.3.groovy'
    }
    include file: '2024-07-18-7.0-Master-Child-Alert-Changes.groovy'
    include file: '2024-09-02-Agg-Alert-Trend-Flag-Column-Update.groovy'
    include file: '2024-09-02-Signal-Report-pdf-column-type-change.groovy'
    include file: '2024-09-09-emerging-issue-constraints-update.groovy'
    include file: '2024-09-27-database-password-update.groovy'
    include file: '2024-09-30-master-config-mapping-rconfig.groovy'
    include file: '2024-09-27-Signal-Memo-Report-Configuration.groovy'
    include file: '2024-10-11-literature-alert-citation-column.groovy'
    include file: '2024-10-09-literature-config-queryId-column.groovy'
    include file: '2024-10-10-embase-literature-alert-changes.groovy'
    include file: '2024-10-25-adhoc-alert-configurable-column-73937.groovy'
    include file: '2024-09-10-alert-bursting-changes.groovy'
    include file: '2024-10-21-share-with-user-config.groovy'
}

