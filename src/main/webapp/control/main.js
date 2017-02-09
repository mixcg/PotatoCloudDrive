/**
 * 网盘----------------------------
 */
var token = new Object();
var fileListTRDefault = null;

$(function() {
	sessionStorage.clear();
	sessionStorage.setItem("uploadArray", new Array());
	setInterval("refTransList()", 1000);
	token.pcdtoken = CookieUtil.getCookie("pcdtoken");
	ReqFileList();
	$('#myModal').on('show.bs.modal', function(e) {
		$("#myModal div[class='row']").each(function() {
			$(this).addClass("hidden");
		});
	})
	$('#myModal').on('hidden.bs.modal', function(e) {
		$("#modalSubmit").removeClass("copy");
		$("#modalSubmit").unbind();
		$("#modalSubmit").text("确定");
	})
});
// 传输列表刷新
function refTransList() {
	var uploadArray = sessionStorage.getItem("uploadArray");
	if (uploadArray) {
		uploadArray = JSON.parse(uploadArray);
		$(".badge").text(uploadArray.length);
		for (var i = 0; i < uploadArray.length; i++) {
			var filename = uploadArray[i];
			console.log(filename + " " +sessionStorage.getItem(filename));
		}
	}
}

function loadDrive() {
	$("#wangpan").show();
	$("#transportlist").hide();
	$("#sharelist").hide();
	ReqFileList();
}
// 请求文件列表
function ReqFileList(filepath) {
	if (!filepath) {
		filepath = "api/files"
		CustomAjax.ajaxRequest(filepath, "GET", token, loadFileList);
		$("#dirul li:gt(0)").remove();
	} else {
		filepath = "api/files/" + filepath;
		CustomAjax.ajaxRequest(filepath, "GET", token, loadFileList);
	}
}
// 加载文件列表
function loadFileList(data) {
	var datas = data.result;
	if (!fileListTRDefault) {
		fileListTRDefault = $('#filetable tbody tr').eq(0);
	}
	$('#filetable tbody').empty();
	for (var i = 0; i < datas.length; i++) {
		var tmptr = fileListTRDefault.clone();
		var tmpdata = datas[i];
		if (tmpdata.filetype == "文件夹") {
			tmptr.find("td").eq(1).find("a").html(tmpdata.filename);
			tmptr.find("td").eq(2).find("a[type='download']").hide();
			tmptr.find("td").eq(2).find("a[type='play']").hide();
		} else {
			tmptr.find("td").eq(0).find("img").attr("src", "img/" + tmpdata.filetype + ".png");
			tmptr.find("td").eq(1).html(tmpdata.filename);
			if (tmpdata.filetype.toLowerCase() != "mp4") {
				tmptr.find("td").eq(2).find("a[type='play']").hide();
			}
		}
		tmptr.find("td").eq(3).html(tmpdata.filetype);
		tmptr.find("td").eq(4).html(tmpdata.descsize);
		tmptr.find("td").eq(5).html(tmpdata.lastmodifiedtime);
		tmptr.attr("path", tmpdata.base64filepath);
		tmptr.attr("filename", tmpdata.filename);
		$('#filetable tbody').append(tmptr);
	}
	$("#filetable td").hover(function() {
		$(this).parent().find("td").eq(2).find("div").css('visibility', 'visible');
	}, function() {
		$(this).parent().find("td").eq(2).find("div").css('visibility', 'hidden');
	})
}
// 进入指定目录
function EnterDir(label) {
	var path = $(label).parent().parent().attr("path");
	var li = $("#dirul").find("li").eq(0).clone();
	li.find("a").attr("path", path).html($(label).html());
	$("#dirul").append(li);
	ReqFileList(path);
}
// 返回到指定目录
function BackDir(label) {
	var index = $(label).parent().index();
	$("#dirul li:gt(" + index + ")").remove();
	var path = $(label).attr("path");
	ReqFileList(path);
}
// 新建文件或文件夹
function createNewFile(label) {
	var type = $(label).attr("type");
	var modalTitle = $("#myModal h4[class='modal-title']");
	modalTitle.html("新建文件（夹）");
	var singleRowInput = $("#myModal div[type='singleRowInput']");
	singleRowInput.find("input").val("");
	$("#myModal").modal();
	singleRowInput.removeClass("hidden");
	$("#modalSubmit").on(
			'click',
			function() {
				var thisdir = $("#dirul li:last").find("a").attr("path");
				var newfilename = singleRowInput.find("input").val();
				newfilename = Base64.encodeURI(newfilename);
				if (!thisdir) {
					CustomAjax.ajaxRequest("api/files/" + type + "/" + newfilename, "POST", token, refreshThisDir);
				} else {
					CustomAjax.ajaxRequest("api/files/" + thisdir + "/" + type + "/" + newfilename, "POST", token,
							refreshThisDir);
				}
			});
}
// 关闭窗体,刷新当前目录信息
function refreshThisDir(data) {
	$("#myModal").modal('hide');
	var thisdir = $("#dirul li:last").find("a").attr("path");
	ReqFileList(thisdir);
}

