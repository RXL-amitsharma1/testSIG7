import grails.util.Holders

databaseChangeLog = {
    if (!Holders.config.signal.legacy.migrations) {
        include file: 'changelog_release_6.2.groovy'
    }
    include file: '2024-06-03-blinded-user-group.groovy'
    include file: '2024-08-01-study-cases.groovy'
    include file: 'grailChangeMigrations.groovy'
}

