import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "Nitesh (generated)", id: "1611139538775-5") {

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

    changeSet(author: "Nitesh (generated)", id: "1611139538774-3") {

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



    changeSet(author: "Nitesh (generated)", id: "1611139538775-2") {

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
    changeSet(author: "Nitesh (generated)", id: "1611139538775-4") {

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
}
