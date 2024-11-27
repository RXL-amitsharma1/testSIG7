<g:set var="reminderTime" value="${grailsApplication.config.springsession.timeout.dialogue.display.time}"/>
<g:set var="appTimeoutInterval" value="${grailsApplication.config.springsession.timeout.interval}"/>
<g:set var="appBaseUrl" value="${grailsApplication.config.grails.serverURL}"/>
<g:set var="logoutUri"
       value="${grailsApplication.config.grails.plugin.springsecurity.saml.active ? grailsApplication.config.saml.local.logout.url : grailsApplication.config.grails.plugin.springsecurity.logout.uri}"/>
<g:set var="keepAliveInterval" value="${(session.maxInactiveInterval % 3) * 60}"/>
<asset:javascript src="store.modern.min.js"/>
%{--Added customised version of jquery-idleTimeout.js--}%
<asset:javascript src="jquery/customise-jquery-idleTimeout.js"/>
<g:javascript>
    $(document).ready(function() {
      $(document).idleTimeout({
      redirectUrl:  "${appBaseUrl}/${logoutUri}",
      idleTimeLimit: ${ appTimeoutInterval * 60},
      sessionKeepAliveUrl: "${appBaseUrl}/keep-alive",
      sessionKeepAliveTimer: ${keepAliveInterval ?: 60},
      enableDialog: true,
      dialogDisplayLimit: ${reminderTime * 60},
      activityEvents: 'click keypress scroll wheel mousewheel mousemove',
      customCallback: function() {
          var forms = $('form');
            if(forms.find('.changed-input').length){
                alert($.i18n._('navigateAwayChangesLostMessage'));
                clearFormInputsChangeFlag(forms);
            }
      }
      });
    });
</g:javascript>
