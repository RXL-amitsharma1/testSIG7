package com.rxlogix

import com.rxlogix.config.AllowedDictionaryDataCache
import com.rxlogix.config.DictionaryMapping
import com.rxlogix.config.ProductDictionaryCache
import com.rxlogix.util.SignalQueryHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.GroovyResultSet
import groovy.sql.Sql

import java.sql.SQLException

@Transactional
class ProductDictionaryCacheService {

    def cacheService
    def pvsProductDictionaryService
    def signalDataSourceService
    def dataObjectService
    def productGroupCache = [:]
    def dataSource_pva

    /**
     * This we will do it on the request from UI
     *
     * @param group
     * @param productNameList
     * @param isGroup
     * @return
     */
    def updateProductDictionaryCache(def group, List productNameList, boolean isGroup) {

        if (group && group.allowedProd) {
            ProductDictionaryCache productDictionaryCache =
                    isGroup ? ProductDictionaryCache.findByGroup(group) : ProductDictionaryCache.findBySafetyGroup(group)

            if (!productDictionaryCache) {
                productDictionaryCache = new ProductDictionaryCache()
                if (isGroup) {
                    productDictionaryCache.group = group
                } else {
                    productDictionaryCache.safetyGroup = group
                }
            }
            productDictionaryCache.allowedDictionaryData?.clear()
            //Fetch the dictionary mapped cached at the time of booting
            Map dictionaryMap = dataObjectService.getProductMapping()
            Set keySet = dictionaryMap.keySet()

            //First of all, fetch the product id list based on the passed products from UI.
            String productViewName = dataObjectService.getProductViewName()

            //String products = SignalQueryHelper.getDictProdNameINCriteria(productNameList.collect { "'"+ it + "'" }, "COL_3")?.toString()

            String productNameColumn = Holders.config.dictionary.product.name.column
            String otherNameColumn = Holders.config.dictionary.other.name.column
            String idColumn = Holders.config.dictionary.id.column

            String products = SignalQueryHelper.getDictProdNameINCriteria(productNameList.collect {
                //Replacing the apostrophe with double apostrophe to fix one ORA SQL error. -PS
                "'" + it?.replaceAll("'", "''") + "'"
            }, productNameColumn)?.toString()
            String productNameSql = "select $idColumn from " + productViewName + " where " + products

            List productIdList = pvsProductDictionaryService.fetchProductsData(idColumn, productNameSql)

            Sql sql = null

            try {
                sql = new Sql(dataSource_pva)

                //Iterating over the key to fetch the data from map
                for (keys in keySet) {

                    Map dictMap = dictionaryMap.get(keys)

                    if (!dictMap.isProduct) {

                        String view = dictMap.get("view")
                        String productLinkView = dictMap.get("productLinkView")

                        //Fetching the other object ids based on the selected products ids passed from UI
                        List dataIdList = []

                        String otherDictObjects =
                                SignalQueryHelper.getDictProdNameINCriteria(productIdList.collect {
                                    "'" + it + "'"
                                }, otherNameColumn)?.toString()

                        String sqlStatement = "select $idColumn from " + productLinkView + " where " + otherDictObjects
                        sql.eachRow(sqlStatement) { GroovyResultSet resultSet ->
                            if(resultSet.getString(idColumn)){
                                dataIdList.add(resultSet.getString(idColumn))
                            }
                        }

                        if (dataIdList) {
                            //Fetching the list of names from the views based on the prepared data ids of other objects.
                            List nameList = []
                            List idList = []
                            String finalDictObjects = SignalQueryHelper.getDictProdNameINCriteria(dataIdList.collect {
                                "'" + it + "'"
                            }, idColumn)?.toString()
                            String nameStatement = "select $idColumn, $otherNameColumn from " + view + " where " + finalDictObjects
                            log.info("The named ")

                            sql.eachRow(nameStatement) { GroovyResultSet resultSet ->
                                nameList.add(resultSet.getString(otherNameColumn))
                                idList.add(resultSet.getString(idColumn))
                            }

                            //Preparing the allowed dictionary data.
                            AllowedDictionaryDataCache allowedDictionaryData = new AllowedDictionaryDataCache()
                            allowedDictionaryData.fieldLevelId = dictMap.fieldId
                            allowedDictionaryData.label = dictMap.label
                            allowedDictionaryData.isProduct = dictMap.isProduct
                            allowedDictionaryData.allowedData = nameList.join(",")
                            allowedDictionaryData.allowedDataIds = idList.join(",")
                            productDictionaryCache.addToAllowedDictionaryData(allowedDictionaryData)
                        }
                    }
                }

                //Now set the product fields
                DictionaryMapping dictionaryMapping = null

                DictionaryMapping.withTransaction {
                    dictionaryMapping = DictionaryMapping."pva".findByIsProductAndLanguageValueAndViewType(true, "en", Constants.DictionaryFilterType.FILTER)
                }

                AllowedDictionaryDataCache allowedDictionaryData = new AllowedDictionaryDataCache()
                allowedDictionaryData.fieldLevelId = dictionaryMapping?.id
                allowedDictionaryData.label = dictionaryMapping?.label
                allowedDictionaryData.isProduct = dictionaryMapping?.isProduct
                allowedDictionaryData.allowedData = productNameList.join(",")
                allowedDictionaryData.allowedDataIds = productIdList.join(",")
                productDictionaryCache.addToAllowedDictionaryData(allowedDictionaryData)
            } catch (SQLException sqlException) {
                sqlException.printStackTrace()
            } catch (Throwable th) {
                th.printStackTrace()
            } finally {
                sql?.close()
            }
            productDictionaryCache.save(flush: true)
        }
    }

    def getAllowProductIdLevelList(group, level, boolean isGroup = true) {

        ProductDictionaryCache productDictionaryCache
        if (isGroup) {
            productDictionaryCache = ProductDictionaryCache.findByGroup(group)
        } else {
            productDictionaryCache = ProductDictionaryCache.findBySafetyGroup(group)
        }
        def dictData
        productDictionaryCache?.allowedDictionaryData?.each {
            if ((it.fieldLevelId).toString() == level.toString()) {
                dictData = it.allowedDataIds?.split(",")
            }
        }
        dictData
    }
}
