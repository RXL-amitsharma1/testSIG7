databaseChangeLog = {
    changeSet( author: "Hemlata(generated)", id: "1715678403625-001" ) {
        grailsChange {
            change {
                ctx.alertFieldService.updateRelatedColumnInMart()
            }
        }
    }
}