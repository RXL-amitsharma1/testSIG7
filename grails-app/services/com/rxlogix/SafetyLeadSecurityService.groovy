package com.rxlogix

import com.rxlogix.config.SafetyGroup
import com.rxlogix.signal.Alert
import com.rxlogix.user.User
import com.rxlogix.Constants.ProductDictionarySelection

class SafetyLeadSecurityService {

    def genericNameCacheService
    def cacheService
    def dataObjectService

    List<String> allAllowedProductsForUser(Long userId) {
        List<String> allowedProductsNameList = cacheService.getProductNamesListForSafetyLeadByValue(userId)
        allowedProductsNameList
    }

    def allowedGenericNamesForUser(User user) {
        Set<SafetyGroup> currentSafetyGroups = user.safetyGroups
        if(currentSafetyGroups?.allowedProductList)
            new HashSet<String>((currentSafetyGroups?.allowedProductList.flatten() - null).collect {it->
                genericNameCacheService.getGenericNameForProduct(it)?.toLowerCase()?.trim()
            })
    }

    def isProductSafetyLead(Set alertProducts, Set productSet, Set genericSet) {
        if(!alertProducts) {
            false
        } else {
            if(productSet && productSet.containsAll(alertProducts)) {
                true
            } else if (genericSet && genericSet.containsAll(alertProducts)) {
                true
            }
            else {
                false
            }
        }
    }

    def isUserSafetyLead(User user, alert){
        if(!user) {
            return false
        }

        def productSet = allAllowedProductsForUser(user.id) as Set
        def genericSet =  allowedGenericNamesForUser(user) as Set
        def alertProducts = null

        if (alert instanceof Alert) {
            alertProducts = alert.productNameList ? alert.productNameList.collect{it.toLowerCase()} as Set : null
        } else {
            alertProducts = alert.productName ? [alert.productName.toLowerCase()] : null
        }
        isProductSafetyLead(alertProducts, productSet, genericSet)
    }
}
