package com.rxlogix.commandObjects

class ProductGroupCO {
    Long id;
    String name;
    String oldName;
    List<ProductCO> products;
    List<String> includeSources;
    String owner;
    Map<String,List<String>> sharedWith;
    Integer type;
    String description;
    Integer tenantId;
    String copyGroups;
    String validationError;
    String uniqueIdentifier;
    Long productGroupStatusId;
    Set<String> includeSourcesToAdd;

    String getStringOfProducts() {
        StringBuffer sb = new StringBuffer();
        if( this.products != null && this.products.size() > 0 ) {
            sb.append("[")
            this.products.each {
                it->
                    sb.append("{\"productId\":\""+it.productId+"\"," +
                            "\"productName\":\""+it.productName+"\"," +
                            "\"hierarchy\":\""+it.hierarchy+"\"," +
                            "\"source\":\""+it.source+"\"," +
                            "\"validationError\":\""+it.validationError+"\"," +
                            "}")
            }
            sb.append("]")
        }
    }
}
