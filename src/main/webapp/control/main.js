var errorEvent = function (response) {
	toastr.error(response.data);
	if (response.status == 401) {
		setTimeout("window.location.href = 'login.html'", 1000)
	}
}

var app = angular.module('mainPageApp', []);
var postCfg = {
	headers: {
		'Content-Type': 'application/x-www-form-urlencoded'
	},
	transformRequest: function (data) {
		return $.param(data);
	}
};

app.directive('fallbackSrc', function () {
	var fallbackSrc = {
		link: function postLink(scope, iElement, iAttrs) {
			iElement.bind('error', function () {
				angular.element(this).attr("src", iAttrs.fallbackSrc);
			});
		}
	}
	return fallbackSrc;
});

// 主面板
app.controller("container", function ($scope, $http) {
	$scope.uploadlist = new Array();
	$scope.downloadlist = new Array();
	// token
	var token = {};
	token.pcdtoken = CookieUtil.getCookie("pcdtoken");
	$scope.token = token;
	$scope.showCtrl = 3;
	$scope.showCtrl = 1;
	// 显示网盘
	$scope.showFileList = function () {
		$scope.showCtrl = 1;
	}
	// 显示传输
	$scope.showTransport = function () {
		$scope.showCtrl = 0;
	}
	// 显示分享
	$scope.showShare = function () {
		$scope.showCtrl = -1;
	}
});
// 文件列表
app.controller("filelist", function ($scope, $http) {
	// 加载指定目录
	function loadDir(requrl) {
		if (requrl) {
			requrl = "api/files/" + requrl;
		} else {
			requrl = "api/files";
		}
		$http.get(requrl, $scope.$parent.token).then(function success(response) {
			if (response) {
				$scope.filelist = response.data.result;
				$scope.selectedFile = null;
			}
		}, errorEvent);
	}
	// 监听页面切换显示
	$scope.$watch("showCtrl", function (showCtrlValue) {
		if (showCtrlValue == 1) {
			loadDir();
			var length = $scope.dirul.length;
			$scope.dirul.splice(1, length);
			$scope.selectedFile = null;
		}
	});
	// 文件列表对话框关闭
	$('#FileModal').on('hidden.bs.modal', function (e) {
		$scope.filename = null;
		$scope.selectedFile = null;
		$scope.FileModal = null
		$scope.shareFile = null;
	});
	// 初始化目录导航
	var navbar = new Array();
	var file = {};
	file.base64filepath = "";
	file.name = "主目录";
	navbar.push(file);
	$scope.dirul = navbar;
	// 进入指定目录
	$scope.enterDir = function (a) {
		loadDir(a.base64filepath);
		var file = {};
		file.base64filepath = a.base64filepath;
		file.name = a.filename;
		$scope.dirul.push(file);
	}
	// 返回到指定目录
	$scope.backup = function (li) {
		var index = li.$index;
		var length = $scope.dirul.length;
		$scope.dirul.splice(index + 1, length - index + 1);
		loadDir(li.li.base64filepath);
	}
	// 文件列表选中
	$scope.select = function (tr) {
		$scope.selectedFile = tr;
	}
	// 文件列表取消选择
	$scope.cancelSelect = function () {
		$scope.selectedFile = null;
	}
	// 重命名文件Modal
	$scope.renameFileModal = function () {
		var modal = {};
		modal.title = "重命名文件：" + $scope.selectedFile.filename;
		modal.type = "rename";
		$scope.FileModal = modal;
		$("#FileModal").modal();
	}
	// 删除文件Modal
	$scope.delFileModal = function () {
		var modal = {};
		modal.title = "删除文件：" + $scope.selectedFile.filename;
		modal.type = "del";
		$scope.FileModal = modal;
		$("#FileModal").modal();
	}
	// 新建文件Modal
	$scope.newFileModal = function () {
		var modal = {};
		modal.title = "新建文件：";
		modal.type = "file";
		$scope.FileModal = modal;
		$("#FileModal").modal();
	}
	// 新建文件夹Modal
	$scope.newDirModal = function () {
		var modal = {};
		modal.title = "新建文件夹：";
		modal.type = "directory";
		$scope.FileModal = modal;
		$("#FileModal").modal();
	}
	// 离线下载
	$scope.addDownloadTask = function () {
		var modal = {};
		modal.title = "离线下载：";
		modal.type = "download";
		$scope.FileModal = modal;
		$("#FileModal").modal();
	}
	// 文件分享Modal
	$scope.shareFileModal = function () {
		// TODO 完善链接
		$http.post("api/share/" + $scope.selectedFile.base64filepath, $scope.$parent.token, postCfg).then(function (response) {
			if (response) {
				response.data.result.link = response.data.result.id;
				$scope.shareFile = response.data.result;
				var modal = {};
				modal.title = "分享成功！";
				modal.type = "share";
				$scope.FileModal = modal;
				$("#FileModal").modal();
			}
		}, errorEvent);
	}
	// 文件播放
	$scope.playFile = function () {
		window.location.href = 'play.html?url=' + $scope.selectedFile.base64filepath;
	}
	// 文件下载
	$scope.dlFile = function () {
		window.location.href = 'api/files/' + $scope.selectedFile.base64filepath + "/download";
	}
	// 上传文件
	$scope.uploadFile = function () {
		$("#file_upload").unbind();
		$("#file_upload").change(function () {
			if(!this.files){
				return;
			}
			var file = this.files[0];
			var xhr = new XMLHttpRequest();
			var fd = new FormData();
			fd.append("fileName", file);
			var uploadID = new Date().getTime();
			// 监听事件
			xhr.upload.addEventListener("progress", function (evt) {
				if (evt.lengthComputable) {
					// evt.loaded：文件上传的大小 evt.total：文件总的大小
					var percentComplete = Math.round((evt.loaded) * 100 / evt.total);
					var upload = sessionStorage.getItem(uploadID);
					if (!upload) {
						upload = {};
						upload.id = uploadID;
					} else {
						upload = JSON.parse(upload);
					}
					upload.filename = file.name;
					upload.process = percentComplete;
					upload.status = 0;
					sessionStorage.setItem(uploadID, JSON.stringify(upload));
				}
			}, false);
			// 发送文件和表单自定义参数
			xhr.open("POST", "api/files", true);
			xhr.send(fd);
			xhr.onreadystatechange = function () {
				if (xhr.readyState === 4) {
					var upload = sessionStorage.getItem(uploadID);
					if (!upload) {
						upload = {};
						upload.id = uploadID;
					} else {
						upload = JSON.parse(upload);
					}
					if (xhr.status === 200) { //
						// 上传完成
						upload.status = 1;
					} else {
						// 上传出错
						upload.status = -1;
						upload.errormsg= xhr.responseText;
					}
					sessionStorage.setItem(uploadID, JSON.stringify(upload));
				}
			}
			if (file) {
				var uploadArray = sessionStorage.getItem("uploadArray");
				if (!uploadArray) {
					uploadArray = new Array();
				} else {
					uploadArray = JSON.parse(uploadArray);
				}
				uploadArray.push(uploadID);
				sessionStorage.setItem("uploadArray", JSON.stringify(uploadArray));
				$("#file_upload").val("");
			}
		})
		$("#file_upload").trigger('click');
	}
	// Modal提交
	$scope.fileModalSubmit = function (modal) {
		// TODO 移动
		var success = function (response) {
			if (response) {
				toastr.info(response.data.resultdesc);
				loadDir($scope.dirul[$scope.dirul.length - 1].base64filepath);
				$("#FileModal").modal('hide');
			}
		}
		switch (modal.type) {
			case "del":
				$http.delete("api/files/" + $scope.selectedFile.base64filepath, $scope.$parent.token, postCfg).then(success,
					errorEvent);
				break;
			case "file":
			case "directory":
				if (!$scope.filename) {
					toastr.error("请输入文件（夹）名称！");
					return;
				}
				var newfilename = Base64.encodeURI($scope.filename);
				var nowdir = $scope.dirul[$scope.dirul.length - 1].base64filepath;
				if (nowdir) {
					$http.post("api/files/" + nowdir + "/" + modal.type + "/" + newfilename, $scope.$parent.token, postCfg).then(success,
						errorEvent);
				} else {
					$http.post("api/files/" + modal.type + "/" + newfilename, $scope.$parent.token, postCfg).then(success,
						errorEvent);
				}
				break;
			case "rename":
				if (!$scope.filename) {
					toastr.error("请输入文件（夹）名称！");
					return;
				}
				var newfilename = Base64.encodeURI($scope.filename);
				$http.put("api/files/" + $scope.selectedFile.base64filepath + "/" + newfilename, $scope.$parent.token, postCfg).then(success,
					errorEvent);
				break;
			case "download":
				if (!$scope.url) {
					toastr.error("请输入下载链接！");
					return;
				}
				var url = Base64.encodeURI($scope.url);
				$http.post("api/download/" + url, $scope.$parent.token, postCfg).then(success,
					errorEvent);
				break;
		}
	}
});

