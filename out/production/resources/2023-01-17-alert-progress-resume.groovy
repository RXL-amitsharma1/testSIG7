import com.rxlogix.EmailNotification
import com.rxlogix.user.Preference
import  com.rxlogix.json.JsonOutput;
import grails.util.Holders;
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
databaseChangeLog = {


	changeSet(author: "mohit (generated)", id: "167116605726599-2") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'APP_ALERT_PROGRESS_STATUS')
			}
		}

		createTable(tableName: "APP_ALERT_PROGRESS_STATUS") {
			column(name: "ID", type: "NUMBER(19, 0)") {
				constraints(primaryKey: "true", primaryKeyName: "APP_ALERT_PROGRESS_STATUSPK")
			}

			column(name: "EX_STATUS_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "EXECUTED_CONFIG_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "NAME", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "PROGRESS_STATUS", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "FINAL_STATUS", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "START_TIME", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "END_TIME", type: "NUMBER(19, 0)") {
				constraints(nullable: "true")
			}

			column(name: "TYPE", type: "varchar2(255 char)") {
				constraints(nullable: "false")
			}

			column(name: "VERSION", type: "number(19,0)") {
				constraints(nullable: "false")
			}

			column(name: "date_created", type: "TIMESTAMP") {
				constraints(nullable: "false")
			}

			column(name: "last_updated", type: "TIMESTAMP") {
				constraints(nullable: "false")
			}
		}
	}

}