// 文件操作
function FileOperate(label) {
	var filename = $(label).parents("tr").attr("filename");
	var path = $(label).parents("tr").attr("path");
	var dotype = $(label).attr("type");
	switch (dotype) {
	case "download":
		var filepath = "";
		CustomAjax.ajaxRequest(filepath, "GET", token, null);
		break;
	case "play":
		window.location.href = 'play.html?url=' + path;
		break;
	case "del":
		var modalTitle = $("#myModal h4[class='modal-title']");
		modalTitle.html("删除文件（夹）：" + filename);
		var deltip = $("#myModal div[type='deltip']");
		$("#myModal").modal();
		deltip.removeClass("hidden");
		$("#modalSubmit").on('click', function() {
			CustomAjax.ajaxRequest("api/files/" + path, "DELETE", token, refreshThisDir);
		});
		break;
	case "mv":
		break;
	case "rename":
		var type = $(label).attr("type");
		var modalTitle = $("#myModal h4[class='modal-title']");
		modalTitle.html("重命名文件（夹）：" + filename);
		var singleRowInput = $("#myModal div[type='singleRowInput']");
		singleRowInput.find("input").val("");
		$("#myModal").modal();
		singleRowInput.removeClass("hidden");
		$("#modalSubmit").on('click', function() {
			var newfilename = singleRowInput.find("input").val();
			newfilename = Base64.encodeURI(newfilename);
			CustomAjax.ajaxRequest("api/files/" + path + "/" + newfilename, "PUT", token, refreshThisDir);
		});
		break;
	case "share":
		CustomAjax.ajaxRequest("api/share/" + path, "POST", token, shareSuccess);
		break;
	}
}

function shareSuccess(data) {
	var modalTitle = $("#myModal h4[class='modal-title']");
	modalTitle.html("分享成功！");
	var sharefile = data.result;
	$("#sharelink").val(getRootPath() + sharefile.id);
	$("#sharepwd").val(sharefile.password);

	$("#modalSubmit").text("复制");
	$("#modalSubmit").addClass("copy");
	new Clipboard('.copy', {
		text : function(trigger) {
			// TODO finish copy
			toastr.info("复制到剪切板成功！");
			return "aaaaa";
		}
	});
	var shareDiv = $("#myModal div[type='shareDiv']");
	$("#myModal").modal();
	shareDiv.removeClass("hidden");
}
// 上传文件
function uploadFile() {
	$("#file_upload").unbind();
	$("#file_upload").change(function() {
		var file = this.files[0];
		var xhr = new XMLHttpRequest();
		var fd = new FormData();
		fd.append("fileName", file);
		// 监听事件
		xhr.upload.addEventListener("progress", function(evt) {
			if (evt.lengthComputable) {
				// evt.loaded：文件上传的大小 evt.total：文件总的大小
				var percentComplete = Math.round((evt.loaded) * 100 / evt.total);
				sessionStorage.setItem(file.name, percentComplete);
			}
		}, false);
		// 发送文件和表单自定义参数
		xhr.open("POST", "api/files", true);
		xhr.send(fd);
		if (file) {
			var uploadArray = sessionStorage.getItem("uploadArray");
			if (!uploadArray) {
				uploadArray = new Array();
			} else {
				uploadArray = JSON.parse(uploadArray);
			}
			uploadArray.push(file.name);
			sessionStorage.setItem("uploadArray", JSON.stringify(uploadArray));
		}
	})
	$("#file_upload").trigger('click');
}
/**
 * 传输列表
 */
