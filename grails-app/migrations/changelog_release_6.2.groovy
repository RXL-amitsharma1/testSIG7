import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_6.1.groovy'
    }
    include file:'2024-08-21-icr-and-aggregate-on-demand-indexing.groovy'
    include file:'2024-05-31-who-drug-record-number.groovy'
    include file:'2024-07-03-product-id-column-length-increased.groovy'
    include file: '2024-06-22-Added_Is_MultiIngredient_Column_In_ADhoc.groovy'
    include file: 'changelog-report-history-14-06-2024.groovy'
    include file: '2024-08-07-update-filename-size-evdasfileprocesslog_table.groovy' //Added for PVS-59164
}
