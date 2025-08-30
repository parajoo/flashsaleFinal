//   鼠标移入礼品小图查看大图数据
$('.commodityDetails-container').on('mouseenter', '.commodity-li', function () {
    var currentImg = $(this).children('img').attr('src')
    $('.big-img-data').attr('src', currentImg)
    $(this).css({'opacity': '1'}).siblings('').css({'opacity': '0.4'})
})
// 判断用户是否登录  如果没有登录自动显示登陆页面
function initDocument () {
    if (!localStorage.getItem('token')) {
        layer.confirm('您还未登陆！！', {
            btn: ['马上登录','取消'] //按钮
        }, function(){
            window.location.href = '/login.html'
        }, function(index){
            layer.close(index)
        });
    }
}
function initDocumentData () { // 获取商品列表
    var currentParams = window.location.search.split('=')
    var commontidy = currentParams[2] // 获取商品id
    var timeId = currentParams[1].split('&')[0] // 时间场次id
    ajaxHttp({
        url: '/seckill/seckillProduct/find?time='+timeId+'&seckillId='+commontidy+''
    },(res) => {
        $('#commodityMsg').html(template($('#commodityMessage').html(), {currentData: res.data}))
        $("#productImg").prop("src",res.data.productImg);
        $("#productImg_small").prop("src",res.data.productImg);
        initDocument()
    })
}
var socket;
// 立即抢购操作
function snapped (seckillId, time) {
    var token = localStorage.getItem('token');
    if (token) {
        ajaxHttp({
            url: '/seckill/order/doSeckill',
            type: 'POST',
            contentType:'application/x-www-form-urlencoded;charset=UTF-8',
            data: {seckillId:seckillId,time:time},
        }, function(params){
             if(!socket){
                socket = createScoket(token);
             }
            // window.location.href="/order_detail.html?orderNo="+params.data;
        });
    } else {
        layer.confirm('请先进行登录！！', {
            btn: ['马上登录','取消'] //按钮
        }, function(){
            window.location.href = '/login.html'
        }, function(index){
           layer.close(index)
        });
    }
}
function initUserInfo(){
    var userInfoStr = window.localStorage.getItem("userInfo");
    if(userInfoStr){
        $(".loginBtn").hide();
        var userInfo = JSON.parse(userInfoStr);
        $("#nickName").html(userInfo.nickName);
        $(".userInfo").show();
    }else{
        $(".loginBtn").show();
        $(".userInfo").hide();
    }
}
$(document).ready(function () {

    initDocumentData()
    initUserInfo()
})