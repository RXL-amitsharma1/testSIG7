pvReports.controller('addEditTemplateTabs', ['$scope', function ($scope) {
    $scope.tabs = [{
        title: 'Case Line Listing',
        url: 'app/templates/caselinelisting.html'
    }, {
        title: 'CIOMS II Line Listing',
        url: 'app/templates/ciomsiilinelisting.html'
    }, {
        title: 'Data Tabulation',
        url: 'app/templates/datatabulation.html'
    }];

    $scope.currentTab = 'app/templates/caselinelisting.html';

    $scope.onClickTab = function (tab) {
        $scope.currentTab = tab.url;
    }

    $scope.isActiveTab = function(tabUrl) {
        return tabUrl == $scope.currentTab;
    }
}]);