// 传输列表
app.controller("transportlist", function ($scope, $http,$interval) {
	var reqFlag = true;
	
	var p = $interval(function(){
		if(reqFlag){
			loadUpload();
			reqFlag = false;
		}
		},2000);
	
	
	// 加载服务端下载任务
	function loadDownload(){
		$http.get("api/download", $scope.$parent.token).then(function success(response) {
			if (response) {
				$scope.$parent.downloadlist = response.data.result;
				reqFlag = true;
			}
		}, function (response) {
			reqFlag = true;
		});
	}
	// 获取上传任务
	function loadUpload(){
		$scope.$parent.uploadlist = [];
		var uploadArray = sessionStorage.getItem("uploadArray");
		if(uploadArray){
			uploadArray = JSON.parse(uploadArray);
			for (var i = 0; i < uploadArray.length; i++) {
				$scope.$parent.uploadlist.push(JSON.parse(sessionStorage.getItem(uploadArray[i])));
			}
		}
		loadDownload();
	}
	
	sessionStorage.clear();
	// 监听页面切换显示
	$scope.$watch("showCtrl", function (showCtrlValue) {
		if (showCtrlValue == 0) {
			$scope.ullist = $scope.$parent.uploadlist;
		}
	});
	// 监控上传任务
	$scope.$watch("uploadlist", function (uploadlist) {
		$scope.ullist = uploadlist;
	});
	// 监控下载任务
	$scope.$watch("downloadlist", function (uploadlist) {
		$scope.dllist = uploadlist;
	});
	// 清除已完成的任务
	$scope.delUpload = function(ts){
		var uploadArray = JSON.parse(sessionStorage.getItem("uploadArray"));
		for (var i = 0; i < uploadArray.length; i++) {
			if(uploadArray[i] == ts.id){
				uploadArray.splice(i, 1); 
			}
		}
		sessionStorage.setItem("uploadArray",JSON.stringify(uploadArray))
		sessionStorage.removeItem(ts.id);
	}
	var success = function(response){
		if (response) {
			toastr.info(response.data.resultdesc);
		}
	}
	// 停止下载
	$scope.stopDownload = function(dl){
		$http.patch("api/download/"+dl.taskid, $scope.$parent.token).then(success,errorEvent);
	}
	// 下载重试
	$scope.retryDownload = function(dl){
		$http.put("api/download/"+dl.taskid, $scope.$parent.token).then(success,errorEvent);
	}
	// 删除下载
	$scope.delDownload = function(dl){
		$http.delete("api/download/"+dl.taskid, $scope.$parent.token).then(success,errorEvent);
	}
});
// 分享列表
app.controller("sharelist", function ($scope, $http) {
	function loadShareList() {
		$http.get("api/share", $scope.$parent.token).then(function success(response) {
			if (response) {
				$scope.sharelist = response.data.result;
			}
		}, errorEvent);
	}

	// 监听页面切换显示
	$scope.$watch("showCtrl", function (showCtrlValue) {
		if (showCtrlValue == -1) {
			loadShareList();
		}
	});
	// 取消分享
	$scope.cancelShare = function (file) {
		$http.delete("api/share/" + file.id, $scope.$parent.token).then(function success(response) {
			if (response) {
				toastr.info(response.data.resultdesc);
				loadShareList();
			}
		}, errorEvent);
	}

	// 复制分享连接
	$scope.copyShareLink = function (file) {
		// TODO 完善链接
		new Clipboard('.copy', {
			text: function (trigger) {
				toastr.info("复制到剪切板成功！");
				return "aaaaa";
			}
		});
	}
});



