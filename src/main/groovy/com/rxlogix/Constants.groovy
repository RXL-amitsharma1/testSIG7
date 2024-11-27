package com.rxlogix
import grails.util.Holders

interface Constants {

    String POI_INPUT_PATTERN_REGEX = /&\S+[.]/
    String CASE_ABREVATIVE_NARRATIVE = "Case Abbreviated Narrative"
    String ERROR_URL = "error_url"
    String USER_GROUP_TOKEN = "UserGroup_"
    String USER_TOKEN = "User_"
    String MINE = "Mine_"
    String ASSIGN_TO_ME = "AssignToMe_"
    String SHARED_WITH_ME = "SharedWithMe_"
    String ALL_SIGNALS = "AllSignals_"
    String ASSIGN_TO_PARAM = "assignedToValue"
    String OWNER_SELECT_VALUE = "owner"
    String SHARED_WITH_ME_SELECT_VALUE = "sharedWithMe"
    String CASE_SERIES_OWNER = "PVS"
    String NULL_STRING = 'null'
    String UTC = "UTC"
    String GMT = "GMT"
    String SYSTEM_USER = "System"
    String REFERERNCE_LINK = "Reference Link"
    String PVS_CASE_SERIES_OWNER = "PVS"
    String NOTIFICATION_QUEUE = "/topic/"
    String CASE_FORM_FILE_NAME_REGEX = /[+().,='"!@;:#$%&*^<?|>\\/]/
    String SPOTFIRE_FILE_NAME_REGEX =/[+().,='"!@;:#$%&*^<?|>\\/]/
    String MEDICATION_ERROR_PT_REGEX = /\./
    String SIGNAL_ANALYSIS_PRIORITIZATION="Signal Analysis & Prioritization";
    String SAFETY_OBSERVATION_VALIDATION="Safety Observation Validation";
    String SIGNAL_ASSESSMENT="Signal Assessment";
    String TO_REPLACE_STRING = "ThisStringReservedToReplaceOtherStrings"
    String DATE_CLOSED= 'Date Closed'
    String VALIDATION_DATE= 'Validation Date'
    String ONGOING_SIGNAL='Ongoing'
    String VERSION_NUMBER = 'Version Number#'
    String FOLLOW_UP_NUMBER = 'Follow-up Number#'
    String INITIAL = 'Initial'
    String ENABLE_ALERT_EXECUTION = 'EnableAlertExecutionPreChecks'
    String ENABLE_SIGNAL_CHARTS = 'EnableSignalCharts'
    String PRODUCT_GROUP_UPDATE = 'UpdateDomainsAfterProductGroupUpdate'
    String DESCENDING_ORDER = "desc"
    String OTHER_STRING="Others"
    String XLS_FORMAT = "xls"
    String PII_OWNER = "PVD_SECURITY_FIELDS"
    String PII_ENCRYPTION_KEY = "PVS"
    String API_CALL_FAILED_MESSAGE = "API call failed, Exception came in fetching data from pubmed."
    String FIELD_REFRESH_TOPIC_NAME = "FieldManagementRefreshNotification"
    String EMBASE_API_CALL_FAILED_MESSAGE = "API call failed, Exception came in fetching data from embase."
    String JADER_DB = "JADER-DB"
    String FAERS_DB = "FAERS-DB"
    String VAERS_DB = "VAERS-DB"
    String VIGIBASE_DB = "VIGIBASE-DB"
    String DRUG_RECORD_NUMBER_LEVEL = "100"
    Integer DRUG_RECORD_NUMBER_LEVEL_IDX = 100
    String DRUG_RECORD_NUMBER = "Drug Record Number"
    Integer IMPORT_AGG_MESSAGE_INDEX = 18
    Integer IMPORT_ICR_MESSAGE_INDEX = 17
    Integer IMPORT_AGG_UNSCHEDULE_INDEX = 17
    String DRUG_PROD_HIERARCHY = "299"
    String IS_MULTI_INGREDIENT_FIELD = "isMultiIngredient"
    String PG_RESPONSE_DATASOURSE_SEPERATOR = "Data Source:"
    String EMPTY_STRING = ""
    String NULL_VALUE = null
    String NULL_CHARACTER = "\0"
    String SPACE = " "
    Integer EIGHT_THOUSAND = 8000
    String LEADING_NONWORD_UNDERSCORE_REGEX = /^[\W_]+/
    String DOUBLE_BACKSLASH_NON_ALPHANUMERIC_SPOTFIRE_SPECIFIC_REGEX = "\\\\|[^a-zA-Z0-9\\-]|/[+().,='\"!@;:#\$%&*^<?|>\\\\/]/"
    String DEFAULT_SQL_CONDITION = '1=1'
    String COMMA_SEPARATOR = ", "
    String DELETED_CASES_QUERY = 'Deleted Cases'
    String SIGNAL_OUTCOME = 'Signal Outcome'
    String ACTION_TAKEN = 'Action Taken'
    String DISPOSITION = 'Disposition'
    String SIGNAL_NAME = '(?i)\\[Signal Name\\]'
    String PRODUCT_NAME = '(?i)\\[Product Name\\]'
    String SIGNAL_LINK = '(?i)\\[Signal Link\\]'
    Integer MIN_LENGTH = 0
    Integer MAX_LENGTH = 255
    Integer MAX_FILE_NAME_LENGTH = 200
    interface Search {
        Long MAX_SEARCH_RESULTS = 25
        Long MIN_OFFSET = 0
        String FAIL = "FAILURE"
        String INVALID_PARAM = "INVALID_PARAMETERS"

    }
    interface defaultUrls {
        String NO_URL = "javascript:void(0)"
    }
    interface OracleFunctions {
        String NVL = """def NVL(def field1, def field2){
        if(field1 != null && field1 != 0){
            return field1
        } else if(field2 != null && field2 != 0){
            return field2
        } else {
            return 0
        }
        }"""
    }

    interface SpecialCharacters {
        //By default these special characters are not allowed.
        String[] DEFAULT_CHARS = ["<", ">", "!", "|", "/", "\\","#"]
        String[] DEFAULT_DISP_CHARS = ["<", ">", "!", "|", "/", "\\", "#", ",", "'", "&"]
        //By default these special characters are not allowed for textarea and description fields.
        String[] TEXTAREA_CHARS = ["<", ">"]
    }

    interface ButtonClass {
        String HIDDEN = "hidden"
    }
    interface SpotfireStatus {
        String IN_PROGRESS = "InProgress"
        String FINISHED = "Finished"
        String FAILED = "Failed"
        String QUEUED = "Queued"
        String CANCELED = "Canceled"
        String NOTSET = "NotSet"
    }
    interface JSONColumns {

        String COLUMNS = "columns"
        String SEQUENCE = "seq"
        String RENAMED_COLUMN = "newName"
    }
    interface CaseDetailFields {
        String TENANT_ID = "TENANT_ID"
        String VERSION_NUM = "VERSION_NUM"
        String MASTER_CASE = "MASTER_CASE"
        String CASE_ID = "CASE_ID"
        String SECTION = "SECTION"
        String COMMENTS = "Comments"
        String FULL_TEXT_FIELD = "FullTextTypeField"
        String COMMENT_FIELD = "CommentField"
        String CHECK_SEQ_NUM = "check_seq_num"
        String CHECK_SEQ_NUM_TWO = "check_seq_num_two"
        String CONTAINS_VALUES = "containsValues"
        List TREE_NODES = ["Workflow and Categories Management", "Case Detail", "Attachments", "Comments", "Actions", "History"]
    }
    interface CaseDetailUniqueName {
        String EVENT_SERIOUSNESS = "EventSeriousness"
        String DEVICE_INFORMATION = "DeviceInformation"
        String REFERENCE_NUMBER = "Reference Number"
        String REFERENCE_NUMBER_VAL = "ReferenceNumber"
        String REFERENCE_TYPE = "Reference Type"
        String REFERENCE_TYPE_VAL = "ReferenceType"
        String DUPLICATE_CASES = "Duplicate Cases"
        String LINKED_CHILD_CASE = "LinkedChildCase"
        String DOB_VAL="DOB"
    }

    interface DateFormat {
        String WITH_TZ = "yyyy-MM-dd'T'HH:mm:ssZZ"
        String WITHOUT_SECONDS = "yyyy-MM-dd'T'HH:mmZ"
        String SIMPLE_DATE = "MMM-dd-yyyy"
        String BASIC_DATE = "yyyyMMdd"
        String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm.SSZZ"
        String NO_TZ = "yyyy-MM-dd HH:mm:ss"
        String DISPLAY_DATE = "MM/DD/YYYY"
        String DISPLAY_NEW_DATE = "dd-MM-yyyy"
        String STANDARD_DATE = "dd-MMM-yyyy"
        String STANDARD_DATE_WITH_TIME = "dd-MMM-yyyy HH:mm:ss"
        String MIN_DATE = "Mon Jan 01 00:00:00 UTC 1900"
        String SCHEDULE_DATE = "yyyy-MM-dd'T'HH:mm"
        String REGULAR_DATETIME = "dd-MMM-yyyy HH:mm:ss"
    }

    interface DynamicReports {
        String ADVANCED_OPTIONS_SUFFIX = "-adv"
        String COMPANY_LOGO = "company-logo.png"
        String SIGNAL_SUMM_RPT = "Signal Summary Report"
        String AGG_CASE_ALERT = "Aggregate Case Alerts"
        String CASE_FORM = "Case Form"
        String CASE_FORM_EXPORT = "Case Form Export for Alert"
        String ASSIGNED_TO_CHANGE = "Assigned To Change"
        String CASE_FORM_GROUPING = "Case Number"
        String CONFIDENTIAL_LOGO = "sensitivity-label-confidential.png"
        String TEST_SIGNAL = "Test Bed Report"
    }

    interface AuditLog {
        String BATCH_LOT_ENTITY="Batch Lot"
        String BATCH_LOT_ENTITY_VALUE = "Batch Lot ETL Status"
        String AUDIT_CONTROL_PANEL="Control Panel"
        String NO_VALUE = "AUDIT_LOG_NO_VALUE"
        String EMPTY_VALUE = ""
        String ACTION = "Action"
        String EVDAS_REVIEW = "EVDAS Review"
        String ADHOC_AGGREGATE_REVIEW = "Adhoc Aggregate Review"
        String ADHOC_SINGLE_REVIEW = "Adhoc Individual Case Review"
        String ADHOC_EVDAS_REVIEW = "Adhoc EVDAS Review"
        String AGGREGATE_REVIEW = "Aggregate Review"
        String SINGLE_REVIEW = "Individual Case Review"
        String LITERATURE_REVIEW = "Literature Review"
        String ADHOC_REVIEW = "Ad-Hoc Review"
        String AGGREGATE_REVIEW_DASHBOARD = "Aggregate Review Dashboard"
        String SINGLE_REVIEW_DASHBOARD = "Individual Case Review Dashboard"
        String LITERATURE_REVIEW_DASHBOARD = "Literature Review Dashboard"
        String EVDAS_REVIEW_DASHBOARD = "EVDAS Review Dashboard"
        String ADHOC_REVIEW_DASHBOARD = "Adhoc Review Dashboard"
        String REPORTING="Reporting"
        String CATEGORY=": Categories"
        Map typeToEntityMap = [
                "Single Case Alert":"Individual Case Review",
                "Aggregate Case Alert":"Aggregate Review",
                "Literature Search Alert":"Literature Review",
                "EVDAS Alert": "EVDAS Review",
                "Single Case Alert on Demand":"Ad-Hoc Review"
        ]
        Map typeToConfigMap = [
                "Single Case Alert":"Individual Case Alert",
                "Aggregate Case Alert":"Aggregate Alert",
                "Literature Search Alert":"Literature Alert",
                "EVDAS Alert": "EVDAS Alert",
                "Ad-Hoc Alert":"Ad-Hoc Alert"
        ]
        Map domainToEntityMap = [
                "ArchivedSingleCaseAlert":"Individual Case Review: Archived Alert",
                "SingleCaseAlert":"Individual Case Review",
                "ArchivedAggregateCaseAlert":"Aggregate Review: Archived Alert",
                "AggregateCaseAlert":"Aggregate Review",
                "ArchivedLiteratureAlert":"Literature Review: Archived Alert",
                "LiteratureAlert":"Literature Review",
                "ArchivedEvdasAlert": "EVDAS Review: Archived Alert",
                "EvdasAlert": "EVDAS Review"
        ]

    }

    interface AlertConfigType {
        String SINGLE_CASE_ALERT = "Single Case Alert"
        String SINGLE_CASE_ALERT_DEMAND = "Single Case Alert on Demand"
        String SINGLE_CASE_ALERT_DASHBOARD = "Single Case Alert - Dashboard"
        String SINGLE_CASE_ALERT_DRILL_DOWN = "Single Case Alert - DrillDown"
        String SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC = "Single Case Alert - Adhoc DrillDown"
        String SINGLE_CASE_ALERT_FAERS = "Single Case Alert - Faers"
        String SINGLE_CASE_ALERT_VAERS = "Single Case Alert - Vaers"
        String SINGLE_CASE_ALERT_VIGIBASE = "Single Case Alert - Vigibase"
        String SINGLE_CASE_ALERT_JADER = "Single Case Alert - Jader"
        String AGGREGATE_CASE_ALERT = "Aggregate Case Alert"
        String UNDERSCORE_AGGREGATE_CASE_ALERT = "AGGREGATE_CASE_ALERT"
        String AGGREGATE_CASE_ALERT_SMQ = "Aggregate Case Alert - SMQ"
        String AGGREGATE_CASE_ALERT_SMQ_FAERS = "Aggregate Case Alert - SMQ FAERS"
        String AGGREGATE_CASE_ALERT_SMQ_VAERS = "Aggregate Case Alert - SMQ VAERS"
        String AGGREGATE_CASE_ALERT_SMQ_VIGIBASE = "Aggregate Case Alert - SMQ VIGIBASE"
        String AGGREGATE_CASE_ALERT_SMQ_JADER = "Aggregate Case Alert - SMQ JADER"
        String AGGREGATE_CASE_ALERT_DASHBOARD = "Aggregate Case Alert - Dashboard"
        String AGGREGATE_CASE_ALERT_CUMMULATIVE = "Aggregate Case Alert - Cumulative"
        String AGGREGATE_CASE_ALERT_DEMAND = "Aggregate Case Alert on Demand"
        String AGGREGATE_CASE_ALERT_DEMAND_FAERS = "Aggregate Case Alert on Demand - FAERS"
        String AGGREGATE_CASE_ALERT_DEMAND_VAERS = "Aggregate Case Alert on Demand - VAERS"
        String AGGREGATE_CASE_ALERT_DEMAND_VIGIBASE = "Aggregate Case Alert on Demand - VIGIBASE"
        String AGGREGATE_CASE_ALERT_DEMAND_JADER = "Aggregate Case Alert on Demand - JADER"
        String AGGREGATE_CASE_ALERT_SMQ_DEMAND = "Aggregate Case Alert - SMQ on Demand"
        String AGGREGATE_CASE_ALERT_SMQ_DEMAND_FAERS = "Aggregate Case Alert - SMQ on Demand FAERS"
        String AGGREGATE_CASE_ALERT_SMQ_DEMAND_VAERS = "Aggregate Case Alert - SMQ on Demand VAERS"
        String AGGREGATE_CASE_ALERT_SMQ_DEMAND_VIGIBASE = "Aggregate Case Alert - SMQ on Demand VIGIBASE"
        String AGGREGATE_CASE_ALERT_SMQ_DEMAND_JADER = "Aggregate Case Alert - SMQ on Demand JADER"
        String LITERATURE_SEARCH_ALERT = "Literature Search Alert"
        String EMBASE_LITERATURE_SEARCH_ALERT = "Embase Literature Search Alert"
        String AGGREGATE_CASE_ALERT_FAERS = "Aggregate Case Alert - FAERS"
        String AGGREGATE_CASE_ALERT_VAERS = "Aggregate Case Alert - VAERS"
        String AGGREGATE_CASE_ALERT_VIGIBASE = "Aggregate Case Alert - VIGIBASE"
        String AGGREGATE_CASE_ALERT_JADER = "Aggregate Case Alert - JADER"
        String AD_HOC_ALERT = "Ad-Hoc Alert"
        String SIGNAL_MANAGEMENT = "Signal Management"
        String TOPIC = "Topic"
        String VALIDATED_SIGNAL = "Validated Signal"
        String COMMUNICATION = "communication"
        String EVDAS_ALERT = "EVDAS Alert"
        String EVDAS_ALERT_DEMAND = "EVDAS Alert on Demand"
        String EVDAS_ALERT_DASHBOARD = "EVDAS Alert - Dashboard"
        String EVDAS_ALERT_CUMMULATIVE = "EVDAS Alert - Cumulative"
        String SIGNAL = "Signal"
        String EVENT_INFORMATION = "Event Information"
        String INDIVIDUAL_CASE_ALERT = "Individual Case Alert"
        String INDIVIDUAL_ON_DEMAND = "Qualitative on demand"
        String INDIVIDUAL_CASE_CONFIGURATIONS = "Individual Case Alert Configuration"
        String AGGREGATE_CASE_CONFIGURATIONS ="Aggregate Alert Configuration"
        String EVDAS_ALERT_CONFIGURATIONS ="EVDAS Alert Configuration"
        String LITERATURE_ALERT_CONFIGURATIONS ="Literature Alert Configuration"
        String INDIVIDUAL_CASE_REVIEW = "Individual Case Review"
        String ADHOC_ALERT = "Adhoc Alert"
    }

    interface AlertConfigTypeShort {
        String QUALITATIVE_ALERT = 'Individual Case'
        String QUANTITATIVE_ALERT = 'Aggregate'
        String ON_DEMAND = 'On Demand'
        String CONTAINER_2_STRING = '"containerView":2'
    };

    interface HistoryType {
        String PRIORITY = "PRIORITY"
        String DISPOSITION = "DISPOSITION"
        String ASSIGNED_TO = "ASSIGNED_TO"
        String ALERT_TAGS = "ALERT_TAGS"
        String STATISTIC_SCORES = "STATISTIC_SCORES"
        String GROUP = "GROUP"
        String FIRST_EXECUTION = "FIRST_EXECUTION"
        String SUB_TAGS = "SUB_TAGS"
        String ACTION = "ACTION"
        String UNDO_ACTION = "UNDO_ACTION"
    }
    interface SubGroups {
        String REL = "Rel"
    }

    interface Commons {
        String TAGS = "tags"
        String GLOBAL_TAG = "GLOBAL_TAG"
        String CASE_SERIES_TAG = "CASE_SERIES_TAG"
        String PRIVATE_TAG_ALERT= "PRIVATE_TAG_ALERT"
        String PRIVATE_TAG_GLOBAL = "PRIVATE_TAG_GLOBAL"
        String DASHBOARD = "dashboard"
        String ASSIGNED_DASHBOARD = "Assigned"
        String NO = "No"
        String YES = "Yes"
        String BLANK_STRING = ""
        String NA_LISTED= "N/A"
        String SPACE = " "
        String DASH_STRING = "-"
        String SPACE_HYPHEN_SPACE = " - "
        String UNDERSCORE = "_"
        String COMMA = ","
        String EMPTY_LIST = "[]"
        String ALL = "ALL"
        String NONE = "NONE"
        String UNDEFINED = "undefined"
        String UNCODED = "<UNCODED>"
        Integer UNDEFINED_NUM = 0
        String UNDEFINED_NUM_STR = "0"
        Integer UNDEFINED_NUM_INT_REVIEW = -1
        Integer ZERO = 0
        Integer ONE = 1
        Integer TWO = 2
        Integer VAERS_END_DATE_INDEX = 13
        Integer SEVEN = 7
        String SYSTEM = "SYSTEM"
        String LOW = "LOW"
        String HIGH = "HIGH"
        String TRIGGERED_ALERTS = "Triggered Alerts"
        String TRIGGERED_ALERT = "Triggered Alert"
        String REVIEW = "review"
        String POSITIVE = "Positive"
        String NEGATIVE = "Negative"
        String EVEN = ""
        Long DEFAULT_COLUMN_LIST_USER_ID = 0L
        String LOCALE_EN = "en"
        String ROLE_DEV = "ROLE_DEV"
        String DEFAULT_VIEW = "System View"
        String NA = "NA"
        String AGE_STANDARD_UNIT = "Years"
        String TAG_LABEL = "Category/Sub-Category(ies)"
        String TAG = "Categories"
        String SUBTAG = "Sub-Category(ies)"
        String THRESHOLD_VALUE = 'THRESHOLD_VALUE'
        String PREVIOUS = '-Previous'
        String CHI_SQUARE = 'chiSquare'
        String LAST_UPDATED = 'lastUpdated'
        String DATE_CREATED = 'dateCreated'
        String EVENTS = 'EVENTS'
        String PRODUCTS = 'PRODUCTS'
        String DISPOSITION_REQUIRES_REVIEW = 'Requires Review'
        String DISPOSITION_THRESHOLD_NOT_MET = 'Threshold Not Met'
        String SAFETY_TOPIC = 'Safety Topic'
        String YES_LOWERCASE = "yes"
        String YES_UPPERCASE = "YES"
        String NO_LOWERCASE = "no"
        String NO_UPPERCASE = "NO"
        String SHARED = "(S)"
        Integer RESUME_REPORT = 4
        Integer RESUME_SPOTFIRE = 5
        Double UNDEFINED_NUM_DOUBLE = -1.0
        String RETAINED_CATEGORY = "*"
        String SIGNAL_DATASOURCE_SEPERATOR = "##"
        String PRODUCT_GROUP_VALUE = "199"
        String NEW_UPPERCASE = "NEW"
        String BULK_API_BATCH_ID="batchId"
        String BULK_API_BATCH_DATE="batchDate"
        String BULK_API_BATCH_VALID_RECORD_COUNT="validRecordCount"
        String BULK_API_BATCH_COUNT="count"
        String BULK_API_BATCH_UPLOADED_DATE="uploadedAt"
        String BULK_API_BATCH_ADDED_BY="addedBy"
        String UNIQUE_IDENTIFIER="uniqueIdentifier"
        String SMQ_ADVANCEDFILTER_LABEL="SMQ"
        String PT_ADVANCEDFILTER_LABEL="PT"
        String CASE_VERSION_NO = "Case(Ver#)"
        String LAST_EXECUTED_DATE_RANGE = "Last Executed Date Range"
        String S_UNDERSCORE = 'S_'
        Integer TWENTY = 20
        Integer VALIDATED_SIGNAL_NAME_MAX_LIMIT = 255
    }

    interface SignalTables {
        String AUTO_ALERT_CASES = "PVS_AUTO_ALERT_INCR_CASES"
    }
    interface DbDataSource{
        String IS_ARISG_PVIP = "isArisgPvip"
        String ARISG = "ARISG"
        String PVIP = "PVIP"
        String PVCM = "PVCM"
    }
    interface SingleCaseViewAvailability{
        String ALL = "ALL"
        String ONLY_SAFETY = "ONLY_SAFETY"
        String ONLY_FDA = "ONLY_FDA"
    }


    interface DataSource {
        String PVA = "pva"
        String VIGIBASE = "vigibase"
        String VIGIBASE_CAMEL_CASE = "VigiBase"
        String FAERS = "faers"
        String EVDAS = "evdas"
        String EUDRA = "eudra"
        String VAERS = "vaers"
        String JADER = "jader"
        String DATASOURCE_EUDRA = "EVDAS"
        String DATASOURCE_PVA = Holders.config.signal.dataSource.safety.name
        String DATASOURCE_FAERS = "FAERS"
        String DATASOURCE_VAERS = "VAERS"
        String DATASOURCE_VIGIBASE = "VIGIBASE"
        String DATASOURCE_JADER = "JADER"
        String PUB_MED = "PubMed"
        String EMBASE = "Embase"
        String DATASOURCE_SAFETY_DB = "SAFETY DB"
    }

    interface ReferenceName {
        String ASSESSMENT_REPORTS = "AssessmentReports"
        String ASSESSMENT_DETAILS = "AssessmentDetails"
        String CHARTS = "Charts"
        String SERIOUSNESS_COUNTS_OVER_TIME = "Seriousness_Counts_over_time"
        String AGE_GROUP_OVER_TIME = "Age_Group_Over_time"
        String GENDER_OVER_TIME = "Gender_Over_time"
        String COUNTY_OVER_TIME = "Country_Over_time"
        String CASE_OUTCOME = "Case_Outcome"
        String SOURCE_OVER_TIME = "Source_Over_time"
    }

    interface BusinessConfigType {
        String PRR = "PRR"
        String ROR = "ROR"
        String EBGM = "EBGM"
        String E = "eValue"
        String RR = "rrValue"
        String PREVIOUS_PERIOD = 'Previous Period '
        def EBGMTYPE = [EBGM: 'EBGM', EB05: 'EB05', EB95: 'EB95', EB05CHECK: 'EB05 > Prev-EB95']
        List prrRor = ['prrLCI','prrUCI','rorLCI','rorUCI']
    }

    interface ProductSelectionType {
        String PRODUCT = "product"
        String FAMILY = "family"
        String INGREDIENT = "ingredient"
    }

    interface ProductSelectionTypeValue {
        String PRODUCT = "3"
        String FAMILY = "2"
        String INGREDIENT = "1"
    }

    interface AlertActions {
        String COPY = 'copy'
        String CREATE = 'create'
        String EDIT = 'edit'
    }

    interface SignalReportTypes {
        String PEBER = "peber"
        String SIGNAL_SUMMARY = "signalSummary"
        String SIGNALS_BY_STATE = "Signals By State"
    }

    interface DMSDocTypes {
        String SIGNAL_SUMMARY_REPORT = "Signal Summary Report"
        String SIGNAL_ASSESSMENT_REPORT = "Signal Assessment Report"
        String PBRER_SIGNAL_SUMMARY_REPORT = "PBRER Signal Summary Report"
        String SIGNAL_ACTION_DETAIL_REPORT = "All Signal Actions"
        String GENERATED_REPORT = "Generated Report"
    }

    interface AssignmentType {
        String USER = "USER"
        String GROUP = "GROUP"
        String AUTO_ASSIGN = "Auto Assign"
    }

    interface SignalReportOutputType {
        String PDF = "PDF"
        String DOCX = "DOCX"
        String XLSX = "XLSX"
    }

    interface Scheduler {
        String RUN_ONCE = "FREQ=DAILY;INTERVAL=1;COUNT=1"
        String HOURLY = "FREQ=HOURLY"
    }

    interface ProductDictionarySelection {
        String INGREDIENT = "INGREDIENT"
        String FAMILY = "FAMILY"
        String PRODUCT = "PRODUCT"
        String LICENCE = "LICENCE"
    }

    interface ExcelDataUpload {
        String CELL_DATA_TYPE_NUMBER = "NUMBER"
        String ERMR_DATA_CAPTURE_POINTER = "Active Substance"
        String CASE_LISTING_CAPTURE_POINTER = "EU Local Number"
        String ERMR_LINK_NEW_EVPM = "NEW EVPM"
        String ERMR_LINK_TOTAL_EVPM = "TOT EVPM"

    }

    interface EvdasUpload {
        String FILE_DOWNLOAD_STATUS_SUCCESS = "PASS"
        String FILE_DOWNLOAD_STATUS_FAIL = "FAIL"
        Integer FILE_DOWNLOAD_COUNT_FAIL = -1
        Integer FOLDER_MAX_SIZE_LIMIT = 255
        Integer FOLDER_MIN_SIZE_LIMIT = 0

    }
    interface EventFields {
        String BROAD = "(Broad)"
        String NARROW = "(Narrow)"
    }


    interface SignalCounts {
        String PVACOUNT = "PVACOUNT"
        String EVDASCOUNT = "EVDASCOUNT"
        String CASECOUNT = "CASECOUNT"
        String CASECOUNT_DEMAND = "CASECOUNT_DEMAND"

    }

    interface BusinessConfigAttributes {
        String DME_AGG_ALERT = "DME"
        String IME_AGG_ALERT = "IME"
        String SPECIAL_MONITORING = "SPECIAL_MONITORING"
        String STOP_LIST = "STOP_LIST"
        String POSITIVE_RE_CHALLENGE = "positiveRechallenge"
        String NEW_PROD_COUNT = "newProdCount"
        String DSS_SCORE="pecImpNumHigh"
        String E_VALUE="eValue"
        String RR_VALUE="rrValue"
        String NEW_CUM_COUNT = "cumProdCount"
        String PERCENTAGE_INCREASE_FROM_PREVIOUS_PERIOD = "PERCENTAGE_INCREASE_FROM_PREVIOUS_PERIOD"
        String Chi_Square="Chi-Square"

        String PRR_SCORE = "prrValue"
        String PRRLCI_SCORE = "prrLCI"
        String PRRUCI_SCORE = "prrUCI"
        String ROR_SCORE = "rorValue"
        String RORLCI_SCORE = "rorLCI"
        String RORUCI_SCORE = "rorUCI"
        String EBGM_SCORE = "ebgm"
        String EB05_SCORE = "eb05"
        String EB95_SCORE = "eb95"
        String NEW_COUNT = "newCount"
        String CUMM_COUNT = "cumCount"
        String NEW_SPON_COUNT = "newSponCount"
        String NEW_SPON = "newSponCount"
        String CUM_SPON_COUNT = "cumSponCount"
        String NEW_STUDY_COUNT = "newStudyCount"
        String CUM_STUDY_COUNT = "cumStudyCount"
        String NEW_SERIOUS_COUNT = "newSeriousCount"
        String CUM_SERIOUS_COUNT = "cumSeriousCount"
        String NEW_FATAL_COUNT = "newFatalCount"
        String CUM_FATAL_COUNT = "cumFatalCount"

        String NEW_INTERACTING_COUNT = "newInteractingCount"
        String CUMM_INTERACTING_COUNT = "cumInteractingCount"
        String NEW_GERIATRIC_COUNT = "newGeriatricCount"
        String CUM_GERIATRIC_COUNT = "cumGeriatricCount"
        String NEW_PEDIATRIC_COUNT = "newPediatricCount"
        String CUM_PEDIATRIC_COUNT = "cumPediatricCount"
        String NEW_NON_SERIOUS_COUNT = "newNonSerious"
        String CUM_NON_SERIOUS_COUNT = "cumNonSerious"

        //PVS-53957 bug fix code starts from here.
        String CHI_SQUARE = "chiSquare"
        String LISTEDNESS = "LISTEDNESS"
        //PVS-53957 bug fix code end here. 
        String NEW_EVENT = "NEW_EVENT"
        String PREVIOUS_CATEGORY = "PREVIOUS_CATEGORY"
        String ALL_CATEGORY = "ALL_CATEGORY"
        String TREND_TYPE = "trendType"
        String TREND_FLAG = "trendFlag"
        String FREQ_PERIOD = "freqPeriod"
        String CUM_FREQ_PERIOD = "cumFreqPeriod"

        //Faers Columns
        String NEW_COUNT_FAERS = "newCountFaers"
        String CUMM_COUNT_FAERS = "cumCountFaers"
        String NEW_PAED_FAERS = "newPediatricCountFaers"
        String CUMM_PAED_FAERS = "cumPediatricCountFaers"
        String NEW_GERIA_FAERS = "newGeriatricCountFaers"
        String CUMM_GERIA_FAERS = "cumGeriatricCountFaers"
        String CHI_SQUARE_FAERS = "chiSquareFaers"
        String NEW_INTER_FAERS = "newInteractingCountFaers"
        String CUMM_INTER_FAERS = "cumInteractingCountFaers"
        String NEW_NON_SERIOUS_FAERS = "newNonSeriousFaers"
        String CUM_NON_SERIOUS_FAERS = "cumNonSeriousFaers"
        String NEW_SPON_COUNT_FAERS = "newSponCountFaers"
        String CUM_SPON_COUNT_FAERS = "cumSponCountFaers"
        String NEW_STUDY_COUNT_FAERS = "newStudyCountFaers"
        String CUM_STUDY_COUNT_FAERS = "cumStudyCountFaers"
        String NEW_SERIOUS_COUNT_FAERS = "newSeriousCountFaers"
        String CUM_SERIOUS_COUNT_FAERS = "cumSeriousCountFaers"
        String NEW_FATAL_COUNT_FAERS = "newFatalCountFaers"
        String CUM_FATAL_COUNT_FAERS = "cumFatalCountFaers"
        String PRR_SCORE_FAERS = "prrValueFaers"
        String PRRLCI_SCORE_FAERS = "prrLCIFaers"
        String PRRUCI_SCORE_FAERS = "prrUCIFaers"
        String ROR_SCORE_FAERS = "rorValueFaers"
        String RORLCI_SCORE_FAERS = "rorLCIFaers"
        String RORUCI_SCORE_FAERS = "rorUCIFaers"
        String EBGM_SCORE_FAERS = "ebgmFaers"
        String EB05_SCORE_FAERS = "eb05Faers"
        String EB95_SCORE_FAERS = "eb95Faers"
        String RR_VALUE_FAERS = "rrValueFaers"
        String E_VALUE_FAERS = "eValueFaers"

        //Vaers Columns
        String NEW_COUNT_VAERS = "newCountVaers"
        String CUMM_COUNT_VAERS= "cumCountVaers"
        String NEW_SERIOUS_COUNT_VAERS = "newSeriousCountVaers"
        String CUM_SERIOUS_COUNT_VAERS = "cumSeriousCountVaers"
        String NEW_FATAL_COUNT_VAERS = "newFatalCountVaers"
        String CUM_FATAL_COUNT_VAERS = "cumFatalCountVaers"
        String PRR_SCORE_VAERS = "prrValueVaers"
        String PRRLCI_SCORE_VAERS = "prrLCIVaers"
        String PRRUCI_SCORE_VAERS = "prrUCIVaers"
        String ROR_SCORE_VAERS = "rorValueVaers"
        String RORLCI_SCORE_VAERS = "rorLCIVaers"
        String RORUCI_SCORE_VAERS = "rorUCIVaers"
        String EBGM_SCORE_VAERS = "ebgmVaers"
        String EB05_SCORE_VAERS = "eb05Vaers"
        String EB95_SCORE_VAERS = "eb95Vaers"
        String NEW_GERIATRIC_COUNT_VAERS = "newGeriatricCountVaers"
        String CUM_GERIATRIC_COUNT_VAERS = "cumGeriatricCountVaers"
        String NEW_PAED_VAERS = "newPediatricCountVaers"
        String CUM_PAED_VAERS = "cumPediatricCountVaers"
        String CHI_SQUARE_VAERS = "chiSquareVaers"
        String RR_VALUE_VAERS = "rrValueVaers"
        String E_VALUE_VAERS = "eValueVaers"

        //Vigibase Columns
        String NEW_COUNT_VIGIBASE = "newCountVigibase"
        String CUMM_COUNT_VIGIBASE= "cumCountVigibase"
        String NEW_SERIOUS_COUNT_VIGIBASE = "newSeriousCountVigibase"
        String CUM_SERIOUS_COUNT_VIGIBASE = "cumSeriousCountVigibase"
        String NEW_FATAL_COUNT_VIGIBASE = "newFatalCountVigibase"
        String CUM_FATAL_COUNT_VIGIBASE = "cumFatalCountVigibase"
        String PRR_SCORE_VIGIBASE = "prrValueVigibase"
        String PRRLCI_SCORE_VIGIBASE = "prrLCIVigibase"
        String PRRUCI_SCORE_VIGIBASE = "prrUCIVigibase"
        String ROR_SCORE_VIGIBASE = "rorValueVigibase"
        String RORLCI_SCORE_VIGIBASE = "rorLCIVigibase"
        String RORUCI_SCORE_VIGIBASE = "rorUCIVigibase"
        String EBGM_SCORE_VIGIBASE = "ebgmVigibase"
        String EB05_SCORE_VIGIBASE = "eb05Vigibase"
        String EB95_SCORE_VIGIBASE = "eb95Vigibase"
        String NEW_GERIATRIC_COUNT_VIGIBASE = "newGeriatricCountVigibase"
        String CUM_GERIATRIC_COUNT_VIGIBASE = "cumGeriatricCountVigibase"
        String NEW_PAED_VIGIBASE = "newPediatricCountVigibase"
        String CUM_PAED_VIGIBASE = "cumPediatricCountVigibase"
        String CHI_SQUARE_VIGIBASE = "chiSquareVigibase"
        String RR_VALUE_VIGIBASE = "rrValueVigibase"
        String E_VALUE_VIGIBASE = "eValueVigibase"

        //Jader Columns
        String NEW_COUNT_JADER = "newCountJader"
        String CUMM_COUNT_JADER= "cumCountJader"
        String NEW_SERIOUS_COUNT_JADER = "newSeriousCountJader"
        String CUM_SERIOUS_COUNT_JADER = "cumSeriousCountJader"
        String NEW_FATAL_COUNT_JADER = "newFatalCountJader"
        String CUM_FATAL_COUNT_JADER = "cumFatalCountJader"
        String PRR_SCORE_JADER = "prrValueJader"
        String PRRLCI_SCORE_JADER = "prrLCIJader"
        String PRRUCI_SCORE_JADER = "prrUCIJader"
        String ROR_SCORE_JADER = "rorValueJader"
        String RORLCI_SCORE_JADER = "rorLCIJader"
        String RORUCI_SCORE_JADER = "rorUCIJader"
        String EBGM_SCORE_JADER = "ebgmJader"
        String EB05_SCORE_JADER = "eb05Jader"
        String EB95_SCORE_JADER = "eb95Jader"
        String NEW_GERIATRIC_COUNT_JADER = "newGeriatricCountJader"
        String CUM_GERIATRIC_COUNT_JADER = "cumGeriatricCountJader"
        String NEW_PAED_JADER = "newPediatricCountJader"
        String CUM_PAED_JADER = "cumPediatricCountJader"
        String CHI_SQUARE_JADER = "chiSquareJader"
        String RR_VALUE_JADER = "rrValueJader"
        String E_VALUE_JADER = "eValueJader"




        String CONTINUING_TREND = "Continuing Trend"
        String EMERGING_TREND = "Emerging Trend"
        String NO_TREND = "No Trend"



    }


    interface BusinessConfigAttributesEvdas {
        String DME_AGG_ALERT = "DME"
        String IME_AGG_ALERT = "IME"
        String SPECIAL_MONITORING = "SPECIAL_MONITORING"
        String STOP_LIST = "STOP_LIST"
        String POSITIVE_RE_CHALLENGE = "POSITIVE_RE_CHALLENGE"
        String NEW_PROD_COUNT = "newProdCount"
        String DSS_SCORE="pecImpNumHigh"
        String E_VALUE="eValue"
        String RR_VALUE="rrValue"
        String PERCENTAGE_INCREASE_FROM_PREVIOUS_PERIOD = "PERCENTAGE_INCREASE_FROM_PREVIOUS_PERIOD"
        String EVDAS_LISTEDNESS = "EVDAS_LISTEDNESS"
        String NEW_EVENT = "NEW_EVENT"
        String PREVIOUS_CATEGORY = "PREVIOUS_CATEGORY"
        String ALL_CATEGORY = "ALL_CATEGORY"

        String ROR_EUROPE_EVDAS = "rorEuropeEvdas"
        String ROR_N_AMERICA_EVDAS = "rorNAmericaEvdas"
        String ROR_JAPAN_EVDAS = "rorJapanEvdas"
        String ROR_ASIA_EVDAS = "rorAsiaEvdas"
        String ROR_REST_EVDAS = "rorRestEvdas"
        String ROR_ALL_EVDAS = "rorAllEvdas"
        String SDR_EVDAS = "sdrEvdas"
        String CHANGES_EVDAS = "changesEvdas"
        String EVDAS_IME_DME = "dmeImeEvdas"
        String RELTV_ROR_PAED_VS_OTHR = "ratioRorPaedVsOthersEvdas"
        String EVDAS_SDR_PAED = "sdrPaedEvdas"
        String RELTV_ROR_GERTR_VS_OTHR = "ratioRorGeriatrVsOthersEvdas"
        String EVDAS_SDR_GERTR = "sdrGeratrEvdas"
        String NEW_EV_EVDAS = "newEvEvdas"
        String TOTAL_EV_EVDAS = "totalEvEvdas"
        String NEW_EEA_EVDAS = "newEEAEvdas"
        String TOTAL_EEA_EVDAS = "totEEAEvdas"
        String NEW_HCP_EVDAS = "newHCPEvdas"
        String TOTAL_HCP_EVDAS = "totHCPEvdas"
        String NEW_SERIOUS_EVDAS = "newSeriousEvdas"
        String TOTAL_SERIOUS_EVDAS = "totalSeriousEvdas"
        String NEW_OBS_EVDAS = "newObsEvdas"
        String TOTAL_OBS_EVDAS = "totObsEvdas"
        String NEW_FATAL_EVDAS = "newFatalEvdas"
        String TOTAL_FATAL_EVDAS = "totalFatalEvdas"
        String NEW_MED_ERR_EVDAS = "newMedErrEvdas"
        String TOTAL_MED_ERR_EVDAS = "totMedErrEvdas"
        String NEW_PLUS_RC_EVDAS = "newRcEvdas"
        String TOTAL_PLUS_RC_EVDAS = "totRcEvdas"
        String NEW_LITERATURE_EVDAS = "newLitEvdas"
        String TOTAL_LITERATURE_EVDAS = "totalLitEvdas"
        String NEW_PAED_EVDAS = "newPaedEvdas"
        String TOTAL_PAED_EVDAS = "totalPaedEvdas"
        String NEW_GERIAT_EVDAS = "newGeriatEvdas"
        String TOTAL_GERIAT_EVDAS = "totalGeriatEvdas"
        String NEW_SPON_EVDAS = "newSponEvdas"
        String TOTAL_SPON_EVDAS = "totSpontEvdas"
        String TOTAL_SPON_EUROPE = "totSpontEuropeEvdas"
        String TOTAL_SPON_N_AMERICA = "totSpontNAmericaEvdas"
        String TOTAL_SPON_JAPAN = "totSpontJapanEvdas"
        String TOTAL_SPON_ASIA = "totSpontAsiaEvdas"
        String TOTAL_SPON_REST = "totSpontRestEvdas"




    }

    interface AggregateAlertFields {
        String TREND_FLAG = "trendFlag"
        String RATIONALE = "rationale"
        String PEC_IMP_NUM_HIGH = "pecImpNumHigh"
        Integer BATCH_SIZE = 3000
    }
    interface DictionaryFilterType {
        String FILTER = "FILTER"
        String SEARCH = "SEARCH"
        String PRODUCT_COLUMN_PRODUCT_ROW = "COL_3"
    }

    interface Groups {
        String GROUP = "GROUP"
        String SAFETY_GROUP = "SAFETY_GROUP"
    }

    interface LiteratureFields {
        String PUBMED_ARTICLE = "PubmedArticle"
        String AUTHOR = "Author"
        String PUB_DATE = "PubDate"
        String ARTICLE_DATE = "ArticleDate"
        String PUB_MED_PUB_DATE = "PubMedPubDate"
        String MEDLINE_DATE = "MedlineDate"
        String PMID = "PMID"
        String ARTICLE_TITLE = "ArticleTitle"
        String TITLE="Title"
        String ABSTRACT_TEXT="AbstractText"
        String LAST_NAME="LastName"
        String FORE_NAME="ForeName"
        String YEAR="Year"
        String MONTH="Month"
        String DAY="Day"
        String PUBMED = "PubMed"
        String EMBASE = "Embase"

    }
    interface AlertType {
        String QUALITATIVE = "Qualitative"
        String QUANTITATIVE = "Quantitative"
        String QUALITATIVE_ALERT = "Qualitative Alert"
        String QUALITATIVE_ON_DEMAND = "QualitativeOnDemandAlert"
        String QUANTITATIVE_ALERT = "Quantitative Alert"
        String AGGREGATE_ALERT = "Aggregate Alert"
        String AGGREGATE_ALERT_FAERS = "Aggregate Alert - FAERS"
        String EVDAS_ALERT = "Evdas Alert"
        String EVDAS_ON_DEMAND = "EvdasOnDemandAlert"
        String LITERATURE_ALERT = "Literature Alert"
        String QUANTITATIVE_ON_DEMAND = "AggregateOnDemandAlert"
        String AGGREGATE_NEW = "Aggregate"
        String INDIVIDUAL_CASE_SERIES = "ICR"
        String EVDAS = "EVDAS"
        String EVDAS_ADHOC = "EVDAS (adhoc)"
        String AGGREGATE_ADHOC = "Aggregate (adhoc)"
        String ICR_ADHOC = "ICR (adhoc)"
        String LITERATURE = "Literature"
    }

    interface CaseInforMapFields {
        String CASE_INFORMATION = "Case Information"
        String PRODUCT_INFORMATION = "Product Information"
        String PRODUCT_INFORMATION_VAERS = "Vaccine Information"
        String PRODUCT_INFORMATION_VIGIBASE = "Product Information"
        String EVENT_INFORMATION = "Event Information"
        String EVENT_INFORMATION_VAERS = "Result or Outcome of the Adverse Event"
        String EVENT_INFORMATION_VIGIBASE = "Event Information"
        String PE_INFORMATION = "Product Event Information"
        String MEDICAL_HISTORY_INFORMATION = "Patient Medical History"
        String DEATH_INFORMATION = "Cause Of Death Information"
        String DOSAGE_INFORMATION = "Dosage Regimen"
        String LAB_INFORMATION = "Lab Data"
        String NARRATIVE_INFORMATION = "Narrative"
        String VERSION_INFORMATION = "Versions"
        String LITERATURE_INFORMATION = "Literature Information"
        String PATIENT_INFORMATION = "Patient Information"
        String PREGNANCY_INFORMATION = "Pregnancy Information"
        String STUDY_INFORMATION = "Study Information"
        String ADDITIONAL_CASE_INFORMATION = "Additional Case Information"
        String PRODUCT_REFERENCE_INFORMATION = "Product Reference Information"
        String CASE_REFERENCE_INFORMATION = "Case References"
        String FDA_REFERENCE_INFORMATION = "Reporter Information"
        String DEVICE_INFORMATION = "Device Information"
        String DEVICE_PROBLEM ="Device Problems"
        String PRIMARY_IND_INFORMATION = "Primary IND#"
        String PRE_ANDA = "Pre-ANDA"
        String CASE_NARRATIVE = "Case Narrative"
        String CASE_ABB_NARRATIVE = "Case Abbreviated Narrative"
        String COMBO_FLAG = 'Combo Flag'
        String ONSET_LATENCY = 'Onset Latency'
        String EVENT_SERIOUSNESS= 'Event Seriousness'
        String DOSAGE_REGIMEN_VIGIBASE = "Dosage Regimen"
        String GENERAL_INFORMATION = "General"
    }

    interface ActionItemFilterType {
        String MY_OPEN = "My Open Action Items"
        String MY_ALL = "My All Action Items"
        String ALL = "All Action Items"
    };

    interface SignalHistory {
        String SIGNAL_CREATED = "New Signal Created"
        String SIGNAL_TYPE = "Signal Creation"
    }

    interface Stratification_Fields {
        String PRR = "PRR"
        String ROR = "ROR"
        String PRRLCI = "PRRLCI"
        String PRRUCI = "PRRUCI"
        String RORLCI = "RORLCI"
        String RORUCI = "RORUCI"
        String EBGM = "EBGM"
        String EB05 = "EB05"
        String EB95 = "EB95"
    }

    interface SignalCharts {
        String AGE_GROUP = 'age-grp-over-time-chart'
        String SERIOUSNESS = 'seriousness-over-time-chart'
        String COUNTRY = 'country-over-time-chart'
        String GENDER = 'gender-over-time-chart'
        String OUTCOME = 'outcome-over-time-chart'
        String SERIOUS_PIE_CHART = 'seriousness-count-pie-chart'
        String HEAT_MAP = 'system-organ-heat-map'
        String ASSESSMENT_DETAILS = 'assessmentDetails'
        String SYSTEM_ORGAN_CLASS = 'systemOrganClass'
    }

    interface TagsSyncCondition {
        String GLOBAL_TAGS = 'GlobalTags'
        String CASE_SERIES_TAGS = 'CaseSeriesTags'
        String SYNC_DONE = 'Done'
    }

    interface ActionStatus {
        String COMPLETED = "Completed"
        String CLOSED = "Closed"
        String DELETED = "Deleted"
        String NEW = "New"
        String EXECUTION_COMPLETED="Execution Completed"
        String EXECUTION_FAILED="Execution Failed"
    }

    interface SMQType {
        String NARROW = "Narrow"
        String BROAD = "Broad"
    }

    interface EmailNotificationModuleKeys {
        String SCA_ALERT = "SCA_ALERT"
        String ACA_PVA = "ACA_PVA"
        String ACA_FAERS = "ACA_FAERS"
        String EVDAS_ALERT = "EVDAS_ALERT"
        String ADHOC_ALERT = "ADHOC_ALERT"
        String LITERATURE_ALERT = "LITERATURE_ALERT"
        String MEETING_CREATION_UPDATION = "MEETING_CREATION_UPDATION"
        String MINUTES_CREATION_UPDATION = "MINUTES_CREATION_UPDATION"
        String ACTION_CREATION_UPDATION = "ACTION_CREATION_UPDATION"
        String ACTION_REMINDER_OVER_DUE = "ACTION_REMINDER_OVER_DUE"
        String ACTION_UPDATE = "ACTION_UPDATE"
        String ACTION_ASSIGNMENT_UPDATE = "ACTION_ASSIGNMENT_UPDATE"
        String DISPOSITION_CHANGE_SCA = "DISPOSITION_CHANGE_SCA"
        String DISPOSITION_CHANGE_ACA_PVA = "DISPOSITION_CHANGE_ACA_PVA"
        String DISPOSITION_CHANGE_ACA_FAERS = "DISPOSITION_CHANGE_ACA_FAERS"
        String DISPOSITION_CHANGE_ACA_VIGIBASE = "DISPOSITION_CHANGE_ACA_VIGIBASE"
        String DISPOSITION_CHANGE_ADHOC = "DISPOSITION_CHANGE_ADHOC"
        String DISPOSITION_CHANGE_EVDAS = "DISPOSITION_CHANGE_EVDAS"
        String DISPOSITION_CHANGE_LITERATURE = "DISPOSITION_CHANGE_LITERATURE"
        String DISPOSITION_CHANGE_SIGNAL = "DISPOSITION_CHANGE_SIGNAL"
        String ASSIGNEE_UPDATE = "ASSIGNEE_UPDATE"
        String BULK_UPDATE = "BULK_UPDATE"
        String SIGNAL_CREATION = "SIGNAL_CREATION"
        String DISPOSITION_AUTO_ROUTE_SCA = "DISPOSITION_AUTO_ROUTE_SCA"
        String DISPOSITION_AUTO_ROUTE_LA = "DISPOSITION_AUTO_ROUTE_LA"
        String SPOTFIRE_GENERATION = "SPOTFIRE_GENERATION"
        String ACA_VAERS = "ACA_VAERS"
        String DISPOSITION_CHANGE_ACA_VAERS = "DISPOSITION_CHANGE_ACA_VAERS"
        String ACA_VIGIBASE = "ACA_VIGIBASE"
        String ACA_JADER = "ACA_JADER"
        String DISPOSITION_CHANGE_ACA_JADER = "DISPOSITION_CHANGE_ACA_JADER"
        String ACA_INTEGRATED = "ACA_INTEGRATED"
        String DISPOSITION_CHANGE_ACA_INTEGRATED = "DISPOSITION_CHANGE_ACA_INTEGRATED"
        String ERMR_UPLOAD_FILE = "ERMR_UPLOAD_FILE"
        String CASE_LISTING_UPLOAD_FILE = "CASE_LISTING_UPLOAD_FILE"
    }

    interface ConfigurationType {
        String QUAL_TYPE = "Qualitative Configuration"
        String QUANT_TYPE = "Quantitative Configuration"
        String EVDAS_TYPE = "EVDAS Configuration"
        String ADHOC_TYPE = "Ad-Hoc Alert"
        String LITERATURE_TYPE = "Literature Configuration"
    }

    interface Operators {
        String CONTAINS = "Contains"
        String IS_EMPTY = "Is Empty"
        String IS_NOT_EMPTY = "Is Not Empty"
        String IS_EMPTY_COUNTS = "Is Empty Counts"
        String IS_NOT_EMPTY_COUNTS = "Is Not Empty Counts"
        String DOES_NOT_CONTAIN = "Does Not Contain"
        String END_WITH = "Ends With"
        String DOES_NOT_END_WITH = "Does Not End With"
        String DOES_NOT_START_WITH = "Does Not Start With"
        String LESS_THAN_OR_EQUAL_TO = "Less Than Equal To"
        String GREATER_THAN_OR_EQAUL_TO = "Greater Than Equal To"
        String EQUAL_TO = "Equal To"
        String NOT_EQUAL_TO = "Not Equal To"
        String GREATER_THAN = "Greater Than"
        String CONTENT_MATCH = "Content Match"
        String LESS_THAN = "Less Than"
    }

    interface DMLType {
        String INSERT = "I"
        String DELETE = "D"
        String UPDATE = "U"
    }

    interface Badges {
        String BADGE_TEXT = "badge"
        String FLAGS = "flags"
        String NEW = "New"
        String PREVIOUSLY_REVIEWED = "Previously Reviewed"
        String AUTO_FLAGGED = "Auto Flagged"
        String PENDING_REVIEW = "Due from Previous Period"
        String FLAGGED_PREVIOUSLY_REVIEWED = "Auto Flagged & Previously Reviewed"
    }

    interface RuleType {
        String GLOBAL = 'Global'
        String PRODUCT = 'Product'
    }

    interface Alias {
        String TAGS_ALIAS = 'gt.tagText'
        String NEXT_LINE = "<br>"
        String NEW_LINE = "\n"
    }

    interface AdvancedFilter {
        String SIGNAL = 'signal'
        String AGG_SIGNAL = 'aggSignal'
        String EVDAS_SIGNAL = 'evdasSignal'
        String IN = 'IN'
        String NOT_IN = 'NOT IN'
        String CASE_NUMBER = 'caseNumber'
        String CASE_SERIES = 'caseSeries'
        String PT_FIELD = 'pt'
        String COMMENTS = "comments"
        String COMMENT = "comment"
        String DISPOSITION_ID = "disposition.id"
        String EQUALS = "EQUALS"
        String ASSIGNED_TO_ID = "assignedTo.id"
        String CURRENT_USER_ID = "CURRENT_USER_ID"
        String CURRENT_USER_TEXT = "Current User"
        String ASSIGNED_TO_GROUP_ID = "assignedToGroup.id"
        String CURRENT_GROUP_ID = "CURRENT_GROUP_ID"
        String CURRENT_GROUP_TEXT = "Current User Group"
    }

    interface FilterType {
        String ADVANCED_FILTER = "Filter"
        String VIEW_INSTANCE = "View"
    }

    interface CustomQualitativeFields{
        String APP_TYPE_AND_NUM = "appTypeAndNum"
        String COMPOUNDING_FLAG = "compoundingFlag"
        String SUBMITTER = "submitter"
        String CASE_TYPE = "caseType"
        String COMPLETENESS_SCORE = "completenessScore"
        String MED_ERR_PT_LIST = "medErrorPtList"
        String MED_ERRS_PT = "medErrorsPt"
        String PATIENT_AGE = "patientAge"
        String IND_NUM = "indNumber"
        String PRE_ANDA = "preAnda"
        String PAI_ALL_LIST = "paiAllList"
        String PRIM_SUSP_PAI_LIST = "primSuspPaiList"
        String CROSS_REFERENCE_IND = "crossReferenceInd"
    }

    interface ViewsDataSourceLabels {
        String EVDAS = 'Evdas'
        String FAERS = 'Faers'
        String VAERS = 'Vaers'
        String VIGIBASE = 'Vigibase'
        String JADER = 'Jader'
        String SAFETY = 'safety'
    }

    interface SpotfireFileName {
        String INTERVAL_FILE_PVA = "Current Period Analysis"
        String CUMM_FILE_PVA = "Cumulative Period Analysis"
        String INTERVAL_FILE_FAERS = "Current Period Analysis (FAERS)"
        String CUMM_FILE_FAERS = "Cumulative Period Analysis (FAERS)"
        String INTERVAL_FILE_VIGIBASE = "Current Period Analysis (VIGIBASE)"
        String CUMM_FILE_VIGIBASE = "Cumulative Period Analysis (VIGIBASE)"
        String INTERVAL_FILE_VAERS = "Current Period Analysis (VAERS)"
        String CUMM_FILE_VAERS = "Cumulative Period Analysis (VAERS)"
    }

    interface UserDashboardCounts {
        String USER_DISP_CASE_COUNTS = "userDispCaseCounts"
        String GROUP_DISP_CASE_COUNTS = "groupDispCaseCounts"
        String USER_DUE_DATE_CASE_COUNTS = "userDueDateCaseCounts"
        String GROUP_DUE_DATE_CASE_COUNTS = "groupDueDateCaseCounts"
        String USER_DISP_PECOUNTS = "userDispPECounts"
        String GROUP_DISP_PECOUNTS = "groupDispPECounts"
        String USER_DUE_DATE_PECOUNTS = "userDueDatePECounts"
        String GROUP_DUE_DATE_PECOUNTS = "groupDueDatePECounts"
    }
    interface ExcelConstants {
        int MAX_CELL_LENGTH_XLSX = 32767
        int MAX_CELL_NARRATIVE_LENGTH_XLSX = 32000
        String TRUNCATE_TEXT_XLSX = "...(truncated)"
        int MAX_CELL_CONTENT_LENGTH = 15000
    }


    interface CriteriaSheetLabels {
        String EVDAS_ARTICLE_HISTORY = "Article history"
        String CASE_HISTORY = "Case history"
        String ALERT_NAME = "Alert Name"
        String CRITERIA = "Criteria"
        String DESCRIPTION = "Description"
        String PRODUCT = "Product"
        String MULTI_SUBSTANCE = "Multi-Substance"
        String MULTI_INGREDIENT = "Multi-Ingredient"
        String SEARCH_STRING = "Search String"
        String TITLE = "Title"
        String AUTHORS = "Authors"
        String CURRENT_DISPOSITION = "Current Disposition"
        String ARTICLE_ID = "Article ID"
        String PRODUCT_NAME = "Product Name"
        String EVENT_NAME = "Event Name"
        String CATEGORIES = "Categories"
        String SIGNAL = "Signal"
        String PUBLICATION_DATE = "Publication Date"
        String ARTICLE_ABSTRACT = "Article abstract"
        String DATASHEETS = "Datasheets"
        String SUSPECT_PRODUCT = "Limit to Suspect Product"
        String EVENT_SELECTION = "Event Selection"
        String QUERY_NAME = "Query Name"
        String FOREFROUND_QUERY_NAME = "Foreground Query"
        String QUERY_PARAMETERS = "Query Parameters"
        String FOREFROUND_QUERY_PARAMETERS = "Foreground Parameters"
        String DATE_RANGE_TYPE = "Date Range Type"
        String DATE_RANGE = "Date Range"
        String EVAULATE_ON = "Evaluate On"
        String LIMIT_TO_CASE_SERIES = "Limit to Case Series"
        String EXCLUDE_FOLLOW_UP = "Exclude Follow-Up"
        String AS_OF_DATE = "As of Date"
        String INCLUDE_LOCKED_VERSION = "Include Locked Versions Only"
        String EXCLUDE_NON_VALID_CASE = "Exclude Non-Valid Cases"
        String MISSED_CASE = "Include Cases Missed in the Previous Reporting Period"
        String PRODUCT_AS_DATA_MINING = "Consider selected product as data mining background"
        String FG_DATA_MINING_RUN = "Consider selected filters as data mining foreground"
        String VIEW = "View"
        String FILTER = "Filter"
        String FILTERS = "Filters"
        String FILTER_TEXT = "Filter Text"
        String DISPOSITIONS = "Dispositions"
        String CASE_COUNT = "Case Count"
        String PEC_COUNT = "PEC Count"
        String ARTICLE_COUNT = "Article Count"
        String ROW_COUNT = "Row Count"
        String REPORT_GENERATED_BY = "Report Generated By"
        String EXPORT_TIME = "Export Time"
        String DATE_CREATED = "Execution Date"
        String DATE_EXPORTED = "Exported Date"
        String SOC = "SOC"
        String PT = "PT"
        String DATASOURCE = "Data Source"
        String DRUG_TYPE = "Product Type"
        String GROUP_BY_SMQ = "Data Mining based on SMQ/Event group"
        String STRATIFICATION_PARAMETER_EBGM_FAERS = "Stratification Parameters EBGM (FAERS)"
        String STRATIFICATION_PARAMETER_PRR_FAERS = "Stratification Parameters PRR/ROR/Chi-Square (FAERS)"
        String STRATIFICATION_PARAMETER_EBGM_VAERS = "Stratification Parameters EBGM (VAERS)"
        String STRATIFICATION_PARAMETER_PRR_VAERS = "Stratification Parameters PRR/ROR/Chi-Square (VAERS)"
        String STRATIFICATION_PARAMETER_EBGM_VIGIBASE = "Stratification Parameters EBGM (VigiBase)"
        String STRATIFICATION_PARAMETER_PRR_VIGIBASE = "Stratification Parameters PRR/ROR/Chi-Square (VigiBase)"
        String STRATIFICATION_PARAMETER_EBGM_JADER = "Stratification Parameters EBGM (JADER)"
        String STRATIFICATION_PARAMETER_PRR_JADER = "Stratification Parameters PRR/ROR/Chi-Square (JADER)"
        String SUB_GROUP_PARAMETER = "Sub Groups Parameters (Safety DB)"
        String STRATIFICATION_PARAMETER_FAERS = "Stratification Parameters (FAERS)"
        String SUB_GROUP_PARAMETER_FAERS = "Sub Groups Parameters (FAERS)"
        String SORT_ORDER = "Sort Order"
        String COLUMN_LEVEL_FILTER = "Column Level Filter"
        String FAERS_DATE_RANGE = "Faers Date Range"
        String EVDAS_DATE_RANGE = "Evdas Date Range"
        String VAERS_DATE_RANGE = "Vaers Date Range"
        String VIGIBASE_DATE_RANGE = "VigiBase Date Range"
        String JADER_DATE_RANGE = "Jader Date Range"
        String SIGNAL_NAME = "Signal Name"
        String EVENTS = "Events"
        String EVENT  = "Event"
        String SIGNAL_SOURCE = "Signal Source"
        String PRIORITY = "Priority"
        String ASSIGNED_TO = "Assigned To"
        String DETECTED_DATE = "Detected Date"
        String CREATED_DATE = "Created Date"
        String MINING_VARIABLE = "Data Mining Variable"
        String MINING_VARIABLE_PARAM = "Data Mining Variable Parameters"
        String STRATIFICATION_PARAMETER_EBGM_SAFETY = "Stratification Parameters EBGM (Safety DB)"
        String STRATIFICATION_PARAMETER_PRR_SAFETY = "Stratification Parameters PRR/ROR/Chi-Square (Safety DB)"
        String CRITERIA_SHEET = "Criteria Sheet"
        String ALERT_LEVEL_NEW_COUNT = "Alert Level New Count"
        String ALERT_LEVEL_CUM_COUNT = "Alert Level Cumulative Count"
        String ALERT_LEVEL_STUDY_COUNT = "Alert Level New Study Count"
        String ALERT_LEVEL_STUDY_CUM_COUNT = "Alert Level Cumulative Study Count"
        String ALERT_LEVEL_NEW_COUNT_FAERS = "Alert Level New Count (F)"
        String ALERT_LEVEL_CUM_COUNT_FAERS = "Alert Level Cumulative Count (F)"
        String ALERT_LEVEL_NEW_COUNT_VAERS = "Alert Level New Count (VA)"
        String ALERT_LEVEL_CUM_COUNT_VAERS = "Alert Level Cumulative Count (VA)"
        String ALERT_LEVEL_NEW_COUNT_VIGIBASE = "Alert Level New Count (VB)"
        String ALERT_LEVEL_CUM_COUNT_VIGIBASE = "Alert Level Cumulative Count (VB)"
        String ALERT_LEVEL_NEW_COUNT_LABEL = "Alert Level New Case Count"
        String ALERT_LEVEL_CUM_COUNT_LABEL = "Alert Level Cumulative Case Count"
        String ALERT_LEVEL_STUDY_COUNT_LABEL = "Alert Level New Study Case Count"
        String ALERT_LEVEL_STUDY_CUM_COUNT_LABEL = "Alert Level Cumulative Study Case Count"
        String ALERT_LEVEL_NEW_COUNT_FAERS_LABEL = "Alert Level New Case Count (F)"
        String ALERT_LEVEL_CUM_COUNT_FAERS_LABEL = "Alert Level Cumulative Case Count (F)"
        String ALERT_LEVEL_NEW_COUNT_VAERS_LABEL = "Alert Level New Case Count (VA)"
        String ALERT_LEVEL_CUM_COUNT_VAERS_LABEL = "Alert Level Cumulative Case Count (VA)"
        String ALERT_LEVEL_NEW_COUNT_VIGIBASE_LABEL = "Alert Level New Case Count (VB)"
        String ALERT_LEVEL_CUM_COUNT_VIGIBASE_LABEL = "Alert Level Cumulative Case Count (VB)"
        String ALERT_LEVEL_NEW_COUNT_JADER = "Alert Level New Case Count"
        String ALERT_LEVEL_CUM_COUNT_JADER = "Alert Level Cumulative Case Count"
        String REVIEW_SCREEN_FOR_CURRENT_PRODUCT_EVENT_FILTER = "Review History for Current Product-Event Filter"
        String REVIEW_HISTORY_FOR_OTHER_ALERT_FILTER = "Review History From Other Alerts Filter"
        String REVIEW_HISTORY_FOR_CURRENT_PRODUCT_FILTER = "Review History for Current Product Filter"
        String REVIEW_HISTORY_FOR_CURRENT_ARTICLE_FILTER = "Review History for Current Article Filter"
        String IMPORT_SHEET_NOTE = "The Criteria Sheet should be removed if the Excel is being used for importing purposes."
        String INCLUDE_WHO_DRUGS = "Include WHO Drugs"
        String DRUG_RECORD_NUMBER = "Drug Record Number"
        String INCLUDE_STUDY_CASES = "Include All Study Drugs Cases"
        String CONSIDER_ONLY_POI = "Consider only POI for Completed/Unblinded Studies"
        String IGNORE_STUDY_MEDICATION = "Ignore Study Medication Type"
    }

    interface WorkFlowLog{
        String DUE_DATE="Due Date"
        String DATE_CLOSED="Date Closed"
        String VALIDATION_DATE="Validation Date"
        String ASSESSMENT_COMPLETION_DATE="Assessment Completion Date"
        String ASSESSMENT_DATE="Assessment Date"
        String VALIDATION_STATUS_COMMENT = "Signal Validation Date added based on disposition transition"
    }

    interface DrugType{
        String VACCINE = "VACCINE"
    }

    interface CustomRptFields {
        String CSI_SPONSOR_STUDY_NUMBER = 'crepoud_text_11'
        String STUDY_CLASSIFICATION_ID = 'crepoud_text_12'
        String FDA_CSI_SPONSOR_STUDY_NUMBER = 'vwcsiSponsorStudyNumber'
        String FDA_STUDY_CLASSIFICATION_ID = 'vwstudyClassificationId'
    }

    interface pvaDataSources{
        String ARGUS = "ARGUS"
        String ARIS = "ARIS"
        String PVCM = "PVCM"
    }



    interface DatabaseRegex {
        String SINGLE_QUOTE_REGEX = "(?i)'"
        String FOUR_SINGLE_QUOTES = "''''"
    }
    interface AlertDomainCommonField {
        String LAST_DISPOSITION_CHANGE = "dispLastChange"
    }

    interface ActivityExportRegex {
        String OPEN_SQUARE_BRACKET = '['
        String CLOSED_SQUARE_BRACKET = ']'
        String OPEN_CURLY_BRACKET = ' ('
        String CLOSED_CURLY_BRACKET = ')'
    }

    interface DatasheetOptions {
        String ALL_SHEET = 'ALL_SHEET'
        String CORE_SHEET = 'CORE_SHEET'
        String SEPARATOR = '__'
        String ID_SEPARATOR = '_'
    }
    interface ChangeDetailsUndo {
        String UNDO_DISPOSITION_CHANGE = "Undo Action Performed"
    }

    interface CountsLabel {
        String CUMM_LABEL = 'CUMM'
        String CUM_LABEL = 'CUM'
    }

    interface AlertUtils {
        String IS_READY_FOR_EXECUTION = "isReadyForExecution"
        String ALERT_DISABLE_REASON = "alertDisableReason"
        String LAST_ETL_STATUS = "latestEtlStatus"
        String LAST_ETL_STATUS_LOG_ID = "latestEtlStatusLogId"
        String LATEST_SUCCESS_ETL_START_DATE = "latestSuccessEtlStartDate"
        String LATEST_SUCCESS_ETL_LOG_ID = "latestSuccessEtlLogId"
    }

    interface CommonUtils {
        String SUCCESS = "SUCCESS"
        String PRE_REQUISITE_FAIL = "PRE_REQUISITE_FAIL"
    }

    interface AlertDisableReason {
        String PVR_INACCESSIBLE = "PVR INACCESSIBLE"
        String ETL_AS_OF_VERSION_FAILURE = "ETL AS OF VERSION CHECK ENABLED"
        String LATEST_VERSION_ETL_CHECKS_ENABLED = "LATEST VERSION ETL CHECK ENABLED"
        String ETL_FAILURE_CHECK_ENABLED = "ETL FAILURE CHECK ENABLED"
        String ETL_IN_PROGRESS_CHECK_ENABLED = "ETL IN PROGRESS CHECK ENABLED"
    }

    interface AlertStatus {
        String SCHEDULED = "Scheduled"
        String UNSCHEDULED = "Unscheduled"
        String IN_PROGRESS = "In Progress"
        String COMPLETED = "Completed"
        String AUTO_DISABLED = "Auto Disabled"
        String USER_DISABLED = "User Disabled"
        String DELETION_IN_PROGRESS = "Deletion In Progress"
        List<String> BASE_CONFIGURATION_PROPERTIES = ["name", "owner", "scheduleDateJSON", "description", "isPublic", "isDeleted", "isEnabled", "adhocRun", "dateRangeType", "productSelection", "eventSelection", "studySelection", "configSelectedTimeZone", "productGroupSelection", "eventGroupSelection", "asOfVersionDate", "evaluateDateAs", "excludeFollowUp", "includeLockedVersion", "adjustPerScheduleFrequency", "excludeNonValidCases", "groupBySmq", "limitPrimaryPath", "includeMedicallyConfirmedCases", "missedCases", "dateCreated", "lastUpdated", "createdBy", "modifiedBy", "numOfExecutions", "totalExecutionTime", "blankValuesJSON", "type", "selectedDatasource",
                                                      "configurationService", "assignedTo", "assignedToGroup", "workflowGroup", "isAutoTrigger", "repeatExecution", "drugClassification", "referenceNumber", "productDictionarySelection", "aggExecutionId", "aggAlertId", "aggCountType", "isCaseSeries", "alertQueryName", "alertForegroundQueryName", "alertTriggerCases", "alertTriggerDays", "alertRmpRemsRef", "onOrAfterDate", "spotfireSettings", "applyAlertStopList", "suspectProduct", "dataObjectService", "alertCaseSeriesId", "alertCaseSeriesName", "dataMiningVariable", "dataMiningVariableValue", "isProductMining", "foregroundSearch", "foregroundSearchAttr"]
    }

    interface EtlStatus {
        String SUCCESS = "SUCCESS"
        String FAILED = "ERROR"
        String IN_PROGRESS = "START"
    }

    interface TimeUnits {
        String MilliSeconds = "MILLISECONDS"
        String Hours = "HOURS"
        String Minutes = "MINUTES"
        String Seconds = "SECONDS"
    }
    interface SignalAutomation {
        String SMOKE_TESTING = 'SmokeTestingPVSignalDev'
        String DIRECTION_ASC = 'asc'
        String SORT = 'runDate'
        String FILE_NAME = "ImportFile.xlsx"

    }
    interface AlertProgress {
        String SINGLE_CASE_ALERT = "Single Case Alert"
        String AGGREGATE_CASE_ALERT = "Aggregate Case Alert"
        String BUSINESS_RULES = "BR"
        String PERSIST = "PERSIST"
        String ARCHIEVE = "ARCHIEVE"
        String COMMA = ","
        Integer TEN = 10
        Integer FIFTY = 50
        Integer HUNDRED = 100

    }

    interface SystemPrecheck {
        String PVR = 'PVR'
        String PVCC = 'PVCC'
        String PVA = "PVA"
        String PVS = "PVS"
        String URL_LITERATURE = "URL_LITERATURE"
        String SAFETY = "SAFETY"
        String VIGIBASE = "VIGIBASE"
        String JADER = "JADER"
        String FAERS = "FAERS"
        String EVDAS = "EVDAS"
        String EUDRA = "EUDRA"
        String VAERS = "VAERS"
        String SPOTFIRE = "SPOTFIRE"
        String SPOTFIRE_CONNECTION = "SPOTFIRE_CONNECTION"
        String EBGM = "EBGM"
        String DSS = "DSS"
        String DATABASE = "DATABASE"
        String FOLDER = "FOLDER"
        String IMPORT_CONFIG_FOLDER = "IMPORT_CONFIG_FOLDER"
        String IMPORT_PRODUCT_ASSIGNEMENT_FOLDER = "IMPORT_PRODUCT_ASSIGNEMENT_FOLDER"
        String SIGNAL_MANAGEMENT_FOLDER = "SIGNAL_MANAGEMENT_FOLDER"
        String ERMR_FOLDER = "ERMR_FOLDER"
        String CASE_LINE_LISTING_FOLDER = "CASE_LINE_LISTING_FOLDER"
        String EVDAS_FOLDER = "EVDAS_FOLDER"
        String SPOTFIRE_KEYS = "SPOTFIRE_KEYS"
        String RAM = "RAM"
    }

    interface ProductAssignment {
        Integer batchSize =1000
    }

    interface NonSeriousColumn {
        String New_Non_Ser = "newNonSerious"
        String Cum_Non_Ser = "cumNonSerious"
    }

    interface MeetingUpdate {
        String MEETING_TITLE = "meetingTitle"
        String MEETING_OWNER = "meetingOwner"
        String DURATION = "duration"
        String MEETING_AGENDA = "meetingAgenda"
        String MEETING_DATE = "meetingDate"
        String ATTENDEES = "attendees"
        String GUEST_ATTENDEE = "guestAttendee"
    }
    interface FilterOptions {
        String OWNER = "Mine (I'm the owner)"
        String ASSIGNED_TO_ME = "Assigned To Me"
        String SHARED_WITH_ME = "Shared With Me"
        String ALL_SIGNALS = "All Signals"
    }
    interface FilterHeadings {
        String USER_GROUP = "Assigned to User Group"
        String USER = "Assigned to User"
    }

    interface publicTokens {
        String PVS_TOKEN = "PVS_PUBLIC_TOKEN"
    }

    interface ConfigManagement {
        String BUSINESS_CONFIG_EXCEL = "BUSINESS_CONFIG.xlsx"
    }

    interface importConfigSheet {
        String MULTI_INGREDIENT_COLUMN = "multi-ingredient"
        String INCLUDE_WHO_COLUMN = "include who drugs"
        String UNSCHEDULE_LOG = "Unschedule"
        Map BASE_COLUMN_MAP = ['alert name': 'String',
                              'alert type': 'String',
                              'owner': 'String',
                              'master configuration': 'String',
                              'configuration template': 'String',
                              'products': 'String',
                              'include who drugs': 'String',
                              'multi-ingredient': 'String',
                              'datasheets':'String',
                              'date range type': 'String',
                              'x': 'Number',
                              'start date': 'String',
                              'end date': 'String',
                              'version as of date': 'String',
                              'assigned to': 'String',
                              'share with': 'String',
                              'scheduler': 'String']

        Map MASTER_COLUMN_MAP = ['master configuration': 'String',
                                 'configuration template': 'String',
                                 'products hierarchy': 'String',
                                 'include who drugs': 'String',
                                 'multi-ingredient': 'String',
                                 'date range type': 'String',
                                 'x': 'Number',
                                 'start date': 'String',
                                 'end date': 'String',
                                 'version as of date': 'String',
                                 'scheduler': 'String']

        String IMPORT_VALUE_ONE = "1"
        String IMPORT_VALUE_EMPTY = ""
        String PRODUCT_HIERARCHY_COLUMN = "products hierarchy"
        String PRODUDT_TYPE_INGREDIENT = "Ingredient"
        String PRODUDT_TYPE_SUBSTANCE = "Substance"
    }

    interface fieldManagementCustom { // Custom handling to keep in sync with PVCM
        interface TXN_SUCCESS {
            static final Integer resultCode = 01
            static final String resultStatus = "TXN_SUCCESS"
            static final String resultMsg = "Txn Success"
        }
    }

    interface Action {
        String ACTION_CREATION = "Action Creation"
        String EMPTY = ""
        String ACTION_ITEM_CREATED_MESSAGE = "New Action Item Created"
        String ACTION_ITEM_ASSIGNED_MESSAGE = "New Action item assigned with below information"
    }

    interface ImportConfiguration {
        String ALERT_NAME = "alert name"
        String NAME = "Name"
    }

    interface MigrationStatus {
        String NO_BR_EXIST = "Rule Information not found for entity id"
    }

    interface BlindingStatus{
        String REDACTED = 'Redacted'
        String BLINDED = 'Blinded'
        String AVAILABLE = 'Available'
    }

    interface ICRFields{
        String CURRENT_RUN = 'CurrentRun'
        String INITIAL_FU = 'initialFu'
        String TAGS = 'tags'
        String TAG_TEXT = 'tag_text'
        String SUB_TAG_TEXT = 'sub_tag_text'
        String PRODUCT_ID = 'productId'
        String SUSP_PROD = 'suspProd'
        String CON_COMIT = 'conComit'
        String DUE_DATE = 'dueDate'
        String DISP_LAST_CHANGE = 'dispLastChange'
        String DISPOSITION = 'disposition'
        String ASSIGNED_TO = 'assignedTo'
        String CASE_INIT_RECIEPT_DATE = 'caseInitReceiptDate'
        String ACCESS_LISTEDNESS = 'assessListedness'
        String LISTEDNESS = 'listedness'
        String ACCESS_SERIOUSNESS = 'assessSeriousness'
        String SERIOUS = 'serious'
        String ASSIGNED = 'assigned'
        String ASSIGNED_TO_USER = 'assignedToUser'
        String ASSIGNED_TO_USER_GROUP = 'assignedToUserAndGroup'
        String SIGNAL_TOPICS = 'signalsAndTopics'
        String NEW_DASHBOARD_FILTER = 'newDashboardFilter'
        String TIME_TO_ONSET = 'timeToOnset'
        String COMPLETENESS_SCORE = 'completenessScore'
        String PATIENT_AGE = 'patientAge'
        String ALERT_TAGS = 'alertTags'
        String TOTAL_COUNT = 'total_count'
        String ID = 'id'
        String VERSION = 'version'
        String PRODUCT_NAME = 'productName'
        String REPORTED_PRODUCT_NAME = 'reported_product_name'
        String PT = 'pt'
        String DETECTED_DATE = 'detectedDate'
        String SINGLE_CASE_ALERT = 'SINGLE_CASE_ALERT'
        String ARCHIVED_SINGLE_CASE_ALERT = 'ARCHIVED_SINGLE_CASE_ALERT'
        String SINGLE_ON_DEMAND_ALERT = 'SINGLE_ON_DEMAND_ALERT'
        String AND = 'AND'
        String OR = 'OR'
    }

    interface viewMapping {
        // this column view index is according to container view constructed in view instance
        Map VIEW_INFO = [1: ["type": "selected", "title": "Selected Fields"], 2: ["type": "safety", "title": "Safety Optional Fields"], 3: ["type": "faers", "title": "FAERS Optional Fields"], 4: ["type": "evdas", "title": "EVDAS Optional Fields"], 5: ["type": "vaers", "title": "VAERS Optional Fields"], 6: ["type": "vigibase", "title": "VigiBase Optional Fields"]]
        Map JADER_VIEW_INFO = [1: ["type": "selected", "title": "Selected Fields"], 3: ["type": "safety", "title": "Optional Fields"]]

    }

    interface PVRApiPath{
        String UPDATE_BLINDED_DATA = "/reports/public/api/fetchBlindedUsergroups"
    }
    //API RESPONSE CONSTANTS
    static final String RESULT_CODE = "resultCode"
    static final String RESULT_STATUS = "resultStatus"
    static final String RESULT_MESSAGE = "resultMsg"
    static final String RESULT_DATA = "data"
    static final String RESULT_TITLE = "resultTitle"
    static final Map TXN_SUCCESS = ["resultCode": 200, "resultStatus": "TXN_SUCCESS", "resultMsg": "Txn Success"]
    static final Map NO_RECORD_FOUND = ["resultCode": 404, "resultStatus": "NO_RECORD_FOUND", "resultMsg": "No Record Found"]
    static final Map REDIRECT = ["resultCode": 302, "resultStatus": "REDIRECT", "resultMsg": "Redirect"]
    static final Map TXN_FAILURE = ["resultCode": 401, "resultStatus": "TXN_FAILURE", "resultMsg": "Your request has been declined by server. Please connect to Administrator"]
    static final Map ACCESS_DENIED = ["resultCode": 601, "resultStatus": "ACCESS_DENIED", "resultMsg": "Either you don't have the permission to access this case or this caseId doesn't exists"]
    static final Map INVALID_PARAMETERS = ["resultCode": 400, "resultStatus": "INVALID_PARAMETERS", "resultMsg": "Invalid or insufficient parameters in the request"]
    static final Map FORBIDDEN = ["resultCode": 403, "resultStatus": "TXN_FORBIDDEN", "resultTitle": "Permission Denied", "resultMsg": "You are not authorized to perform this action."]
    static final Map INTERNAL_SERVER_ERROR = ["resultCode": 500, "resultStatus": "INTERNAL SERVER ERROR", "resultTitle": "Your request has been failed. Please connect to Administrator"]

    interface AdhocAlert {
        String GROUPED_CONFIGURATION = "groupedConfiguration"
        String SECTION_LABEL_MAP = "sectionLabelMap"
        String DROPDOWN_VALUES_BY_KEYS = "dropDownValuesByKeys"
        List<String> signalAdhocReviewObservationLabelList = ['name', 'productSelection', 'eventSelection', 'detectedBy', 'initialDataSource', 'disposition']
        List<String> alwaysMandatoryFields = ['assignedToValue', 'priority', 'detectedDate', 'name', 'assignedTo']
    }

}
