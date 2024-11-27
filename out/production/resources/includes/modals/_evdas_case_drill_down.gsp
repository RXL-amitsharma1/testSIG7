<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.rxlogix.Constants;com.rxlogix.enums.ReportFormat" %>
<div class="modal modal-xlg fade modal-wide" id="evdas-case-drill-down-modal">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">

                <button type="button" class="close font-24" data-dismiss="modal"
                        aria-label="Close" id="cross-icon"><span aria-hidden="true">&times;</span></button>

                <label class="modal-title" style="float: left;">Cases Drill Down</label>

                <div class="ico-menu export-evdas-drill-down" id="exportTypesEvdas" >
                    <span class="pull-right" title="Export to Excel" >
                        <span tabindex="0" class="grid-menu-tooltip" title="Export to Excel">
                        <g:if test="${alertType == Constants.AlertConfigType.EVDAS_ALERT||alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                            <g:link controller="evdasAlert" action="exportEVDASCaseListColumns"
                                    params="${[outputFormat: ReportFormat.XLSX, id : id,alertType:alertType]}">
                                <i class="mdi mdi-export font-24 lh-1" style="color: #353d43"></i>
                            </g:link>
                        </g:if>
                        <g:else>
                            <g:link controller="evdasOnDemandAlert" action="exportEVDASOnDemandCaseListColumns"
                                    params="${[outputFormat: ReportFormat.XLSX, id : id,alertType:alertType]}">
                                <i class="mdi mdi-export font-24 lh-1" style="color: #353d43"></i>
                            </g:link>
                        </g:else>
                        </span>
                    </span>
                </div>

                <g:if test="${alertType == Constants.AlertConfigType.EVDAS_ALERT ||alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT}">
                    <a href="#" class="add-case-attachment pv-ic grid-menu-tooltip" title="Add to Attachments" tabindex="0"
                       id="attach-case-list"><i
                            class="mdi mdi-plus font-24 "></i></a>
                </g:if>
            </div>

            <div id="evdas-drill-down-table-container" class="panel-body"></div>

        </div>
    </div>
</div>


<script>
    $('#attach-case-list').click(function () {
        $('#evdasAttachmentModal').css({display: 'block'});
        $('#evdas-attachment-box').val('');
    });
</script>