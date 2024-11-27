<g:set var="sessionTimeoutInSeconds" value="${grailsApplication.config.springsession.timeout.interval}"/>
<g:set var="dialogueDisplayTimeInMinutes"
       value="${grailsApplication.config.springsession.timeout.dialogue.display.time}"/>
<style>
.swal2-content {
    border-bottom: 1px solid #ccc;
    padding-bottom: 15px;
}
.swal2-header {
    background: #e9e9e9;
    font-size: 10px;
    padding: 5px 40px 5px 0px;
    border-radius: 2px;
    border: 1px solid #e4dfdf;
}
.swal2-popup {
    padding: 5px;
    height: auto;
    width: 300px;
    top: auto;
    left: auto;
}
.swal2-title {
    margin: -6px 0px -6px;
    padding: 0;
    color: #595959;
    font-size: 13px;
    font-weight: 600;
    text-align: left !important;
    color: #333333;
    font-family: Arial, Helvetica, sans-serif;
}
.swal2-content {
    padding: 10px 10px 20px 12px !important;
    font-size: 13px !important;
    text-align: left !important;
}
.swal2-actions button {
    background-color: #efefef !important;
    color: #1e1e1e !important;
    border: 1px solid #979797 !important;
    font-family: Arial, Helvetica, sans-serif;
}
.swal2-actions {
    margin: 5px;
    font-size: 11px !important;
    padding-right: 20px;
    justify-content: right;
}
.swal2-styled:focus {
    box-shadow: none !important;
}
.swal2-styled {
    padding: 3px 10px !important;
}
</style>
<asset:javascript src="swal2/sweetalert2.all.min.js"/>
<g:javascript>
    var sessionTimeoutInSeconds =${sessionTimeoutInSeconds}
    var dialogueDisplayTimeInSeconds =${dialogueDisplayTimeInMinutes*60}
    localStorage.removeItem("SessionCounter")
    localStorage.removeItem("stayLogin")
    localStorage.removeItem("logout")
    localStorage.removeItem("SessionCounterUpdatedOn")
    localStorage.setItem("SessionCounter", sessionTimeoutInSeconds);
    localStorage.setItem("stayLogin", false);
    localStorage.setItem("logout", false);
    var popUpShown = false,logoutExecuted = false;
    var timerStoper;
       $(document).ready(function () {
           startEventListener()
       timerStoper=setInterval(timeCounter, 1000);
       })

       function timeCounter() {
           var lastUpdated = localStorage.getItem("SessionCounterUpdatedOn"),now = new Date(), check = false;
           if (lastUpdated == null) {
               localStorage.setItem("SessionCounterUpdatedOn", now);
           } else if (now.getTime() - new Date(lastUpdated).getTime() >= 1000) {
               localStorage.setItem("SessionCounterUpdatedOn", now);
               check = true;
           }
           var isStayLogedIn=JSON.parse(localStorage.getItem("stayLogin"));
           var isLoggedOut=JSON.parse(localStorage.getItem("logout"));
           if(isStayLogedIn){
                Swal.close()
                popUpShown=false;
                localStorage.setItem("stayLogin", false);
           }
           if(isLoggedOut && !JSON.parse(logoutExecuted)){
                logout()
           }
           if (check) {
               if (sessionTimeoutInSeconds > 0) {
                   if (parseInt(localStorage.getItem("SessionCounter"), 10) > 0) {
                       sessionTimeoutInSeconds = parseInt(localStorage.getItem("SessionCounter"), 10);
                   }
                   sessionTimeoutInSeconds--;
                   localStorage.setItem("SessionCounter", sessionTimeoutInSeconds);
               }
               else if(!JSON.parse(logoutExecuted)) {
                   logout()
               }
           } else {
               sessionTimeoutInSeconds = parseInt(localStorage.getItem("SessionCounter"), 10);
           }
           displayPopup()
       }

   function logout(){
        clearInterval(timerStoper)
        logoutExecuted=true;
        localStorage.removeItem('i18keys');
        localStorage.setItem("logout", true);
        if(samlEnabled){
            window.location.href=window.location.origin+samlURL
        } else {
            window.location.href=window.location.origin+"/signal/logout"
        }

   }

   function stayLogin(){
         // Call keep me logged in api to make the session active in the server side
         $.get(window.location.origin+"/signal/user/keepAlive");
         localStorage.setItem("stayLogin", true);
         Swal.close()
         localStorage.setItem("SessionCounter", ${sessionTimeoutInSeconds});
         clearInterval(timerStoper)
         timerStoper=setInterval(timeCounter, 1000);
         popUpShown = false;
   }

   function displayPopup() {
      if(dialogueDisplayTimeInSeconds>=sessionTimeoutInSeconds && sessionTimeoutInSeconds>=0){
            if(!JSON.parse(popUpShown)){
           Swal.fire({
           title: "PV Signal Session Expiration Warning",
           html:true,
           text: " ",
           showCancelButton: true,
           cancelButtonText: 'Log Out Now',
           confirmButtonText: 'Stay Logged In',
           allowOutsideClick: false,
           closeOnClickOutside: false,
           onOpen:function() {
           $(".swal2-cancel").click(function() {
           logout()
           })
           $(".swal2-confirm").click(function() {
           stayLogin()
         })
         }
       })
        popUpShown = true;
      }
      }else{
          Swal.close();
          popUpShown = false;
      }
      $("#swal2-content").html("Because you have been inactive, your session is about to expire.<br><br>Time remaining: "+toHHMMSS(sessionTimeoutInSeconds))
   }

   function startEventListener(event){
         wrapper.addEventListener('click', resetTime);
         wrapper.addEventListener('mousemove', resetTime);
         wrapper.addEventListener('keyup', resetTime);
         wrapper.addEventListener('keydown', resetTime);
         wrapper.addEventListener('keypress', resetTime);
   }

   function resetTime(){
         wrapper.removeEventListener("click", startEventListener());
         wrapper.removeEventListener("mousemove", startEventListener());
         wrapper.removeEventListener("keyup", startEventListener());
         wrapper.removeEventListener("keydown", startEventListener());
         wrapper.removeEventListener("keypress", startEventListener());
         sessionTimeoutInSeconds= ${sessionTimeoutInSeconds}
        localStorage.setItem("SessionCounter", ${sessionTimeoutInSeconds});
   }

    function toHHMMSS(totalNumberOfSeconds) {
        var hours = parseInt( totalNumberOfSeconds / 3600 );
        var minutes = parseInt( (totalNumberOfSeconds - (hours * 3600)) / 60 );
        var seconds = Math.floor((totalNumberOfSeconds - ((hours * 3600) + (minutes * 60))));
        if(totalNumberOfSeconds>3600){
            return (hours < 10 ? "0" + hours : hours) + ":" + (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds  < 10 ? "0" + seconds : seconds);
        }else{
        return  (minutes < 10 ? "0" + minutes : minutes) + ":" + (seconds  < 10 ? "0" + seconds : seconds);
        }
    }
</g:javascript>