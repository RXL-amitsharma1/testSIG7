package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders


class LmProductFamily implements SelectableList {

    Long id
    String name

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_FAMILY_NAME_DSP"

        cache: "read-only"
        version false

        id column: "PROD_FAMILY_ID", generator: "assigned"
        name column: "FAMILY_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:40)
    }

    @Override
    def getSelectableList() {
        LmProductFamily.withTransaction {
            return this.executeQuery("select distinct lmp.name from LmProductFamily lmp order by lmp.name asc")
        }
    }

    static List<String> getAllNamesForIds(List<String> productIds) {
        LmProductFamily.withTransaction {
            LmProductFamily.createCriteria().list {
                projections {
                    distinct("name")
                }
                'or' {
                    productIds.collate(1000).each {
                        'in'("id", it*.toLong())
                    }
                }
                order("name", "asc")
            }
        }
    }
}
