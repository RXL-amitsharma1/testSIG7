package com.rxlogix.util


import com.rxlogix.Constants
import com.rxlogix.dto.AlertDTO
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders

import java.text.SimpleDateFormat

class SignalQueryHelper {

    /* used instead of User.getWorkflowGroup() cause performance optimization */
    static SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM_3)
    static workflow_group_id_sql = { Long userId ->
        """
        SELECT 
            g.id FROM GROUPS g
        WHERE
            g.group_type = 'WORKFLOW_GROUP'
            AND g.id IN (
                SELECT
                    ug_s.group_id
                FROM
                    USER_GROUP_S ug_s 
                WHERE
                    ug_s.user_id = ${userId}
                UNION
                SELECT
                    ugm.group_id 
                FROM
                    USER_GROUP_MAPPING ugm 
                WHERE
                    ugm.user_id = ${userId}
            )
                LIMIT 1
        """
    }

    static single_case_and_single_archive_alert_list_sql = { Long signalId, String blindedFields, String redactedFields, String availableFields, int isAllFieldsRedacted ->
        """
        WITH sca_ids AS (
            SELECT single_alert_id AS id FROM validated_single_alerts WHERE validated_signal_id = ${signalId}
        )
        SELECT * FROM (SELECT
            sca.id as id,
            sca.alert_configuration_id as alertConfigId,
            sca.name as alertName,
            CASE 
                WHEN 'masterCaseNum' in (${redactedFields}) THEN '${Constants.BlindingStatus.REDACTED}' 
                WHEN 'masterCaseNum' in (${blindedFields}) THEN 
                    CASE 
                        WHEN sca.study_blinding_Status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE sca.case_number END
                WHEN 'masterCaseNum' in (${availableFields}) THEN sca.case_number
                WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}'  
                ELSE sca.case_number 
            END as caseNumber,           
            sca.case_id as caseId,
            CASE 
                WHEN ('productProductId' in (${redactedFields}) OR 'productProductName' in (${redactedFields})) THEN '${Constants.BlindingStatus.REDACTED}' 
                WHEN ('productProductId' in (${blindedFields}) OR 'productProductName' in (${blindedFields})) THEN 
                    CASE 
                        WHEN sca.study_blinding_Status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE sca.product_name END
                
                WHEN ('productProductId' in (${availableFields}) OR 'productProductName' in (${availableFields})) THEN sca.product_name 
                WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}' 
                ELSE sca.product_name 
            END as productName,   
            sca.product_family as productFamily,
            CASE 
                WHEN 'masterPrefTermSurAll' in (${redactedFields}) THEN '${Constants.BlindingStatus.REDACTED}' 
                WHEN 'masterPrefTermSurAll' in (${blindedFields}) THEN 
                    CASE 
                        WHEN sca.study_blinding_Status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE substring(sca.master_pref_term_all from 1 for length(sca.master_pref_term_all)) END
                WHEN 'masterPrefTermSurAll' in (${availableFields}) THEN substring(sca.master_pref_term_all from 1 for length(sca.master_pref_term_all)) 
                WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}' 
                ELSE sca.master_pref_term_all
            END as masterPrefTermAll, 
            sca.case_version as caseVersion,
            sca.follow_up_number as followUpNumber,
            sca.exec_config_id as execConfigId,
            d.display_name as disposition,
            sca.priority_id as priorityId,
            sca.case_number as actualCaseNumber,
            sca.product_name as actualProductName,
            conf.is_standalone as isStandalone,
            0 as isArchived
        FROM
            SINGLE_CASE_ALERT sca
        INNER JOIN
            VALIDATED_SINGLE_ALERTS vsa ON sca.id = vsa.single_alert_id
        INNER JOIN
            VALIDATED_SIGNAL vs ON vsa.validated_signal_id = vs.id
        INNER JOIN
            DISPOSITION d ON sca.disposition_id = d.id
        LEFT OUTER JOIN
            RCONFIG conf ON sca.alert_configuration_id = conf.id
        WHERE
            vs.id = ${signalId}
            AND (sca.id IN (SELECT si.id FROM sca_ids si))
        ORDER BY
            sca.id DESC) AS single_case_alerts
            
        UNION ALL
        
        SELECT * FROM (SELECT
            sca.id as id,
            sca.alert_configuration_id as alertConfigId,
            sca.name as alertName,
            CASE 
                WHEN 'masterCaseNum' in (${redactedFields}) THEN '${Constants.BlindingStatus.REDACTED}' 
                WHEN 'masterCaseNum' in (${blindedFields}) THEN 
                    CASE 
                        WHEN sca.study_blinding_Status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE sca.case_number END
                WHEN 'masterCaseNum' in (${availableFields}) THEN sca.case_number 
                WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}' 
                ELSE sca.case_number 
            END as caseNumber,           
            sca.case_id as caseId,
            CASE 
                WHEN ('productProductId' in (${redactedFields}) OR 'productProductName' in (${redactedFields})) THEN '${Constants.BlindingStatus.REDACTED}' 
                WHEN ('productProductId' in (${blindedFields}) OR 'productProductName' in (${blindedFields})) THEN 
                    CASE 
                        WHEN sca.study_blinding_Status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE sca.product_name END
                
                WHEN ('productProductId' in (${availableFields}) OR 'productProductName' in (${availableFields})) THEN sca.product_name 
                WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}' 
                ELSE sca.product_name 
            END as productName,  
            sca.product_family as productFamily,
            CASE
                WHEN 'masterPrefTermSurAll' in (${redactedFields}) THEN '${Constants.BlindingStatus.REDACTED}' 
                WHEN 'masterPrefTermSurAll' in (${blindedFields}) THEN 
                    CASE 
                        WHEN sca.study_blinding_Status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE substring(sca.master_pref_term_all from 1 for length(sca.master_pref_term_all)) END
                WHEN 'masterPrefTermSurAll' in (${availableFields}) THEN substring(sca.master_pref_term_all from 1 for length(sca.master_pref_term_all)) 
                WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}'  
                ELSE sca.master_pref_term_all
            END as masterPrefTermAll,  
            sca.case_version as caseVersion,
            sca.follow_up_number as followUpNumber,
            sca.exec_config_id as execConfigId,
            d.display_name as disposition,
            sca.priority_id as priorityId,
            sca.case_number as actualCaseNumber,
            sca.product_name as actualProductName,
            false as isStandalone,
            1 as isArchived
        FROM
            ARCHIVED_SINGLE_CASE_ALERT sca
        INNER JOIN
            VALIDATED_ARCHIVED_SCA vsa ON sca.id = vsa.ARCHIVED_SCA_ID
        INNER JOIN
            VALIDATED_SIGNAL vs ON vsa.validated_signal_id = vs.id
        INNER JOIN
            DISPOSITION d ON sca.disposition_id = d.id
        WHERE
            vs.id = ${signalId}
        ORDER BY
            sca.id DESC) AS archived_single_case_alerts
        """
    }

    static agg_evdas_combined_alert_list_sql = { signalId, Long userId, dataSourcePva, String dataSourceEvdas, String searchTerm ->
        def integerSearchTermFragment = searchTerm.isInteger() ? """
            OR newCount1 = ${searchTerm}
            OR cumCount1 = ${searchTerm}
            OR newSeriousCount = ${searchTerm}
            OR cumSeriousCount = ${searchTerm}
        """ : """"""

        def doubleSearchTermFragment = searchTerm.isDouble() ? """
            OR prrValue = ${searchTerm}
            OR rorValue = ${searchTerm}
            OR ebgm = ${searchTerm}
            OR eb05 = ${searchTerm}
            OR eb95 = ${searchTerm}
        """ : """"""

        def searchTermFragment = searchTerm ? """
            AND LOWER(alertName) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR LOWER(productName) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR LOWER(soc) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR LOWER(preferredTerm) LIKE '%' || LOWER('${searchTerm}') || '%'
            OR (
                LOWER(dataSource) LIKE '%' || LOWER('${searchTerm}') || '%'
            )
            OR (
                LOWER(disposition) LIKE '%' || LOWER('${searchTerm}') || '%'
            )
            ${integerSearchTermFragment}
            ${doubleSearchTermFragment}
        """ : """"""

        def aggAlertsColumns = """
            aa.id as id,
            aa.alert_configuration_id as alertConfigId,
            aa.name as alertName,
            aa.product_id as productId,
            aa.product_name as productName,
            aa.soc as soc,
            aa.pt as preferredTerm,
            aa.pt_code as ptCode,
            aa.exec_configuration_id as execConfigId,
            conf.selected_data_source as dataSourceValue,

            CASE 
                WHEN conf.selected_data_source LIKE '%pva%' THEN '${dataSourcePva}' 
                ELSE 
                    CASE 
                        WHEN POSITION('pva,' IN conf.selected_data_source) = 0 THEN 
                            REPLACE(UPPER(conf.selected_data_source), ',PVA', ',${dataSourcePva}')
                    ELSE 
                        REPLACE(UPPER(conf.selected_data_source), 'PVA,', '${dataSourcePva},')
                    END
            END AS dataSource,

            d.display_name as disposition,
    
            CASE 
                WHEN ex_conf.selected_data_source like '%pva%' THEN 
                    CASE WHEN aa.new_count != -1 THEN aa.new_count ELSE null END
                WHEN ex_conf.selected_data_source like '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'newCountFaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source like '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'newCountVaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source like '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'newCountVigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source like '%jader%' THEN
                    CAST((CAST(aa.jader_columns AS JSONB)->>'newCountJader') AS NUMERIC)
                ELSE CASE WHEN aa.new_count != -1 THEN aa.new_count ELSE null END
            END as newCount1,
            
            CASE
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.cumm_count != -1 THEN aa.cumm_count ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'cummCountFaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'cummCountVaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'cummCountVigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    /* jader_columns contains cumCountJader instead of cummCountJader */
                    CAST((CAST(aa.jader_columns AS JSONB)->>'cumCountJader') AS NUMERIC)
                ELSE CASE WHEN aa.cumm_count != -1 THEN aa.cumm_count ELSE NULL END
            END AS cumCount1,
            
            
            
             CASE 
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.new_serious_count != -1 THEN aa.new_serious_count ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'newSeriousCountFaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'newSeriousCountVaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'newSeriousCountVigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    NULL
                ELSE CASE WHEN aa.new_serious_count != -1 THEN aa.new_serious_count ELSE NULL END
            END AS newSeriousCount,

            CASE 
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.cum_serious_count != -1 THEN aa.cum_serious_count ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'cumSeriousCountFaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'cumSeriousCountVaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'cumSeriousCountVigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    NULL
                ELSE CASE WHEN aa.cum_serious_count != -1 THEN aa.cum_serious_count ELSE NULL END
            END AS cumSeriousCount,

            CASE 
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.prr_value != -1.0 THEN aa.prr_value ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'prrValueFaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'prrValueVaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'prrValueVigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    CAST((CAST(aa.jader_columns AS JSONB)->>'prrValueJader') AS NUMERIC)
                ELSE CASE WHEN aa.prr_value != -1.0 THEN aa.prr_value ELSE NULL END
            END AS prrValue,
            
            CASE 
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.ror_value != -1.0 THEN aa.ror_value ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'rorValueFaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'rorValueVaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'rorValueVigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    CAST((CAST(aa.jader_columns AS JSONB)->>'rorValueJader') AS NUMERIC)
                ELSE CASE WHEN aa.ror_value != -1.0 THEN aa.ror_value ELSE NULL END
            END AS rorValue,
            
            CASE 
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.ebgm != -1.0 THEN aa.ebgm ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'ebgmFaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'ebgmVaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'ebgmVigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    CAST((CAST(aa.jader_columns AS JSONB)->>'ebgmJader') AS NUMERIC)
                ELSE CASE WHEN aa.ebgm != -1.0 THEN aa.ebgm ELSE NULL END
            END AS ebgm,
            
            CASE
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.eb05 != -1.0 THEN aa.eb05 ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'eb05Faers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'eb05Vaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'eb05Vigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    CAST((CAST(aa.jader_columns AS JSONB)->>'eb05Jader') AS NUMERIC)
                ELSE CASE WHEN aa.eb05 != -1.0 THEN aa.eb05 ELSE NULL END
            END AS eb05,
            
            CASE 
                WHEN ex_conf.selected_data_source LIKE '%pva%' THEN 
                    CASE WHEN aa.eb95 != -1.0 THEN aa.eb95 ELSE NULL END
                WHEN ex_conf.selected_data_source LIKE '%faers%' THEN
                    CAST((CAST(aa.faers_columns AS JSONB)->>'eb95Faers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vaers%' THEN
                    CAST((CAST(aa.vaers_columns AS JSONB)->>'eb95Vaers') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%vigibase%' THEN
                    CAST((CAST(aa.vigibase_columns AS JSONB)->>'eb95Vigibase') AS NUMERIC)
                WHEN ex_conf.selected_data_source LIKE '%jader%' THEN
                    CAST((CAST(aa.jader_columns AS JSONB)->>'eb95Jader') AS NUMERIC)
                ELSE CASE WHEN aa.eb95 != -1.0 THEN aa.eb95 ELSE NULL END
            END AS eb95,

1 AS isAggAlert

        """

        def evdasAlertsColumns = """
            ea.id as id,
            ea.alert_configuration_id as alertConfigId,
            ea.name as alertName,
            ea.substance_id as productId,
            ea.substance as productName,
            ea.soc as soc,
            ea.pt as preferredTerm,
            ea.pt_code as ptCode,
            ea.exec_configuration_id as execConfigId,
            '${dataSourceEvdas}' as dataSourceValue,
            '${dataSourceEvdas.toUpperCase()}' as dataSource,
            d.display_name as disposition,
    
            CAST(ea.new_ev AS NUMERIC) as newCount1,
            CAST(ea.total_ev AS NUMERIC) as cumCount1,
            CAST(ea.new_serious AS NUMERIC) as newSeriousCount,
            CAST(ea.total_serious AS NUMERIC) as cumSeriousCount,
            CAST(null AS NUMERIC) as prrValue,
            CAST(ea.ror_value AS NUMERIC) as rorValue,
            CAST(null AS NUMERIC) as ebgm,
            CAST(null AS NUMERIC) as eb05,
            CAST(null AS NUMERIC) as eb95,
            
            0 as isAggAlert
        """

        """
        WITH agg_ids AS (
            SELECT agg_alert_id as id FROM VALIDATED_AGG_ALERTS WHERE validated_signal_id = ${signalId}
        ),
        evdas_ids AS (
            SELECT evdas_alert_id as id FROM VALIDATED_EVDAS_ALERTS WHERE validated_signal_id = ${signalId}
        ),
        workflow_group AS (
            ${SignalQueryHelper.workflow_group_id_sql(userId)}
        )

        /* AGG ALERTS */
        SELECT * FROM (
        
            SELECT * FROM (SELECT
                ${aggAlertsColumns},
                0 as isArchived
                
            FROM
                AGG_ALERT aa
                INNER JOIN
                    VALIDATED_AGG_ALERTS vaa ON aa.id = vaa.agg_alert_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vaa.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON aa.disposition_id = d.id
                LEFT OUTER JOIN
                    RCONFIG conf ON aa.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_RCONFIG ex_conf ON aa.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = false
                AND ex_conf.is_enabled = true
                AND ex_conf.adhoc_run = false
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
                AND (aa.id IN (SELECT ai.id FROM agg_ids ai))

            ORDER BY aa.id DESC) AS agg_alerts
                
            UNION ALL    
                
            SELECT * FROM (SELECT
                ${aggAlertsColumns},
                1 as isArchived
                
            FROM
                ARCHIVED_AGG_ALERT aa
                INNER JOIN
                    VALIDATED_ARCHIVED_ACA vaa ON aa.id = vaa.archived_aca_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vaa.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON aa.disposition_id = d.id
                LEFT OUTER JOIN
                    RCONFIG conf ON aa.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_RCONFIG ex_conf ON aa.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = false
                AND ex_conf.is_enabled = true
                AND ex_conf.adhoc_run = false
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
            ORDER BY aa.id DESC) AS archived_agg_alerts
        ) AS combined_alerts
        WHERE
            1=1
            ${searchTermFragment}
                
        UNION ALL 
        
        /* EVDAS ALERTS */
        SELECT * FROM (
        
            SELECT * FROM (SELECT
                ${evdasAlertsColumns},
                0 as isArchived
                
            FROM
                EVDAS_ALERT ea
                INNER JOIN
                    VALIDATED_EVDAS_ALERTS vea ON ea.id = vea.evdas_alert_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vea.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON ea.disposition_id = d.id
                LEFT OUTER JOIN
                    EVDAS_CONFIG conf ON ea.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_EVDAS_CONFIG ex_conf ON ea.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = false
                AND ex_conf.is_enabled = true
                AND ex_conf.adhoc_run = false
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
                AND (ea.id IN (SELECT ei.id FROM evdas_ids ei))
            ORDER BY ea.id DESC) AS evdas_alerts
            
            UNION ALL
            
            SELECT * FROM (SELECT
                ${evdasAlertsColumns},
                1 as isArchived
                
            FROM
                ARCHIVED_EVDAS_ALERT ea
                INNER JOIN
                    VALIDATED_ARCH_EVDAS_ALERTS vea ON ea.id = vea.archived_evdas_alert_id
                INNER JOIN
                    VALIDATED_SIGNAL vs ON vea.validated_signal_id = vs.id
                INNER JOIN
                    DISPOSITION d ON ea.disposition_id = d.id
                LEFT OUTER JOIN
                    EVDAS_CONFIG conf ON ea.alert_configuration_id = conf.id
                LEFT OUTER JOIN
                    EX_EVDAS_CONFIG ex_conf ON ea.exec_configuration_id = ex_conf.id
            WHERE
                vs.id = ${signalId}
                AND ex_conf.is_deleted = false
                AND ex_conf.is_enabled = true
                AND ex_conf.adhoc_run = false
                AND ex_conf.workflow_group = (SELECT wg.id FROM workflow_group wg)
            ORDER BY ea.id DESC) AS archived_evdas_alerts
        ) AS combined_evdas_alerts    
        WHERE
            1=1
            ${searchTermFragment}
        """
    }

    static event_prod_info_sql = { cl, eventCode, eventCodeVal ->
        """
            SELECT
            COUNT (DISTINCT b.case_id) COUNT, a.case_num CASE_NUMBER, a.version_num VERSION, a.${eventCodeVal} EVENT_VAL
               FROM
            (SELECT caei.mdr_ae_pt_code, caei.mdr_ae_pt, caei.mdr_ae_llt_code,
                caei.mdr_ae_llt, caei.mdr_ae_hlt, caei.mdr_ae_hlt_code,
                caei.mdr_ae_hlgt_code, caei.mdr_ae_hlgt, caei.mdr_ae_soc_code,
                caei.mdr_ae_soc, ci.case_num, ci.case_id, ci.version_num
            FROM c_ae_identification caei JOIN c_identification_fu cifu
                ON caei.case_id = cifu.case_id
                AND caei.version_num = cifu.version_num
                AND caei.tenant_id = cifu.tenant_id
                AND caei.ae_rec_num = cifu.prim_ae_rec_num
                JOIN c_identification ci
                ON cifu.case_id = ci.case_id
                AND cifu.version_num = ci.version_num
                AND cifu.tenant_id = ci.tenant_id
                AND flag_primary_ae = 1
                AND mdr_ae_pt IS NOT NULL
                AND (ci.case_num, ci.version_num) IN (${cl})
            ) a, (
            SELECT caei.mdr_ae_pt_code, caei.mdr_ae_pt, caei.mdr_ae_llt_code,
                caei.mdr_ae_llt, caei.mdr_ae_hlt, caei.mdr_ae_hlt_code,
                caei.mdr_ae_hlgt_code, caei.mdr_ae_hlgt, caei.mdr_ae_soc_code,
                caei.mdr_ae_soc, ci.case_num, ci.case_id, ci.version_num
            FROM c_ae_identification caei JOIN c_identification ci
            ON caei.case_id = ci.case_id
            AND caei.version_num = ci.version_num
            AND caei.tenant_id = ci.tenant_id
            AND mdr_ae_pt IS NOT NULL
            AND (ci.case_num, ci.version_num) IN (${cl})) b
            WHERE a.${eventCode} = b.${eventCode}
            GROUP BY a.case_id, a.case_num, a.${eventCodeVal}, a.version_num       
        """
    }

    static faers_date_range = { "SELECT DECODE(SUBSTR(ETL_VALUE, 6, 7), 'Q1', '31-MAR-', 'Q2', '30-JUN-', 'Q3', '30-SEP-', 'Q4', '31-DEC-', '') " +
            "|| SUBSTR(ETL_VALUE, 1, 4) AS FAERS_DATE FROM PVR_ETL_CONSTANTS WHERE ETL_KEY = 'FAERS_PROCESSED_QUARTER'" }
    static jader_date_range = { "SELECT DECODE(SUBSTR(ETL_VALUE, 6, 7), 'Q4', '31-MAR-', 'Q1', '30-JUN-', 'Q2', '30-SEP-', 'Q3', '31-DEC-', '') " +
            "|| SUBSTR(ETL_VALUE, 1, 4) AS JADER_DATE FROM PVR_ETL_CONSTANTS WHERE ETL_KEY = 'JADER_PROCESSED_QUARTER'" }
    //Updated Vaers Date Range for Integrated Alert PVS-67898
    static vaers_date_range = {
        "SELECT " +
                "    TO_CHAR(TO_DATE(" +
                "        (SELECT ETL_VALUE FROM pvr_etl_constants WHERE ETL_KEY = 'VAERS_LATEST_PROCESSED_START_DATE'), " +
                "        'DD-MM-YYYY'), 'DD-MM-YYYY') " +
                "    || '/' || " +
                "    TO_CHAR(TO_DATE(" +
                "        (SELECT ETL_VALUE FROM pvr_etl_constants WHERE ETL_KEY = 'VAERS_LATEST_PROCESSED_END_DATE'), " +
                "        'DD-MM-YYYY'), 'DD-MM-YYYY') " +
                "AS latest_uploaded_date_range " +
                "FROM dual"
    }
    static vigibase_date_range = {"SELECT DECODE(SUBSTR(ETL_VALUE, 6, 7), 'Q1', '31-MAR-', 'Q2', '30-JUN-', 'Q3', '30-SEP-', 'Q4', '31-DEC-', '') " +
            "|| SUBSTR(ETL_VALUE, 1, 4) AS VIGIBASE_DATE FROM PVR_ETL_CONSTANTS WHERE ETL_KEY = 'VIGIBASE_PROCESSED_QUARTER'"}

    static vigibase_date_range_display = { "select ETL_VALUE AS vigibase_date  from pvr_etl_constants where etl_key = 'VIGIBASE_LATEST_PROCESSED_DATE'" }
    static jader_date_range_display = { "select ETL_VALUE AS jader_date  from pvr_etl_constants where etl_key = 'JADER_LATEST_PROCESSED_DATE'" }
    static statification_enabled_ebgm = {"select count(1) from pvs_constants_ebgm where pvs_key like 'STR_%' and pvs_value=1"}
    static statification_enabled = {dataSource -> "select count(1) from VW_ADMIN_APP_CONFIG where CONFIG_KEY like 'STR_%' and JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = '${dataSource}'"}

    static mining_variable_statification_enabled_ebgm = { keyId,dataSource -> "select count(1) from VW_ADMIN_APP_CONFIG where CONFIG_KEY='BS_${keyId}' and APPLICATION_NAME = '${dataSource}' and JSON_VALUE(CONFIG_VALUE,'\$.EBGM_STRATIFICATION') is not null"}
    static statification_enabled_data_Mining = {keyId,dataSource -> "select count(1) from VW_ADMIN_APP_CONFIG where CONFIG_KEY='BS_${keyId}' and APPLICATION_NAME = '${dataSource}' and JSON_VALUE(CONFIG_VALUE,'\$.PRR_STRATIFICATION') is not null"}
    static stratification_values_ebgm = {dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group_dsp
        Where
            lang_id = '1'
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender_dsp
        WHERE
            lang_id = '1'
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_GENDER' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
        """
    }
    static stratification_values_ebgm_jader = {dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group_dsp
        Where
            lang_id = '1'
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender_dsp
        WHERE
            lang_id = '1'
        UNION ALL
        ( SELECT
            '男' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '女性' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '未知' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_GENDER' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            '通年' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'EBGM_STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
    ) = 1
)
        """
    }

    static stratification_values = { dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group_dsp
        Where
            lang_id = '1'
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender_dsp
        WHERE
            lang_id = '1'
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_GENDER' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
            
    ) = 1
)
        """
    }
    static stratification_values_jader = { dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group_dsp
        Where
            lang_id = '1'
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_AGE_GROUP' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender_dsp
        WHERE
            lang_id = '1'
        UNION ALL
        ( SELECT
            '男' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '女性' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            '未知' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_GENDER' and APPLICATION_NAME = '${dataSource}' 
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            '通年' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            JSON_VALUE(config_value,'\$.PVS_VALUE') as pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'STR_LATEST_DATE' and APPLICATION_NAME = '${dataSource}'
            
    ) = 1
)
        """
    }

    static stratification_values_ebgm_data_Mining = { keyId,dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group_dsp
        Where
            lang_id = '1'
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            ebgm_stratification LIKE '%AGE_GROUP%'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender_dsp
        WHERE
            lang_id = '1'    
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            ebgm_stratification LIKE '%GENDER%'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            ebgm_stratification LIKE '%LATEST_DATE%'
    ) = 1
)
        """
    }

    static stratification_values_data_Mining = { keyId,dataSource ->
        """
