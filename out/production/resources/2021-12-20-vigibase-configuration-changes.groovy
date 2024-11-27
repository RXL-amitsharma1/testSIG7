import com.rxlogix.EmailNotification
import com.rxlogix.user.Preference
import  com.rxlogix.json.JsonOutput;
import grails.util.Holders;
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
databaseChangeLog = {

	changeSet(author: "mohit (generated)", id: "1613730275841-01") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'vigibase_date_range')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "vigibase_date_range", type: "VARCHAR2(255 CHAR)")
		}
	}

	changeSet(author: "mohit (generated)", id: "1613730275841-02") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ALERT', columnName: 'vigibase_columns')
			}
		}
		addColumn(tableName: "AGG_ALERT") {
			column(name: "vigibase_columns", type: "varchar2(4000 CHAR)")
		}
	}

	changeSet(author: "mohit (generated)", id: "1613730275841-03") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'ARCHIVED_AGG_ALERT', columnName: 'VIGIBASE_COLUMNS')
			}
		}
		addColumn(tableName: "ARCHIVED_AGG_ALERT") {
			column(name: "VIGIBASE_COLUMNS", type: "varchar2(4000 CHAR)")
		}
	}
	changeSet(author: "mohit (generated)", id: "1613730275841-04") {

		preConditions(onFail: 'MARK_RAN') {
			not{
				sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
			}
			sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'ACA_VIGIBASE'")
		}

		grailsChange {
			change{
				try {
					EmailNotification acaVigibase = new EmailNotification(key: 'ACA_VIGIBASE', moduleName: 'Alert Trigger Email for Aggregate Review Alert for VigiBase', isEnabled: true, defaultValue: true)
					acaVigibase.save(flush: true, failOnError: true)
				} catch (Exception ex) {
					println(ex)
					println("######### Error occurred while creating Email Notification module for Alert Trigger Email for Aggregate Review Alert for VigiBase ###########")
				}
			}
		}
	}

	changeSet(author: "mohit (generated)", id: "1613730275841-05") {

		preConditions(onFail: 'MARK_RAN') {
			not{
				sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
			}
			sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'DISPOSITION_CHANGE_ACA_VIGIBASE'")
		}

		grailsChange {
			change{
				try {
					EmailNotification notification = new EmailNotification(key: 'DISPOSITION_CHANGE_ACA_VIGIBASE', moduleName: 'Disposition Change Notification for Aggregate Review Alerts for VigiBase', isEnabled: true, defaultValue: true)
					notification.save(flush: true, failOnError: true)
				} catch (Exception ex) {
					println(ex)
					println("######### Error occurred while creating Email Notification module for Disposition Change Notification for Aggregate Review Alerts for VigiBase ###########")
				}
			}
		}
	}

	changeSet(author: "mohit (generated)", id: "1613730275841-06") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'vigibase_cum_case_series_id')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "vigibase_cum_case_series_id", type: "NUMBER(19, 0)")
		}
	}

	changeSet(author: "mohit (generated)", id: "1613730275841-07") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'vigibase_case_series_id')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "vigibase_case_series_id", type: "NUMBER(19, 0)")
		}
	}

	changeSet(author: "Mohit(generated)", id: "1613730275841-15") {

		preConditions(onFail: 'MARK_RAN') {
			tableExists(tableName: 'ROLE')
		}
		grailsChange {
			change{
				try {
					if(Holders.config.signal.vigibase.enabled){
						Role vigibaseRole = Role.findByAuthority("ROLE_VIGIBASE_CONFIGURATION")
						if(!vigibaseRole){
							vigibaseRole = new Role(authority: "ROLE_VIGIBASE_CONFIGURATION", description: "Perform VigiBase alerts configuration and review",
									createdBy: "Application", modifiedBy: "Application")
							vigibaseRole.save(flush: true, failOnError: true)
						}
					}

				} catch (Exception ex){
					println ("Some error occured while updating roles")
					ex.printStackTrace()
				}
			}
		}
	}

	changeSet(author: "mohit (generated)", id: "1613730275841-10") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'vaers_cum_case_series_id')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "vaers_cum_case_series_id", type: "NUMBER(19, 0)")
		}
	}

	changeSet(author: "mohit (generated)", id: "1613730275841-11") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'vaers_case_series_id')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "vaers_case_series_id", type: "NUMBER(19, 0)")
		}
	}

    //Added for evdas caselisting fileupload email and ermr fileupload email
    changeSet(author: "mohit (generated)", id: "1613730275841-12") {

        preConditions(onFail: 'MARK_RAN') {
            not{
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'ERMR_UPLOAD_FILE'")
        }

        grailsChange {
            change{
                try {
                    EmailNotification ermrFileUpload = new EmailNotification(key: 'ERMR_UPLOAD_FILE', moduleName: 'eRMR Upload Notification', isEnabled: true, defaultValue: true)
                    ermrFileUpload.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for eRMR Upload Notification ###########")
                }
            }
        }
    }

    changeSet(author: "mohit (generated)", id: "1613730275841-13") {

        preConditions(onFail: 'MARK_RAN') {
            not{
                sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
            }
            sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'CASE_LISTING_UPLOAD_FILE'")
        }

        grailsChange {
            change{
                try {
                    EmailNotification caseListingFileUpload = new EmailNotification(key: 'CASE_LISTING_UPLOAD_FILE', moduleName: 'Case Listing Upload Notification', isEnabled: true, defaultValue: true)
                    caseListingFileUpload.save(flush: true, failOnError: true)
                } catch (Exception ex) {
                    println(ex)
                    println("######### Error occurred while creating Email Notification module for Case Listing Upload Notification ###########")
                }
            }
        }
    }

	changeSet(author: "mohit (generated)", id: "1613730275841-14") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_CASE_ALERT', columnName: 'REGION')
			}
		}
		addColumn(tableName: "SINGLE_CASE_ALERT") {
			column(name: "REGION", type: "varchar2(256 char)") {
				constraints(nullable: "true")
			}
		}
	}

