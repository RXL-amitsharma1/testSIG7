import grails.util.Holders

databaseChangeLog = {
    if(!Holders.config.signal.legacy.migrations) {
        include file: '2020-12-22-signal-release-5.0.groovy'
        include file : '2021-03-30-signal-release_5.0-5.1.groovy'
        include file : '2021-06-21-signal-release_5.1-5.2.groovy'
    }
    include file : '2021-16-21-trend-flag-calculation.groovy'
    // These files are from 512 was not merged in 52 release so this will go in 54
    include file: '2021-07-06-Primary_susp_pai_new_column.groovy'
    include file: '2021-07-09-all-pt-field-added.groovy'
    include file: '2021-07-23-add-config-nodename-column.groovy'
    // for token update and batch lot dropdown options
    include file : '2021-10-06-Actual-due-date-and-milestone-completion-date.groovy'
    include file : '2021-10-06-signal-outcome-and-disposition-mapping.groovy'
    include file : '2021-09-15-Agg-DSS-Columns-Add.groovy'
    include file : '2021-11-16-Archive-Agg-DSS-Columns-Add.groovy'
    include file : '2021-09-03-add-reference-detail.groovy'
    include file: '2021-09-22-batch-changesets.groovy'
    include file: '2021-10-20-Alert-details-enhancements.groovy'
    include file: '2021-08-23-vaers-configuration-changes.groovy'
    include file: '2021-10-28-add-master-config-tables.groovy'
    // all grails changes should run after migrations have been completed
    include file: '2021-12-27-5.3-5.4-grails-changes.groovy'
    include file : '2022-01-20-add-json-field-column.groovy'
    include file : '2022-02-22-share-with-user-add-index.groovy'
    include file: '2022-03-03-add-events-emerging-issues.groovy'
    include file : '2022-03-09-modify-emerging-issues-columns-type-to-clob.groovy'
    include file: '2022-03-29-Add-Mail-OAuth-Token.groovy'
    include file : '2022-03-30-remove_foreign_constraints.groovy'
    include file : '2022-03-01-master-child-status-table-add.groovy'
    include file : '2022-04-05-master-child-integrated-column-add.groovy'
    include file : '2022-04-11-create-pvs-app-configuration-table.groovy'
    include file: '2022-06-22-update-sca-faers-view-instance.groovy'
    include file: '2022-06-30-update-product-group-data-column-to-clob.groovy'

    // for grails version upgrade from 3.29 to 3.3.9
    include file: 'changelog_grails_upgrade.groovy'
    include file : '2022-04-27-add-scimId-column-user-domain.groovy'
    include file : '2022-07-11-add-indexing-Single-Evdas-Aggregate.groovy'
    include file : '2022-07-28-dashoard-due-date-widget-correction.groovy'
    include file : '2022-08-26-template-query-table-indexing.groovy'
}