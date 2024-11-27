import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.PriorityDispositionConfig
import groovy.json.JsonSlurper
import org.joda.time.DateTime

databaseChangeLog = {

	changeSet(author: "ujjwal (generated)", id: "1613730275339-200") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'removed_users')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "removed_users", type: "varchar2(2000 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-201") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'removed_users')
			}
		}
		addColumn(tableName: "EX_EVDAS_CONFIG") {
			column(name: "removed_users", type: "varchar2(2000 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-202") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'removed_users')
			}
		}
		addColumn(tableName: "EX_LITERATURE_CONFIG") {
			column(name: "removed_users", type: "varchar2(2000 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-503") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'requires_review_count')
			}
		}
		addColumn(tableName: "EX_EVDAS_CONFIG") {
			column(defaultValue: "0", name: "requires_review_count", type: "varchar2(255 CHAR)")
		}

		grailsChange {
			change {
				ctx.dispositionService.updateReviewCountsForAllExecutedConfigurations(sql, "EX_EVDAS_CONFIG")
				confirm "Successfully Updated RequireReviewCount values in EX_EVDAS_CONFIG Table."
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-504") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'requires_review_count')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(defaultValue: "0", name: "requires_review_count", type: "varchar2(255 CHAR)")
		}

		grailsChange {
			change {
				ctx.dispositionService.updateReviewCountsForAllExecutedConfigurations(sql, "EX_RCONFIG")
				confirm "Successfully Updated RequireReviewCount values in EX_RCONFIG Table."
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-208") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_RCONFIG', columnName: 'product_name')
			}
		}
		addColumn(tableName: "EX_RCONFIG") {
			column(name: "product_name", type: "clob")
		}

		grailsChange {
			change {
				List<Map> execConfigList = []
				List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllByIsDeletedAndIsEnabledAndIsLatest(false, true, true)
				ctx.dataObjectService.prepareProductDictValues()
				ctx.dataObjectService.setIdLabelMap()
				ctx.dataObjectService.setLabelIdMap()
				executedConfigurationList.each { ExecutedConfiguration executedConfiguration ->
					execConfigList.add(id: executedConfiguration.id, products: ctx.reportExecutorService.generateProductName(executedConfiguration))
				}
				sql.withBatch(100, "UPDATE EX_RCONFIG SET product_name = :products WHERE ID = :id", { preparedStatement ->
					execConfigList.each {
						preparedStatement.addBatch(id: it.id, products: it.products)
					}
				})
				confirm "Successfully Updated ProdcutName in EX_RCONFIG Table."
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-800") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'product_name')
			}
		}
		addColumn(tableName: "EX_EVDAS_CONFIG") {
			column(name: "product_name", type: "varchar2(1000 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-801") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_EVDAS_CONFIG', columnName: 'review_due_date')
			}
		}
		addColumn(tableName: "EX_EVDAS_CONFIG") {
			column(name: "review_due_date", type: "TIMESTAMP")
		}
		grailsChange {
			change {
				List<ExecutedEvdasConfiguration> executedConfigurationList = ExecutedEvdasConfiguration.findAllByIsDeletedAndIsEnabledAndIsLatest(false, true, true)
				executedConfigurationList.each { ExecutedEvdasConfiguration executedConfiguration ->
					Disposition defaultEvdasDisposition = executedConfiguration.owner.workflowGroup.defaultEvdasDisposition
					List<PriorityDispositionConfig> dispositionConfigs = executedConfiguration.priority.dispositionConfigs
					Integer reviewPeriod = dispositionConfigs?.find{it.disposition == defaultEvdasDisposition}?.reviewPeriod
					reviewPeriod = reviewPeriod ?: executedConfiguration.priority.reviewPeriod
					DateTime theDueDate = reviewPeriod ? new DateTime(executedConfiguration.dateCreated).plusDays(reviewPeriod) : new DateTime(new Date())
					executedConfiguration.reviewDueDate = theDueDate.toDate()
					ctx.evdasAlertExecutionService.generateProductName(executedConfiguration)
					executedConfiguration.save(flush:true)
				}
				confirm "Successfully Updated ReviewDueDate value in EX_RCONFIG Table."
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-505") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'requires_review_count')
			}
		}
		addColumn(tableName: "EX_LITERATURE_CONFIG") {
			column(defaultValue: "0", name: "requires_review_count", type: "varchar2(255 CHAR)")
		}

		grailsChange {
			change {
				ctx.dispositionService.updateLiteratureConfigurations(sql)
				confirm "Successfully Updated RequireReviewCount values in EX_LITERATURE_CONFIG Table."
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-802") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EX_LITERATURE_CONFIG', columnName: 'product_name')
			}
		}
		addColumn(tableName: "EX_LITERATURE_CONFIG") {
			column(name: "product_name", type: "clob")
		}

		grailsChange {
			change {
				List<ExecutedLiteratureConfiguration> executedConfigurationList = ExecutedLiteratureConfiguration.findAllByIsDeletedAndIsEnabledAndIsLatest(false, true, true)
				executedConfigurationList.each { ExecutedLiteratureConfiguration executedConfiguration ->
					ctx.literatureExecutionService.generateProductName(executedConfiguration)
					executedConfiguration.save(flush:true)
				}
				confirm "Successfully Updated ProdcutName in EX_LITERATURE_CONFIG Table."
			}
		}

	}

	changeSet(author: "ujjwal (generated)", id: "1613730275339-602") {

		preConditions(onFail: 'MARK_RAN') {
			columnExists(tableName: 'PREFERENCE', columnName: 'DASHBOARD_CONFIG_JSON')
		}
		grailsChange {
			change {
				try {
					sql.execute(''' UPDATE PREFERENCE SET DASHBOARD_CONFIG_JSON = null ''')
				} catch (Exception ex) {
					println(ex)
					println("##################### Error occurred while mirating dashboard config. #############")
				}
			}
		}

	}

}