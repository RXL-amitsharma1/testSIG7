databaseChangeLog = {

    changeSet(id: "19976543213-363", author: "Gaurav (generated)") {
        sqlFile(path: "create-embase-tables.sql",
                relativeToChangelogFile: true, // Set to true if the path is relative
                endDelimiter: ";",
                splitStatements: true,
                stripComments: true)
    }
}