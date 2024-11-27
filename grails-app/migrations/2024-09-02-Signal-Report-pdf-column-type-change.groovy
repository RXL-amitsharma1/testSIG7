databaseChangeLog = {
    changeSet(author: "Gaurav (generated)", id: "19976543213-348") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'oid', "SELECT data_type FROM information_schema.columns WHERE table_name = 'signal_report' AND column_name = 'excel_report';")
        }
        sql("ALTER TABLE signal_report ADD COLUMN excel_report_bytea bytea;")
        sql("UPDATE signal_report SET excel_report_bytea = lo_get(excel_report);")
        sql("ALTER TABLE signal_report DROP COLUMN excel_report;")
        sql("ALTER TABLE signal_report RENAME COLUMN excel_report_bytea TO excel_report;")

    }

    changeSet(author: "Gaurav (generated)", id: "19976543213-349") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'oid', "SELECT data_type FROM information_schema.columns WHERE table_name = 'signal_report' AND column_name = 'pdf_report';")
        }
        sql("ALTER TABLE signal_report ADD COLUMN pdf_report_bytea bytea;")
        sql("UPDATE signal_report SET pdf_report_bytea = lo_get(pdf_report);")
        sql("ALTER TABLE signal_report DROP COLUMN pdf_report;")
        sql("ALTER TABLE signal_report RENAME COLUMN pdf_report_bytea TO pdf_report;")

    }

    changeSet(author: "Gaurav (generated)", id: "19976543213-350") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'oid', "SELECT data_type FROM information_schema.columns WHERE table_name = 'signal_report' AND column_name = 'word_report';")
        }
        sql("ALTER TABLE signal_report ADD COLUMN word_report_bytea bytea;")
        sql("UPDATE signal_report SET word_report_bytea = lo_get(word_report);")
        sql("ALTER TABLE signal_report DROP COLUMN word_report;")
        sql("ALTER TABLE signal_report RENAME COLUMN word_report_bytea TO word_report;")

    }

    changeSet(author: "Shoaib Akhtar (generated)", id: "19976541213-089") {
        preConditions(onFail: "MARK_RAN") {
            columnExists(tableName: "report_history", columnName: "memo_report")
        }

        sql("ALTER TABLE report_history DROP COLUMN memo_report;")
        sql("ALTER TABLE report_history ADD COLUMN memo_report BYTEA;")
    }


    changeSet(author: "Mohit (generated)", id: "19976543213-352") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: 'oid', "SELECT data_type FROM information_schema.columns WHERE table_name = 'file_attachments' AND column_name = 'source_attachments';")
        }
        // Add a new column of type bytea
        sql("""
            ALTER TABLE FILE_ATTACHMENTS
            ADD COLUMN source_attachments_temp bytea;
        """)

        // Copy data from the old column to the new column
        sql("""
            UPDATE FILE_ATTACHMENTS
            SET source_attachments_temp = lo_get(source_attachments);
        """)

        // Drop the old column
        sql("""
            ALTER TABLE FILE_ATTACHMENTS
            DROP COLUMN source_attachments;
        """)

        // Rename the new column to the original column name
        sql("""
            ALTER TABLE FILE_ATTACHMENTS
            RENAME COLUMN source_attachments_temp TO source_attachments;
        """)
    }

    changeSet(author: "Mohit (generated)", id: "19976543213-353") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', """
            SELECT COUNT(*) 
            FROM pg_constraint 
            WHERE conname = 'uc_validated_signalname_col' 
              AND conrelid = (
                SELECT oid 
                FROM pg_class 
                WHERE relname = 'validated_signal'
              );
        """)
        }
        dropUniqueConstraint(constraintName: "uc_validated_signalname_col", tableName: "validated_signal")
    }

    changeSet(author: "Shoaib Akhtar(generated)", id: "19976543098", failOnError: true) {
        grailsChange {
            change {
                ctx.alertFieldService.updatingDataTypeOfAggField()
            }
        }
    }


}
