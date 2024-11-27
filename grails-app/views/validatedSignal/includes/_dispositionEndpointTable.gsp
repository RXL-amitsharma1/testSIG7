<table id="disposition-endOfReview-table" class="dataTable row-border hover no-footer">
    <thead>
    <tr>
        <th class="sorting_disabled"><g:message code="controlPanel.signal.dispositions"/></th>
        <th class="sorting_disabled"><g:message code="controlPanel.signal.status"/></th>
        <th class="sorting_disabled ${buttonClass}"></th>
    </tr>
    </thead>
    <tbody>
    <tr role="row" id="addToRowMapping" class="odd signalHistoryRow" style="display: none">
        <td class="col-md-2">
            <span>
                <select class="signalDispositionSelect form-control" name="signalDispositionSelect" multiple>
                    <g:each in="${dispositionList}" var="dispo">
                        <option value="${dispo.value}">${dispo.text}</option>
                    </g:each>
                </select>
            </span>
        </td>
        <td class="col-md-2">
            <g:select name="signalStatus" class="form-control signalStatus"
                      from="${selectableEndPointValues}" noSelection="['': '-Select Status -']"/>
        </td>
        <td class="icons-class col-md-1 text-right">
            <a tabindex="0" title="Save" class="saveDispositionEndpoints showOk" style="cursor: pointer"><i
                    class="mdi mdi-check grey-2"></i></a>
            <a href="#disposition-endOfReview-table" tabindex="0" title="Delete"
               class="btn-add-row cancelEndPointUpdate showOk" style="cursor: pointer"><i
                    class="mdi mdi-close font-16 grey-1"></i></a>
        </td>
    </tr>
    <g:each status="i" in="${finalList}" var="dispositionEndPoint">
    <tr role="row" class="${(i % 2) == 0 ? 'even' : 'odd'} signalHistoryRow">
        <td class="col-md-2">
            <span>
                <select class="signalDispositionSelect form-control" name="signalDispositionSelect" data-reload-value="${dispositionEndPoint.dispositions*.value?.join(",")}" multiple>
                    <g:each in="${dispositionEndPoint.dispositions}" var="dispo">
                        <option value="${dispo.value}" selected>${dispo.text}</option>
                    </g:each>
                    <g:each in="${dispositionList}" var="dispo">
                        <option value="${dispo.value}">${dispo.text}</option>
                    </g:each>
                </select>
            </span>                        </td>
        <td class="col-md-2">
            <select class="signalStatus form-control" data-actual="${dispositionEndPoint.endPoint}" name="signalStatus">
                <option value="${dispositionEndPoint.endPoint}" selected>${dispositionEndPoint.endPoint}</option>
                <g:each in="${selectableEndPointValues}" var="endPoint">
                    <option value="${endPoint}">${endPoint}</option>
                </g:each>
            </select>
        </td>
        <td class="icons-class col-md-1 text-right">
            <a tabindex="0" title="Edit" class="editDispositionEndpoints showOk" style="cursor: pointer"><i
                    class="mdi mdi-pencil grey-2"></i></a>
            <a tabindex="0" title="Save" class="saveDispositionEndpoints noShow" style="display: none; cursor: pointer;"><i
                    class="mdi mdi-check grey-2"></i></a>
            <a href="#disposition-endOfReview-table" tabindex="0" title="Delete" class="btn-add-row cancelEndPointUpdate showOk"><i
                    class="mdi mdi-close font-16 grey-1"></i></a>
        </td>
    </tr>
    </g:each>
    </tbody>
</table>