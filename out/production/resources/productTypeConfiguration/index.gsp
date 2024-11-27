<%--
  Created by IntelliJ IDEA.
  User: rxlogix
  Date: 02/02/23
  Time: 11:47 AM
--%>

<%@ page contentType="text/html;charset=UTF-8" import="com.rxlogix.Constants;  com.rxlogix.enums.ReportFormat" %>
<html>
<head>
    <meta name="layout" content="main"/>
    <title><g:message code="app.productTypeConfig.title" default="Product Type Configuration"/></title>
    <asset:javascript src="app/pvs/productTypeConfiguration.js"/>
    <asset:javascript src="yadcf/jquery.dataTables.yadcf.js"/>
    <asset:stylesheet src="yadcf/jquery.dataTables.yadcf.css"/>
    <asset:javascript src="app/pvs/bootbox.min.js"/>
    <g:javascript>
        productTypeListUrl = "${createLink(controller: 'productTypeConfiguration', action: 'list')}";
        productTypeCreateUrl = "${createLink(controller: 'productTypeConfiguration', action: 'create')}";
        productTypeUpdateUrl = "${createLink(controller: 'productTypeConfiguration', action: 'update')}";
        productTypeDeleteUrl = "${createLink(controller: 'productTypeConfiguration', action: 'delete')}";
    </g:javascript>
    <style type="text/css">
    .yadcf-filter-wrapper {
        display: block;
    }
    #productTypeTable tr {
        height: 45px;
    }
    .countBox{
        font-weight: 800 !important;
    }
    ::-webkit-scrollbar-thumb {
        background: #aaa;
    }

    </style>
</head>

<body>
    <div id="productTypeConfigContainer" style="padding-top: 10px;">
        <rx:container title="Aggregate Alert Product Type Configuration" productTypeConfig="${true}">
            <g:render template="/includes/layout/flashErrorsDivs"/>
            <div class="messageContainer" style="word-break: break-all"></div>
            <div id="productTypeTableDiv">
                <table id="productTypeTable" class="dataTable row-border hover no-footer">
                    <thead>
                        <tr>
                            <th class="col-sm-4"><g:message code="productType.signal.name" default="Name"/><span class="required-indicator">*</span></th>
                            <th class="col-sm-3"><g:message code="productType.signal.productType"  default="Product Type"/><span class="required-indicator">*</span></th>
                            <th class="col-sm-3"><g:message code="productType.signal.roleType"  default="Role"/><span class="required-indicator">*</span></th>
                            <th class="sorting_disabled col-sm-2"></th>
                        </tr>
                    </thead>
                </table>
            </div>
        </rx:container>
    </div>

</body>
</html>