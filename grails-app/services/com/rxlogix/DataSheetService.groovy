package com.rxlogix

import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.sql.GroovyResultSetExtension
import groovy.sql.OutParameter
import groovy.sql.Sql
import oracle.jdbc.OracleTypes
import org.apache.http.util.TextUtils

class DataSheetService {
    def dataSource_pva
    def sqlGenerationService
    def signalDataSourceService

    Map getDataSheets(String searchedKey, String enabledSheet, int offset, int max, List productsList=[], Boolean isProductGroup = false, Boolean isMultiIngredient=false) {
        List dataSheets = []
        try {
            if (productsList && !productsList?.isEmpty()) {
                productsList?.each{
                    dataSheets += fetchDataSheets(it as String, enabledSheet,isProductGroup, isMultiIngredient)
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        }
        return [dataSheetList: dataSheets?.unique(), totalCount: dataSheets?.size() ?: 0]
    }

    Map getAllActiveDatasheets(String searchedKey="", String enabledSheet, int offset=0, int max=30){
        Sql sql = null
        List dataSheets = []
        String dataSheetClause = ""
        String whereClause = " where "
        if(searchedKey){
            dataSheetClause += " lower(display_col) like '%${searchedKey?.toLowerCase()?.trim()}%' "
        }
        if (enabledSheet == Constants.DatasheetOptions.CORE_SHEET) {
            if (dataSheetClause) {
                dataSheetClause += " and core_sheet = 1 "
            } else {
                dataSheetClause += " core_sheet = 1 "
            }
        }
        dataSheetClause = dataSheetClause ? whereClause + dataSheetClause : ""
        String dataSheetSql =""" select * from vw_pvs_ds_pf_list ${dataSheetClause} order by UPPER(display_col) asc, display_col desc """
        try {
            sql = new Sql(dataSource_pva)
            sql.eachRow(dataSheetSql,  []){ row  ->
                dataSheets?.add([id: row.datasheet_id + Constants.DatasheetOptions.ID_SEPARATOR + row.Base_Id, name: row.datasheet_name?.toString(), dispName: row.display_col?.toString()])
            }
            dataSheets.removeAll([null])
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
            return [dataSheetList: dataSheets, totalCount: dataSheets?.size() ?: 0]
        }
    }

    List getAllActiveDatasheetsList(String searchedKey="", String enabledSheet, int offset=0, int max=30){
        Sql sql = null
        List dataSheets = []
        String dataSheetClause = ""
        String whereClause = " where "
        if(searchedKey){
            dataSheetClause += " lower(display_col) like '%${searchedKey?.toLowerCase()?.trim()}%' "
        }
        if (enabledSheet == Constants.DatasheetOptions.CORE_SHEET) {
            if (dataSheetClause) {
                dataSheetClause += " and core_sheet = 1 "
            } else {
                dataSheetClause += " core_sheet = 1 "
            }
        }
        dataSheetClause = dataSheetClause ? whereClause + dataSheetClause : ""
        String dataSheetSql =""" select * from vw_pvs_ds_pf_list ${dataSheetClause} order by UPPER(display_col) asc, display_col desc """
        try {
            sql = new Sql(dataSource_pva)
            sql.eachRow(dataSheetSql,  []){ row  ->
                dataSheets?.add([id: row.datasheet_id + Constants.DatasheetOptions.ID_SEPARATOR + row.Base_Id, name: row.datasheet_name?.toString(), dispName: row.display_col?.toString()])
            }
            dataSheets.removeAll([null])
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
            return dataSheets
        }
    }

    private String initializeGTTForDatsheets(String productSelection, Boolean isProductGroup= false){
        sqlGenerationService.initializeGTTForDatsheets(productSelection, isProductGroup)
    }

     List fetchDataSheets(String productDictionarySelection, String enabledSheet,Boolean isProductGroup = false, Boolean isMultiIngredient=false) throws Exception{
        final Sql sql
        List<Map> dataSheets = []
        def sheet = 0
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String insertStatement = "Begin " +
                    "execute immediate ('delete from gtt_filter_key_values');"
            insertStatement += initializeGTTForDatsheets(productDictionarySelection, isProductGroup)
            insertStatement += " INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_MULTI_INGREDIENT','${isMultiIngredient && !isProductGroup?1:0}'); "
            insertStatement += " END;"
            if (insertStatement) {
                sql.execute(insertStatement)
            }
            if (enabledSheet && enabledSheet == Constants.DatasheetOptions.CORE_SHEET) {
                sheet = 1
            }
            OutParameter CURSOR_PARAMETER = new OutParameter() {
                public int getType() {
                    return OracleTypes.CURSOR;
                }
            };
            log.info("calling P_PVS_DS_DROP_DOWN(?,?)")
            String procedure = "call P_PVS_DS_DROP_DOWN(?,?)"
            sql.call("{${procedure}}", [sheet,CURSOR_PARAMETER]){ result ->
                result.eachRow() { GroovyResultSetExtension row ->
                    dataSheets?.add([id: row.datasheet_id + Constants.DatasheetOptions.ID_SEPARATOR + row.Base_Id, name: row.datasheet_name?.toString(), dispName: row.display_col?.toString()])
                }
            }

        } catch (Throwable ex) {
            log.error(ex.getMessage())
        } finally {
            sql?.close()
        }
        dataSheets.removeAll([null])
        dataSheets?:[]
    }

    List formatDatasheetMap (def config) {
        Map dataSheetMap = [:]
        List formatedList = []
        if(config.selectedDataSheet){
            dataSheetMap = JSON.parse(config.selectedDataSheet)
        }
        dataSheetMap?.each { k, v ->
            formatedList.add([id: v + Constants.DatasheetOptions.SEPARATOR + k, text : v])
        }
        return formatedList
    }

    List formatActiveDatasheetMap(Map dataSheetsMap) {
        List formatedList = []
        dataSheetsMap?.each { k, v ->
            formatedList.add([id: v + Constants.DatasheetOptions.SEPARATOR + k, text : v])
        }
        return formatedList
    }
}
