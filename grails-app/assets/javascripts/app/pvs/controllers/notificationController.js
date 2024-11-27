pvReports.factory('alertService', function($rootScope,$timeout) {
    var alertService = {};
    $rootScope.alerts = [];
    alertService.add = function(type, msg) {
        $rootScope.alerts.push({'type': type, 'msg': msg});
    }
    alertService.closeAlert = function(index) {

        $rootScope.alerts.splice(index,1);

    };

    return alertService;
});
pvReports.controller('NotifyController',['$scope', 'alertService', function($rootScope, alertService){

    $rootScope.closeAlert = alertService.closeAlert;
}]);

pvReports.controller('userNotificationController',['$scope', '$http', 'alertService', '$timeout', '$interval', '$rootScope', function($scope, $http, alertService, $timeout,$interval, $rootScope){

    var getUserNotification=function() {
        $http({method: 'GET', url: 'notifications/user/1'}).done(function (data) {
            if ($rootScope.alerts.length === 0 && data.length > 0) {
                var usernotice = data.pop();
                alertService.add("info", usernotice.message);
                $http({method: 'DELETE', url: 'notifications/'+usernotice.id}).done(function (data) {
                    console.log("sucessfully deleted"+usernotice.id);
                });
            }

        }).fail(function (status) {
            console.log("Got an error = " + status + " canceling timer");
            $interval.cancel(timer);
            timer=undefined;
        });
    }
    var timer = $interval(getUserNotification, 15000);
}]);


