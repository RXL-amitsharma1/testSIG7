package com.rxlogix.dto

class BlindedUserGroupsDTO {

    String userGroupName
    Boolean isBlinded = null
    Boolean isDeleted = null
    List<String> fieldIds
    List<String> blindedFieldIds
    List<String> protectedFieldIds
}
