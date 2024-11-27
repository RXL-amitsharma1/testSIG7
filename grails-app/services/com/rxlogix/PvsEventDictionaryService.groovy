package com.rxlogix

import com.rxlogix.mapping.MedDraHLGT
import com.rxlogix.mapping.MedDraHLT
import com.rxlogix.mapping.MedDraLLT
import com.rxlogix.mapping.MedDraPT
import com.rxlogix.mapping.MedDraSOC


class PvsEventDictionaryService {

    def getEventInstance(String level, String eventId, String searchTerm, String selectedDatasource) {
        switch (level) {
            case "1":
                MedDraSOC."$selectedDatasource".withTransaction {
                    if (eventId) return MedDraSOC."$selectedDatasource".get(eventId)
                    else if (searchTerm) return MedDraSOC."$selectedDatasource".findAllByNameIlikeAndLangId('%' + searchTerm + '%',1, [sort: "name", order: "asc"])
                }
                break
            case "2":
                MedDraHLGT."$selectedDatasource".withTransaction {
                    if (eventId) return MedDraHLGT."$selectedDatasource".get(eventId)
                    else if (searchTerm) return MedDraHLGT."$selectedDatasource".findAllByNameIlikeAndLangId('%' + searchTerm + '%',1, [sort: "name", order: "asc"])
                }
                break
            case "3":
                MedDraHLT."$selectedDatasource".withTransaction {
                    if (eventId) return MedDraHLT."$selectedDatasource".get(eventId)
                    else if (searchTerm) return MedDraHLT."$selectedDatasource".findAllByNameIlikeAndLangId('%' + searchTerm + '%', 1, [sort: "name", order: "asc"])
                }
                break
            case "4":
                MedDraPT."$selectedDatasource".withTransaction {
                    if (eventId) return MedDraPT."$selectedDatasource".get(eventId)
                    else if (searchTerm) return MedDraPT."$selectedDatasource".findAllByNameIlikeAndLangId('%' + searchTerm + '%',1, [sort: "name", order: "asc"])
                }
                break
            case "5":
                MedDraLLT."$selectedDatasource".withTransaction {
                    if (eventId) return MedDraLLT."$selectedDatasource".get(eventId)
                    else if (searchTerm) return MedDraLLT."$selectedDatasource".findAllByNameIlikeAndLangId('%' + searchTerm + '%', 1, [sort: "name", order: "asc"])
                }
                break
        }
    }
}