( SELECT
    'age' param,
    age_group param_value
FROM
    (
        ( SELECT
            age_group,
            1 is_custom
        FROM
            str_pvs_age_group_config
        UNION ALL
        SELECT
            'UNK' age_group,
            1 is_custom
        FROM
            dual
        )
        UNION ALL
        ( SELECT
            age_group
            || '>='
            || group_low
            || ' and <'
            || group_high
            || ' Years' age_group,
            0 is_custom
        FROM
            vw_lag_age_group_dsp
        Where
            lang_id = '1'
        UNION ALL
        SELECT
            'UNK' age_group,
            0 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_AGE_GRP_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            prr_stratification LIKE '%AGE_GROUP%'
    ) = 1
)
UNION ALL
( SELECT
    'gender' param,
    gender
FROM
    (
        SELECT
            gender,
            0 is_custom
        FROM
            vw_lg_gender_dsp
        WHERE
            lang_id = '1'
        UNION ALL
        ( SELECT
            'Male' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Female' gender,
            1 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            'Unknown' gender,
            1 is_custom
        FROM
            dual
        )
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_GENDER_STR' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            prr_stratification LIKE '%GENDER%'
    ) = 1
)
UNION ALL
( SELECT
    'receipt_years' param,
    year
FROM
    (
        SELECT
            'All Years' year,
            0 is_custom
        FROM
            dual
        UNION ALL
        SELECT
            year_group year,
            1 is_custom
        FROM
            pvs_ltst_date_config
    )
WHERE
    is_custom = (
        SELECT
            to_number(config_value) AS pvs_value
        FROM
            VW_ADMIN_APP_CONFIG
        WHERE
            config_key = 'FLAG_CSTM_LATEST_YEAR_GRP' and APPLICATION_NAME = '${dataSource}'
    )
    AND (
        SELECT
            count(1)
        FROM
            pvs_batch_signal_constants
        WHERE
            key_id = ${keyId} AND
            prr_stratification LIKE '%LATEST_DATE%'
    ) = 1
)
        """
    }

    static stratification_subgroup_enabled = { dataSource ->
        "select count(1) from VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%EBGM_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = '${dataSource}'"
    }

    static stratification_subgroup_values = { dataSource ->
        """
    (
    SELECT  'age_subgroup' param, age_group param_value
    FROM (
        SELECT  CASE
                    WHEN group_low IS NOT NULL THEN
                            age_group || '>='|| group_low || ' and <' || group_high || ' Years'
                END ||
                CASE
                    WHEN group_low IS NOT NULL AND cpc_age_group IS NOT NULL THEN
                        ' or '
                END ||
                CASE
                    WHEN cpc_age_group IS NOT NULL THEN
                        ' age group IN (' || RTRIM(SUBSTR(cpc_age_group, INSTR(UPPER(cpc_age_group), ' IN ') + 5), ')')|| ')'
                END age_group,
                1   is_custom
        FROM    pvs_age_group_config
        UNION ALL
        SELECT  age_group || '>=' || group_low || ' and <' || group_high || ' Years' age_group,
                0 is_custom
        FROM    vw_lag_age_group_dsp 
        Where
            lang_id = '1' 
        )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_AGE_GRP' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT  'gender_subgroup' param, gender param_value
    FROM (
        SELECT  gender, 0 is_custom
        FROM    vw_lg_gender_dsp
        WHERE
            lang_id = '1'
        UNION ALL
        (
        SELECT  label AS gender, 1 is_custom
        FROM    VW_PVS_GENDER_CONFIG_DISP
        )
    )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_GENDER' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT 'country_subgroup' param, label param_value
    FROM    VW_PVS_COUNTRY_DISP
)
UNION ALL
(
    SELECT  'region_subgroup' param, label param_value
    FROM (
        SELECT  label, 'PVS_REGIONS_EEA_DESC' is_custom
        FROM    VW_PVS_REGIONS_EEA_DISP
        UNION ALL
        SELECT  label, 'PVS_REGIONS_DESC' is_custom
        FROM    VW_PVS_REGIONS_DISP
        )
    WHERE   is_custom = ( SELECT JSON_VALUE(config_value,'\$.PVS_STR_VIEW') as PVS_STR_VIEW FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'EBGM_SUBGROUP_REGION' and APPLICATION_NAME = '${dataSource}')
)
        """
    }

    static stratification_subgroup_values_faers = { String dataSource ->
        """
(
    SELECT  'age_subgroup' param, age_group param_value
    FROM (
             SELECT  CASE
                         WHEN group_low IS NOT NULL THEN
                                 age_group || '>='|| group_low || ' and <' || group_high || ' Years'
                         END age_group,
                     1   is_custom
             FROM    pvs_age_group_config
             UNION ALL
             SELECT  age_group || '>=' || group_low || ' and <' || group_high || ' Years' age_group,
                     0 is_custom
             FROM    vw_lag_age_group_dsp
             Where
                lang_id = '1'
         )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_AGE_GRP' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT  'gender_subgroup' param, gender param_value
    FROM (
             SELECT  gender, 0 is_custom
             FROM    vw_lg_gender_dsp
             WHERE
                lang_id = '1'
             UNION ALL
             (
                 SELECT  label AS gender, 1 is_custom
                 FROM    VW_PVS_GENDER_CONFIG_DISP
             )
         )
    WHERE   is_custom = ( SELECT to_number(config_value) AS pvs_value FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'FLAG_CSTM_GENDER' and APPLICATION_NAME = '${dataSource}' )
)
UNION ALL
(
    SELECT 'country_subgroup' param, label param_value
    FROM    VW_PVS_COUNTRY_DISP
)
UNION ALL
(
    SELECT  'region_subgroup' param, label param_value
    FROM (
             SELECT  label, 'PVS_REGIONS_EEA_DESC' is_custom
             FROM    VW_PVS_REGIONS_EEA_DISP
             UNION ALL
             SELECT  label, 'PVS_REGIONS_DESC' is_custom
             FROM    VW_PVS_REGIONS_DISP
         )
    WHERE   is_custom = ( SELECT JSON_VALUE(config_value,'\$.PVS_STR_VIEW') as PVS_STR_VIEW FROM VW_ADMIN_APP_CONFIG WHERE config_key = 'EBGM_SUBGROUP_REGION' and APPLICATION_NAME = '${dataSource}')
)
        """
    }

    static case_info_sql = { cl, eventCode, eventCodeVal ->
        """
            SELECT DISTINCT ci.case_num case_number, ci.version_num case_version,
                lmp.product_name product_name, ce.mdr_ae_pt pt,
                cifu.case_labelness_desc listedness, cifu.case_outcome_desc,
                ls.seriousness seriousness,
                dv.rptd_result_assessment_desc determined_causality,
                dv.rptd_result_assessment_id reported_causality,
                cifu.flag_any_source_hcp hcp_flag,
                cifu.significant_counter follow_up                                   
            FROM c_identification ci,
                c_identification_fu cifu,
                c_prod_identification cp,
                c_ae_identification ce,
                vw_pud_seriousness ls,
                c_prod_ae_causality dv,
                vw_product lmp,
                (Select * from vw_lcau_causality where lang_id = '1') vlc
            WHERE ci.case_id = cp.case_id
            AND ci.tenant_id = cp.tenant_id
            AND ci.version_num = cp.version_num
            AND ci.case_id = cifu.case_id
            AND ci.tenant_id = cifu.tenant_id
            AND ci.version_num = cifu.version_num
            AND ci.case_id = ce.case_id
            AND ci.tenant_id = ce.tenant_id
            AND ci.version_num = ce.version_num
            AND ci.case_id = dv.case_id
            AND ci.tenant_id = dv.tenant_id
            AND ci.version_num = dv.version_num
            AND cp.prod_rec_num = dv.prod_rec_num
            AND ce.ae_rec_num = dv.ae_rec_num
            AND ls.ID(+) = ci.flag_serious
            AND cp.tenant_id = lmp.tenant_id(+)
            AND cp.prod_id_resolved = lmp.product_id(+)
            AND cp.flag_primary_prod = 1
            AND ce.${eventCode}               = '${eventCodeVal}'
            AND (ci.case_num, ci.version_num) IN (${cl})
            AND ls.lang_id = '1'
            AND lmp.lang_id = '1'
        """
    }

    static evdas_wwid_case_and_version_sql = { wwid ->
        """
        SELECT
    ci.case_num,
    ci.version_num,
    ci.flag_master_case
FROM
    c_identification_src ci
WHERE
    ci.case_num IS NOT NULL
    AND ci.case_id = coalesce((
        SELECT DISTINCT
            scwwid.case_id AS case_id
        FROM
                 c_case_ww_identifier_src scwwid
            JOIN c_identification_src ci_src ON(ci_src.case_num IS NOT NULL
                                                AND ci_src.tenant_id = scwwid.tenant_id
                                                AND ci_src.case_id = scwwid.case_id
                                                AND ci_src.version_num = scwwid.version_num)
        WHERE
            TRIM(upper(scwwid.wwid)) = '${wwid.toString().toUpperCase().trim()}'
        FETCH FIRST 1 ROWS ONLY
    ),
                              (
                             SELECT DISTINCT
                                 ci1.case_id AS case_id
                             FROM
                                 c_identification_src ci1
                             WHERE
                                     TRIM(upper(ci1.worldwide_case_identifier)) = '${wwid.toString().toUpperCase().trim()}'
                                 AND ci1.case_num IS NOT NULL
                                 AND ci1.case_id IS NOT NULL
                             FETCH FIRST 1 ROWS ONLY
                         ),
                              (
                             SELECT DISTINCT
                                 ci1.case_id
                             FROM
                                      c_references_fu_src ci1
                                 JOIN c_identification_src ci_src ON(ci_src.case_num IS NOT NULL
                                                                     AND ci_src.tenant_id = ci1.tenant_id
                                                                     AND ci_src.case_id = ci1.case_id
                                                                     AND ci_src.version_num = ci1.version_num)
                             WHERE
                                     TRIM(upper(ci1.reference_num)) = '${wwid.toString().toUpperCase().trim()}'
                                 AND ci1.reference_type_desc IN('E2B Company #', 'E2B Authority #', 'E2B Company Number', 'E2B Authority Number'
                                 )
                             FETCH FIRST 1 ROWS ONLY
                         ))
ORDER BY
    version_num DESC
FETCH FIRST 1 ROWS ONLY
        """
    }

    static signal_agg_alerts_sql = { aggAlertIds ->
        "select validated_signal_id from validated_agg_alerts where agg_Alert_id in ($aggAlertIds)"
    }

    static signal_evdas_alerts_sql = { evdasAlertIds ->
        "select validated_signal_id from validated_evdas_alerts where EVDAS_ALERT_ID in ($evdasAlertIds)"
    }

    //This is for the calling with we are calculating only EBGM or Ebgm with PRR\ROR
    static ebgm_calling_sql = { id ->
        "SELECT * from PVS_APP_EB_FULL_CNT_${id} where PT_NAME is not null and PRODUCT_NAME is not null and ROW_COUNT_A <> 0"
    }

    //This will run when only PRR is configured and we don't need EBGM.
    static prr_calling_sql = { id ->
        "SELECT * from PVS_APP_EB_FULL_CNT_${id} where PT_NAME is not null and PRODUCT_NAME is not null"
    }

    static dss_calling_sql = { id ->
        "SELECT * from PVS_APP_DSS_FULL_CNT where PT_NAME is not null and PRODUCT_NAME is not null"
    }

    static select_auto_alert_sql = {
        "select FINISH_DATETIME from V_PVR_ETL_STATUS where UPPER(STATUS)='SUCCESS' "
    }

    static update_auto_alert_sql = { cases ->
        "UPDATE PVS_AUTO_ALERT_INCR_CASES SET DELETED_FLAG = 1 WHERE DELETED_FLAG = 0 and case_num in (" + cases + ")"
    }

    static alert_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_alert_status where execution_id = ${id}"
    }


    static alert_db_progress_status_sql = { id ->
        "select PROGRESS_PCT, CURRENT_STATUS from VW_PVS_ALERT_PROGRESS where execution_id = ${id}"
    }


    static multiple_alert_db_progress_status_sql = { exConfigIds ->
        "select * from VW_PVS_ALERT_PROGRESS where execution_id in (${exConfigIds})"
    }

    static alert_resumption_limit_hours = {
        "select KEY_VALUE from pvs_app_constants where key_id='ALERT_RESUMPTION_LIMIT_HOURS'"
    }

    static select_etl_alert_sql = {
        "select FINISH_DATETIME from vw_pvs_etl_status"
    }

    static execution_status_detail = { id ->
        "select CONFIG_ID, execution_level, type from ex_status where id = ${id}"
    }

    static execution_completed_status_detail = {
        "SELECT ID FROM EX_STATUS WHERE ID IN (SELECT EX_STATUS_ID FROM APP_ALERT_PROGRESS_STATUS) AND EX_STATUS = 'COMPLETED'"
    }
    static check_prr_count_db = { executedConfigurationId, boolean isAws = false ->
        "select * from ${isAws ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_prr_ror_ml_info_${executedConfigurationId}": "pvs_prr_ror_ml_info_${executedConfigurationId}"}"
    }

    static criteria_sheet_count = { executedConfigurationId, boolean isAws = false, boolean isHierarchyAlert = false, String eventHierarchy = '', String productId = "" ->
        """
        SELECT
    execution_id,
    new_total_count,
    cumm_total_count,
    study_total_new_count,
    study_total_cumm_count
FROM
    (
        SELECT
            execution_id,
            COUNT(DISTINCT
                CASE
                    WHEN cumm_flag = 1 THEN
                        case_id
                END
            ) cumm_total_count,
            COUNT(DISTINCT
                CASE
                    WHEN cumm_flag = 1
                         AND date_range_type_flag = 1 THEN
                        case_id
                END
            ) new_total_count,
            COUNT(DISTINCT
                CASE
                    WHEN study_flag = 1 THEN
                        case_id
                END
            ) study_total_cumm_count,
            COUNT(DISTINCT
                CASE
                    WHEN study_flag = 1
                         AND date_range_type_flag = 1 THEN
                        case_id
                END
            ) study_total_new_count
        FROM
            ${isAws ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_dd_temp_${executedConfigurationId}": isHierarchyAlert ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_dd_temp_${eventHierarchy}_${executedConfigurationId} where base_id = '${productId}'" : "pvs_case_drill_down WHERE execution_id IN (${executedConfigurationId.join(',')})"}
        GROUP BY
            execution_id
    )
"""
    }


    static agg_count_sql = { id, isEventGroup, exConfigIds,selectedDatasource, boolean isAws = false, boolean isHierarchyAlert = false, String eventHierarchy = "", String productId = "" ->
        """
            SELECT
                PRODUCT_NAME AS PRODUCT_NAME,
                PRODUCT_ID AS PRODUCT_ID,
                PT_NAME AS PT,
                PT_CODE AS PT_CODE,
                ${isEventGroup ? '':'SOC_NAME AS SOC,'}
                CUMM_SPONT_COUNT AS CUMM_SPON_COUNT,
                NEW_SPONT_COUNT AS NEW_SPON_COUNT,
                CUMM_STUDY_COUNT AS CUMM_STUDY_COUNT,
                NEW_STUDY_COUNT AS NEW_STUDY_COUNT,
                CUMM_SERIOUS_COUNT AS CUMM_SERIOUS_COUNT,
                NEW_SERIOUS_COUNT AS NEW_SERIOUS_COUNT,
                CUMM_FATAL_COUNT AS CUMM_FATAL_COUNT,
                NEW_FATAL_COUNT  AS NEW_FATAL_COUNT,
                FLAG_RECHAL AS POSITIVE_RECHALLENGE,
                FLAG_DECHAL AS POSITIVE_DECHALLENGE,
                FLAG_PREG AS PREGENENCY,
                FLAG_LABEL AS LISTED,
                FLAG_CONSERVATIVE_RELATEDNESS AS RELATEDNESS,
                NEW_COUNT AS NEW_COUNT,
                CUMM_COUNT AS CUMM_COUNT,
                NEW_PEDIA_COUNT AS NEW_PEDIA_COUNT,
                CUMM_PEDIA_COUNT AS CUMM_PEDIA_COUNT,              
                NEW_INTERACTING_COUNT AS NEW_INTERACTING_COUNT,
                CUMM_INTERACTING_COUNT AS CUMM_INTERACTING_COUNT,
                NEW_GERIA_COUNT AS NEW_GERIA_COUNT,
                CUMM_GERIA_COUNT AS CUMM_GERIA_COUNT,
                NEW_NON_SERIOUS_COUNT AS NEW_NON_SERIOUS_COUNT,
                CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT,
                PROD_HIERARCHY_ID AS PROD_HIERARCHY_ID,
                EVENT_HIERARCHY_ID AS EVENT_HIERARCHY_ID,
                PROD_N_PERIOD AS PROD_N_PERIOD,
                PROD_N_CUMUL AS PROD_N_CUMUL,CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT
                ${selectedDatasource == 'pva' ? ", HLT_NAME AS HLT_NAME, HLGT_NAME AS HLGT_NAME ,SMQ_NARROW_NAME AS SMQ_NARROW_NAME,#NEW_DYNAMIC_COUNT":""}
                ${exConfigIds? ", CHILD_EXECUTION_ID AS CHILD_EXECUTION_ID":""}
            FROM ${isAws ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_app_agg_counts_${id}": isHierarchyAlert ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_app_agg_counts_${eventHierarchy}_${id}" : "PVS_APP_AGG_COUNTS_${id}"} where ${ isHierarchyAlert ? "PRODUCT_ID = '${productId}' AND": ""} PT_NAME IS NOT NULL
            ${exConfigIds? "and CHILD_EXECUTION_ID in (${exConfigIds.join(",")})":''}
        """
    }


    static agg_count_sql_sv = { id, isEventGroup, exConfigIds ,selectedDatasource, boolean isAws = false, boolean isHierarchyAlert = false, String eventHierarchy = ""->
        """
            SELECT
                a.PRODUCT_NAME AS PRODUCT_NAME,
                a.PRODUCT_ID AS PRODUCT_ID,
                a.PT_NAME AS PT,
                a.PT_CODE AS PT_CODE,
                ${isEventGroup ? '':'a.SOC_NAME AS SOC,'}
                a.CUMM_SPONT_COUNT AS CUMM_SPON_COUNT,
                a.NEW_SPONT_COUNT AS NEW_SPON_COUNT,
                a.CUMM_STUDY_COUNT AS CUMM_STUDY_COUNT,
                a.NEW_STUDY_COUNT AS NEW_STUDY_COUNT,
                a.CUMM_SERIOUS_COUNT AS CUMM_SERIOUS_COUNT,
                a.NEW_SERIOUS_COUNT AS NEW_SERIOUS_COUNT,
                a.CUMM_FATAL_COUNT AS CUMM_FATAL_COUNT,
                a.NEW_FATAL_COUNT  AS NEW_FATAL_COUNT,
                a.FLAG_RECHAL AS POSITIVE_RECHALLENGE,
                a.FLAG_DECHAL AS POSITIVE_DECHALLENGE,
                a.FLAG_PREG AS PREGENENCY,
                a.FLAG_LABEL AS LISTED,
                a.FLAG_CONSERVATIVE_RELATEDNESS AS RELATEDNESS,
                a.NEW_COUNT AS NEW_COUNT,
                a.CUMM_COUNT AS CUMM_COUNT,
                a.NEW_PEDIA_COUNT AS NEW_PEDIA_COUNT,
                a.CUMM_PEDIA_COUNT AS CUMM_PEDIA_COUNT,              
                a.NEW_INTERACTING_COUNT AS NEW_INTERACTING_COUNT,
                a.CUMM_INTERACTING_COUNT AS CUMM_INTERACTING_COUNT,
                a.NEW_GERIA_COUNT AS NEW_GERIA_COUNT,
                a.CUMM_GERIA_COUNT AS CUMM_GERIA_COUNT,
                a.NEW_NON_SERIOUS_COUNT AS NEW_NON_SERIOUS_COUNT,
                a.CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT,
                a.PROD_HIERARCHY_ID AS PROD_HIERARCHY_ID,
                a.EVENT_HIERARCHY_ID AS EVENT_HIERARCHY_ID,
                b.NEW_COUNT as NEW_COUNT_FREQ_CALC,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC,
                a.PROD_N_PERIOD AS PROD_N_PERIOD,
                a.PROD_N_CUMUL AS PROD_N_CUMUL,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC
${selectedDatasource == 'pva' ? ", HLT_NAME AS HLT_NAME, HLGT_NAME AS HLGT_NAME ,SMQ_NARROW_NAME AS SMQ_NARROW_NAME,#NEW_DYNAMIC_COUNT":""}
                ${exConfigIds? ", CHILD_EXECUTION_ID AS CHILD_EXECUTION_ID":""}
            FROM ${isAws ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_app_agg_counts_${id}": isHierarchyAlert ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_app_agg_counts_${eventHierarchy}_${id}" : "PVS_APP_AGG_COUNTS_${id}"} a
            LEFT JOIN ${isAws ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_app_agg_cnt_sv_${id}": isHierarchyAlert ? "glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_app_agg_cnt_sv_${eventHierarchy}_${id}" :  "PVS_APP_AGG_CNT_SV_${id}"} b 
            ON a.PRODUCT_ID = b.PRODUCT_ID AND a.PT_CODE = b.PT_CODE
            where a.PT_NAME IS NOT NULL
            ${exConfigIds? "and a.CHILD_EXECUTION_ID in (${exConfigIds.join(",")})":''}
        """
    }

    static agg_count_sql_sv_faers = { id, isEventGroup, exConfigIds ,selectedDatasource->
        """
            SELECT
                a.PRODUCT_NAME AS PRODUCT_NAME,
                a.PRODUCT_ID AS PRODUCT_ID,
                a.PT_NAME AS PT,
                a.PT_CODE AS PT_CODE,
                ${isEventGroup ? '':'a.SOC_NAME AS SOC,'}
                a.CUMM_SPONT_COUNT AS CUMM_SPON_COUNT,
                a.NEW_SPONT_COUNT AS NEW_SPON_COUNT,
                a.CUMM_STUDY_COUNT AS CUMM_STUDY_COUNT,
                a.NEW_STUDY_COUNT AS NEW_STUDY_COUNT,
                a.CUMM_SERIOUS_COUNT AS CUMM_SERIOUS_COUNT,
                a.NEW_SERIOUS_COUNT AS NEW_SERIOUS_COUNT,
                a.CUMM_FATAL_COUNT AS CUMM_FATAL_COUNT,
                a.NEW_FATAL_COUNT  AS NEW_FATAL_COUNT,
                a.FLAG_PREG AS PREGENENCY,
                a.FLAG_LABEL AS LISTED,
                a.FLAG_CONSERVATIVE_RELATEDNESS AS RELATEDNESS,
                a.NEW_COUNT AS NEW_COUNT,
                a.CUMM_COUNT AS CUMM_COUNT,
                a.NEW_PEDIA_COUNT AS NEW_PEDIA_COUNT,
                a.CUMM_PEDIA_COUNT AS CUMM_PEDIA_COUNT,              
                a.NEW_INTERACTING_COUNT AS NEW_INTERACTING_COUNT,
                a.CUMM_INTERACTING_COUNT AS CUMM_INTERACTING_COUNT,
                a.NEW_GERIA_COUNT AS NEW_GERIA_COUNT,
                a.CUMM_GERIA_COUNT AS CUMM_GERIA_COUNT,
                a.NEW_NON_SERIOUS_COUNT AS NEW_NON_SERIOUS_COUNT,
                a.CUMM_NON_SERIOUS_COUNT AS CUMM_NON_SERIOUS_COUNT,
                a.PROD_HIERARCHY_ID AS PROD_HIERARCHY_ID,
                a.EVENT_HIERARCHY_ID AS EVENT_HIERARCHY_ID,
                b.NEW_COUNT as NEW_COUNT_FREQ_CALC,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC,
                a.PROD_N_PERIOD AS PROD_N_PERIOD,
                a.PROD_N_CUMUL AS PROD_N_CUMUL,
                b.CUMM_COUNT as CUMM_COUNT_FREQ_CALC
${selectedDatasource == 'pva' ? ", HLT_NAME AS HLT_NAME, HLGT_NAME AS HLGT_NAME ,SMQ_NARROW_NAME AS SMQ_NARROW_NAME,#NEW_DYNAMIC_COUNT":""}
                ${exConfigIds? ", CHILD_EXECUTION_ID AS CHILD_EXECUTION_ID":""}
            FROM PVS_APP_AGG_COUNTS_${id} a
            LEFT JOIN PVS_APP_AGG_CNT_SV_${id} b 
            ON a.PRODUCT_ID = b.PRODUCT_ID AND a.PT_CODE = b.PT_CODE
            where a.PT_NAME IS NOT NULL
            ${exConfigIds? "and a.CHILD_EXECUTION_ID in (${exConfigIds.join(",")})":''}
        """
    }

    static signal_detail_sql = {
        """
           select  JSON_VALUE(products,'\$."3"."name"')   as "Product Name", name as "Signal Name", INITIAL_DATA_SOURCE as "Initial Data Source",   (select b.DISPLAY_NAME  from Disposition b where b.ID =a.DISPOSITION_ID and rownum=1  ) as "Disposition",
(select c.value  from Priority c  where c.id = a.PRIORITY_ID and rownum=1) as "Priority" ,  case  trim(ASSIGNMENT_TYPE) when 'GROUP' then 'Group: '||  (   select e.name from  GROUPS e where e.id = a.ASSIGNED_TO_ID and rownum=1)  else
                                                                                                                   'User: ' ||(   select e.USERNAME from  pvuser e where e.id = a.ASSIGNED_TO_ID and rownum=1) end   as "Assigned To" ,
(select  (select  a2.name from SIGNAL_CATEGORY a2  where a2.id = a1.VALIDATED_SIGNAL_CATEGORY_ID and rownum=1 ) from VALIDATED_SIGNAL_CATEGORY a1 where a1.VALIDATED_SIGNAL_ID = a.id and rownum = 1) as "Signal Type",
to_char(START_DATE,'dd-MON-yyyy') as "Start Date", to_char(END_DATE,'dd-MON-yyyy') as "End Date"
from validated_signal a
        """
    }

    static signal_action_sql = { signalId ->
        """
            select 
            b.name as "Signal Name", 
            (select DISPLAY_NAME from ACTION_CONFIGURATIONS where id = a.config_id) as "Action Name", 
            (select DISPLAY_NAME from  action_types where id = TYPE_ID) "Action Type",
            (select e.USERNAME from  pvuser e where e.id = a.ASSIGNED_TO_ID) as "Assigned To", 
            (select g.NAME from  GROUPS g where g.id = a.ASSIGNED_TO_GROUP_ID) as "Assigned To Group", 
            ACTION_STATUS as "Status", to_char(CREATED_DATE,'dd-MON-yyyy')  as "Creation Date",
            to_char(a.DUE_DATE,'dd-MON-yyyy') as "Due Date", to_char(COMPLETED_DATE,'dd-MON-yyyy') as "Completion Date", 
            DETAILS as "Details", COMMENTS as "Comments",
            a.GUEST_ATTENDEE_EMAIL as "Guest Email"
            from actions a ,validated_signal b, validated_signal_actions c
            where b.id = ${signalId} and a.id = c.action_id and b.id = c.validated_signal_actions_id
        """
    }

    static evdas_query_sql = {
        "{call pkg_create_report_sql_evd.p_main_query(?,?)}"
    }

    static meeting_detail_sql = { signalId ->
        """
            select 
            validated_signal.name "Signal Name" , 
            Meeting_title "Title", 
            to_char(meeting_date,'DD-MON-YYYY HH24:MM:SS') "Meeting Date/Time",
            meeting_agenda "Agenda", 
            meeting_minutes  "Minutes" , 
            meeting.Modified_by  "Last Updated By",
            to_char(meeting.last_updated,'DD-MON-YYYY HH24:MM:SS') "Last Updated"
            from Meeting, validated_signal
            where meeting.validated_signal_id=validated_signal.id(+) 
            and validated_signal.id = ${signalId}
        """
    }

    static family_name_from_case_sql = { caseNum, caseVersion ->
        """
           SELECT DISTINCT family_name
           FROM c_identification ci LEFT JOIN c_prod_identification_fu cpi
                ON (    ci.tenant_id = cpi.tenant_id
                    AND ci.case_id = cpi.case_id
                    AND ci.version_num = cpi.version_num
                   )
                LEFT JOIN (select * from vw_family_name where lang_id = '1') vfn
                ON (    cpi.prod_family_id_resolved = vfn.prod_family_id
                    AND cpi.tenant_id = vfn.tenant_id
                   )
				   where ci.case_num = '${caseNum}'
            and ci.version_num = ${caseVersion}
        """
    }

    static soc_pt_sql = { soc ->
        "SELECT pt_name, pt_code FROM pvr_md_pref_term_dsp pmpt JOIN pvr_md_soc_dsp pmsd on (pmpt.PT_SOC_CODE = pmsd.SOC_CODE) WHERE soc_code =  '${soc}' AND pmsd.lang_id = 1 and pmpt.lang_id = 1"
    }

    static trend_analysis_sql = { startDate, endDate ->
        "{call p_trend_analysis(${startDate},${endDate}, ?, ?, ?, ?, ?, ?)}"
    }

    static add_case_sql = { List<String> cl ->
        String inClause = ''
        String csiSponsorStudyNumber
        String studyClassificationId

        if (Holders.config.custom.qualitative.fields.enabled) {
            csiSponsorStudyNumber = Constants.CustomRptFields.FDA_CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.FDA_STUDY_CLASSIFICATION_ID
        } else {
            csiSponsorStudyNumber = Constants.CustomRptFields.CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.STUDY_CLASSIFICATION_ID
        }

        if (cl && cl.size() <= 1000) {
            inClause = "(${cl.join(",")})"
        } else if (cl && cl.size() > 1000) {
            inClause = cl.collate(1000).join(" OR ci.CASE_NUM IN ").replace("[", "(").replace("]", ")")
        }

        """
WITH max_version AS (
                  SELECT
                      MAX(version_num) version_num,
                      case_id,
                      tenant_id
                  FROM
                      v_c_identification
                      where 
                      ( case_num IN ${ inClause })
                  GROUP BY
                      case_id,
                      tenant_id
              ), data AS (
                          SELECT DISTINCT
                              ci.case_id                     case_id,
                              ci.case_num                    case_num,
                              ci.version_num                 version_num,
                              ci.tenant_id                   tenant_id,
                  cpifu.prod_rec_num              prod_rec_num,
                  caei.ae_rec_num                gt_ae_rec_num,
                                VW_LH_HCP.HCP				   CsHcpFlag,
                              ci.source_type_desc            source_type_desc,
                              ci.date_first_receipt          date_first_receipt,
                  ci.txt_date_first_receipt      txt_date_first_receipt,
                              cifu.date_receipt              date_receipt,
                              cifu.case_outcome_desc         case_outcome_desc,
                              cpifu.prod_family_name         prod_family_name,
                              cifu.significant_counter       significant_counter,
                              decode(cifu.flag_ver_significant, 1, 'Yes', 'No') flag_ver_significant,
                              cpi.product_name               product_name,
                  caei.mdr_ae_pt                 prim_ae_pt,
                              cifu.prim_prod_name            prim_prod_name,
                              caei.ae_outcome_desc           ae_outcome_desc,
                              decode(ci.flag_serious, 1, 'Serious', 'Non Serious') flag_serious,
                              ci.occured_country_desc        occured_country_desc,
                              cpc.patient_age_group_desc     patient_age_group_desc,
                              cpc.patient_sex_desc           patient_sex_desc,
                              decode(cdifu.rechallenge_id, 3, 'N/A', 0, 'No',
                                     1, 'Yes', 2, 'Unk') rechallenge_id,
                              cifu.date_locked               date_locked,
                  cifu.txt_date_locked           txt_date_locked,
                              decode(ci.flag_serious_death, 1, 'Yes', 0, 'No',
                                     'Unk') flag_serious_death,
                                decode(ci.flag_combination_product, 1, 'Yes', NULL, '-',
                                       'No') flag_combination_product,
                              caei.mdr_ae_pt                 mdr_ae_pt,
                              cpi.prod_id_resolved           prod_id_resolved,
                              caei.ae_rec_num                ae_rec_num,
                                cifu.CASE_LABELNESS_DESC       listedness_text,
                                1 AS flag_master_case,
                              patient_age_onset_years        AS age_in_years,
                  csti.project_num            project_num,
                  csti.study_number           study_number,
                  cstdidfc.study_number        PreAnda_study_number,
                  cpat.tto_days              tto_days,
                  cls1_57.state_yn            as flag_susar,
                  F_GET_SUR_SER_UNL_REL('SAFETY',ci.tenant_id,ci.case_id,ci.version_num,cpifu.prod_rec_num,caei.AE_REC_NUM)                  sur
                          FROM
                              c_identification            ci
                              JOIN max_version              gqcl ON (ci.tenant_id = gqcl.tenant_id
                                                                      AND ci.version_num = gqcl.version_num
                                                                      AND ci.case_id = gqcl.case_id )
                              LEFT JOIN c_identification_fu         cifu ON ( ci.tenant_id = cifu.tenant_id
                                                                      AND ci.version_num = cifu.version_num
                                                                      AND ci.case_id = cifu.case_id )
                              LEFT JOIN c_patient_characteristics   cpc ON ( ci.tenant_id = cpc.tenant_id
                                                                           AND ci.version_num = cpc.version_num
                                                                           AND ci.case_id = cpc.case_id )
                              LEFT JOIN c_ae_identification         caei ON ( ci.tenant_id = caei.tenant_id
                                                                      AND ci.version_num = caei.version_num
                                                                      AND ci.case_id = caei.case_id )
                              LEFT JOIN c_prod_identification_fu    cpifu ON ( ci.tenant_id = cpifu.tenant_id
                                                                            AND ci.version_num = cpifu.version_num
                                                                            AND ci.case_id = cpifu.case_id )
                              LEFT JOIN c_prod_identification       cpi ON ( cpifu.tenant_id = cpi.tenant_id
                                                                       AND cpifu.version_num = cpi.version_num
                                                                       AND cpifu.case_id = cpi.case_id
                                                                       AND cpifu.prod_rec_num = cpi.prod_rec_num )
                              LEFT JOIN c_ae_identification_fu      caefu ON ( caefu.tenant_id = caei.tenant_id
                                                                          AND caefu.version_num = caei.version_num
                                                                          AND caefu.case_id = caei.case_id
                                                                          AND caefu.ae_rec_num = caei.ae_rec_num )
                              LEFT JOIN vw_product                  vpr ON ( cpi.prod_id_resolved = vpr.product_id and vpr.lang_id = '1' )
                              LEFT JOIN c_drug_identification_fu    cdifu ON ( cpifu.tenant_id = cdifu.tenant_id
                                                                            AND cpifu.version_num = cdifu.version_num
                                                                            AND cpifu.case_id = cdifu.case_id
                                                                            AND cpifu.prod_rec_num = cdifu.prod_rec_num )
                              LEFT JOIN c_prod_devices_addl         cpda ON ( cpi.tenant_id = cpda.tenant_id
                                                                      AND cpi.case_id = cpda.case_id
                                                                      AND cpi.version_num = cpda.version_num
                                                                      AND cpi.prod_rec_num = cpda.prod_rec_num )
                  LEFT OUTER JOIN c_study_identification         csti ON ( csti.tenant_id = ci.tenant_id
                                                    AND csti.version_num = ci.version_num
                                                    AND csti.case_id = ci.case_id )
                  LEFT OUTER JOIN VW_PRE_ANDA_STUDY_NUMBER         cstdidfc ON ( cstdidfc.tenant_id = ci.tenant_id
                                                    AND cstdidfc.version_num = ci.version_num
                                                    AND cstdidfc.case_id = ci.case_id )
                  LEFT OUTER JOIN cdr_prod_ae_tto                cpat ON ( cpat.tenant_id = caei.tenant_id
                                               AND cpat.version_num = caei.version_num
                                               AND cpat.ae_rec_num = caei.ae_rec_num
                                               AND cpat.case_id = caei.case_id
                                               AND cpat.tenant_id = cpifu.tenant_id
                                               AND cpat.version_num = cpifu.version_num
                                               AND cpat.case_id = cpifu.case_id
                                               AND cpat.prod_rec_num = cpifu.prod_rec_num )
                  LEFT OUTER JOIN (select * from vw_clp_state_yn where lang_id = '1')                cls1_57 ON ( cifu.flag_susar = cls1_57.id )
								LEFT OUTER JOIN (SELECT * FROM VW_CASE_HCP WHERE LANG_ID = '1') cshcpflg ON ( cshcpflg.tenant_id = cifu.tenant_id
                                                                        AND cshcpflg.version_num = cifu.version_num
                                                                        AND cshcpflg.case_id = cifu.case_id )
								LEFT OUTER JOIN (SELECT * FROM VW_LH_HCP WHERE LANG_ID = '1') VW_LH_HCP ON ( VW_LH_HCP.HCP_ID = cshcpflg.FLAG_HCP )

                          WHERE
                              cpifu.flag_primary_prod = 1
                              AND caefu.flag_primary_ae = 1
                      ), clob_data AS (
                          SELECT
                              cdc.case_id         case_id,
                              cdc.version_num     version_num,
                              cdc.tenant_id       tenant_id,
                              cs.case_narrative   case_narrative,
                              replace(replace(replace(cdc.ae_pt, '!@##@!', CHR(13)
                                                                           || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') ae_pt,
                              replace(replace(replace(csda.company_susp_prod_all, '!@##@!', CHR(13)
                                                                                            || CHR(10)), '!@_@!', '-'), '!@.@!'
                                                                                            , ') ') company_susp_prod_all,
                              replace(replace(replace(csda.concomit_prod_all, '!@##@!', CHR(13)
                                                                                        || CHR(10)), '!@_@!', '-'), '!@.@!', ') '
                                                                                        ) concomit_prod_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','CHARACTERISTIC_ALL_CS',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as characteristic_all_cs,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','PAT_MED_COND',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as pat_med_cond_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','PAT_DRUG_HIST',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as coded_drug_name_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','CAUSE_OF_DEATH',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as cd_codd_reptd_all
                          FROM
                              cdr_clob            cdc
                              LEFT JOIN cdr_clob_prod_all   csda ON ( csda.case_id = cdc.case_id
                                                                    AND csda.version_num = cdc.version_num
                                                                    AND csda.tenant_id = cdc.tenant_id )
                              LEFT JOIN c_summary           cs ON ( cs.case_id = cdc.case_id
                                                          AND cs.version_num = cdc.version_num
                                                          AND cs.tenant_id = cdc.tenant_id )

                 where ( cdc.tenant_id,
                     cdc.case_id,
                     cdc.version_num ) IN (
                                  SELECT
                                      tenant_id,
                                      case_id,
                                      version_num
                                  FROM
                                      max_version
                              )
                      )
            ,clob_data_prod as(
            select gt.tenant_id  , gt.version_num  , gt.CASE_ID, gt.prod_rec_num,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','INDICATION',gt.tenant_id  , gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as ind_codd_reptd_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','LOT_NO_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as lot_no_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','THERAPY_DATES_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as therapy_dates_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','DOSE_DETAIL_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as dose_detail_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('SAFETY','ALL_PTS_SUR',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as all_pts_sur
            from (select distinct gt.tenant_id  , gt.version_num  , gt.CASE_ID, cpifu.prod_rec_num from max_version gt 
                  inner join c_prod_identification_fu    cpifu on gt.tenant_id = cpifu.tenant_id
                                AND gt.version_num = cpifu.version_num
                                AND gt.case_id = cpifu.case_id 
                  where cpifu.flag_primary_prod = 1
            
            ) gt                         
            )
                      SELECT
                          d.case_id                    "masterCaseId",
                          d.case_num                   "masterCaseNum",
                          d.version_num                "masterVersionNum",
                            d.CsHcpFlag                "CsHcpFlag",
                          d.source_type_desc           "masterRptTypeId",
                          d.date_first_receipt         "masterInitReptDate",
               d.txt_date_first_receipt     "ciTxtDateReceiptInitial",
                          d.date_receipt               "masterFollowupDate",
                          d.listedness_text            "assessListedness",
                          cd.company_susp_prod_all     "masterSuspProdList",
                          cd.concomit_prod_all         "masterConcomitProdList",
                          d.case_outcome_desc          "assessOutcome",
                          d.prod_family_name           "productFamilyId",
                          d.significant_counter        "masterFupNum",
                          d.flag_ver_significant       "masterFlagSt",
                          d.product_name               "productProductId",
                          d.prim_ae_pt                      "masterPrimEvtPrefTerm",
                          d.prim_prod_name             "masterPrimProdName",
                          d.ae_outcome_desc            "eventEvtOutcomeId",
                          d.flag_serious               "assessSeriousness",
                          d.occured_country_desc       "masterCountryId",
                          d.patient_age_group_desc     "patInfoAgeGroupId",
                          d.patient_sex_desc           "patInfoGenderId",
                          d.rechallenge_id             "prodDrugsPosRechallenge",
                          d.date_locked                "masterDateLocked",
               d.txt_date_locked         "cifTxtDateLocked",
                          d.flag_serious_death         "masterFatalFlag",
                            d.flag_combination_product   "deviceComboProduct",
                          d.mdr_ae_pt                  "eventPrefTerm",
                          d.prod_id_resolved           "cpiProdIdResolved",
                          cd.case_narrative            "narrativeNarrative",
                          cd.ae_pt                     "ccAePt",
                          NULL AS "cmadlflagEligibleLocalExpdtd",
                          NULL AS "caseMasterPvrUdText12",
                          NULL AS "${csiSponsorStudyNumber}",
                          NULL AS "${studyClassificationId}",
                          NULL AS "vwcpai1FlagCompounded",
                          NULL AS "csisender_organization",
                          NULL AS "cdrClobAeAllUdUdClob1",
                          age_in_years                 AS "casePatInfoPvrUdNumber2",
                          NULL AS "casProdDrugsPvrUdText20",
                          NULL AS "caseProdDrugsPvrUdNumber10",
                          NULL AS "caseProdDrugsPvrUdNumber11",
                          d.flag_master_case           "flagMasterCase",
               d.project_num            "vwstudyProtocolNum",
               d.PreAnda_study_number     "PreAndastudyStudyNum",
               d.tto_days              "dvProdEventTimeOnsetDays",
               d.flag_susar             "masterSusar",
               d.sur                  "ceSerUnlRel",
               cdp.ind_codd_reptd_all     "productIndCoddorReptd",
               cdp.lot_no_all              "productLotNoAllcs",
               cdp.all_pts_sur                 "masterPrefTermSurAll",
               cdp.therapy_dates_all         "productStartStopDateAllcs",
               cdp.dose_detail_all              "productDoseDetailAllcs",
               cd.characteristic_all_cs       "masterCharactersticAllcs",
               cd.pat_med_cond_all              "cprmConditionAll",
               cd.coded_drug_name_all        "ccMedHistDrugAll",
               cd.cd_codd_reptd_all          "ccCoddRptdCauseDeathAll"
                      FROM
                          data        d,
                          clob_data   cd,
               clob_data_prod cdp
                      WHERE
                          d.case_id = cd.case_id
                          AND d.version_num = cd.version_num
                          AND d.tenant_id = cd.tenant_id
                          AND d.case_id = cdp.case_id
                          AND d.version_num = cdp.version_num
                          AND d.tenant_id = cdp.tenant_id
                          AND d.prod_rec_num = cdp.prod_rec_num
  """

    }

    static add_case_sql_custom_col = { List<String> cl ->
        String inClause = ''
        String csiSponsorStudyNumber
        String studyClassificationId

        if (Holders.config.custom.qualitative.fields.enabled) {
            csiSponsorStudyNumber = Constants.CustomRptFields.FDA_CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.FDA_STUDY_CLASSIFICATION_ID
        } else {
            csiSponsorStudyNumber = Constants.CustomRptFields.CSI_SPONSOR_STUDY_NUMBER
            studyClassificationId = Constants.CustomRptFields.STUDY_CLASSIFICATION_ID
        }

        if (cl && cl.size() <= 1000) {
            inClause = "(${cl.join(",")})"
        } else if (cl && cl.size() > 1000) {
            inClause = cl.collate(1000).join(" OR ci.CASE_NUM IN ").replace("[", "(").replace("]", ")")
        }

        """

              WITH max_version AS (
                  SELECT
                      MAX(version_num) version_num,
                      case_id,
                      tenant_id
                  FROM
                      v_c_identification
                      where 
                      ( case_num IN ${inClause})
                  GROUP BY
                      case_id,
                      tenant_id
              ), data AS (
                          SELECT DISTINCT
                              ci.case_id                     case_id,
                              ci.case_num                    case_num,
                              ci.version_num                 version_num,
                              ci.tenant_id                   tenant_id,
                  cpifu.prod_rec_num              prod_rec_num,
                  caei.ae_rec_num                gt_ae_rec_num,
                                decode(ci.flag_medically_confirm, 1, 'Yes', 2, 'No',
                                        3, 'Unknown') flag_primary_source_hcp,
                               ci.source_type_desc            source_type_desc,
                              ci.date_first_receipt          date_first_receipt,
                  ci.txt_date_first_receipt      txt_date_first_receipt,
                              cifu.date_receipt              date_receipt,
                              cifu.case_outcome_desc         case_outcome_desc,
                              cpifu.prod_family_name         prod_family_name,
                              cifu.significant_counter       significant_counter,
                              decode(cifu.flag_ver_significant, 1, 'Yes', 'No') flag_ver_significant,
                              cpi.product_name               product_name,
                  caei.mdr_ae_pt                 prim_ae_pt,
                              cifu.prim_prod_name            prim_prod_name,
                              caei.ae_outcome_desc           ae_outcome_desc,
                              decode(ci.flag_serious, 1, 'Serious', 'Non Serious') flag_serious,
                              ci.occured_country_desc        occured_country_desc,
                              cpc.patient_age_group_desc     patient_age_group_desc,
                              cpc.patient_sex_desc           patient_sex_desc,
                              decode(cdifu.rechallenge_id, 3, 'N/A', 0, 'No',
                                     1, 'Yes', 2, 'Unk') rechallenge_id,
                              cifu.date_locked               date_locked,
                  cifu.txt_date_locked           txt_date_locked,
                              decode(ci.flag_serious_death, 1, 'Yes', 0, 'No',
                                     'Unk') flag_serious_death,
                                decode(ci.flag_combination_product, 1, 'Yes',
                                       NULL, '-', 'No') flag_combination_product,
                              caei.mdr_ae_pt                 mdr_ae_pt,
                              cpi.prod_id_resolved           prod_id_resolved,
                              caei.ae_rec_num                ae_rec_num,
                                cifu.CASE_LABELNESS_DESC       listedness_text,
                   vfct.case_type                                                case_type,
                  cma.ud_text_12                                                completeness_score,
                  CASE
                                     WHEN upper(cra.STUDY_TYPE_DESC) LIKE '%CLINICAL%TRIALS%'
                                          OR upper(cra.STUDY_TYPE_DESC) LIKE '%INDIVIDUAL%PATIENT%USE%'
                                          OR upper(cra.STUDY_TYPE_DESC) LIKE '%OTHER%STUDIES%'
                                          OR upper(cra.STUDY_TYPE_DESC) LIKE '%REPORT%FROM%AGGREGATE%ANALYSIS%' THEN
                                         cra.SPONSOR_STUDY_NUM
                                     ELSE
                                         NULL
                                 END AS primary_ind,
                                 CASE
                                     WHEN cra.STUDY_TYPE_DESC IS NOT NULL THEN
                                         cra.STUDY_TYPE_DESC
                                     ELSE
                                         NULL
                                 END AS study_type,
                                 decode(csi.sender_outsourced, 1, 'Yes', 2, 'No',
						  
							 
						  
                                        3, 'Unknown') AS comp_flag,
                                 csi.SENDER_ORGANIZATION_DESC      AS sender_organization,
                                 cpia.ud_number_2             AS age,
                                 cpda.ud_text_20              nda,
                                 cpda.ud_number_10            bla,
                                 cpda.ud_number_11            anda,
                                 1 AS flag_master_case,
                                patient_age_onset_years        AS age_in_years,
								csti.project_num 				project_num,
								csti.study_number				study_number,
								cstdidfc.study_number			PreAnda_study_number,
								cpat.tto_days					tto_days,
								cls1_57.state_yn				as flag_susar,
								F_GET_SUR_SER_UNL_REL('FDA',ci.tenant_id,ci.case_id,ci.version_num,cpifu.prod_rec_num,caei.AE_REC_NUM)                  sur
                          FROM
                              c_identification            ci
                              JOIN max_version              gqcl ON (ci.tenant_id = gqcl.tenant_id
                                                                      AND ci.version_num = gqcl.version_num
                                                                      AND ci.case_id = gqcl.case_id )
                              LEFT JOIN c_identification_fu         cifu ON ( ci.tenant_id = cifu.tenant_id
                                                                      AND ci.version_num = cifu.version_num
                                                                      AND ci.case_id = cifu.case_id )
                              LEFT JOIN c_patient_characteristics   cpc ON ( ci.tenant_id = cpc.tenant_id
                                                                           AND ci.version_num = cpc.version_num
                                                                           AND ci.case_id = cpc.case_id )
                              LEFT JOIN c_ae_identification         caei ON ( ci.tenant_id = caei.tenant_id
                                                                      AND ci.version_num = caei.version_num
                                                                      AND ci.case_id = caei.case_id )
                              LEFT JOIN cdr_conser_evt_label        ccel ON ( caei.tenant_id = ccel.tenant_id
                                                                       AND caei.version_num = ccel.version_num
                                                                       AND caei.case_id = ccel.case_id
                                                                       AND caei.ae_rec_num = ccel.ae_rec_num )
                              LEFT JOIN c_prod_identification_fu    cpifu ON ( ci.tenant_id = cpifu.tenant_id
                                                                            AND ci.version_num = cpifu.version_num
                                                                            AND ci.case_id = cpifu.case_id )
                              LEFT JOIN c_prod_identification       cpi ON ( cpifu.tenant_id = cpi.tenant_id
                                                                       AND cpifu.version_num = cpi.version_num
                                                                       AND cpifu.case_id = cpi.case_id
                                                                       AND cpifu.prod_rec_num = cpi.prod_rec_num )
                              LEFT JOIN c_ae_identification_fu      caefu ON ( caefu.tenant_id = caei.tenant_id
                                                                          AND caefu.version_num = caei.version_num
                                                                          AND caefu.case_id = caei.case_id
                                                                          AND caefu.ae_rec_num = caei.ae_rec_num )
                  LEFT JOIN c_master_addl              cma ON ( ci.tenant_id = cma.tenant_id
                                           AND ci.version_num = cma.version_num
                                           AND ci.case_id = cma.case_id )
                                 LEFT JOIN C_STUDY_IDENTIFICATION    cra ON ( ci.tenant_id = cra.tenant_id
                                             AND ci.version_num = cra.version_num
                                             AND ci.case_id = cra.case_id )
                  LEFT JOIN c_sender_info              csi ON ( ci.tenant_id = csi.tenant_id
                                           AND ci.case_id = csi.case_id
                                           AND ci.version_num = csi.version_num )
                  LEFT JOIN cdr_clob_ae_all_ud         csmq ON ( ci.tenant_id = csmq.tenant_id
                                                AND ci.case_id = csmq.case_id
                                                AND ci.version_num = csmq.version_num )
                  LEFT JOIN c_pat_info_addl            cpia ON ( ci.tenant_id = cpia.tenant_id
                                             AND ci.case_id = cpia.case_id
                                             AND ci.version_num = cpia.version_num )
                  LEFT JOIN c_prod_drugs_addl          cpda ON ( ci.tenant_id = cpda.tenant_id
                                               AND ci.case_id = cpda.case_id
                                               AND ci.version_num = cpda.version_num
                                               AND cpi.prod_rec_num = cpda.prod_rec_num )
                                  LEFT JOIN c_prod_devices_addl           cpdal ON ( ci.tenant_id = cpda.tenant_id
                                                                       AND ci.case_id = cpda.case_id
                                                                       AND ci.version_num = cpda.version_num
                                                                       AND cpi.prod_rec_num = cpda.prod_rec_num )
                              LEFT JOIN vw_product                  vpr ON ( cpi.prod_id_resolved = vpr.product_id and vpr.lang_id = '1' )
                              LEFT JOIN c_drug_identification_fu    cdifu ON ( cpifu.tenant_id = cdifu.tenant_id
                                                                            AND cpifu.version_num = cdifu.version_num
                                                                            AND cpifu.case_id = cdifu.case_id
                                                                            AND cpifu.prod_rec_num = cdifu.prod_rec_num )
                  LEFT OUTER JOIN c_study_identification         csti ON ( csti.tenant_id = ci.tenant_id
                                                    AND csti.version_num = ci.version_num
                                                    AND csti.case_id = ci.case_id )
                  LEFT OUTER JOIN VW_PRE_ANDA_STUDY_NUMBER         cstdidfc ON ( cstdidfc.tenant_id = ci.tenant_id
                                                    AND cstdidfc.version_num = ci.version_num
                                                    AND cstdidfc.case_id = ci.case_id )
                  LEFT OUTER JOIN cdr_prod_ae_tto                cpat ON ( cpat.tenant_id = caei.tenant_id
                                               AND cpat.version_num = caei.version_num
                                               AND cpat.ae_rec_num = caei.ae_rec_num
                                               AND cpat.case_id = caei.case_id
                                               AND cpat.tenant_id = cpifu.tenant_id
                                               AND cpat.version_num = cpifu.version_num
                                               AND cpat.case_id = cpifu.case_id
                                               AND cpat.prod_rec_num = cpifu.prod_rec_num )
                  LEFT OUTER JOIN (select * from vw_clp_state_yn where lang_id = '1')                cls1_57 ON ( cifu.flag_susar = cls1_57.id )
                  LEFT JOIN vw_fda_case_type           vfct ON ( ci.flag_eligible_local_expdtd = vfct.id )
                          WHERE
                              cpifu.flag_primary_prod = 1
                              AND caefu.flag_primary_ae = 1
                      ), clob_data AS (
                          SELECT
                              cdc.case_id         case_id,
                              cdc.version_num     version_num,
                              cdc.tenant_id       tenant_id,
                              cs.case_narrative   case_narrative,
                              replace(replace(replace(cdc.ae_pt, '!@##@!', CHR(13)
                                                                           || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') ae_pt,
                              replace(replace(replace(csda.company_susp_prod_all, '!@##@!', CHR(13)
                                                                                            || CHR(10)), '!@_@!', '-'), '!@.@!'
                                                                                            , ') ') company_susp_prod_all,
                              replace(replace(replace(csda.concomit_prod_all, '!@##@!', CHR(13)
                                                                                        || CHR(10)), '!@_@!', '-'), '!@.@!', ') '
                                                                                        ) concomit_prod_all,
                  CASE
                     WHEN csmq.ud_clob_1 IS NOT NULL THEN
                        replace(replace(csmq.ud_clob_1, '!@.@!', ''), '!@##@!',
                              ',')
                     ELSE
                        NULL
                  END                  AS smq_med,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','CHARACTERISTIC_ALL_CS',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as characteristic_all_cs,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','PAT_MED_COND',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as pat_med_cond_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','PAT_DRUG_HIST',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as coded_drug_name_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','CAUSE_OF_DEATH',cdc.tenant_id,cdc.case_id,cdc.version_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as cd_codd_reptd_all
                          FROM
                              cdr_clob            cdc
                              LEFT JOIN cdr_clob_prod_all   csda ON ( csda.case_id = cdc.case_id
                                                                    AND csda.version_num = cdc.version_num
                                                                    AND csda.tenant_id = cdc.tenant_id )
                              LEFT JOIN c_summary           cs ON ( cs.case_id = cdc.case_id
                                                          AND cs.version_num = cdc.version_num
                                                          AND cs.tenant_id = cdc.tenant_id )
                  LEFT JOIN cdr_clob_ae_all_ud  csmq ON ( cdc.tenant_id = csmq.tenant_id
                                                AND cdc.case_id = csmq.case_id
                                                   AND cdc.version_num = csmq.version_num )
                 where ( cdc.tenant_id,
                     cdc.case_id,
                     cdc.version_num ) IN (
                                  SELECT
                                      tenant_id,
                                      case_id,
                                      version_num
                                  FROM
                                      max_version
                              )
                      )
            ,clob_data_prod as(
            select gt.tenant_id  , gt.version_num  , gt.CASE_ID, gt.prod_rec_num,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','INDICATION',gt.tenant_id  , gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as ind_codd_reptd_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','LOT_NO_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as lot_no_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','THERAPY_DATES_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as therapy_dates_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','DOSE_DETAIL_ALL',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as dose_detail_all,
                  replace(replace(replace(F_GET_CLOB_FIELD_DATA_ALL('FDA','ALL_PTS_SUR',gt.tenant_id  ,gt.CASE_ID, gt.version_num  , gt.prod_rec_num), '!@##@!', CHR(13) || CHR(10)), '!@_@!', '-'), '!@.@!', ') ') as all_pts_sur
            from (select distinct gt.tenant_id  , gt.version_num  , gt.CASE_ID, cpifu.prod_rec_num from max_version gt 
                  inner join c_prod_identification_fu    cpifu on gt.tenant_id = cpifu.tenant_id
                                AND gt.version_num = cpifu.version_num
                                AND gt.case_id = cpifu.case_id 
                  where cpifu.flag_primary_prod = 1
            
            ) gt                         
            )
                      SELECT
                          d.case_id                    "masterCaseId",
                          d.case_num                   "masterCaseNum",
                          d.version_num                "masterVersionNum",
                          d.flag_primary_source_hcp    "CsHcpFlag",
                          d.source_type_desc           "masterRptTypeId",
                          d.date_first_receipt         "masterInitReptDate",
               d.txt_date_first_receipt     "ciTxtDateReceiptInitial",
                          d.date_receipt               "masterFollowupDate",
                          d.listedness_text            "assessListedness",
                          cd.company_susp_prod_all     "masterSuspProdList",
                          cd.concomit_prod_all         "masterConcomitProdList",
                          d.case_outcome_desc          "assessOutcome",
                          d.prod_family_name           "productFamilyId",
                          d.significant_counter        "masterFupNum",
                          d.flag_ver_significant       "masterFlagSt",
                          d.product_name               "productProductId",
                          d.prim_ae_pt                      "masterPrimEvtPrefTerm",
                          d.prim_prod_name             "masterPrimProdName",
                          d.ae_outcome_desc            "eventEvtOutcomeId",
                          d.flag_serious               "assessSeriousness",
                          d.occured_country_desc       "masterCountryId",
                          d.patient_age_group_desc     "patInfoAgeGroupId",
                          d.patient_sex_desc           "patInfoGenderId",
                          d.rechallenge_id             "prodDrugsPosRechallenge",
                          d.date_locked                "masterDateLocked",
               d.txt_date_locked         "cifTxtDateLocked",
                          d.flag_serious_death         "masterFatalFlag",
                          d.mdr_ae_pt                  "eventPrefTerm",
                          d.prod_id_resolved           "cpiProdIdResolved",
                          cd.case_narrative            "narrativeNarrative",
                          cd.ae_pt                     "ccAePt",
                d.case_type "cmadlflagEligibleLocalExpdtd",
                d.completeness_score "caseMasterPvrUdText12",
                d.primary_ind "${csiSponsorStudyNumber}",
                d.study_type "${studyClassificationId}",
                d.comp_flag "vwcpai1FlagCompounded",
                d.sender_organization "csisender_organization",
                cd.smq_med "cdrClobAeAllUdUdClob1",
                             d.flag_combination_product  "deviceComboProduct",
                d.age "casePatInfoPvrUdNumber2",
                d.nda "casProdDrugsPvrUdText20",
                d.bla "caseProdDrugsPvrUdNumber10",
                d.anda "caseProdDrugsPvrUdNumber11",
                          d.flag_master_case           "flagMasterCase",
               d.project_num            "vwstudyProtocolNum",
               d.PreAnda_study_number     "PreAndastudyStudyNum",
               d.tto_days              "dvProdEventTimeOnsetDays",
               d.flag_susar             "masterSusar",
               d.sur                  "ceSerUnlRel",
               cdp.ind_codd_reptd_all     "productIndCoddorReptd",
               cdp.lot_no_all              "productLotNoAllcs",
               cdp.all_pts_sur                 "masterPrefTermSurAll",
               cdp.therapy_dates_all         "productStartStopDateAllcs",
               cdp.dose_detail_all              "productDoseDetailAllcs",
               cd.characteristic_all_cs       "masterCharactersticAllcs",
               cd.pat_med_cond_all              "cprmConditionAll",
               cd.coded_drug_name_all        "ccMedHistDrugAll",
               cd.cd_codd_reptd_all          "ccCoddRptdCauseDeathAll"
                      FROM
                          data        d,
                          clob_data   cd,
               clob_data_prod cdp
                      WHERE
                          d.case_id = cd.case_id
                          AND d.version_num = cd.version_num
                          AND d.tenant_id = cd.tenant_id
                          AND d.case_id = cdp.case_id
                          AND d.version_num = cdp.version_num
                          AND d.tenant_id = cdp.tenant_id
                          AND d.prod_rec_num = cdp.prod_rec_num;
  """
    }

    static product_summary_sql = { Integer productId, periodStartDate, periodEndDate, start, length, disposition, dataSrc, isOutputFormat, orderByCriteria, searchCriteria ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND aa.disposition_id IN (${disposition})"
        }
        def serverSideCriteria = ""
        if (!isOutputFormat) {
            serverSideCriteria = "OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        }


        """
        SELECT * FROM
          (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as filtered_count ,total_count, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,ALERT_CONFIGURATION_ID
          FROM
             (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as total_count , PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,ALERT_CONFIGURATION_ID
                FROM (SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95,EB05,COMMENTS,NAME,requested_by,product_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment, NULL AS ALERT_CONFIGURATION_ID
                      FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME,aa.period_start_date,aa.period_end_date,aa.product_name,aa.requested_by,
                                         MAX(aa.last_updated) OVER (PARTITION BY vaa.validated_signal_id, aa.product_id, aa.pt_code) last_updated,
                                         dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,rconf.selected_data_source,aa.NEW_SPON_COUNT, aa.EB05,
                                         ac.COMMENTS,aa.CUM_SPON_COUNT,aa.EB95
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                         LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE aa.product_id = ${productId}
                                         AND period_start_date = '${periodStartDate}'
                                         AND period_end_date = '${periodEndDate}'
                                         AND rconf.selected_data_source = '${dataSrc}'
                                         ${dispositionCriteria}
                                         )
                                         SELECT aa.ID,vaa.validated_signal_id, mp.DISPLAY_NAME,aa.last_updated, vs.NAME,aa.product_id,aa.requested_by,
                                             aa.pt_code,aa.PT,ROWNUM rn, mp.selected_data_source, aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                         JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date =aa.period_start_date
                                                        AND mp.period_end_date = aa.period_end_date AND mp.disposition_id = aa.disposition_id
                                                        AND mp.last_updated = aa.last_updated))
                                         WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                              UNION 
                              SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT, CUM_SPON_COUNT,EB95,EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment,ALERT_CONFIGURATION_ID
                              FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,ALERT_CONFIGURATION_ID
                                    FROM (WITH max_per AS
                                           (SELECT DISTINCT aa.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME, aa.period_start_date,aa.period_end_date,aa.product_name,
                                             MAX(aa.last_updated) OVER (PARTITION BY aa.NAME, aa.product_id, aa.pt_code) last_updated , rconf.selected_data_source, aa.NEW_SPON_COUNT,aa.CUM_SPON_COUNT,aa.EB95, aa.EB05,  ac.COMMENTS,aa.requested_by,aa.ASSIGNED_TO_ID,aa.EXEC_CONFIGURATION_ID
                                             FROM agg_alert aa
                                             JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                             LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                             LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON aa.product_id = AC.product_id and aa.pt_code = AC.pt_code AND AC.rn = 1
                                             WHERE aa.product_id = ${productId}
                                             AND period_start_date = '${periodStartDate}'
                                             AND period_end_date = '${periodEndDate}'
                                             AND rconf.selected_data_source = '${dataSrc}'
                                             ${dispositionCriteria}
                                             )
                                             SELECT aa.ID,vaa.validated_signal_id,mp.DISPLAY_NAME, aa.last_updated, aa.NAME,aa.product_id, aa.pt_code, aa.PT,ROWNUM rn, mp.selected_data_source
                                              , aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95, mp.COMMENTS,aa.requested_by,aa.EXEC_CONFIGURATION_ID,aa.ASSIGNED_TO_ID, aa.alert_configuration_id
                                              FROM agg_alert aa LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                              JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date = aa.period_start_date
                                                              AND mp.period_end_date = aa.period_end_date
                                                              AND mp.disposition_id = aa.disposition_id
                                                              AND mp.last_updated = aa.last_updated ))
                                             ORDER BY rn
                                    )
                                   WHERE validated_signal_id IS NULL))
                                   ${searchCriteria}
                                   ${orderByCriteria}
                                )
                                   ${serverSideCriteria}
               
        """

    }

    static product_summary_count = { Integer productId, periodStartDate, periodEndDate, disposition, dataSrc ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND aa.disposition_id IN (${disposition})"
        }
        """
         SELECT count(*) cnt
                FROM (SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT, EB05,CUM_SPON_COUNT,EB95, COMMENTS,NAME,requested_by,product_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment
                      FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME,aa.period_start_date,aa.period_end_date,aa.product_name,aa.requested_by,
                                         MAX(aa.last_updated) OVER (PARTITION BY vaa.validated_signal_id, aa.product_id, aa.pt_code) last_updated,
                                         dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,rconf.selected_data_source,aa.NEW_SPON_COUNT, aa.EB05,
                                         ac.COMMENTS,aa.CUM_SPON_COUNT,aa.EB95
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                         LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE aa.product_id = ${productId}
                                         AND period_start_date = '${periodStartDate}'
                                         AND period_end_date = '${periodEndDate}'
                                         AND rconf.selected_data_source = '${dataSrc}'
                                         ${dispositionCriteria}
                                         )
                                         SELECT aa.ID,vaa.validated_signal_id, mp.DISPLAY_NAME,aa.last_updated, vs.NAME,aa.product_id,aa.requested_by,
                                             aa.pt_code,aa.PT,ROWNUM rn, mp.selected_data_source, aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                         FROM agg_alert aa 
                                         LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                                                                  LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                         JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date =aa.period_start_date
                                                        AND mp.period_end_date = aa.period_end_date AND mp.disposition_id = aa.disposition_id
                                                        AND mp.last_updated = aa.last_updated))
                                         WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                              UNION 
                              SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, selected_data_source, NEW_SPON_COUNT, CUM_SPON_COUNT,EB95,EB05, COMMENTS,NAME,requested_by,product_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment
                              FROM (SELECT ID, validated_signal_id,DISPLAY_NAME, last_updated, NAME, product_id,pt_code, PT, rn, selected_data_source, NEW_SPON_COUNT,CUM_SPON_COUNT,EB95, EB05, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID
                                    FROM (WITH max_per AS
                                           (SELECT DISTINCT aa.NAME,aa.product_id,aa.pt_code, aa.PT, aa.disposition_id,disp.DISPLAY_NAME, aa.period_start_date,aa.period_end_date,aa.product_name,
                                             MAX(aa.last_updated) OVER (PARTITION BY aa.NAME, aa.product_id, aa.pt_code) last_updated , rconf.selected_data_source, aa.NEW_SPON_COUNT,aa.CUM_SPON_COUNT,aa.EB95, aa.EB05,  ac.COMMENTS,aa.requested_by,aa.ASSIGNED_TO_ID,aa.EXEC_CONFIGURATION_ID
                                             FROM agg_alert aa
                                             JOIN RCONFIG rconf ON (aa.alert_configuration_id = rconf.id)
                                             LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
                                             LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON aa.product_id = AC.product_id and aa.pt_code = AC.pt_code AND AC.rn = 1
                                             WHERE aa.product_id = ${productId}
                                             AND period_start_date = '${periodStartDate}'
                                             AND period_end_date = '${periodEndDate}'
                                             AND rconf.selected_data_source = '${dataSrc}'
                                             ${dispositionCriteria}
                                             )
                                             SELECT aa.ID,vaa.validated_signal_id,mp.DISPLAY_NAME, aa.last_updated, aa.NAME,aa.product_id, aa.pt_code, aa.PT,ROWNUM rn, mp.selected_data_source
                                              , aa.NEW_SPON_COUNT, aa.EB05,aa.CUM_SPON_COUNT,aa.EB95, mp.COMMENTS,aa.requested_by,aa.EXEC_CONFIGURATION_ID,aa.ASSIGNED_TO_ID
                                              FROM agg_alert aa LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                                              JOIN max_per mp ON ( mp.product_name = aa.product_name AND mp.period_start_date = aa.period_start_date
                                                              AND mp.period_end_date = aa.period_end_date
                                                              AND mp.disposition_id = aa.disposition_id
                                                              AND mp.last_updated = aa.last_updated ))
                                             ORDER BY rn
                                    )
                                   WHERE validated_signal_id IS NULL)
      """

    }

    static product_summary_evdas_sql = { productName, periodStartDate, periodEndDate, start, length, disposition, isOutputFormat, orderByCriteria, searchCriteria, dataSource ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND ea.disposition_id IN (${disposition})"
        }
        def serverSideCriteria = ""
        if (!isOutputFormat) {
            serverSideCriteria = "OFFSET ${start} ROWS FETCH NEXT ${length} ROWS ONLY"
        }

        """
          SELECT * FROM
          (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as filtered_count ,total_count, PT, DISPLAY_NAME, NEW_SPONT AS NEW_SPON_COUNT,TOT_SPONT AS CUM_SPON_COUNT, COMMENTS,NAME,requested_by,substance_id AS PRODUCT_ID,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,selected_data_source,ALERT_CONFIGURATION_ID
       
          FROM 
          (SELECT ID, validated_signal_id, ROWNUM rn,count(*) over () as total_count ,PT, DISPLAY_NAME, NEW_SPONT,TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,generic_comment,selected_data_source,ALERT_CONFIGURATION_ID
                FROM (SELECT DISTINCT ID, validated_signal_id,PT, DISPLAY_NAME, NEW_SPONT,TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment,'${
            dataSource
        }' AS selected_data_source, NULL AS ALERT_CONFIGURATION_ID
                      FROM (SELECT ID,validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code,PT, rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,ea.requested_by,
                                         MAX(ea.last_updated) OVER (PARTITION BY vaa.validated_signal_id, ea.substance_id, ea.pt_code) last_updated,
                                          dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,ea.NEW_SPONT,
                                         ac.COMMENTS,ea.TOT_SPONT
                                         FROM evdas_alert ea 
                                         LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE substance = '${productName}'
                                         AND period_start_date ='${periodStartDate}'
                                         AND period_end_date ='${periodEndDate}'
                                         ${dispositionCriteria}
                                   )
                                   SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, vs.NAME,ea.substance_id, ea.requested_by,ea.pt_code,ea.PT,ROWNUM rn,
                                     ea.NEW_SPONT,ea.TOT_SPONT,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                   FROM evdas_alert ea 
                                   LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                   LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                   JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date =ea.period_start_date
                                                        AND mp.period_end_date = ea.period_end_date AND mp.disposition_id = ea.disposition_id
                                                        AND mp.last_updated = ea.last_updated))
                                   WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                            UNION
                            SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, NEW_SPONT, TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment,'${
            dataSource
        }' AS selected_data_source,ALERT_CONFIGURATION_ID
                            FROM (SELECT ID, validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code, PT,rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,ALERT_CONFIGURATION_ID
                            FROM (WITH max_per AS
                                  (SELECT DISTINCT ea.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,
                                   MAX(ea.last_updated) OVER (PARTITION BY ea.NAME, ea.substance_id, ea.pt_code) last_updated, ea.NEW_SPONT,ea.TOT_SPONT, ac.COMMENTS,ea.requested_by,ea.ASSIGNED_TO_ID,ea.EXEC_CONFIGURATION_ID
                                   FROM evdas_alert ea
                                   LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                   LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON ea.substance_id = AC.product_id and ea.pt_code = AC.pt_code AND AC.rn = 1
                                   WHERE substance = '${productName}'
                                   AND period_start_date = '${periodStartDate}'
                                   AND period_end_date = '${periodEndDate}'
                                   ${dispositionCriteria}
                                  )
                                  SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, ea.NAME,ea.substance_id, ea.pt_code,ROWNUM rn,ea.PT,
                                   ea.NEW_SPONT,ea.TOT_SPONT, mp.COMMENTS,ea.requested_by,ea.EXEC_CONFIGURATION_ID,ea.ASSIGNED_TO_ID, ea.alert_configuration_id
                                  FROM evdas_alert ea LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                  JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date = ea.period_start_date
                                                       AND mp.period_end_date = ea.period_end_date
                                                       AND mp.disposition_id = ea.disposition_id
                                                       AND mp.last_updated = ea.last_updated ))
                                  ORDER BY rn)
                                  WHERE validated_signal_id IS NULL))
                                  ${searchCriteria}
                                  ${orderByCriteria}
                                )
                                ${serverSideCriteria}
                                 
        """
    }

    static product_summary_evdas_count = { productName, periodStartDate, periodEndDate, disposition, dataSource ->
        def dispositionCriteria = ""
        if (disposition) {
            dispositionCriteria = "AND ea.disposition_id IN (${disposition})"
        }
        """
           SELECT count(*) cnt
                FROM (SELECT DISTINCT ID, validated_signal_id,PT, DISPLAY_NAME, NEW_SPONT,TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,NULL AS EXEC_CONFIGURATION_ID,NULL AS ASSIGNED_TO_ID,generic_comment,'${
            dataSource
        }' AS selected_data_source
                      FROM (SELECT ID,validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code,PT, rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,generic_comment
                            FROM (WITH max_per AS
                                   (SELECT DISTINCT vs.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,ea.requested_by,
                                         MAX(ea.last_updated) OVER (PARTITION BY vaa.validated_signal_id, ea.substance_id, ea.pt_code) last_updated,
                                          dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment,ea.NEW_SPONT,
                                         ac.COMMENTS,ea.TOT_SPONT
                                         FROM evdas_alert ea 
                                         LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                         LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                                         LEFT JOIN VALIDATED_ALERT_COMMENTS vac ON (vs.ID = vac.VALIDATED_SIGNAL_ID)
                                         LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                         LEFT JOIN ALERT_COMMENT AC on (AC.id = vac.COMMENT_ID)
                                         WHERE substance = '${productName}'
                                         AND period_start_date ='${periodStartDate}'
                                         AND period_end_date ='${periodEndDate}'
                                         ${dispositionCriteria}      
                                   )
                                   SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, vs.NAME,ea.substance_id, ea.requested_by,ea.pt_code,ea.PT,ROWNUM rn,
                                     ea.NEW_SPONT,ea.TOT_SPONT,
                                             mp.COMMENTS,dbms_lob.substr(vs.generic_comment,dbms_lob.getlength(vs.generic_comment),1) generic_comment
                                   FROM evdas_alert ea 
                                   LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                   LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID

                                   JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date =ea.period_start_date
                                                        AND mp.period_end_date = ea.period_end_date AND mp.disposition_id = ea.disposition_id
                                                        AND mp.last_updated = ea.last_updated))
                                   WHERE validated_signal_id IS NOT NULL
                                  ORDER BY rn)
                            UNION
                            SELECT DISTINCT ID, validated_signal_id, PT, DISPLAY_NAME, NEW_SPONT, TOT_SPONT, COMMENTS,NAME,requested_by,substance_id,pt_code,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID,NULL AS generic_comment,'${
            dataSource
        }' AS selected_data_source
                            FROM (SELECT ID, validated_signal_id,DISPLAY_NAME,last_updated, NAME, substance_id,pt_code, PT,rn, NEW_SPONT,TOT_SPONT, COMMENTS,requested_by,EXEC_CONFIGURATION_ID,ASSIGNED_TO_ID
                            FROM (WITH max_per AS
                                  (SELECT DISTINCT ea.NAME,ea.substance_id,ea.pt_code,ea.PT,ea.disposition_id,disp.DISPLAY_NAME,ea.period_start_date,ea.period_end_date,ea.substance,
                                   MAX(ea.last_updated) OVER (PARTITION BY ea.NAME, ea.substance_id, ea.pt_code) last_updated, ea.NEW_SPONT,ea.TOT_SPONT, ac.COMMENTS,ea.requested_by,ea.ASSIGNED_TO_ID,ea.EXEC_CONFIGURATION_ID
                                   FROM evdas_alert ea
                                   LEFT JOIN DISPOSITION disp on (ea.disposition_id = disp.ID)
                                   LEFT JOIN
                                             (SELECT AC.* , ROW_NUMBER() OVER (PARTITION BY PRODUCT_ID,PT_CODE ORDER BY AC.ID DESC) AS rn FROM ALERT_COMMENT AC)
                                              AC ON ea.substance_id = AC.product_id and ea.pt_code = AC.pt_code AND AC.rn = 1
                                   WHERE substance = '${productName}'
                                   AND period_start_date = '${periodStartDate}'
                                   AND period_end_date = '${periodEndDate}'
                                   ${dispositionCriteria}
                                  )
                                  SELECT ea.ID,vaa.validated_signal_id,mp.DISPLAY_NAME,ea.last_updated, ea.NAME,ea.substance_id, ea.pt_code,ROWNUM rn,ea.PT,
                                   ea.NEW_SPONT,ea.TOT_SPONT, mp.COMMENTS,ea.requested_by,ea.EXEC_CONFIGURATION_ID,ea.ASSIGNED_TO_ID
                                  FROM evdas_alert ea LEFT JOIN validated_evdas_alerts vaa ON (ea.ID = vaa.evdas_alert_id)
                                  JOIN max_per mp ON ( mp.substance = ea.substance AND mp.period_start_date = ea.period_start_date
                                                       AND mp.period_end_date = ea.period_end_date
                                                       AND mp.disposition_id = ea.disposition_id
                                                       AND mp.last_updated = ea.last_updated ))
                                  ORDER BY rn)
                                  WHERE validated_signal_id IS NULL) 
        """

    }

    static add_case_faers_sql = { List<String> cl ->
        String inClause = ''
        if (cl && cl.size() <= 1000) {
            inClause = "(${cl.join(",")})"
        } else if (cl && cl.size() > 1000) {
            inClause = cl.collate(1000).join(" OR case_num IN ").replace("[", "(").replace("]", ")")
        }
        """
        WITH max_version AS
     (SELECT   MAX (version_num) version_num, case_id
          FROM c_identification
      GROUP BY case_id)
       SELECT ci.case_id "masterCaseId_28", ci.case_num "masterCaseNum_0", ci.version_num "masterVersionNum_1",
       cifu.flag_primary_source_hcp "reportersHcpFlag_2",
       ci.source_type_desc "masterRptTypeId_3",
       ci.date_first_receipt "masterInitReptDate_4",
       cifu.date_receipt "masterFollowupDate_5",
       cdrc.ae_pt_all "masterPrefTermAll_6",
       cifu.case_outcome_desc "assessOutcome_7",
       vll.listedness_text "eventConserCoreListedness_8",
       cpifu.prod_family_name "productFamilyId_9",
       cifu.significant_counter "masterFupNum_10",
       DECODE (cifu.flag_ver_significant, 1, 'Yes', 'No') "masterFlagSt_11",
       vpr.product_name "productProductId_27",
       cifu.prim_evt_pref_term "masterPrimEvtPrefTerm_13",
       cifu.prim_prod_name "masterPrimProdName_14",
       caei.ae_outcome_desc "eventEvtOutcomeId_15",
       case when 
        ci.flag_serious_hosp = 1 or
        ci.flag_serious_other_med_imp = 1 or
        ci.flag_serious_death = 1 or
        ci.flag_serious_threat = 1 or
        ci.flag_serious_disable = 1 or
        ci.flag_serious_cong_anom = 1 or
        ci.flag_serious_int_req = 1
        then 'Serious' 
        else 'Non Serious'
        end as "assessSeriousness_16",
       csda.susp_prod_info "masterSuspProdAgg_17",
       cdrc.concomit_prod_all "masterConcomitProdList_32",
       ci.occured_country_desc "masterCountryId_19",
       cpc.patient_age_group_desc "patInfoAgeGroupId_20",
       cpc.patient_sex_desc "patInfoGenderId_21",
       cdifu.rechallenge_id "prodDrugsPosRechallenge_22",
       cifu.date_locked "masterDateLocked_23",
       decode(ci.flag_serious_death,1,'Yes', 'No') "masterFatalFlag_74",
       caei.mdr_ae_pt "eventPrefTerm_25",
       cpi.prod_id_resolved "cpiProdIdResolved_26",
       null as "narrativeNarrative_29",
       null as "ccAePt_30"
  FROM max_version mv JOIN c_identification ci
       ON (mv.version_num = ci.version_num AND mv.case_id = ci.case_id)
       LEFT JOIN c_identification_fu cifu
       ON (    ci.tenant_id = cifu.tenant_id
           AND ci.version_num = cifu.version_num
           AND ci.case_id = cifu.case_id
          )
       LEFT JOIN c_patient_characteristics cpc
       ON (    ci.tenant_id = cpc.tenant_id
           AND ci.version_num = cpc.version_num
           AND ci.case_id = cpc.case_id
          )
       LEFT JOIN c_ae_identification caei
       ON (    ci.tenant_id = caei.tenant_id
           AND ci.version_num = caei.version_num
           AND ci.case_id = caei.case_id
           AND mdr_ae_pt = prim_evt_pref_term
          )
       LEFT JOIN cdr_clob cdrc
       ON (    ci.tenant_id = cdrc.tenant_id
           AND ci.version_num = cdrc.version_num
           AND ci.case_id = cdrc.case_id
          )
       LEFT JOIN c_prod_identification_fu cpifu
       ON (    ci.tenant_id = cpifu.tenant_id
           AND ci.version_num = cpifu.version_num
           AND ci.case_id = cpifu.case_id
           AND prod_name_resolved = prim_prod_name
          )
       LEFT JOIN c_prod_identification cpi
       ON (    cpifu.tenant_id = cpi.tenant_id
           AND cpifu.version_num = cpi.version_num
           AND cpifu.case_id = cpi.case_id
           AND cpifu.prod_rec_num = cpi.prod_rec_num
          )
       LEFT JOIN vw_product vpr
       ON(cpi.prod_id_resolved=vpr.product_id and vpr.lang_id = '1') 
       LEFT JOIN c_drug_identification_fu cdifu
       ON (    cpifu.tenant_id = cdifu.tenant_id
           AND cpifu.version_num = cdifu.version_num
           AND cpifu.case_id = cdifu.case_id
           AND cpifu.prod_rec_num = cdifu.prod_rec_num
          )
       LEFT JOIN cdr_conser_evt_label ccel
       ON (    caei.tenant_id = ccel.tenant_id
           AND caei.version_num = ccel.version_num
           AND caei.case_id = ccel.case_id
           AND caei.ae_rec_num = ccel.ae_rec_num
          )
       LEFT JOIN c_susp_dose_agg csda
       ON (    ci.tenant_id = csda.tenant_id
           AND ci.version_num = csda.version_num
           AND ci.case_id = csda.case_id
          )
       LEFT JOIN vw_llist_listedness vll
       ON (ccel.conser_listedness = vll.listedness_id and vll.lang_id = '1')
        WHERE case_num IN ${inClause}
        """
    }

    static child_case_max_veriosn = { caseNumber ->
        """
        select max(Version_num) MAX_VERSION from v_c_identification where case_num='${caseNumber}'
        group by case_num
        """
    }
    static default_followup_number = { caseNumber, version ,caseType ->
        """
        SELECT (case when significant_counter > 0 then (significant_counter -1) else significant_counter end) significant_counter
        FROM c_identification_fu
        cifu JOIN v_c_identification ci
        ON(cifu.tenant_id = ci.tenant_id
                AND cifu.case_id = ci.case_id
                AND cifu.version_num = ci.version_num
        )
        WHERE ci.case_num = '${caseNumber}' AND ci.version_num = ${version} AND ci.flag_Master_Case = ${caseType}
        """
    }

    static signal_alert_ids = { productIdAndPtCode, execConfigId, prevExecConfigId ->
        """
         with t1 as 
           (SELECT VSID,PRODUCT_ID,PT_CODE,rn 
           FROM (SELECT vs.ID VSID, PRODUCT_ID ,PT_CODE  ,row_number() OVER(PARTITION BY aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, vs.ID ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                 WHERE (PRODUCT_ID ,PT_CODE,SOC,PT) IN (${productIdAndPtCode}) AND aa.EXEC_CONFIGURATION_ID = ${prevExecConfigId}
                 )
           where rn = '1'),
           t2 as 
                (select  aa.ID alertId,PRODUCT_ID ,PT_CODE
                 FROM AGG_ALERT aa
                 WHERE aa.EXEC_CONFIGURATION_ID = ${execConfigId} and (PRODUCT_ID ,PT_CODE,SOC,PT) IN (${productIdAndPtCode})
                 )
         select ALERTID,VSID from t1 join t2 on (t1.PRODUCT_ID = t2.PRODUCT_ID and t1.PT_CODE = t2.PT_CODE)
       
       
        """
    }
    static single_alert_actions = { execConfigId, prevExecConfigId ->
        """
            with t1 as
            (SELECT distinct SCA.CASE_NUMBER,saa.ACTION_ID as actionId
            FROM SINGLE_ALERT_ACTIONS saa
            INNER JOIN ACTIONS actions ON saa.ACTION_ID=actions.id
            INNER JOIN SINGLE_CASE_ALERT SCA on saa.SINGLE_CASE_ALERT_ID = SCA.ID
            WHERE SCA.EXEC_CONFIG_ID = ${prevExecConfigId} and actions.ACTION_STATUS != 'Closed'
            ),
            t2 as
            (SELECT distinct SCA.CASE_NUMBER,SCA.ID as alertId
            From SINGLE_CASE_ALERT SCA
            WHERE SCA.EXEC_CONFIG_ID = ${execConfigId}
            )
            select alertId,actionId from t1 join t2 on (t1.CASE_NUMBER  = t2.CASE_NUMBER)
        """
    }
    static agg_alert_actions = { execConfigId, prevExecConfigId ->
        """
        WITH t1 AS (
            SELECT DISTINCT AGG.PRODUCT_ID, AGG.PT_CODE, COALESCE(AGG.SMQ_CODE, '0') AS smqCode, aaa.ACTION_ID AS actionId
            FROM AGG_ALERT_ACTIONS aaa
            INNER JOIN ACTIONS actions ON aaa.ACTION_ID = actions.id
            INNER JOIN AGG_ALERT AGG ON aaa.AGG_ALERT_ID = AGG.ID
            WHERE AGG.EXEC_CONFIGURATION_ID = ${prevExecConfigId} 
              AND actions.ACTION_STATUS != 'Closed'
        ),
        t2 AS (
            SELECT DISTINCT AGG.PRODUCT_ID, AGG.PT_CODE, COALESCE(AGG.SMQ_CODE, '0') AS smqCode, AGG.ID AS alertId
            FROM AGG_ALERT AGG
            WHERE AGG.EXEC_CONFIGURATION_ID = ${execConfigId}
        )
        SELECT alertId, actionId
        FROM t1 
        JOIN t2 ON t1.PRODUCT_ID = t2.PRODUCT_ID 
                 AND t1.PT_CODE = t2.PT_CODE 
                 AND t1.smqCode = t2.smqCode
    """
    }
    static evdas_alert_actions = { execConfigId, prevExecConfigId ->
        """
            with t1 as
            (SELECT distinct EA.SUBSTANCE_ID,EA.PT_CODE,eaa.ACTION_ID as actionId
            FROM EVDAS_ALERT_ACTIONS eaa
            INNER JOIN ACTIONS actions ON eaa.ACTION_ID=actions.id
            INNER JOIN EVDAS_ALERT EA on eaa.EVDAS_ALERT_ID = EA.ID
            WHERE EA.EXEC_CONFIGURATION_ID = ${prevExecConfigId} and actions.ACTION_STATUS != 'Closed'
            ),
            t2 as
            (SELECT distinct EA.SUBSTANCE_ID,EA.PT_CODE,EA.ID as alertId
            From EVDAS_ALERT EA
            WHERE EA.EXEC_CONFIGURATION_ID = ${execConfigId}
            )
            select alertId,actionId from t1 join t2 on (t1.SUBSTANCE_ID = t2.SUBSTANCE_ID and t1.PT_CODE = t2.PT_CODE)
        """
    }
    static lit_alert_actions = { execConfigId, prevExecConfigId ->
        """
            with t1 as
            (SELECT distinct LA.ARTICLE_ID,laa.ACTION_ID as actionId
            FROM LIT_ALERT_ACTIONS laa
            INNER JOIN ACTIONS actions ON laa.ACTION_ID=actions.id
            INNER JOIN LITERATURE_ALERT LA on laa.LITERATURE_ALERT_ID = LA.ID
            WHERE LA.EX_LIT_SEARCH_CONFIG_ID = ${prevExecConfigId} and actions.ACTION_STATUS != 'Closed'
            ),
            t2 as
            (SELECT distinct LA.ARTICLE_ID,LA.ID as alertId
            From LITERATURE_ALERT LA
            WHERE LA.EX_LIT_SEARCH_CONFIG_ID = ${execConfigId}
            )
            select alertId,actionId from t1 join t2 on (t1.ARTICLE_ID = t2.ARTICLE_ID)
        """

    }

    static single_alert_actions_count = { execConfigId ->
        """
            UPDATE SINGLE_CASE_ALERT sca
            SET ACTION_COUNT = (
                SELECT COUNT(saa.ACTION_ID)
                FROM SINGLE_ALERT_ACTIONS saa
                WHERE saa.SINGLE_CASE_ALERT_ID = sca.ID
            )
            WHERE sca.EXEC_CONFIG_ID = ${execConfigId}
        """
    }

    static agg_alert_actions_count = { execConfigId ->
        """
        UPDATE AGG_ALERT sca
        SET ACTION_COUNT = (
            SELECT COUNT(saa.ACTION_ID)
            FROM AGG_ALERT_ACTIONS saa
            WHERE saa.AGG_ALERT_ID = sca.ID ::numeric
        )
        WHERE sca.EXEC_CONFIGURATION_ID = ${execConfigId} ::numeric
    """
    }
    static evdas_alert_actions_count = { execConfigId ->
        """
            UPDATE EVDAS_ALERT sca
            SET ACTION_COUNT = (
                SELECT COUNT(saa.ACTION_ID)
                FROM EVDAS_ALERT_ACTIONS saa
                WHERE saa.EVDAS_ALERT_ID = sca.ID
            )
            WHERE sca.EXEC_CONFIGURATION_ID = ${execConfigId}
        """
    }
    static lit_alert_actions_count = { execConfigId ->
        """
            UPDATE LITERATURE_ALERT sca
            SET ACTION_COUNT = (
                SELECT COUNT(saa.ACTION_ID)
                FROM LIT_ALERT_ACTIONS saa
                WHERE saa.LITERATURE_ALERT_ID = sca.ID
            )
            WHERE sca.EX_LIT_SEARCH_CONFIG_ID = ${execConfigId}
        """
    }
    static insert_icr_actions_sql = {
        "INSERT INTO SINGLE_ALERT_ACTIONS(SINGLE_CASE_ALERT_ID, ACTION_ID, IS_RETAINED) VALUES(?, ?, ?)"
    }
    static insert_agg_actions_sql = {
        "INSERT INTO AGG_ALERT_ACTIONS(AGG_ALERT_ID, ACTION_ID, IS_RETAINED) VALUES(?, ?, ?)"
    }
    static insert_evdas_actions_sql = {
        "INSERT INTO EVDAS_ALERT_ACTIONS(EVDAS_ALERT_ID, ACTION_ID, IS_RETAINED) VALUES(?, ?, ?)"
    }
    static insert_lit_actions_sql = {
        "INSERT INTO LIT_ALERT_ACTIONS(LITERATURE_ALERT_ID, ACTION_ID, IS_RETAINED) VALUES(?, ?, ?)"
    }

    static signal_ids_auto_routing = { productIdAndPtCode, execConfigId, prevExecConfigId ->
        """
           SELECT VSID
           FROM (SELECT vs.ID VSID, PRODUCT_ID ,PT_CODE  ,row_number() OVER(PARTITION BY aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, vs.ID ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                 WHERE (PRODUCT_ID ,PT_CODE,SOC,PT) IN (${productIdAndPtCode}) AND aa.EXEC_CONFIGURATION_ID = ${prevExecConfigId})
           WHERE rn = '1'
        """
    }

    static signal_alert_ids_single = { caseNumberAndProductFamily, execConfigId, configId ->
        """
         with t1 as 
           (SELECT VSID,CASE_NUMBER,PRODUCT_FAMILY,rn 
           FROM (SELECT vs.ID VSID, CASE_NUMBER , PRODUCT_FAMILY ,row_number() OVER(PARTITION BY sca.CASE_NUMBER , sca.PRODUCT_FAMILY, vs.ID ORDER by sca.ID) rn
                 FROM SINGLE_CASE_ALERT sca
                 JOIN VALIDATED_SINGLE_ALERTS vsa ON (sca.ID = vsa.SINGLE_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vsa.VALIDATED_SIGNAL_ID
                 WHERE (CASE_NUMBER ,PRODUCT_FAMILY) IN (${caseNumberAndProductFamily}) AND sca.alert_configuration_id = ${configId}
                 )
           where rn = '1'),
           t2 as 
                (select  sca.ID alertId,CASE_NUMBER,PRODUCT_FAMILY
                 FROM SINGLE_CASE_ALERT sca
                 WHERE sca.EXEC_CONFIG_ID = ${execConfigId} and (CASE_NUMBER ,PRODUCT_FAMILY) IN (${caseNumberAndProductFamily})
                 )
         select ALERTID,VSID from t1 join t2 on (t1.CASE_NUMBER = t2.CASE_NUMBER and t1.PRODUCT_FAMILY = t2.PRODUCT_FAMILY)
       
      
        """
    }

    static signal_alert_ids_evdas = { substanceIdAndPtCode, execConfigId, prevExecConfigId ->
        """
         with t1 as 
           (SELECT VSID,SUBSTANCE_ID,PT_CODE,rn 
           FROM (SELECT vs.ID VSID, SUBSTANCE_ID ,PT_CODE  ,row_number() OVER(PARTITION BY ea.SUBSTANCE_ID , ea.PT_CODE, vs.ID ORDER by ea.ID) rn
                 FROM EVDAS_ALERT ea
                 JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                 WHERE (SUBSTANCE_ID ,PT_CODE) IN (${substanceIdAndPtCode}) AND ea.EXEC_CONFIGURATION_ID = ${prevExecConfigId}
                 )
           where rn = '1'),
           t2 as 
                (select  ea.ID alertId,SUBSTANCE_ID ,PT_CODE
                 FROM EVDAS_ALERT ea
                 WHERE ea.EXEC_CONFIGURATION_ID = ${execConfigId} and (SUBSTANCE_ID ,PT_CODE) IN (${
            substanceIdAndPtCode
        })
                 )
         select ALERTID,VSID from t1 join t2 on (t1.SUBSTANCE_ID = t2.SUBSTANCE_ID and t1.PT_CODE = t2.PT_CODE)
       
       
        """
    }

    static signal_ids_evdas_auto_routing = { substanceIdAndPtCode, execConfigId, prevExecConfigId ->
        """
           SELECT VSID 
           FROM (SELECT vs.ID VSID, SUBSTANCE_ID ,PT_CODE  ,row_number() OVER(PARTITION BY ea.SUBSTANCE_ID , ea.PT_CODE, vs.ID ORDER by ea.ID) rn
                 FROM EVDAS_ALERT ea
                 JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                 WHERE (SUBSTANCE_ID ,PT_CODE) IN (${substanceIdAndPtCode}) AND ea.EXEC_CONFIGURATION_ID = ${prevExecConfigId})
           WHERE rn = '1'
        """
    }

    static signal_pecs = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
         With t1 as (
           SELECT VSID,rn 
           FROM (SELECT vs.ID VSID,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, aa.alert_configuration_id ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                 JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIGURATION_ID)
                 where erc.is_Deleted = false
                 and erc.is_Enabled = true
                 and erc.adhoc_run = false
                  
               )
           where rn = '1'
           union all
           SELECT VSID,rn 
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.SUBSTANCE_ID , ea.PT_CODE,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM EVDAS_ALERT ea
                      JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_EVDAS_CONFIG erc ON (erc.ID = ea.EXEC_CONFIGURATION_ID)
                      where erc.is_Deleted = false
                      and erc.is_Enabled =true
                      and erc.adhoc_run = false
                       )
                where rn = '1'
            )
            select t1.vsid as signalId,count(t1.vsid) as count 
            from t1
            group by t1.vsid
        """
    }
    static agg_archived_pecs = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
       SELECT VSID as signalId,count(*) as count
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, aa.alert_configuration_id ORDER by aa.ID) rn
                      FROM ARCHIVED_AGG_ALERT aa
                      JOIN VALIDATED_ARCHIVED_ACA vaa ON (aa.ID = vaa.ARCHIVED_ACA_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIGURATION_ID)
                      where erc.is_Deleted = false
                      and erc.is_Enabled =true
                      and erc.adhoc_run = false
                      )
                WHERE rn ='1'
                group by VSID
             
        """
    }

    static signal_case_count = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
       SELECT VSID as signalId,count(*) as count
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.case_number , ea.product_family,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM SINGLE_CASE_ALERT ea
                      JOIN VALIDATED_SINGLE_ALERTS vea ON (ea.ID = vea.SINGLE_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_RCONFIG erc ON (erc.id = ea.EXEC_CONFIG_ID)
                      where erc.is_Deleted = false
                      and (erc.is_enabled =true or erc.is_standalone = true)
                      and erc.adhoc_run = false
                      ) as subquery
                WHERE rn ='1'
                group by VSID
             
        """
    }

    static signal_case_archived_count = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'vs.ID').toString()
        """
       SELECT VSID as signalId,count(*) as count
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.case_number , aa.product_family,aa.alert_configuration_id ORDER by aa.ID) rn
                      FROM ARCHIVED_SINGLE_CASE_ALERT aa
                      JOIN VALIDATED_ARCHIVED_SCA vaa ON (aa.ID = vaa.ARCHIVED_SCA_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID AND $inCriteriaWhereClause
                      JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIG_ID)
                      where erc.is_Deleted = false
                      and erc.is_Enabled =true
                      and erc.adhoc_run = false
                      ) as subquery
                WHERE rn ='1'
                group by VSID
             
        """
    }

    static signal_pec_validated_signal_count = { signalId ->
        """
           SELECT 'PVACOUNT' as abc ,count(*)
           FROM (SELECT vs.ID VSID,row_number() OVER(PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID , aa.PT_CODE,aa.SOC, aa.alert_configuration_id ORDER by aa.ID) rn
                 FROM AGG_ALERT aa
                 JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
                 JOIN EX_RCONFIG erc ON (erc.id = aa.EXEC_CONFIGURATION_ID)
                 where erc.is_Deleted = false
                 and erc.is_Enabled =true
                 and erc.adhoc_run = false
                 and vs.ID = ${signalId}    
              ) as subquery
              group by rn
              having rn ='1'
           union all
            
          SELECT 'EVDASCOUNT' as abc ,count(*)
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.SUBSTANCE_ID , ea.PT_CODE,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM EVDAS_ALERT ea
                      JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                      JOIN EX_EVDAS_CONFIG erc ON (erc.ID = ea.EXEC_CONFIGURATION_ID)
                      where erc.is_Deleted = false
                      and erc.is_Enabled =true
                      and erc.adhoc_run = false
                      and vs.ID = ${signalId}    
                       ) as subquery2
                group by rn
              having rn ='1'
              union all
            
          SELECT 'CASECOUNT' as abc ,count(*)
                FROM (SELECT vs.ID VSID ,row_number() OVER(PARTITION BY vea.validated_signal_id, ea.case_number , ea.product_family,ea.alert_configuration_id ORDER by ea.ID) rn
                      FROM SINGLE_CASE_ALERT ea
                      JOIN VALIDATED_SINGLE_ALERTS vea ON (ea.ID = vea.SINGLE_ALERT_ID)
                      JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
                      JOIN EX_RCONFIG erc ON (erc.id = ea.EXEC_CONFIG_ID)
                      where erc.is_Deleted = false
                      and erc.is_Enabled =true
                      and erc.adhoc_run = false
                      and vs.ID = ${signalId} 
                       ) as subquery3
                group by rn
              having rn ='1'
            
        """
    }


    static signal_concepts_map = { signalId ->
        """
    WITH t1 AS (
        SELECT VSID, rn, MEDICAL_CONCEPTS_ID, mname, 'aggregateAlerts' AS DATA_SOURCE
        FROM (
            SELECT aa.ID VSID, MEDICAL_CONCEPTS_ID, mc.name mname,
                   ROW_NUMBER() OVER (PARTITION BY vaa.validated_signal_id, aa.PRODUCT_ID, aa.PT_CODE, aa.SOC, aa.alert_configuration_id ORDER BY aa.ID) rn
            FROM AGG_ALERT aa
            JOIN validated_agg_alerts vaa ON aa.ID = vaa.agg_alert_id
            JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
            JOIN AGG_SIGNAL_CONCEPTS ascs ON aa.ID = ascs.AGG_ALERT_ID
            JOIN MEDICAL_CONCEPTS mc ON mc.ID = ascs.MEDICAL_CONCEPTS_ID
            WHERE vs.ID = ${signalId}
        ) AS subquery_agg_alerts
        WHERE rn = 1
        
        UNION ALL
        
        SELECT VSID, rn, MEDICAL_CONCEPTS_ID, mname, 'evdasAlerts' AS DATA_SOURCE
        FROM (
            SELECT ea.ID VSID, MEDICAL_CONCEPTS_ID, mc.name mname,
                   ROW_NUMBER() OVER (PARTITION BY vea.validated_signal_id, ea.SUBSTANCE_ID, ea.PT_CODE, ea.alert_configuration_id ORDER BY ea.ID) rn
            FROM EVDAS_ALERT ea
            JOIN VALIDATED_EVDAS_ALERTS vea ON ea.ID = vea.EVDAS_ALERT_ID
            JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
            JOIN EVDAS_SIGNAL_CONCEPTS ascs ON ea.ID = ascs.EVDAS_ALERT_ID
            JOIN MEDICAL_CONCEPTS mc ON mc.ID = ascs.MEDICAL_CONCEPTS_ID
            WHERE vs.ID = ${signalId}
        ) AS subquery_evdas_alerts
        WHERE rn = 1
        
        UNION ALL
        
        SELECT VSID, rn, MEDICAL_CONCEPTS_ID, mname, 'singleCaseAlerts' AS DATA_SOURCE
        FROM (
            SELECT ea.ID VSID, MEDICAL_CONCEPTS_ID, mc.name mname,
                   ROW_NUMBER() OVER (PARTITION BY vea.validated_signal_id, ea.product_family, ea.case_number, ea.alert_configuration_id ORDER BY ea.ID) rn
            FROM SINGLE_CASE_ALERT ea
            JOIN VALIDATED_SINGLE_ALERTS vea ON ea.ID = vea.SINGLE_ALERT_ID
            JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
            JOIN SINGLE_SIGNAL_CONCEPTS ascs ON ea.ID = ascs.SINGLE_CASE_ALERT_ID
            JOIN MEDICAL_CONCEPTS mc ON mc.ID = ascs.MEDICAL_CONCEPTS_ID
            WHERE vs.ID = ${signalId}
        ) AS subquery_single_case_alerts
        WHERE rn = 1
    )
    
    SELECT 
        t1.mname,
        COUNT(CASE WHEN t1.DATA_SOURCE = 'aggregateAlerts' THEN 1 END) AS AggregateAlerts,
        COUNT(CASE WHEN t1.DATA_SOURCE = 'singleCaseAlerts' THEN 1 END) AS SingleCaseAlerts,
        COUNT(CASE WHEN t1.DATA_SOURCE = 'evdasAlerts' THEN 1 END) AS EvdasAlerts
    FROM t1
    GROUP BY t1.mname
    """
    }

    static aggregateCaseAlert_attached_signals = {

        """
        SELECT DISTINCT vs.id,PRODUCT_ID ,PT_CODE 
        FROM AGG_ALERT aa
        JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
        JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
        
        """
    }

    static aggregateCaseAlert_signals_to_add = { productIdAndPtCode ->

        """
       SELECT aa.Id,PRODUCT_ID,PT_CODE
       FROM Agg_alert aa 
       LEFT JOIN validated_agg_alerts vaa ON (aa.ID = vaa.agg_alert_id)
       LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vaa.VALIDATED_SIGNAL_ID
       where (aa.PRODUCT_ID , aa.PT_CODE) IN (${productIdAndPtCode}) 
       and vs.id is null
        
        """
    }

    static evdasCaseAlert_attached_signals = {

        """
        SELECT Distinct vs.ID VSID, SUBSTANCE_ID ,PT_CODE 
        FROM EVDAS_ALERT ea
        JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
        JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
        
        """
    }

    static evdasCaseAlert_signals_to_add = { productIdAndPtCode ->

        """
       SELECT ea.Id,SUBSTANCE_ID,PT_CODE
       FROM  EVDAS_ALERT ea 
       LEFT JOIN VALIDATED_EVDAS_ALERTS vea ON (ea.ID = vea.EVDAS_ALERT_ID)
       LEFT JOIN VALIDATED_SIGNAL vs ON vs.ID = vea.VALIDATED_SIGNAL_ID
       where (ea.SUBSTANCE_ID , ea.PT_CODE) IN (${productIdAndPtCode}) 
       and vs.id is null
        
        """
    }

    static caseAlert_dashboard_single_due_date = {Long currentUserId, Long workflowGrpId,List<Long> groupIdList ->
        """ 
        select 
        sum(case when DATE(aa.DUE_DATE) < DATE(CURRENT_TIMESTAMP) then 1 else 0 end) as PASTCOUNT,
        sum(case when DATE(aa.DUE_DATE) > DATE(CURRENT_TIMESTAMP) then 1 else 0 end) as FUTURECOUNT,
        sum(case when DATE(aa.DUE_DATE) = DATE(CURRENT_TIMESTAMP)  then 1 else 0 end) as CURRENTCOUNT
        FROM SINGLE_CASE_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.EXEC_CONFIG_ID)
        LEFT JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        where (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND rc.adhoc_run      = false AND rc.is_deleted = false AND rc.is_latest = true  and rc.IS_ENABLED=true AND aa.IS_CASE_SERIES = false  
        AND disp.review_completed = false AND rc.workflow_group = ${workflowGrpId}
         """
    }
    static caseAlert_dashboard_agg_due_date = {Long currentUserId, Long workflowGrpId,List<Long> groupIdList ->
        """ 
        select 
        sum(case when DATE(aa.DUE_DATE) < DATE(CURRENT_TIMESTAMP) then 1 else 0 end) as PASTCOUNT,
        sum(case when DATE(aa.DUE_DATE) > DATE(CURRENT_TIMESTAMP) then 1 else 0 end) as FUTURECOUNT,
        sum(case when DATE(aa.DUE_DATE) = DATE(CURRENT_TIMESTAMP)  then 1 else 0 end) as CURRENTCOUNT
        FROM AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        where (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND rc.adhoc_run      = false AND rc.is_deleted = false AND rc.is_latest = true  and rc.IS_ENABLED=true
        AND disp.review_completed = false AND rc.workflow_group = ${workflowGrpId}
         """
    }
    static aggCaseAlert_dashboard_count_by_disposition = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
       select disp.display_name wv ,count(aa.id) cnt
        from AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
        where 
        (aa.ASSIGNED_TO_ID = ${currentUserId}
            OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
         )
        and rc.adhoc_run = false 
        and disp.review_completed = false 
        and rc.is_deleted = false
        and rc.is_latest = true
        and rc.workflow_group = ${workflowGroupId}
        group by disp.display_name,disp.id
        order by disp.id
        
        """
    }

    static aggCaseAlert_dashboard_by_disposition = { boolean isUser, Long dispositionId = null, List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "aa.ASSIGNED_TO_ID" : "aa.ASSIGNED_TO_GROUP_ID"
        """
        select disp.id,$assignedColumn, rc.workflow_group ,count(aa.id) cnt
        from AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp on (aa.disposition_id = disp.ID)
        where
        rc.adhoc_run = false 
        ${userId ? "AND aa.ASSIGNED_TO_ID = $userId" : ""}
        and rc.is_deleted = false
        AND rc.is_latest = true
        and disp.review_Completed = false
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by disp.id,$assignedColumn,rc.workflow_group
        """
    }
    static singleCaseAlert_dashboard_count_by_disposition = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
       select disp.display_name wv ,count(sca.id) cnt
        from SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        LEFT JOIN DISPOSITION disp on (sca.disposition_id = disp.ID)
        where  
        (sca.ASSIGNED_TO_ID = ${currentUserId}
            OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
        )
        AND sca.IS_CASE_SERIES = false
        and rc.adhoc_run = false
        and rc.is_deleted = false
        AND rc.is_latest = true
        and disp.closed = false
        and rc.workflow_group = ${workflowGroupId}
        group by disp.display_name,disp.id
        order by disp.id
        """
    }
    static singleCaseAlert_dashboard_by_disposition = { boolean isUser, Long dispositionId = null, List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "sca.ASSIGNED_TO_ID" : "sca.ASSIGNED_TO_GROUP_ID"
        """
        select disp.id,$assignedColumn, rc.workflow_group ,count(sca.id) cnt
        from SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        LEFT JOIN DISPOSITION disp on (sca.disposition_id = disp.ID)
        where
        sca.IS_CASE_SERIES = false
        ${userId ? "AND sca.ASSIGNED_TO_ID = $userId" : ""}
        and rc.adhoc_run = false 
        and rc.is_deleted = false
        AND rc.is_latest = true
        and rc.is_enabled = true
        and disp.review_Completed = false
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by disp.id,$assignedColumn,rc.workflow_group
        """
    }

    static singleCaseAlert_dashboard_due_date = { boolean isUser, Long dispositionId = null,List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "sca.ASSIGNED_TO_ID" : "sca.ASSIGNED_TO_GROUP_ID"
        """
        select to_char(DATE(due_date),'DD-MM-YYYY') as due_date, $assignedColumn,rc.workflow_group,count(DATE(due_date)) as cnt
        from SINGLE_CASE_ALERT sca
        JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        JOIN DISPOSITION disp ON (sca.disposition_id  = disp.ID)
        where  
        sca.IS_CASE_SERIES = false
        ${userId ? "AND sca.ASSIGNED_TO_ID = $userId" : ""}
        and rc.adhoc_run = false
        ${dispositionId ? "" : "AND disp.review_Completed = false"}
        and rc.is_deleted = false
        AND rc.is_latest = true
        AND due_date is not null
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by DATE(due_date), $assignedColumn,rc.workflow_group
        """
    }

    static aggCaseAlert_dashboard_due_date = { boolean isUser, Long dispositionId = null,List<Long> groupIdList = [], Long userId = null ->
        String assignedColumn = isUser ? "aa.ASSIGNED_TO_ID" : "aa.ASSIGNED_TO_GROUP_ID"
        """
        select to_char(DATE(due_date),'DD-MM-YYYY') as due_date, $assignedColumn,rc.workflow_group,count(DATE(due_date)) as cnt
        from AGG_ALERT aa
        JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        where  
        rc.adhoc_run = false
        ${userId ? "AND aa.ASSIGNED_TO_ID = $userId" : ""}
        ${dispositionId ? "" : "AND disp.review_Completed = false"}
        and rc.is_deleted = false
        AND rc.is_latest = true
        AND due_date is not null
        ${dispositionId ? "AND disp.id = $dispositionId" : ""}
        AND $assignedColumn IS NOT NULL
        ${groupIdList.size() ? "AND aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(',')})": ""}
        group by DATE(due_date), $assignedColumn,rc.workflow_group
        """
    }

    static dashboard_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList ->
        """
      SELECT 'evdas' AS evdas,COUNT(ea.id)
      FROM EVDAS_ALERT ea
      LEFT JOIN EX_EVDAS_CONFIG ec ON (ec.id = ea.exec_configuration_id)
      LEFT JOIN DISPOSITION disp ON (ea.disposition_id  = disp.ID)
      WHERE (ea.ASSIGNED_TO_ID = ${currentUserId}
            OR ea.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
         )
      AND ec.adhoc_run       = false
      AND disp.review_completed = false
      AND ec.is_deleted = false
      AND ec.is_latest = true
      AND ec.workflow_group = ${workflowGroupId}

        """

    }

    static aggCaseAlert_dashboard_by_status = {Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
          select aa.product_name,dp.display_name as "Disposition", 
          count(distinct aa.product_id||aa.pt||aa.soc||aa.exec_configuration_id) AS "CNT"
          from   agg_alert aa 
          left join disposition dp on aa.disposition_id = dp.id 
          where dp.closed = false and dp.REVIEW_COMPLETED= false 
            and (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")} )  )  
            and aa.exec_configuration_id in (select id from EX_RCONFIG where adhoc_run= false and is_deleted= false and is_latest=true and workflow_group=${workflowGroupId}) 
            group by aa.product_name,dp.display_name
          order by 1
        """
    }

    static product_name_selection = {String viewName, String productColumn, String languageId ->
        "select "+ productColumn + " from "+ viewName+ " where LANG_ID = '"+ languageId + "'"
    }

    static dashboard_aggregate_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList ->
        """
        SELECT COUNT(aa.id) FROM AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON (rc.id = aa.exec_configuration_id)
        LEFT JOIN DISPOSITION disp ON (aa.disposition_id  = disp.ID)
        WHERE (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND rc.adhoc_run = false AND rc.is_deleted = false AND rc.is_latest = true  and rc.IS_ENABLED=true and rc.SELECTED_DATA_SOURCE != 'jader'
        AND disp.review_completed = false AND rc.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_counts2 = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->
        """
    SELECT COUNT(aa.id) FROM AGG_ALERT aa
    LEFT JOIN EX_RCONFIG rc ON rc.id = aa.exec_configuration_id
    LEFT JOIN DISPOSITION disp ON aa.disposition_id  = disp.ID
    INNER JOIN (
        SELECT DISTINCT aa_id
        FROM (
            SELECT aa.id AS aa_id
            FROM AGG_ALERT aa
            WHERE aa.ASSIGNED_TO_ID = ${currentUserId}
            UNION
            SELECT aa.id AS aa_id
            FROM AGG_ALERT aa
            WHERE aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
        ) AS subquery1
    ) AS filtered_aa ON aa.id = filtered_aa.aa_id
    WHERE rc.adhoc_run = false AND rc.is_deleted = false AND rc.is_latest = true AND rc.IS_ENABLED = true 
    AND rc.SELECTED_DATA_SOURCE != 'jader'
    AND disp.review_completed = false AND rc.workflow_group = ${workflowGroupId}
    """
    }

    static dashboard_aggregate_single_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList  ->
        """
        SELECT COUNT(sca.id) FROM SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON (rc.id = sca.exec_config_id)
        LEFT JOIN DISPOSITION disp ON (sca.disposition_id  = disp.ID) 
        WHERE  (sca.ASSIGNED_TO_ID = ${currentUserId} OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) ) 
            AND sca.IS_CASE_SERIES = false AND rc.adhoc_run = false and rc.IS_ENABLED=true
            AND rc.is_deleted = false AND rc.is_latest = true  AND disp.review_completed = false 
            AND rc.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_single_counts2 = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->
        """
    SELECT COUNT(sca.id) FROM SINGLE_CASE_ALERT sca
    LEFT JOIN EX_RCONFIG rc ON rc.id = sca.exec_config_id
    LEFT JOIN DISPOSITION disp ON sca.disposition_id = disp.ID
    INNER JOIN (
        SELECT DISTINCT sca_id
        FROM (
            SELECT sca.id AS sca_id
            FROM SINGLE_CASE_ALERT sca
            WHERE sca.ASSIGNED_TO_ID = ${currentUserId}
            UNION
            SELECT sca.id AS sca_id
            FROM SINGLE_CASE_ALERT sca
            WHERE sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
        ) AS subquery1
    ) AS filtered_sca ON sca.id = filtered_sca.sca_id
    WHERE sca.IS_CASE_SERIES = false AND rc.adhoc_run = false AND rc.IS_ENABLED = true
    AND rc.is_deleted = false AND rc.is_latest = true AND disp.review_completed = false 
    AND rc.workflow_group = ${workflowGroupId}
    """
    }

    static dashboard_aggregate_evdas_counts = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList  ->
        """
        SELECT COUNT(ea.id) FROM EVDAS_ALERT ea
        LEFT JOIN EX_EVDAS_CONFIG ec ON (ec.id = ea.exec_configuration_id)
        LEFT JOIN DISPOSITION disp ON (ea.disposition_id  = disp.ID) 
        WHERE (ea.ASSIGNED_TO_ID = ${currentUserId} OR ea.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}) )
        AND ec.adhoc_run       = false AND ec.is_deleted = false AND ec.is_latest = true AND disp.review_completed = false 
        AND ec.workflow_group = ${workflowGroupId}
        """
    }
    static dashboard_aggregate_evdas_counts2 = { Long currentUserId, Long workflowGroupId,List<Long> groupIdList  ->
        """
        SELECT COUNT(ea.id) FROM EVDAS_ALERT ea
        LEFT JOIN EX_EVDAS_CONFIG ec ON ec.id = ea.exec_configuration_id
        LEFT JOIN DISPOSITION disp ON ea.disposition_id  = disp.ID
        INNER JOIN (
        SELECT DISTINCT eva_id
        FROM (
            SELECT eva.id AS eva_id
            FROM EVDAS_ALERT eva
            WHERE eva.ASSIGNED_TO_ID = ${currentUserId}
            UNION
            SELECT eva.id AS eva_id
            FROM EVDAS_ALERT eva
            WHERE eva.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
            ) as subquery1
        ) filtered_eva ON ea.id = filtered_eva.eva_id
        WHERE ec.adhoc_run = false AND ec.is_deleted = false AND ec.is_latest = true AND disp.review_completed = false 
        AND ec.workflow_group = ${workflowGroupId}
        """
    }

    static singleCaseAlert_dashboard_by_status = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->

        """
          select sca.product_name,dp.display_name as "Disposition", 
          count(distinct sca.CASE_NUMBER||sca.product_family||sca.exec_config_id) AS "CNT"
          from  SINGLE_CASE_ALERT sca left join disposition dp on  sca.disposition_id = dp.id where dp.closed <> true and dp.REVIEW_COMPLETED= false
            and (sca.ASSIGNED_TO_ID =  ${currentUserId} OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})) 
            and sca.IS_CASE_SERIES = false 
            and sca.exec_config_id in (select id from EX_RCONFIG where adhoc_run= false and is_deleted= false and is_latest=true and workflow_group=${workflowGroupId}) 
            group by sca.product_name,dp.display_name
          order by 1
        """
    }

    static list_agg_tag_name = {Long execConfigId,List<Long> reviewedDispIdList ->
        """
        select
          aca.id as id,
          STRING_AGG(at.name, '@@@' ORDER BY aca.id) as tags
          from
            AGG_ALERT aca
          inner join
            DISPOSITION disposition
              on aca.disposition_id=disposition.id
          inner join
            AGG_ALERT_TAGS aaT
              on aca.id = aat.AGG_ALERT_ID
          inner join
            ALERT_TAG at
              on aat.ALERT_TAG_ID = at.id
          where
            aca.exec_configuration_id=${execConfigId}
            and not (aca.disposition_id in (${reviewedDispIdList.join(",").toString()}))    
          group by aca.id
        """
    }

    static list_single_case_tag_name = {Long execConfigId,List<Long> reviewedDispIdList ->
        """
        select
          sca.id as id,
          STRING_AGG(at.tag_text, '@@@' ORDER BY sca.id) as tags
          from
            SINGLE_CASE_ALERT sca
          inner join
            DISPOSITION disposition
              on sca.disposition_id=disposition.id
          inner join
            SINGLE_GLOBAL_TAG_MAPPING saT
              on sca.id = sat.SINGLE_ALERT_ID
          inner join
            SINGLE_CASE_ALL_TAG at
              on sat.SINGLE_GLOBAL_ID = at.id
          where
            sca.exec_config_id=${execConfigId}
            and not (sca.disposition_id in (${reviewedDispIdList.join(",").toString()}))    
          group by sca.id
        """
    }

    static list_single_case_tag_name_bulk = {List<Long> alertIdList ->
        """
        select
           sca_id as id,
           STRING_AGG(tag_text, '@@@' ORDER BY sca_id) as tags from (
           Select  sca.id as sca_id,at.tag_text as tag_text,row_number() over (partition by sca.id, tag_text order by sca.id) as rn
           from
            SINGLE_CASE_ALERT sca
            left join ex_rconfig ex
             on sca.EXEC_CONFIG_ID = ex.id 
          inner join
            SINGLE_CASE_ALL_TAG at
              on sca.case_id = at.case_id and (ex.PVR_CASE_SERIES_ID = at.case_Series_id or at.case_series_id is null)
          where
            sca.id in (${alertIdList.join(",").toString()})   
          )  as subquery
          where rn = 1
          group by sca_id

        """
    }
//TODO archive check
    static update_literature_alert_level_disposition = {Map queryParameters ->
        String domainTableString
        if (queryParameters.isEmbase) {
            domainTableString = queryParameters.isArchived ? 'ARCHIVED_EMBASE_LITERATURE_ALERT' : 'EMBASE_LITERATURE_ALERT'
        } else {
            domainTableString = queryParameters.isArchived ? 'ARCHIVED_LITERATURE_ALERT' : 'LITERATURE_ALERT'
        }
        String literatureAlertSql = "update ${domainTableString} set disposition_id = ${queryParameters.targetDispositionId} , disp_performed_by = '${queryParameters.dispPerformedBy}', is_disp_changed = ${queryParameters.isDispChanged} " +
        "where EX_LIT_SEARCH_CONFIG_ID = ${ queryParameters.execConfigId} " +
                "AND disposition_id not in (${queryParameters.reviewCompletedDispIdList.join(",")}) "
        literatureAlertSql
    }

    private static String getProdNameINCriteriaForSCA(List<String> allowedProducts) {
        String productNameCriteria = ""
        if (allowedProducts.size() > 1000) {
            List<List<String>> allowedProductsSubList = allowedProducts.collate(1000)
            productNameCriteria += "( sca.product_name IN (${allowedProductsSubList[0].join(",")})"
            allowedProductsSubList.remove(0)
            allowedProductsSubList.each {
                productNameCriteria += " OR sca.product_name IN (${it.join(",").toString()})"
            }
            productNameCriteria += ")"
        } else {
            productNameCriteria += "sca.product_name IN (${allowedProducts.join(",")})"
        }
        productNameCriteria
    }

    static String getProdNameINCriteria(List<String> allowedProducts) {
        String productNameCriteria = ""
        if (allowedProducts.size() > 1000) {
            List<List<String>> allowedProductsSubList = allowedProducts.collate(1000)
            productNameCriteria += "( aa.product_name IN (${allowedProductsSubList[0].join(",").toString()})"
            allowedProductsSubList.remove(0)
            allowedProductsSubList.each {
                productNameCriteria += " OR aa.product_name IN (${it.join(",").toString()})"
            }
            productNameCriteria += ")"
        } else {
            productNameCriteria += "aa.product_name IN (${allowedProducts.join(",").toString()})"
        }
        productNameCriteria
    }

    //TODO: Refactor this code to make sure that this is generically used.
    static StringBuilder getDictProdNameINCriteria(List allowedProducts, String columnName) {
        StringBuilder productNameCriteria = new StringBuilder()
        if (allowedProducts.size() > 1000) {
            List allowedProductsSubList = allowedProducts.collate(1000)
            productNameCriteria.append("( $columnName IN (${allowedProductsSubList[0].join(",").toString()})")
            allowedProductsSubList.remove(0)
            allowedProductsSubList.each {
                productNameCriteria.append(" OR $columnName IN (${it.join(",").toString()})")
            }
            productNameCriteria.append(")")
        } else {
            productNameCriteria.append("$columnName IN (${allowedProducts.join(",").toString()})")
        }
        productNameCriteria
    }

    static ebgm_strat_view_sql = {
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%STR_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = 'PVA-DB'" + ""
    }

    static ebgm_sub_group_view_sql = { String applicationName ->
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME, JSON_VALUE(config_value,'\$.PVS_STR_COLUMN') as PVS_STR_COLUMN FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%EBGM_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and APPLICATION_NAME = '${applicationName}'" + ""
    }
    static ebgm_other_sub_group_view_sql = {
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME, JSON_VALUE(config_value,'\$.PVS_STR_COLUMN') as PVS_STR_COLUMN FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%EBGM_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and CONFIG_KEY not in ('EBGM_SUBGROUP_AGE_GROUP','EBGM_SUBGROUP_GENDER') and APPLICATION_NAME = 'PVA-DB'" + ""
    }
    static other_sub_group_view_sql = {
        "SELECT JSON_VALUE(config_value,'\$.DSP_VIEW_NAME') as DSP_VIEW_NAME, JSON_VALUE(config_value,'\$.PVS_STR_COLUMN') as PVS_STR_COLUMN FROM VW_ADMIN_APP_CONFIG WHERE CONFIG_KEY LIKE '%PRR_SUBGROUP_%' AND JSON_VALUE(config_value,'\$.PVS_VALUE') = '1' and CONFIG_KEY not in ('EBGM_SUBGROUP_AGE_GROUP','EBGM_SUBGROUP_GENDER') and APPLICATION_NAME = 'PVA-DB'" + ""
    }
    static rel_ror_sub_group_enabled = {
        "select dbms_lob.substr(CONFIG_VALUE) as PVS_VALUE from VW_ADMIN_APP_CONFIG where CONFIG_KEY = 'PRR_ENABLE_RELATIVE_ROR' and APPLICATION_NAME = 'PVA-DB'"
    }

    static ebgm_strat_column_sql = { vwName ->
        "select * from ${vwName}"
    }

    static agg_on_demand_col_sql = {
        "select * from AGG_ON_DEM_RPT_FIELD_MAPPING where enabled = true order by id asc"
    }
    static distinct_advance_filter_new_column_list = { String columnName, String fieldName, String tableName, Long execConfigId ->
        "SELECT DISTINCT (CAST($columnName AS jsonb))->>'$fieldName' FROM $tableName WHERE EXEC_CONFIGURATION_ID = $execConfigId"
    }
    static distinct_advance_filter_new_column_clob_list = { String columnName, String fieldName,String tableName,Long execConfigId,String term,Integer start,Integer length ->
        String values  = """
            select distinct $columnName ->>'$fieldName' from $tableName where upper($columnName ->>'$fieldName') like upper('%$term%')
        """
        if (execConfigId > 0) {
            values += " and exec_configuration_id=$execConfigId"
        }

        values += " OFFSET ${start} LIMIT ${length}"
        values
    }
    static distinct_values_for_clob_columns = { String columnName, Long execConfigId, String joinTableName, String term,Integer start,Integer length ->
        String values = """
         select distinct SUBSTRING (alias.$columnName FROM 1 FOR 32767)
          from SINGLE_CASE_ALERT alert
          inner join $joinTableName alias on alert.id=alias.SINGLE_ALERT_ID
          where alias.$columnName ~ '$term'
          and alert.adhoc_run = false
        """
        if (execConfigId > 0) {
            values += " and alert.exec_config_id=$execConfigId"
        }

        values += " OFFSET ${start} LIMIT ${length}"
        values
    }
    static distinct_values_for_clob_columns_adhoc_alert = { String columnName, Long execConfigId, String joinTableName, String term,Integer start,Integer length ->
        String values = """
         select distinct SUBSTRING(alias.$columnName FROM 1 FOR 32767)
          from SINGLE_CASE_ALERT alert
          inner join $joinTableName alias on alert.id=alias.SINGLE_ALERT_ID
          where   alias.$columnName ~ '$term'
          and alert.adhoc_run = true
        """
        if (execConfigId > 0) {
            values += " and alert.exec_config_id=$execConfigId"
        }

        values += " OFFSET ${start} LIMIT ${length}"
        values
    }

    static search_dispostion_from_signal_memo = { String searchString, String esc_char ->
        return """
                                         {alias}.id IN (
                                         SELECT SIGNAL_NOTIFICATION_MEMO_ID 
                                         FROM DISPOSITION_IDS_MEMO 
                                         WHERE DISPOSITION_IDS IN (
                                         SELECT id FROM DISPOSITION 
                                         WHERE LOWER(display_name) LIKE LOWER('%${searchString.replaceAll("'", "''")}%') ESCAPE '${esc_char}'
                            )
                            )
                        """

    }

    static distinct_count_for_clob_columns = { String columnName, Long execConfigId, String joinTableName, String term ->
        String count = """
         select
          count(distinct DBMS_LOB.SUBSTR(alias.$columnName,32767))
          from SINGLE_CASE_ALERT alert
          inner join
          $joinTableName alias on alert.id=alias.SINGLE_ALERT_ID
          where regexp_like(alias.$columnName, '$term',  'i')
          and alert.adhoc_run = false
        """
        if (execConfigId > 0) {
            count += " and alert.exec_config_id=$execConfigId"
        }
        count
    }

    static distinct_values_for_clob_on_demand_columns = { String columnName, Long execConfigId, String joinTableName, String term,Integer start,Integer length ->
        String subAliasColumn = ""
        if(columnName in ["PT", "CON_COMIT", "PRODUCT_NAME", "MED_ERROR"] ) {
            subAliasColumn = "SINGLE_ALERT_OD_ID"
        } else {
            subAliasColumn = "SINGLE_ALERT_ID"
        }
        String values = """
         select distinct SUBSTRING(alias.$columnName FROM 1 FOR 32767)
          from SINGLE_ON_DEMAND_ALERT alert
          inner join $joinTableName alias on alert.id=alias.$subAliasColumn
          where alias.$columnName ~ '$term'
           and alert.exec_config_id=$execConfigId
        """
        values += " OFFSET ${start} LIMIT ${length}"
        values
    }

    static distinct_count_for_clob_on_demand_columns = { String columnName, Long execConfigId, String joinTableName, String term ->
        String subAliasColumn = ""
        if(columnName in ["PT", "CON_COMIT", "PRODUCT_NAME", "MED_ERROR"] ) {
            subAliasColumn = "SINGLE_ALERT_OD_ID"
        } else {
            subAliasColumn = "SINGLE_ALERT_ID"
        }
        String count = """
         select
          count(distinct DBMS_LOB.SUBSTR(alias.$columnName,32767))
          from SINGLE_ON_DEMAND_ALERT alert
          inner join
          $joinTableName alias on alert.id=alias.$subAliasColumn
          where regexp_like(alias.$columnName, '$term',  'i')
          and alert.exec_config_id=$execConfigId
        """
        count
    }
    static fetch_signal_list_query_product_group = { String pecProductList, boolean isAssociateClosedSignal,String productGroup ->
        String fetchQuery = ""
        if(isAssociateClosedSignal){
            fetchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            left join VALIDATED_SIGNAL_ALL_PRODUCT v2
            on v1.id = v2.VALIDATED_SIGNAL_ID
            where (v2.SIGNAL_ALL_PRODUCTS in (${pecProductList}) and v1.PRODUCTS is not null) or (v1.PRODUCT_GROUP_SELECTION ${searchLikeSql(productGroup)})
            order by v1.LAST_DISP_CHANGE desc
             """
        }else{
            fetchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            left join VALIDATED_SIGNAL_ALL_PRODUCT v2
            on v1.id = v2.VALIDATED_SIGNAL_ID
            where ((v2.SIGNAL_ALL_PRODUCTS in (${pecProductList}) and v1.PRODUCTS is not null) or (v1.PRODUCT_GROUP_SELECTION ${searchLikeSql(productGroup)}))
            and v1.SIGNAL_STATUS != '${Constants.DATE_CLOSED}' order by v1.LAST_DISP_CHANGE desc
             """
        }
        fetchQuery
    }
    static fetch_signal_list_query_product = { String pecProductList, boolean isAssociateClosedSignal ->
        String fetchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            left join VALIDATED_SIGNAL_ALL_PRODUCT v2
            on v1.id = v2.VALIDATED_SIGNAL_ID
            where v2.SIGNAL_ALL_PRODUCTS in (${pecProductList})
        """
        if(isAssociateClosedSignal){
            fetchQuery = fetchQuery + "order by v1.LAST_DISP_CHANGE desc"
        }else{
            fetchQuery = fetchQuery + " and v1.SIGNAL_STATUS != '${Constants.DATE_CLOSED}' order by v1.LAST_DISP_CHANGE desc"
        }
        fetchQuery
    }
    static final_signal_list_query_product_event = { String alertPt, String signalList, Long dispositionId ->
        String MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.ALL_EVENTS ${searchLikeSql(alertPt)}
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId} and v1.EVENTS is not null
            order by v1.LAST_DISP_CHANGE desc
        """
        MatchQuery
    }
    static final_signal_list_query_product_event_all_pt_checked = { String alertPt, String signalList,Long dispositionId ->
        String MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.ALL_EVENTS_WITHOUT_HIERARCHY ${searchLikeSql(alertPt)}
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId}
            order by v1.LAST_DISP_CHANGE desc
        """
        MatchQuery
    }
    static final_signal_list_query_product_event_smq_alert = { String alertPt, String signalList,Long dispositionId,boolean isSplitToPtLevel ->
        String MatchQuery = ""
        if(isSplitToPtLevel){
            MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where ((v1.ALL_SMQS ${searchLikeSql(alertPt)}) or (v1.ALL_EVENTS ${searchLikeSql(alertPt)}))
            and ((${signalList}) and v1.DISPOSITION_ID = ${dispositionId})
            order by v1.LAST_DISP_CHANGE desc
        """
        }else{
            MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.ALL_SMQS ${searchLikeSql(alertPt)}
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId}
            order by v1.LAST_DISP_CHANGE desc
        """
        }
        MatchQuery
    }
    static final_signal_list_query_product_event_smq_alert_event_group = { String alertPt, String signalList,Long dispositionId ->
        String MatchQuery = """
            select v1.id from VALIDATED_SIGNAL v1
            where v1.EVENT_GROUP_SELECTION like '%${alertPt}%'
            and (${signalList}) and v1.DISPOSITION_ID = ${dispositionId}
            order by v1.LAST_DISP_CHANGE desc
        """
        MatchQuery
    }
    static fetch_pt_from_smq = { Long smqId, Long termScope ->
        String MatchQuery = """
            select mpt.pt_name from meddra_smq_to_llt_pt_temp msmq
            join pvr_md_pref_term mpt on (nvl(msmq.pt_code,msmq.llt_code)=mpt.pt_code)
            where msmq.meddra_dict_id in (select MEDDRA_DICT_ID from vw_meddra_tenant_mapping) and mpt.meddra_dict_id in (select MEDDRA_DICT_ID from vw_meddra_tenant_mapping) and msmq.smq_parent = ${smqId} and msmq.term_scope = ${termScope}
        """
        MatchQuery
    }

    static fetch_pt_from_soc = { Long socCode ->
        String MatchQuery = """
            select pt_name from pvr_md_hierarchy where soc_code = ${socCode}
        """
        MatchQuery
    }

    static fetch_pt_from_hlt = { Long hltCode ->
        String MatchQuery = """
            select pt_name from pvr_md_hierarchy where hlt_code = ${hltCode}
        """
        MatchQuery
    }

    static fetch_pt_from_hlgt = { Long hlgtCode ->
        String MatchQuery = """
            select pt_name from pvr_md_hierarchy where hlgt_code = ${hlgtCode}
        """
        MatchQuery
    }
    static auto_routing_signal_name_for_audit_log = {String signalList ->
        String MatchQuery = """
            select name from VALIDATED_SIGNAL 
            where id in (${signalList})
        """
        MatchQuery
    }
    static add_activity_for_exec_config = {
        "INSERT INTO ex_rconfig_activities(EX_CONFIG_ACTIVITIES_ID,ACTIVITY_ID) VALUES(?,?)"
    }

    static add_activity_for_signal_memo = {
        "INSERT INTO VALIDATED_ALERT_ACTIVITIES(VALIDATED_SIGNAL_ID,ACTIVITY_ID) VALUES(?,?)"
    }

    static add_signalRMMs_for_signal = {
        "INSERT INTO SIGNAL_SIG_RMMS(SIG_RMM_ID,VALIDATED_SIGNAL_ID) VALUES(?,?)"
    }

    static case_history_change = { String caseAndConfigId ->
        """
         select case_number,config_id,JUSTIFICATION,CHANGE from (
               select case_number,config_id,JUSTIFICATION ,change,row_number() OVER(PARTITION BY case_number, config_id,change ORDER by last_updated desc) rn
               from case_history
               where change in ('DISPOSITION','PRIORITY') and (case_number,config_id) in ($caseAndConfigId))
               where rn = 1
         """
    }

    static product_event_history_change = { String productEventConfigIds ->
        """
         select product_name,event_name,config_id,JUSTIFICATION,DISPOSITION_ID,CHANGE from (
               select product_name,event_name,config_id,JUSTIFICATION,DISPOSITION_ID,change,row_number() OVER(PARTITION BY product_name,event_name, config_id,change ORDER by last_updated desc) rn
               from product_event_history
               where change in ('DISPOSITION','PRIORITY') and (product_name,event_name,config_id) in ($productEventConfigIds)) as subquery 
               where rn = 1
         """
    }

    static evdas_history_change = { Long configId ->
        """
         select product_name, event_name, config_id, JUSTIFICATION, CHANGE from (
                select product_name, event_name, config_id, JUSTIFICATION, change,
                row_number() OVER (PARTITION BY product_name, event_name, config_id, change ORDER by last_updated desc) rn
               from evdas_history
               where change in ('DISPOSITION', 'PRIORITY') and config_id = $configId
               ) as history_alias
               where rn = 1
        """
    }

    static add_signal_for_agg_alert = { Boolean isArchived ->
        if(isArchived)
            "INSERT into VALIDATED_ARCHIVED_ACA(VALIDATED_SIGNAL_ID,ARCHIVED_ACA_ID) VALUES(?,?)"
        else
            "INSERT INTO VALIDATED_AGG_ALERTS(VALIDATED_SIGNAL_ID,AGG_ALERT_ID, DATE_CREATED) VALUES(?,?,?)"
    }

    static signal_action_count = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'validated_signal_actions_id').toString()
        """
           Select validated_signal_actions_id AS SIGNALID,count(action_id) AS COUNT 
           from validated_signal_actions where $inCriteriaWhereClause
           group by validated_signal_actions_id
           """
    }

    static prev_actions_sql = { List prevAlertIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(prevAlertIdList, 'saa.SINGLE_CASE_ALERT_ID').toString()
        """
         SELECT SINGLE_CASE_ALERT_ID as alertId,
                 action_status as actionStatus,
                 alert_type as alertType,
                 assigned_to_id as assignedToId,
                 assigned_to_group_id as assignedToGroupId,
                 comments ,
                 completed_date as completedDate ,
                 config_id as configId ,
                 created_date as createdDate,
                 details,
                 due_date as dueDate,
                 guest_attendee_email as guestAttendeeEmail,
                 owner_id as ownerId,
                 type_id as typeId,
                 viewed
                 FROM SINGLE_ALERT_ACTIONS saa
                 INNER JOIN ACTIONS actions ON saa.ACTION_ID=actions.id
                 WHERE $inCriteriaWhereClause
         """
    }

    static quant_case_series_proc = { boolean isCumulative ->
        isCumulative ? "{call PKG_PVS_ALERT_EXECUTION.P_GET_QUANT_CASE_SERIES_CUM(?,?)}" : "{call PKG_PVS_ALERT_EXECUTION.P_GET_QUANT_CASE_SERIES_NEW(?,?)}"
    }

    static quant_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT acvt.suspect_product AS product, acvt.event_name AS event, dr.date_rng_end_absolute AS lastEndDate
            FROM ex_rconfig ex
                LEFT JOIN ex_alert_date_range dr ON ex.ex_alert_date_range_id = dr.id
                LEFT JOIN ex_rconfig_activities ex_acvt ON ex.id = ex_acvt.ex_config_activities_id
                LEFT JOIN activities acvt ON acvt.id = ex_acvt.activity_id
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static qual_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT acvt.case_number AS caseNumber, dr.date_rng_end_absolute AS lastEndDate
            FROM ex_rconfig ex
                LEFT JOIN ex_alert_date_range dr ON ex.ex_alert_date_range_id = dr.id
                LEFT JOIN ex_rconfig_activities ex_acvt ON ex.id = ex_acvt.ex_config_activities_id
                LEFT JOIN activities acvt ON acvt.id = ex_acvt.activity_id
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static evdas_cumulative_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT ACVT.SUSPECT_PRODUCT AS product, ACVT.EVENT_NAME AS event, EX.DATE_CREATED AS lastEndDate
            FROM EX_EVDAS_CONFIG EX
                LEFT JOIN EX_EVDAS_DATE_RANGE DR ON EX.DATE_RANGE_INFORMATION_ID = DR.ID
                LEFT JOIN EX_EVDAS_CONFIG_ACTIVITIES EX_ACTV ON EX.ID = EX_ACTV.EX_EVDAS_CONFIG_ID
                LEFT JOIN ACTIVITIES ACVT ON ACVT.ID = EX_ACTV.ACTIVITY_ID
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static evdas_custom_last_review_sql = {exconfigIds ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(exconfigIds, 'ex.id').toString()
        """
            SELECT ACVT.SUSPECT_PRODUCT AS product, ACVT.EVENT_NAME AS event, DR.DATE_RNG_END_ABSOLUTE AS lastEndDate
            FROM EX_EVDAS_CONFIG EX
                LEFT JOIN EX_EVDAS_DATE_RANGE DR ON EX.DATE_RANGE_INFORMATION_ID = DR.ID
                LEFT JOIN EX_EVDAS_CONFIG_ACTIVITIES EX_ACTV ON EX.ID = EX_ACTV.EX_EVDAS_CONFIG_ID
                LEFT JOIN ACTIVITIES ACVT ON ACVT.ID = EX_ACTV.ACTIVITY_ID
            WHERE $inCriteriaWhereClause
            ORDER BY ex.id DESC
        """
    }

    static list_literature_tag_name = {Long execConfigId,List<Long> reviewedDispIdList ->
        """
        select
          lit.id as id,
          STRING_AGG(at.name, '@@@' ORDER BY lit.id) as tags
          from
            LITERATURE_ALERT lit
          inner join
            DISPOSITION disposition
              on lit.disposition_id=disposition.id
          inner join
            LITERATURE_ALERT_TAGS laT
              on lit.id = lat.LITERATURE_ALERT_ID
          inner join
            ALERT_TAG at
              on lat.ALERT_TAG_ID = at.id
          where
            lit.EX_LIT_SEARCH_CONFIG_ID=${execConfigId}
            and not (lit.disposition_id in (${reviewedDispIdList.join(",").toString()}))    
          group by lit.id
        """
    }

    static list_embase_literature_tag_name = {Long execConfigId, List<Long> reviewedDispIdList ->
        """
        select
          lit.id as id,
          STRING_AGG(at.name, '@@@' ORDER BY lit.id) as tags
          from
            EMBASE_LITERATURE_ALERT lit
          inner join
            DISPOSITION disposition
              on lit.disposition_id=disposition.id
          inner join
            EMBASE_LITERATURE_ALERT_TAGS lat
              on lit.id = lat.EMBASE_LITERATURE_ALERT_ID
          inner join
            ALERT_TAG at
              on lat.ALERT_TAG_ID = at.id
          where
            lit.EX_LIT_SEARCH_CONFIG_ID=${execConfigId}
            and not (lit.disposition_id in (${reviewedDispIdList.join(",").toString()}))    
          group by lit.id
        """
    }

    static disable_all_constraints_on_table = { tableName ->
        "BEGIN " +
                "FOR c IN " +
                "(SELECT c.owner, c.table_name, c.constraint_name " +
                "FROM user_constraints c, user_tables t " +
                "WHERE c.table_name = '${tableName}' " +
                "AND c.status = 'ENABLED' " +
                "AND NOT (t.iot_type IS NOT NULL AND c.constraint_type = 'P') " +
                "ORDER BY c.constraint_type DESC) " +
                "LOOP " +
                "dbms_utility.exec_ddl_statement('alter table \"' || c.owner || '\".\"' || c.table_name || '\" disable constraint ' || c.constraint_name || ' cascade'); " +
                "END LOOP; " +
                "END; "
    }

    static enable_all_constraints_on_table = { tableName ->
        "BEGIN " +
                "FOR c IN " +
                "(SELECT c.owner, c.table_name, c.constraint_name " +
                "FROM user_constraints c, user_tables t " +
                "WHERE c.table_name = '${tableName}' " +
                "AND c.status = 'DISABLED' " +
                "ORDER BY c.constraint_type) " +
                "LOOP " +
                "dbms_utility.exec_ddl_statement('alter table \"' || c.owner || '\".\"' || c.table_name || '\" enable constraint ' || c.constraint_name ); " +
                "END LOOP; " +
                "END; "
    }

    static literature_archived_sql = { Long configId, Long exConfigId ->
        """
        DO \$\$
        DECLARE
        lvc_sql TEXT;
        lvc_exec_sql TEXT;
        BEGIN
        -- Concatenate column names excluding specific columns
        SELECT string_agg(column_name, ',' ORDER BY ordinal_position)
        INTO lvc_sql
        FROM information_schema.columns
        WHERE table_name = 'archived_literature_alert';

        -- Dynamic SQL execution
        lvc_exec_sql := 'INSERT INTO archived_literature_alert (' || lvc_sql || ') ' ||
                'SELECT ' || lvc_sql || ' FROM literature_alert ' ||
                'WHERE lit_search_config_id = ' || ${configId} || ' ' ||
                'AND ex_lit_search_config_id = ' || ${exConfigId};

        EXECUTE lvc_exec_sql;

        -- Insert into VALIDATED_ARCHIVED_LIT_ALERTS
        INSERT INTO validated_archived_lit_alerts (archived_lit_alert_id, validated_signal_id)
        SELECT ala.literature_alert_id, ala.validated_signal_id
        FROM validated_literature_alerts ala
        INNER JOIN literature_alert la
        ON ala.literature_alert_id = la.id
        WHERE lit_search_config_id = ${configId}
        AND ex_lit_search_config_id = ${exConfigId};

        -- Insert into ARCHIVED_LIT_CASE_ALERT_TAGS
        INSERT INTO archived_lit_case_alert_tags (archived_lit_alert_id, pvs_alert_tag_id)
        SELECT ala.literature_alert_id, ala.pvs_alert_tag_id
        FROM literature_case_alert_tags ala
        INNER JOIN literature_alert la
        ON ala.literature_alert_id = la.id
        WHERE lit_search_config_id = ${configId}
        AND ex_lit_search_config_id = ${exConfigId};

        -- Insert into ARCHIVED_LIT_ALERT_ACTIONS
        INSERT INTO archived_lit_alert_actions (archived_lit_alert_id, action_id, is_retained)
        SELECT ala.literature_alert_id, ala.action_id, ala.is_retained
        FROM lit_alert_actions ala
        INNER JOIN literature_alert la
        ON ala.literature_alert_id = la.id
        WHERE lit_search_config_id = ${configId}
        AND ex_lit_search_config_id = ${exConfigId};

        -- Use INSERT ... ON CONFLICT to handle the MERGE equivalent
        UPDATE attachment_link
        SET reference_class = 'com.rxlogix.config.ArchivedLiteratureAlert'
        WHERE reference_id IN (
            SELECT t1.reference_id
            FROM attachment_link t1
            LEFT JOIN literature_alert t2 ON t1.reference_id = t2.id
            WHERE t2.lit_search_config_id = ${configId}
            AND t2.ex_lit_search_config_id = ${exConfigId}
            AND t1.reference_class = 'com.rxlogix.config.LiteratureAlert'
        );

        INSERT INTO attachment_link (reference_id, reference_class)
        SELECT t1.reference_id, 'com.rxlogix.config.ArchivedLiteratureAlert'
        FROM attachment_link t1
        LEFT JOIN literature_alert t2 ON t1.reference_id = t2.id
        WHERE t2.lit_search_config_id = ${configId}
        AND t2.ex_lit_search_config_id = ${exConfigId}
        AND t1.reference_class = 'com.rxlogix.config.LiteratureAlert'
        AND NOT EXISTS (
            SELECT 1 FROM attachment_link al WHERE al.reference_id = t1.reference_id
        );

        -- Delete statements
        DELETE FROM validated_literature_alerts
        WHERE literature_alert_id IN (
                SELECT ala.literature_alert_id
                FROM validated_literature_alerts ala
                INNER JOIN literature_alert la
                ON ala.literature_alert_id = la.id
                WHERE lit_search_config_id = ${configId}
                AND ex_lit_search_config_id = ${exConfigId}
        );

        DELETE FROM literature_case_alert_tags
        WHERE literature_alert_id IN (
                SELECT ala.literature_alert_id
                FROM literature_case_alert_tags ala
                INNER JOIN literature_alert la
                ON ala.literature_alert_id = la.id
                WHERE lit_search_config_id = ${configId}
                AND ex_lit_search_config_id = ${exConfigId}
        );

        DELETE FROM lit_alert_actions
        WHERE literature_alert_id IN (
                SELECT ala.literature_alert_id
                FROM lit_alert_actions ala
                INNER JOIN literature_alert la
                ON ala.literature_alert_id = la.id
                WHERE lit_search_config_id = ${configId}
                AND ex_lit_search_config_id = ${exConfigId}
        );

        DELETE FROM literature_alert
        WHERE lit_search_config_id = ${configId}
        AND ex_lit_search_config_id = ${exConfigId};

        EXCEPTION
        WHEN OTHERS THEN
        RAISE EXCEPTION 'Error: %', SQLERRM;
        END \$\$;
        """

    }

    static embase_literature_archived_sql = { Long configId, Long exConfigId ->
    """
        DO \$\$
        DECLARE
          lvc_sql TEXT;
          lvc_exec_sql TEXT;
        BEGIN
          SELECT string_agg(column_name, ',' ORDER BY ordinal_position)
          INTO lvc_sql
          FROM information_schema.columns
          WHERE table_name = 'archived_embase_literature_alert';

          lvc_exec_sql := 'INSERT INTO archived_embase_literature_alert (' || lvc_sql || ') ' ||
                'SELECT ' || lvc_sql || ' FROM embase_literature_alert ' ||
                'WHERE lit_search_config_id = ' || ${configId} || ' ' ||
                'AND ex_lit_search_config_id = ' || ${exConfigId};

          EXECUTE lvc_exec_sql;
          
          INSERT INTO validated_archived_embase_lit_alerts (archived_embase_lit_alert_id, validated_signal_id)
          SELECT ala.embase_literature_alert_id, ala.validated_signal_id
          FROM validated_embase_literature_alerts ala
          INNER JOIN embase_literature_alert la
          ON ala.embase_literature_alert_id = la.id
          WHERE lit_search_config_id = ${configId}
          AND ex_lit_search_config_id = ${exConfigId};
        
          INSERT INTO archived_embase_lit_case_alert_tags (archived_embase_lit_alert_id, pvs_alert_tag_id)
          SELECT ala.embase_literature_alert_id, ala.pvs_alert_tag_id
          FROM embase_literature_case_alert_tags ala
          INNER JOIN embase_literature_alert la
          ON ala.embase_literature_alert_id = la.id
          WHERE lit_search_config_id = ${configId}
          AND ex_lit_search_config_id = ${exConfigId};
        
          INSERT INTO archived_embase_lit_alert_actions (archived_embase_lit_alert_id, action_id, is_retained)
          SELECT ala.embase_literature_alert_id, ala.action_id, ala.is_retained
          FROM embase_literature_alert_actions ala
          INNER JOIN embase_literature_alert la
          ON ala.embase_literature_alert_id = la.id
          WHERE lit_search_config_id = ${configId}
          AND ex_lit_search_config_id = ${exConfigId};
       
        
        UPDATE attachment_link
        SET reference_class = 'com.rxlogix.config.ArchivedEmbaseLiteratureAlert'
        WHERE reference_id IN (
            SELECT t1.reference_id
            FROM attachment_link t1
            LEFT JOIN literature_alert t2 ON t1.reference_id = t2.id
            WHERE t2.lit_search_config_id = ${configId}
            AND t2.ex_lit_search_config_id = ${exConfigId}
            AND t1.reference_class = 'com.rxlogix.config.EmbaseLiteratureAlert'
        );

        INSERT INTO attachment_link (reference_id, reference_class)
        SELECT t1.reference_id, 'com.rxlogix.config.ArchivedEmbaseLiteratureAlert'
        FROM attachment_link t1
        LEFT JOIN literature_alert t2 ON t1.reference_id = t2.id
        WHERE t2.lit_search_config_id = ${configId}
        AND t2.ex_lit_search_config_id = ${exConfigId}
        AND t1.reference_class = 'com.rxlogix.config.EmbaseLiteratureAlert'
        AND NOT EXISTS (
            SELECT 1 FROM attachment_link al WHERE al.reference_id = t1.reference_id
        );
        
        -- Delete statements
        DELETE FROM validated_embase_literature_alerts
        WHERE embase_literature_alert_id IN (
                SELECT ala.embase_literature_alert_id
                FROM validated_embase_literature_alerts ala
                INNER JOIN embase_literature_alert la
                ON ala.embase_literature_alert_id = la.id
                WHERE lit_search_config_id = ${configId}
                AND ex_lit_search_config_id = ${exConfigId}
        );

        DELETE FROM embase_literature_case_alert_tags
        WHERE embase_literature_alert_id IN (
                SELECT ala.embase_literature_alert_id
                FROM embase_literature_case_alert_tags ala
                INNER JOIN embase_literature_alert la
                ON ala.embase_literature_alert_id = la.id
                WHERE lit_search_config_id = ${configId}
                AND ex_lit_search_config_id = ${exConfigId}
        );
        
        DELETE FROM embase_literature_alert_actions
        WHERE embase_literature_alert_id IN (
                SELECT ala.embase_literature_alert_id
                FROM embase_literature_alert_actions ala
                INNER JOIN embase_literature_alert la
                ON ala.embase_literature_alert_id = la.id
                WHERE lit_search_config_id = ${configId}
                AND ex_lit_search_config_id = ${exConfigId}
        );
        
        DELETE FROM embase_literature_alert
        WHERE lit_search_config_id = ${configId}
        AND ex_lit_search_config_id = ${exConfigId};
        
        EXCEPTION
        WHEN OTHERS THEN
        RAISE EXCEPTION 'Error: %', SQLERRM;
        END \$\$;
    """
    }

    static sca_archived_sql = { Long configId, Long exConfigId ->
        """
        DO \$\$
        DECLARE
        lvc_sql TEXT;
        lvc_exec_sql TEXT;
        BEGIN
        -- Generate the list of columns
        SELECT STRING_AGG(column_name, ',' ORDER BY ordinal_position)
        INTO lvc_sql
        FROM information_schema.columns
        WHERE table_name = 'archived_single_case_alert';

        -- Execute the dynamic SQL for inserting into ARCHIVED_SINGLE_CASE_ALERT
        lvc_exec_sql := 'INSERT INTO archived_single_case_alert (' || lvc_sql || ') ' ||
                'SELECT ' || lvc_sql || ' FROM single_case_alert ' ||
                'WHERE alert_configuration_id = ' || ${configId} ||
                ' AND exec_config_id = ' || ${exConfigId};
        EXECUTE lvc_exec_sql;

        -- Insert data into VALIDATED_ARCHIVED_SCA
        INSERT INTO validated_archived_sca (archived_sca_id, validated_signal_id)
        SELECT vsca.single_alert_id, vsca.validated_signal_id
        FROM validated_single_alerts vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into ARCHIVED_SCA_TAGS
        INSERT INTO archived_sca_tags (single_alert_id, pvs_alert_tag_id)
        SELECT vsca.single_alert_id, vsca.pvs_alert_tag_id
        FROM single_case_alert_tags vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into ARCHIVED_SCA_PT
        INSERT INTO archived_sca_pt (archived_sca_id, archived_sca_pt, pt_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_pt, vsca.pt_list_idx
        FROM single_alert_pt vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into ARCHIVED_SCA_CON_COMIT
        INSERT INTO archived_sca_con_comit (archived_sca_id, alert_con_comit, con_comit_list_idx)
        SELECT vsca.single_alert_id, vsca.alert_con_comit, vsca.con_comit_list_idx
        FROM single_alert_con_comit vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into ARCHIVED_SCA_SUSP_PROD
        INSERT INTO archived_sca_susp_prod (archived_sca_id, sca_product_name, suspect_product_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_product_name, vsca.suspect_product_list_idx
        FROM single_alert_susp_prod vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into ARCHIVED_SCA_MED_ERR_PT_LIST
        INSERT INTO archived_sca_med_err_pt_list (archived_sca_id, sca_med_error, med_error_pt_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_med_error, vsca.med_error_pt_list_idx
        FROM single_alert_med_err_pt_list vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into ARCHIVED_SCA_ACTIONS
        INSERT INTO archived_sca_actions (archived_sca_id, action_id, is_retained)
        SELECT vsca.single_case_alert_id, vsca.action_id, vsca.is_retained
        FROM single_alert_actions vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_case_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_INDICATION_LIST
        INSERT INTO ar_sin_alert_indication_list (archived_sca_id, sca_indication, indication_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_indication, vsca.indication_list_idx
        FROM single_alert_indication_list vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_CAUSE_OF_DEATH
        INSERT INTO ar_sin_alert_cause_of_death (archived_sca_id, sca_cause_of_death, cause_of_death_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_cause_of_death, vsca.cause_of_death_list_idx
        FROM single_alert_cause_of_death vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_PAT_MED_HIST
        INSERT INTO ar_sin_alert_pat_med_hist (archived_sca_id, sca_pat_med_hist, patient_med_hist_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_pat_med_hist, vsca.patient_med_hist_list_idx
        FROM single_alert_pat_med_hist vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_PAT_HIST_DRUGS
        INSERT INTO ar_sin_alert_pat_hist_drugs (archived_sca_id, sca_pat_hist_drugs, patient_hist_drugs_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_pat_hist_drugs, vsca.patient_hist_drugs_list_idx
        FROM single_alert_pat_hist_drugs vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_BATCH_LOT_NO
        INSERT INTO ar_sin_alert_batch_lot_no (archived_sca_id, sca_batch_lot_no, batch_lot_no_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_batch_lot_no, vsca.batch_lot_no_list_idx
        FROM single_alert_batch_lot_no vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_CASE_CLASSIFI
        INSERT INTO ar_sin_alert_case_classifi (archived_sca_id, sca_case_classification, case_classification_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_case_classification, vsca.case_classification_list_idx
        FROM single_alert_case_classifi vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_THERAPY_DATES
        INSERT INTO ar_sin_alert_therapy_dates (archived_sca_id, sca_therapy_dates, therapy_dates_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_therapy_dates, vsca.therapy_dates_list_idx
        FROM single_alert_therapy_dates vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SIN_ALERT_DOSE_DETAILS
        INSERT INTO ar_sin_alert_dose_details (archived_sca_id, sca_dose_details, dose_details_list_idx)
        SELECT vsca.single_alert_id, vsca.sca_dose_details, vsca.dose_details_list_idx
        FROM single_alert_dose_details vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SINGLE_ALERT_GENERIC_NAME
        INSERT INTO ar_single_alert_generic_name (single_alert_id, generic_name, generic_name_list_idx)
        SELECT vsca.single_alert_id, vsca.generic_name, vsca.generic_name_list_idx
        FROM single_alert_generic_name vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        -- Insert data into AR_SINGLE_ALERT_ALLPT_OUT_COME
        INSERT INTO ar_single_alert_allpt_out_come (single_alert_id, allpts_outcome, allpts_outcome_list_idx)
        SELECT vsca.single_alert_id, vsca.allpts_outcome, vsca.allpts_outcome_list_idx
        FROM single_alert_allpt_out_come vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE sca.alert_configuration_id = ${configId}
        AND sca.exec_config_id = ${exConfigId};

        END \$\$;
        """

    }

    static aca_archived_sql = { Long configId, Long exConfigId ->
        """
    DO \$\$
    DECLARE
        lvc_sql TEXT;
        lvc_exec_sql TEXT;
    BEGIN
        -- Step 1: Aggregate column names
        SELECT STRING_AGG(column_name, ',' ORDER BY ordinal_position)
        INTO lvc_sql
        FROM information_schema.columns
        WHERE table_name = 'archived_agg_alert';    

        -- Step 2: Construct and execute dynamic SQL for ARCHIVED_AGG_ALERT
        lvc_exec_sql := 'INSERT INTO archived_agg_alert (' || lvc_sql || ') ' ||
                        'SELECT ' || lvc_sql || ' FROM agg_alert ' ||
                        'WHERE alert_configuration_id = ' || ${configId} || 
                        ' AND exec_configuration_id = ' || ${exConfigId};
        EXECUTE lvc_exec_sql;

        -- Step 3: Insert into VALIDATED_ARCHIVED_ACA
        EXECUTE 'INSERT INTO validated_archived_aca(archived_aca_id, validated_signal_id) ' ||
                'SELECT vaca.agg_alert_id, vaca.validated_signal_id ' ||
                'FROM validated_agg_alerts vaca ' ||
                'INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id ' ||
                'WHERE alert_configuration_id = ' || ${configId} || 
                ' AND exec_configuration_id = ' || ${exConfigId};

        -- Step 4: Insert into ARCHIVED_AGG_CASE_ALERT_TAGS
        EXECUTE 'INSERT INTO archived_agg_case_alert_tags(agg_alert_id, pvs_alert_tag_id) ' ||
                'SELECT vaca.agg_alert_id, vaca.pvs_alert_tag_id ' ||
                'FROM agg_case_alert_tags vaca ' ||
                'INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id ' ||
                'WHERE alert_configuration_id = ' || ${configId} || 
                ' AND exec_configuration_id = ' || ${exConfigId};

        -- Step 5: Insert into ARCHIVED_ACA_ACTIONS
        EXECUTE 'INSERT INTO archived_aca_actions(archived_aca_id, action_id, is_retained) ' ||
                'SELECT vaca.agg_alert_id, vaca.action_id, vaca.is_retained ' ||
                'FROM agg_alert_actions vaca ' ||
                'INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id ' ||
                'WHERE alert_configuration_id = ' || ${configId} || 
                ' AND exec_configuration_id = ' || ${exConfigId};

        -- Step 6: Merge into attachment_link
        EXECUTE 'UPDATE attachment_link al SET reference_class = ''com.rxlogix.signal.ArchivedAggregateCaseAlert'' ' ||
                'FROM (SELECT t1.reference_id FROM attachment_link t1 ' ||
                'LEFT JOIN agg_alert t2 ON t1.reference_id = t2.id ' ||
                'WHERE t2.alert_configuration_id = ' || ${configId} || 
                ' AND t2.exec_configuration_id = ' || ${exConfigId} || 
                ' AND t1.reference_class = ''com.rxlogix.signal.AggregateCaseAlert'') conf ' ||
                'WHERE al.reference_id = conf.reference_id';

        -- Step 7: Update product_event_history
        EXECUTE 'UPDATE product_event_history ' ||
                'SET archived_agg_case_alert_id = agg_case_alert_id, agg_case_alert_id = NULL ' ||
                'WHERE config_id = ' || ${configId} || 
                ' AND exec_config_id = ' || ${exConfigId};

        -- Step 8: Delete from VALIDATED_AGG_ALERTS
        EXECUTE 'DELETE FROM validated_agg_alerts WHERE agg_alert_id IN ' ||
                '(SELECT vaca.agg_alert_id FROM validated_agg_alerts vaca ' ||
                'INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id ' ||
                'WHERE aca.alert_configuration_id = ' || ${configId} || 
                ' AND aca.exec_configuration_id = ' || ${exConfigId} || ')';

        -- Step 9: Delete from AGG_CASE_ALERT_TAGS
        EXECUTE 'DELETE FROM agg_case_alert_tags WHERE agg_alert_id IN ' ||
                '(SELECT vaca.agg_alert_id FROM agg_case_alert_tags vaca ' ||
                'INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id ' ||
                'WHERE aca.alert_configuration_id = ' || ${configId} || 
                ' AND aca.exec_configuration_id = ' || ${exConfigId} || ')';

        -- Step 10: Delete from AGG_ALERT_ACTIONS
        EXECUTE 'DELETE FROM agg_alert_actions WHERE agg_alert_id IN ' ||
                '(SELECT vaca.agg_alert_id FROM agg_alert_actions vaca ' ||
                'INNER JOIN agg_alert aca ON vaca.agg_alert_id = aca.id ' ||
                'WHERE aca.alert_configuration_id = ' || ${configId} || 
                ' AND aca.exec_configuration_id = ' || ${exConfigId} || ')';

        -- Step 11: Delete from AGG_ALERT
        EXECUTE 'DELETE FROM agg_alert WHERE alert_configuration_id = ' || ${configId} || 
                ' AND exec_configuration_id = ' || ${exConfigId};

    EXCEPTION WHEN OTHERS THEN
        RAISE EXCEPTION 'An error occurred: %', SQLERRM;
    END \$\$;
    """
    }



    static evdas_archived_sql = { Long configId, Long exConfigId ->
        """
    DO \$\$
    DECLARE
        lvc_sql TEXT;
        lvc_exec_sql TEXT;
    BEGIN
        -- Step 1: Aggregate column names for ARCHIVED_EVDAS_ALERT
        SELECT STRING_AGG(column_name, ',' ORDER BY ordinal_position)
        INTO lvc_sql
        FROM information_schema.columns
        WHERE table_name = 'archived_evdas_alert'; 

        -- Step 2: Construct and execute dynamic SQL for ARCHIVED_EVDAS_ALERT
        lvc_exec_sql := 'INSERT INTO archived_evdas_alert (' || lvc_sql || ') ' ||
                        'SELECT ' || lvc_sql || ' FROM evdas_alert ' ||
                        'WHERE alert_configuration_id = ' || ${configId} || 
                        ' AND exec_configuration_id = ' || ${exConfigId};
        EXECUTE lvc_exec_sql;

        -- Step 3: Insert into VALIDATED_ARCH_EVDAS_ALERTS
        INSERT INTO validated_arch_evdas_alerts (archived_evdas_alert_id, validated_signal_id)
        SELECT veva.evdas_alert_id, veva.validated_signal_id
        FROM validated_evdas_alerts veva
        INNER JOIN evdas_alert eva ON veva.evdas_alert_id = eva.id
        WHERE eva.alert_configuration_id = ${configId} 
        AND eva.exec_configuration_id = ${exConfigId};

        -- Step 4: Insert into ARCHIVED_EVDAS_ALERT_ACTIONS
        INSERT INTO archived_evdas_alert_actions (archived_evdas_alert_id, action_id, is_retained)
        SELECT veva.evdas_alert_id, veva.action_id, veva.is_retained
        FROM evdas_alert_actions veva
        INNER JOIN evdas_alert eva ON veva.evdas_alert_id = eva.id
        WHERE eva.alert_configuration_id = ${configId} 
        AND eva.exec_configuration_id = ${exConfigId};

        -- Step 5: Move the attachments to Archived Evdas Alert
        UPDATE attachment_link al
        SET reference_class = 'com.rxlogix.config.ArchivedEvdasAlert'
        FROM (SELECT t1.reference_id 
              FROM attachment_link t1 
              LEFT JOIN evdas_alert t2 ON t1.reference_id = t2.id 
              WHERE t2.alert_configuration_id = ${configId} 
              AND t2.exec_configuration_id = ${exConfigId} 
              AND t1.reference_class = 'com.rxlogix.config.EvdasAlert') conf
        WHERE al.reference_id = conf.reference_id;

        -- Step 6: Update evdas_history
        UPDATE evdas_history eh
        SET archived_evdas_alert_id = conf.alert_id, evdas_alert_id = NULL
        FROM (SELECT t1.id as history_id, t1.evdas_alert_id as alert_id 
              FROM evdas_history t1 
              LEFT JOIN evdas_alert t2 ON t1.evdas_alert_id = t2.id 
              WHERE t2.alert_configuration_id = ${configId} 
              AND t2.exec_configuration_id = ${exConfigId}) conf
        WHERE eh.id = conf.history_id;

        -- Step 7: Delete from VALIDATED_EVDAS_ALERTS
        DELETE FROM validated_evdas_alerts 
        WHERE evdas_alert_id IN (
            SELECT veva.evdas_alert_id
            FROM validated_evdas_alerts veva
            INNER JOIN evdas_alert eva ON veva.evdas_alert_id = eva.id
            WHERE eva.alert_configuration_id = ${configId} 
            AND eva.exec_configuration_id = ${exConfigId}
        );

        -- Step 8: Delete from EVDAS_ALERT_ACTIONS
        DELETE FROM evdas_alert_actions 
        WHERE evdas_alert_id IN (
            SELECT veva.evdas_alert_id
            FROM evdas_alert_actions veva
            INNER JOIN evdas_alert eva ON veva.evdas_alert_id = eva.id
            WHERE eva.alert_configuration_id = ${configId} 
            AND eva.exec_configuration_id = ${exConfigId}
        );

        -- Step 9: Delete from EVDAS_ALERT
        DELETE FROM evdas_alert 
        WHERE alert_configuration_id = ${configId} 
        AND exec_configuration_id = ${exConfigId};

    EXCEPTION WHEN OTHERS THEN
        RAISE EXCEPTION 'An error occurred: %', SQLERRM;
    END \$\$;
    """
    }


    static signal_alert_ids_literature = { articleIdList, execConfigId, configId ->
        """
         with t1 as 
           (SELECT VSID,ARTICLE_ID,rn 
           FROM (SELECT vs.ID VSID, ARTICLE_ID  ,row_number() OVER(PARTITION BY la.ARTICLE_ID ,  vs.ID ORDER by la.ID) rn
                 FROM LITERATURE_ALERT la
                 JOIN VALIDATED_LITERATURE_ALERTS vla ON (la.ID = vla.LITERATURE_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vla.VALIDATED_SIGNAL_ID
                 WHERE ARTICLE_ID IN (${articleIdList}) AND la.lit_Search_Config_ID = $configId
                 )
           where rn = '1'),
           t2 as 
                (select  la.ID alertId,ARTICLE_ID
                 FROM  LITERATURE_ALERT la
                 WHERE la.EX_LIT_SEARCH_CONFIG_ID= ${execConfigId} and ARTICLE_ID IN (${
                    articleIdList
                })
                 )
         select ALERTID,VSID from t1 join t2 on (t1.ARTICLE_ID = t2.ARTICLE_ID)
        
        """
    }

    static signal_alert_ids_embase_literature = { articleIdList, execConfigId, configId ->
        """
         with t1 as 
           (SELECT VSID,ARTICLE_ID,rn 
           FROM (SELECT vs.ID VSID, ARTICLE_ID  ,row_number() OVER(PARTITION BY la.ARTICLE_ID ,  vs.ID ORDER by la.ID) rn
                 FROM EMBASE_LITERATURE_ALERT la
                 JOIN VALIDATED_EMBASE_LITERATURE_ALERTS vla ON (la.ID = vla.EMBASE_LITERATURE_ALERT_ID)
                 JOIN VALIDATED_SIGNAL vs ON vs.ID = vla.VALIDATED_SIGNAL_ID
                 WHERE ARTICLE_ID IN (${articleIdList}) AND la.lit_Search_Config_ID = $configId
                 )
           where rn = '1'),
           t2 as 
                (select  la.ID alertId,ARTICLE_ID
                 FROM  EMBASE_LITERATURE_ALERT la
                 WHERE la.EX_LIT_SEARCH_CONFIG_ID= ${execConfigId} and ARTICLE_ID IN (${
            articleIdList
        })
                 )
         select ALERTID,VSID from t1 join t2 on (t1.ARTICLE_ID = t2.ARTICLE_ID)
        
        """
    }

    static fetchGlobalTags = {domain ->
        """
             select tag_text as tagText , sub_tag_text as subTagText , global_Id as globalId  from pvs_global_tag where domain='${domain}'
        """
    }

    static fetchAlertTags = {domain ->
        """
             select tag_text as tagText , sub_tag_text as subTagText , alert_Id as alertId  from pvs_alert_tag where domain='${domain}'
        """
    }

    static literature_archivedAlertCat_sql = { oldExecConfigID, executedConfigId ->
        """
        SELECT
            la.id As LIT_ALERT_ID,
            at.id AS ALERT_TAG_ID
        FROM
            pvs_alert_tag               at
            INNER JOIN archived_literature_alert   ala ON ala.id = at.alert_id
            INNER JOIN literature_alert LA on ala.article_id = la.article_id
        WHERE
           ${getDictProdNameINCriteria(oldExecConfigID,'ala.ex_lit_search_config_id').toString()} and la.ex_lit_search_config_id=${executedConfigId}
        """
    }

    static embaseLiterature_archivedAlertCat_sql = { oldExecConfigID, executedConfigId ->
        """
        SELECT
            ela.id As LIT_ALERT_ID,
            at.id AS ALERT_TAG_ID
        FROM
            pvs_alert_tag at
            INNER JOIN archived_embase_literature_alert eala ON eala.id = at.alert_id
            INNER JOIN embase_literature_alert ela on eala.article_id = ela.article_id
        WHERE
           ${getDictProdNameINCriteria(oldExecConfigID,'eala.ex_lit_search_config_id').toString()} and ela.ex_lit_search_config_id=${executedConfigId}
        """
    }

    static aca_archivedAlertCat_sql = { oldExecConfigID, executedConfigId ->

        """
        SELECT
            agg.id   AS agg_alert_id,
            at.id    AS alert_tag_id,
            arch.disposition_id AS agg_disposition_id

        FROM
            pvs_alert_tag        at
            INNER JOIN archived_agg_alert   arch ON arch.id = at.alert_id
            INNER JOIN agg_alert            agg ON agg.product_id = arch.product_id
                                        AND agg.pt_code = arch.pt_code
        WHERE
            arch.exec_configuration_id = ${oldExecConfigID}
            AND agg.exec_configuration_id = ${executedConfigId}
         AND at.domain = 'Aggregate Case Alert'
        """
    }

    static sca_archivedAlertCat_sql = { oldExecConfigID, executedConfigId ->
        """
        SELECT
            sca.id   AS sca_alert_id,
            at.id    AS alert_tag_id,
            sca.badge AS sca_badge
        FROM
            pvs_alert_tag        at
            INNER JOIN archived_single_case_alert   arch ON arch.id = at.alert_id
            INNER JOIN single_case_alert            SCA ON sca.case_id = arch.case_id
        WHERE
            ${getDictProdNameINCriteria(oldExecConfigID,'arch.exec_config_id').toString()}
            AND sca.exec_config_id = ${executedConfigId}
            AND at.domain = 'Single Case Alert'
            """

    }

    static fetchETLCases = {
        """
            select * from CAT_CASES_REFRESH_HIST where is_Refreshed = 'N'
        """
    }

    static updateETLCases = { cases ->

        String query
        cases.eachWithIndex { caseIdString, index ->
            if (index == 0) {
                query = "UPDATE CAT_CASES_REFRESH_HIST SET is_Refreshed = 'Y' WHERE is_Refreshed = 'N' and ( CASE_ID in (" + caseIdString + ")"
            } else {
                query = query + " OR CASE_ID in (" + caseIdString + ") "
            }
        }
        query = query + ")"
        return query


    }

    static aca_allGlobalTags_sql = { configIds ->
        """
        SELECT DISTINCT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            gt.tag_text AS tag
        FROM
            pvs_global_tag        gt
            INNER JOIN agg_alert   aca ON aca.global_identity_id = gt.global_id            
        WHERE
            aca.exec_configuration_id in (${configIds})
            AND gt.domain = 'Aggregate Case Alert'
            AND (gt.private_user is null OR  gt.private_user = '0')
            AND (gt.is_retained is null OR  gt.is_retained = false)
            """
    }

    static deleteGlobalTags = { String globalCaseId ->
        """
            DELETE FROM PVS_GLOBAL_TAG WHERE GLOBAL_ID in ( ${globalCaseId} )
        """
    }

    static deleteGlobalTagsMapping = { String globalCaseId ->
        """
            DELETE FROM SINGLE_GLOBAL_TAGS WHERE GLOBAL_CASE_ID in ( ${globalCaseId} )
        """
    }
    
    static memo_reports_sql = { String productSelectionIds, String alertType ->
        """ 
    SELECT id 
    FROM ex_rconfig, 
         LATERAL jsonb_each(PRODUCT_SELECTION::jsonb) AS top_level(key, value),
         LATERAL jsonb_array_elements(value) AS elem
    WHERE jsonb_typeof(PRODUCT_SELECTION::jsonb) = 'object'
      AND (elem->>'id')::bigint IN ($productSelectionIds)
      AND IS_DELETED = FALSE 
      AND IS_ENABLED = TRUE 
      AND adhoc_Run = FALSE 
      AND IS_CASE_SERIES = FALSE 
      AND type = '$alertType'
    
    UNION ALL

    SELECT id 
    FROM ex_rconfig, 
         LATERAL jsonb_each(PRODUCT_GROUP_SELECTION::jsonb) AS top_level(key, value),
         LATERAL jsonb_array_elements(value) AS elem
    WHERE jsonb_typeof(PRODUCT_GROUP_SELECTION::jsonb) = 'object'
      AND (elem->>'id')::bigint IN ($productSelectionIds)
      AND IS_DELETED = FALSE 
      AND IS_ENABLED = TRUE 
      AND adhoc_Run = FALSE 
      AND IS_CASE_SERIES = FALSE 
      AND type = '$alertType'
    """
    }


    static aca_previousAlertTags_sql = { executedConfigId ->
        """
        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id = ${executedConfigId}
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = false)

        UNION

        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN archived_agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id = ${executedConfigId}
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = false)
            """
    }

    static aca_allAlertTags_sql = { configIds ->
        """
        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id in (${configIds})
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = false)
            
        UNION
        
        SELECT
            aca.product_id   AS product_id,
            aca.pt_code    AS pt_code,
            aca.smq_code    AS smq,
            at.tag_text AS tag
        FROM
            pvs_alert_tag        at
            INNER JOIN archived_agg_alert   aca ON aca.id = at.alert_id            
        WHERE
            at.exec_config_id in (${configIds})
            AND at.domain = 'Aggregate Case Alert'
            AND (at.private_user is null OR  at.private_user = '0')
            AND (at.is_retained is null OR  at.is_retained = false)
            """
    }

    static configurations_firstX = { AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds ->
        """
         select * from (
             ${getConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getEvdasConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)} 
             ${getLiteratureConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)} 
         ) as subquery_alias
         ${searchSql(alertDTO.searchString)}  
         ${orderSql(alertDTO.sort, alertDTO.direction)}  
         OFFSET ${alertDTO.offset} ROWS FETCH NEXT ${alertDTO.max} ROWS ONLY    
        """
    }

    static count_configurations = { AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds ->
        """
         select count(*) from (
             ${getCountConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getCountEvdasConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getCountLiteratureConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
         ) as subquery
        """
    }
    static filtered_count_configurations = {AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds ->
        """
         select count(*) from (
             ${getConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getEvdasConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
             ${getLiteratureConfigurations(alertDTO, currentUserId, workflowGroupId, groupIds)}
         ) as subquery
         ${searchSql(alertDTO.searchString)}
        """
    }

    static String getConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) {
         return  """ 
           select ex1.*, case 
            when ex1.type='${Constants.AlertType.INDIVIDUAL_CASE_SERIES}' then EXTRACT(DAY FROM (sca.min_due_date - CURRENT_DATE)) 
            when ex1.type='${Constants.AlertType.AGGREGATE_NEW}' then EXTRACT(DAY FROM (aca.min_due_date - CURRENT_DATE)) 
            else NULL
          end as dueIn
          from (select er.id, er.name, er.adhoc_Run as adhocRun, er.date_created as dateCreated,
          CAST(er.PRODUCT_NAME as TEXT) as product, CAST(er.PRODUCT_SELECTION as TEXT) as productSelection, er.PRODUCT_GROUP_SELECTION as productGroupSelection, er.PRODUCT_DICTIONARY_SELECTION as productDictionarySelection, CAST(er.study_selection as TEXT) as study, er.data_mining_variable as dataMiningVariable, EX_ALERT_DATE_RANGE.DATE_RNG_START_ABSOLUTE as dateRangeStart, EX_ALERT_DATE_RANGE.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
          EX_ALERT_DATE_RANGE.id as dateRangeId,
          case
            when er.adhoc_run= false then CAST(er.REQUIRES_REVIEW_COUNT AS INTEGER)
            else NULL
          end as requiresReview,     
          case
            when er.adhoc_run= false then EXTRACT(DAY FROM (er.review_due_date - CURRENT_DATE))
            else NULL
          end as dueIn2,          
          case
            when er.adhoc_Run=true and er.type='${Constants.AlertConfigType.SINGLE_CASE_ALERT}' then '${Constants.AlertType.ICR_ADHOC}'
            when er.type='${Constants.AlertConfigType.SINGLE_CASE_ALERT}' then '${Constants.AlertType.INDIVIDUAL_CASE_SERIES}'
            when er.adhoc_Run=true and er.type='${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}' then '${Constants.AlertType.AGGREGATE_ADHOC}'
            when er.type='${Constants.AlertConfigType.AGGREGATE_CASE_ALERT}' then '${Constants.AlertType.AGGREGATE_NEW}'
          end as type,
          er.drug_record_numbers as drugRecordNumbers
          from EX_RCONFIG er 
               JOIN EX_ALERT_DATE_RANGE on er.EX_ALERT_DATE_RANGE_ID=EX_ALERT_DATE_RANGE.id 
               where er.is_latest=true and er.IS_ENABLED=true and er.IS_CASE_SERIES= false
               ${roleSql(alertDTO)}
               and er.CONFIG_ID IN (${user_configuration_sql(currentUserId, workflowGroupId, groupIds, null, alertDTO.selectedFilterValues)})
               ${getReviewTypeQuery()}
               and (er.removed_users is null or er.removed_users not like '%,$currentUserId%')
          ) ex1
          LEFT JOIN (select exec_config_id, min(due_Date) as min_due_date from single_case_alert group by exec_config_id) sca on sca.exec_config_id = ex1.id and ex1.type = '${Constants.AlertType.INDIVIDUAL_CASE_SERIES}'
          LEFT JOIN (select exec_configuration_id, min(due_Date) as min_due_date from agg_alert group by exec_configuration_id) aca on aca.exec_configuration_id = ex1.id and ex1.type = '${Constants.AlertType.AGGREGATE_NEW}'
         
          
        """
        }
        ""
    }

    static String getReviewTypeQuery(){
        if(Holders.config.show.all.alerts.on.widget){
            """"""
        }else {
            """and (er.requires_review_count <> '0' OR er.adhoc_Run = true)"""
        }
    }

    static String getCountConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) {
            return """ 
            select er.id
            from EX_RCONFIG er 
            LEFT JOIN EX_ALERT_DATE_RANGE on er.EX_ALERT_DATE_RANGE_ID=EX_ALERT_DATE_RANGE.id
            where er.is_latest=true and er.IS_ENABLED=true and er.IS_CASE_SERIES= false
               ${roleSql(alertDTO)}
               and er.CONFIG_ID IN (${user_configuration_sql(currentUserId, workflowGroupId, groupIds, null, [])})
               ${getReviewTypeQuery()}
               and (er.removed_users is null or er.removed_users not like '%,$currentUserId%')
        """
        }
        ""
    }

    static String getEvdasConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if (alertDTO.evdasRole) {
           return  """
           ${(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
           select ex2.*, EXTRACT(DAY FROM (evdas.min_due_date - CURRENT_DATE)) AS dueIn
            from (select evr.id, evr.name, evr.adhoc_Run as adhocRun, evr.date_created as dateCreated,
           CAST(evr.product_name as TEXT) as product, CAST(evr.PRODUCT_SELECTION as TEXT) as productSelection, evr.PRODUCT_GROUP_SELECTION as productGroupSelection, null as productDictionarySelection, null as study,null as dataMiningVariable, EX_EVDAS_DATE_RANGE.DATE_RNG_START_ABSOLUTE as dateRangeStart, EX_EVDAS_DATE_RANGE.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
           EX_EVDAS_DATE_RANGE.id as dateRangeId,
          case
            when adhoc_run= false then CAST(evr.requires_review_count AS INTEGER)
            else NULL
          end as requiresReview,      
          case
            when adhoc_run = false then EXTRACT(DAY FROM (evr.review_due_date - CURRENT_DATE))
            else NULL
          end as dueIn2,            
          case
            when adhoc_Run=true then '${Constants.AlertType.EVDAS_ADHOC}'
            else '${Constants.AlertType.EVDAS}'
          end as type, null as drugRecordNumbers
            from EX_EVDAS_CONFIG evr
            JOIN EX_EVDAS_DATE_RANGE on evr.DATE_RANGE_INFORMATION_ID=EX_EVDAS_DATE_RANGE.id
             where evr.is_latest=true and evr.IS_ENABLED=true 
            and evr.CONFIG_ID IN (${evdas_configuration_sql(currentUserId, workflowGroupId, groupIds, alertDTO.selectedFilterValues)})
            ${getReviewTypeQueryForEvdas()}
            and (evr.removed_users is null or evr.removed_users not like '%,$currentUserId%')
           ) ex2
           LEFT JOIN (select exec_configuration_id, min(due_Date) as min_due_date from EVDAS_ALERT group by exec_configuration_id) evdas on evdas.exec_configuration_id = ex2.id

          
        """
        }
        ""
    }

    static String getReviewTypeQueryForEvdas(){
        if(Holders.config.show.all.alerts.on.widget){
            """"""
        }else {
            """and (evr.requires_review_count <> '0' OR evr.adhoc_Run = true)"""
        }
    }

    static String getCountEvdasConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if (alertDTO.evdasRole) {
            return """
           ${(alertDTO.singleCaseRole || alertDTO.aggRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
           select evr.id
            from EX_EVDAS_CONFIG evr 
            LEFT JOIN EVDAS_DATE_RANGE on evr.DATE_RANGE_INFORMATION_ID=EVDAS_DATE_RANGE.id
            where evr.is_latest=true and evr.IS_ENABLED=true
            and evr.CONFIG_ID IN (${evdas_configuration_sql(currentUserId, workflowGroupId, groupIds, [])})         
            ${getReviewTypeQueryForEvdas()}
            and (evr.removed_users is null or evr.removed_users not like '%,$currentUserId%')
        """
        }
        ""
    }

    static String getCountLiteratureConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.literatureRole) {
            return """
            ${(alertDTO.singleCaseRole || alertDTO.aggRole || alertDTO.evdasRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
            select elit.id
            from EX_LITERATURE_CONFIG elit 
            LEFT JOIN EX_LITERATURE_DATE_RANGE ON elit.DATE_RANGE_INFORMATION_ID=EX_LITERATURE_DATE_RANGE.id 
            where elit.is_latest=true and elit.IS_ENABLED=true 
            and elit.CONFIG_ID IN (${literature_configuration_sql(currentUserId, workflowGroupId, groupIds, [])})
            ${getReviewTypeQueryForLit()}
            and (elit.removed_users is null or elit.removed_users not like '%,$currentUserId%')
        """
        }
        ""
    }
    static String getReviewTypeQueryForLit(){
        if(Holders.config.show.all.alerts.on.widget){
            """"""
        }else {
            """and elit.requires_review_count <> '0'"""
        }
    }

    static String getLiteratureConfigurations(AlertDTO alertDTO, Long currentUserId , Long workflowGroupId, String groupIds) {
        if(alertDTO.literatureRole) {
         return """
           ${(alertDTO.singleCaseRole || alertDTO.aggRole || alertDTO.evdasRole || SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") || SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) ? "UNION ALL" : ""}
            select ex3.*, CAST(NULL AS INTEGER) as dueIn
            from (select elit.id, name, CAST(FALSE AS BOOLEAN) AS adhocRun, elit.date_created as dateCreated,
            CAST(elit.PRODUCT_NAME as TEXT) product,CAST(elit.PRODUCT_SELECTION as TEXT) as productSelection, elit.PRODUCT_GROUP_SELECTION as productGroupSelection, null as productDictionarySelection, null as study,null as dataMiningVariable, EX_LITERATURE_DATE_RANGE.DATE_RNG_START_ABSOLUTE as dateRangeStart, EX_LITERATURE_DATE_RANGE.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
            EX_LITERATURE_DATE_RANGE.id as dateRangeId,
            CAST(elit.requires_review_count AS INTEGER) as requiresReview,   
            CAST(NULL AS INTEGER) as dueIn2,
            '${Constants.AlertType.LITERATURE}' as type,
            null as drugRecordNumbers
            from EX_LITERATURE_CONFIG elit 
            JOIN EX_LITERATURE_DATE_RANGE ON elit.DATE_RANGE_INFORMATION_ID=EX_LITERATURE_DATE_RANGE.id 
            where elit.is_latest = true and elit.IS_ENABLED=true 
             and elit.CONFIG_ID IN (${literature_configuration_sql(currentUserId, workflowGroupId, groupIds, alertDTO.selectedFilterValues)})
             ${getReviewTypeQueryForLit()}
             and (elit.removed_users is null or elit.removed_users not like '%,$currentUserId%')
           ) ex3
           
        """
        }
        ""
    }

    static String saveUserInfoInPvUserWebappTable(Long id, String email, String fullName, String username, String updatedByUsername, Date date){
        String sqlStatement = """
            INSERT INTO PVUSER_WEBAPP 
            (ID,EMAIL,FULL_NAME,USERNAME,LST_INS_UPD_USR,LST_INS_UPD_DATE)
            VALUES
            (${id},'${email}','${fullName}','${username}','${updatedByUsername}','${sdf.format(date)}')
        """
        return sqlStatement
    }
    static String saveGroupInfoInGroupsWebappTable(Long id, String name, String description,String groupType, String updatedByUsername, Boolean isActive, Date date){
        String sqlStatement = """
            INSERT INTO GROUPS_WEBAPP 
            (ID,NAME,DESCRIPTION,GROUP_TYPE,LST_INS_UPD_USR,IS_ACTIVE,LST_INS_UPD_DATE)
            VALUES
            (${id},'${name.replaceAll("'","''")}','${description?.replaceAll("'","''")}','${groupType}','${updatedByUsername}',${isActive?1:0},'${sdf.format(date)}')
        """
        return sqlStatement
    }
    static String updateUserInfoInPvUserWebappTable(Long id, String email, String fullName, String username, String updatedByUsername, Date date){
        String sqlStatement = """
            UPDATE PVUSER_WEBAPP 
            SET EMAIL = '${email}',
                FULL_NAME = '${fullName}',
                USERNAME = '${username}',
                LST_INS_UPD_USR = '${updatedByUsername}',
                LST_INS_UPD_DATE = '${sdf.format(date)}'
            WHERE
            ID = ${id}
        """
        return sqlStatement
    }

    static String updateGroupInfoInGroupsWebappTable(Long id, String name, String description,String groupType, String updatedByUsername, Boolean isActive, Date date){
        String sqlStatement = """
            UPDATE GROUPS_WEBAPP 
            SET NAME = '${name.replaceAll("'", "''")}',
                DESCRIPTION= '${description?.replaceAll("'", "''")}',
                GROUP_TYPE = '${groupType}',
                LST_INS_UPD_USR = '${updatedByUsername}',
                IS_ACTIVE = ${isActive?1:0},
                LST_INS_UPD_DATE = '${sdf.format(date)}'
            WHERE
            id = ${id}
        """
        return sqlStatement
    }

    static deleteUserInfoFRomPvUserWebApp(Long id){
        String sqlStatement = """DELETE FROM PVUSER_WEBAPP WHERE ID = ${id}"""
        return sqlStatement
    }
    static deleteGroupInfoFRomGroupsWebApp(Long id){
        String sqlStatement = """DELETE FROM GROUPS_WEBAPP WHERE ID = ${id}"""
        return sqlStatement
    }

    static String migrateAllUsersToPvUserWebappTable(List userList, Date date){
        String insertStatement = "Begin"
        userList.each {
            insertStatement += """ Insert into PVUSER_WEBAPP (ID,EMAIL,FULL_NAME,USERNAME,LST_INS_UPD_DATE) VALUES 
                                (${it.id},'${it.email}','${it.fullName}','${it.username}','${sdf.format(date)}');"""
        }
        insertStatement += "End;"
        return insertStatement
    }
    static String migrateAllGroupsToGroupsWebappTable(List groupList, Date date){
        String insertStatement = "Begin"
        groupList.each {
            insertStatement += """ Insert into GROUPS_WEBAPP (ID,NAME,DESCRIPTION,GROUP_TYPE,IS_ACTIVE,LST_INS_UPD_DATE) VALUES 
                                (${it.id},'${it.name.replaceAll("'", "''")}','${it.description?.replaceAll("'", "''")}','${it.groupType}','${it.isActive?1:0}','${sdf.format(date)}');"""
        }
        insertStatement += "End;"
        return insertStatement
    }

    static user_view_product_search_sql = { String searchString ->
        """
            select assignment.id from user_view_assignment assignment, json_table(PRODUCTS,'\$[*]'
                                                                       columns(name VARCHAR2 path '\$.name')) 
                                                                        t1 where UPPER(t1.name) LIKE UPPER('%${searchString}%')
        """
    }

    static String createFilterQueryLiterature(List<String> filterWithUsersAndGroups ) {
        String filterAlertsQuery = ""
        if(filterWithUsersAndGroups.size()) {
            filterAlertsQuery = " and ( "
            filterWithUsersAndGroups.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${extractedId} or "
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.PVUSER_ID=${extractedId} or "
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${it.id} or "
                    }
                }
                else if(it.contains("SharedWithMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "sharewithu1_.id=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "sharewithg2_.id=${it.id} or "
                    }
                }
            }
            filterAlertsQuery = filterAlertsQuery.substring(0,filterAlertsQuery.length()-3)
            filterAlertsQuery += " )"
        }
        filterAlertsQuery
    }

    static String createFilterQuery(List<String> filterWithUsersAndGroups ) {
        String filterAlertsQuery = ""
        if(filterWithUsersAndGroups.size()) {
            filterAlertsQuery = " and ( "
            filterWithUsersAndGroups.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${extractedId} or "
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.PVUSER_ID=${extractedId} or "
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${it.id} or "
                    }
                }
                else if(it.contains("SharedWithMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "sharewithu1_.id=${extractedId} or autosharew3_.id=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "sharewithg2_.id=${it.id} or autosharew4_.id=${it.id} or "
                    }
                }
            }
            filterAlertsQuery = filterAlertsQuery.substring(0,filterAlertsQuery.length()-3)
            filterAlertsQuery += " )"
        }
        filterAlertsQuery
    }

    static user_configuration_sql = { Long userId, Long workflowGroupId, String groupIds, String alertType= null, List<String> filterWithUsersAndGroups=[] ->
        """
    select
        this_.id
    from
        RCONFIG this_ 

    left outer join
        AUTO_SHARE_WITH_GROUP_CONFIG autosharew6_ 
            on this_.id=autosharew6_.CONFIG_ID 
    left outer join
        GROUPS autosharew4_ 
            on autosharew6_.AUTO_SHARE_WITH_GROUPID=autosharew4_.id 
    left outer join
        AUTO_SHARE_WITH_USER_CONFIG autosharew8_ 
            on this_.id=autosharew8_.CONFIG_ID 
    left outer join
        PVUSER autosharew3_ 
            on autosharew8_.AUTO_SHARE_WITH_USERID=autosharew3_.id 
    left outer join
        SHARE_WITH_GROUP_CONFIG sharewithg10_ 
            on this_.id=sharewithg10_.CONFIG_ID 
    left outer join
        GROUPS sharewithg2_ 
            on sharewithg10_.SHARE_WITH_GROUPID=sharewithg2_.id 
    left outer join
        SHARE_WITH_USER_CONFIG sharewithu12_ 
            on this_.id=sharewithu12_.CONFIG_ID 
    left outer join
        PVUSER sharewithu1_ 
            on sharewithu12_.SHARE_WITH_USERID=sharewithu1_.id 
    where
        (
          ( sharewithu1_.id=$userId 
            or autosharew3_.id=$userId
            or this_.PVUSER_ID=$userId
            ${groupSql(groupIds)} 
            ${autoGroupSql(groupIds)} )
            ${createFilterQuery(filterWithUsersAndGroups)}
        ) 
        ${alertType ? "and this_.type='$alertType' " : ""} 
        and this_.WORKFLOW_GROUP=$workflowGroupId
        """
    }

    static user_exconfiguration_sql = { Long userId, Long workflowGroupId, String groupIds, String alertType= null, List<String> filterWithUsersAndGroups=[] ->
        """
    select
        this_.id
    from
        EX_RCONFIG this_ 
    left outer join
        RCONFIG rconfig_
            on this_.CONFIG_ID=rconfig_.id
    left outer join
        AUTO_SHARE_WITH_GROUP_CONFIG autosharew6_ 
            on rconfig_.id=autosharew6_.CONFIG_ID 
    left outer join
        GROUPS autosharew4_ 
            on autosharew6_.AUTO_SHARE_WITH_GROUPID=autosharew4_.id 
    left outer join
        AUTO_SHARE_WITH_USER_CONFIG autosharew8_ 
            on rconfig_.id=autosharew8_.CONFIG_ID 
    left outer join
        PVUSER autosharew3_ 
            on autosharew8_.AUTO_SHARE_WITH_USERID=autosharew3_.id 
    left outer join
        SHARE_WITH_GROUP_EXCONFIG sharewithg10_ 
            on this_.id=sharewithg10_.EXCONFIG_ID 
    left outer join
        GROUPS sharewithg2_ 
            on sharewithg10_.SHARE_WITH_GROUPID=sharewithg2_.id 
    left outer join
        SHARE_WITH_USER_EXCONFIG sharewithu12_ 
            on this_.id=sharewithu12_.EXCONFIG_ID 
    left outer join
        PVUSER sharewithu1_ 
            on sharewithu12_.SHARE_WITH_USERID=sharewithu1_.id 
    where
        (
          ( sharewithu1_.id=$userId 
            or autosharew3_.id=$userId
            or this_.PVUSER_ID=$userId
            ${groupSql(groupIds)} 
            ${autoGroupSql(groupIds)} )
            ${createFilterQuery(filterWithUsersAndGroups)}
        ) 
        ${alertType ? "and this_.type='$alertType' " : ""} 
        and this_.WORKFLOW_GROUP=$workflowGroupId
        """
    }

    static createfilterQueryForEvdas(List<String> filterWithUsersAndGroups) {
        String filterAlertsQuery = ""
        if(filterWithUsersAndGroups.size()) {
            filterAlertsQuery = " and ( "
            filterWithUsersAndGroups.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${extractedId} or "
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.OWNER_ID=${extractedId} or "
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${it.id} or "
                    }
                }
                else if(it.contains("SharedWithMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "sharewithu1_.id=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "sharewithg2_.id=${it.id} or "
                    }
                }
            }
            filterAlertsQuery = filterAlertsQuery.substring(0,filterAlertsQuery.length()-3)
            filterAlertsQuery += " )"
        }
        filterAlertsQuery
    }

    static evdas_configuration_sql = { Long userId, Long workflowGroupId, String groupIds, List<String> selectedFilterValues=[] ->
        """
        select
         this_.id  
    from
        EVDAS_CONFIG this_ 
    left outer join
        SHARE_WITH_GROUP_EVDAS_CONFIG sharewithg4_ 
            on this_.id=sharewithg4_.CONFIG_ID 
    left outer join
        GROUPS sharewithg2_ 
            on sharewithg4_.SHARE_WITH_GROUPID=sharewithg2_.id 
    left outer join
        SHARE_WITH_USER_EVDAS_CONFIG sharewithu6_ 
            on this_.id=sharewithu6_.CONFIG_ID 
    left outer join
        PVUSER sharewithu1_ 
            on sharewithu6_.SHARE_WITH_USERID=sharewithu1_.id 
    where
        (
          ( sharewithu1_.id=$userId
            or this_.OWNER_ID=$userId
            ${groupSql(groupIds)} )
            ${createfilterQueryForEvdas(selectedFilterValues)}
        ) 
        and this_.WORKFLOW_GROUP=$workflowGroupId

        """
    }

    static literature_configuration_sql = { Long userId, Long workflowGroupId, String groupIds, List<String> filterWithUsersAndGroups=[] ->
        """
        select
        this_.id
    from
        LITERATURE_CONFIG this_ 
    left outer join
        SHARE_WITH_GROUP_LITR_CONFIG sharewithg4_ 
            on this_.id=sharewithg4_.CONFIG_ID 
    left outer join
        GROUPS sharewithg2_ 
            on sharewithg4_.SHARE_WITH_GROUPID=sharewithg2_.id 
    left outer join
        SHARE_WITH_USER_LITR_CONFIG sharewithu6_ 
            on this_.id=sharewithu6_.CONFIG_ID 
    left outer join
        PVUSER sharewithu1_ 
            on sharewithu6_.SHARE_WITH_USERID=sharewithu1_.id 
    where
        (
          ( sharewithu1_.id=$userId
            or this_.PVUSER_ID=$userId
            ${groupSql(groupIds)} )
            ${createFilterQueryLiterature(filterWithUsersAndGroups)}
        ) 
        and this_.workflow_group_id=$workflowGroupId

        """
    }

    static String orderSql(String sort, String direction) {
        if(!sort) {
            "ORDER BY dateCreated desc NULLS LAST"
        } else if(sort in ["name", "type"]) {
            "ORDER BY UPPER($sort) $direction NULLS LAST"
        } else if(sort.equals("dateRange")) {
            "ORDER BY DATERANGESTART $direction, DATERANGEEND $direction"
        } else if (sort.equals("product")){
            "ORDER BY UPPER(COALESCE(SUBSTRING($sort FROM 1 FOR LENGTH($sort)), '')) $direction NULLS LAST"
        } else {
            "ORDER BY $sort $direction NULLS LAST"
        }
    }
    static String searchLikeSql(String searchString){
        if(searchString) {
            String esc_char = ""
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                return """
                 like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                """
            } else {
                return """
                 like '%${searchString.replaceAll("'", "''")}%'
            """
            }
        }
        ""
    }
    static String searchSql(String searchString) {
        if(searchString) {
            searchString = searchString.toLowerCase()
            String esc_char = ""
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                // TO DO:-search on study selection for more than one study selected
                return """               
                WHERE LOWER(name) LIKE '%${searchString.replaceAll("'", "''")}%' ESCAPE '${esc_char}'
                OR LOWER(REGEXP_REPLACE(product, '\\([0-9]+\\)\\s*\\([A-Z\\s]+\\),', ',', 'gi')) LIKE '%${searchString.replaceAll("'", "''")}%' ESCAPE '${esc_char}'
                OR LOWER(type) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(CAST(dueIn AS TEXT)) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(CAST(requiresReview AS TEXT)) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(study) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(CONCAT(dataMiningVariable, '(', REGEXP_REPLACE(product, '\\([0-9]+\\)\\s*\\([A-Z\\s]+\\),', ',', 'gi'), ')')) LIKE '%${searchString.replaceAll("'", "''")}%'
                """
            } else {
                // TO DO:-search on study selection for more than one study selected
                return """
                where LOWER(name) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(REGEXP_REPLACE(product, '\\([0-9]+\\)\\s*\\([A-Z\\s]+\\),', ',', 'gi')) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(type) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(study) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR CAST(dueIn AS TEXT) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR CAST(requiresReview AS TEXT) LIKE '%${searchString.replaceAll("'", "''")}%'
                OR LOWER(CONCAT(dataMiningVariable, '(', REGEXP_REPLACE(product, '\\([0-9]+\\)\\s*\\([A-Z\\s]+\\),', ',', 'gi'), ')')) LIKE '%${searchString.replaceAll("'", "''")}%'
            """
            }
        }
        ""
    }

    static String groupSql(String groupIds){
        if(groupIds) {
            return """
            or (
                sharewithg2_.id in (
                    $groupIds
                )
            ) 
            """
        }
        ""
    }

    static String autoGroupSql(String groupIds){
        if(groupIds) {
            return """
            or (
                autosharew4_.id in (
                    $groupIds
                )
            ) 
            """
        }
        ""
    }

    static String roleSql(AlertDTO alertDTO) {
        String roles = ""
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%faers%') """
        }
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%vaers%') """
        }
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%vigibase%') """
        }
        if (!alertDTO.singleCaseRole && !alertDTO.aggRole && SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert' AND er.SELECTED_DATA_SOURCE like '%jader%') """
        }
        if (!alertDTO.singleCaseRole && alertDTO.aggRole) {
            roles += appendRole(roles)
            roles += """ (er.type='Aggregate Case Alert') """
        } else if(alertDTO.singleCaseRole && !alertDTO.aggRole) {
            roles += appendRole(roles)
            roles += """ (er.type='Single Case Alert') """
        }
        if (roles != "") {
            return roles += " )"
        } else {
            return roles
        }
    }

    static String appendRole(String role) {
        String roles = ""
        if (role == "") {
            roles = "AND ( "
        } else {
            roles = "OR "
        }
        return roles
    }

    static fetchCaseComments = {
        """
            select TENANT_ID, CASE_NUMBER, VERSION_NUM, CASE_ID, FOLLOW_UP_NUM,
            COMMENT_TXT, CONFIG_ID, EXECUTION_ID, CREATED_BY, CREATED_DATE, UPDATED_BY, UPDATED_DATE, ALERT_NAME,
            PRODUCT_FAMILY, PRODUCT_NAME, PRODUCT_ID, EVENT_NAME, PT_CODE 
            from vw_alert_comment
        """
    }

    static executeMigrationScript = {
        """
            "{call PKG_COMMENT.P_PVS_COMMENTS_MIG()}"
        """
    }

    static insert_gtt_cumulative_case_series = { Long execConfigId ->
        """
            insert into GTT_FILTER_KEY_VALUES(CODE,TEXT)
            Select version_num, case_num from 
            ALRT_QRY_CASELST_QLC_$execConfigId
        """
    }

    static primary_suspect_sql = {
        """
              BEGIN
                   delete from GTT_QUERY_DETAILS;
                   delete from GTT_QUERY_SETS;
                   delete from GTT_REPORT_VAR_INPUT;
               INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values (1,null,null,0, 2,0);
               INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID) values (1,null,null,null, null,1,'AND',null,0);
               INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID,ADDL_PARAMS, IS_FIELD_COMPARE) values (1,1,'eventFirstEvent','EQUALS', '''Yes''',1,null,null,1,null, 0);
               END;
                    """
    }

    static deleted_cases_sql = {dataSource->
        "select to_number(JSON_VALUE(CONFIG_VALUE,'\$.KEY_VALUE')) as KEY_VALUE from VW_ADMIN_APP_CONFIG where CONFIG_KEY = 'ENABLE_REMOVE_DELETED_CASES' and APPLICATION_NAME ='${dataSource}'"
    }

    static case_history_sql = { String execConfigIds, String caseNumbers ->
        """
         select exec_config_id as execConfigId ,case_number as caseNumber,justification from (
              select exec_config_id,case_number,justification, 
              row_number() over (partition by exec_Config_Id, case_number order by last_updated desc) RNum
              from case_history
              where exec_config_id in ($execConfigIds) and case_number in ($caseNumbers)
         )
         where RNum=1
        """
    }

    static auto_shared_config_id_sql = { List configIds ->
        String result = ""
        String result1 = """
          SELECT CONFIG_ID as id FROM AUTO_SHARE_WITH_USER_ECONFIG """
        String result2 = """
          SELECT CONFIG_ID as id FROM AUTO_SHARE_WITH_GROUP_ECONFIG """
        String whereClause = ""
        configIds.collate(999).each{
            if(whereClause == ""){
                whereClause += "WHERE CONFIG_ID IN (${it.join(',')})"
            }
            else{
                whereClause += " OR CONFIG_ID IN  (${it.join(',')})"
            }

        }
        result = result1 + whereClause + " UNION " + result2 + whereClause
        return result

    }

    static date_range_sql = { List exDateRangeInfoIds ->
        String result = """
       select id, CAST(DATE_RNG_END_ABSOLUTE AS DATE) as dateRangeEndAbsolute , CAST(DATE_RNG_START_ABSOLUTE AS DATE) as dateRangeStartAbsolute , DATE_RNG_ENUM as dateRangeEnum
       from EX_ALERT_DATE_RANGE  """
        String whereClause = ""
        exDateRangeInfoIds.collate(999).each{
            if(whereClause == "") {
                whereClause += " where id in (${it.join(',')})"
            } else {
                whereClause += " or id in (${it.join(',')})"
            }
        }
        return result+whereClause
    }
    static signal_action_taken_sql = { List signalIdList ->
        String inCriteriaWhereClause = getDictProdNameINCriteria(signalIdList, 'validated_signal_id').toString()
        """
          select validated_signal_id as id,
                 action_taken_string as name 
          from validated_signal_action_taken 
          where $inCriteriaWhereClause
       """
    }

    static String signals_without_share_with =
        """
        select id from validated_signal where id not in ((select validated_signal_id from share_with_group_signal where share_with_groupid in
                                                          (select ID from GROUPS WHERE NAME IS NOT NULL)) 
                                                          union
                                                          (select validated_signal_id from SHARE_WITH_USER_SIGNAL where SHARE_WITH_USERID in
                                                          (select ID from PVUSER WHERE NAME IS NOT NULL)))
        """

    static signal_share_with_all = { String signalIds ->
        """
            INSERT INTO SHARE_WITH_GROUP_SIGNAL(SHARE_WITH_GROUPID,SHARE_WITH_GROUP_IDX,VALIDATED_SIGNAL_ID)
                (select (select ID from GROUPS WHERE NAME = 'All Users'),'0', ID from VALIDATED_SIGNAL where id in (${signalIds}))
        """
    }
    static date_range_sql_literature_alert = { String exDateRangeInfoIds ->
        """
       select
        id, DATE_RNG_END_ABSOLUTE as dateRangeEndAbsolute , DATE_RNG_START_ABSOLUTE as dateRangeStartAbsolute
      from
        EX_LITERATURE_DATE_RANGE
      where
        id in ($exDateRangeInfoIds)
        """
    }

    static date_range_sql_evdas_alert = { String exDateRangeInfoIds ->
        """
       select
        id, DATE_RNG_END_ABSOLUTE as dateRangeEndAbsolute , DATE_RNG_START_ABSOLUTE as dateRangeStartAbsolute
      from
        EX_EVDAS_DATE_RANGE
      where
        id in ($exDateRangeInfoIds)
        """
    }

    static batch_variables_sql = {
        "SELECT key_id, ui_label, use_case, paste_import_option,dic_level,dic_type,isautocomplete,validatable,DECODE_TABLE,DECODE_COLUMN FROM pvs_batch_signal_constants_dsp order by ui_label"
    }

    static find_meddra_field_sql={
        "SELECT key_id, ui_label, use_case,dic_level,dic_type,isautocomplete,validatable FROM pvs_batch_signal_constants_dsp where DECODE_TABLE like 'PVR%MD%'"
    }

    static batch_lot_status= {
        "SELECT vs FROM BatchLotStatus vs WHERE 1=1"
    }

    static batch_lot_status_with_columns = {
        "SELECT new Map(vs.batchId as batchId, vs.id as id , vs.batchDate as batchDate, vs.count as count,vs.validRecordCount as validRecordCount ,vs.invalidRecordCount as invalidRecordCount,vs.uploadedAt as uploadedAt,vs.addedBy as addedBy) FROM BatchLotStatus vs WHERE 1=1"
    }

    static batch_lot_status_update_to_started = {
        "update PVS_BS_APP_BATCH_LOT_STATUS set etl_start_date = sysdate where etl_status is null"
    }

    static batch_lot_status_update_to_completed = {
        "update PVS_BS_APP_BATCH_LOT_STATUS set etl_start_date = sysdate where etl_status = 'STARTED' "
    }

    static pvd_etl_status_completed = {
        "select etl_value from pvr_etl_constants where ETL_KEY = 'ETL_STATUS' and etl_value=3"
    }

    static batch_lot_status_count = {
        " SELECT count(vs.id) FROM BatchLotStatus vs WHERE 1 = 1 "
    }

    static product_groups_status = {
        "SELECT vs FROM ProductGroupStatus vs WHERE 1=1"
    }

    static product_groups_status_sql_qry = {
        "SELECT vs.id as id, vs.version as version, vs.unique_Identifier as uniqueIdentifier, vs.count as count, " +
                " vs.valid_Record_Count as validRecordCount, vs.invalid_Record_Count as invalidRecordCount, " +
                " vs.UPLOADED_DATE as uploadedAt, vs.added_By as addedBy , vs.is_Api_Processed as isApiProcessed " +
                " FROM PVS_BS_APP_PROD_GROUP_STATUS vs WHERE 1=1 "
    }

    static product_groups_status_with_columns = {
        "SELECT new Map(vs.uniqueIdentifier as uniqueIdentifier, vs.id as id , vs.count as count,vs.validRecordCount as validRecordCount ,vs.invalidRecordCount as invalidRecordCount,vs.uploadedAt as uploadedAt,vs.addedBy as addedBy) FROM ProductGroupStatus vs WHERE 1=1"
    }

    static product_groups_status_with_columns_sql_query = {
        "SELECT vs.id as id, vs.version as version, vs.unique_Identifier as uniqueIdentifier, vs.count as count, vs.valid_Record_Count as validRecordCount,  \n" +
                " vs.invalid_Record_Count as invalidRecordCount, vs.UPLOADED_DATE as uploadedAt, vs.added_By as addedBy , vs.is_Api_Processed as isApiProcessed " +
                " FROM PVS_BS_APP_PROD_GROUP_STATUS vs WHERE 1=1 "
    }

    static product_groups_status_count = {
        " SELECT count(vs.id) FROM ProductGroupStatus vs WHERE 1 = 1 "
    }

    static master_count_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_counts_status where execution_id = ${id}"
    }

    static master_ebgm_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_ebgm_status where execution_id = ${id}"
    }

    static master_prr_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_prr_status where execution_id = ${id}"
    }

    static master_dss_status_sql = { id ->
        "select CURRENT_STATUS from vw_check_alert_status where execution_id = ${id}"
    }

    static batch_lot_status_sql= {
        "SELECT vs.id as id , vs.batch_Id as batchId, " +
                " vs.date_Range as dateRange, " +
                " vs.count as count,vs.valid_Record_Count as validRecordCount ,vs.invalid_Record_Count as invalidRecordCount,vs.uploaded_date as uploadedAt, " +
                " vs.added_By as addedBy, vs.is_Api_Processed isApiProcessed , vs.Etl_Status etlStatus FROM PVS_BS_APP_BATCH_LOT_STATUS vs WHERE 1=1"
    }

    static batch_lot_status_with_columns_sql = {
        "SELECT vs.id as id , vs.batch_Id as batchId, " +
                " vs.date_Range as dateRange, " +
                " vs.count as count,vs.valid_Record_Count as validRecordCount ,vs.invalid_Record_Count as invalidRecordCount,vs.uploaded_date as uploadedAt, " +
                " vs.added_By as addedBy, vs.is_Api_Processed isApiProcessed, vs.Etl_Status etlStatus FROM PVS_BS_APP_BATCH_LOT_STATUS vs WHERE 1=1"
    }

    static batch_lot_status_date_range_sql = {
        "select listagg(concat(concat(NVL(START_DATE,''),' - '), NVL(END_DATE,'')),', ') \n" +
                " within group (order by concat(concat(NVL(START_DATE,''),' - '), NVL(END_DATE,'')))\n" +
                " from (select DISTINCT START_DATE, END_DATE from PVS_BS_APP_BATCH_LOT_DATA where 1 = 1 "
    }

    static batch_lot_status_count_sql = {
        " SELECT count(vs.id) FROM PVS_BS_APP_BATCH_LOT_STATUS vs WHERE 1 = 1 "
    }

    static batch_lot_etl_status_by_id = { id ->
        " select count(*) from PVS_BS_APP_BATCH_LOT_STATUS where ETL_STATUS='COMPLETED' and id = ${id} "
    }

    static update_batch_lot_status_count = { bl ->
        "update PVS_BS_APP_BATCH_LOT_STATUS set VALID_RECORD_COUNT = ${bl.getValidRecordCount()}, INVALID_RECORD_COUNT= ${bl.getInvalidRecordCount()} where id = ${bl.getId()} "
    }

    static batch_lot_status_update_to_failed = { id ->
        "update PVS_BS_APP_BATCH_LOT_STATUS set ETL_STATUS='FAILED' where id = ${id} "
    }

    static batch_lot_etl_not_successful_count = {
        " select count(*) from PVS_BS_APP_BATCH_LOT_DATA where \n" +
                " batch_lot_id in (select id from PVS_BS_APP_BATCH_LOT_STATUS where ETL_START_DATE = " +
                " (select MAX(ETL_START_DATE) from PVS_BS_APP_BATCH_LOT_STATUS) ) \n" +
                " and  (ETL_STATUS != 'SUCCESS' or VALIDATION_ERROR is not null) "
    }

    static batch_lot_etl_last_successful_date = {
        "select max(ETL_START_DATE) from (\n" +
                "select STATUS.id, status.IS_ETL_PROCESSED, status.ETL_START_DATE , \n" +
                "(select count(*) from PVS_BS_APP_BATCH_LOT_DATA where \n" +
                "batch_lot_id =STATUS.id  and  (ETL_STATUS = 'SUCCESS' and VALIDATION_ERROR is null)) cnt, \n" +
                "(select count(*) from PVS_BS_APP_BATCH_LOT_DATA where \n" +
                "batch_lot_id =STATUS.id  and  (ETL_STATUS != 'SUCCESS' or VALIDATION_ERROR is not null)) invalid_cnt \n" +
                "from PVS_BS_APP_BATCH_LOT_STATUS STATUS \n" +
                "where status.ETL_START_DATE is not null \n" +
                ") successfull_etl where cnt>0 and invalid_cnt=0 "
    }

    static aggregate_alert_product_group_update = { configuration ->
        "update RCONFIG set product_group_selection='${configuration.productGroupSelection}' where id = ${configuration.id}"
    }

    static String orderRefSql(String sort, String direction){
        if(!sort){
            "order by rm.priority desc NULLS LAST"
        } else if(sort == "name"){
            "order by lower(ar.input_name) $direction NULLS LAST"
        } else if(sort == "date"){
            "order by rd.date_created $direction NULLS LAST"
        }
    }

    static String fetchRefSql(Integer max, Integer offset){
        if(max>0){
            "OFFSET ${offset} ROWS FETCH NEXT ${max} ROWS ONLY"
        } else{
            ""
        }
    }

    static searchReferencesSql(String searchString) {
        if (searchString) {
            searchString = searchString.toLowerCase()
            String esc_char = ""
            if (searchString.contains('_')) {
                searchString = searchString.replaceAll("\\_", "!_%")
                esc_char = "!"
            } else if (searchString.contains('%')) {
                searchString = searchString.replaceAll("\\%", "!%%")
                esc_char = "!"
            }
            if (esc_char) {
                return """
                and (lower(ar.input_name) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                or lower(ar.reference_link) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'
                or lower(rd.created_by) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}')
                """
            } else {
                return """
                and (lower(ar.input_name) like '%${searchString.replaceAll("'", "''")}%'
                or lower(ar.reference_link) like '%${searchString.replaceAll("'", "''")}%'
                or lower(rd.created_by) like '%${searchString.replaceAll("'", "''")}%')
            """
            }
        }
        ""
    }
    static get_references_sql = { userId, isPinned, sort, direction, max, offset, searchString ->
        """
        select rd.id as referenceId, rm.is_pinned as pinned, rm.priority as priority
        from REFERENCE_DETAILS rd
        LEFT JOIN USER_REFERENCES_MAPPING rm ON rd.id = rm.reference_id
        LEFT JOIN ATTACHMENT_REFERENCE ar ON rd.ATTACHMENT_ID = ar.id
        where rm.is_deleted = false and rm.is_pinned =${isPinned} and rm.user_id = ${userId}
        ${searchReferencesSql(searchString)}
        ${(orderRefSql(sort, direction))}
        ${(fetchRefSql(max, offset))}
        """

    }
    static get_ref_count_sql = { userId, searchString ->
        """
        select count(*)
        from REFERENCE_DETAILS rd
        LEFT JOIN USER_REFERENCES_MAPPING rm ON rd.id = rm.reference_id
        LEFT JOIN ATTACHMENT_REFERENCE ar ON rd.ATTACHMENT_ID = ar.id
        where rm.is_deleted = false and rm.user_id = ${userId}
        ${searchReferencesSql(searchString)}
        """

    }

    static remove_reference_mapping_to_users_and_groups = {Long referenceId,List<Long> userIdList ->
        """
            delete from USER_REFERENCES_MAPPING where REFERENCE_ID = ${referenceId} and USER_ID not in (${userIdList.join(",")})
        """
    }

    static get_user_id_from_group_sql = { groupId ->
        """
        select user_id as userId from user_group_s where group_id = ${groupId}
        """
    }


    static get_user_group_mapping_count_sql = { roleId, userId ->
        """
select count(1) as fieldCount from USER_GROUP_MAPPING ugm left join USER_GROUP_ROLE ugr on ugm.GROUP_ID=ugr.USER_GROUP_ID where ugr.ROLE_ID=${
            roleId
        } and ugm.USER_ID=" ${ userId } 

        """
    }

    static is_migration_required = {
        "select param_value from cat_parameters where param_key = 'IS_WEBAPP_MIG_REQUIRED' "
    }
    static delete_agg_global_tags = {
        '''DELETE FROM agg_global_tags WHERE pvs_global_tag_id IN (SELECT id FROM pvs_global_tag) '''
    }
    static delete_single_global_tags = {
        '''DELETE FROM SINGLE_GLOBAL_TAGS WHERE pvs_global_tag_id IN (SELECT id FROM pvs_global_tag) '''
    }
    static update_agg_alerts = {
        "UPDATE AGG_ALERT SET global_identity_id = NULL"
    }
    static update_archived_agg_alerts = {
        "UPDATE ARCHIVED_AGG_ALERT SET global_identity_id = NULL"
    }
    static update_single_alerts = {
        "UPDATE SINGLE_CASE_ALERT SET global_identity_id = NULL"
    }
    static update_archived_single_alerts = {
        // global_identity_id is nullable false hence setting -1
        "UPDATE ARCHIVED_SINGLE_CASE_ALERT SET global_identity_id = -1"
    }
    static disable_constraint_agg_table = {
        '''ALTER TABLE AGG_ALERT DISABLE CONstraint FK9wophmgrppuk00kumrk81waqb'''
    }
    static disable_constraint_archived_agg_table = {
        "ALTER TABLE ARCHIVED_AGG_ALERT DISABLE CONstraint FKedhgc4ecfee0ta6ixe5syha4e"
    }
    static disable_constraint_single_table = {
        '''ALTER TABLE SINGLE_CASE_ALERT DISABLE CONstraint FK8IC0IQI8EYNBXKKWXROC6IO1R'''
    }
    static disable_constraint_archived_single_table = {
        "ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT DISABLE CONstraint FKQAXCHL4OFG9634TB244AW63E5"
    }
    static truncate_global_product_table = {
        'TRUNCATE TABLE GLOBAL_PRODUCT_EVENT'
    }
    static truncate_global_case_table = {
        'TRUNCATE TABLE GLOBAL_CASE'
    }
    static enable_constraint_agg_table = {
        'ALTER TABLE AGG_ALERT ENABLE CONstraint FK9wophmgrppuk00kumrk81waqb'
    }
    static enable_constraint_archived_agg_table = {
        'ALTER TABLE ARCHIVED_AGG_ALERT ENABLE CONstraint FKedhgc4ecfee0ta6ixe5syha4e'
    }
    static enable_constraint_single_table = {
        'ALTER TABLE SINGLE_CASE_ALERT ENABLE CONstraint FK8IC0IQI8EYNBXKKWXROC6IO1R'
    }
    static enable_constraint_archived_single_table = {
        'ALTER TABLE ARCHIVED_SINGLE_CASE_ALERT ENABLE CONstraint FKQAXCHL4OFG9634TB244AW63E5'
    }
    static insert_dummy_values_global_case = {
        'insert into GLOBAL_CASE(globalcaseid, version, case_id, version_num) values (-1,-1,-1,-1)'
    }
    static merge_agg_alerts = {
        '''MERGE INTO agg_alert a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON (( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || 'null')
                                        AND b.PRODUCT_KEY_ID = a.PROD_HIERARCHY_ID
                                        AND b.EVENT_KEY_ID   =   a.EVENT_HIERARCHY_ID)
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NULL  '''
    }
    static merge_single_alerts = {
        '''
        MERGE INTO SINGLE_CASE_ALERT a USING (
        SELECT
            *
        FROM
        global_case
        )
        b ON (b.CASE_ID = a.CASE_ID and b.VERSION_NUM = a.CASE_VERSION)
        WHEN MATCHED THEN UPDATE SET a.global_identity_id = b.GLOBALCASEID
        '''
    }
    static merge_agg_on_demand_alerts = {
        '''MERGE INTO agg_on_demand_alert a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON (( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || 'null')
                                        AND b.PRODUCT_KEY_ID = a.PROD_HIERARCHY_ID
                                        AND b.EVENT_KEY_ID   =   a.EVENT_HIERARCHY_ID)
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NULL  '''
    }
    static merge_single_on_demand_alerts = {
        '''
        MERGE INTO SINGLE_ON_DEMAND_ALERT a USING (
        SELECT
            *
        FROM
        global_case
        )
        b ON (b.CASE_ID = a.CASE_ID and b.VERSION_NUM = a.CASE_VERSION)
        WHEN MATCHED THEN UPDATE SET a.global_identity_id = b.GLOBALCASEID
        '''
    }
    static merge_archived_agg_alerts = {
        '''MERGE INTO ARCHIVED_AGG_ALERT a USING (
                                    SELECT
                                        *
                                    FROM
                                        global_product_event
                                )
                                b ON (( b.product_event_comb = a.product_id
                                                              || '-'
                                                              || a.pt_code
                                                              || '-'
                                                              || 'null' )
                                        AND b.PRODUCT_KEY_ID = a.PROD_HIERARCHY_ID
                                        AND b.EVENT_KEY_ID   =   a.EVENT_HIERARCHY_ID)
                                WHEN MATCHED THEN UPDATE SET a.global_identity_id = globalproducteventid
                                WHERE
                             a.smq_code IS NULL  '''
    }

    static merge_archived_single_alerts = {
        '''
        MERGE INTO ARCHIVED_SINGLE_CASE_ALERT a USING (
        SELECT
        *
        FROM
        global_case
        )
        b ON (b.CASE_ID = a.CASE_ID and b.VERSION_NUM = a.CASE_VERSION)
        WHEN MATCHED THEN UPDATE SET a.global_identity_id = b.GLOBALCASEID
        '''
    }

    static update_agg_alert_for_smq = {
       """UPDATE agg_alert tgt
            SET
            tgt.event_hierarchy_id = decode(tgt.smq_code, 1, 19, 18)
            WHERE
            tgt.smq_code IS NOT NULL
    """
    }


    static update_agg_on_demand_alert_for_smq = {
        """UPDATE agg_on_demand_alert tgt
            SET
            tgt.event_hierarchy_id = decode(tgt.smq_code, 1, 19, 18)
            WHERE
            tgt.smq_code IS NOT NULL
    """
    }

    static update_archived_agg_alert_for_smq = {
        """UPDATE archived_agg_alert tgt
            SET
            tgt.event_hierarchy_id = decode(tgt.smq_code, 1, 19, 18)
            WHERE
            tgt.smq_code IS NOT NULL
    """
    }

    static insert_agg_alerts = {
        """
        INSERT INTO AGG_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_PRODUCT_EVENT_ID)
        SELECT ID, GLOBAL_ID FROM PVS_GLOBAL_TAG 
        """
    }
    static insert_single_alerts = {
        """
        INSERT INTO SINGLE_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_CASE_ID)
        SELECT ID, GLOBAL_ID FROM PVS_GLOBAL_TAG
        """
    }
    static get_agg_sql = {
        """
        select id as id, product_id as productId, pt_code as ptCode, smq_code as smqCode, prod_hierarchy_id as prodHierarchyId, event_hierarchy_id as eventHierarchyId from agg_alert where global_identity_id is null
        """
    }
    static get_archived_agg_sql = {
        """
        select id as id, product_id as productId, pt_code as ptCode, smq_code as smqCode, prod_hierarchy_id as prodHierarchyId, event_hierarchy_id as eventHierarchyId from archived_agg_alert where global_identity_id is null
        """
    }
    static get_single_sql = {
        """
        select id as id, CASE_ID as caseId, COALESCE(CASE_VERSION,0) as versionNum from SINGLE_CASE_ALERT where global_identity_id is null
        """
    }
    static get_archived_single_sql = {
        """
        select id as id, CASE_ID as caseId, COALESCE(CASE_VERSION,0) as versionNum from ARCHIVED_SINGLE_CASE_ALERT where global_identity_id = -1
        """
    }
    static agg_count_due_date = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->
        """
    SELECT IDENTIFICATION, COUNT(1) 
    FROM (
        SELECT 
            CASE 
                WHEN DATE_TRUNC('day', DUE_DATE) < CURRENT_DATE THEN 'OLD' 
                WHEN DATE_TRUNC('day', DUE_DATE) = CURRENT_DATE THEN 'CURRENT' 
                WHEN DATE_TRUNC('day', DUE_DATE) > CURRENT_DATE THEN 'NEW' 
            END as IDENTIFICATION 
        FROM AGG_ALERT aa
        LEFT JOIN EX_RCONFIG rc ON rc.id = aa.exec_configuration_id
        LEFT JOIN DISPOSITION disp ON aa.disposition_id = disp.ID
        WHERE (aa.ASSIGNED_TO_ID = ${currentUserId} OR aa.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}))
        AND rc.adhoc_run = false 
        AND rc.is_deleted = false 
        AND rc.is_latest = true
        AND rc.is_enabled = true  
        AND disp.review_completed = false
        AND rc.SELECTED_DATA_SOURCE != 'jader' 
        AND rc.workflow_group = ${workflowGroupId}
    ) AS subquery1
    GROUP BY IDENTIFICATION
    """
    }

    static sca_count_due_date = { Long currentUserId, Long workflowGroupId, List<Long> groupIdList ->
        """
    SELECT IDENTIFICATION, COUNT(1) 
    FROM (
        SELECT 
            CASE 
                WHEN DATE_TRUNC('day', DUE_DATE) < CURRENT_DATE THEN 'OLD' 
                WHEN DATE_TRUNC('day', DUE_DATE) = CURRENT_DATE THEN 'CURRENT' 
                WHEN DATE_TRUNC('day', DUE_DATE) > CURRENT_DATE THEN 'NEW' 
            END as IDENTIFICATION 
        FROM SINGLE_CASE_ALERT sca
        LEFT JOIN EX_RCONFIG rc ON rc.id = sca.exec_config_id
        LEFT JOIN DISPOSITION disp ON sca.disposition_id = disp.ID
        WHERE (sca.ASSIGNED_TO_ID = ${currentUserId} OR sca.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")}))
        AND sca.IS_CASE_SERIES = false 
        AND rc.adhoc_run = false 
        AND rc.is_deleted = false 
        AND rc.is_latest = true  
        AND rc.is_enabled = true AND disp.review_completed = false 
        AND rc.workflow_group = ${workflowGroupId}
    ) AS subquery1
    GROUP BY IDENTIFICATION
    """
    }


    static String case_details_sections =
            """
        select id, section_name from case_details_section
        """

    static fetch_data_sheet_count = { searchedTerm="", String coreSheet, max, offset ->
        String whereClause = " where "
        String dataSheetClause = ""
        if(searchedTerm){
            dataSheetClause += " lower(display_col) like '%${searchedTerm.trim()}%' "
        }

        if (coreSheet && coreSheet == Constants.DatasheetOptions.CORE_SHEET) {
            if (dataSheetClause) {
                dataSheetClause += " and core_sheet = true "
            } else {
                dataSheetClause += " core_sheet = true "
            }
        }

        dataSheetClause = dataSheetClause ? whereClause + dataSheetClause : ""

        """
            select count(*) from vw_pvs_ds_pf_list 
            ${dataSheetClause}
            order by display_col asc
            //${fetchRefSql(max, offset)}
        """
    }


    static getListedNess = { executedId ->
        """
                  select Meddra_pt_code, Listedness_data from PVS_LISTEDNESS_${executedId}_AGG  
            """
    }

    static caseAlert_dashboard_due_date = {Long currentUserId, Long workflowGrpId,List<Long> groupIdList ->
        """ 
        select sum(case when DATE(due_date) < DATE(CURRENT_TIMESTAMP) then 1 else 0 end) as PASTCOUNT,
              sum(case when DATE(due_date) > DATE(CURRENT_TIMESTAMP) then 1 else 0 end) as FUTURECOUNT,
              sum(case when DATE(due_date) = DATE(CURRENT_TIMESTAMP)  then 1 else 0 end) as CURRENTCOUNT
        from ALERTS alert
        JOIN DISPOSITION disp ON (alert.disposition_id  = disp.ID)
        where  
        (alert.ASSIGNED_TO_ID = ${currentUserId}
            OR alert.ASSIGNED_TO_GROUP_ID IN (${groupIdList.join(",")})
        )
        AND disp.review_completed = false
        AND alert.WORKFLOW_GROUP = ${workflowGrpId}
         """
    }

    static rpt_to_ui_label_table = {->
        """
            SELECT * FROM VW_ICR_RPT_FIELD_MAPPING WHERE LANG_ID='en'
        """
    }
    static rpt_to_ui_label_table_with_params = {String uid -> // using '*' for default as no multilingual support in PVS as of now
        """
            SELECT * FROM VW_ICR_RPT_FIELD_MAPPING WHERE UNIQUE_FIELD_ID in ( '${uid}') AND LANG_ID = 'en'
        """
    }

    static rpt_to_ui_label_table_pvr = {->
        """
            SELECT * FROM PVR_RPT_FIELD_LABEL
        """
    }

    static disassociate_signal_from_pec = { String joinTableName, String joinColumnName, String alertIds ->
        """
delete from ${joinTableName} where ${joinColumnName} in (${alertIds})
"""
    }

    static disassociate_pec_from_signal = { String joinTableName, Long signalId ->
        """
delete from ${joinTableName} where VALIDATED_SIGNAL_ID = ${signalId}
"""
    }

    static delete_signal_by_ids = { String signalIdList ->
        """
            DO \$\$
            BEGIN
            DELETE FROM VALIDATED_SINGLE_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            
            DELETE FROM VALIDATED_LITERATURE_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_ALL_PRODUCT
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ADHOC_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ALERT_ACTIVITIES
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_AGG_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_EVDAS_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCHIVED_SCA
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCHIVED_LIT_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCHIVED_ACA
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ARCH_EVDAS_ALERTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ALERT_COMMENTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_ALERT_DOCUMENTS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VAL_SIGNAL_TOPIC_CATEGORY
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_GROUP
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VS_EVAL_METHOD
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_RCONFIG
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VS_EVDAS_CONFIG
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_LINKED_SIGNALS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_LINKED_SIGNALS
                WHERE LINKED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_SIG_STATUS_HISTORY
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM VALIDATED_SIGNAL_OUTCOMES
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SHARE_WITH_USER_SIGNAL
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SHARE_WITH_GROUP_SIGNAL
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            DELETE FROM SIGNAL_SIG_RMMS
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
            
            DELETE FROM SIGNAL_HISTORY
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );

            DELETE FROM VALIDATED_SIGNAL_ACTIONS
                WHERE VALIDATED_SIGNAL_ACTIONS_ID in ( ${signalIdList} );
           
            DELETE FROM SIGNAL_CHART
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );

            DELETE FROM VALIDATED_SIGNAL_ACTION_TAKEN
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );

            DELETE FROM MEETING_ATTACHMENTS where MEETING_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_ACTIONS where MEETING_ACTIONS_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_ACTIVITIES where  MEETING_ACTIVITIES_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_GUEST_ATTENDEE where MEETING_GUEST_ATTENDEE_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING_PVUSER where MEETING_ATTENDEES_ID in
            (select id from MEETING where VALIDATED_SIGNAL_ID in (${signalIdList}));

            DELETE FROM MEETING
                WHERE VALIDATED_SIGNAL_ID in ( ${signalIdList} );
    
            DELETE FROM VALIDATED_SIGNAL WHERE id in ( ${signalIdList} );
            EXCEPTION
                WHEN OTHERS THEN
            RAISE EXCEPTION 'An error occurred: %', SQLERRM;
            END \$\$;
            
        """
    }
    static getExConfigIdsForMasterAlertQuery = { Long masterId, Boolean isLatest ->
        String query = "select id from ex_rconfig where config_id in (select id from rconfig where master_config_id =${masterId})"
        if(isLatest){
            query += " and is_latest =true"
        }
        query
    }
    static updateConfigurationDeletionStatus ={ exConfigIds ->
        """
         update rconfig
         set next_run_date = null,
         deletion_status = 'DELETION_IN_PROGRESS',
         deletion_In_Progress=true
         where id in (select CONFIG_ID from ex_rconfig where id in (${exConfigIds}))
        """
    }
    static configurationDeletionCompleted ={ exConfigIds ->
        """
         update rconfig
         set deletion_status = 'DELETED',
         deletion_In_Progress = false
         where id in (select CONFIG_ID from ex_rconfig where id in (${exConfigIds}))
        """
    }
    static getCaseSeriesIds = { Long exConfigId ->
        "select pvr_case_series_id as id from ex_rconfig where id in (select distinct exec_config_id from single_case_alert where exec_config_id = ${exConfigId})"
    }

    static getReportId = { Long exConfigId ->
        "select report_id as id from ex_rconfig where id=${exConfigId}"
    }

    static get_manually_added_signal_count = {List exConfigIdList, String joinTable, String joinColumn ->
        """
        select count(*)
        from ${joinTable}
        where IS_CARRY_FORWARD is null
        and ${joinColumn} in (${exConfigIdList.join(",")})
        """
    }

    static get_carry_forward_signal_count = {List exConfigIdList, String joinTable, String joinColumn ->
        """
        select count(*)
        from ${joinTable}
        where IS_CARRY_FORWARD = true
        and ${joinColumn} in (${exConfigIdList.join(",")})
        """
    }

    static delete_carry_forward_signals = {List exConfigIdList, String joinTable, String joinColumn ->
        """
        delete
        from ${joinTable}
        where IS_CARRY_FORWARD = true
        and ${joinColumn} in (${exConfigIdList.join(",")})
        """
    }
    static validated_agg_alert_ids = { Long signalId, Date dateClosed ->
        "select agg_alert_id from VALIDATED_AGG_ALERTS where validated_signal_id = ${signalId}"
    }

    static validated_agg_alert_id_List = { List signalIds ->
        StringBuilder whereClause = new StringBuilder()
        signalIds.collate(999).each { batch ->
            if (whereClause.length() == 0) {
                whereClause.append(" where agg.VALIDATED_SIGNAL_ID in (")
            } else {
                whereClause.append(" or agg.VALIDATED_SIGNAL_ID in (")
            }
            whereClause.append(batch.join(',')).append(")")
        }

        """
    select
        agg_alert_id,
        validated_signal_id
    from
        VALIDATED_AGG_ALERTS agg
        inner join VALIDATED_SIGNAL vs on agg.VALIDATED_SIGNAL_ID = vs.id
    """ + whereClause.toString()
    }

    static validated_evdas_alert_ids_List = { List signalIds ->
        StringBuilder whereClause = new StringBuilder()
        signalIds.collate(999).each { batch ->
            if (whereClause.length() == 0) {
                whereClause.append(" where ev.VALIDATED_SIGNAL_ID in (")
            } else {
                whereClause.append(" or ev.VALIDATED_SIGNAL_ID in (")
            }
            whereClause.append(batch.join(',')).append(")")
        }

        """
    select
        evdas_alert_id,
        validated_signal_id
    from
        VALIDATED_EVDAS_ALERTS ev
        inner join VALIDATED_SIGNAL vs on ev.VALIDATED_SIGNAL_ID = vs.id
    """ + whereClause.toString()
    }


    static validated_evdas_alert_ids = { Long signalId, Date dateClosed ->
        "select evdas_alert_id from VALIDATED_EVDAS_ALERTS where validated_signal_id = ${signalId} "
    }

    static generic_comment_sql = { Long signalId ->
        "select GENERIC_COMMENT from VALIDATED_SIGNAL where ID = ${signalId}"
    }

    static single_case_attachments = { List<Long> configIdList ->
        String queryString = """
            select sca.CASE_NUMBER || '_' || sca.ALERT_CONFIGURATION_ID , sca.EXEC_CONFIG_ID
            from ATTACHMENT_LINK al
                     inner join SINGLE_CASE_ALERT sca
                                on
                                   al.REFERENCE_CLASS = 'com.rxlogix.signal.SingleCaseAlert' and
                                   al.REFERENCE_ID = sca.ID and
        """
        if(configIdList.size()>1000){
            List<List<Long>> configIdSubList = configIdList.collate(1000)
            queryString += "( sca.ALERT_CONFIGURATION_ID IN (${configIdSubList[0].join(",").toString()})"
            configIdSubList.remove(0)
            configIdSubList.each {
                queryString += " OR sca.ALERT_CONFIGURATION_ID IN (${it.join(",").toString()})"
            }
            queryString += ")"
        }else{
            queryString += " sca.ALERT_CONFIGURATION_ID in (${configIdList.join(",")})"
        }
        queryString
    }

    static archived_single_case_attachments = { List<Long> configIdList ->
        String queryString = """
            select asca.CASE_NUMBER || '_' || asca.ALERT_CONFIGURATION_ID , asca.EXEC_CONFIG_ID
            from ATTACHMENT_LINK al
                     inner join ARCHIVED_SINGLE_CASE_ALERT asca
                                on
                                   al.REFERENCE_CLASS = 'com.rxlogix.signal.ArchivedSingleCaseAlert' and
                                   al.REFERENCE_ID = asca.ID and
        """
        if(configIdList.size()>1000){
            List<List<Long>> configIdSubList = configIdList.collate(1000)
            queryString += "( asca.ALERT_CONFIGURATION_ID IN (${configIdSubList[0].join(",").toString()})"
            configIdSubList.remove(0)
            configIdSubList.each {
                queryString += " OR asca.ALERT_CONFIGURATION_ID IN (${it.join(",").toString()})"
            }
            queryString += ")"
        }else{
            queryString += " asca.ALERT_CONFIGURATION_ID in (${configIdList.join(",")})"
        }
        queryString
    }
    static delete_roles_from_user = {Long roleId ->
        """
        DELETE FROM PVUSERS_ROLES WHERE ROLE_ID = ${roleId}
        """
    }

    static evdas_activity_from_dashboard = { Long userId ->
        """    
            select id as id from activities act inner join ex_evdas_config_activities execa on act.id = execa.activity_id 
            where act.assigned_to_id =${userId}
        """
    }

    static agg_activity_from_dashboard = { Long userId ->
        """
            select  id as id from activities act inner join ex_rconfig_activities execa on act.id = execa.activity_id 
            where performed_by_id =${userId} and case_number is null and suspect_product is not null ORDER BY id DESC
        """
    }
    static evdas_activity_list = { Long exeAlertId ->
        """
            select distinct disposition.display_name from evdas_alert
            inner join disposition on evdas_alert.disposition_id = disposition.id 
            where evdas_alert.exec_configuration_id = ${exeAlertId}
        """
    }
    static agg_activity_list = {  Long exeAlertId ->
        """
            select distinct disposition.display_name from agg_alert 
             inner join disposition on agg_alert.disposition_id = disposition.id 
            where agg_alert.exec_configuration_id = ${exeAlertId}
        """
    }
    static singleCase_activity_list = { Long exeAlertId ->
        """
            select distinct disposition.display_name from single_case_alert
            inner join disposition on single_case_alert.disposition_id = disposition.id
            where single_case_alert.exec_config_id = ${exeAlertId}
        """
    }
    static context_setting_pvs_func_creation = { ->
        """
            CREATE OR REPLACE FUNCTION f_security_management(
    p_schema text,
    p_obj text
)
RETURNS boolean AS \$\$
DECLARE
    lvc_predicate boolean;
BEGIN
    IF (current_setting('PVD_SECURITY_FIELDS.ENCryption_KEY') IN ('PVR', 'PVS', 'PVA', 'PVD', 'PVCM')) THEN
        lvc_predicate := TRUE;
    ELSE
        lvc_predicate := FALSE;
    END IF;
    RETURN lvc_predicate;
END;
\$\$ LANGUAGE plpgsql;
            
        """
    }

    static context_setting_pvs_drop_policies = { ->
        """
            DO \$\$
DECLARE
    r record;
BEGIN
    FOR r IN SELECT table_schema, table_name FROM information_schema.tables
               WHERE table_schema NOT IN ('pg_catalog', 'information_schema')
               AND EXISTS (
                   SELECT 1 FROM pg_policies
                   WHERE schemaname = table_schema
                   AND tablename = table_name
                   AND policyname = 'SCAPOLICY'
               )
    LOOP
        EXECUTE 'REVOKE POLICY SCAPOLICY ON TABLE ' || quote_ident(r.table_schema) || '.' || quote_ident(r.table_name) || ' CASCADE';
    END LOOP;
END \$\$;

        """
    }
    static enable_row_level_security_sca = {
        """ALTER TABLE single_case_alert ENABLE ROW LEVEL SECURITY""";
    }

    static context_setting_pvs_add_policy_SCA = { String schema ->
        """
           CREATE POLICY SCAPOLICY
ON SINGLE_CASE_ALERT
FOR ALL
TO public
USING (F_SECURITY_MANAGEMENT('DATE_OF_BIRTH', 'DATE_OF_BIRTH'));
        """
    }

    static enable_row_level_security_asca = {
        """            ALTER TABLE archived_single_case_alert ENABLE ROW LEVEL SECURITY;
"""
    }

    static context_setting_pvs_add_policy_ArchivedSCA = { String schema ->
        """
             CREATE POLICY SCAPOLICY
 ON ARCHIVED_SINGLE_CASE_ALERT
 FOR ALL
 TO public
 USING (F_SECURITY_MANAGEMENT('DATE_OF_BIRTH','DATE_OF_BIRTH'));

        """
    }

    static context_setting_pvs_replace_procedure_p_set_context_sec = {->
        """
             create or replace PROCEDURE p_set_context_sec (pi_owner varchar, pi_encryption_key varchar)
 LANGUAGE 'plpgsql'
 AS \$\$ 
 BEGIN
 PERFORM set_config(pi_owner || '.ENCRYPTION_KEY', pi_encryption_key, true);
 END; 
 \$\$;

        """
    }

    static context_setting_pvs_replace_procedure_p_set_context = {->
        """
             create or replace PROCEDURE p_set_context (pi_owner varchar, pi_encryption_key varchar)
 LANGUAGE 'plpgsql'
 AS \$\$
 BEGIN
     PERFORM set_config('PVD_SECURITY_FIELDS.ENCRYPTION_KEY', pi_encryption_key, false);
    
    CALL p_set_context_sec(pi_owner, pi_encryption_key);

 END;
 \$\$;
        """
    }

    static context_setting_pvs_p_set_context = {->
        """
            CALL p_set_context('PVD_SECURITY_FIELDS', 'PVS');
        """
    }

    static retrieve_justification_by_class_and_id = {String className, Long objectId->
        """
            SELECT JUSTIFICATION 
                    FROM ACTION_JUSTIFICATION AJ
            WHERE EXISTS (
                    SELECT 1
                        FROM jsonb_array_elements(AJ.ATTRIBUTES_MAP::jsonb->'INSTANCES_INFO') AS elem
                    WHERE elem->>'id' = '${objectId}'
            )
            AND POSTER_CLASS = '${className}'
        """
    }

    static icr_data_clean_for_failed_execution = { Long configId, Long exConfigId, Long failedExConfigId ->
        """
        DO \$\$
        DECLARE
        lvc_sql TEXT;
        lvc_exec_sql TEXT;
        BEGIN
        -- Concatenate column names excluding specific columns
        SELECT string_agg(column_name, ',' ORDER BY ordinal_position)
        INTO lvc_sql
        FROM information_schema.columns
        WHERE table_name = 'single_case_alert'
        AND column_name NOT IN ('json_field', 'reporter_qualification', 'risk_category');

        -- Dynamic SQL execution
        lvc_exec_sql := 'INSERT INTO single_case_alert (' || lvc_sql || ') ' ||
                'SELECT ' || lvc_sql || ' FROM archived_single_case_alert ' ||
                'WHERE alert_configuration_id = ' || ${configId} || ' ' ||
                'AND exec_config_id = ' || ${exConfigId};

        EXECUTE lvc_exec_sql;

        -- Insert into VALIDATED_SINGLE_ALERTS
        INSERT INTO validated_single_alerts(single_alert_id, validated_signal_id)
        SELECT vsca.archived_sca_id, vsca.validated_signal_id
        FROM validated_archived_sca vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_CASE_ALERT_TAGS
        INSERT INTO single_case_alert_tags(single_alert_id, pvs_alert_tag_id)
        SELECT vsca.single_alert_id, vsca.pvs_alert_tag_id
        FROM archived_sca_tags vsca
        INNER JOIN single_case_alert sca
        ON vsca.single_alert_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_PT
        INSERT INTO single_alert_pt(single_alert_id, sca_pt, pt_list_idx)
        SELECT vsca.archived_sca_id, vsca.archived_sca_pt, vsca.pt_list_idx
        FROM archived_sca_pt vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_CON_COMIT
        INSERT INTO single_alert_con_comit(single_alert_id, alert_con_comit, con_comit_list_idx)
        SELECT vsca.archived_sca_id, vsca.alert_con_comit, vsca.con_comit_list_idx
        FROM archived_sca_con_comit vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_SUSP_PROD
        INSERT INTO single_alert_susp_prod(SINGLE_ALERT_ID, SCA_PRODUCT_NAME, SUSPECT_PRODUCT_LIST_IDX)
        SELECT vsca.archived_sca_id, vsca.SCA_PRODUCT_NAME, vsca.SUSPECT_PRODUCT_LIST_IDX
        FROM ARCHIVED_SCA_SUSP_PROD vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_MED_ERR_PT_LIST
        INSERT INTO SINGLE_ALERT_MED_ERR_PT_LIST(SINGLE_ALERT_ID, SCA_MED_ERROR, MED_ERROR_PT_LIST_IDX)
        SELECT vsca.archived_sca_id, vsca.SCA_MED_ERROR, vsca.MED_ERROR_PT_LIST_IDX
        FROM ARCHIVED_SCA_MED_ERR_PT_LIST vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_ACTIONS
        INSERT INTO SINGLE_ALERT_ACTIONS(SINGLE_CASE_ALERT_ID, ACTION_ID, IS_RETAINED)
        SELECT vsca.ARCHIVED_SCA_ID, vsca.ACTION_ID, vsca.IS_RETAINED
        FROM ARCHIVED_SCA_ACTIONS vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_INDICATION_LIST
        INSERT INTO SINGLE_ALERT_INDICATION_LIST(SINGLE_ALERT_ID, SCA_INDICATION, indication_list_idx)
        SELECT vsca.archived_sca_id, vsca.SCA_INDICATION, vsca.indication_list_idx
        FROM AR_SIN_ALERT_INDICATION_LIST vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_CAUSE_OF_DEATH
        INSERT INTO SINGLE_ALERT_CAUSE_OF_DEATH(SINGLE_ALERT_ID, SCA_CAUSE_OF_DEATH, cause_of_death_list_idx)
        SELECT vsca.archived_sca_id, vsca.SCA_CAUSE_OF_DEATH, vsca.cause_of_death_list_idx
        FROM AR_SIN_ALERT_CAUSE_OF_DEATH vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Insert into SINGLE_ALERT_PAT_MED_HIST
        INSERT INTO SINGLE_ALERT_PAT_MED_HIST(SINGLE_ALERT_ID, SCA_PAT_MED_HIST, patient_med_hist_list_idx)
        SELECT vsca.archived_sca_id, vsca.SCA_PAT_MED_HIST, vsca.patient_med_hist_list_idx
        FROM AR_SIN_ALERT_PAT_MED_HIST vsca
        INNER JOIN archived_single_case_alert sca
        ON vsca.archived_sca_id = sca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_config_id = ${exConfigId};

INSERT into SINGLE_ALERT_PAT_HIST_DRUGS(SINGLE_ALERT_ID,SCA_PAT_HIST_DRUGS,patient_hist_drugs_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_PAT_HIST_DRUGS, vsca.patient_hist_drugs_list_idx
               FROM AR_SIN_ALERT_PAT_HIST_DRUGS vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
            INSERT into SINGLE_ALERT_BATCH_LOT_NO(SINGLE_ALERT_ID,SCA_BATCH_LOT_NO,batch_lot_no_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_BATCH_LOT_NO, vsca.batch_lot_no_list_idx
               FROM AR_SIN_ALERT_BATCH_LOT_NO vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
             INSERT into SINGLE_ALERT_CASE_CLASSIFI(SINGLE_ALERT_ID,SCA_CASE_CLASSIFICATION,case_classification_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_CASE_CLASSIFICATION, vsca.case_classification_list_idx
               FROM AR_SIN_ALERT_CASE_CLASSIFI vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
             INSERT into SINGLE_ALERT_THERAPY_DATES(SINGLE_ALERT_ID,SCA_THERAPY_DATES,therapy_dates_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_THERAPY_DATES, vsca.therapy_dates_list_idx
               FROM AR_SIN_ALERT_THERAPY_DATES vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
                    
            INSERT into SINGLE_ALERT_DOSE_DETAILS(SINGLE_ALERT_ID,SCA_DOSE_DETAILS,dose_details_list_idx) 
        SELECT vsca.ARCHIVED_SCA_ID, vsca.SCA_DOSE_DETAILS, vsca.dose_details_list_idx
               FROM AR_SIN_ALERT_DOSE_DETAILS vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.ARCHIVED_SCA_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};
                    
                    
            INSERT into SINGLE_ALERT_GENERIC_NAME(SINGLE_ALERT_ID,GENERIC_NAME,GENERIC_NAME_LIST_IDX) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.GENERIC_NAME, vsca.GENERIC_NAME_LIST_IDX
               FROM AR_SINGLE_ALERT_GENERIC_NAME vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};

        INSERT into SINGLE_ALERT_ALLPT_OUT_COME(SINGLE_ALERT_ID,ALLPTS_OUTCOME,ALLPTS_OUTCOME_LIST_IDX) 
        SELECT vsca.SINGLE_ALERT_ID, vsca.ALLPTS_OUTCOME, vsca.ALLPTS_OUTCOME_LIST_IDX
               FROM AR_SINGLE_ALERT_ALLPT_OUT_COME vsca
                INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
                    ON vsca.SINGLE_ALERT_ID = sca.ID
                    WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId};

        -- MERGE equivalent in PostgreSQL using INSERT ON CONFLICT
        INSERT INTO attachment_link (reference_id, reference_class)
        SELECT t1.reference_id, 'com.rxlogix.signal.SingleCaseAlert'
        FROM attachment_link t1
        LEFT JOIN archived_single_case_alert t2 ON t1.reference_id = t2.id
        WHERE t1.reference_class = 'com.rxlogix.signal.ArchivedSingleCaseAlert'
        AND alert_configuration_id = configId
        AND exec_config_id = exConfigId
        ON CONFLICT (reference_id) DO UPDATE
        SET reference_class = 'com.rxlogix.signal.SingleCaseAlert';

        -- Update case_history
        UPDATE case_history
        SET single_alert_id = archived_single_alert_id,
            archived_single_alert_id = NULL
        WHERE config_id = configId
        AND exec_config_id = exConfigId;

        -- DELETE statements for various tables
        DELETE FROM validated_archived_sca
        WHERE archived_sca_id IN (
                SELECT vsca.archived_sca_id
                FROM validated_archived_sca vsca
                INNER JOIN archived_single_case_alert sca
                ON vsca.archived_sca_id = sca.id
                WHERE alert_configuration_id = configId
                AND exec_config_id = exConfigId
        );

        DELETE FROM ARCHIVED_SCA_TAGS WHERE (SINGLE_ALERT_ID) in (
       SELECT vsca.SINGLE_ALERT_ID
       FROM ARCHIVED_SCA_TAGS vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.SINGLE_ALERT_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        DELETE FROM ARCHIVED_SCA_PT WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_PT vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_CON_COMIT WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_CON_COMIT vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_SUSP_PROD WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_SUSP_PROD vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_MED_ERR_PT_LIST WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_MED_ERR_PT_LIST vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId}
        );
        
        DELETE FROM ARCHIVED_SCA_ACTIONS WHERE (ARCHIVED_SCA_ID) in (
       SELECT vsca.ARCHIVED_SCA_ID
       FROM ARCHIVED_SCA_ACTIONS vsca
        INNER JOIN ARCHIVED_SINGLE_CASE_ALERT sca
            ON vsca.ARCHIVED_SCA_ID = sca.ID
            WHERE ALERT_CONFIGURATION_ID = ${configId} and EXEC_CONFIG_ID = ${exConfigId});

        -- Final DELETE statements for SINGLE_CASE_ALERT and other tables
        DELETE FROM single_case_alert
        WHERE exec_config_id = failedExConfigId
        AND alert_configuration_id = configId;

        DELETE FROM case_history
        WHERE exec_config_id = failedExConfigId;

        DELETE FROM ex_rconfig_activities
        WHERE ex_config_activities_id = failedExConfigId;

        DELETE FROM single_case_alert_tags
        WHERE pvs_alert_tag_id IN (
                SELECT id
                FROM pvs_alert_tag
                WHERE exec_config_id = failedExConfigId
        );

        DELETE FROM pvs_global_tag
        WHERE exec_config_id = failedExConfigId
        AND domain = 'Single Case Alert'
        AND created_at > (
                SELECT date_created
        FROM ex_rconfig
        WHERE id = failedExConfigId
        );

        DELETE FROM pvs_alert_tag
        WHERE exec_config_id = failedExConfigId
        AND domain = 'Single Case Alert';

        -- Exception handling in PostgreSQL
        EXCEPTION
        WHEN OTHERS THEN
        RAISE EXCEPTION 'An error occurred: %', SQLERRM;

        END \$\$;
        """

    }

    static agg_data_clean_for_failed_execution = { Long configId, Long exConfigId, Long failedExConfigId ->
        """
        DO \$\$
        DECLARE
        lvc_sql TEXT;
        lvc_exec_sql TEXT;
        BEGIN
        -- Concatenate column names excluding specific columns
        SELECT string_agg(column_name, ',' ORDER BY ordinal_position)
        INTO lvc_sql
        FROM information_schema.columns
        WHERE table_name = 'agg_alert';

        -- Dynamic SQL execution
        lvc_exec_sql := 'INSERT INTO agg_alert (' || lvc_sql || ') ' ||
                'SELECT ' || lvc_sql || ' FROM archived_agg_alert ' ||
                'WHERE alert_configuration_id = ' || ${configId} || ' ' ||
                'AND exec_configuration_id = ' || ${exConfigId};

        EXECUTE lvc_exec_sql;

        -- Insert into VALIDATED_AGG_ALERTS
        INSERT INTO validated_agg_alerts(agg_alert_id, validated_signal_id)
        SELECT vaca.archived_aca_id, vaca.validated_signal_id
        FROM validated_archived_aca vaca
        INNER JOIN archived_agg_alert aca
        ON vaca.archived_aca_id = aca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_configuration_id = ${exConfigId};

        -- Insert into AGG_CASE_ALERT_TAGS
        INSERT INTO agg_case_alert_tags(agg_alert_id, pvs_alert_tag_id)
        SELECT vaca.agg_alert_id, vaca.pvs_alert_tag_id
        FROM archived_agg_case_alert_tags vaca
        INNER JOIN archived_agg_alert aca
        ON vaca.agg_alert_id = aca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_configuration_id = ${exConfigId};

        -- Insert into AGG_ALERT_ACTIONS
        INSERT INTO agg_alert_actions(agg_alert_id, action_id, is_retained)
        SELECT vaca.archived_aca_id, vaca.action_id, vaca.is_retained
        FROM archived_aca_actions vaca
        INNER JOIN archived_agg_alert aca
        ON vaca.archived_aca_id = aca.id
        WHERE alert_configuration_id = ${configId}
        AND exec_configuration_id = ${exConfigId};

        -- Use INSERT ... ON CONFLICT to handle the MERGE equivalent
        INSERT INTO attachment_link (reference_id, reference_class)
        SELECT t1.reference_id, 'com.rxlogix.signal.AggregateCaseAlert'
        FROM attachment_link t1
        LEFT JOIN archived_agg_alert t2 ON t1.reference_id = t2.id
        WHERE alert_configuration_id = ${configId}
        AND exec_configuration_id = ${exConfigId}
        AND t1.reference_class = 'com.rxlogix.signal.ArchivedAggregateCaseAlert'
        ON CONFLICT (reference_id) DO UPDATE
        SET reference_class = 'com.rxlogix.signal.AggregateCaseAlert';

        -- Update product_event_history
        UPDATE product_event_history
        SET archived_agg_case_alert_id = NULL,
            agg_case_alert_id = agg_case_alert_id
        WHERE config_id = ${configId}
        AND exec_config_id = ${exConfigId};

        -- Delete statements
        DELETE FROM validated_archived_aca
        WHERE archived_aca_id IN (
                SELECT vaca.archived_aca_id
                FROM validated_archived_aca vaca
                INNER JOIN archived_agg_alert aca
                ON vaca.archived_aca_id = aca.id
                WHERE alert_configuration_id = ${configId}
                AND exec_configuration_id = ${exConfigId}
        );

        DELETE FROM archived_agg_case_alert_tags
        WHERE agg_alert_id IN (
                SELECT vaca.agg_alert_id
                FROM archived_agg_case_alert_tags vaca
                INNER JOIN archived_agg_alert aca
                ON vaca.agg_alert_id = aca.id
                WHERE alert_configuration_id = ${configId}
                AND exec_configuration_id = ${exConfigId}
        );

        DELETE FROM archived_aca_actions
        WHERE archived_aca_id IN (
                SELECT vaca.archived_aca_id
                FROM archived_aca_actions vaca
                INNER JOIN archived_agg_alert aca
                ON vaca.archived_aca_id = aca.id
                WHERE alert_configuration_id = ${configId}
                AND exec_configuration_id = ${exConfigId}
        );

        DELETE FROM archived_agg_alert
        WHERE alert_configuration_id = ${configId}
        AND exec_configuration_id = ${exConfigId};

        DELETE FROM product_event_history
        WHERE exec_config_id = ${failedExConfigId};

        DELETE FROM ex_rconfig_activities
        WHERE ex_config_activities_id = ${failedExConfigId};

        DELETE FROM agg_global_tags
        WHERE pvs_global_tag_id IN (
                SELECT agt.pvs_global_tag_id
                FROM agg_global_tags agt
                INNER JOIN pvs_global_tag pgt
                ON agt.pvs_global_tag_id = pgt.id
                WHERE pgt.exec_config_id = ${failedExConfigId}
                AND pgt.domain = 'Aggregate Case Alert'
                AND agt.creation_date > (
                SELECT date_created
                FROM ex_rconfig
                WHERE id = ${failedExConfigId}
        )
        );

        DELETE FROM agg_case_alert_tags
        WHERE pvs_alert_tag_id IN (
                SELECT id
                FROM pvs_alert_tag
                WHERE exec_config_id = ${failedExConfigId}
        );

        DELETE FROM pvs_global_tag
        WHERE exec_config_id = ${failedExConfigId}
        AND domain = 'Aggregate Case Alert'
        AND created_at > (
                SELECT date_created
        FROM ex_rconfig
        WHERE id = ${failedExConfigId}
        );

        DELETE FROM pvs_alert_tag
        WHERE exec_config_id = ${failedExConfigId}
        AND domain = 'Aggregate Case Alert';

        DELETE FROM agg_alert
        WHERE alert_configuration_id = ${configId}
        AND exec_configuration_id = ${failedExConfigId};

        EXCEPTION
        WHEN OTHERS THEN
        RAISE EXCEPTION 'Error: %', SQLERRM;
        END \$\$;
        """


    }
    static retrieve_case_details_sections_mapping_info = { ->
        """
            select cds.ID, cds.SECTION_NAME, cds.SECTION_POSITION, cds.UD_SECTION_NAME, cds.SECTION_KEY, cds.IS_FULL_TEXT, cdfm.UI_LABEl , cdfm.FIELD_VARIABLE
            from
                case_details_section cds,
                case_details_field_mapping cdfm
            where
                cds.section_name = cdfm.section_name
                and cds.flag_enable = 1
                order by cds.section_position

        """
    }

    static insert_blinding_fields_into_gtt = { String field, def value ->
        "INSERT INTO GTT_PVS_COMMON(UD_TEXT_1,UD_NUMBER_1) VALUES('${field}',${value});\n"

    }

    static list_ICR_activities_by_executed_config = { String exConfigIds, String currentUsername, Map blindedData, int isAllFieldsRedacted ->
        """
SELECT
    CASE 
        WHEN 'masterCaseNum' in (${blindedData.redactedData}) THEN '${Constants.BlindingStatus.REDACTED}' 
        WHEN 'masterCaseNum' IN (${blindedData.blindedData}) THEN 
          CASE 
                WHEN sca.study_blinding_status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE act.case_number END
        WHEN 'masterCaseNum' in (${blindedData.availableData}) THEN act.case_number
        WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}'  
        ELSE act.case_number 
    END as caseNumber, 
    REGEXP_REPLACE(a.name, '\\\\s{2,}', ' ')                AS alertName,
    act.alert_id          AS alertId,
    act.id                AS activity_id,
    act.details,
    act.timestamp         AS timestamp,
    act.justification     AS justification,
    CASE 
        WHEN ('productProductId' in (${blindedData.redactedData}) OR 'productProductName' in (${blindedData.redactedData})) THEN '${Constants.BlindingStatus.REDACTED}' 
        WHEN ('productProductId' in (${blindedData.blindedData}) OR 'productProductName' in (${blindedData.blindedData})) THEN 
            CASE 
                WHEN sca.study_blinding_Status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE act.suspect_product END
        
        WHEN ('productProductId' in (${blindedData.availableData}) OR 'productProductName' in (${blindedData.availableData})) THEN act.suspect_product 
        WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}' 
        ELSE act.suspect_product 
    END as suspect,     
    CASE 
        WHEN ('masterPrimEvtPrefTerm' in (${blindedData.redactedData}) OR 'eventDescReptd' in (${blindedData.redactedData})) THEN '${Constants.BlindingStatus.REDACTED}'
        WHEN ('masterPrimEvtPrefTerm' IN (${blindedData.blindedData}) OR 'eventDescReptd' in (${blindedData.blindedData})) THEN 
            CASE 
                WHEN sca.study_blinding_status = true THEN '${Constants.BlindingStatus.BLINDED}' ELSE act.event_name END
        WHEN ('masterPrimEvtPrefTerm' in (${blindedData.availableData}) OR 'eventDescReptd' in (${blindedData.availableData})) THEN act.event_name  
        WHEN ${isAllFieldsRedacted} = 1 THEN '${Constants.BlindingStatus.REDACTED}'        
        ELSE act.event_name 
    END as eventName,
    CASE
        WHEN act.assigned_to_id IS NOT NULL THEN
            usr.full_name
        WHEN act.assigned_to_group_id IS NOT NULL THEN
            grp.name
        ELSE
            act.guest_attendee_email
    END AS currentAssignment,
    CASE 
        WHEN upper(usr_performed.full_name) = 'SYSTEM' THEN 'SYSTEM'
        ELSE usr_performed.full_name
    END as performedBy,
     STRING_AGG(department.department_name, ', ' ORDER BY department.department_name) AS currentAssignmentDept,
     STRING_AGG(performed_by_department.department_name, ', ' ORDER BY performed_by_department.department_name) AS performedByDept,
    act_type.value as type
FROM
    activities               act
    LEFT OUTER JOIN alerts                   a ON act.alert_id = a.id
    LEFT OUTER JOIN single_case_alert sca on (act.case_number = sca.case_number and sca.EXEC_CONFIG_ID in (${exConfigIds}))
    LEFT OUTER JOIN archived_single_case_alert asca on (act.case_number = asca.case_number and asca.EXEC_CONFIG_ID in (${exConfigIds}))
    LEFT OUTER JOIN pvuser                   usr ON act.assigned_to_id = usr.id
    LEFT OUTER JOIN groups                   grp ON act.assigned_to_group_id = grp.id
    LEFT OUTER JOIN pvuser usr_performed     ON act.performed_by_id = usr_performed.id  
    LEFT OUTER JOIN pvuser_user_department   pvuser_department ON act.assigned_to_id = pvuser_department.user_user_departments_id
    LEFT OUTER JOIN user_department          department ON pvuser_department.user_department_id = department.id
    LEFT OUTER JOIN pvuser_user_department   pvuser_department_performed ON act.performed_by_id = pvuser_department_performed.user_user_departments_id
    LEFT OUTER JOIN user_department          performed_by_department ON pvuser_department_performed.user_department_id = performed_by_department.id
    LEFT OUTER JOIN ACTIVITY_TYPE                act_type ON act.type_id = act_type.id
    
WHERE
    act.id IN (
        SELECT
            activity_id
        FROM
            ex_rconfig_activities
        WHERE
            ex_config_activities_id IN (
                (${exConfigIds})
            )
    ) 
    AND ( act.private_user_name IS NULL
        OR act.private_user_name = '${currentUsername}' )
GROUP BY
    a.name,
    act.alert_id,
    act.id,
    act.details,
    act.timestamp,
    act.justification,
    act.case_number,
    act.suspect_product,
    act.event_name,
    usr_performed.full_name,
    CASE
        WHEN act.assigned_to_id IS NOT NULL THEN
            usr.full_name
        WHEN act.assigned_to_group_id IS NOT NULL THEN
            grp.name
        ELSE
            act.guest_attendee_email
    END,
    act_type.value,
    sca.study_blinding_status,
    asca.study_blinding_status
"""
    }

    static list_activities_by_executed_config = { String exConfigIds, String currentUsername, String activityTable = '', String columnName = '' ->
        """
SELECT
    act.case_number       AS caseNumber,
    REGEXP_REPLACE(a.name, '\\\\s{2,}', ' ')                AS alertName,
    act.alert_id          AS alertId,
    act.id                AS activity_id,
    act.details           AS details,
    act.timestamp         AS timestamp,
    act.justification     AS justification,
    act.suspect_product   AS suspect,
    act.event_name        AS eventName,
    CASE
        WHEN act.assigned_to_id IS NOT NULL THEN
            usr.full_name
        WHEN act.assigned_to_group_id IS NOT NULL THEN
            grp.name
        ELSE
            act.guest_attendee_email
    END AS currentAssignment,
    CASE 
        WHEN upper(usr_performed.full_name) = '${Constants.Commons.SYSTEM}' THEN '${Constants.Commons.SYSTEM}'
        ELSE usr_performed.full_name
    END as performedBy,
    STRING_AGG(department.department_name, ', ' ORDER BY department.department_name) AS currentAssignmentDept,
    STRING_AGG(performed_by_department.department_name, ', ' ORDER BY performed_by_department.department_name) AS performedByDept,
    act_type.value as type
FROM
    activities               act
    LEFT OUTER JOIN alerts                   a ON act.alert_id = a.id
    LEFT OUTER JOIN pvuser                   usr ON act.assigned_to_id = usr.id
    LEFT OUTER JOIN groups                   grp ON act.assigned_to_group_id = grp.id
    LEFT OUTER JOIN pvuser usr_performed     ON act.performed_by_id = usr_performed.id  
    LEFT OUTER JOIN pvuser_user_department   pvuser_department ON act.assigned_to_id = pvuser_department.user_user_departments_id
    LEFT OUTER JOIN user_department          department ON pvuser_department.user_department_id = department.id
    LEFT OUTER JOIN pvuser_user_department   pvuser_department_performed ON act.performed_by_id = pvuser_department_performed.user_user_departments_id
    LEFT OUTER JOIN user_department          performed_by_department ON pvuser_department_performed.user_department_id = performed_by_department.id
    LEFT OUTER JOIN ACTIVITY_TYPE            act_type ON act.type_id = act_type.id
WHERE
    act.id IN (
        SELECT
            activity_id
        FROM
            ${activityTable?:'ex_rconfig_activities'}
        WHERE
            ${columnName?:'ex_config_activities_id'} IN (
                ${exConfigIds}
            )
    )
    AND ( act.private_user_name IS NULL
        OR act.private_user_name = '${currentUsername}' )    
GROUP BY
    a.name,
    act.alert_id,
    act.id,
    act.details,
    act.timestamp,
    act.justification,
    act.case_number,
    act.suspect_product,
    act.event_name,
    usr_performed.full_name,
    CASE
        WHEN act.assigned_to_id IS NOT NULL THEN
            usr.full_name
        WHEN act.assigned_to_group_id IS NOT NULL THEN
            grp.name
        ELSE
            act.guest_attendee_email
    END,
    act_type.value
"""
    }
    static child_drug_gtt_insert_query = {
        """ Insert into GTT_AGG_MASTER_CHILD_DTLS (MASTER_EXECUTION_ID, CHILD_EXECUTION_ID, ALERT_NAME, HIERARCHY_ID, BASE_ID, BASE_NAME) """

    }
}
