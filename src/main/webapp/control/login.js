function LoginControl() {
	this.username = $("#username").val();
	this.password = $("#password").val();
	this.pubkey = "";

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
		$.ajax({
			type : 'POST',
			url : "api/login/getpubkey",
			data : null,
			success : function(data) {
				if (!data) {
					toastr.error("啊哦，出错了！");
				} else {
					obj.pubkey = data;
					obj.loginAuth();
				}
			},
			error : function() {
				toastr.error("啊哦，出错了！");
			}
		});
	}

	this.loginAuth = function() {
		var uname = RSAEncrypt.encrypt(this.pubkey, this.username);
		var pwd = RSAEncrypt.encrypt(this.pubkey, this.password);
		$.ajax({
			type : 'POST',
			url : "api/login/auth",
			data : {
				username : uname,
				password : pwd
			},
			success : function(data) {
				if (data) {
					if (data.status != 200) {
						toastr.error(data.resultdesc);
					} else {
						CookieUtil.delCookie("pcdtoken");
						CookieUtil.setCookie("pcdtoken", data.result);
						window.location.href = "main.html";
					}
				}
			},
			error : function() {
				toastr.error("啊哦，出错了！");
			}
		});
	}
}

$("#loginButton").click(function() {
	new LoginControl().login();
});

