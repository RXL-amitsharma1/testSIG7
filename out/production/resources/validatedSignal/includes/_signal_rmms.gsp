<%@ page import="com.rxlogix.util.DateUtil;grails.converters.JSON" %>
<div class="panel-collapse rxmain-container-content pv-scrollable-dt rxmain-container-show collapse in" id="accordion-pvs-rmms">
    <table id="signal-rmms-table" class="dataTable table table-striped row-border hover no-footer">
        <thead>
        <style>
        .webui-popover.top>.webui-arrow, .webui-popover.top-right>.webui-arrow, .webui-popover.top-left>.webui-arrow {
            display: none;
        }
        </style>
        <tr class="relative-position">
            <th class="col-sm-1"><label><g:message code="signal.rmms.label.type"/><span class="required-indicator">*</span></label></th>
            <th class="col-sm-1-half"><label><g:message code="app.label.qualitative.details.column.country"/></label></th>
            <th class="col-sm-1-half"><label><g:message code="signal.rmms.label.description"/></label></th>
            <th class="col-md-1-half"><label><g:message code="signal.rmms.label.fileName"/></label></th>
            <th class="col-md-1-half"><label><g:message code="signal.rmms.label.resp"/><span class="required-indicator">*</span></label></th>
            <th class="col-md-1-half"><label><g:message code="signal.rmms.label.status"/><span class="required-indicator">*</span></label></th>
            <th class="col-sm-1 col-min-100"><label><g:message code="signal.rmms.label.dueDate"/><span class="required-indicator">*</span></label></th>
            <th class="col-md-1 col-min-100"></th>
        </tr>
        </thead>
        <tbody></tbody>
        <tfoot class="newRow">
        <tr data-id="signal-rmms-table" class="relative-position">
            <td class="flag-select">
                <div class="rmm-type row" id="rmmTypeSelect">
                        <g:select name="rmmType" class="form-control rmmType"
                                  from="${JSON.parse(rmmType as String)}" noSelection="['': 'Select']"/>
                </div>
            </td>
            <td class="addCountry">
                    <input type="text" id="addNewCountry" class="form-control " name="addNewCountry" maxlength="4000" style="width: 80%" disabled>
            </td>
            <td >
                <div class="textarea-ext">
                    <g:textArea id="signalRMMDescription" type="text" class="form-control description comment min-height" style="width:100%" name="description"  rows="1"/>
                    <a class="btn-text-ext openStatusComment" href="javascript:void(0);" tabindex="0"
                       title="Open in extended form">
                        <i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
                </div>
            </td>
            <td >
                <div class="file-uploader" data-provides="fileupload">
                    <div class="input-group" style="width: 100%">
                        <input type="text" class="form-control " id="attachmentFilePathRmm" placeholder="Attach a file"
                               name="assessment-file" value="" title="" style="width: 100%">
                        <span class="input-group-btn ">
                            <button class="browse btn btn-primary btn-file-upload allow-rmm-edit" type="button"
                                    data-toggle="modal" data-target="#rmmAttachmentFileModal">
                                <i class="glyphicon glyphicon-search"></i>
                            </button>
                        </span>

                    </div>
                </div>
            </td>
            <td >
                <g:initializeAssignToElement assignedToId="rmmResp" isLabel="false" isTags="true" bean="${signalRmm}"/>
            </td>
            <td class="col-md-1">
                <g:select name="rmmStatus" class="form-control status"
                          from="${JSON.parse(rmmStatus as String)}"
                          noSelection="['': 'Select']"/>
            </td>
            <td>
                <div class="fuelux">
                    <div class="datepicker toolbarInline rmmsDatePicker">
                        <div class="input-group">
                            <input placeholder="Select Date"
                                   class="form-control due-date"
                                   name="Date" type="text"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </td>

            <td class=" text-center">
                <a href="javascript:void(0);" title="Save" class="table-row-saved saveRecord hidden-ic pv-ic">
                    <i class="mdi mdi-check" aria-hidden="true"></i>
                </a>
                <a href='javascript:void(0);' title='Delete' class='table-row-del remove-rmm-row hidden-ic'>
                    <i class='mdi mdi-close' aria-hidden='true'></i>
                </a>
            </td>

        </tr>
        </tfoot>

    </table>
</div>

<g:render template="/validatedSignal/includes/countrySelectModal"/>