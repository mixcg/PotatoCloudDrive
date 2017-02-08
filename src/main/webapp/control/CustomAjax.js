var CustomAjax = {
	/**
	 * <pre>
	 * url: 请求url
	 * reqtype: 请求类型
	 * reqdata: 请求数据
	 * callbackObj: 回调函数
	 * </pre>
	 */
	ajaxRequest : function(url, reqtype, reqdata, callbackFunc) {
		$.ajax({
			type : reqtype,
			url : url,
			data : reqdata,
			success : function(returndata) {
				toastr.info(returndata.resultdesc);
				if (callbackFunc) {
					callbackFunc(returndata);
				} 
			},
			error : function(jqXHR) {
				toastr.error(jqXHR.responseText);
				if(jqXHR.status == 401){
					setTimeout("window.location.href = 'login.html'", 1000 )
				}
			}
		});
	}
}