databaseChangeLog = {

	changeSet(author: "ujjwal (generated)", id: "1718626578687-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'combo_flag')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "combo_flag", type: "varchar2(255 CHAR)", defaultValue :"No") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'malfunction')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "malfunction", type: "varchar2(255 CHAR)", defaultValue :"No") {
				constraints(nullable: "false")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-3") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'new_ev_link')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "new_ev_link", type: "varchar2(600 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-4") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'total_ev_link')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "total_ev_link", type: "varchar2(600 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-5") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'dme_ime')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "dme_ime", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-6") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'attributes')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "attributes", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-7") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'ratio_ror_paed_vs_others')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "ratio_ror_paed_vs_others", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-8") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'ratio_ror_geriatr_vs_others')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "ratio_ror_geriatr_vs_others", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-9") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'changes')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "changes", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-10") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'sdr_paed')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "sdr_paed", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-11") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'sdr_geratr')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "sdr_geratr", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-12") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_europe')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "tot_spont_europe", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-13") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spontnamerica')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "tot_spontnamerica", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-14") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_japan')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "tot_spont_japan", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-15") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_asia')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "tot_spont_asia", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-16") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'tot_spont_rest')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "tot_spont_rest", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-17") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'format')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "format", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-52") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'frequency')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "frequency", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-53") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'flags')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "flags", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-18") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'listedness')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "listedness", type: "number(1, 0)") {
				constraints(nullable: "true")
			}
		}
		sql("update EVDAS_ON_DEMAND_ALERT set listedness = 0;")
		addNotNullConstraint(tableName: "EVDAS_ON_DEMAND_ALERT", columnName: "listedness")
	}


	changeSet(author: "rxlogix (generated)", id: "1585066211531-6") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'imp_events')
			}
		}
		addColumn(tableName: "EVDAS_ON_DEMAND_ALERT") {
			column(name: "imp_events", type: "VARCHAR(255 CHAR)"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "rxlogix (generated)", id: "1718626578687-19") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'EOD_ALERT_IMP_EVENT_LIST')
			}
		}
		createTable(tableName: "EOD_ALERT_IMP_EVENT_LIST") {
			column(name: "EVDAS_ON_DEMAND_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "EVDAS_ON_DEMAND_IMP_EVENTS", type: "VARCHAR(255 CHAR)")

			column(name: "EV_IMP_EVENT_LIST_IDX", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-21") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_mh')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "prr_mh", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-22") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_str05')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "prr_str05", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-23") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'EVDAS_ON_DEMAND_ALERT', columnName: 'prr_str95')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "prr_str95", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-24") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_mh')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "ror_mh", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-25") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_str05')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "ror_str05", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-26") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_str95')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "ror_str95", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-27") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'prr_str')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "prr_str", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-28") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'pec_imp_high')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "pec_imp_high", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-29") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'pec_imp_low')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "pec_imp_low", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-30") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'format')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "format", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-31") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ror_str')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "ror_str", type: "varchar2(255 CHAR)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-32") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cumm_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "cumm_count", type: "number(10, 0)"){
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-36") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cumm_interacting_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "cumm_interacting_count", type: "number(10, 0)"){
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-37") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cumm_pediatric_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "cumm_pediatric_count", type: "number(10, 0)"){
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-38") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "new_count", type: "number(10, 0)"){
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-39") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_interacting_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "new_interacting_count", type: "number(10, 0)"){
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-40") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_pediatric_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "new_pediatric_count", type: "number(10, 0)"){
			}
		}
	}

	changeSet(author: "amit (generated)", id: "1718626578687-41") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cum_geriatric_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "cum_geriatric_count", type: "number(10, 0)") {
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-42") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'cum_non_serious')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "cum_non_serious", type: "number(10, 0)") {
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-43") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_geriatric_count')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "new_geriatric_count", type: "number(10, 0)") {
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-44") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'new_non_serious')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "new_non_serious", type: "number(10, 0)") {
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-45") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb05str')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "eb05str", type: "clob"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-46") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'eb95str')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "eb95str", type: "clob"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-47") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'ebgm_str')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "ebgm_str", type: "clob"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-48") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'imp_events')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "imp_events", type: "VARCHAR(255 CHAR)"){
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-50") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'AOD_ALERT_IMP_EVENT_LIST')
			}
		}
		createTable(tableName: "AOD_ALERT_IMP_EVENT_LIST") {
			column(name: "AGG_ON_DEMAND_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "AGA_ON_DEMAND_IMP_EVENTS", type: "VARCHAR(255 CHAR)")

			column(name: "AGA_IMP_EVENT_LIST_IDX", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578688-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'global_identity_id')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "global_identity_id", type: "NUMBER(19, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578688-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'AGG_ON_DEMAND_ALERT', columnName: 'global_identity_id')
			}
		}
		addColumn(tableName: "AGG_ON_DEMAND_ALERT") {
			column(name: "global_identity_id", type: "NUMBER(19, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578692-52") {
		createTable(tableName: "SINGLE_ALERT_OD_PT") {
			column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "PT", type: "CLOB")

			column(name: "pt_list_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1828626578692-51") {
		createTable(tableName: "SINGLE_ALERT_OD_SUSP_PROD") {
			column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "PRODUCT_NAME", type: "CLOB")

			column(name: "suspect_product_list_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1828626578692-53") {
		createTable(tableName: "SINGLE_ALERT_OD_CON_COMIT") {
			column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}
			column(name: "CON_COMIT", type: "CLOB")
			column(name: "con_comit_list_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578687-59") {
		createTable(tableName: "SINGLE_ALERT_OD_MED_ERR") {
			column(name: "SINGLE_ALERT_OD_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}
			column(name: "MED_ERROR", type: "CLOB")

			column(name: "med_error_pt_list_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "nitesh (generated)", id: "1608824568998-1") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'INDICATION')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "INDICATION", type: "varchar2(16000 char)") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-2") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'EVENT_OUTCOME')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "EVENT_OUTCOME", type: "varchar2(2000 char)") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-3") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'CAUSE_OF_DEATH')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "CAUSE_OF_DEATH", type: "clob") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-4") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'SERIOUS_UNLISTED_RELATED')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "SERIOUS_UNLISTED_RELATED", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-5") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PATIENT_MED_HIST')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "PATIENT_MED_HIST", type: "clob") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-6") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PATIENT_HIST_DRUGS')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "PATIENT_HIST_DRUGS", type: "clob") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-7") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'BATCH_LOT_NO')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "BATCH_LOT_NO", type: "clob") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-8") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'TIME_TO_ONSET')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "TIME_TO_ONSET", type: "number(19,0)") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-9") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'CASE_CLASSIFICATION')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "CASE_CLASSIFICATION", type: "clob") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-10") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'INITIAL_FU')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "INITIAL_FU", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-11") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PROTOCOL_NO')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "PROTOCOL_NO", type: "varchar2(500 char)") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-12") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'IS_SUSAR')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "IS_SUSAR", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-13") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'THERAPY_DATES')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "THERAPY_DATES", type: "clob") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "nitesh (generated)", id: "1608824568998-14") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'DOSE_DETAILS')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "DOSE_DETAILS", type: "clob") {
				constraints(nullable: "true")
			}
		}
	}
	changeSet(author: "ujjwal (generated)", id: "1608824568998-15") {

		preConditions(onFail: 'MARK_RAN') {
			not {
				columnExists(tableName: 'SINGLE_ON_DEMAND_ALERT', columnName: 'PRE_ANDA')
			}
		}
		addColumn(tableName: "SINGLE_ON_DEMAND_ALERT") {
			column(name: "PRE_ANDA", type: "varchar2(255 char)") {
				constraints(nullable: "true")
			}
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578690-1") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_INDICATION_LIST')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_INDICATION_LIST") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_INDICATION", type: "varchar(500)")

			column(name: "indication_list_idx", type: "NUMBER(10, 0)")
		}
	}
	changeSet(author: "ujjwal (generated)", id: "1718626578690-2") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_CAUSE_OF_DEATH')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_CAUSE_OF_DEATH") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_CAUSE_OF_DEATH", type: "varchar(1500)")

			column(name: "cause_of_death_list_idx", type: "NUMBER(10, 0)")
		}
	}
	changeSet(author: "ujjwal (generated)", id: "1718626578690-3") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_PAT_MED_HIST')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_PAT_MED_HIST") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_PAT_MED_HIST", type: "varchar(500)")

			column(name: "patient_med_hist_list_idx", type: "NUMBER(10, 0)")
		}
	}
	changeSet(author: "ujjwal (generated)", id: "1718626578690-4") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_PAT_HIST_DRUGS')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_PAT_HIST_DRUGS") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_PAT_HIST_DRUGS", type: "varchar(500)")

			column(name: "patient_hist_drugs_list_idx", type: "NUMBER(10, 0)")
		}
	}
	changeSet(author: "ujjwal (generated)", id: "1718626578690-5") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_BATCH_LOT_NO')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_BATCH_LOT_NO") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_BATCH_LOT_NO", type: "varchar(500)")

			column(name: "batch_lot_no_list_idx", type: "NUMBER(10, 0)")
		}
	}
	changeSet(author: "ujjwal (generated)", id: "1718626578690-6") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_CASE_CLASSIFI')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_CASE_CLASSIFI") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_CASE_CLASSIFICATION", type: "varchar(3000)")

			column(name: "case_classification_list_idx", type: "NUMBER(10, 0)")
		}
	}
	changeSet(author: "ujjwal (generated)", id: "1718626578690-7") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_THERAPY_DATES')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_THERAPY_DATES") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_THERAPY_DATES", type: "varchar(500)")

			column(name: "therapy_dates_list_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578690-8") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_DOSE_DETAILS')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_DOSE_DETAILS") {
			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SCA_DOSE_DETAILS", type: "varchar(15000)")

			column(name: "dose_details_list_idx", type: "NUMBER(10, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578690-11") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'SINGLE_DEMAND_ALERT_TAGS')
			}
		}
		createTable(tableName: "SINGLE_DEMAND_ALERT_TAGS") {
			column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "SINGLE_ALERT_ID", type: "NUMBER(19, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578690-12") {
		preConditions(onFail: 'MARK_RAN') {
			not {
				tableExists(tableName: 'AGG_DEMAND_ALERT_TAGS')
			}
		}
		createTable(tableName: "AGG_DEMAND_ALERT_TAGS") {
			column(name: "PVS_ALERT_TAG_ID", type: "NUMBER(19, 0)") {
				constraints(nullable: "false")
			}

			column(name: "AGG_ALERT_ID", type: "NUMBER(19, 0)")
		}
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-231") {
		sql("update AGG_ON_DEMAND_ALERT set new_count = new_spon_count where new_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-241") {
		sql("update AGG_ON_DEMAND_ALERT set cumm_count = cum_spon_count where cumm_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-25") {
		sql("update AGG_ON_DEMAND_ALERT set new_pediatric_count = 0 where new_pediatric_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-26") {
		sql("update AGG_ON_DEMAND_ALERT set cumm_pediatric_count = 0 where cumm_pediatric_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-37") {
		sql("update AGG_ON_DEMAND_ALERT set new_interacting_count = 0 where new_interacting_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-28") {
		sql("update AGG_ON_DEMAND_ALERT set cumm_interacting_count = 0 where cumm_interacting_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-29") {
		sql("update AGG_ON_DEMAND_ALERT set new_geriatric_count = 0 where new_geriatric_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-30") {
		sql("update AGG_ON_DEMAND_ALERT set cum_geriatric_count = 0 where cum_geriatric_count is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-31") {
		sql("update AGG_ON_DEMAND_ALERT set new_non_serious = 0 where new_non_serious is null;")
	}

	changeSet(author: "ujjwal (generated)", id: "1718626578691-32") {
		sql("update AGG_ON_DEMAND_ALERT set cum_non_serious = 0 where cum_non_serious is null;")
	}
}