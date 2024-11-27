import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_5.5.groovy'
    }
    include file: '2022-07-01-add-foreground-query-columns.groovy'
    include file: '2022-09-07-undoable-disposition.groovy'
    include file: '2022-09-08-13711-Alert-Pre-Checks-Changes.groovy'
    include file: '2022-11-09-System-config-changes-for-due-in.groovy'
    include file: '2022-11-09-signal_column_added.groovy'
    include file: '2022-10-11-auto-routing-pec-17284.groovy'
    include file: '2022-11-15-1425-make-notes-column-8000-characters.groovy'
    include file : '2022-11-15-1343-make-comments-column-8000-characters.groovy'
    include file: '2022-11-14-add-region-column-in-archived-sca.groovy'
    include file: '2022-11-15-alert-deletion-changes.groovy'
    include file : '2022-11-16-1009-make-commentSignalStatus-column-8000-characters.groovy'
    include file : '2022-11-23-legacy-attachment-migration.groovy'
    include file : '2022-11-29-validated-signal-reasonForEvaluation-count.groovy'
    include file: '2022-12-02-make-PT-column-4000-characters.groovy'
    include file: '2022-12-07-add-date-column-in-global-tag.groovy'
    include file: '2022-12-13-add-executed-config-in-action.groovy'
    include file: '2022-12-15-137145-Alert-Review-Completed.groovy'
    include file: '2022-12-14-carry-forward-info-in-signal-alert-jointable.groovy'

}