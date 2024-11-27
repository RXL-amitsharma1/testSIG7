package com.rxlogix

import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.CustomUserDetails

class DictionaryInterceptor {
    def productBasedSecurityService
    def springSecurityService

    DictionaryInterceptor() {
        match(controller: ~/(studyDictionary|eventDictionary|productDictionary)/)
    }

    def productDictionaryCacheService
    def alertService

    boolean before() {

        def currentUser
        if (session.'currentUuser') {
            currentUser = session.'currentUuser'
        }
        if (!currentUser) {
            currentUser = springSecurityService.currentUser
            if (currentUser instanceof CustomUserDetails) {
                currentUser = User.findByUsernameIlike(currentUser.username)
            }
            session.'currentUuser' = currentUser
        }
        if (currentUser) {
            Map langMaping=[
                    "en" : "1",
                    "ja" : "25"
            ]
            params.currentLang = langMaping.get(currentUser.preference?.locale,"1")
            if (params.dataSource == Constants.DataSource.FAERS || !alertService.isProductSecurity()) {
                params.includeIds = []
            } else if (alertService.isProductSecurity()) {
                params.includeIds = getProductIds(currentUser, params.dictionaryLevel)?.collect { it as String }
            }
        }

        true
    }

    private getProductIds(user, level) {
        def allowedProducts = []
        Set<Group> currentGroups = user.groups
        currentGroups.each { Group group ->
            def products = productDictionaryCacheService.getAllowProductIdLevelList(group, level, true)
            if (products) {
                allowedProducts.add(products)
            }
        }
        allowedProducts.flatten()
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
