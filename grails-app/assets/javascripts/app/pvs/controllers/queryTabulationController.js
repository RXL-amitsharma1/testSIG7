pvReports.factory('citems', function() {
    var citems={};
    citems.name;
    citems.dateRange;
    citems.tempelId;
    citems.queryId;
    citems.language;

    citems.setName=function(name){
        this.name=name
    }
    citems.setlanguage=function(name){
        this.language=name
    }


    citems.setDateRange=function(date){
        this.dateRange=date
    }
    citems.settempelId=function(tem){
        this.tempelId=tem
    }
    citems.setqueryId=function(query){
        this.queryId=query
    }
    return citems;

});

pvReports.controller('QueryController',['$scope','$http','$location','$filter','citems', function($scope, $http, $location, $filter,citems){
    $scope.show = false;
    $scope.dateRangeValues = [
        {name:"Last day", value: "day"},
        {name:"Last week", value: "week"},
        {name:"Last month", value: "month"},
        {name:"Last 3 months", value: "3months"},
        {name:"Last 6 months", value: "6months"},
        {name:"Last year", value: "year"},
        {name:"Last 3 years", value: "3years"},
        {name:"Last 30 days", value: "30days"},
        {name:"Last 90 days", value: "90days"},
        {name:"Last 180 days", value: "180days"},
        {name:"Last 365 days", value: "365days"},
        {name:"All dates", value: "all"}
    ];

    $scope.searchForQueries = function (val) {
        return $http.get('queries/search', {
            params: {
                q: val
            }
        }).then(function (queryNames) {
            var queries = [];
            angular.forEach(queryNames.data, function (item) {
                if (queries.length < 4) {
                    queries.push(item);
                }
            });
            return queries;
        });
    }
    var routeToRunLaters = window.location.href.split("#");
    var templateID = routeToRunLaters[1].split(":");

    $scope.getRunLater = function ($criteriaService) {
        if($scope.name == undefined){
            $scope.name = "scheduledReport"
        }
        citems.setName($scope.name);
        citems.setDateRange($scope.dateRange)

        citems.setqueryId($scope.queryName.id)
        var queryId = 'queryId=' + $scope.queryName.id;

        $location.path("/templates/schedule:" + templateID[1] + "&" + queryId);
    }
    $scope.runReportNow = function() {
        if($scope.name == undefined) {
            $scope.name ='Report';
        }
        var name = $scope.name;
        var queryId = $scope.queryName.id;
        var datetime = $filter('date')(new Date(), 'yyyy-MM-dd HH:mm:ss.000');
        var tempId = templateID[1].split("=");

        $http({
            url: 'runNow',
            method: 'POST',
            headers : { 'Content-Type': 'application/x-www-form-urlencoded' },
            params: {'template.id': tempId[1], 'query.id': queryId, 'nextRunDate': datetime, 'reportDateRange': $scope.dateRange, 'name': name}

        }).done(function (data, status, headers, config) {
            $location.path("/signal/coolreport:reportId=" + data.id);
        }).fail(function (data, status, headers, config) {
        })
        //  }
    }
}]);




alert-inbox