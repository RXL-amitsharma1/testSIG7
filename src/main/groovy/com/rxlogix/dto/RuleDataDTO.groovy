package com.rxlogix.dto

import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.Constants
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.PVSState
import com.rxlogix.signal.AggregateCaseAlert

class RuleDataDTO {

    Long id

    //Aggregated data
    Integer newCount = 0
    Integer cumCount = 0
    Integer newStudyCount = 0
    Integer cumStudyCount = 0
    Integer newSponCount = 0
    Integer cumSponCount = 0
    Integer newSeriousCount = 0
    Integer cumSeriousCount = 0
    Integer newFatalCount = 0
    Integer cumFatalCount = 0
    Integer cumInteractingCount = 0
    Integer newInteractingCount =0
    Integer newGeriatricCount = 0
    Integer cumGeriatricCount = 0
    Integer newNonSerious = 0
    Integer cumNonSerious = 0
    Boolean isDME = false
    Boolean isIME = false
    Boolean isStopList = false
    Boolean isSpecialMonitoring = false
    Boolean isDMEProd = false
    Boolean isIMEProd = false
    Boolean isStopListProd = false
    Boolean isSpecialMonitoringProd = false
    Double chiSquare = 0
    Boolean isNewEvent = false
    Integer newPediatricCount = 0
    Integer cumPediatricCount = 0

    //Previous Aggregate Data
    Map prevAggData = [:]

    //Previous EVDAS Data
    Map prevEvdasData = [:]

    //Evdas Data
    Integer newEv = 0
    Integer newEvEvdas = 0
    Integer totalEv = 0
    Integer totalEvEvdas = 0
    Integer newFatalEvdas = 0
    Integer totalFatalEvdas = 0
    Integer newSeriousEvdas = 0
    Integer totalSeriousEvdas = 0
    Integer newLitEvdas = 0
    Integer totalLitEvdas = 0
    Integer newPaedEvdas = 0
    Integer totalPaedEvdas = 0
    Integer newGeriatEvdas = 0
    Integer totalGeriatEvdas = 0
    Integer newEEAEvdas = 0
    Integer totEEAEvdas = 0
    Integer newHCPEvdas = 0
    Integer totHCPEvdas = 0
    Integer newObsEvdas = 0
    Integer totObsEvdas = 0
    Integer newMedErrEvdas = 0
    Integer totMedErrEvdas = 0
    Integer newPlusRCEvdas = 0
    Integer totPlusRCEvdas = 0
    Integer newSponEvdas = 0
    Integer totSponEvdas = 0
    Double rorEuropeEvdas = 0
    Double rorNAmericaEvdas = 0
    Double rorJapanEvdas = 0
    Double rorAsiaEvdas = 0
    Double rorRestEvdas = 0
    Double rorAllEvdas = 0
    String sdr
    String changes

    //EBGM values
    Double eb95 = 0
    Double eb05 = 0
    Double ebgm = 0
    Double prr = 0
    Double prrLCI = 0
    Double prrUCI = 0
    Double ror = 0
    Double rorLCI = 0
    Double rorUCI = 0

    Double prevEB95 = 0
    PVSState workflowState
    Disposition disposition

    Integer lastReviewDuration = 0
    boolean isLastReviewDuration = false

    Boolean isEvdasAlert = false
    String alertType
    String products
    String productSelection
    String dataSource
    Boolean isPEDetectedFirstTime = false

    List queryCaseMaps
    Long caseId

    String configProductSelection
    String listedness
    String positiveRechallenge
    String evdasListedness
    StringBuilder logString
    String trendType
    String trendFlag
    Integer newProdCount
    Integer cumProdCount
    Double freqPeriod
    Double cumFreqPeriod
    Double pecImpNumHigh
    //Update as per PVS-5177
    //adding counts
    Integer totSpontEurope = 0
    Integer totSpontNAmerica = 0
    Integer totSpontJapan = 0
    Integer totSpontAsia = 0
    Integer totSpontRest = 0

