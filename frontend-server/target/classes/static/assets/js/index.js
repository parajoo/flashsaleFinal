var timeStart;
var currentSet;
var timeArray = [10,12,14] //
var tabData = [{
    time: timeArray[0] + ':00', // 当前时间
    val: timeArray[0],
    hourse: '00',
    miute: '00',
    sen: '00',
    status:1, // 状态1为秒杀活动已开始
},{
    time: timeArray[1] + ':00', // 当前时间
    val: timeArray[1],
    hourse: '00',
    miute: '00',
    sen: '00',
    status:0, // 状态1为秒杀活动已开始
},{
    time:  timeArray[2] + ':00', // 当前时间
    val: timeArray[2],
    hourse: '00',
    miute: '00',
    sen: '00',
    status:0, // 状态1为秒杀活动已开始
}]
function initTab () {
    $('#seckillTab').html(template($('#commodityTab').html(), {data: tabData}))
    timeStart = setInterval(function () {
        for (var i = 0; i < tabData.length; i++) { // 进行时间数据倒计时处理
            var seckillTabChid = $('#seckillTab').children('.seckill-tab-list').eq(i)
            if (initTimes(timeArray[i]).length > 0) {
                tabData[i].hourse = initTimes(timeArray[i])[0]
                tabData[i].miute = initTimes(timeArray[i])[1]
                tabData[i].sen = initTimes(timeArray[i])[2]
                seckillTabChid.find('.tip-text').text('即将开始')
                seckillTabChid.find('.text').text('距开始')
                seckillTabChid.attr('comstatus', 0)
                seckillTabChid.find('.timeArray').html(template($('#los').html(), {pag: tabData[i]}))
            } else { // 如果大于当前时间则数据为空 提示活动已结束
                if (initTimes(timeArray[i + 1]).length > 0) {
                    seckillTabChid.find('.tip-text').text('正在秒杀')
                    seckillTabChid.find('.text').text('距结束')
                    seckillTabChid.attr('comstatus', 1)
                    seckillTabChid.find('.timeArray').text(initTimes(timeArray[i + 1])[0] + ' : ' + initTimes(timeArray[i + 1])[1] + ' : ' + initTimes(timeArray[i + 1])[2])
                } else {
                    seckillTabChid.find('.tip-time').text('秒杀已结束')
                    seckillTabChid.find('.tip-text').hide()
                    seckillTabChid.attr('comstatus', 2)
                }
            }
        }
    }, 1000)
//     判断用户是否已登录， 未登录默认打开登录弹窗
//     if (!localStorage.getItem('token')) {
//         $('.userLogin').show(500)
//     }
    return tabData
}
function initCommodity (val) {
    ajaxHttp({
        url: '/seckill/seckillProduct/queryByTime?time='+val+''
    },(res) => {
        var data = res.data
        $('#currentCommodity').html(template($('#commodityData').html(), {data: data}))
    },(err) => {})
} // 列表数据声明变量
// tab切换活动时间段筛选信息
function selectTabData(th, status, val) {
    var _thisSibing = $(th).siblings()
    var ide = $(th).index()
    clearInterval(timeStart) // 清除数据倒计时
    $(th).addClass('seckill-tab-list-active').siblings().removeClass('seckill-tab-list-active')
    $(th).children('.seckill-tab-list-data').addClass('seckill-tab-active')
    $(th).siblings('.seckill-tab-list').find('.seckill-tab-list-data').removeClass('seckill-tab-active')
    for (var i = 0; i < _thisSibing.length; i++) { // 兄弟级展示对应显示的数据状态
        $(_thisSibing).eq(i).find('#seckillChange').html(template($('#seckillSelectAwait').html(), {changeData: parseInt($(_thisSibing).eq(i).attr('comStatus'))}))
    }
    $(th).find('#seckillChange').html(template($('#seckillSelectStart').html(), {parentData: tabData[ide]}))
    $(th).find('.timeArray').html(template($('#los').html(), {pag: tabData[ide]})) // 先默认加载数据渲染
    setInterval(function () { // 切换之后添加无限循环进行间隔一秒进行时间递减
    if (initTimes(timeArray[ide]).length > 0) {
            tabData[ide].hourse = initTimes(timeArray[ide])[0]
            tabData[ide].miute = initTimes(timeArray[ide])[1]
            tabData[ide].sen = initTimes(timeArray[ide])[2]
            $(th).find('.tip-text').text('即将开始')
            $(th).find('.text').text('距开始')
            $(th).attr('comstatus', 0)
            $(th).find('.timeArray').html(template($('#los').html(), {pag: tabData[ide]}))
    } else {
        if (initTimes(timeArray[$(th).index() + 1]).length > 0) {
            $(th).find('.tip-text').text('正在秒杀')
            $(th).find('.text').text('距结束')
            $(th).attr('comstatus', 1)
            $(th).find('.timeArray').text(initTimes(timeArray[ide + 1])[0] + ' : ' + initTimes(timeArray[ide + 1])[1] + ' : ' + initTimes(timeArray[ide + 1])[2])
        } else {
            $(th).find('.tip-text').hide()
            $(th).attr('comstatus', 2)
            $(th).find('.tip-time').text('秒杀已结束')
        }
    }
    }, 1000)
    initCommodity(val)
    return currentSet
}
// 屏幕滚动处理固定头部导航
$(window).on('scroll',function () {
    if (window.scrollY > 170) { // 如果当前滚动距离到170则头部导航固定顶部
      $('.seckill-classify').addClass('seckill-classify-active')
      $('.seckill-tab').addClass('seckill-tab-array-active')
    } else {
        $('.seckill-classify').removeClass('seckill-classify-active')
        $('.seckill-tab').removeClass('seckill-tab-array-active')
    }
})
// 时间倒计时处理函数
var times=[]
function initTimes(houres) {
    var currentData = new Date().setHours(houres, 0,0,0);
    var curTime = new Date()
    var residue = currentData - curTime
    if (residue > 0) { // 如果当前时间为还未开始则进行回填至计算
        var hs = parseInt(residue/1000/60/60%24)
        var ms = parseInt(residue/1000/60%60)
        var ss = parseInt(residue/1000%60)
        var h = hs < 10 && hs >= 1 ? ('0' + hs) : hs
        var m = ms < 10 && ms >= 1 ? ('0' + ms) : ms
        var s = ss < 10 && ss >= 1 ? ('0' + ss) : ss
        times =  [h,m,s]
    } else { // 活动时间已经过去结束的进行中文提示活动结束
        times = []
    }
    return times
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
    initCommodity(10) //10
    initTab()
    initUserInfo()
    console.log(window.location.origin);
})


