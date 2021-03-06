﻿﻿<%@page import="com.zhicloud.op.app.helper.LoginHelper"%>
<%@page import="com.zhicloud.op.exception.ErrorCode"%>
<%@page import="com.zhicloud.op.login.LoginInfo"%>
<%@page pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<%
	Integer userType = Integer.valueOf(request.getParameter("userType"));
	LoginInfo loginInfo = LoginHelper.getLoginInfo(request, userType);
%>
<!DOCTYPE html>
<!-- email_config_manage.jsp -->
<html>
<head>
	<meta charset="UTF-8" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=9;IE=8;IE=7;" />
	<title>运营商管理员 - 邮件配置管理</title>
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/js/easyui/themes/default/easyui.css">
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/js/easyui/themes/icon.css">
	<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/css/common.css">
</head>
	
<body style="visibility:hidden;">
<div class="oper-wrap">
	<div class="tab-container">
		<div id="toolbar">
			<div style="display: table; width: 100%;">
				<div style="display: table-cell; text-align: left">
					<a class="easyui-linkbutton oper-btn-sty" href="javascript:;" data-options="plain:true,iconCls:'icon-add'" id="add_mail_config_btn">添加</a> 
					<a class="easyui-linkbutton oper-btn-sty f-ml10" href="javascript:;" data-options="plain:true,iconCls:'icon-remove'" id="del_mail_config_btn">删除</a>
				</div>
				<div style="display: table-cell; text-align: right;">
					<span class="sear-row">
						<label class="f-mr5">配置名称</label>
						<input class="messager-input" type="text" name = "name" id="config_name"/>
					</span>
					<span class="sear-row">
						<a class="easyui-linkbutton oper-btn-sty f-ml10" href="javascript:;" data-options="iconCls:'icon-search'" id="query_mail_config_btn">查询</a>
						<a class="easyui-linkbutton oper-btn-sty f-ml10" href="javascript:;" data-options="iconCls:'icon-redo'" id="clear_mail_config_btn">清除</a>
					</span>
				</div>
			</div>
		</div>
		<table id="mail_config_datagrid" class="easyui-datagrid" title="邮件配置管理" data-options="url: '<%=request.getContextPath()%>/bean/ajax.do?userType=<%=userType%>&bean=emailConfigService&method=getAllConfig',queryParams: {},toolbar: '#toolbar',rownumbers: false,striped: true,remoteSort: false,fitColumns: true,pagination: true,pageSize: 20,view:createView(),onLoadSuccess: onLoadSuccess">
			<thead>
				<tr>
					<th data-options="field:'ck',checkbox:true"></th>
					<th data-options="field:'name',sortable:true,width:100">配置名称</th>
					<th data-options="field:'sender',width:100">发送人</th>
					<th data-options="field:'senderAddress',width:100">发送人邮箱</th>
					<th data-options="field:'modifiedTime',formatter:timeFormat,width:100">更新时间</th>
					<th data-options="field:'operate',formatter:configColumnFormatter,width:100">操作</th>
				</tr>
			</thead>
		</table>
	</div>
</div>
<!-- JavaScript -->
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.ext.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/jquery.util.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/easyui/jquery.easyui.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/js/easyui/locale/easyui-lang-zh_CN.js"></script>
<script type="text/javascript">
window.name = "selfWin";

var ajax = new RemoteCallUtil("<%=request.getContextPath()%>/bean/call.do?userType=<%=userType%>");
ajax.async = false;


// 布局初始化
$("#mail_config_datagrid").height( $(document.body).height()-20);

function timeFormat(val, row)
{
	return $.formatDateString(val, "yyyyMMddHHmmssSSS", "yyyy-MM-dd HH:mm:ss");
}

function configColumnFormatter(value, row, index)
{
	return "<div row_index='"+index+"'>\
				<a href='#' class='datagrid_row_linkbutton modify_btn'>修改</a>\
			</div>";
}

//查询结果为空
function createView(){
	return $.extend({},$.fn.datagrid.defaults.view,{
	    onAfterRender:function(target){
	        $.fn.datagrid.defaults.view.onAfterRender.call(this,target);
	        var opts = $(target).datagrid('options');
	        var vc = $(target).datagrid('getPanel').children('div.datagrid-view');
	        vc.children('div.datagrid-empty').remove();
	        if (!$(target).datagrid('getRows').length){
	            var d = $('<div class="datagrid-empty"></div>').html( '没有相关记录').appendTo(vc);
	            d.css({
	                position:'absolute',
	                left:0,
	                top:50,
	                width:'100%',
	                textAlign:'center'
	            });
	        }
	    }
    });
}

function onLoadSuccess()
{
	$("body").css({
		"visibility":"visible"
	});
	// 每一行的'修改'按钮
	$("a.modify_btn").click(function(){
		$this = $(this);
		rowIndex = $this.parent().attr("row_index");
		var data = $("#mail_config_datagrid").datagrid("getData");
		var id = data.rows[rowIndex].id; 
		top.showSingleDialog({
			url: "<%=request.getContextPath()%>/bean/page.do?userType=<%=userType%>&bean=emailConfigService&method=modPage&id="+encodeURIComponent(id),
			onClose: function(data){
				$('#mail_config_datagrid').datagrid('reload');
			}
		});
	});
}

$(function(){
	// 查询
	$("#query_mail_config_btn").click(function(){
		var queryParams = {};
		queryParams.name = $("#config_name").val().trim();
		$('#mail_config_datagrid').datagrid({
			"queryParams": queryParams
		});
	});
	//清除
	$("#clear_mail_config_btn").click(function(){
		$("#config_name").val("");

	});		
	// 添加邮件配置
	$("#add_mail_config_btn").click(function(){
		top.showSingleDialog({
			url: "<%=request.getContextPath()%>/bean/page.do?userType=<%=userType%>&bean=emailConfigService&method=addPage",
			onClose: function(data){
				$('#mail_config_datagrid').datagrid('reload');
			}
		});
	});
	// 删除邮件配置
	$("#del_mail_config_btn").click(function() {
		var rows = $('#mail_config_datagrid').datagrid('getSelections');
		if (rows == null || rows.length == 0) {
			top.$.messager.alert("警告","未选择删除项","warning");
			return;
		}
		var ids = rows.joinProperty("id");
		top.$.messager.confirm("确认", "确定要删除吗?", function (r) {
	        if (r) {   
				ajax.remoteCall("bean://emailConfigService:deleteConfigByIds",
					[ ids ], 
					function(reply) {
						if (reply.status == "exception") {
							if(reply.errorCode=="<%=ErrorCode.ERROR_CODE_FAIL_TO_CALL_BEFORE_LOGIN%>"){
								top.$.messager.alert("警告","会话超时，请重新登录","warning",function(){
									top.location.reload();
								});
							}else{
								top.$.messager.alert("警告",reply.exceptionMessage,"warning",function(){
									top.location.reload();
								});
							}
						} else {
							$('#mail_config_datagrid').datagrid(
									'reload');
						}
					}
				);
	        }  
	    }); 
	});


	$('#config_name').bind('keypress',function(event){
        if(event.keyCode == "13")    
        {
        	$("#query_mail_config_btn").click();
        }
    });
});
</script>
</body>
</html>
