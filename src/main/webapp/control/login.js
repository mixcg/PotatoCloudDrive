var app = angular.module('loginFormApp', []);

var postCfg = {
	headers : {
		'Content-Type' : 'application/x-www-form-urlencoded'
	},
	transformRequest : function(data) {
		return $.param(data);
	}
};
app.controller("login", function($scope, $http) {
	$scope.loginAuth = function() {
		var requrl = "api/login";
		$http.get(requrl, null).then(function success(response) {
			if (response) {
				var data = {};
				data.username = RSAEncrypt.encrypt(response.data.result, $scope.username);
				data.password = RSAEncrypt.encrypt(response.data.result, $scope.password);
				$http.post(requrl, data, postCfg).then(function success(response) {
					if (response) {
						CookieUtil.delCookie("pcdtoken");
						CookieUtil.setCookie("pcdtoken", response.data.result);
						CookieUtil.setCookie("username", $scope.username);
						window.location.href = "main.html";
					}
				}, function error(response) {
					toastr.error(response.data);
				});
			} else {
				toastr.error("啊哦，出错了！");
			}
		}, function error(response) {
			toastr.error("啊哦，出错了！");
		});
	}
});
