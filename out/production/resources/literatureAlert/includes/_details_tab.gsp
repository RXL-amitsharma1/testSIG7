<%@ page import="com.rxlogix.enums.ReportFormat" %>
<style>
table.dataTable thead > tr > th {
    padding-left: 5px;
    padding-right: 5px;
}
#activities .m-l-1 {
    margin-top: -48px;
    margin-right: -10px!important;
}
div.dataTables_wrapper {
    margin: 0 auto;
}

</style>

<div class="row">
    <div class="panel-heading pv-sec-heading m-b-10">
        <div class="row">
            <div class="col-md-4"></div>
              <div class="col-md-8 grid_margin">
                  <div>
       <div class="pos-rel pull-right">
    <!------------------=================--------------pinned icon code started-------------================----------------------->
        <span style="padding-top:20px">
    <!------------------=================--------------Abstract view code started-----------================----------------------->
           <span class="m-r-10 grid-pin collapse" id="ic-abstractview">
               <label for="detailed-view-checkbox">
                   <g:checkBox name="detailed-view-checkbox" checked="${false}" accesskey="v"/>
                   <g:message code="app.label.literature.abstract.view" />
               </label>
           </span>
    <!------------------================--------------Abstract view code end----------------================---------------------->

                    <!------------==================-------Field selection code start------==========------------------->
                                  <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-configureLiteratureFields" data-fieldconfigurationbarid="literatureFields" data-container="columnList-container" title="${message(code: 'app.label.choosefields')}">
                                      <i class="mdi mdi-settings-outline font-24"></i>
                                  </a>
                    <!------------==================---------------Field selection code end----------==========------------------->
                    <!------------==================--------------export to code start---------------==========------------------->
           <span class="grid-pin collapse theme-color dropdown" id="exportTypes">
               <span  tabindex="0" class="dropdown-toggle saveViewPanel grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.exportTo')}">
                   <i class="mdi mdi-export font-24"></i>
                   <span class="caret hidden"></span>
               </span>
           <ul class="dropdown-menu save-list dropdown-content dropdown-menu-right ddm-padding" id="exportTypes">
               <strong class="font-12 title-spacing"><g:message code="app.label.export"/></strong>
               <li><g:link controller="literatureAlert" action="exportReport" class="m-r-30"
                           params="${[outputFormat: com.rxlogix.enums.ReportFormat.DOCX, configId: configId, isArchived: detailsisArchived]}">
                   <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/><g:message
                           code="save.as.word"/>
               </g:link>
               </li>
               <li><g:link controller="literatureAlert" action="exportReport" class="m-r-30"
                           params="${[outputFormat: ReportFormat.XLSX, configId: configId, isArchived: detailsisArchived]}">
                   <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                           code="save.as.excel"/>
               </g:link></li>
               <li><g:link controller="literatureAlert" action="exportReport" class="m-r-30"
                           params="${[outputFormat: ReportFormat.PDF, configId: configId, isArchived: detailsisArchived]}">
                   <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                           code="save.as.pdf"/>
               </g:link></li>
           </ul>
           </span>
         </span>

           <!------------==================-------Disposition code start------==========------------------->
           <span class="grid-pin collapse theme-color dropdown" id="dispositionTypes2">
               <span  tabindex="0" class="dropdown-toggle grid-menu-tooltip dropbtn" data-toggle="dropdown" accesskey="s" title="${message(code: 'app.label.disposition')}">
                   <i class="mdi mdi-checkbox-marked-outline font-24"></i>
               </span>
               <ul class="dropdown-menu dropdown-content dropdown-menu-right export-type-list disposition-ico disposition-li" id="dispositionTypes"></ul>
           </span>
           <!------------==================-------Disposition code end------==========-------------------->

           <!------------==================-------Filter code start------==========------------------->
           <a href="javascript:void(0)" class="grid-pin collapse theme-color" id="ic-toggle-column-filters" data-fieldconfigurationbarid="quantitativeFields" data-pagetype="quantitative_alert" title="${message(code: 'app.label.filter')}">
               <i class="mdi mdi-filter-outline font-24"></i>
           </a>
           <!------------==================-------Filter code end--------==========------------------->
       <!------------==================-------Alert level disposition code start------==========------------------->
               <span class="grid-pin collapse theme-color dropdown ${buttonClass}" id="ic-alert-level-disposition">
                   <span tabindex="0" class="dropdown-toggle grid-menu-tooltip" data-toggle="dropdown" accesskey="l" title="${message(code: 'alert.level.disposition.label')}">
                       <i class="mdi mdi-alpha-d-box-outline font-24"></i>
                   </span>
                   <ul class="dropdown-menu dropdown-content col-min-150 dropdown-menu-right ul-ddm-child alert-disp-dmm literature-ico-circle">
                       <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
                   </ul>
               </span>
       <!---------------===============--------Alert level disposition code closed---------==========-------------->


       <!-------------------------------====================----------------pinned code closed-------------------------==================--------------------------->

