function createScoket(token){
    var socket;
    if(typeof(WebSocket) == "undefined") {
        console.log("您的浏览器不支持WebSocket");
    }else{
        // var host = window.location.origin.replace("http:","ws:")
        // 实现化WebSocket对象，指定要连接的服务器地址与端口  建立连接
        socket = new WebSocket("ws://localhost:9000/ws/"+token);
        //打开事件
        socket.onopen = function() {
            console.log("Socket 已打开");
            //socket.send("这是来自客户端的消息" + location.href + new Date());
        };
        //获得消息事件
        socket.onmessage = function(result) {
            var data = $.parseJSON(result.data);
            if(data.orderNo){
                layer.confirm("恭喜你，秒杀成功！查看订单？",{btn:["确定","取消"]},
                    function(){
                        window.location.href = "/order_detail.html?orderNo="+data.orderNo;
                    },
                    function(){
                        layer.closeAll();
                    });
            }else{
                layer.msg(data.msg);
            }
        };
        //关闭事件
        socket.onclose = function() {
            console.log("Socket已关闭");
        };
        //发生了错误事件
        socket.onerror = function() {
            console.log("Socket发生了错误");
        }
        //窗口关闭
        $(window).unload(function(event){
            socket.close();
        });
    }
    return socket;
}