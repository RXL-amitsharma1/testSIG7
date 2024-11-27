<%@ page import="com.rxlogix.mapping.LmProduct" %>
<g:set var="column1Width" value="4"/>
<g:set var="column2Width" value="8"/>

<div class="row">
    <div class="col-md-4">

        <div class="form-group">
            <label class="" for="name">
                <g:message code="group.name.label"/>
                <span class="required-indicator">*</span>
            </label>
            <g:textField class="form-control" name="name" required="" value="${safetyGroupInstance?.name}" />
        </div>

        <div class="form-group">
            <label class="col-md-12" for="searchedProducts">
                <g:message code="group.allowed.products" default="Allowed Products"/>
            </label>
            <div style="width: calc(100% - 50px); display:inline-flex">
                <g:textField class="form-control" placeholder="Enter Product Name" name="searchedProducts" id="searchedProducts" value=""/>
                <a class=" btn btn-primary box-inline m-l-5" id = "searchProd" tabindex="0" ><i class="fa fa-search"></i></a>
            </div>
        </div>

    </div>
</div>

<div class="row">
        <div class="col-md-12 m-t-15">
            <g:select id="allowedProductList" name="allowedProductList" class="allowedProductList"
                      from="" multiple="true" value="${allowedProductsList}" />
        </div>
        <g:hiddenField name="savedProductsList" value="${allowedProductsList?.join('#%#')}" id="savedProductsList"/>
</div>




