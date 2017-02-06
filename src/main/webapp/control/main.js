$.confirm.options = {
	confirmButton : "确定",
	cancelButton : "取消",
	post : false,
	submitForm : false,
	confirmButtonClass : "btn-primary",
	cancelButtonClass : "btn-default",
	dialogClass : "modal-dialog"
}
/**
 * 网盘----------------------------
 */
var token = "";
$(function() {
	token = CookieUtil.getCookie("pcdtoken");
	File.getFileList("");
});

// 文件操作
function FileOperate(type, filepath, filename) {
	this.type = type;
	this.filepath = filepath;
	this.returndata = {};
	this.doRequest = function() {
		var obj = this;
		switch (type) {
		case "getFileList":
			var url = "api/file/" + filepath;
			if (!filepath) {
				url = "api/file/";
			}
			ajaxRequest(url, this);
			break;
		case "del":
			$.confirm({
				text : "你确定要删除此文件吗？",
				title : "提示",
				confirm : function(button) {
					ajaxRequest("api/file/del/" + filepath, obj);
				},
				cancel : function(button) {
				},
				confirmButtonClass : "btn-danger",
			});
			break;
		case "mv":
			$.confirm({
				text : "<div id='tree' class='row'></div>",
				title : "选择存储位置",
				confirm : function(button) {
				},
				cancel : function(button) {
				}
			});
			$("#tree").load("html/mv.html")
			$("#dirtable").load(function() {
				$("div[name='filename']").html("移动&nbsp;" + filepath);
				$('#dirtableul').html("主目录");
			});
			break;
		case "rename":
			$.confirm({
				text : "<div class='row'>" + "<div class='col-md-12'><label for='newfilename'>新文件名称</label><input type='text' class='form-control' id='newfilename' required='required' /></div>"
						+ "</div>",
				title : "重命名：" + filename,
				confirm : function(button) {
					var newfilename = $('#newfilename').val();
					newfilename = Base64.encodeURI(newfilename);
					ajaxRequest("api/file/rename/" + filepath + "/" + newfilename, obj);
				},
				cancel : function(button) {
				}
			});
			$('#newfilename').val(filename);
			break;
		case "share":
			ajaxRequest("api/file/share/" + filepath, obj);
			break;
		case "createNewFile":
			$.confirm({
				text : "<div class='row'>" + "<div class='col-md-12'><label for='newfilename'>文件名称</label><input type='text' class='form-control' id='newfilename' required='required' /></div>"
						+ "</div>",
				title : "新建文件",
				confirm : function(button) {
					var thisdir = $("#dirul li:last").find("a").attr("path");
					var newfilename = $('#newfilename').val();
					newfilename = Base64.encodeURI(newfilename);
					if (!thisdir) {
						ajaxRequest("api/file/newfile/" + newfilename, obj);
					} else {
						ajaxRequest("api/file/newfile/" + thisdir + "/" + newfilename, obj);
					}
				},
				cancel : function(button) {
				}
			});
			break;
		case "createNewDir":
			$.confirm({
				text : "<div class='row'>" + "<div class='col-md-12'><label for='newfilename'>文件夹名称</label><input type='text' class='form-control' id='newfilename' required='required' /></div>"
						+ "</div>",
				title : "新建文件夹",
				confirm : function(button) {
					var thisdir = $("#dirul li:last").find("a").attr("path");
					var newfilename = $('#newfilename').val();
					newfilename = Base64.encodeURI(newfilename);
					if (!thisdir) {
						ajaxRequest("api/file/newdir/" + newfilename, obj);
					} else {
						ajaxRequest("api/file/newdir/" + thisdir + "/" + newfilename, obj);
					}

				},
				cancel : function(button) {
				}
			});
			break;
		case "play":
			window.location.href = 'play.html?url=' + filepath;
			break;
		}
	}

	this.callback = function(data) {
		this.returndata = data;
		switch (this.type) {
		case "getFileList":
			this.__getFileList();
			break;
		case "del":
		case "rename":
		case "createNewDir":
		case "createNewFile":
			this.__delFile();
			break;
		case "share":
			this.__shareFile();
			break;
		}

	}
	// 加载文件列表
	this.__getFileList = function() {
		if (this.returndata.result) {
			if (!this.filepath) {
				$("#dirul").empty();
				$("#dirul").append("<li><a href='javascript:void(0)' path='' onclick='File.backDir(this)'>主目录</a></li>");
			}
			table.addRow(this.returndata.result);
		} else {
			toastr.error(this.returndata.resultdesc);
		}
	}
	this.__delFile = function() {
		if (this.returndata.status == 200) {
			var thisdir = $("#dirul li:last").find("a").attr("path");
			new FileOperate("getFileList", thisdir).doRequest();
			toastr.info(this.returndata.resultdesc);
		} else {
			toastr.error(this.returndata.resultdesc);
		}
	}

	this.__shareFile = function() {
		if (this.returndata.status == 200) {
			$.confirm({
				confirmButton : "关闭窗口",
				text : "<div class='row'><div class='col-md-12'><label for='sharelink'>文件分享链接</label><input type='text' class='form-control' id='sharelink' onclick='this.select()'/></div></div>"
						+ "<div class='row'><div class='col-md-4'><label for='sharepwd'>访问密码</label><input type='text' class='form-control' id='sharepwd' onclick='this.select()'/></div><div class='col-md-8'></div></div>",
				title : "分享成功！",
			});
			$("#sharelink").val(getRootPath() + "/api/share/" + this.returndata.result.id);
			$("#sharepwd").val(this.returndata.result.password);
		} else {
			toastr.error(this.returndata.resultdesc);
		}
	}
}

