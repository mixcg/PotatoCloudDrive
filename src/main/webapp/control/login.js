function LoginControl() {
	this.username = $("#username").val();
	this.password = $("#password").val();

	this.login = function() {
		var flag = this.logincheck();
		if (flag) {
			this.getPubkey();
		}
	};

	this.logincheck = function() {
		if (!this.username) {
			toastr.error($("#username").attr('placeholder'));
			return false;
		}
		if (!this.password) {
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
		var uname = RSAEncrypt.encrypt(data, this.username);
		var pwd = RSAEncrypt.encrypt(data, this.password);
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
