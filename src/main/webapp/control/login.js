function loginAuth(data) {
	var uname = RSAEncrypt.encrypt(data, $("#username").val());
	var pwd = RSAEncrypt.encrypt(data, $("#password").val());
	var reqdata = {
		username : uname,
		password : pwd
	};
	CustomAjax.ajaxRequest("api/login/auth", "POST", reqdata, loginSuccess);
}
function loginSuccess(data) {
	if (data) {
		CookieUtil.delCookie("pcdtoken");
		CookieUtil.setCookie("pcdtoken", data.result);
		window.location.href = "main.html";
	}
}

$("#loginButton").click(function() {
	if (!$("#username").val()) {
		toastr.error($("#username").attr('placeholder'));
		return;
	}
	if (!$("#password").val()) {
		toastr.error($("#password").attr('placeholder'));
		return;
	}
	CustomAjax.ajaxRequest("api/login/auth", "GET", null, loginAuth);
});
