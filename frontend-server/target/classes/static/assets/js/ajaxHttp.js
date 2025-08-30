//  通用的ajax方法
function ajaxHttp (obj, callback, err) {
    $.ajax(window.location.origin+":9000"+obj.url, {
        type: obj.type || 'get',
        contentType: obj.contentType || 'application/json;charset=UTF-8',
        headers:{'token': localStorage.getItem('token')},
        data: obj.data || {},
        success: function (res) {
            if (res.code === 200) {
                callback(res)
            } else {
                if (res.code === -2 || res.code === -3) {
                    localStorage.removeItem('token')
                    $('.commodity-header').find('.seckill-shopping').css('display', 'block')
                    setTimeout(function () {
                        layer.confirm('请先进行登录！！', {
                            btn: ['马上登录','取消'] //按钮
                        }, function(){
                            window.location.href = '/login.html'
                        }, function(index){
                            layer.close(index)
                        });
                    }, 200)
                }else{
                    layer.msg(res.msg)
                }
            }
        },
        error: err || defaultError
    })
}
function defaultError(){
    layer.msg('网站繁忙，稍后再试！')
}