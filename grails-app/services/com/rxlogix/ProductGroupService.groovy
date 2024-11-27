package com.rxlogix

import com.rxlogix.config.DictionaryMapping
import com.rxlogix.config.ProductGroup
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.config.ViewConfig
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql

@Transactional
class ProductGroupService {

    def signalDataSourceService
    def CRUDService
    def dataSource
    def dataSource_pva

    List<Map> fetchProductGroupsListByDisplay(Boolean display) {
        ProductGroup.findAllByDisplay(display).collect {
            [
                    id       : it.id,
                    groupName: it.groupName
            ]
        }
    }

    /**
     *  This method is responsible for saving/updating product Grp details in mart
     * proc -->
     * PKG_PVS_APP_UTIL.P_PRODUCT_GROUP(pi_grp_id IN NUMBER,
     * pi_grp_name IN VARCHAR2,
     * pi_class IN VARCHAR2,
     * pi_disp IN NUMBER,
     * pi_delete IN NUMBER DEFAULT NULL) ;
     *
     * */


    ProductGroup saveUpdateProductGroupMart(ProductGroup productGroup) {
        /**
         * Commented code due to creating new connection not taking from pool
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        */
        Sql sql = null
        try {
            sql = new Sql(dataSource_pva)
            if (productGroup.getId()) {
                CRUDService.update(productGroup)
            } else {
                CRUDService.save(productGroup)
            }
            List<Map> productDetails = MiscUtil?.getProductDictionaryValues(productGroup?.productSelection, true)

            String insertStatement = "Begin execute immediate('delete from gtt_filter_key_values'); "
            List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
            productDetails.eachWithIndex { Map entry, int i ->
                int keyId = productViewsList.get(i).keyId
                entry.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                }
            }
            insertStatement += " END;"
            if (insertStatement) {
                sql.execute(insertStatement)
            }
            log.info("Save proc called for productGrpName = $productGroup.groupName, productGrpselection = $productGroup.productSelection, productGrpClassification = $productGroup.classification")
            sql.call("{call PKG_PVS_APP_UTIL.P_PRODUCT_GROUP(?,?,?,?,?)}", [productGroup.getId(), productGroup.groupName, productGroup.classification.toString(), productGroup.display, false])
            log.info("Product Group Saved")

            return productGroup
        } catch (Exception e) {
            log.info(e.printStackTrace())
            throw e
        } finally {
            if (Objects.nonNull(sql)) {
                sql.close()
            }
        }
    }

    void deleteProductGroupMart(Long id) {
        ProductGroup productGroup = ProductGroup.get(id)
        CRUDService.delete(productGroup)
        /**
         * Commented code due to creating new connection not taking from pool
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        */
        Sql sql = null
         try {
            sql = new Sql(dataSource_pva)
            sql.call("{call PKG_PVS_APP_UTIL.P_PRODUCT_GROUP(?,?,?,?,?)}", [id, null, null, null, true])
            log.info("Product Group Deleted")

        } catch (Exception e) {
            log.info(e.printStackTrace())
            throw e
        } finally {
             if (Objects.nonNull(sql)) {
                 sql.close()
             }
        }
    }

    void productSelectionUpgrade(def domain, String tableName, String productSelectionColumnName) {
        if(Holders.config.productGroupUpgradationFlag) {
            Integer productGroupIntIndex = getProductDictionary()["Product Group"]
            int dictionaryColumns =  getProductDictionary().size()
            String searchableIndex = "%\"" + dictionaryColumns + "\"" + ":[%"
            String begin = 'BEGIN '
            String updateQuery = ''
            List list = []
            JsonSlurper slurper = new JsonSlurper()
            Map productSelectionMap
            if (domain != ValidatedSignal) {
                list = domain.createCriteria().list {
                    not {
                        like('productSelection', searchableIndex)
                    }
                }

                list?.each {
                    productSelectionMap = slurper.parseText(it.productSelection)
                    for (int prodDictColumns = dictionaryColumns; prodDictColumns > productGroupIntIndex; prodDictColumns--) {
                        productSelectionMap[prodDictColumns as String] = productSelectionMap[(prodDictColumns - 1) as String]
                    }
                    productSelectionMap[productGroupIntIndex as String] = []
                    it.productSelection = productSelectionMap as JSON
                    updateQuery += 'UPDATE ' + tableName + ' SET ' + productSelectionColumnName + ' = \'' + it.productSelection + '\' WHERE ID = ' + it.id + ';'
                }
            } else {
                list = ValidatedSignal.createCriteria().list {
                    not {
                        like('products', searchableIndex)
                    }
                }

                list?.each {
                    productSelectionMap = slurper.parseText(it.products)
                    for (int prodDictColumns = dictionaryColumns; prodDictColumns > productGroupIntIndex; prodDictColumns--) {
                        productSelectionMap[prodDictColumns as String] = productSelectionMap[(prodDictColumns - 1) as String]
                    }
                    productSelectionMap[productGroupIntIndex as String] = []
                    it.products = productSelectionMap as JSON
                    updateQuery += 'UPDATE ' + tableName + ' SET ' + productSelectionColumnName + ' = \'' + it.products + '\' WHERE ID = ' + it.id + ';'
                }
            }

            Sql sql = new Sql(dataSource)
            if(updateQuery) {
                updateQuery = begin + updateQuery
                updateQuery += ' END;'
                sql.executeUpdate(updateQuery)
            }
            sql?.close()
        }

    }

    Map getProductDictionary() {
        List<DictionaryMapping> productGroupLabel
        List<DictionaryMapping> ingredientLabel
        List<DictionaryMapping> familyLabel
        List<DictionaryMapping> productNameLabel
        List<DictionaryMapping> tradeNameLabel

        String productGroup = Holders.config.dictionary.productGroup.column.name
        String ingredient = Holders.config.dictionary.ingredient.column.name
        String family = Holders.config.dictionary.family.column.name
        String product = Holders.config.dictionary.product.column.name
        String tradeName = Holders.config.dictionary.trade.column.name

        DictionaryMapping.withTransaction {
            productGroupLabel = productGroup ? DictionaryMapping.findAllByLabel(productGroup) - null : ""
            ingredientLabel = productGroup ? DictionaryMapping.findAllByLabel(ingredient) - null : ""
            familyLabel = productGroup ? DictionaryMapping.findAllByLabel(family) - null : ""
            productNameLabel = productGroup ? DictionaryMapping.findAllByLabel(product) - null: ""
            tradeNameLabel = productGroup ? DictionaryMapping.findAllByLabel(tradeName) - null : ""
        }

        ["Product Group" : productGroupLabel[0].id as String, "Ingredient" : ingredientLabel[0].id as String, "Family" : familyLabel[0].id as String,
         "Product Name" : productNameLabel[0].id as String, "Trade Name" : tradeNameLabel[0].id as String]
    }
}
