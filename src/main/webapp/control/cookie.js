var CookieUtil = {
	delCookie : function(key) {
		var exp = new Date();
		exp.setTime(exp.getTime() - 1);
		var cval = CookieUtil.getCookie(name);
		if (cval != null) {
			document.cookie = key + "=" + cval + ";expires="
					+ exp.toGMTString();
		}
	},
	setCookie : function(key, value) {
		CookieUtil.delCookie(key);
		document.cookie = key + "=" + value + ";";
	},
	getCookie : function(key) {
		var arr, reg = new RegExp("(^| )" + key + "=([^;]*)(;|$)");
		if (arr = document.cookie.match(reg))
			return unescape(arr[2]);
		else
			return null;
	}
}