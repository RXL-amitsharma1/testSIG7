import com.rxlogix.Constants
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.signal.SignalNotificationMemo

databaseChangeLog = {

	changeSet(author: "ujjwal (generated)", id: "1613730275340-16") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SIGNAL_NOTIFICATION_MEMO')
			}
		}
		createTable(tableName: "SIGNAL_NOTIFICATION_MEMO") {
			column(autoIncrement: "true", name: "id", type: "NUMBER(19, 0)") {
				constraints(primaryKey: "true", primaryKeyName: "SIGNAL_NOTIFICATION_MEMOPK")
			}

			column(name: "version", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "config_name", type: "VARCHAR2(255 CHAR)") {
				constraints(nullable: "false")
			}

			column(name: "signal_source", type: "VARCHAR2(4000 CHAR)") {
				constraints(nullable: "true")
			}

			column(name: "trigger_variable", type: "VARCHAR2(255 CHAR)") {
				constraints(nullable: "true")
			}

			column(name: "trigger_value", type: "VARCHAR2(4000 CHAR)") {
				constraints(nullable: "true")
			}

			column(name: "email_subject", type: "VARCHAR2(255 CHAR)") {
				constraints(nullable: "true")
			}

			column(name: "email_body", type: "VARCHAR2(4000 CHAR)") {
				constraints(nullable: "true")
			}

			column(name: "date_created", type: "TIMESTAMP") {
				constraints(nullable: "true")
			}

			column(name: "email_address", type: "VARCHAR2(4000 CHAR)"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-22") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SIGNAL_RMMs', columnName: 'criteria')
			}
		}
		addColumn(tableName: "SIGNAL_RMMs") {
			column(name: "criteria", type: "VARCHAR2(8000 CHAR)")
		}
	}


	changeSet(author: "ujjwal (generated)", id: "1613730275340-25") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SIGNAL_NOTIFICATION_MEMO', columnName: 'updated_by')
			}
		}
		addColumn(tableName: "SIGNAL_NOTIFICATION_MEMO") {
			column(name: "updated_by", type: "VARCHAR2(255 CHAR)", defaultValue: "System") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-13") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'MAIL_USERS_MEMO')
			}
		}
		createTable(tableName: "MAIL_USERS_MEMO") {
			column(name: "SIGNAL_NOTIFICATION_MEMO_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "USER_ID", type: "NUMBER(19, 0)")

			column(name: "mail_users_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-14") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'MAIL_GROUPS_MEMO')
			}
		}
		createTable(tableName: "MAIL_GROUPS_MEMO") {
			column(name: "SIGNAL_NOTIFICATION_MEMO_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "GROUP_ID", type: "NUMBER(19, 0)")

			column(name: "mail_groups_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-8") {

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'SIGNAL_RMMs', columnName: 'status')
		}
		sql("ALTER TABLE SIGNAL_RMMs MODIFY status VARCHAR2(255 CHAR) NULL;")
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-9") {

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'SIGNAL_RMMs', columnName: 'due_date')
		}
		sql("ALTER TABLE SIGNAL_RMMs MODIFY due_date TIMESTAMP(6) NULL;")
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-20") {
		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'signal_email_log', columnName: 'assigned_to')
		}
		sql("alter table signal_email_log modify assigned_to VARCHAR2(8000 CHAR);")
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-30") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SIGNAL_OUTCOME', columnName: 'IS_DISABLED')
			}
		}
		addColumn(tableName: "SIGNAL_OUTCOME") {
			column(name: "IS_DISABLED", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-31") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SIGNAL_OUTCOME', columnName: 'IS_DELETED')
			}
		}
		addColumn(tableName: "SIGNAL_OUTCOME") {
			column(name: "IS_DELETED", type: "number(1,0)", defaultValueBoolean: "false") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275340-33") {
		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'SIGNAL_NOTIFICATION_MEMO', columnName: 'SIGNAL_SOURCE')
		}
		grailsChange {
			change {
				List<SignalNotificationMemo> signalNotificationMemoList = SignalNotificationMemo.findAllBySignalSource(Constants.NULL_STRING)
				signalNotificationMemoList.each { SignalNotificationMemo signalNotificationMemo ->
					signalNotificationMemo.signalSource = null
					signalNotificationMemo.save(flush:true)
				}
				confirm "Successfully Updated Signal source field in SIGNAL_NOTIFICATION_MEMO Table."
			}
		}
	}

}