/*	changeSet(author: "mohit (generated)", id: "1613730275341-06") {

		preConditions(onFail: 'MARK_RAN') {
			not{
				sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
			}
			sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'ACA_INTEGRATED'")
		}

		grailsChange {
			change{
				try {
					EmailNotification emailNotification = new EmailNotification(key: 'ACA_INTEGRATED', moduleName: 'Alert Trigger Email for Aggregate Review Alert for Integrated review', isEnabled: true, defaultValue: true)
					emailNotification.save(flush: true, failOnError: true)
				} catch (Exception ex) {
					println(ex)
					println("######### Error occurred while creating Email Notification module for Alert Trigger Email for Aggregate Review Alert for Integrated review ###########")
				}
			}
		}
	}
	changeSet(author: "shivam (generated)", id: "1613730275341-07") {

		preConditions(onFail: 'MARK_RAN') {
			not{
				sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION")
			}
			sqlCheck(expectedResult: '0', "SELECT COUNT(*) FROM EMAIL_NOTIFICATION WHERE KEY = 'DISPOSITION_CHANGE_ACA_INTEGRATED'")
		}

		grailsChange {
			change{
				try {
					EmailNotification emailNotification = new EmailNotification(key: 'DISPOSITION_CHANGE_ACA_INTEGRATED', moduleName: 'Disposition Change Notification for Aggregate Review Alerts for Integrated review', isEnabled: false, defaultValue: false)
					emailNotification.save(flush: true, failOnError: true)
				} catch (Exception ex) {
					println(ex)
					println("######### Error occurred while creating Email Notification module for Disposition Change Notification for Aggregate Review Alerts for Integrated review ###########")
				}
			}
		}
	}
	changeSet(author: "suraj (generated)", id: "1631641054247-160") {

		grailsChange {
			change {
				try {
					List<Preference> prefrences = Preference.createCriteria().list {
						isNotNull("dashboardConfig")
					}
					prefrences.each {
						if (it!=null) {
							Map widgetConfig = Holders.config.signal.dashboard.widgets.config
							it.dashboardConfig = JsonOutput.toJson(widgetConfig)
							ctx.CRUDService.update(it)
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace()
					println(ex)
					println("##################### Error updating dashboardConfig #############")
				}
			}
		}
	}*/
}
