<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Seckill Detail</title>
</head>
<body>
   <div class="container">

       <div class="panel panel-default text-center">
           <div class="panel-heading">
               <h1>${seckill.name}</h1>
           </div>

           <div class="panel-body">
               <h2 class="text-danger">
                   <!--显示time图标-->
                   <span class="glyphicon glyphicon-time"></span>
                   <!--展示倒计时-->
                   <span class="glyphicon" id="seckill-box"></span>
               </h2>
           </div>
       </div>

   </div>

 <div id="killPhoneModal" class="modal fade">
     <div class="modal-dialog">
         <div class="modal-content">
             <div class="modal-header">
                 <h3 class="modal-title text-center">
                     <span class="glyphicon glyphicon-phone"></span>phone:
                 </h3>
             </div>
             <div class="modal-body">
                 <div class="col-xs-8 col-xs-offset-2">
                     <input type="text" name="killphone" id="killphoneKey"
                            placeholder="填手机号" class="form-control"/>
                 </div>
             </div>
         </div>

         <div class="model-footer">

             <!--验证信息-->
             <span id="killphoneMessage" class="glyphicon"></span>
             <button type="button" id="killPhoneBtn" class="btn btn-success">
                 <span class="glyphicon glyphicon-phone"></span>
                 Submit
             </button>

         </div>


     </div>

 </div>

   <script src="/resources/script/jquery/js/jquery.min.js"></script>
   <script src="/resources/script/bootstrap/js/bootstrap.min.js"></script>
   <script src="/resources/script/cookie/js/cookie.js"></script>
   <!--倒计时插件-->
   <script src="https://cdn.bootcss.com/jquery.countdown/2.1.0/jquery.countdown.min.js"></script>
   <!--开始编写交互逻辑-->
   <script type="text/javascript" src="/resources/script/seckill.js"></script>
   <script type="text/javascript">
       $(function (){

           //使用El表达式传入参数
           seckill.detail.init({
               seckillId : ${seckill.seckillId},
               startTime : ${seckill.startTime.time},//毫秒
               endTime : ${seckill.endTime.time}
           });
       });
   </script>


</body>
</html>
