/***
 *
 *  我的订单模块
 *
 */
var priceType=0; // 支付方式默认为现金支付
$('.paymentPage-content').on('click', '.checkBtn',function () {
    $(this).children('.checkBtnRadios').css('display', 'block')
    $(this).addClass('checkBtnActive')
    $(this).parents('.message-wxStyle').siblings('.message-wxStyle').find('.checkBtnRadios').css('display', 'none')
    $(this).parents('.message-wxStyle').siblings('.message-wxStyle').find('.checkBtn').removeClass('checkBtnActive')
    priceType = $(this).attr('value')
})
function initData () { // 初始化加载订单数据
  var orderNo = window.location.search.split('orderNo=')[1]
    ajaxHttp({
        url: '/seckill/order/find?orderNo='+orderNo
    }, (res) => {
        $('#orderListData').html(template($('#orderData').html(), {orderData: res.data}))
    })
};
// 订单支付操作
function startPayMent(orderNo) {
    ajaxHttp({
        url: '/seckill/orderPay/pay?orderNo='+orderNo+'&type='+priceType
    }, (res) => {
        if(priceType==0){
            $("#pay").append(res.data);
        }else{
            window.location.reload();
        }
    })
}
// 取消订单操作
function cancelMent () {
    alert("不实现这个功能，同学们可以自行实现");
}
// 申请退款操作
function delectPayMent (orderNo) {
    ajaxHttp({
        url: "/seckill/orderPay/refund?orderNo="+orderNo
    }, (res) => {
        window.location.reload();
    })
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
    initData()
    layui.form.render()
    initUserInfo()

})