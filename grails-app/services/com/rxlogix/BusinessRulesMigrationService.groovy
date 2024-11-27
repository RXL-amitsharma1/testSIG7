package com.rxlogix

import com.rxlogix.config.RuleMigrationStatus
import com.rxlogix.signal.RuleInformation
import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class BusinessRulesMigrationService {
    def alertService
    def cacheService
    //Added maps in service itself as no need to add these in configurations
    def replacementMapPVA = [
            "NEW_COUNT"                    : "newCount",
            "CUMM_COUNT"                   : "cumCount",
            "NEW_SPON"                     : "newSponCount",
            "CUMM_SPON"                    : "cumSponCount",
            "NEW_SER"                      : "newSeriousCount",
            "CUMM_SER"                     : "cumSeriousCount",
            "NEW_FATAL"                    : "newFatalCount",
            "CUMM_FATAL"                   : "cumFatalCount",
            "NEW_STUDY"                    : "newStudyCount",
            "CUMM_STUDY"                   : "cumStudyCount",
            "cummInteractingCount"         : "cumInteractingCount",
            "cummPediatricCount"           : "cumPediatricCount",
            "PRR"                          : "prrValue",
            "PRRLCI"                       : "prrLCI",
            "PRRUCI"                       : "prrUCI",
            "ROR"                          : "rorValue",
            "RORLCI"                       : "rorLCI",
            "RORUCI"                       : "rorUCI",
            "EBGM"                         : "ebgm",
            "EB05"                         : "eb05",
            "EB95"                         : "eb95",
            "CHI_SQUARE"                   : "chiSquare",
            "Chi-Square"                   : "chiSquare",
            "PRR-Previous"                 : "prrValue-Previous",
            "PRRLCI-Previous"              : "prrLCI-Previous",
            "PRRUCI-Previous"              : "prrUCI-Previous",
            "ROR-Previous"                 : "rorValue-Previous",
            "RORLCI-Previous"              : "rorLCI-Previous",
            "RORUCI-Previous"              : "rorUCI-Previous",
            "EBGM-Previous"                : "ebgm-Previous",
            "EB05-Previous"                : "eb05-Previous",
            "EB95-Previous"                : "eb95-Previous",
            "cummCount"                    : "cumCount",
            "cummCount-Previous"           : "cumCount-Previous",
            "cummInteractingCount-Previous": "cumInteractingCount-Previous",
            "cummPediatricCount-Previous"  : "cumPediatricCount-Previous",
            "POSITIVE_RE_CHALLENGE"        : "positiveRechallenge"
    ]
    def replacementMapFaers = [
            "PRR"                               : "prrValueFaers",
            "PRRLCI"                            : "prrLCIFaers",
            "PRRUCI"                            : "prrUCIFaers",
            "ROR"                               : "rorValueFaers",
            "RORLCI"                            : "rorLCIFaers",
            "RORUCI"                            : "rorUCIFaers",
            "EBGM"                              : "ebgmFaers",
            "EB05"                              : "eb05Faers",
            "EB95"                              : "eb95Faers",
            "ebgm"                              : "ebgmFaers",
            "eb05"                              : "eb05Faers",
            "eb95"                              : "eb95Faers",
            "CHI_SQUARE"                        : "chiSquareFaers",
            "Chi-Square"                        : "chiSquareFaers",
            //COUNTS data
            "NEW_COUNT"                         : "newCountFaers",
            "CUMM_COUNT"                        : "cumCountFaers",
            "NEW_SPON"                          : "newSponCountFaers",
            "CUMM_SPON"                         : "cumSponCountFaers",
            "NEW_SER"                           : "newSeriousCountFaers",
            "CUMM_SER"                          : "cumSeriousCountFaers",
            "NEW_FATAL"                         : "newFatalCountFaers",
            "CUMM_FATAL"                        : "cumFatalCountFaers",
            "NEW_STUDY"                         : "newStudyCountFaers",
            "CUMM_STUDY"                        : "cumStudyCountFaers",
            "newInteractingCount"               : "newInteractingCountFaers",
            "cummInteractingCount"              : "cumInteractingCountFaers",
            "newGeriatricCount"                 : "newGeriatricCountFaers",
            "cumGeriatricCount"                 : "cumGeriatricCountFaers",
            "newPediatricCount"                 : "newPediatricCountFaers",
            "cummPediatricCount"                : "cumPediatricCountFaers",
            "newNonSerious"                     : "newNonSeriousFaers",
            "cumNonSerious"                     : "cumNonSeriousFaers",
            //countValues data
            "newCount"                          : "newCountFaers",
            "cummCount"                         : "cumCountFaers",
            "newSponCount"                      : "newSponCountFaers",
            "cumSponCount"                      : "cumSponCountFaers",
            "newSeriousCount"                   : "newSeriousCountFaers",
            "cumSeriousCount"                   : "cumSeriousCountFaers",
            "newFatalCount"                     : "newFatalCountFaers",
            "cumFatalCount"                     : "cumFatalCountFaers",
            "newStudyCount"                     : "newStudyCountFaers",
            "cumStudyCount"                     : "cumStudyCountFaers",
            "newCount-Previous"                 : "newCountFaers-Previous",
            "cummCount-Previous"                : "cumCountFaers-Previous",
            "newSponCount-Previous"             : "newSponCountFaers-Previous",
            "cumSponCount-Previous"             : "cumSponCountFaers-Previous",
            "newSeriousCount-Previous"          : "newSeriousCountFaers-Previous",
            "cumSeriousCount-Previous"          : "cumSeriousCountFaers-Previous",
            "newFatalCount-Previous"            : "newFatalCountFaers-Previous",
            "cumFatalCount-Previous"            : "cumFatalCountFaers-Previous",
            "newStudyCount-Previous"            : "newStudyCountFaers-Previous",
            "cumStudyCount-Previous"            : "cumStudyCountFaers-Previous",
            "newGeriatricCount-Previous"        : "newGeriatricCountFaers-Previous",
            "cumGeriatricCount-Previous"        : "cumGeriatricCountFaers-Previous",
            "newNonSerious-Previous"            : "newNonSeriousFaers-Previous",
            "cumNonSerious-Previous"            : "cumNonSeriousFaers-Previous",
            "newInteractingCount-Previous"      : "newInteractingCountFaers-Previous",
            "cummInteractingCount-Previous"     : "cumInteractingCountFaers-Previous",
            "newPediatricCount-Previous"        : "newPediatricCountFaers-Previous",
            "cummPediatricCount-Previous"       : "cumPediatricCountFaers-Previous",
            //other e,rr,and chisquare threshold data
            "PRR-Previous"                      : "prrValueFaers-Previous",
            "PRRLCI-Previous"                   : "prrLCIFaers-Previous",
            "PRRUCI-Previous"                   : "prrUCIFaers-Previous",
            "ROR-Previous"                      : "rorValueFaers-Previous",
            "RORLCI-Previous"                   : "rorLCIFaers-Previous",
            "RORUCI-Previous"                   : "rorUCIFaers-Previous",
            "EBGM-Previous"                     : "ebgmFaers-Previous",
            "EB05-Previous"                     : "eb05Faers-Previous",
            "EB95-Previous"                     : "eb95Faers-Previous",
            "chiSquare-Previous"                : "chiSquareFaers-Previous",
            "POSITIVE_RE_CHALLENGE"             : "positiveRechallenge",
            //Format Options
            "NEW_COUNT_FAERS"            : "newCountFaers",
            "CUMM_COUNT_FAERS"           : "cumCountFaers",
            "NEW_FATAL_FAERS"            : "newFatalCountFaers",
            "CUMM_FATAL_FAERS"           : "cumFatalCountFaers",
            "NEW_SER_FAERS"              : "newSeriousCountFaers",
            "CUMM_SER_FAERS"             : "cumSeriousCountFaers",
            "NEW_GERIATRIC_FAERS"        : "newGeriatricCountFaers",
            "CUMM_GERIATRIC_FAERS"       : "cumGeriatricCountFaers",
            "NEW_PAED_FAERS"             : "newPediatricCountFaers",
            "CUM_PAED_FAERS"             : "cumPediatricCountFaers",
            "NEW_SPON_FAERS"             :"newSponCountFaers",
            "CUMM_SPON_FAERS"            :"cumSponCountFaers",
            "NEW_STUDY_FAERS"            :"newStudyCountFaers",
            "CUMM_STUDY_FAERS"           :"cumStudyCountFaers",
            "E_VALUE_FAERS"              :"eValueFaers",
            "RR_VALUE_FAERS"             :"rrValueFaers",
            "PRR_FAERS"                  : "prrValueFaers",
            "PRRLCI_FAERS"               : "prrLCIFaers",
            "PRRUCI_FAERS"               : "prrUCIFaers",
            "ROR_FAERS"                  : "rorValueFaers",
            "RORLCI_FAERS"               : "rorLCIFaers",
            "RORUCI_FAERS"               : "rorUCIFaers",
            "EBGM_FAERS"                 : "ebgmFaers",
            "EB05_FAERS"                 : "eb05Faers",
            "EB95_FAERS"                 : "eb95Faers",
            "CHI_SQUARE_FAERS"           : "chiSquareFaers",
            //6.1 data
            "cummCountFaers"                    : "cumCountFaers",
            "cummCountFaers-Previous"           : "cumCountFaers-Previous",
            "cummPediatricCountFaers"           : "cumPediatricCountFaers",
            "cummPediatricCountFaers-Previous"  : "cumPediatricCountFaers-Previous",
            "cummInteractingCountFaers"         : "cumInteractingCountFaers",
            "cummInteractingCountFaers-Previous": "cumInteractingCountFaers-Previous",
            "PRR"                               : "prrValueFaers",
            "prrValue"                          : "prrValueFaers",
            "prrLCI"                            : "prrLCIFaers",
            "prrUCI"                            : "prrUCIFaers",
            "PRR-Previous"                      : "prrValueFaers-Previous",
            "prrLCI-Previous"                   : "prrLCIFaers-Previous",
            "prrUCI-Previous"                   : "prrUCIFaers-Previous",
            "ROR-Previous"                      : "rorValueFaers-Previous",
            "rorLCI-Previous"                   : "rorLCIFaers-Previous",
            "rorUCI-Previous"                   : "rorUCIFaers-Previous",
            "ROR"                               : "rorValueFaers",
            "rorValue"                          : "rorValueFaers",
            "rorLCI"                            : "rorLCIFaers",
            "rorUCI"                            : "rorUCIFaers"

    ]

    def replacementMapVaers = [
            "PRR_VAERS"                         : "prrValueVaers",
            "PRRLCI_VAERS"                      : "prrLCIVaers",
            "PRRUCI_VAERS"                      : "prrUCIVaers",
            "ROR_VAERS"                         : "rorValueVaers",
            "RORLCI_VAERS"                      : "rorLCIVaers",
            "RORUCI_VAERS"                      : "rorUCIVaers",
            "EBGM_VAERS"                        : "ebgmVaers",
            "EB05_VAERS"                        : "eb05Vaers",
            "EB95_VAERS"                        : "eb95Vaers",
            "CHI_SQUARE_VAERS"                  : "chiSquareVaers",
            "Chi-Square"                        : "chiSquareVaers",//6.1 format

            //COUNTS data
            "NEW_COUNT_VAERS"                   : "newCountVaers",
            "CUMM_COUNT_VAERS"                  : "cumCountVaers",
            "NEW_FATAL_VAERS"                   : "newFatalCountVaers",
            "CUMM_FATAL_VAERS"                  : "cumFatalCountVaers",
            "NEW_SER_VAERS"                     : "newSeriousCountVaers",
            "CUMM_SER_VAERS"                    : "cumSeriousCountVaers",
            "NEW_GERIATRIC_VAERS"               : "newGeriatricCountVaers",
            "CUMM_GERIATRIC_VAERS"              : "cumGeriatricCountVaers",
            "NEW_PAED_VAERS"                    : "newPediatricCountVaers",
            "CUM_PAED_VAERS"                    : "cumPediatricCountVaers",

            //countValues data
            "newCount"                          : "newCountVaers",
            "cummCount"                         : "cumCountVaers",
            "newSeriousCount"                   : "newSeriousCountVaers",
            "cumSeriousCount"                   : "cumSeriousCountVaers",
            "newFatalCount"                     : "newFatalCountVaers",
            "cumFatalCount"                     : "cumFatalCountVaers",
            "newGeriatricCount"                 : "newGeriatricCountVaers",
            "cumGeriatricCount"                 : "cumGeriatricCountVaers",
            "newPediatricCount"                 : "newPediatricCountVaers",
            "cummPediatricCount"                : "cumPediatricCountVaers",
            "newCount-Previous"                 : "newCountVaers-Previous",
            "cummCount-Previous"                : "cumCountVaers-Previous",
            "newSeriousCount-Previous"          : "newSeriousCountVaers-Previous",
            "cumSeriousCount-Previous"          : "cumSeriousCountVaers-Previous",
            "newFatalCount-Previous"            : "newFatalCountVaers-Previous",
            "cumFatalCount-Previous"            : "cumFatalCountVaers-Previous",
            "newGeriatricCount-Previous"        : "newGeriatricCountVaers-Previous",
            "cumGeriatricCount-Previous"        : "cumGeriatricCountVaers-Previous",
            "newPediatricCount-Previous"        : "newPediatricCountVaers-Previous",
            "cummPediatricCount-Previous"       : "cumPediatricCountVaers-Previous",

            //other prev data
            "PRR"                               : "prrValueVaers",
            "PRRLCI"                            : "prrLCIVaers",
            "PRRUCI"                            : "prrUCIVaers",
            "ROR"                               : "rorValueVaers",
            "RORLCI"                            : "rorLCIVaers",
            "RORUCI"                            : "rorUCIVaers",
            "EBGM"                              : "ebgmVaers",
            "EB05"                              : "eb05Vaers",
            "EB95"                              : "eb95Vaers",
            "ebgm"                              : "ebgmVaers",
            "eb05"                              : "eb05Vaers",
            "eb95"                              : "eb95Vaers",
            "chiSquare"                         : "chiSquareVaers",
            "PRR-Previous"                      : "prrValueVaers-Previous",
            "PRRLCI-Previous"                   : "prrLCIVaers-Previous",
            "PRRUCI-Previous"                   : "prrUCIVaers-Previous",
            "ROR-Previous"                      : "rorValueVaers-Previous",
            "RORLCI-Previous"                   : "rorLCIVaers-Previous",
            "RORUCI-Previous"                   : "rorUCIVaers-Previous",
            "EBGM-Previous"                     : "ebgmVaers-Previous",
            "EB05-Previous"                     : "eb05Vaers-Previous",
            "EB95-Previous"                     : "eb95Vaers-Previous",
            "chiSquare-Previous"                : "chiSquareVaers-Previous",
            //6.1 format data
            "prrUciVaers"                       : "prrUCIVaers",
            "cummCountVaers"                    : "cumCountVaers",
            "cummCountVaers-Previous"           : "cumCountVaers-Previous",
            "cummPediatricCountVaers"           : "cumPediatricCountVaers",
            "cummPediatricCountVaers-Previous"  : "cumPediatricCountVaers-Previous",
            "cummInteractingCountVaers"         : "cumInteractingCountVaers",
            "cummInteractingCountVaers-Previous": "cumInteractingCountVaers-Previous",

            "PRR"                               : "prrValueVaers",
            "prrValue"                          : "prrValueVaers",
            "prrLCI"                            : "prrLCIVaers",
            "prrUCI"                            : "prrUCIVaers",
            "PRR-Previous"                      : "prrValueVaers-Previous",
            "prrLCI-Previous"                   : "prrLCIVaers-Previous",
            "prrUCI-Previous"                   : "prrUCIVaers-Previous",
            "ROR-Previous"                      : "rorValueVaers-Previous",
            "rorLCI-Previous"                   : "rorLCIVaers-Previous",
            "rorUCI-Previous"                   : "rorUCIVaers-Previous",
            "ROR"                               : "rorValueVaers",
            "rorValue"                          : "rorValueVaers",
            "rorLCI"                            : "rorLCIVaers",
            "rorUCI"                            : "rorUCIVaers",
    ]


    def replacementMapEvdas = [

            "ROR_EUROPE_EVDAS"                : "rorEuropeEvdas",
            "ROR_N_AMERICA_EVDAS"             : "rorNAmericaEvdas",
            "ROR_JAPAN_EVDAS"                 : "rorJapanEvdas",
            "ROR_ASIA_EVDAS"                  : "rorAsiaEvdas",
            "ROR_REST_EVDAS"                  : "rorRestEvdas",
            "ROR_ALL_EVDAS"                   : "rorAllEvdas",
            "SDR_EVDAS"                       : "sdrEvdas",
            "DME"                             : "DME",
            "IME"                             : "IME",
            "SPECIAL_MONITORING"              : "SPECIAL_MONITORING",
            "STOP_LIST"                       : "STOP_LIST",
            "CHANGES_EVDAS"                   : "changesEvdas",
            "EVDAS_IME_DME"                   : "dmeImeEvdas",
            "RELT_V_ROR_PAED_VS_OTHR"         : "ratioRorPaedVsOthersEvdas",
            "EVDAS_SDR_PAED"                  : "sdrPaedEvdas",
            "RELT_V_ROR_GERTR_VS_OTHR"        : "ratioRorGeriatrVsOthersEvdas",
            "EVDAS_SDR_GERTR"                 : "sdrGeratrEvdas",
            "EVDAS_LISTEDNESS"                : "EVDAS_LISTEDNESS",
            "NEW_EVENT"                       : "NEW_EVENT",
            "PREVIOUS_CATEGORY"               : "PREVIOUS_CATEGORY",
            "ALL_CATEGORY"                    : "ALL_CATEGORY",
            "rorValue"                        : "rorAllEvdas",
            "europeRor"                       : "rorEuropeEvdas",
            "northAmericaRor"                 : "rorNAmericaEvdas",
            "japanRor"                        : "rorJapanEvdas",
            "asiaRor"                         : "rorAsiaEvdas",
            "restRor"                         : "rorRestEvdas",
            "allRor"                          : "rorAllEvdas",
            "ratioRorPaedVsOthers"            : "ratioRorPaedVsOthersEvdas",
            "ratioRorGeriatrVsOthers"         : "ratioRorGeriatrVsOthersEvdas",
            "rorValue-Previous"               : "rorAllEvdas-Previous",
            "europeRor-Previous"              : "rorEuropeEvdas-Previous",
            "northAmericaRor-Previous"        : "rorNAmericaEvdas-Previous",
            "japanRor-Previous"               : "rorJapanEvdas-Previous",
            "asiaRor-Previous"                : "rorAsiaEvdas-Previous",
            "restRor-Previous"                : "rorRestEvdas-Previous",
            "allRor-Previous"                 : "rorAllEvdas-Previous",
            "ratioRorPaedVsOthers-Previous"   : "ratioRorPaedVsOthersEvdas-Previous",
            "ratioRorGeriatrVsOthers-Previous": "ratioRorGeriatrVsOthersEvdas-Previous",
            "NEW_EV_EVDAS"                    : "newEvEvdas",
            "TOTAL_EV_EVDAS"                  : "totalEvEvdas",
            "NEW_EEA_EVDAS"                   : "newEEAEvdas",
            "TOTAL_EEA_EVDAS"                 : "totEEAEvdas",
            "NEW_HCP_EVDAS"                   : "newHCPEvdas",
            "TOTAL_HCP_EVDAS"                 : "totHCPEvdas",
            "NEW_SERIOUS_EVDAS"               : "newSeriousEvdas",
            "TOTAL_SERIOUS_EVDAS"             : "totalSeriousEvdas",
            "NEW_OBS_EVDAS"                   : "newObsEvdas",
            "TOTAL_OBS_EVDAS"                 : "totObsEvdas",
            "NEW_FATAL_EVDAS"                 : "newFatalEvdas",
            "TOTAL_FATAL_EVDAS"               : "totalFatalEvdas",
            "NEW_MED_ERR_EVDAS"               : "newMedErrEvdas",
            "TOTAL_MED_ERR_EVDAS"             : "totMedErrEvdas",
            "NEW_PLUS_RC_EVDAS"               : "newRcEvdas",
            "TOTAL_PLUS_RC_EVDAS"             : "totRcEvdas",
            "NEW_LITERATURE_EVDAS"            : "newLitEvdas",
            "TOTAL_LITERATURE_EVDAS"          : "totalLitEvdas",
            "NEW_PAED_EVDAS"                  : "newPaedEvdas",
            "TOTAL_PAED_EVDAS"                : "totalPaedEvdas",
            "NEW_GERIAT_EVDAS"                : "newGeriatEvdas",
            "TOTAL_GERIAT_EVDAS"              : "totalGeriatEvdas",
            "NEW_SPON_EVDAS"                  : "newSponEvdas",
            "TOTAL_SPON_EVDAS"                : "totSpontEvdas",
            "TOTAL_SPON_EUROPE"               : "totSpontEuropeEvdas",
            "TOTAL_SPON_N_AMERICA"            : "totSpontNAmericaEvdas",
            "TOTAL_SPON_JAPAN"                : "totSpontJapanEvdas",
            "TOTAL_SPON_ASIA"                 : "totSpontAsiaEvdas",
            "TOTAL_SPON_REST"                 : "totSpontRestEvdas",
            "newEv"                           : "newEvEvdas",
            "totalEv"                         : "totalEvEvdas",
            "newEea"                          : "newEEAEvdas",
            "totEea"                          : "totEEAEvdas",
            "newHcp"                          : "newHCPEvdas",
            "totHcp"                          : "totHCPEvdas",
            "newSerious"                      : "newSeriousEvdas",
            "totalSerious"                    : "totalSeriousEvdas",
            "newObs"                          : "newObsEvdas",
            "totObs"                          : "totObsEvdas",
            "newFatal"                        : "newFatalEvdas",
            "totalFatal"                      : "totalFatalEvdas",
            "newMedErr"                       : "newMedErrEvdas",
            "totMedErr"                       : "totMedErrEvdas",
            "newRc"                           : "newRcEvdas",
            "totRc"                           : "totRcEvdas",
            "newLit"                          : "newLitEvdas",
            "totalLit"                        : "totalLitEvdas",
            "newPaed"                         : "newPaedEvdas",
            "totPaed"                         : "totalPaedEvdas",
            "newGeria"                        : "newGeriatEvdas",
            "totGeria"                        : "totalGeriatEvdas",
            "newSpont"                        : "newSponEvdas",
            "totSpont"                        : "totSpontEvdas",
            "totSpontEurope"                  : "totSpontEuropeEvdas",
            "totSpontNAmerica"                : "totSpontNAmericaEvdas",
            "totSpontJapan"                   : "totSpontJapanEvdas",
            "totSpontAsia"                    : "totSpontAsiaEvdas",
            "totSpontRest"                    : "totSpontRestEvdas",
            "newEv-Previous"                  : "newEvEvdas-Previous",
            "totalEv-Previous"                : "totalEvEvdas-Previous",
            "newEea-Previous"                 : "newEEAEvdas-Previous",
            "totEea-Previous"                 : "totEEAEvdas-Previous",
            "newHcp-Previous"                 : "newHCPEvdas-Previous",
            "totHcp-Previous"                 : "totHCPEvdas-Previous",
            "newSerious-Previous"             : "newSeriousEvdas-Previous",
            "totalSerious-Previous"           : "totalSeriousEvdas-Previous",
            "newObs-Previous"                 : "newObsEvdas-Previous",
            "totObs-Previous"                 : "totObsEvdas-Previous",
            "newFatal-Previous"               : "newFatalEvdas-Previous",
            "totalFatal-Previous"             : "totalFatalEvdas-Previous",
            "newMedErr-Previous"              : "newMedErrEvdas-Previous",
            "totMedErr-Previous"              : "totMedErrEvdas-Previous",
            "newRc-Previous"                  : "newRcEvdas-Previous",
            "totRc-Previous"                  : "totRcEvdas-Previous",
            "newLit-Previous"                 : "newLitEvdas-Previous",
            "totalLit-Previous"               : "totalLitEvdas-Previous",
            "newPaed-Previous"                : "newPaedEvdas-Previous",
            "totPaed-Previous"                : "totalPaedEvdas-Previous",
            "newGeria-Previous"               : "newGeriatEvdas-Previous",
            "totGeria-Previous"               : "totalGeriatEvdas-Previous",
            "newSpont-Previous"               : "newSponEvdas-Previous",
            "totSpont-Previous"               : "totSpontEvdas-Previous",
            "totSpontEurope-Previous"         : "totSpontEuropeEvdas-Previous",
            "totSpontNAmerica-Previous"       : "totSpontNAmericaEvdas-Previous",
            "totSpontJapan-Previous"          : "totSpontJapanEvdas-Previous",
            "totSpontAsia-Previous"           : "totSpontAsiaEvdas-Previous",
            "totSpontRest-Previous"           : "totSpontRestEvdas-Previous"]

    def replacementMapVigibase = [
            "PRR_VIGIBASE"                         : "prrValueVigibase",
            "PRRLCI_VIGIBASE"                      : "prrLCIVigibase",
            "PRRUCI_VIGIBASE"                      : "prrUCIVigibase",
            "ROR_VIGIBASE"                         : "rorValueVigibase",
            "RORLCI_VIGIBASE"                      : "rorLCIVigibase",
            "RORUCI_VIGIBASE"                      : "rorUCIVigibase",
            "EBGM_VIGIBASE"                        : "ebgmVigibase",
            "EB05_VIGIBASE"                        : "eb05Vigibase",
            "EB95_VIGIBASE"                        : "eb95Vigibase",
            "CHI_SQUARE_VIGIBASE"                  : "chiSquareVigibase",
            "Chi-Square"                           : "chiSquareVigibase",//6.1 format
            "newCount"                             : "newCountVigibase",
            "cummCount"                            : "cumCountVigibase",
            "newSeriousCount"                      : "newSeriousCountVigibase",
            "cumSeriousCount"                      : "cumSeriousCountVigibase",
            "newFatalCount"                        : "newFatalCountVigibase",
            "cumFatalCount"                        : "cumFatalCountVigibase",
            "newGeriatricCount"                    : "newGeriatricCountVigibase",
            "cumGeriatricCount"                    : "cumGeriatricCountVigibase",
            "newPediatricCount"                    : "newPediatricCountVigibase",
            "cummPediatricCount"                   : "cumPediatricCountVigibase",
            "newCount-Previous"                    : "newCountVigibase-Previous",
            "cummCount-Previous"                   : "cumCountVigibase-Previous",
            "newSeriousCount-Previous"             : "newSeriousCountVigibase-Previous",
            "cumSeriousCount-Previous"             : "cumSeriousCountVigibase-Previous",
            "newFatalCount-Previous"               : "newFatalCountVigibase-Previous",
            "cumFatalCount-Previous"               : "cumFatalCountVigibase-Previous",
            "newGeriatricCount-Previous"           : "newGeriatricCountVigibase-Previous",
            "cumGeriatricCount-Previous"           : "cumGeriatricCountVigibase-Previous",
            "newPediatricCount-Previous"           : "newPediatricCountVigibase-Previous",
            "cummPediatricCount-Previous"          : "cumPediatricCountVigibase-Previous",
            "NEW_COUNT_VIGIBASE"                   : "newCountVigibase",
            "CUMM_COUNT_VIGIBASE"                  : "cumCountVigibase",
            "NEW_FATAL_VIGIBASE"                   : "newFatalCountVigibase",
            "CUMM_FATAL_VIGIBASE"                  : "cumFatalCountVigibase",
            "NEW_SER_VIGIBASE"                     : "newSeriousCountVigibase",
            "CUMM_SER_VIGIBASE"                    : "cumSeriousCountVigibase",
            "NEW_GERIATRIC_VIGIBASE"               : "newGeriatricCountVigibase",
            "CUMM_GERIATRIC_VIGIBASE"              : "cumGeriatricCountVigibase",
            "NEW_PAED_VIGIBASE"                    : "newPediatricCountVigibase",
            "CUM_PAED_VIGIBASE"                    : "cumPediatricCountVigibase",
            "ROR"                                  : "rorValueVigibase",
            "rorValue"                             : "rorValueVigibase",
            "RORLCI"                               : "rorLCIVigibase",
            "RORUCI"                               : "rorUCIVigibase",
            "ROR-Previous"                         : "rorValueVigibase-Previous",
            "RORLCI-Previous"                      : "rorLCIVigibase-Previous",
            "RORUCI-Previous"                      : "rorUCIVigibase-Previous",
            "PRR"                                  : "prrValueVigibase",
            "PRRLCI"                               : "prrLCIVigibase",
            "PRRUCI"                               : "prrUCIVigibase",
            "PRR-Previous"                         : "prrValueVigibase-Previous",
            "PRRLCI-Previous"                      : "prrLCIVigibase-Previous",
            "PRRUCI-Previous"                      : "prrUCIVigibase-Previous",
            "EBGM"                                 : "ebgmVigibase",
            "EB05"                                 : "eb05Vigibase",
            "EB95"                                 : "eb95Vigibase",
            "ebgm"                                 : "ebgmVigibase",
            "eb05"                                 : "eb05Vigibase",
            "eb95"                                 : "eb95Vigibase",
            "EBGM-Previous"                        : "ebgmVigibase-Previous",
            "EB05-Previous"                        : "eb05Vigibase-Previous",
            "EB95-Previous"                        : "eb95Vigibase-Previous",
            "chiSquare"                            : "chiSquareVigibase",
            "chiSquare-Previous"                   : "chiSquareVigibase-Previous",
            "cummCountVigibase"                    : "cumCountVigibase",
            "cummCountVigibase-Previous"           : "cumCountVigibase-Previous",
            "cummPediatricCountVigibase"           : "cumPediatricCountVigibase",
            "cummPediatricCountVigibase-Previous"  : "cumPediatricCountVigibase-Previous",
            "cummInteractingCountVigibase"         : "cumInteractingCountVigibase",
            "cummInteractingCountVigibase-Previous": "cumInteractingCountVigibase-Previous",
            "PRR"                                  : "prrValueVigibase",
            "prrValue"                             : "prrValueVigibase",
            "prrLCI"                               : "prrLCIVigibase",
            "prrUCI"                               : "prrUCIVigibase",
            "PRR-Previous"                         : "prrValueVigibase-Previous",
            "prrLCI-Previous"                      : "prrLCIVigibase-Previous",
            "prrUCI-Previous"                      : "prrUCIVigibase-Previous",
            "ROR-Previous"                         : "rorValueVigibase-Previous",
            "rorLCI-Previous"                      : "rorLCIVigibase-Previous",
            "rorUCI-Previous"                      : "rorUCIVigibase-Previous",
            "ROR"                                  : "rorValueVigibase",
            "rorLCI"                               : "rorLCIVigibase",
            "rorUCI"                               : "rorUCIVigibase"
    ]

    Boolean updateOldRuleJson() {
        log.info("Started Processing Business Rules for ruleJSON update ")
        List<RuleMigrationStatus> ruleMigrationStatusList = RuleMigrationStatus.findAllByIsMigrationCompleted(false)
        Boolean exceptionCaught = false
        //List<RuleInformation> aggRules = RuleInformation.findAllByIsSingleCaseAlertType(false)
        ruleMigrationStatusList?.each {
            try {
                //TODO : getClass().classLoader.loadClass(it.entityClass) - use entity class for domain for general code
                RuleInformation ruleInformation = RuleInformation.findById(it.entityId)
                if (ruleInformation) {
                    log.info("Processing BR with Name ${ruleInformation.getInstanceIdentifierForAuditLog()} Rule information id: ${ruleInformation.id}")
                    switch (ruleInformation.businessConfiguration.dataSource) {
                        case Constants.DataSource.PVA:
                            ruleInformation.ruleJSON = replaceKeysWithUpdatedValue(ruleInformation.ruleJSON, replacementMapPVA)
                            ruleInformation.format = replaceKeysWithUpdatedValue(ruleInformation.format, replacementMapPVA)
                            break;
                        case Constants.DataSource.EUDRA:
                            ruleInformation.ruleJSON = replaceKeysWithUpdatedValue(ruleInformation.ruleJSON, replacementMapEvdas)
                            ruleInformation.format = replaceKeysWithUpdatedValue(ruleInformation.format, replacementMapEvdas)
                            break;
                        case Constants.DataSource.VAERS:
                            ruleInformation.ruleJSON = replaceKeysWithUpdatedValue(ruleInformation.ruleJSON, replacementMapVaers)
                            ruleInformation.format = replaceKeysWithUpdatedValue(ruleInformation.format, replacementMapVaers)
                            break;
                        case Constants.DataSource.FAERS:
                            ruleInformation.ruleJSON = replaceKeysWithUpdatedValue(ruleInformation.ruleJSON, replacementMapFaers)
                            ruleInformation.format = replaceKeysWithUpdatedValue(ruleInformation.format, replacementMapFaers)
                            break;
                        case Constants.DataSource.VIGIBASE:
                            ruleInformation.ruleJSON = replaceKeysWithUpdatedValue(ruleInformation.ruleJSON, replacementMapVigibase)
                            ruleInformation.format = replaceKeysWithUpdatedValue(ruleInformation.format, replacementMapVigibase)
                            break;
                        default:
                            break;
                    }
                    ruleInformation.save(failOnError: true, flush: true)
                    Map finalDataMap = ["rules": ruleInformation.ruleJSON, "format": ruleInformation.format]
                    String finalDataString = finalDataMap as JSON
                    it.migratedData = finalDataString
                    it.isMigrationCompleted = true
                    it.error = null
                } else {
                    log.info("Rule Information not found for id: ${it.entityId}")
                    it.isMigrationCompleted = true
                    it.error = Constants.MigrationStatus.NO_BR_EXIST
                }
            } catch (Exception ex) {
                String errorMessage = ex.getMessage()
                it.error = errorMessage.length() > 32000 ? errorMessage.substring(0,32000) : errorMessage
                exceptionCaught = true
            }
            it.save(failOnError: true, flush: true)
        }
        log.info("Completed Processing Business Rules for ruleJSON update")
        cacheService.prepareRuleInformationCache()
        return exceptionCaught
    }
    void insertDataForMigration() {
        log.info("Insert Migration Data in Rule Migration Status Started")

        List<RuleInformation> aggRules = RuleInformation.findAllByIsSingleCaseAlertType(false)
        List<RuleMigrationStatus> ruleMigrationStatusList = []
        aggRules.each {
            RuleMigrationStatus ruleMigrationStatus = new RuleMigrationStatus()
            ruleMigrationStatus.entityId = it.id
            ruleMigrationStatus.entityClass = "RuleInformation"
            ruleMigrationStatus.datasource = it.businessConfiguration.dataSource
            ruleMigrationStatus.isMigrationCompleted = false
            Map initialDataMap = ["rules": it.ruleJSON, "format" : it.format]
            String initialDataString  = initialDataMap as JSON
            ruleMigrationStatus.initialData = initialDataString
            ruleMigrationStatusList.add(ruleMigrationStatus)
        }
        alertService.batchPersistForDomain(ruleMigrationStatusList, RuleMigrationStatus.class.name)
        log.info("Insert Migration Data in Rule Migration Status completed")
    }
    Boolean checkMigrationRequired(){
        List<RuleMigrationStatus> errorMigrations = RuleMigrationStatus.findAllByIsMigrationCompleted(false)
        return errorMigrations.size() > 0
    }

    def replaceKeysWithUpdatedValue(String inputString, Map<String, String> map) {
        log.info("Existing data: ${inputString}")
        try {
            // Create a regex pattern by joining all the keys with a pipe (|) for alternation
            def pattern = map.keySet().collect { key -> "\\b${key}\\b" }.join('|')
            // Use replaceAll with a closure to replace each match with its corresponding value
            def outputString = inputString?.replaceAll(pattern) { match ->
                map.containsKey(match) ? map[match] : match
            }
            log.info("Transformed JSON : ${outputString}")
            return outputString
        } catch (Exception exception) {
            log.info("An error  occurred while replacing keys with values for business configuration: ${exception.message}")
        }
    }

}

