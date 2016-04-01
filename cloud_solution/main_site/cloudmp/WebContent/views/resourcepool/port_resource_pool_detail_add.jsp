<%@ page pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
 <html>
  <head>
    <title>控制台-${productName}</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta charset="UTF-8" />

    <link rel="icon" type="image/ico" href="<%=request.getContextPath()%>/assets/images/favicon.ico" />
    <!-- Bootstrap -->
    <link href="<%=request.getContextPath()%>/assets/css/vendor/bootstrap/bootstrap.min.css" rel="stylesheet">
    <link href="<%=request.getContextPath()%>/font-awesome/css/font-awesome.css" rel="stylesheet">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/vendor/animate/animate.css">
    <link type="text/css" rel="stylesheet" media="all" href="<%=request.getContextPath()%>/assets/js/vendor/mmenu/css/jquery.mmenu.all.css" />
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/js/vendor/videobackground/css/jquery.videobackground.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/vendor/bootstrap-checkbox.css">

    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/js/vendor/chosen/css/chosen.min.css">
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/js/vendor/chosen/css/chosen-bootstrap.css">

    <link href="<%=request.getContextPath()%>/assets/css/zhicloud.css" rel="stylesheet">

    <!-- HTML5 Shim and Respond.js IE8 support of HTML5 elements and media queries -->
    <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
    <!--[if lt IE 9]>
      <script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
      <script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
    <![endif]-->
  </head>
  <body class="bg-1">
     <!-- Preloader -->
    <div class="mask"><div id="loader"></div></div>
    <!--/Preloader -->

    <!-- Wrap all page content here -->
    <div id="wrap">

      <!-- Make page fluid -->
      <div class="row">
        <%@include file="/views/common/common_menus.jsp" %>        
        <!-- Page content -->
        <div id="content" class="col-md-12">
          <!-- page header -->
          <div class="pageheader">
            <h2><i class="fa fa-cogs"></i>创建端口资源</h2>
          </div>
          <!-- /page header -->

          <!-- content main container -->
          <div class="main">
            <!-- row -->
            <div class="row">
              <div class="col-md-12">
                <!-- tile -->
                <section class="tile color transparent-black">
                  <!-- tile widget -->
                  <div class="tile-widget color transparent-black rounded-top-corners nopadding nobg">
                    <!-- Nav tabs -->
                    <ul class="nav nav-tabs tabdrop">
                      <li ><a href="#users-tab" data-toggle="tab" onclick="window.location.href='<%=request.getContextPath() %>/networkpool/ipresourcepool/all';">地址资源池管理 </a></li>
                      <li class="active"><a href="#orders-tab" onclick="window.location.href='<%=request.getContextPath() %>/networkpool/portresourcepool/all';" data-toggle="tab">端口资源池管理</a></li>
		                      <div id="space"></div>
                      
                     </ul>
                    <!-- / Nav tabs -->
                  </div>
                  <!-- /tile widget -->
                  <!-- tile header -->
                  <div class="tile-header">
                    <h3>输入端口资源信息</h3>
                    <div class="controls">
                      <a href="#" class="refresh"><i class="fa fa-refresh"></i></a>
                    </div>
                  </div>
                  <!-- /tile header -->

                  <!-- tile body -->
                  <div class="tile-body" style="padding-bottom:0px;margin-bottom:-45px;">
                    
                    <form class="form-horizontal" role="form" parsley-validate id="basicvalidations" action="<%=request.getContextPath() %>/networkpool/portresourcepool/${poolId}/an" method="post"   >
                      <input name="prefixion" type="hidden" value="server_pool_"/>
                      <div class="form-group">
                        <label for="ip" class="col-sm-2 control-label">起始IP *</label>
                         <div class="col-sm-4">
                             <input type="text" class="form-control" id="ip" name="ip"  parsley-trigger="change"  parsley-ip="true" parsley-required="true"  parsley-maxlength="50" parsley-validation-minlength="1" onchange="getCount()">
                        </div>
                      </div>
                      <div class="form-group">  
                        <label for="range" class="col-sm-2 control-label">IP数量*</label>
                         <div class="col-sm-4">
                             <input type="text" class="form-control" id="range" name="range"  parsley-trigger="change" parsley-required="true" parsley-type="integer" parsley-min="1" parsley-validation-minlength="1"/>
                        </div>
                      </div>
                      
                     <div class="form-group form-footer footer-white">
                        <div class="col-sm-offset-4 col-sm-8">
                          <button type="button" class="btn btn-greensea" onclick="saveForm();"><i class="fa fa-plus"></i>
                              <span> 创 建 </span></button>
                          <button type="reset" class="btn btn-default" onclick="backhome();"><i class="fa fa-refresh"></i>
                              <span> 重 置 </span></button>
                        </div>
                      </div>
                            
                    </form>

                  </div>
                  <!-- /tile body -->
                </section>
                <!-- /tile -->
                <div class="tile-body">

                   <a href="#modalDialog" id="dia" role="button"  data-toggle="modal"> </a>
                   <a href="#modalConfirm" id="con" role="button"   data-toggle="modal"> </a>

                   
                   <div class="modal fade" id="modalDialog" tabindex="-1" role="dialog" aria-labelledby="modalDialogLabel" aria-hidden="true">
                     <div class="modal-dialog">
                       <div class="modal-content" style="width:60%;margin-left:20%;">
                         <div class="modal-header">
                           <button type="button" class="close" data-dismiss="modal" aria-hidden="true">Close</button>
                           <h3 class="modal-title" id="modalDialogLabel"><strong>提示</strong></h3>
                         </div>
                         <div class="modal-body">
                           <p id="tipscontent"></p>
                         </div>
                       </div><!-- /.modal-content -->
                     </div><!-- /.modal-dialog -->
                   </div><!-- /.modal -->

                 </div>      
              </div> 
            </div>
            <!-- /row -->
          </div>
          <!-- /content container -->
        </div>
        <!-- Page content end -->
      </div>
      <!-- Make page fluid-->

    </div>
    <!-- Wrap all page content end -->
    <section class="videocontent" id="video"></section>
    <script type="text/javascript" src="<%=request.getContextPath()%>/js/plugins/jquery.form.js"></script>
    <script>
		var poolId = "${poolId}";
		var isCommited = false;
	    //返回
	    function backhome(){
	    	window.location.href = "<%=request.getContextPath()%>/networkpool/portresourcepool/"+poolId+"/qn";
	    }

        function getCount() {
            var ip = $("#ip").val();
            var p = new Array();
            p = ip.split(".");

            var n1 = parseInt(p[0]);
            var n2 = parseInt(p[1]);
            var n3 = parseInt(p[2]);
            var n4 = parseInt(p[3]);

            var sum = 0;

            if (n1 < 127) {
                sum = 2097152000;
            }
            if (n1 < 192) {
                sum = 1072130048;
            }
            if (n1 < 224) {
                sum = 2031616;
            }

            max = sum - (n1 * n2 * n3 * n4);

        }

    	function saveForm(){

            if (parseInt($("#range").val()) > max) {
                $("#tipscontent").html("IP个数超过最大值" + max);
                $("#dia").click();
                return;
            }

			jQuery.ajax({
		        url: '<%=request.getContextPath()%>/main/checklogin',
		        type: 'post', 
		        dataType: 'json',
		        timeout: 10000,
		        async: true,
		        error: function(){
		            alert('Error!');
		        },
		        success: function(result){ 
		        	if(result.status == "fail"){
		        		  isCommited = false;
		        		  $("#tipscontent").html("登录超时，请重新登录");
		     		      $("#dia").click();
			        	}else{ 
 		        			var options = {
	        					success:function result(data){
	        						if(data.status == "fail"){
	        							isCommited = false;
					        			$("#tipscontent").html(data.message);
					     		    	$("#dia").click();  		        							
	        						}else{  		        							
  		        						location.href = "<%=request.getContextPath()%>/networkpool/portresourcepool/"+poolId+"/qn";
	        						}
	        					},
	        					dataType:'json',
	        					timeout:3*60*1000
 		        			};
 		        			var form = jQuery("#basicvalidations");
 		        			form.parsley('validate');
 		        			if(form.parsley('isValid')){
                                if(isCommited){
                                    return false;
                                }
                                isCommited=true;

                                jQuery("#basicvalidations").ajaxSubmit(options);
 		        			}
			        	} 
		        	}
		     }); 
		}
    	$(function(){  
    	    $("#space").width($("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop").width()
    				 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(0).width()
    				 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(1).width()
    				 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(2).width()
    				 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(3).width()
    				 -1).height(
    				  $("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(0).height());
    		$(window).resize(function(){
    			 $("#space").width($("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop").width()
    					 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(0).width()
    					 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(1).width()
    					 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(2).width()
    					 -$("body #content .tile .tile-widget.color.transparent-black.nobg .tabdrop li").eq(3).width()
    					 -1);
    		});
    		});
          
    </script>
  </body>
</html>
      

      

      
 
