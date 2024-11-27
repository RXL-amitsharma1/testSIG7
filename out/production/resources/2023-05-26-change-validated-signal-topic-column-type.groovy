databaseChangeLog = {
    changeSet(author: "sarthak (generated)", id: "15643587683393-2") {

        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'TOPIC')
        }

        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "TOPIC_COPY", type: "VARCHAR2(4000 CHAR)")
        }

        sql("update VALIDATED_SIGNAL set TOPIC_COPY = TOPIC;")

        dropColumn(tableName: "VALIDATED_SIGNAL", columnName: "TOPIC")

        renameColumn(tableName: "VALIDATED_SIGNAL", oldColumnName: "TOPIC_COPY", newColumnName: "TOPIC")

    }
}