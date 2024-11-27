package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional


@Transactional
class OwnershipService {
    def auditLogService

    Map<String, Integer> updateOwners(User previous, User current) {
        def resultMap = [:]

        def criteria1 = Configuration.where{owner == previous}
        resultMap.put("configuration", criteria1.updateAll(owner: current))
        return resultMap;
    }
}
