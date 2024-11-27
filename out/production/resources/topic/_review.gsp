<g:render template="/validatedSignal/includes/adHocReview" model="[isTopic: true]"/>
<asset:javascript src="app/pvs/validated_signal/review.js"/>
<g:render template="/validatedSignal/includes/aggregateReview"/>
<g:render template="/validatedSignal/includes/singleReview"/>
<input type="hidden" id="topicId" value="${topic.id}" />