// app.controller("navbar", function($navbar, $http) {
// $navbar.showDirve = function() {
// }
// $navbar.showShare = function() {
// }
// $navbar.addNewDL = function() {
//
// }
// $navbar.showTransport = function() {
// }
// });
// var token = new Object();
// var fileListTRDefault = null;
//
// $(function() {
// sessionStorage.clear();
// sessionStorage.setItem("uploadArray", new Array());
// setInterval("refTransList()", 1000);
// token.pcdtoken = CookieUtil.getCookie("pcdtoken");
// ReqFileList();
// $('#myModal').on('show.bs.modal', function(e) {
// $("#myModal div[class='row']").each(function() {
// $(this).addClass("hidden");
// });
// })
// $('#myModal').on('hidden.bs.modal', function(e) {
// $("#modalSubmit").removeClass("copy");
// $("#modalSubmit").unbind();
// $("#modalSubmit").text("确定");
// })
// });
// // 传输列表刷新
// function refTransList() {
// var uploadArray = sessionStorage.getItem("uploadArray");
// if (uploadArray) {
// uploadArray = JSON.parse(uploadArray);
// $(".badge").text(uploadArray.length);
// for (var i = 0; i < uploadArray.length; i++) {
// var filename = uploadArray[i];
// console.log(filename + " " + sessionStorage.getItem(filename));
// }
// }
// }
//
// function loadDrive() {
// $("#wangpan").show();
// $("#transportlist").hide();
// $("#sharelist").hide();
// ReqFileList();
// }
// // 请求文件列表
// function ReqFileList(filepath) {
// if (!filepath) {
// filepath = "api/files"
// CustomAjax.ajaxRequest(filepath, "GET", token, loadFileList);
// $("#dirul li:gt(0)").remove();
// } else {
// filepath = "api/files/" + filepath;
// CustomAjax.ajaxRequest(filepath, "GET", token, loadFileList);
// }
// }
// // 加载文件列表
// function loadFileList(data) {
// var datas = data.result;
// if (!fileListTRDefault) {
// fileListTRDefault = $('#filetable tbody tr').eq(0);
// }
// $('#filetable tbody').empty();
// for (var i = 0; i < datas.length; i++) {
// var tmptr = fileListTRDefault.clone();
// var tmpdata = datas[i];
// if (tmpdata.filetype == "文件夹") {
// tmptr.find("td").eq(1).find("a").html(tmpdata.filename);
// tmptr.find("td").eq(2).find("a[type='download']").hide();
// tmptr.find("td").eq(2).find("a[type='play']").hide();
// } else {
// tmptr.find("td").eq(0).find("img").attr("src", "img/" + tmpdata.filetype +
// ".png");
// tmptr.find("td").eq(1).html(tmpdata.filename);
// if (tmpdata.filetype.toLowerCase() != "mp4") {
// tmptr.find("td").eq(2).find("a[type='play']").hide();
// }
// }
// tmptr.find("td").eq(3).html(tmpdata.filetype);
// tmptr.find("td").eq(4).html(tmpdata.descsize);
// tmptr.find("td").eq(5).html(tmpdata.lastmodifiedtime);
// tmptr.attr("path", tmpdata.base64filepath);
// tmptr.attr("filename", tmpdata.filename);
// $('#filetable tbody').append(tmptr);
// }
// $("#filetable td").hover(function() {
// $(this).parent().find("td").eq(2).find("div").css('visibility', 'visible');
// }, function() {
// $(this).parent().find("td").eq(2).find("div").css('visibility', 'hidden');
// })
// }
// // 进入指定目录
// function EnterDir(label) {
// var path = $(label).parent().parent().attr("path");
// var li = $("#dirul").find("li").eq(0).clone();
// li.find("a").attr("path", path).html($(label).html());
// $("#dirul").append(li);
// ReqFileList(path);
// }
// // 返回到指定目录
// function BackDir(label) {
// var index = $(label).parent().index();
// $("#dirul li:gt(" + index + ")").remove();
// var path = $(label).attr("path");
// ReqFileList(path);
// }
// // 新建文件或文件夹
// function createNewFile(label) {
// var type = $(label).attr("type");
// var modalTitle = $("#myModal h4[class='modal-title']");
// modalTitle.html("新建文件（夹）");
// var singleRowInput = $("#myModal div[type='singleRowInput']");
// singleRowInput.find("input").val("");
// $("#myModal").modal();
// singleRowInput.removeClass("hidden");
// $("#modalSubmit").on('click', function() {
// var thisdir = $("#dirul li:last").find("a").attr("path");
// var newfilename = singleRowInput.find("input").val();
// newfilename = Base64.encodeURI(newfilename);
// if (!thisdir) {
// CustomAjax.ajaxRequest("api/files/" + type + "/" + newfilename, "POST",
// token, refreshThisDir);
// } else {
// CustomAjax.ajaxRequest("api/files/" + thisdir + "/" + type + "/" +
// newfilename, "POST", token, refreshThisDir);
// }
// });
// }
// // 关闭窗体,刷新当前目录信息
// function refreshThisDir(data) {
// $("#myModal").modal('hide');
// var thisdir = $("#dirul li:last").find("a").attr("path");
// ReqFileList(thisdir);
// }
//
// // 文件操作
// function FileOperate(label) {
// var filename = $(label).parents("tr").attr("filename");
// var path = $(label).parents("tr").attr("path");
// var dotype = $(label).attr("type");
// switch (dotype) {
// case "download":
// var filepath = "";
// CustomAjax.ajaxRequest(filepath, "GET", token, null);
// break;
// case "play":
// window.location.href = 'play.html?url=' + path;
// break;
// case "del":
// var modalTitle = $("#myModal h4[class='modal-title']");
// modalTitle.html("删除文件（夹）：" + filename);
// var deltip = $("#myModal div[type='deltip']");
// $("#myModal").modal();
// deltip.removeClass("hidden");
// $("#modalSubmit").on('click', function() {
// CustomAjax.ajaxRequest("api/files/" + path, "DELETE", token, refreshThisDir);
// });
// break;
// case "mv":
// break;
// case "rename":
// var type = $(label).attr("type");
// var modalTitle = $("#myModal h4[class='modal-title']");
// modalTitle.html("重命名文件（夹）：" + filename);
// var singleRowInput = $("#myModal div[type='singleRowInput']");
// singleRowInput.find("input").val("");
// $("#myModal").modal();
// singleRowInput.removeClass("hidden");
// $("#modalSubmit").on('click', function() {
// var newfilename = singleRowInput.find("input").val();
// newfilename = Base64.encodeURI(newfilename);
// CustomAjax.ajaxRequest("api/files/" + path + "/" + newfilename, "PUT", token,
// refreshThisDir);
// });
// break;
// case "share":
// CustomAjax.ajaxRequest("api/share/" + path, "POST", token, shareSuccess);
// break;
// }
// }
//
// function shareSuccess(data) {
// var modalTitle = $("#myModal h4[class='modal-title']");
// modalTitle.html("分享成功！");
// var sharefile = data.result;
// $("#sharelink").val(getRootPath() + sharefile.id);
// $("#sharepwd").val(sharefile.password);
//
// $("#modalSubmit").text("复制");
// $("#modalSubmit").addClass("copy");
// new Clipboard('.copy', {
// text : function(trigger) {
// // TODO finish copy
// toastr.info("复制到剪切板成功！");
// return "aaaaa";
// }
// });
// var shareDiv = $("#myModal div[type='shareDiv']");
// $("#myModal").modal();
// shareDiv.removeClass("hidden");
// }
// // 上传文件
// function uploadFile() {
// $("#file_upload").unbind();
// $("#file_upload").change(function() {
// var file = this.files[0];
// var xhr = new XMLHttpRequest();
// var fd = new FormData();
// fd.append("fileName", file);
// // 监听事件
// xhr.upload.addEventListener("progress", function(evt) {
// if (evt.lengthComputable) {
// // evt.loaded：文件上传的大小 evt.total：文件总的大小
// var percentComplete = Math.round((evt.loaded) * 100 / evt.total);
// sessionStorage.setItem(file.name, percentComplete);
// }
// }, false);
// // 发送文件和表单自定义参数
// xhr.open("POST", "api/files", true);
// xhr.send(fd);
// if (file) {
// var uploadArray = sessionStorage.getItem("uploadArray");
// if (!uploadArray) {
// uploadArray = new Array();
// } else {
// uploadArray = JSON.parse(uploadArray);
// }
// uploadArray.push(file.name);
// sessionStorage.setItem("uploadArray", JSON.stringify(uploadArray));
// }
// })
// $("#file_upload").trigger('click');
// }
// /**
// * 传输列表
// */
// function loadTransport() {
// $("#wangpan").hide();
// $("#transportlist").show();
// $("#sharelist").hide();
// }
//
// /**
// * 分享列表----------------------------
// */
// var shareListTRDefault = null;
// function loadShareList() {
// $("#wangpan").hide();
// $("#transportlist").hide();
// $("#sharelist").show();
// CustomAjax.ajaxRequest("api/share", "GET", token, loadShareFileList);
// new Clipboard('.copy').destroy();
// new Clipboard('.copy', {
// text : function(trigger) {
// var tr = $(trigger).parents("tr");
// var id = tr.attr("id");
// var password = tr.attr("password");
// toastr.info("复制到剪切板成功！");
// return id + password;
// }
// });
// }
// // 加载分享列表
// function loadShareFileList(data) {
// var datas = data.result;
// if (!shareListTRDefault) {
// shareListTRDefault = $('#sharefiletable tbody tr').eq(0);
// }
// $('#sharefiletable tbody').empty();
// for (var i = 0; i < datas.length; i++) {
// var tmptr = shareListTRDefault.clone();
// var tmpdata = datas[i];
// if (tmpdata.filetype != "文件夹") {
// tmptr.find("td").eq(0).find("img").attr("src", "img/" + tmpdata.filetype +
// ".png");
// }
// tmptr.find("td").eq(1).html(tmpdata.filename);
// tmptr.find("td").eq(2).html(tmpdata.filetype);
// tmptr.find("td").eq(3).html(tmpdata.sharedate);
// tmptr.find("td").eq(4).html(tmpdata.downloadtimes);
// tmptr.attr("id", tmpdata.id);
// tmptr.attr("password", tmpdata.password);
// tmptr.attr("filepath", tmpdata.filepath);
// $('#sharefiletable tbody').append(tmptr);
// }
// }
// // 取消分享
// function cancelShare(label) {
// var id = $(label).parents("tr").attr("id");
// CustomAjax.ajaxRequest("api/share/" + id, "DELETE", token,
// cancelShareSuccess);
// }
// function cancelShareSuccess(data) {
// CustomAjax.ajaxRequest("api/share", "GET", token, loadShareFileList);
// }
// /**
// * 退出登录----------------------------
// */
// function logout() {
// CustomAjax.ajaxRequest("api/login/logout", "DELETE", token, logoutSuccess);
// }
// function logoutSuccess() {
// CookieUtil.delCookie("pcdtoken");
// window.location.href = "login.html";
// }
// /**
// * other ---------------------------
// */
// // 图片加载失败处理
// function ImgError(obj) {
// obj.src = 'img/file.png'
// obj.onerror = null
// }
// // 获取项目根路径
// function getRootPath() {
// var curWwwPath = window.document.location.href;
// var pathName = window.document.location.pathname;
// var pos = curWwwPath.indexOf(pathName);
// var localhostPaht = curWwwPath.substring(0, pos);
// var projectName = pathName.substring(0, pathName.substr(1).indexOf('/') + 1);
// return (localhostPaht + projectName);
// }
