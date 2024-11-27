<div class="horizontalRuleFull"></div>
<div class="greyText">
  <div>
    <small class="text-muted">
      <span id="dateCreated-label" class="property-label">
        <g:message code="app.label.dateCreated"/>:
      </span>
      <span class="property-value" aria-labelledby="dateCreated-label">
          <g:render template="/includes/widgets/dateDisplayWithTimezone" model="[date:theInstance?.dateCreated]"/>
      </span>

      <span id="createdBy-label" class="property-label" style="margin-left: 20px;">
       <g:message code="app.label.createdBy"/>:
      </span>
      <span class="property-value" aria-labelledby="dateCreated-label">
        ${theInstance?.createdBy}
      </span>

      <span id="lastUpdated-label" class="property-label" style="margin-left: 50px;">
       <g:message code="app.label.modifiedDate"/>:
      </span>
      <span class="property-value" aria-labelledby="lastUpdated-label">
          <g:render template="/includes/widgets/dateDisplayWithTimezone" model="[date:theInstance?.lastUpdated]"/>
      </span>

      <span id="modifiedBy-label" class="property-label" style="margin-left: 20px;">
        <g:message code="app.label.modifiedBy"/>:
      </span>
      <span class="property-value" aria-labelledby="dateCreated-label">
          ${theInstance?.modifiedBy}
      </span>
    </small>
  </div>
</div>