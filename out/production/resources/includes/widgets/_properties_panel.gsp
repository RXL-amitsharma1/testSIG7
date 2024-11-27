<asset:stylesheet href="properties_panel.css"/>
<asset:javascript src="app/pvs/widgets/properties_panel.js" />
<div class="container-fluid p-0">
    <g:each in="${rows}" var="row">
        <div class="row prop prop-label bg-grey">
            <g:each in="${row}" var="col">
                <div class="col-xs-${col[4]} p-tb-5">
                    <div class="">${col[0]}</div>
                </div>
            </g:each>
        </div>
        <div class="row prop word-wrap-break-word">
            <g:each in="${row}" var="col">
                <div class="col-xs-${col[4]} prop-value text-${col[3]} ${col[4]}">
                    <g:if test="${col[2]}">
                        ${col[2]}
                    </g:if>
                    <g:else>
                        &nbsp;
                    </g:else>
                </div>
            </g:each>
        </div>
    </g:each>
</div>