    //adding algos
    String dmeIme
    Double reltRorPadVsOhtr = 0
    String sdrPaed
    Double reltRorGrtVsOthr = 0
    String sdrGeratr
    Boolean isFirstExecution = true
    StratificationScoreDTO stratificationScoreDTO
    NewCountScoreDTO newCountScoreDTO
    Boolean isEvdasIntegratedReview = false
    AggregateCaseAlert previousAggAlert
    def previousEvdasAlert
    Long execConfigId
    Set previousTags
    Set allPrevTags
    Long ruleId
    BigInteger productId

    //VAERS data
    Integer newCountVaers = 0
    Integer cumCountVaers = 0
    Integer newFatalCountVaers = 0
    Integer cumFatalCountVaers = 0
    Integer newSeriousCountVaers = 0
    Integer cumSeriousCountVaers = 0
    Integer newGeriatricCountVaers = 0
    Integer cumGeriatricCountVaers = 0
    Integer newPediatricCountVaers = 0
    Integer cumPediatricCountVaers = 0
    Double prrValueVaers = 0
    Double prrLCIVaers = 0
    Double prrUCIVaers = 0
    Double rorValueVaers = 0
    Double rorLCIVaers = 0
    Double rorUCIVaers = 0
    Double ebgmVaers = 0
    Double eb05Vaers = 0
    Double eb95Vaers = 0
    Double chiSquareVaers = 0

    Double chiSquareFaers = 0
    Double newSponCountFaers=0
    Double cumSponCountFaers=0
    Double newStudyCountFaers=0
    Double cumStudyCountFaers=0
    Double newSeriousCountFaers=0
    Double cumSeriousCountFaers=0
    Double newFatalCountFaers=0
    Double cumFatalCountFaers=0
    Double newCountFaers = 0
    Double cumCountFaers = 0
    Double newNonSeriousFaers = 0
    Double cumNonSeriousFaers = 0
    Double newPediatricCountFaers=0
    Double cumPediatricCountFaers=0
    Double newGeriatricCountFaers=0
    Double cumGeriatricCountFaers=0
    Double newInteractingCountFaers=0
    Double cumInteractingCountFaers=0
    Double prrValueFaers=0
    Double prrLCIFaers=0
    Double prrUCIFaers=0
    Double rorValueFaers=0
    Double rorLCIFaers=0
    Double rorUCIFaers=0
    Double ebgmFaers=0
    Double eb05Faers=0
    Double eb95Faers=0

    //VIGIBASE data
    Integer newCountVigibase = 0
    Integer cumCountVigibase = 0
    Integer newFatalCountVigibase = 0
    Integer cumFatalCountVigibase = 0
    Integer newSeriousCountVigibase = 0
    Integer cumSeriousCountVigibase = 0
    Integer newGeriatricCountVigibase = 0
    Integer cumGeriatricCountVigibase = 0
    Integer newPediatricCountVigibase = 0
    Integer cumPediatricCountVigibase = 0
    Double prrValueVigibase = 0
    Double prrLCIVigibase = 0
    Double prrUCIVigibase = 0
    Double rorValueVigibase = 0
    Double rorLCIVigibase = 0
    Double rorUCIVigibase = 0
    Double ebgmVigibase = 0
    Double eb05Vigibase = 0
    Double eb95Vigibase = 0
    Double chiSquareVigibase = 0

    Double rrValue = 0
    Double eValue = 0
    Double rrValueFaers = 0
    Double eValueFaers = 0
    Double rrValueVaers = 0
    Double eValueVaers = 0
    Double rrValueVigibase = 0
    Double eValueVigibase = 0
    Map newCountsJson

    //JADER data
    Integer newCountJader = 0
    Integer cumCountJader = 0
    Integer newFatalCountJader = 0
    Integer cumFatalCountJader = 0
    Integer newSeriousCountJader = 0
    Integer cumSeriousCountJader = 0
    Integer newGeriatricCountJader = 0
    Integer cumGeriatricCountJader = 0
    Integer newPediatricCountJader = 0
    Integer cumPediatricCountJader = 0
    Double prrValueJader = 0
    Double prrLCIJader = 0
    Double prrUCIJader = 0
    Double rorValueJader = 0
    Double rorLCIJader = 0
    Double rorUCIJader = 0
    Double ebgmJader = 0
    Double eb05Jader = 0
    Double eb95Jader = 0
    Double chiSquareJader = 0
    Double rrValueJader = 0
    Double eValueJader = 0
    
