package com.rxlogix

import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole

class UserRoleService {

    Map getUpdatedRecordMap(List initialRecords, List finalRecords) {
        List commonRecords = initialRecords.intersect(finalRecords)
        Role devRole = Role.findByAuthority("ROLE_DEV")
        List<Role> deletedRecords = (initialRecords - commonRecords) - devRole
        ["deletedRecords": deletedRecords, "addedRecords": (finalRecords - commonRecords)]
    }

    List getSelectedRolesList(params, List selectedRolesList) {
        for (String key in params.keySet()) {
            if (('on' == params.get(key))) {
                selectedRolesList.add(key)
            }
        }
        return selectedRolesList
    }

    void changeUserRoles(User userInstance, def params) {
        List<Role> selectedRoles = getSelectedRolesList(params, []) ? Role.findAllByAuthorityInList(getSelectedRolesList(params, [])) : []
        List<Role> roles = UserRole.findAllByUser(userInstance)?.role
        List<Role> deletedRoles
        List<Role> addedRoles
        Map result = getUpdatedRecordMap(roles, selectedRoles)
        deletedRoles = result.deletedRecords
        addedRoles = result.addedRecords
        if (deletedRoles.size() > 0) {
            List<UserRole> mapping = UserRole.findAllByUserAndRoleInList(userInstance, deletedRoles)
            mapping.each { UserRole userRole ->
                userRole.delete(flush: true)
            }
        }
        if (addedRoles?.size() > 0) {
            addedRoles?.each { Role role ->
                UserRole.create userInstance, role, true
            }
        }
        userInstance.userRolesString=UserRole.findAllByUser(userInstance)?.role.toString()
        userInstance.save(flush:true)

    }

    void updateRoleAuthorityDisplay() {
        List<Role> roleList = Role.findAll();
        try {
            List<Role> updatedRoleList = [ ]
            for( int i = 0; i < roleList.size(); i++ ) {
                roleList.get( i ).authorityDisplay = roleList.get( i ).toString()
                updatedRoleList.add( roleList.get( i ) )
            }
            Role.saveAll( updatedRoleList )
        } catch( Exception e ) {
            e.printStackTrace()
        } finally {
        }
    }

}
