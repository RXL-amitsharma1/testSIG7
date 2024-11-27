<asset:javascript src="app/pvs/validated_signal/review.js"/>
<div class="rxmain-container panel-group pv-max-scrollable-table">
    <g:render template="includes/aggregateReview" model="[validatedAggHelpMap:validatedAggHelpMap,labelConfigJson : labelConfigJson, labelConfigNew: labelConfigNew,labelConfig:labelConfig,labelConfigCopy:labelConfigCopy,labelConfigCopyJson:labelConfigCopyJson,labelConfigKeyId:labelConfigKeyId,hyperlinkConfiguration:hyperlinkConfiguration]"/>
    <g:render template="includes/singleReview" model="[validatedSingleHelpMap:validatedSingleHelpMap,columnLabelForSCA: columnLabelForSCA]"/>
    <g:render template="includes/literatureReview" model="[validatedLiteratureHelpMap:validatedLiteratureHelpMap]"/>
    <g:render template="includes/adHocReview" model="[isTopic: false]"/>
</div>
<input type="hidden" id="signalId" value="${signal.id}" />

