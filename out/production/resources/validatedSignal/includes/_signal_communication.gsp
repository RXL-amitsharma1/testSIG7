<%@ page import="com.rxlogix.util.DateUtil;grails.converters.JSON" %>
<div class="panel-collapse rxmain-container-content pv-scrollable-dt rxmain-container-show collapse in" id="accordion-pvs-communication">
    <table id="signal-communication-table" class="dataTable table table-striped row-border hover no-footer">
        <thead>
        <tr class="relative-position">
            <th class="col-sm-1"><label><g:message code="signal.rmms.label.type"/><span class="required-indicator">*</span></label></th>
            <th class="col-sm-1-half"><label><g:message code="app.label.qualitative.details.column.country"/></label></th>
            <th class="col-sm-1-half"><label><g:message code="signal.rmms.label.description"/></label></th>
            <th class="col-md-1-half"><label><g:message code="signal.rmms.label.fileName"/></label></th>
            <th class="col-md-1-half col-min-100"><label><g:message code="signal.rmms.label.resp"/></label></th>
            <th class="col-md-1-half"><label><g:message code="signal.rmms.label.status"/></label></th>
            <th class="col-sm-1 col-min-100"><label><g:message code="signal.rmms.label.dueDate"/></label></th>
            <th class="col-md-1-half"><label class="email-sent-comm" ><g:message code="signal.rmms.label.emailSent"/></label></th>
            <th class="col-md-1 col-min-100"></th>
        </tr>
        </thead>
        <tbody></tbody>
        <tfoot class="newRowCommunication">
        <tr data-id="signal-communication-table" class="relative-position">
            <td class="col-sm-1 flag-select" style="width:11%;">
                <div class="rmm-type row" id="communicationTypeSelect">
                    <div>
                        <g:select name="rmmType" class="form-control rmmType"
                                  from="${JSON.parse(communicationType as String)}" noSelection="['': 'Select']"/>
                    </div>
                </div>
            </td>
            <td class="col-md-2 addCountry">
                <input type="text" id="addNewCountryComm" class="form-control " name="addNewCountryComm" maxlength="4000" style="width: 80%" disabled>
            </td>
            <td class="col-md-2">
                <div class="textarea-ext">
                    <g:textArea type="text" class="form-control description comment min-height" style="width:100%" name='description' id="signalCommunicationDescription" maxlength="8000" rows="1"/>
                    <a class="btn-text-ext openStatusComment" href="javascript:void(0);" tabindex="0"
                       title="Open in extended form">
                        <i class="mdi mdi-arrow-expand font-20 blue-1"></i></a>
                </div>
            </td>
            <td class="col-md-2">
                <div class="file-uploader" data-provides="fileupload">
                    <div class="input-group" style="width: 100%">
                        <input type="text" class="form-control " id="attachmentFilePathComm" placeholder="Attach a file"
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
            <td class="col-md-2">
                <g:initializeAssignToElement assignedToId="communicationResp" isLabel="false" isTags="true" bean="${signalRmm}"/>
            </td>
            <td>
                <g:select name="rmmStatus" class="form-control status"
                          from="${JSON.parse(rmmStatus as String)}"
                          noSelection="['': 'Select']"/>
            </td>
            <td >
                <div class="fuelux">
                    <div class="datepicker toolbarInline communicationDatePicker">
                        <div class="input-group">
                            <input placeholder="Select Date"
                                   class="form-control due-date"
                                   name="Date" type="text"/>
                            <g:render template="/includes/widgets/datePickerTemplate"/>
                        </div>
                    </div>
                </div>
            </td>

            <td class="col-md-1-half">
            </td>

            <td class=" text-center">
                <a href="javascript:void(0);" title="Save" class="table-row-saved saveRecord hidden-ic pv-ic">
                    <i class="mdi mdi-check" aria-hidden="true"></i>
                </a>
                <a href='javascript:void(0);' title='Delete' class='table-row-del remove-comm-row hidden-ic'>
                    <i class='mdi mdi-close' aria-hidden='true'></i>
                </a>
            </td>

        </tr>
        </tfoot>

    </table>
</div>

<g:render template="/validatedSignal/includes/countrySelectModal"/>