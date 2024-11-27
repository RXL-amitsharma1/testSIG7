import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.PriorityDispositionConfig
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.util.Holders
import org.joda.time.DateTime

databaseChangeLog = {

    changeSet(author: "ujjwal (generated)", id: "1608626578695-85") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_EVDAS_CONFIG')
        }
        grailsChange {
            change {
                ctx.dispositionService.updateReviewCountsForAllExecutedConfigurations(sql, "EX_EVDAS_CONFIG")
                confirm "Successfully Updated RequireReviewCount values in EX_EVDAS_CONFIG Table."
            }
        }
    }
    changeSet(author: "ujjwal (generated)", id: "1608626578695-86") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_RCONFIG')
        }
        grailsChange {
            change {
                ctx.dispositionService.updateReviewCountsForAllExecutedConfigurations(sql, "EX_RCONFIG")
                confirm "Successfully Updated RequireReviewCount values in EX_RCONFIG Table."
            }
        }
    }
    changeSet(author: "ujjwal (generated)", id: "1608626578695-87") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_RCONFIG')
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
    changeSet(author: "ujjwal (generated)", id: "1608626578695-88") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_EVDAS_CONFIG')
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
    changeSet(author: "ujjwal (generated)", id: "1608626578695-89") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_LITERATURE_CONFIG')
        }
        grailsChange {
            change {
                ctx.dispositionService.updateLiteratureConfigurations(sql)
                confirm "Successfully Updated RequireReviewCount values in EX_LITERATURE_CONFIG Table."
            }
        }
    }
    changeSet(author: "ujjwal (generated)", id: "1608626578695-90") {
        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'EX_LITERATURE_CONFIG')
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
    changeSet(author: "ujjwal (generated)", id: "1608626578695-91") {

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
    changeSet(author: "amrendra (generated)", id: "1608626578695-92") {
        preConditions(onFail: 'MARK_RAN') {
            columnExists(tableName: 'ALERT_COMMENT', columnName: 'data_source')
        }
        grailsChange {
            change {
                try {
                    sql.execute('''UPDATE alert_comment ac
                    SET (data_source, alert_name) = (SELECT config.selected_data_source, config.name
                         FROM rconfig config
                        WHERE ac.config_id = config.id 
                        )
                     WHERE EXISTS (
                        SELECT 1
                          FROM rconfig config
                         WHERE ac.config_id = config.id
                        and ac.alert_type = 'Single Case Alert'
                        and (ac.sync_flag is null or ac.sync_flag = 0)
                        )  ''')

                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating alert_comment table. #############")
                }

            }
        }
    }
    changeSet(author: "Nitesh (generated)", id: "1608626578695-93") {

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ROLE')
        }
        grailsChange {
            change{
                try {
                    Role aggConfigurationRole = Role.findByAuthority("ROLE_AGGREGATE_CASE_CRUD")
                    if(aggConfigurationRole && Holders.config.signal.faers.enabled){
                        Role faersRole = Role.findByAuthority("ROLE_FAERS_CONFIGURATION")
                        if(!faersRole){
                            faersRole = new Role(authority: "ROLE_FAERS_CONFIGURATION", description: "Aggregate Alerts Configuration for FAERS data source",
                                    createdBy: "Application", modifiedBy: "Application")
                            faersRole.save(flush: true, failOnError: true)
                        }
                        List<UserRole> userRoleList = UserRole.findAllByRole(aggConfigurationRole)
                        userRoleList.each{ UserRole userRole ->
                            if(!UserRole.findByUserAndRole(userRole.user, faersRole)) {
                                UserRole newUserRole = new UserRole(user: userRole.user, role: faersRole)
                                newUserRole.save(flush: true, failOnError: true)
                            }
                        }
                    }
                    Map<String,String> rolesToMigrate = [
                            "ROLE_SINGLE_CASE_CRUD"    : ["ROLE_SINGLE_CASE_CONFIGURATION", "Perform Individual Case  configuration and review activities."],
                            "ROLE_AGGREGATE_CASE_CRUD" : ["ROLE_AGGREGATE_CASE_CONFIGURATION", "Perform Aggregate alerts configuration and review"],
                            "ROLE_LITERATURE_CASE_CRUD": ["ROLE_LITERATURE_CASE_CONFIGURATION", "Perform Literature Alerts Configuration"],
                            "ROLE_EVDAS_CASE_CRUD"     : ["ROLE_EVDAS_CASE_CONFIGURATION", "Perform EVDAS Alerts configuration and review"],
                            "ROLE_SIGNAL_MANAGEMENT"   : ["ROLE_SIGNAL_MANAGEMENT_CONFIGURATION", "Create signals from Signal Management"]
                    ]
                    rolesToMigrate.keySet().each { authority ->
                        Role role = Role.findByAuthority(authority)
                        Role newRole = Role.findByAuthority(rolesToMigrate.get(authority).get(0))
                        if(newRole && role){
                            List<UserRole> newUserRoles= UserRole.findAllByRole(newRole)
                            newUserRoles.each{ UserRole userRole ->
                                userRole.delete(flush: true, failOnError: true)
                            }
                            newRole.delete(flush: true, failOnError: true)
                        }
                        if(role) {
                            role.authority = rolesToMigrate.get(authority).get(0)
                            role.description = rolesToMigrate.get(authority).get(1)
                            role.modifiedBy = "Application"
                            role.save(flush: true, failOnError: true)
                        }
                    }

                    // To migrate alert editor role
                    Role alertEditorRole = Role.findByAuthority("ROLE_ALL_ALERT_CRUD")
                    if(alertEditorRole){
                        List alertEditorToNewRoleList = ["ROLE_FAERS_CONFIGURATION", "ROLE_SINGLE_CASE_CONFIGURATION", "ROLE_AGGREGATE_CASE_CONFIGURATION",
                                                         "ROLE_LITERATURE_CASE_CONFIGURATION", "ROLE_EVDAS_CASE_CONFIGURATION"]
                        List<UserRole> alertEditorUserRoleList = UserRole.findAllByRole(alertEditorRole)
                        alertEditorToNewRoleList.each{String authority->
                            Role newRole = Role.findByAuthority(authority)
                            if(newRole) {
                                alertEditorUserRoleList.each { UserRole userRole ->
                                    if (!UserRole.findByUserAndRole(userRole.user, newRole)) {
                                        UserRole newUserRole = new UserRole(user: userRole.user, role: newRole)
                                        newUserRole.save(flush: true, failOnError: true)
                                    }
                                    userRole.delete(flush: true, failOnError: true)
                                }
                            }
                        }
                        alertEditorRole.delete(flush: true, failOnError: true)
                    }

                } catch (Exception ex){
                    println ("Some error occured while updating roles")
                    ex.printStackTrace()
                }
            }
        }
    }
    changeSet(author: "Nitesh (generated)", id: "1608626578695-94") {

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ROLE')
        }
        grailsChange {
            change{
                try{
                    List<User> usersList = User.list()
                    Role reportingRole = Role.findByAuthority("ROLE_REPORTING")
                    if(!reportingRole){
                        reportingRole = new Role(authority: "ROLE_REPORTING", description: "To enable the Reporting option under Analysis section.",
                                createdBy: "Application", modifiedBy: "Application")
                        reportingRole.save(flush :true, failOnError: true)
                    }
                    if(reportingRole) {
                        usersList.each { User user ->
                            if(!UserRole.findByUserAndRole(user, reportingRole)) {
                                UserRole userRole = new UserRole(user: user, role: reportingRole)
                                userRole.save(flush: true, failOnError: true)
                            }
                        }
                    }
                } catch (Exception ex){
                    println ("Some error occured while creating user roles")
                    ex.printStackTrace()
                }
            }
        }
    }
    changeSet(author: "Nitesh (generated)", id: "1608626578695-95") {

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ROLE')
        }
        grailsChange {
            change{
                try{
                    List<User> usersList = User.list()
                    Role categoryRole = Role.findByAuthority("ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT")
                    if(!categoryRole){
                        categoryRole = new Role(authority: "ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT", description: "Add, Update, delete  categories/Subcategories  from alert detail sections",
                                createdBy: "Application", modifiedBy: "Application")
                        categoryRole.save(flush :true, failOnError: true)
                    }
                    if(categoryRole) {
                        usersList.each { User user ->
                            if(!UserRole.findByUserAndRole(user, categoryRole)) {
                                UserRole userRole = new UserRole(user: user, role: categoryRole)
                                userRole.save(flush: true, failOnError: true)
                            }
                        }
                    }
                } catch (Exception ex){
                    println ("Some error occured while creating user roles")
                    ex.printStackTrace()
                }
            }
        }
    }
    changeSet(author: "Nitesh (generated)", id: "1608626578695-96") {

        preConditions(onFail: 'MARK_RAN') {
            tableExists(tableName: 'ROLE')
        }
        grailsChange {
            change{
                try {
                    // To migrate alert editor role
                    Role alertEditorRole = Role.findByAuthority("ROLE_ALL_ALERT_CRUD")
                    if(alertEditorRole){
                        List alertEditorToNewRoleList = ["ROLE_FAERS_CONFIGURATION", "ROLE_SINGLE_CASE_CONFIGURATION", "ROLE_AGGREGATE_CASE_CONFIGURATION",
                                                         "ROLE_LITERATURE_CASE_CONFIGURATION", "ROLE_EVDAS_CASE_CONFIGURATION"]
                        List<UserRole> alertEditorUserRoleList = UserRole.findAllByRole(alertEditorRole)
                        alertEditorToNewRoleList.each{String authority->
                            Role newRole = Role.findByAuthority(authority)
                            if(newRole) {
                                alertEditorUserRoleList.each { UserRole userRole ->
                                    if (!UserRole.findByUserAndRole(userRole.user, newRole)) {
                                        UserRole newUserRole = new UserRole(user: userRole.user, role: newRole)
                                        newUserRole.save(flush: true, failOnError: true)
                                    }
                                    userRole.delete(flush: true, failOnError: true)
                                }
                            }
                        }
                        alertEditorRole.delete(flush: true, failOnError: true)
                    }

                } catch (Exception ex){
                    println ("Some error occured while updating roles")
                    ex.printStackTrace()
                }
            }
        }
    }
    changeSet(author: "rishabh (generated)", id: "1608626578695-97") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(1) from ex_rconfig where is_case_series = 1")
            }
        }
        grailsChange {
            change {
                try {
                    ctx.getBean("alertService").updateIsTempCaseSeries(true,true,null,null)
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating EX_rconfig. #############")
                }
            }
        }

    }
    changeSet(author: "rishabh (generated)", id: "1608626578695-98") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                sqlCheck(expectedResult: '0', "select count(1) from ex_rconfig")
            }
        }
        grailsChange {
            change {
                try {
                    ctx.getBean("alertService").updateIsTempCaseSeries(false,false,null,null)
                } catch (Exception ex) {
                    println(ex)
                    println("##################### Error occurred while updating EX_rconfig. #############")
                }
            }
        }
    }
}