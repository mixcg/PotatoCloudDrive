function LoginControl() {
	this.login = function() {
		var flag = this.logincheck();
		if (flag) {
			this.getPubkey();
		}
	};

	this.logincheck = function() {
		if (!$("#username").val()) {
			toastr.error($("#username").attr('placeholder'));
			return false;
		}
		if (!$("#password").val()) {
			toastr.error($("#password").attr('placeholder'));
			return false;
		}
		return true;
	}

	this.getPubkey = function() {
		var obj = this;
		CustomAjax.ajaxRequest("api/login/auth", "GET", null, this.loginAuth);
	}

	this.loginAuth = function(data) {
		var uname = RSAEncrypt.encrypt(data, $("#username").val());
		var pwd = RSAEncrypt.encrypt(data, $("#password").val());
		var reqdata = {
			username : uname,
			password : pwd
		};
		CustomAjax.ajaxRequest("api/login/auth", "POST", reqdata,
				this.loginSuccess);
	}
	this.loginSuccess = function(data) {
		if (data) {
			CookieUtil.delCookie("pcdtoken");
			CookieUtil.setCookie("pcdtoken", data.result);
			window.location.href = "main.html";
		}
	}
}

$("#loginButton").click(function() {
	new LoginControl().login();
});
