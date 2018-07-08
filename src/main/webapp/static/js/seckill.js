var seckill = {
    // 封装秒杀相关ajax的url
    URL: {
        now: function () {
            return '/seckill/time/now';
        },
        exposer: function (seckillId) {
            return '/seckill/' + seckillId + '/exposer';
        },
        execution: function (seckillId, md5) {
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },
    // 验证手机号
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },
    // 详情页秒杀逻辑
    detail: {
        // 详情页初始化
        init: function (params) {
            // 手机验证和登陆，计时交互
            // 规划我们的交互流程
            // 在cookies中查找手机号
            var kilPhone = $.cookie('killPhone');
            // 验证手机号
            if (!seckill.validatePhone(kilPhone)) {
                // 未登录
                // 绑定手机号
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    // 显示弹出层
                    show: true,
                    backdrop: 'static',  // 禁止位置关闭
                    keyboard: false  // 关闭键盘事件监听
                });
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    if (seckill.validatePhone(inputPhone)) {
                        // 电话写入cookie
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        // 刷新页面
                        window.location.reload();
                    } else {
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误</label>').show(300);
                    }
                });
            } else {
                // 已经登陆
                // 获取系统时间
                $.get(seckill.URL.now(), {}, function (result) {
                    if (result && result['success']) {
                        var nowTime = result['data'];

                        var seckillId = params['seckillId'];
                        var startTime = params['startTime'];
                        var endTime = params['endTime'];

                        // 时间判断，开始计时交互
                        seckill.countDown(seckillId, nowTime, startTime, endTime);
                    } else {
                        console.log('result:' + result);
                    }
                });
            }
        }
    },
    countDown: function (seckillId, nowTime, startTime, endTime) {
        var seckillBox = $('#seckill-box');
        // 时间判断
        if (nowTime > endTime) {
            // 秒杀已经结束了
            seckillBox.html('秒杀结束');
        } else if (nowTime < startTime) {
            // 秒杀未开始，绑定计时事件
            var killTime = new Date(startTime + 1000); // 防止时间偏移
            seckillBox.countdown(killTime, function (event) {
                //时间格式
                var format = event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒 ');
                seckillBox.html(format);
            }).on('finish.countdown', function () {
                // 时间完成后的回调事件
                // 获取秒杀地址,控制现实逻辑,执行秒杀
                seckill.handlerSeckill(seckillId, seckillBox);
            });
        } else {
            // 秒杀已开始
            seckill.handlerSeckill(seckillId, seckillBox);
        }
    },
    // 处理秒杀逻辑
    handlerSeckill: function (seckillId, node) {
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');

        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            if (result && result['success']) {
                var exposer = result['data'];
                if (exposer['exposed']) {
                    // 秒杀已开启
                    // 获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log('killUrl:' + killUrl);
                    $('#killBtn').one('click', function () {
                        // 1. 执行秒杀请求
                        $(this).addClass('disable');
                        // 2. 发起请求，执行秒杀
                        $.post(killUrl, {}, function (result) {
                            if (result && result['success']) {
                                var killResult = result['data'];
                                var status = killResult['status'];
                                var statusInfo = killResult['statusInfo'];
                                // 3. 在网页上显示结果
                                node.html('<span class="label label-success">'+ statusInfo + '</span>');
                            }
                        });
                    }); // 只绑定一次点击事件，防止用户连续点击
                    node.show();
                } else {
                    // 秒杀未开启
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    // 重新计算计时逻辑
                    seckill.countDown(seckillId, now, start, end);
                }
            } else {
                console.log('result:' + result);
            }
        });
    }
};