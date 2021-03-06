﻿<%@page import="com.zhicloud.op.service.constant.AppConstant"%>
<%@page import="com.zhicloud.op.login.LoginInfo"%>
<%@page import="com.zhicloud.op.app.helper.LoginHelper"%>
<%@page import="com.zhicloud.op.common.util.StringUtil"%>
<%@page pageEncoding="utf-8" contentType="text/html; charset=utf-8"%>
<% 
	String URL = (String)request.getAttribute("url");
	Integer userType = AppConstant.SYS_USER_TYPE_TERMINAL_USER;
	String outtime = (String)request.getAttribute("outtime");
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<meta name="viewport" content="height=600, initial-scale=1, minimum-scale=1, maximum-scale=2, user-scalable=yes" />
<title><%=AppConstant.PAGE_TITLE %></title>
<link rel="shortcut icon" href="<%=request.getContextPath() %>/image/logo1.ico" type="image/x-icon" /> 
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/style/style.css" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/javascript/themes/bootstrap/easyui.css" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/javascript/themes/icon.css" /> 
<%--  <script type="text/javascript" src="<%=request.getContextPath()%>/javascript/jquery.js"></script> --%>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/jquery.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/jquery.easyui.min.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/jquery.util.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/jquery.ext.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/locale/easyui-lang-zh_CN.js"></script>

<script type="text/javascript" src="<%=request.getContextPath()%>/javascript/common.js"></script>
<script type="text/javascript">
var a = '<%=request.getContextPath()%>' ;
var ajax = new RemoteCallUtil(a+"/bean/call.do?userType=4");
function checkAccount() {
	var account = new String($("#inputemail").val()).trim(); 
	var myreg = /^([a-zA-Z0-9]+[_|\_|\.]?)*@([a-zA-Z0-9]+[_|\_|\.]?)*[a-zA-Z0-9]+\.[a-zA-Z]{2,3}$/;
	if(account==null || account==""){
		$("#tip-account").html("邮箱不能为空");
		return false;
	}
	if(!myreg.test(account)){
		$("#tip-account").html("邮箱格式不正确");
		return false;
	}
	if(account.length>30){
		$("#tip-account").html("输入不能超过30个字符");
		return false;
		
	}
	else{
		$("#tip-account").html("");
		return true;
		}
	}

function checkPassword(){
	var myPassword = new String($("#inputpassword").val()).trim();
	if(myPassword==null || myPassword==""){
		$("#tip-password").html("密码不能为空");
		return false;
	}else if(myPassword.length<6||myPassword.length>20){ 
		$("#tip-password").html("密码长度为6-20个字符");
		return false;
	} else{
		$("#tip-password").html("");
		return true;
		}
	}
function checkVcode(){
	var verificationCode = new String($("#inputcode").val()).trim();
	if(verificationCode==null || verificationCode==""){
		$("#tip-verificationcode").html("验证码不能为空");
		return false;
	}else{
		$("#tip-verificationcode").html("");
		return true;
	}
}

var ajax = new RemoteCallUtil("<%=request.getContextPath()%>/bean/call.do?userType=<%=userType%>");
ajax.async = false;

function login()
{ 
	var formData = $.formToBean(big_form);
	ajax.remoteCall("bean://sysUserService:login",
		[formData],
		function(reply)
		{ 
			if( reply.status=="exception" )
			{
				top.$.messager.alert("提示", reply.exceptionMessage, "warning");
			} 
			else if( reply.result.status=="success" )
			{ 
				// 跳转页面 
				var userId = reply.result.properties.userId;
				 parent.getLoginInfo($("#inputemail").val(),0,userId);
				 window.location.reload();
			}
			else
			{ 
//				top.$.messager.alert("提示",reply.result.message,"warning");
				$("#tip-verificationcode").html(""+reply.result.message+"");
				$("#password").val("");
				$("#verification_code_img").click();
				$("#verification_code").val("");
			}
		}
	);
}

$(function(){
	// 换一个验证码 
	$("a[name=verification_code_img]").click(function(){
		$("#verification_code").attr("src", "<%=request.getContextPath()%>/public/verificationCode/new.do?userType=<%=userType%>&ts="+Math.random());
	});
	// 回车
	$("#inputemail, #inputpassword,#inputcode").keypress(function(evt){
		if( evt.keyCode==13 ){
			login();
		}
	});
	// 登录
	$("#login_btn").click(function(){ 
		if(checkAccount()&checkPassword()&checkVcode()){
		login();
		}
	});
	// 注册
	$("#register_btn").click(function(){  
		parent.$("#reglink").click();
 	});
});

$(document).ready(function(){  
}); 
function reshVerification()
{ 
	$("#verification_code_img").click(); 	 
}
var t2 = window.setInterval(reshVerification,1740000);
</script>
<!--[if IE 6]>
<script src="javascript/DD_belatedPNG.js"></script>
<script type="text/javascript">
	DD_belatedPNG.fix("*");
</script>
<![endif]-->
</head>

<body style="overflow:hidden;">
<div class="right">
  <div class="box">
    <div class="welcome">&nbsp;</div>
    <form id="big_form" method="post">
      <input type="hidden" id="type" name="type" value="4"/>
      <input id="inputemail" autocomplete="off" name="account" type="text" onfocus="inputfocus('email');" onblur="inputblur('email'),checkAccount()" class="itext"/>
      <div class="itip">
        <label id="emaillabel" for="inputemail" class="ilabel">输入邮箱</label>
        <span class="err" id="tip-account"></span> 
      </div>
      
      <input id="inputpassword" autocomplete="off" name="password" type="password" onpaste='return false' onfocus="inputfocus('password');" onblur="inputblur('password'),checkPassword()" class="itext"/>
      <div class="itip">
        <label id="passwordlabel" for="inputpassword" class="ilabel">输入密码</label>
        <span class="err" id="tip-password"></span>
      </div>
       
      <input id="inputcode" autocomplete="off" name="verification_code" type="text" onfocus="inputfocus('code');" onblur="inputblur('code'),checkVcode()" class="itext l" style="width:100px"/>
      <a href="javascript:void(0);" name="verification_code_img" class="itext l" style="width:100px; height:40px; padding:0; margin-left:10px;"><img id="verification_code"  src="<%=request.getContextPath()%>/public/verificationCode/new.do?userType=<%=userType%>"  width="100" height="40" alt="验证码" class="code"/></a>
      <div class="r" style="height:12px; line-height:16px; padding:4px 0 4px 0;"><a id="verification_code_img" name='verification_code_img' href="javascript:void(0);">看不清<br />
换一张</a></div>
      
       <div class="clear">&nbsp;</div>
      <div class="itip" style="height:20px;">
        <label id="codelabel" for="inputcode"  class="ilabel" style="width:80px">验证码</label>
        <span class="err" id="tip-verificationcode"></span>
      </div>
      <a href="javascript:void(0);" class="ibluebtn l" id="login_btn">登录</a><a href="javascript:void(0);" class="igraybtn r" onclick="parent.slideright();">返回</a>
      <div class="clear"></div>
      <div class="ilink"><a href="#" id="register_btn"  >注册账户</a>　|　<a href="<%=request.getContextPath()%>/forgetpassword.do" target="_parent">忘记密码？</a></div>
       
<!--        <input type="button" id="login_btn"   class="bluebutton shadow r" value="登   录"/> -->
     </form>
  </div>
</div>
</body>
</html>
