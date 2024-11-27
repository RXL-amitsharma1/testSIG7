package com.rxlogix

import com.rxlogix.mapping.LmIngredient
import com.rxlogix.mapping.LmProduct
import com.rxlogix.user.Group
import com.rxlogix.user.User

class ProductBasedSecurityService {

    def genericNameCacheService
    def dataObjectService
    def cacheService


    List<String> allAllowedProductForUser(User user) {
        List<String> allowedProductsNameList = cacheService.getProductNamesListByValue(user.id)
        allowedProductsNameList
    }

    List<String> allAllowedProductIdsForUser(User user) {
        List<String> allowedProductsIdsList = cacheService.getProductIdsListByValue(user.id)
        allowedProductsIdsList
    }

    def allowedGenericNamesForUser(User user) {
        Set<Group> currentGroups = user.groups
        new HashSet<String>((currentGroups.allowedProductList.flatten() - null).collect {it->
            genericNameCacheService.getGenericNameForProduct(it)?.toLowerCase()?.trim()
        })
    }

    boolean checkIngredientExistsForEvdas(List<String> ingredientList) {
        Integer count = 0
        count = LmIngredient."eudra".withTransaction {
            LmIngredient."eudra".countByIngredientInList(ingredientList)
        }
        count != 0
    }

    boolean checkProductExistsForFAERS(def productNameList) {
        Integer count = 0
        count = LmProduct."faers".withTransaction {
            LmProduct."faers".countByNameInList(productNameList)
        }
        count != 0
    }

    boolean checkIngredientExistsForFAERS(def ingredientNameList) {
        Integer count = 0
        count = LmIngredient."faers".withTransaction {
            LmIngredient."faers".countByIngredientInList(ingredientNameList)
        }
        count != 0
    }
}
