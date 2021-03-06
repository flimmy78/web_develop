<%@ page pageEncoding="utf-8"%>
<!-- sys_disk_iso_upload1.jsp -->
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/webupload/webuploader.css" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath() %>/webupload/style.css" />
<script src="<%=request.getContextPath()%>/assets/js/jquery.js"></script>
<script src="<%=request.getContextPath() %>/webupload/webuploader.js"></script>
<script src="<%=request.getContextPath() %>/webupload/upload.js"></script>
<style>
.inputdiv { width:50%;float:left;margin-left:5px; }
.lablediv { width:20%;float:left; }
</style>

<body class="bg-1">
        <div id="wrapper">
        <div id="container">
            <!--头部，相册选择和格式选择-->
		
            <div id="uploader">
                <div class="queueList">
                    <div id="dndArea" class="placeholder">
                        <div id="filePicker"></div>
                        <p>或将照片拖到这里，单次最多可选300张</p>
                    </div>
                </div>
                <div class="statusBar" style="display:none;">
                    <div class="progress">
                        <span class="text">0%</span>
                        <span class="percentage"></span>
                    </div><div class="info"></div>
                    <div class="btns">
                        <div id="filePicker2"></div><div class="uploadBtn">开始上传</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
  </body>
  </html>