function loadTransport(){
	$("#wangpan").hide();
	$("#transportlist").show();
	$("#sharelist").hide();
}

/**
 * 分享列表----------------------------
 */
var shareListTRDefault = null;
function loadShareList() {
	$("#wangpan").hide();
	$("#transportlist").hide();
	$("#sharelist").show();
	CustomAjax.ajaxRequest("api/share", "GET", token, loadShareFileList);
	new Clipboard('.copy').destroy();
	new Clipboard('.copy', {
		text : function(trigger) {
			var tr = $(trigger).parents("tr");
			var id = tr.attr("id");
			var password = tr.attr("password");
			toastr.info("复制到剪切板成功！");
			return id + password;
		}
	});
}
// 加载分享列表
function loadShareFileList(data) {
	var datas = data.result;
	if (!shareListTRDefault) {
		shareListTRDefault = $('#sharefiletable tbody tr').eq(0);
	}
	$('#sharefiletable tbody').empty();
	for (var i = 0; i < datas.length; i++) {
		var tmptr = shareListTRDefault.clone();
		var tmpdata = datas[i];
		if (tmpdata.filetype != "文件夹") {
			tmptr.find("td").eq(0).find("img").attr("src", "img/" + tmpdata.filetype + ".png");
		}
		tmptr.find("td").eq(1).html(tmpdata.filename);
		tmptr.find("td").eq(2).html(tmpdata.filetype);
		tmptr.find("td").eq(3).html(tmpdata.sharedate);
		tmptr.find("td").eq(4).html(tmpdata.downloadtimes);
		tmptr.attr("id", tmpdata.id);
		tmptr.attr("password", tmpdata.password);
		tmptr.attr("filepath", tmpdata.filepath);
		$('#sharefiletable tbody').append(tmptr);
	}
}
// 取消分享
function cancelShare(label) {
	var id = $(label).parents("tr").attr("id");
	CustomAjax.ajaxRequest("api/share/" + id, "DELETE", token, cancelShareSuccess);
}
function cancelShareSuccess(data) {
	CustomAjax.ajaxRequest("api/share", "GET", token, loadShareFileList);
}
// // 查看分享
// function watchShareFile(label) {
// var filepath = $(label).parents("tr").attr("filepath");
// window.location.href = "main.html#" + filepath;
// location.reload();
// }
/**
 * 退出登录----------------------------
 */
function logout() {
	CustomAjax.ajaxRequest("api/login/logout", "DELETE", token, logoutSuccess);
}
function logoutSuccess() {
	CookieUtil.delCookie("pcdtoken");
	window.location.href = "login.html";
}
/**
 * other ---------------------------
 */
// 图片加载失败处理
function ImgError(obj) {
	obj.src = 'img/file.png'
	obj.onerror = null
}
// 获取项目根路径
function getRootPath() {
	var curWwwPath = window.document.location.href;
	var pathName = window.document.location.pathname;
	var pos = curWwwPath.indexOf(pathName);
	var localhostPaht = curWwwPath.substring(0, pos);
	var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
	return (localhostPaht + projectName);
}
// // 获取#后面的值
// function getUrlParam() {
// var curWwwPath = window.document.location.href;
// var arg = curWwwPath.split("#");
// if (arg.length > 1) {
// return arg[1];
// }
// return "";
// }
