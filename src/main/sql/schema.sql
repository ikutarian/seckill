-- 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
USE seckill;

-- 创建秒杀库存表
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill` (
  `seckill_id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品库存id',
  `name` varchar(120) NOT NULL COMMENT '商品名称',
  `number` int NOT NULL COMMENT '库存数量',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `start_time` timestamp NOT NULL COMMENT '秒杀开始时间',
  `end_time` timestamp NOT NULL COMMENT '秒杀结束时间',
  PRIMARY KEY (`seckill_id`),
  KEY idx_start_time(`start_time`),
  KEY idx_end_time(`end_time`),
  KEY idx_create_time(`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1000 DEFAULT CHARSET=UTF8 COMMENT='秒杀库存表';

-- 初始化数据
INSERT INTO seckill (`name`, `number`, `start_time`, `end_time`) VALUES ('1000元秒杀iPhone6', 100, '2018-7-03 00:00:00', '2018-7-04 00:00:00'),
                                                                        ('500元秒杀iPad2', 200, '2018-7-03 00:00:00', '2018-7-04 00:00:00'),
                                                                        ('300元秒杀小米6', 300, '2018-7-03 00:00:00', '2018-7-04 00:00:00'),
                                                                        ('200元秒杀红米', 400, '2018-7-03 00:00:00', '2018-7-04 00:00:00');

-- 秒杀成功明细表
DROP TABLE IF EXISTS `success_killed`;
CREATE TABLE `success_killed` (
  `seckill_id` bigint NOT NULL COMMENT '秒杀商品id',
  `user_phone` bigint NOT NULL COMMENT '用户手机号',
  `status` tinyint NOT NULL DEFAULT -1 COMMENT '状态标识：-1：无效 0：成功 1：已付款 2：已发货',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`seckill_id`, `user_phone`), /* 联合主键：*/
  KEY idx_create_time(`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=UTF8 COMMENT='秒杀成功明细表';