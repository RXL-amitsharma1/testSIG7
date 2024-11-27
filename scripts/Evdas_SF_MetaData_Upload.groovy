import com.rxlogix.signal.*

Date startDate = Date.parse('dd-MMM-yyyy','01-Feb-2016')
Date endDate = Date.parse('dd-MMM-yyyy','29-Feb-2016')

def frequencyMapList = [
    [name: 'Substance1', startDate: startDate, endDate:endDate, uploadFrequency:'Monthly', miningFrequency:'Monthly', frequencyName: "Monthly", alertType: "EVDAS Alert"],
    [name: 'Test Product AJ', startDate: Date.parse('dd-MMM-yyyy','01-Jan-2015'), endDate: Date.parse('dd-MMM-yyyy','31-Dec-2015'), uploadFrequency:'Yearly', miningFrequency:'Yearly', frequencyName: "Yearly TEST", alertType: "Aggregate Case Alert"],
    [name: 'Rxlogix Test Poduct 02', startDate: startDate, endDate:endDate, uploadFrequency:'Monthly', miningFrequency:'Monthly', frequencyName: "Monthly1", alertType: "Aggregate Case Alert"],
    [name: 'Test PVS 4', startDate: Date.parse('dd-MMM-yyyy','01-Jan-2015'), endDate:Date.parse('dd-MMM-yyyy','31-Dec-2015'), uploadFrequency:'Yearly', miningFrequency:'Yearly', frequencyName: "Yearly TEST", alertType: "Aggregate Case Alert"],
    [name: 'Testproduct2', startDate: Date.parse('dd-MMM-yyyy','01-Jan-2014'), endDate:Date.parse('dd-MMM-yyyy','31-Jan-2014'), uploadFrequency:'Monthly', miningFrequency:'Monthly', frequencyName: "Monthly2", alertType: "Aggregate Case Alert"]
]

frequencyMapList.each {def frequencyMap ->
    def substance = new SubstanceFrequency(frequencyMap)
    substance.save(flush:true)
}

