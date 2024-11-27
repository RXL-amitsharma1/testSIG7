package com.rxlogix

import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import groovy.sql.Sql

class UtilService {
    def dataSource
    def applyDescendingSortOnLastUpdated(List resultList){
        resultList = resultList.sort { it.lastUpdated}
        resultList.reverse(true)
        resultList
    }

    def fetchUserListForGroup( User user, GroupType groupType ) {
        def sql = new Sql( dataSource )
        List<String> groups = [ ]
        try {
            List<Map> res = sql.rows( "select * from USER_GROUP_MAPPING  ugm left join GROUPS grp on ugm.GROUP_ID = grp.ID where USER_ID=${ user?.id } and grp.GROUP_TYPE=${groupType as String}" )
            res.each {
                groups.add( it.NAME )
            }
        } catch( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql?.close()
        }
        groups?.sort()
    }

    List<Long> fetchUserListIdForGroup( User user, GroupType groupType ) {
        def sql = new Sql( dataSource )
        List<Long> groups = [ ]
        try {
            List<Map> res = sql.rows( "select * from USER_GROUP_MAPPING  ugm left join GROUPS grp on ugm.GROUP_ID = grp.ID where USER_ID=${ user?.id } and grp.GROUP_TYPE=${ groupType as String }" )
            res.each {
                groups.add( it.GROUP_ID as Long )
            }
        } catch( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql?.close()
        }
        return groups?.sort()
    }
}
