package com.rxlogix.signal

class BatchLotData implements Serializable {
    Long id
    Long batchLotId
    Long version
    String productId
    String product
    String description
    String bulkBatch
    String bulkBatchDate
    String fillBatch
    String fillBatchName
    String fillExpiry
    String fillUnits
    String packageBatch
    String packageCountry
    String packageUnit
    String packageReleaseDate
    String shippingBatch
    String componentBatch
    String dataPeriod
    String startDate
    String productGroupName;
    String endDate
    String udField1
    String udField2
    String udField3
    String udField4
    String udField5
    String udField6
    String udField7
    String udField8
    String udField9
    String udField10
    String validationError
    String etlStatus
    String batchId
    Date batchDate
    String productHierarchy
    String productHierarchyId
    static mapping = {
        datasource "pva"
        table name: "PVS_BS_APP_BATCH_LOT_DATA"
        id generator: 'sequence', params: [sequence: 'BATCH_LOT_DATA_SEQ']
        //id column: "ID", generator: "assigned"
    }

    static constraints = {
        batchLotId nullable: true
        productId nullable: true
        product nullable: true
        description nullable: true
        bulkBatch nullable: true
        bulkBatchDate nullable: true
        fillBatch nullable: true
        fillBatchName nullable: true
        fillExpiry nullable: true
        fillUnits nullable: true
        packageBatch nullable: true
        packageCountry nullable: true
        packageUnit nullable: true
        packageReleaseDate nullable: true
        shippingBatch nullable: true
        componentBatch nullable: true
        dataPeriod nullable: true
        startDate nullable: true
        endDate nullable: true
        productGroupName nullable: true
        udField1 nullable: true
        udField2 nullable: true
        udField3 nullable: true
        udField4 nullable: true
        udField5 nullable: true
        udField6 nullable: true
        udField7 nullable: true
        udField8 nullable: true
        udField9 nullable: true
        udField10 nullable: true
        validationError nullable: true
        etlStatus nullable: true
        batchId nullable: true
        batchDate nullable: true
        productHierarchy nullable: true
        productHierarchyId nullable: true
    }

    Long getId() {
        return id
    }

    void setId(Long id) {
        this.id = id
    }

    Long getBatchLotId() {
        return batchLotId
    }

    void setBatchLotId(Long batchLotId) {
        this.batchLotId = batchLotId
    }

    Long getVersion() {
        return version
    }

    void setVersion(Long version) {
        this.version = version
    }

    String getProductId() {
        return productId
    }

    void setProductId(String productId) {
        this.productId = productId
    }

    String getProduct() {
        return product
    }

    void setProduct(String product) {
        this.product = product
    }

    String getDescription() {
        return description
    }

    void setDescription(String description) {
        this.description = description
    }

    String getBulkBatch() {
        return bulkBatch
    }

    void setBulkBatch(String bulkBatch) {
        this.bulkBatch = bulkBatch
    }

    String getBulkBatchDate() {
        return bulkBatchDate
    }

    void setBulkBatchDate(String bulkBatchDate) {
        this.bulkBatchDate = bulkBatchDate
    }

    String getFillBatch() {
        return fillBatch
    }

    void setFillBatch(String fillBatch) {
        this.fillBatch = fillBatch
    }

    String getFillBatchName() {
        return fillBatchName
    }

    void setFillBatchName(String fillBatchName) {
        this.fillBatchName = fillBatchName
    }

    String getFillExpiry() {
        return fillExpiry
    }

    void setFillExpiry(String fillExpiry) {
        this.fillExpiry = fillExpiry
    }

    String getFillUnits() {
        return fillUnits
    }

    void setFillUnits(String fillUnits) {
        this.fillUnits = fillUnits
    }

    String getPackageBatch() {
        return packageBatch
    }

    void setPackageBatch(String packageBatch) {
        this.packageBatch = packageBatch
    }

    String getPackageCountry() {
        return packageCountry
    }

    void setPackageCountry(String packageCountry) {
        this.packageCountry = packageCountry
    }

    String getPackageUnit() {
        return packageUnit
    }

    void setPackageUnit(String packageUnit) {
        this.packageUnit = packageUnit
    }

    String getPackageReleaseDate() {
        return packageReleaseDate
    }

    void setPackageReleaseDate(String packageReleaseDate) {
        this.packageReleaseDate = packageReleaseDate
    }

    String getShippingBatch() {
        return shippingBatch
    }

    void setShippingBatch(String shippingBatch) {
        this.shippingBatch = shippingBatch
    }

    String getComponentBatch() {
        return componentBatch
    }

    void setComponentBatch(String componentBatch) {
        this.componentBatch = componentBatch
    }

    String getDataPeriod() {
        return dataPeriod
    }

    void setDataPeriod(String dataPeriod) {
        this.dataPeriod = dataPeriod
    }

    String getStartDate() {
        return startDate
    }

    void setStartDate(String startDate) {
        this.startDate = startDate
    }

    String getEndDate() {
        return endDate
    }

    void setEndDate(String endDate) {
        this.endDate = endDate
    }

    String getProductGroupName() {
        return productGroupName
    }

    void setProductGroupName(String productGroupName) {
        this.productGroupName = productGroupName
    }

    String getUdField1() {
        return udField1
    }

    void setUdField1(String udField1) {
        this.udField1 = udField1
    }

    String getUdField2() {
        return udField2
    }

    void setUdField2(String udField2) {
        this.udField2 = udField2
    }

    String getUdField3() {
        return udField3
    }

    void setUdField3(String udField3) {
        this.udField3 = udField3
    }

    String getUdField4() {
        return udField4
    }

    void setUdField4(String udField4) {
        this.udField4 = udField4
    }

    String getUdField5() {
        return udField5
    }

    void setUdField5(String udField5) {
        this.udField5 = udField5
    }

    String getUdField6() {
        return udField6
    }

    void setUdField6(String udField6) {
        this.udField6 = udField6
    }

    String getUdField7() {
        return udField7
    }

    void setUdField7(String udField7) {
        this.udField7 = udField7
    }

    String getUdField8() {
        return udField8
    }

    void setUdField8(String udField8) {
        this.udField8 = udField8
    }

    String getUdField9() {
        return udField9
    }

    void setUdField9(String udField9) {
        this.udField9 = udField9
    }

    String getUdField10() {
        return udField10
    }

    void setUdField10(String udField10) {
        this.udField10 = udField10
    }

    String getValidationError() {
        return validationError
    }

    void setValidationError(String validationError) {
        this.validationError = validationError
    }

    String getEtlStatus() {
        return etlStatus
    }

    void setEtlStatus(String etlStatus) {
        this.etlStatus = etlStatus
    }

    String getBatchId() {
        return batchId
    }

    void setBatchId(String batchId) {
        this.batchId = batchId
    }

    Date getBatchDate() {
        return batchDate
    }

    void setBatchDate(Date batchDate) {
        this.batchDate = batchDate
    }

    String getProductHierarchy() {
        return productHierarchy
    }

    void setProductHierarchy(String productHierarchy) {
        this.productHierarchy = productHierarchy
    }

    String getProductHierarchyId() {
        return productHierarchyId
    }

    void setProductHierarchyId(String productHierarchyId) {
        this.productHierarchyId = productHierarchyId
    }
}
