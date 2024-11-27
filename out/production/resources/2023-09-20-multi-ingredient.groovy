import groovy.sql.Sql
databaseChangeLog = {
    changeSet(author: "bhupender (generated)", id: "202321071307-01") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EMERGING_ISSUE', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "EMERGING_ISSUE") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValueBoolean: "false") {
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "bhupender (generated)", id: "202321071307-02") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'BUSINESS_CONFIGURATION', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "BUSINESS_CONFIGURATION") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValueBoolean: "false"){
                constraints(nullable: "true")
            }
        }
    }

    changeSet(author: "Krishna Joshi(generated)", id: "16877832255216-0031") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'RULE_INFORMATION')
        }
        grailsChange {
            change {
                ctx.alertFieldService.updateBusinessRuleFields()
            }
        }
    }

    changeSet(author: "bhupender (generated)", id: "202321071307-03") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'VALIDATED_SIGNAL', columnName: 'IS_MULTI_INGREDIENT')
            }
        }
        addColumn(tableName: "VALIDATED_SIGNAL") {
            column(name: "IS_MULTI_INGREDIENT", type: "number(1,0)", defaultValueBoolean: "false"){
                constraints(nullable: "true")
            }
        }
    }
    changeSet(author: "bhupender (generated)", id: "20232107307-04") {
        grailsChange{
            change{
                Sql sql = null
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    List<Map> updatedConfigMap = []
                    List<Map> configMap = []
                    sql.eachRow("select id,IS_MULTI_INGREDIENT from RCONFIG") { row ->
                        configMap.add(id: row[0], isMultiIngredient: row[1])
                    }
                    println "original map for rconfig" + configMap
                    configMap?.each { config ->
                        if(config.isMultiIngredient == null){
                            config.isMultiIngredient = false
                        }
                        updatedConfigMap.add(id: config.id, isMultiIngredient: config.isMultiIngredient)
                    }
                    println "updated map for rconfig" + updatedConfigMap
                    sql.withBatch(100, "UPDATE RCONFIG SET IS_MULTI_INGREDIENT = :isMultiIngredient WHERE ID = :id", { preparedStatement ->
                        updatedConfigMap?.each { config ->
                            preparedStatement.addBatch(id: config.id, isMultiIngredient: config.isMultiIngredient)
                        }
                    })
                } catch(Exception ex){
                    println "#### Error while updating multi ingredient information in rconfig"
                    println (ex.getMessage())
                }finally{
                    sql?.close()
                }
            }
        }
    }

    changeSet(author: "bhupender (generated)", id: "20232107307-05") {
        grailsChange{
            change{
                Sql sql = null
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    List<Map> updatedConfigMap = []
                    List<Map> configMap = []
                    sql.eachRow("select id,IS_MULTI_INGREDIENT from EX_RCONFIG where is_multi_ingredient = null ") { row ->
                        configMap.add(id: row[0], isMultiIngredient: row[1])
                    }
                    println "original map for ex_rconfig" + configMap
                    configMap?.each { config ->
                        if(config.isMultiIngredient == null){
                            config.isMultiIngredient = false
                        }
                        updatedConfigMap.add(id: config.id, isMultiIngredient: config.isMultiIngredient)
                    }
                    println "updated map for ex_rconfig" + updatedConfigMap
                    sql.withBatch(100, "UPDATE EX_RCONFIG SET IS_MULTI_INGREDIENT = :isMultiIngredient WHERE ID = :id", { preparedStatement ->
                        updatedConfigMap?.each { config ->
                            preparedStatement.addBatch(id: config.id, isMultiIngredient: config.isMultiIngredient)
                        }
                    })
                } catch(Exception ex){
                    println "#### Error while updating multi ingredient information in ex_rconfig"
                    println (ex.getMessage())
                }finally{
                    sql?.close()
                }
            }
        }
    }


    changeSet(author: "bhupender (generated)", id: "20232107307-06") {
        grailsChange{
            change{
                Sql sql = null
                try{
                    sql = new Sql(ctx.getBean("dataSource"))
                    List<Map> updatedConfigMap = []
                    List<Map> configMap = []
                    sql.eachRow("select id,IS_MULTI_INGREDIENT from MASTER_CONFIGURATION where is_multi_ingredient = null ") { row ->
                        configMap.add(id: row[0], isMultiIngredient: row[1])
                    }
                    println "original map for masterConfiguration" + configMap
                    configMap?.each { config ->
                        if(config.isMultiIngredient == null){
                            config.isMultiIngredient = false
                        }
                        updatedConfigMap.add(id: config.id, isMultiIngredient: config.isMultiIngredient)
                    }
                    println "updatedConfigMap for master_configuration = " + updatedConfigMap
                    sql.withBatch(100, "UPDATE MASTER_CONFIGURATION SET IS_MULTI_INGREDIENT = :isMultiIngredient WHERE ID = :id", { preparedStatement ->
                        updatedConfigMap?.each { config ->
                            preparedStatement.addBatch(id: config.id, isMultiIngredient: config.isMultiIngredient)
                        }
                    })
                } catch(Exception ex){
                    println "#### Error while updating multi ingredient information in master_configuration"
                    println (ex.getMessage())
                }finally{
                    sql?.close()
                }
            }
        }
    }
}