<!------------------------------------------------------------list menu code start----------------------------------------------------------------------------->
<div class="dropdown grid-icon ico-menu pull-right" id="reportIconMenu">
    <span class="dropdown-toggle" data-toggle="dropdown" style="float: left;">
        <i class="mdi mdi-format-list-bulleted mr-10 font-24 pull-right mr-10" style="margin-right:5px"></i>
    </span>
    <ul class="dropdown-menu ul-ddm">
        <!------------------------------------------------------------export to menu code start----------------------------------------------------------------------->
        <li class="li-pin-width">
            <a class="test text-left-prop ul-ddm-hide m-l-5 abstract-menu" href="#" data-checked ="false">
                <g:checkBox style="position:relative; bottom:0.25rem;" name="detailed-view-checkbox" checked="${false}" accesskey="v"/>
                <span style="position:relative; left:0.2rem"><g:message code="app.label.literature.abstract.view" /></span>
            </a>
            <a href="javascript:void(0)" class="text-right-prop" >
                <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-target="#ic-abstractview" data-toggle="collapse"  data-title="<g:message code="app.label.literature.abstract.view" />"></span>
            </a>
        </li>


        <li class="li-pin-width">
            <a class="test text-left-prop ul-ddm-hide" href="#" id="configureLiteratureFields" data-fieldconfigurationbarid="literatureFields"
               data-backdrop="true" data-container="columnList-container" accesskey="c" title="Choose Fields">
                <i class="mdi mdi-settings-outline"></i>
                <span tabindex="0">
                    Field Selection
                </span>
            </a>
            <a href="javascript:void(0)" class="text-right-prop">
            <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-toggle="collapse" data-id="#ic-configureLiteratureFields" data-title="Field selection"></span>
            </a>
        </li>

        <li class="li-pin-width dropdown-submenu">
            <a class="test text-left-prop" href="#">
                <i class="mdi mdi-export"></i> <span tabindex="0" class="dropdown-toggle exportPanel grid-menu-tooltip" data-toggle="dropdown"  accesskey="x">
                Export To
            </span>
            </a>
            <a href="javascript:void(0)" class="text-right-prop">
                <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-title="Export To" data-id="#exportTypes"></span>
            </a>

            <ul class="dropdown-menu export-type-list ul-ddm-child" id="exportTypes">
                <strong class="font-12"><g:message code="app.label.export"/></strong>
                <li><g:link controller="literatureAlert" action="exportReport" class="m-r-30"
                            params="${[outputFormat: com.rxlogix.enums.ReportFormat.DOCX, configId: configId, isArchived: detailsisArchived]}">
                    <img src="/signal/assets/word-icon.png" class="m-r-10" height="16" width="16"/><g:message
                            code="save.as.word"/>
                </g:link>
                </li>
                <li><g:link controller="literatureAlert" action="exportReport" class="m-r-30"
                            params="${[outputFormat: ReportFormat.XLSX, configId: configId, isArchived: detailsisArchived]}">
                    <img src="/signal/assets/excel.gif" class="m-r-10" height="16" width="16"/><g:message
                            code="save.as.excel"/>
                </g:link></li>
                <li><g:link controller="literatureAlert" action="exportReport" class="m-r-30"
                            params="${[outputFormat: ReportFormat.PDF, configId: configId, isArchived: detailsisArchived]}">
                    <img src="/signal/assets/pdf-icon.jpg" class="m-r-10" height="16" width="16"/><g:message
                            code="save.as.pdf"/>
                </g:link></li>
            </ul>
        </li>
        <!-----------------export to menu code end------------------------------->
        <li class="dropdown-submenu li-pin-width">
            <a class="test text-left-prop"  href="#">
                <i class="mdi mdi-checkbox-marked-outline" title="${message(code :'alert.level.disposition.label')}"></i>
                <span class="dropdown-toggle grid-menu-tooltip" data-target="#bulkDispositionPopover" role="button"
                      data-toggle="modal-popover" data-placement="left" accesskey="l">
                    <g:message code="app.label.disposition" />
                </span>
            </a>
            <a href="javascript:void(0)" class="text-right-prop">
                <span class="pin-unpin-rotate pull-right mdi mdi-pin" data-toggle="collapse" title="Pin to top" data-id="#dispositionTypes2" data-title="Disposition"></span>
            </a>
            <ul class="dropdown-menu export-type-list disposition-ico" id="dispositionTypes"></ul>
        </li>

        <g:if test="${!isArchived}">
            <li class="li-pin-width">
                <a class="test text-left-prop" id="toggle-column-filters" href="#">
                    <i class="mdi mdi-filter-outline"></i>
                    <span data-title="Filters" class="test" tabindex="0" role="button" accesskey="y">
                        Filters</span>
                </a>
                <a href="javascript:void(0)" class="text-right-prop">
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-toggle="collapse" data-id="#ic-toggle-column-filters" data-title="Filters"></span>
                </a>
            </li>
        </g:if>


        <g:if test="${alertDispositionList}">
            <li class="dropdown-submenu li-pin-width ${buttonClass}">
                <a tabindex="0" class="m-r-10 grid-menu-tooltip text-left-prop">
                    <i class="mdi mdi-alpha-d-box-outline" title="${message(code :'alert.level.disposition.label')}"></i>
                    <span data-target="#bulkDispositionPopover" role="button" data-toggle="modal-popover" data-placement="left" accesskey="l">
                        Alert level disposition
                    </span>
                </a>
                <a href="javascript:void(0)" class="text-right-prop" data-toggle="collapse">
                    <span class="pin-unpin-rotate pull-right mdi mdi-pin" title="Pin to top" data-id="#ic-alert-level-disposition" data-title="Alert level Disposition"></span>
                </a>
                <ul class="dropdown-menu col-min-150 alert-disp-dmm">
                    <g:render template="/includes/popover/bulkDispositionSelect" model="[alertDispositionList: alertDispositionList]"/>
                </ul>
            </li>
        </g:if>

    </ul>
