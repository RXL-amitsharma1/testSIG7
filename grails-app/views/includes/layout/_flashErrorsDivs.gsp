<g:unless test="${flash.error}">
<g:hasErrors bean="${theInstance}">
    <div class="alert alert-danger alert-dismissible" role="alert">
        <button type="button" class="close" data-dismiss="alert">
            <span onclick="this.parentNode.parentNode.remove(); return false;">x</span>
            <span class="sr-only"><g:message code="default.button.close.label" /></span>
        </button>
        <g:renderErrors bean="${theInstance}" as="list" codec="none"/>
    </div>
</g:hasErrors>
</g:unless>
<g:if test="${flash.message}">
    <div class="alert alert-success alert-dismissible" role="alert" style="word-break: break-all">
        <button type="button" class="close" data-dismiss="alert">
            <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
            <span class="sr-only"><g:message code="default.button.close.label" /></span>
        </button>
        ${raw(flash.message)}
    </div>
</g:if>

<g:if test="${flash.warn}">
    <div class="alert alert-warning alert-dismissible" role="alert" style="word-break: break-all">
        <button type="button" class="close" data-dismiss="alert">
            <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
            <span class="sr-only"><g:message code="default.button.close.label" /></span>
        </button>
        <g:if test="${flash.warn.contains('<linkQuery>')}">
            ${flash.warn.substring(0, flash.warn.indexOf('<linkQuery>'))}
            <a href="${flash.warn.substring(flash.warn.indexOf('<linkQuery>') + 11)}"><g:message code="see.details" /></a>
        </g:if>
        <g:else>
            ${raw(flash.warn)}
        </g:else>
    </div>
</g:if>

<g:if test="${flash.error}">
    <div class="alert alert-danger alert-dismissible" role="alert" style="word-break: break-all">
        <button type="button" class="close" data-dismiss="alert">
            <span  onclick="this.parentNode.parentNode.remove(); return false;">x</span>
            <span class="sr-only"><g:message code="default.button.close.label" /></span>
        </button>
        <g:if test="${flash.error}">
            <g:if test="${flash.error.contains('<linkQuery>')}">
                ${flash.error.substring(0, flash.error.indexOf('<linkQuery>'))}
                <a href="${flash.error.substring(flash.error.indexOf('<linkQuery>') + 11)}"><g:message code="see.details" /></a>
            </g:if>
            <g:else>
                ${raw(flash.error)}
            </g:else>
        </g:if>
    </div>
</g:if>