    Map mapData() {
        Map ruleMap = [
                //Aggregate Data
                (Constants.BusinessConfigAttributes.PRR_SCORE)               : this.prr,
                (Constants.BusinessConfigAttributes.PRRLCI_SCORE)            : this.prrLCI,
                (Constants.BusinessConfigAttributes.PRRUCI_SCORE)            : this.prrUCI,
                (Constants.BusinessConfigAttributes.EBGM_SCORE)              : this.ebgm,
                (Constants.BusinessConfigAttributes.EB05_SCORE)              : this.eb05,
                (Constants.BusinessConfigAttributes.EB95_SCORE)              : this.eb95,
                (Constants.BusinessConfigAttributes.ROR_SCORE)               : this.ror,
                (Constants.BusinessConfigAttributes.RORLCI_SCORE)            : this.rorLCI,
                (Constants.BusinessConfigAttributes.RORUCI_SCORE)            : this.rorUCI,
                (Constants.BusinessConfigAttributes.NEW_COUNT)               : this.newCount,
                (Constants.BusinessConfigAttributes.CUMM_COUNT)              : this.cumCount,
                (Constants.BusinessConfigAttributes.NEW_SPON_COUNT)          : this.newSponCount,
                (Constants.BusinessConfigAttributes.CUM_SPON_COUNT)          : this.cumSponCount,
                (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT)       : this.newSeriousCount,
                (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT)       : this.cumSeriousCount,
                (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT)         : this.newFatalCount,
                (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT)         : this.cumFatalCount,
                (Constants.BusinessConfigAttributes.NEW_STUDY_COUNT)         : this.newStudyCount,
                (Constants.BusinessConfigAttributes.CUM_STUDY_COUNT)         : this.cumStudyCount,
                (Constants.BusinessConfigAttributes.NEW_INTERACTING_COUNT)       : this.newInteractingCount,
                (Constants.BusinessConfigAttributes.CUMM_INTERACTING_COUNT)      : this.cumInteractingCount,
                (Constants.BusinessConfigAttributes.NEW_PEDIATRIC_COUNT)       : this.newPediatricCount,
                (Constants.BusinessConfigAttributes.CUM_PEDIATRIC_COUNT)      : this.cumPediatricCount,
                (Constants.BusinessConfigAttributes.NEW_NON_SERIOUS_COUNT)       : this.newNonSerious,
                (Constants.BusinessConfigAttributes.CUM_NON_SERIOUS_COUNT)      : this.cumNonSerious,
                (Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT)         : this.newGeriatricCount,
                (Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT)         : this.cumGeriatricCount,
                (Constants.BusinessConfigAttributes.NEW_NON_SERIOUS_COUNT)             : this.newNonSerious,
                (Constants.BusinessConfigAttributes.CUM_NON_SERIOUS_COUNT)             : this.cumNonSerious,
                (Constants.BusinessConfigAttributes.DME_AGG_ALERT)               : this.isDME ? Constants.Commons.YES_LOWERCASE : (this.isDME == false ? Constants.Commons.NO_LOWERCASE : null),
                (Constants.BusinessConfigAttributes.IME_AGG_ALERT)               : this.isIME ? Constants.Commons.YES_LOWERCASE : (this.isIME == false ? Constants.Commons.NO_LOWERCASE : null),
                (Constants.BusinessConfigAttributes.SPECIAL_MONITORING)          : this.isSpecialMonitoring ? Constants.Commons.YES_LOWERCASE : (this.isSpecialMonitoring == false ? Constants.Commons.NO_LOWERCASE : null),
                (Constants.BusinessConfigAttributes.STOP_LIST)                   : this.isStopList ? Constants.Commons.YES_LOWERCASE : (this.isStopList == false ? Constants.Commons.NO_LOWERCASE : null),
                DME_PROD                                                         : this.isDMEProd ? Constants.Commons.YES_LOWERCASE : (this.isDMEProd == false ? Constants.Commons.NO_LOWERCASE : null),
                IME_PROD                                                         : this.isIMEProd ? Constants.Commons.YES_LOWERCASE : (this.isIMEProd == false ? Constants.Commons.NO_LOWERCASE : null),
                SPECIAL_MONITORING_PROD                                          : this.isSpecialMonitoringProd ? Constants.Commons.YES_LOWERCASE : (this.isSpecialMonitoringProd == false ? Constants.Commons.NO_LOWERCASE : null),
                STOP_LIST_PROD                                                   : this.isStopListProd ? Constants.Commons.YES_LOWERCASE : (this.isStopListProd == false ? Constants.Commons.NO_LOWERCASE : null),
                (Constants.BusinessConfigAttributes.POSITIVE_RE_CHALLENGE)       : this.positiveRechallenge,
                (Constants.BusinessConfigAttributes.CHI_SQUARE)                  : this.chiSquare,
                trendType                                                        : this.trendType,
                (Constants.BusinessConfigAttributes.TREND_FLAG)                  : this.trendFlag,
                (Constants.BusinessConfigAttributes.FREQ_PERIOD)                 : this.freqPeriod,
                (Constants.BusinessConfigAttributes.CUM_FREQ_PERIOD)             : this.cumFreqPeriod,
                (Constants.BusinessConfigAttributes.NEW_PROD_COUNT)              : this.newProdCount,
                (Constants.BusinessConfigAttributes.NEW_CUM_COUNT)               : this.cumProdCount,
                (Constants.BusinessConfigAttributes.DSS_SCORE)                   : this.pecImpNumHigh,
                (Constants.BusinessConfigAttributes.RR_VALUE)                    : this.rrValue,
                (Constants.BusinessConfigAttributes.E_VALUE)                     : this.eValue,

                //Evdas Data
                (Constants.BusinessConfigAttributesEvdas.NEW_EV_EVDAS)                : this.newEvEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_EV_EVDAS)              : this.totalEvEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_EEA_EVDAS)               : this.newEEAEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_EEA_EVDAS)             : this.totEEAEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_HCP_EVDAS)               : this.newHCPEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_HCP_EVDAS)             : this.totHCPEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_SERIOUS_EVDAS)           : this.newSeriousEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_SERIOUS_EVDAS)         : this.totalSeriousEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_OBS_EVDAS)               : this.newObsEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_OBS_EVDAS)             : this.totObsEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_FATAL_EVDAS)             : this.newFatalEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_FATAL_EVDAS)           : this.totalFatalEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_MED_ERR_EVDAS)           : this.newMedErrEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_MED_ERR_EVDAS)         : this.totMedErrEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_PLUS_RC_EVDAS)           : this.newPlusRCEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_PLUS_RC_EVDAS)         : this.totPlusRCEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_LITERATURE_EVDAS)        : this.newLitEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_LITERATURE_EVDAS)      : this.totalLitEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_PAED_EVDAS)              : this.newPaedEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_PAED_EVDAS)            : this.totalPaedEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_GERIAT_EVDAS)            : this.newGeriatEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_GERIAT_EVDAS)          : this.totalGeriatEvdas,
                (Constants.BusinessConfigAttributesEvdas.NEW_SPON_EVDAS)              : this.newSponEvdas,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EVDAS)            : this.totSponEvdas,

                (Constants.BusinessConfigAttributesEvdas.ROR_EUROPE_EVDAS)            : this.rorEuropeEvdas,
                (Constants.BusinessConfigAttributesEvdas.ROR_N_AMERICA_EVDAS)         : this.rorNAmericaEvdas,
                (Constants.BusinessConfigAttributesEvdas.ROR_JAPAN_EVDAS)             : this.rorJapanEvdas,
                (Constants.BusinessConfigAttributesEvdas.ROR_ASIA_EVDAS)              : this.rorAsiaEvdas,
                (Constants.BusinessConfigAttributesEvdas.ROR_REST_EVDAS)              : this.rorRestEvdas,
                (Constants.BusinessConfigAttributesEvdas.ROR_ALL_EVDAS)               : this.rorAllEvdas,
                (Constants.BusinessConfigAttributesEvdas.SDR_EVDAS)                   : this.sdr,
                (Constants.BusinessConfigAttributesEvdas.CHANGES_EVDAS)               : this.changes,
                (Constants.BusinessConfigAttributesEvdas.EVDAS_LISTEDNESS)            : this.evdasListedness,

                //Common Data
                (Constants.BusinessConfigAttributes.LISTEDNESS)                       : this.listedness,

                //EVDAS updates as per PVS-5177
                //counts add ons
                (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EUROPE)           : this.totSpontEurope,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_N_AMERICA)        : this.totSpontNAmerica,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_JAPAN)            : this.totSpontJapan,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_ASIA)             : this.totSpontAsia,
                (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_REST)             : this.totSpontRest,

                //algorithm add ons
                (Constants.BusinessConfigAttributesEvdas.EVDAS_IME_DME)               : this.dmeIme,
                (Constants.BusinessConfigAttributesEvdas.RELTV_ROR_PAED_VS_OTHR)      : this.reltRorPadVsOhtr,
                (Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_PAED)              : this.sdrPaed,
                (Constants.BusinessConfigAttributesEvdas.RELTV_ROR_GERTR_VS_OTHR)     : this.reltRorGrtVsOthr,
                (Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_GERTR)             : this.sdrGeratr,
                (Constants.BusinessConfigAttributesEvdas.NEW_EVENT)                   : this.isNewEvent ? Constants.Commons.YES_LOWERCASE : Constants.Commons.NO_LOWERCASE,
                (Constants.BusinessConfigAttributesEvdas.PREVIOUS_CATEGORY)           : this.previousTags,
                (Constants.BusinessConfigAttributesEvdas.ALL_CATEGORY)                : this.allPrevTags,
                (Constants.BusinessConfigAttributesEvdas.NEW_PAED_EVDAS)        : this.newPediatricCount,
                (Constants.BusinessConfigAttributesEvdas.NEW_PAED_EVDAS)        : this.cumPediatricCount,
                //VAERS BR
                (Constants.BusinessConfigAttributes.NEW_COUNT_VAERS)             : this.newCountVaers,
                (Constants.BusinessConfigAttributes.CUMM_COUNT_VAERS)            : this.cumCountVaers,
                (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VAERS)       : this.newFatalCountVaers,
                (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VAERS)       : this.cumFatalCountVaers,
                (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VAERS)     : this.newSeriousCountVaers,
                (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VAERS)     : this.cumSeriousCountVaers,
                (Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VAERS)   : this.newGeriatricCountVaers,
                (Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VAERS)   : this.cumGeriatricCountVaers,
                (Constants.BusinessConfigAttributes.NEW_PAED_VAERS)              : this.newPediatricCountVaers,
                (Constants.BusinessConfigAttributes.CUM_PAED_VAERS)              : this.cumPediatricCountVaers,

                (Constants.BusinessConfigAttributes.PRR_SCORE_VAERS)             : this.prrValueVaers,
                (Constants.BusinessConfigAttributes.PRRLCI_SCORE_VAERS)          : this.prrLCIVaers,
                (Constants.BusinessConfigAttributes.PRRUCI_SCORE_VAERS)          : this.prrUCIVaers,
                (Constants.BusinessConfigAttributes.ROR_SCORE_VAERS)             : this.rorValueVaers,
                (Constants.BusinessConfigAttributes.RORLCI_SCORE_VAERS)          : this.rorLCIVaers,
                (Constants.BusinessConfigAttributes.RORUCI_SCORE_VAERS)          : this.rorUCIVaers,
                (Constants.BusinessConfigAttributes.EBGM_SCORE_VAERS)            : this.ebgmVaers,
                (Constants.BusinessConfigAttributes.EB05_SCORE_VAERS)            : this.eb05Vaers,
                (Constants.BusinessConfigAttributes.EB95_SCORE_VAERS)            : this.eb95Vaers,
                (Constants.BusinessConfigAttributes.CHI_SQUARE_VAERS)            : this.chiSquareVaers,
                (Constants.BusinessConfigAttributes.RR_VALUE_VAERS)              : this.rrValueVaers,
                (Constants.BusinessConfigAttributes.E_VALUE_VAERS)               : this.eValueVaers,

                //Faers fields should be added here
                (Constants.BusinessConfigAttributes.NEW_COUNT_FAERS)        : this.newCountFaers,
                (Constants.BusinessConfigAttributes.CUMM_COUNT_FAERS)       : this.cumCountFaers,
                (Constants.BusinessConfigAttributes.NEW_PAED_FAERS)         : this.newPediatricCountFaers,
                (Constants.BusinessConfigAttributes.CUMM_PAED_FAERS)        : this.cumPediatricCountFaers,
                (Constants.BusinessConfigAttributes.NEW_GERIA_FAERS)        : this.newGeriatricCountFaers,
                (Constants.BusinessConfigAttributes.CUMM_GERIA_FAERS)        : this.cumGeriatricCountFaers,
                (Constants.BusinessConfigAttributes.CHI_SQUARE_FAERS)       : this.chiSquareFaers,
                (Constants.BusinessConfigAttributes.NEW_NON_SERIOUS_FAERS)  : this.newNonSeriousFaers,
                (Constants.BusinessConfigAttributes.CUM_NON_SERIOUS_FAERS)  : this.cumNonSeriousFaers,
                (Constants.BusinessConfigAttributes.NEW_INTER_FAERS)        : this.newInteractingCountFaers,
                (Constants.BusinessConfigAttributes.CUMM_INTER_FAERS)       : this.cumInteractingCountFaers,
                (Constants.BusinessConfigAttributes.NEW_SPON_COUNT_FAERS)   : this.newSponCountFaers,
                (Constants.BusinessConfigAttributes.CUM_SPON_COUNT_FAERS)   : this.cumSponCountFaers,
                (Constants.BusinessConfigAttributes.NEW_STUDY_COUNT_FAERS)  : this.newStudyCountFaers,
                (Constants.BusinessConfigAttributes.CUM_STUDY_COUNT_FAERS)  : this.cumStudyCountFaers,
                (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_FAERS): this.newSeriousCountFaers,
                (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_FAERS): this.cumSeriousCountFaers,
                (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_FAERS)  : this.newFatalCountFaers,
                (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_FAERS)  : this.cumFatalCountFaers,
                (Constants.BusinessConfigAttributes.PRR_SCORE_FAERS)        : this.prrValueFaers,
                (Constants.BusinessConfigAttributes.PRRLCI_SCORE_FAERS)     : this.prrLCIFaers,
                (Constants.BusinessConfigAttributes.PRRUCI_SCORE_FAERS)     : this.prrUCIFaers,
                (Constants.BusinessConfigAttributes.ROR_SCORE_FAERS)        : this.rorValueFaers,
                (Constants.BusinessConfigAttributes.RORLCI_SCORE_FAERS)     : this.rorLCIFaers,
                (Constants.BusinessConfigAttributes.RORUCI_SCORE_FAERS)     : this.rorUCIFaers,
                (Constants.BusinessConfigAttributes.EBGM_SCORE_FAERS)       : this.ebgmFaers,
                (Constants.BusinessConfigAttributes.EB05_SCORE_FAERS)       : this.eb05Faers,
                (Constants.BusinessConfigAttributes.EB95_SCORE_FAERS)       : this.eb95Faers,
                (Constants.BusinessConfigAttributes.RR_VALUE_FAERS)         : this.rrValueFaers,
                (Constants.BusinessConfigAttributes.E_VALUE_FAERS)          : this.eValueFaers,

                //VIGIBASE BR
                (Constants.BusinessConfigAttributes.NEW_COUNT_VIGIBASE)          : this.newCountVigibase,
                (Constants.BusinessConfigAttributes.CUMM_COUNT_VIGIBASE)         : this.cumCountVigibase,
                (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VIGIBASE)    : this.newFatalCountVigibase,
                (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VIGIBASE)    : this.cumFatalCountVigibase,
                (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VIGIBASE)  : this.newSeriousCountVigibase,
                (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VIGIBASE)  : this.cumSeriousCountVigibase,
                (Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VIGIBASE): this.newGeriatricCountVigibase,
                (Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VIGIBASE): this.cumGeriatricCountVigibase,
                (Constants.BusinessConfigAttributes.NEW_PAED_VIGIBASE)           : this.newPediatricCountVigibase,
                (Constants.BusinessConfigAttributes.CUM_PAED_VIGIBASE)           : this.cumPediatricCountVigibase,
                (Constants.BusinessConfigAttributes.PRR_SCORE_VIGIBASE)          : this.prrValueVigibase,
                (Constants.BusinessConfigAttributes.PRRLCI_SCORE_VIGIBASE)       : this.prrLCIVigibase,
                (Constants.BusinessConfigAttributes.PRRUCI_SCORE_VIGIBASE)       : this.prrUCIVigibase,
                (Constants.BusinessConfigAttributes.ROR_SCORE_VIGIBASE)          : this.rorValueVigibase,
                (Constants.BusinessConfigAttributes.RORLCI_SCORE_VIGIBASE)       : this.rorLCIVigibase,
                (Constants.BusinessConfigAttributes.RORUCI_SCORE_VIGIBASE)       : this.rorUCIVigibase,
                (Constants.BusinessConfigAttributes.EBGM_SCORE_VIGIBASE)         : this.ebgmVigibase,
                (Constants.BusinessConfigAttributes.EB05_SCORE_VIGIBASE)         : this.eb05Vigibase,
                (Constants.BusinessConfigAttributes.EB95_SCORE_VIGIBASE)         : this.eb95Vigibase,
                (Constants.BusinessConfigAttributes.CHI_SQUARE_VIGIBASE)         : this.chiSquareVigibase,
                (Constants.BusinessConfigAttributes.RR_VALUE_VIGIBASE)           : this.rrValueVigibase,
                (Constants.BusinessConfigAttributes.E_VALUE_VIGIBASE)            : this.eValueVigibase,
                //JADER BR
                (Constants.BusinessConfigAttributes.NEW_COUNT_JADER)          : this.newCountJader,
                (Constants.BusinessConfigAttributes.CUMM_COUNT_JADER)         : this.cumCountJader,
                (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_JADER)    : this.newFatalCountJader,
                (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_JADER)    : this.cumFatalCountJader,
                (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_JADER)  : this.newSeriousCountJader,
                (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_JADER)  : this.cumSeriousCountJader,
                (Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_JADER): this.newGeriatricCountJader,
                (Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_JADER): this.cumGeriatricCountJader,
                (Constants.BusinessConfigAttributes.NEW_PAED_JADER)           : this.newPediatricCountJader,
                (Constants.BusinessConfigAttributes.CUM_PAED_JADER)           : this.cumPediatricCountJader,
                (Constants.BusinessConfigAttributes.PRR_SCORE_JADER)          : this.prrValueJader,
                (Constants.BusinessConfigAttributes.PRRLCI_SCORE_JADER)       : this.prrLCIJader,
                (Constants.BusinessConfigAttributes.PRRUCI_SCORE_JADER)       : this.prrUCIJader,
                (Constants.BusinessConfigAttributes.ROR_SCORE_JADER)          : this.rorValueJader,
                (Constants.BusinessConfigAttributes.RORLCI_SCORE_JADER)       : this.rorLCIJader,
                (Constants.BusinessConfigAttributes.RORUCI_SCORE_JADER)       : this.rorUCIJader,
                (Constants.BusinessConfigAttributes.EBGM_SCORE_JADER)         : this.ebgmJader,
                (Constants.BusinessConfigAttributes.EB05_SCORE_JADER)         : this.eb05Jader,
                (Constants.BusinessConfigAttributes.EB95_SCORE_JADER)         : this.eb95Jader,
                (Constants.BusinessConfigAttributes.CHI_SQUARE_JADER)         : this.chiSquareJader,
                (Constants.BusinessConfigAttributes.RR_VALUE_JADER)           : this.rrValueJader,
                (Constants.BusinessConfigAttributes.E_VALUE_JADER)            : this.eValueJader

        ]
        if (stratificationScoreDTO) {
            ruleMap << stratificationScoreDTO.mapData()
        }

        if (newCountScoreDTO) {
            ruleMap << newCountScoreDTO.mapData()
        }
        ruleMap
    }

    Map getPreviousDataMap() {
        Map prevAlertDataMap = [:]
        if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && !isEvdasIntegratedReview) {
            prevAlertDataMap = prevAggData
        } else {
            prevAlertDataMap = prevEvdasData
        }
        prevAlertDataMap
    }


}
