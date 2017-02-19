var app = angular.module('sharePageApp', []);
var postCfg = {
	headers: {
		'Content-Type': 'application/x-www-form-urlencoded'
	},
	transformRequest: function (data) {
		return $.param(data);
	}
};

// 主面板
app.controller("container", function ($scope, $http) {
    $scope.cancel = false;
    $scope.extractAuth = function(){
        alert();
    }
});