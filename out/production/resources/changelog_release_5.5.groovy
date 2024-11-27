import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_5.4.groovy'
    }

    include file: '2022-02-22-share-with-user-add-index.groovy'
    include file: '2022-03-03-add-events-emerging-issues.groovy'
    include file: '2022-03-09-modify-emerging-issues-columns-type-to-clob.groovy'
    include file: '2022-03-29-Add-Mail-OAuth-Token.groovy'
    include file: '2022-03-30-remove_foreign_constraints.groovy'
    include file: '2022-03-01-master-child-status-table-add.groovy'
    include file: '2022-04-05-master-child-integrated-column-add.groovy'
    include file: '2022-04-11-create-pvs-app-configuration-table.groovy'

    // for grails version upgrade from 3.29 to 3.3.9
    include file: 'changelog_grails_upgrade.groovy'

    include file: '2021-12-20-vigibase-configuration-changes.groovy'
    include file: '2022-04-05-Comment-Template-Added.groovy'
    include file: '2022-06-16-add-config-datasheet.groovy'
    include file: '2022-04-12-data-mining-variable-value-column.groovy'
    include file: '2022-05-18-Added-A,B,C,D,E,RR-Columns.groovy'
    include file: '2022-07-18-Added-dateCreated -Columns.groovy'
    include file: '2022-07-28-master-child-integrated-column-add2.groovy'
    include file: '2022-08-05-signal-rmm-email-column-add.groovy'
    include file: '2022-07-28-dashoard_count_correction.groovy'
    include file: '2022-10-03-reference_link_column_length_changes.groovy'
    include file: '2022-09-26-evdas-description-column-length-increase.groovy'
    include file: '2022-09-28-signal_history_justification_column.groovy'
    include file: '2022-09-28-meeting_minutes_column_length.groovy'
}