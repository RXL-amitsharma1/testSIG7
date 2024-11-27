package com.rxlogix

import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class UtilController {

    def utilService
    def userService

    def serverInfo() {
        Map serverInfo = [:]
        try{
            def ip = InetAddress.getLocalHost()
            serverInfo.ipAddress = InetAddress.getLocalHost().toString()
            serverInfo.hostName = ip.getHostName()
        }catch (Exception e){
            log.error("Some error occurred while executing serverInfo action",e)
        }
        render serverInfo as JSON
    }

    def sharedWithList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max

        Set activeUsers = userService.getAllowedUsersForCurrentUser(term)
        Set activeGroups = userService.getAllowedGroupsForCurrentUser(term)
        List userList = activeUsers.unique { it.id }.collect {
            [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: true]
        }
        List groupList = activeGroups.unique { it.id }.collect {
            [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: true]
        }
        List items = []
        items = splitResult(items, offset, max, groupList, userList)
        render(items as JSON)
    }

    private List splitResult(items, offset, max, groupList, userList) {

        String groupLabel = ""
        String userLabel = ""
        def selectedGroupItems = []
        def selectedUserItems = []
        if (offset == 0 && groupList.size() > 0) {
            groupLabel = ViewHelper.getMessage("user.group.label")
            selectedGroupItems = groupList.subList(0, Math.min(offset + max, groupList.size()))
        } else if ((offset > 0) && (offset < groupList.size())) {
            groupLabel = ""
            selectedGroupItems = groupList.subList(offset, Math.min(offset + max, groupList.size()))
        }

        int userOffset = offset - groupList.size()
        int usermax = max - selectedGroupItems.size()
        if ((userOffset + max) > 0) {
            if (userOffset <= 0 && userList.size() > 0) {
                userLabel = ViewHelper.getMessage("user.label")
                selectedUserItems = userList.subList(0, Math.min(0 + usermax, userList.size()))
            } else if ((userOffset > 0) && (userOffset < userList.size())) {
                userLabel = ""
                selectedUserItems = userList.subList(userOffset, Math.min(userOffset + usermax, userList.size()))
            }
        }
        if (selectedGroupItems.size() > 0)
            items << ["text": groupLabel, "children": selectedGroupItems]
        if (selectedUserItems.size() > 0)
            items << ["text": userLabel, "children": selectedUserItems]
        items
    }


}
