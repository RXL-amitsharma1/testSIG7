<div id="customSQLValueContainer" class="customSQLValueContainer">
    <div class="errorMessageOperator">Enter Valid Number</div>
    <div class="col-xs-2 sqlKeyContainer">
        <p class="inputSQLKey text-right"></p>
    </div>

    <div class="col-xs-5 expressionsNoPad">
        <g:textField name="inputSQLValue" class="form-control inputSQLValue" placeholder="Value"></g:textField>
    </div>
    <div class="col-xs-1" hidden="hidden">
        <input class="${type}Value" value="${qev?.value}" />
        <input name="inputSQLKey" class="${type}Key" value="${qev?.key}" />
    </div>
</div>