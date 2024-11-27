<div class="modal fade" id="modalRuleOrder" role="dialog">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title rxmain-container-header-label">Ordering Rules Priority</h4>
            </div>
            <g:form name="ruleRankOrder" controller="businessConfiguration" action="rulesOrder">
                <div class="modal-body">
                    <ol id="ruleOrderList">
                    </ol>
                </div>
                <g:hiddenField id="ruleOrderArray" name="ruleOrderArray" value=""/>
                <div class="modal-footer">
                    <g:submitButton id="saveRuleOrder" name="saveRuleOrder" value="Save" class="btn btn-primary"/>
                </div>
            </g:form>
        </div>
    </div>
</div>
<style>
    ol > li{
        font-size: large;
        margin: 20px;
        padding: 10px 0px;
        text-align: center;
    }
</style>