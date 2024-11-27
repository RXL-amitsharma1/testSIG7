import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_6.0.groovy'
    }
    include file: 'changelog_release_5.6.2.2.groovy'
    include file: '2023-04-12-signal-scim-script.groovy'
    include file: '2023-06-14-db-add-last-login.groovy'
    include file: '2023-03-09-business-config-rule-name-unique.groovy'
    include file: '2023-05-12-new-count-column-added.groovy'
    include file: '2023-06-05-validated-signal-dynamic-column.groovy'
    include file: '2023-04-26-additional-disproportional-scores.groovy'
    include file: '2023-07-11-case-narrative-config.groovy'
    include file: '2023-08-10-add-new-group-role.groovy'
    include file: '2023-06-26-Migration-for-Audit-log-columns.groovy'
    include file: '2023-07-13-update-fields-to-8k-characters.groovy'
    include file: '2023-08-14-evdas-and-aggregate-history-modification.groovy'
    include file: '2023-08-16-remove-unique-constraint-on-name-field-of-disposition-rule.groovy'
    include file: '2023-09-21-add-multi-ingredient-column.groovy'
    include file: '2023-09-20-multi-ingredient.groovy'
    include file: '2023-09-26-action-justifictaion-justification-column.groovy'
    include file: '2023-10-06-Context-Setting-PVS.groovy'
    include file: '2023-06-05-signal-closed-based-on-disposition.groovy'
    include file: '2023-08-08-criteria-counts-agg.groovy'
    include file: '2023-10-27-dashboard-role-removed.groovy'
    include file: '2023-10-31-literature-activity.groovy'
    include file: '2023-11-10-Signal-parameter-length-change.groovy'
    include file: '2023-12-18-make-contents-column-32000-characters.groovy'
    include file: '2024-01-26-update-meeting-cancelled-activity-type.groovy'
    include file: '2024-02-08-alter-signal-status-nullable.groovy'
    include file: '2024-02-12-index-for-alerts-tables.groovy'
    include file: '2024-01-02-jader-datasource.groovy'
    include file: '2024-02-20-update-format-size-in-business-rule.groovy'
    include file: '2024-02-23-index-for-agg-alerts-name.groovy'
    include file: '2024-03-06-patient-med-hist-column-size-increase.groovy'
    include file: '2024-02-26-indexes-for-tables-related-to-case-details-retrieving-logic.groovy'
    include file: '2024-03-19-BR-JSON-Migration.groovy'
    include file: '2024-03-20-update-validated-signal-type-to-clob.groovy'
    include file: '2024-04-02-make-notes-column-32000-characters.groovy'
    include file: '2024-04-12-category-column-1020-characters.groovy'
    include file: '2024-04-13-Cross-reference-ind-FDA.groovy'
    include file: '2024-04-17-Removal-feature-for-drug-classification-business-rule.groovy'
    include file: '2024-04-18-Added-timestamp-column-caseHistorytable.groovy'
    include file: '2024-04-22-index-eventname-product-event-history.groovy'
    include file: '2024-05-16-Added-isClearDataMining-ExStatusTable.groovy'
    include file: '2024-05-14-related-column.groovy'
    include file: '2024-05-13-Index-Single-Alert-Pt.groovy'
    include file:'2024-05-21-therapy-date-column-size-increase.groovy'
    include file:'2024-08-21-icr-and-aggregate-on-demand-indexing.groovy'
    include file: 'grailChangeMigrations.groovy'
}

