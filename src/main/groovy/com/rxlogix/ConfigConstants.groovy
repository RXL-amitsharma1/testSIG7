package com.rxlogix

interface ConfigConstants {

    interface SpecialCharacterMigration {
        List<Map> tableMap = [
                ["tableName": 'JUSTIFICATION', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'DISPOSITION', "columns": ['DISPLAY_NAME', 'VALUE'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'DISPOSITION', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'COMMENT_TEMPLATE', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'BUSINESS_CONFIGURATION', "columns": ['RULE_NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'BUSINESS_CONFIGURATION', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'RCONFIG', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'EVDAS_CONFIG', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'LITERATURE_CONFIG', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'DISPOSITION_RULES', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'GROUPS', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'GROUPS', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'PRIORITY', "columns": ['VALUE', 'DISPLAY_NAME', 'ICON_CLASS'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'PRIORITY', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'REPORT_HISTORY', "columns": ['REPORT_NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'VALIDATED_SIGNAL', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'VALIDATED_SIGNAL', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'WORK_FLOW_RULES', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'WORK_FLOW_RULES', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'ACTION_TEMPLATE', "columns": ['NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'ACTION_TEMPLATE', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'ACTION_TYPES', "columns": ['VALUE', 'DISPLAY_NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'ACTION_TYPES', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS],
                ["tableName": 'ACTION_CONFIGURATIONS', "columns": ['VALUE', 'DISPLAY_NAME'], "characters": com.rxlogix.Constants.SpecialCharacters.DEFAULT_CHARS],
                ["tableName": 'ACTION_CONFIGURATIONS', "columns": ['DESCRIPTION'], "characters": com.rxlogix.Constants.SpecialCharacters.TEXTAREA_CHARS]
        ]
    }

}
