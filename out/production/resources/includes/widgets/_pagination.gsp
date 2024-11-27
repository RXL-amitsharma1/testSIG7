<%@ page import="com.rxlogix.Constants" %>

<g:if test="${theInstanceTotal > Constants.Search.MAX_SEARCH_RESULTS}">
    <div class="paginateButtons">
        <g:message code="page" />:
        <g:paginate total="${theInstanceTotal}" params="${params}"/>
    </div>
</g:if>