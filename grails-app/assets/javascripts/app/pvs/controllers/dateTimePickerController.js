
pvReports.controller('rxtimePickerController',function($scope,$rootScope){
    $scope.mytime = new Date();

    var timehr=new Date().getHours();

    if(timehr==23){
        $scope.data.hstep='00';
    }else{
        $scope.data.hstep = timehr+1;
    }
    $scope.data.mstep = '00';

    $scope.options = {
        hstep: ['00','01', '02', '03','04','05','06','07','08','09',10,11,12,13,14,15,16,17,18,19,20,21,22,23],
        mstep: ['00','01', '02', '05', 10, 15,20, 25, 30,35,40,45,50,55]
    };

    $scope.ismeridian = true;
    $scope.toggleMode = function() {
        $scope.ismeridian = ! $scope.ismeridian;
    };

    $scope.changed = function () {
        console.log('Time changed to: ' + $scope.mytime);
    };

    $scope.changehstep=function(hstep){
        $rootScope.$broadcast("hstep",hstep );
    }
    $scope.changemstep=function(mstep){
        $rootScope.$broadcast("mstep",mstep );
    }
    $scope.clear = function() {
        $scope.mytime = null;
    };

});
pvReports.controller("rxDatePickerController" ,function ($scope) {

    $scope.clear = function () {
        $scope.data.dt = null;
    };

    $scope.disabled = function(date, mode) {
        return ( mode === 'day' && ( date.getDay() === 0 || date.getDay() === 6 ) );
    };

    $scope.toggleMin = function() {
        $scope.minDate = $scope.minDate ? null : new Date()
    };
    $scope.toggleMin();

    $scope.open = function($event) {
        $event.preventDefault();
        $event.stopPropagation();

        $scope.opened = true;
    };




});