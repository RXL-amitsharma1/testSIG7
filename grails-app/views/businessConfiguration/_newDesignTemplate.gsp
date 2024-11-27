<div id="toAddContainer" class="toAddContainer business-configuration-row">
        <div class="col-xs-3">
            <select name="category" class="form-control dependent-select select1 expressionCategory" id="category"
                    data-next-select="select2">
                <option value="">Select Category</option>
                <option value="ALGORITHM" class="aggregate-category">Algorithm</option>
                <option value="QUERY_CRITERIA" class="single-case-category">Query Criteria</option>
                <option value="COUNTS" class="aggregate-category">Counts</option>
                <option value="REVIEW_STATE">Last Review Disposition</option>
                <option value="SIGNAL_REVIEW_STATE">Last Signal Review Disposition</option>
                <option value="LAST_REVIEW_DURATION">Last Review Duration</option>
            </select>
        </div>

        <div class="col-xs-3">
            <div>
            <select name="algorithm" id="attribute" class="form-control select2 disable-select expressionAttribute"
                    data-next-select="select3"
                    data-prev-select="select1">
                <option value="">Select</option>
            </select>
            </div>
            <div class="hidden isProductSpecific">
                <input type="checkbox" name="isProductSpecific" id="isProductSpecific" autocomplete="off">
                <label for="isProductSpecific">
                    <g:message code="app.label.product.specific"/>
                </label>
            </div>
        </div>

    <div class="col-xs-1" style="width:4px; margin-top:10px;">
        <div style="cursor: not-allowed; color: #C8C8C8" class="fa fa-bars fa-stack percentValue" data-toggle="modal" data-target="#percentModal" onclick="percentClicked(this)"></div>
    </div>

    <div class="col-xs-2">
            <select name="algorithm" id="operator"
                    class="form-control dependent-sdelect select3 disable-select expressionOp"
                    data-next-select="select4"
                    data-prev-select="select2">
                <option value="">Select Operator</option>
                <option value="${com.rxlogix.Constants.Operators.EQUAL_TO}">Equal To</option>
                <option class="not-equal-option hidden-item" value="${com.rxlogix.Constants.Operators.NOT_EQUAL_TO}">Not Equal To</option>
                <option class="numeric-option hidden-item hidd" value="${com.rxlogix.Constants.Operators.GREATER_THAN_OR_EQAUL_TO}">Greater Than Equal To</option>
                <option class="numeric-option hidden-item" value="${com.rxlogix.Constants.Operators.GREATER_THAN}">Greater Than</option>
                <option class="numeric-option hidden-item" value="${com.rxlogix.Constants.Operators.LESS_THAN_OR_EQUAL_TO}">Less Than Equal To</option>
                <option class="numeric-option hidden-item" value="${com.rxlogix.Constants.Operators.LESS_THAN}">Less Than</option>
                <option class="empty-option hidden-item" value="${com.rxlogix.Constants.Operators.IS_EMPTY}">Is Empty</option>
                <option class="empty-option hidden-item" value="${com.rxlogix.Constants.Operators.IS_NOT_EMPTY}">Is Not Empty</option>
                <option class="txt-option hidden-item" value="${com.rxlogix.Constants.Operators.CONTAINS}">Contains</option>
                <option class="txt-option hidden-item" value="${com.rxlogix.Constants.Operators.DOES_NOT_CONTAIN}">Does Not Contain</option>
                <option class="txt-option hidden-item" value="${com.rxlogix.Constants.Operators.DOES_NOT_START_WITH}">Does Not Start With</option>
                <option class="txt-option hidden-item" value="${com.rxlogix.Constants.Operators.END_WITH}">Ends With</option>
                <option class="txt-option hidden-item" value="${com.rxlogix.Constants.Operators.DOES_NOT_END_WITH}">Does Not End With</option>
            </select>
        </div>

    <div class="col-xs-3 threshold">
            <g:textField name="threshold" id="threshold" class="form-control disable-select last-element expressionThreshold select4"
                         data-prev-select="select3" placeholder="threshold"/>
        </div>
    <div class="col-xs-3 threshold-combo hide">
        <g:select name="threshold" id="threshold-combo" class="form-control last-element expressionThreshold-combo select4"
                  data-prev-select="select3" optionKey="id" optionValue="" value="" from="" disabled="true">
            <option value=""><g:message code="app.lable.select.threshold"/></option>
        </g:select>
    </div>

    <div class="checkBoxDiv col-xs-7 hide">
        <div class="col-sm-3" style="width: 20%">
            <input type="checkbox" id="splitSignalToPt" class="lastSignalReview" name="splitSignalToPt"
                          value="true"/>
            <label>
                <g:message code="app.label.business.configuration.splitSignalToPt.Label"/>
            </label>
        </div>

        <div class="col-sm-3" style="width: 25%">
            <input type="checkbox" id="assClosedSignal" class="lastSignalReview" name="assClosedSignal"
                          value="true"/>
            <label>
                Associate PEC’s to Closed Signals
            </label>
        </div>

        <div class="col-sm-3" style="width: 30%">
            <input type="checkbox" id="assMultSignal" class="lastSignalReview" name="assMultSignal"
                          value="true"/>
            <label>
                Associate Multiple Signals to PEC’s
            </label>
        </div>
    </div>


</div>