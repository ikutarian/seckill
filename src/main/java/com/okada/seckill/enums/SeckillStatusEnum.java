package com.okada.seckill.enums;

/**
 * 使用枚举表述常量数据字段
 */
public enum  SeckillStatusEnum {
    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATE_REWRITE(-3, "数据篡改");


    private int status;
    private String statusInfo;

    SeckillStatusEnum(int status, String statusInfo) {
        this.status = status;
        this.statusInfo = statusInfo;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public static SeckillStatusEnum statusOf(int status) {
        for (SeckillStatusEnum seckillStatusEnum : values()) {
            if (seckillStatusEnum.getStatus() == status) {
                return seckillStatusEnum;
            }
        }
        return null;
    }
}
