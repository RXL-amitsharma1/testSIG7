package com.rxlogix.user

class UserGroupMapping {

    User user
    Group group

    static mapping = {
        table "USER_GROUP_MAPPING"
    }

    static boolean remove(Group ug, User u, boolean flush = false) {
        if (ug == null || u == null) return false

        int rowCount = UserGroupMapping.where {
            group == Group.load(ug.id) &&
                    user == User.load(u.id)
        }.deleteAll()

        if (flush) { UserGroupMapping.withSession { it.flush() } }

        rowCount > 0
    }

    static void removeAll(Group u, boolean flush = false) {
        if (u == null) return

        UserGroupMapping.where {
            group == Group.load(u.id)
        }.deleteAll()

        if (flush) { UserGroupMapping.withSession { it.flush() } }
    }

    static void removeAll(User u, boolean flush = false) {
        if (u == null) return

        UserGroupMapping.where {
            user == User.load(u.id)
        }.deleteAll()

        if (flush) { UserGroupMapping.withSession { it.flush() } }
    }


    static UserGroupMapping create(Group userGroup, User user, boolean flush = false) {
        def instance = new UserGroupMapping(group: userGroup, user: user)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean exists(long userGroupId, long userId) {
        UserGroupMapping.where {
            group == Group.load(userGroupId) &&
                    user == User.load(userId)
        }.count() > 0
    }

}