var File = {
	// 获取文件列表
	getFileList : function(path) {
		new FileOperate("getFileList", path).doRequest();
	},
	// 文件操作
	fileManage : function(obj) {
		var path = $(obj).parent().attr("path");
		var type = $(obj).attr("type");
		var filename = $(obj).parent().attr("filename");
		new FileOperate(type, path, filename).doRequest();
	},
	// 进入目录
	enterDir : function(obj) {
		var path = $(obj).parent().next().find("div").attr("path");
		$("#dirul").append("<li><a href='javascript:void(0)' path ='" + path + "' onclick='File.backDir(this)'>" + obj.textContent + "</a></li>");
		File.getFileList(path);
	},
	// 加载指定目录
	backDir : function(obj) {
		var index = $(obj).parent().index();
		$("#dirul li:gt(" + index + ")").remove();
		var b = $(obj).attr("path");
		File.getFileList(b);
	}
}

var table = {
	addRow : function(datas) {
		$('#filetable tbody').empty();
		for (var i = 0; i < datas.length; i++) {
			var data = datas[i];
			var base64filepath = data.base64filepath;
			var td4 = "<td>" + data.fileType + "</td>";
			var td5 = "<td>" + data.descSize + "</td>";
			var td6 = "<td>" + data.lastModifiedTime + "</td></tr>";

			var img = ""
			var td2 = "";
			var td3 = "<td colspan='6'><div style='visibility:hidden;' path='" + data.base64filepath + "' filename='" + data.fileName + "'>";
			if (data.fileType == "文件夹") {
				img = "<img src='img/folder.png' class='img-responsive'>"
				td2 = "<td><a href='javascript:void(0)' onclick='File.enterDir(this)'>" + data.fileName + "</a></td>";
			} else {
				img = "<img src='img/" + data.fileType + ".png' class='img-responsive' base64filepath='" + data.base64filepath + "' onerror='ImgError(this)'>";
				td2 = "<td>" + data.fileName + "</td>";
				if (data.fileType.toLowerCase() == "mp4") {
					td3 += "<a href='play.html?url=" + data.base64filepath + "' target='_blank'><span  title='播放' class='glyphicon glyphicon-facetime-video'></span></a>&nbsp;&nbsp;&nbsp;&nbsp;";
				}
			}
			var td1 = "<tr><td>" + img + "</td>";
			td3 += "<a href='javascipt:void(0)' type='mv' onclick='File.fileManage(this)'><span title='移动' class='glyphicon glyphicon-share-alt'></span></a>&nbsp;&nbsp;&nbsp;&nbsp;";
			td3 += "<a href='javascipt:void(0)' type='rename' onclick='File.fileManage(this)'><span title='重命名' class='glyphicon glyphicon-pencil'></span></a>&nbsp;&nbsp;&nbsp;&nbsp;";
			td3 += "<a href='javascipt:void(0)' type='share' onclick='File.fileManage(this)'><span title='分享' class='glyphicon glyphicon-send'></span></a>&nbsp;&nbsp;&nbsp;&nbsp;";
			td3 += "<a href='javascipt:void(0)' type='del' onclick='File.fileManage(this)'><span title='删除' class='glyphicon glyphicon-trash'></span></a>&nbsp;&nbsp;&nbsp;&nbsp;";
			td3 += "</td></div>";
			$("#filetable").append(td1 + td2 + td3 + td4 + td5 + td6);
		}
		table.tdHover();
	},
	tdHover : function() {
		$("#filetable td").hover(function() {
			$(this).parent().find("td").eq(2).find("div").css('visibility', 'visible');
		}, function() {
			$(this).parent().find("td").eq(2).find("div").css('visibility', 'hidden');
		})
	}
}
/**
 * 分享列表----------------------------
 */
function loadShareList(){
	
}




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
// 退出登录
function logout() {
	$.ajax({
		type : 'POST',
		url : "api/login/logout",
		data : {
			pcdtoken : CookieUtil.getCookie("pcdtoken")
		},
		success : function(data) {
			CookieUtil.delCookie("pcdtoken");
			window.location.href = "login.html";
		},
		error : function(error) {
			if (error) {
				toastr.error(error);
			} else {
				toastr.error("啊哦，出错了！");
			}
		}
	});
}


function ajaxRequest(url, callbackObj) {
	$.ajax({
		type : 'POST',
		url : url,
		data : {
			pcdtoken : token
		},
		success : function(data) {
			callbackObj.callback(data);
		},
		error : function(error) {
			if (error) {
				toastr.error(error);
			} else {
				toastr.error("啊哦，出错了！");
			}
		}
	});
}