</div>
<!-------------------------------------list menu ended---------------------------------------------------------------------------------------------->
          </div>
        </div>
    </div>

           %{--
                <div class="pull-right dropdown-menu menu-large" aria-labelledby="dropdownMenu1">
                    <div class="rxmain-container-dropdown">
                        <div>
                            <table id="tableColumns" class="table no-border">
                                <thead><tr><th>${message(code: 'app.label.name')}</th><th>${message(code: 'app.label.show')}</th>
                                </tr></thead>
                            </table>
                        </div>
                    </div>
                </div>

            </div>--}%
        </div>
    </div>

    <div class="row">
        <div class="col-md-12">
            <table id="alertsDetailsTable" class="auto-scale row-border no-shadow hover DTFC_Cloned">
                <thead>
                <tr id="literatureDetailsTableRow">
                    <th data-field="checkbox">
                        <input id="select-all" type="checkbox"/>
                    </th>
                    <th data-field="dropdown">
                        <div class="th-label"></div>
                    </th>
                    <g:if test="${isPriorityEnabled}">
                        <th data-field="priority">
                            <g:message code="app.label.literature.details.column.priority"/>
                        </th>
                    </g:if>
                    <th data-field="actions">
                        <g:message code="app.label.literature.details.column.actions"/>
                    </th>
                    <th data-field="articleId">
                        <div class="th-label" data-field="articleId"><g:message
                                code="app.label.literature.details.column.articleId"/></div>
                    </th>
                    <th data-field="alertTags">
                        <div class="th-label" data-field="alertTags"><g:message
                                code="app.label.literature.details.column.tags"/></div>
                    </th>

                    <th data-field="title">
                        <div class="th-label" data-field="title"><g:message
                                code="app.label.literature.details.column.title"/></div>
                    </th>
                    <th data-field="authors">
                        <div class="th-label" data-field="authors"><g:message
                                code="app.label.literature.details.column.authors"/></div>
                    </th>
                    <th data-field="publicationDate">
                        <div class="th-label" data-field="publicationDate"><g:message
                                code="app.label.literature.details.column.publication.date"/></div>
                    </th>
                    <th data-field="signal">
                        <div class="th-label" data-field="signal"><g:message
                                code="app.label.literature.details.column.signalsAndTopics"/></div>
                    </th>
                    <th data-field="currentDisposition">
                        <div class="th-label" data-field="currentDisposition"><g:message
                                code="app.label.disposition.to"/></div>
                    </th>
                    <th data-field="assignedTo">
                        <div class="th-label" data-field="assignedTo"><g:message code="app.label.assigned.to"/></div>
                    </th>
                    <th data-field="productName">
                        <div class="th-label" data-field="productName"><g:message
                                code="app.label.literature.details.column.productName"/></div>
                    </th>
                    <th data-field="eventName">
                        <div class="th-label" data-field="eventName"><g:message
                                code="app.label.literature.details.column.eventName"/></div>
                    </th>
                    <th data-field="disposition">
                        <div class="th-label" data-field="disposition"><g:message
                                code="app.label.current.disposition"/></div>
                    </th>
                </tr>
                </thead>
            </table>

        </div>
    </div>

</div>


