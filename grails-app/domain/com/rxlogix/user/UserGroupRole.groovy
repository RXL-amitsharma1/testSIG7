package com.rxlogix.user


class UserGroupRole implements Serializable {
    static auditable = false
    private static final long serialVersionUID = 1

    Group userGroup
    Role role

    static mapping = {
        id composite: ['role', 'userGroup']
        version false
        table name: "USER_GROUP_ROLE"
    }

    static boolean exists(long userGroupId, long roleId) {
        UserGroupRole.where {
            userGroup == Group.load(userGroupId) &&
                    role == Role.load(roleId)
        }.count() > 0
    }
}
