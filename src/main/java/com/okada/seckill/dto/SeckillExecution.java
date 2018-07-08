package com.okada.seckill.dto;

import com.okada.seckill.entity.SuccessKilled;
import com.okada.seckill.enums.SeckillStatusEnum;

/**
 * 封装秒杀执行后的结果
 */
public class SeckillExecution {

    private long seckillId;
    /**
     * 秒杀执行结果状态
     */
    private int status;
    /**
     * 状态标识
     */
    private String statusInfo;

    /**
     * 秒杀成功对象
     */
    private SuccessKilled successKilled;

    /**
     * 成功
     */
    public SeckillExecution(long seckillId, SeckillStatusEnum statusEnum, SuccessKilled successKilled) {
        this.seckillId = seckillId;
        this.status = statusEnum.getStatus();
        this.statusInfo = statusEnum.getStatusInfo();
        this.successKilled = successKilled;
    }

    /**
     * 失败
     */
    public SeckillExecution(long seckillId, SeckillStatusEnum statusEnum) {
        this.seckillId = seckillId;
        this.status = statusEnum.getStatus();
        this.statusInfo = statusEnum.getStatusInfo();
    }

    public long getSeckillId() {
        return seckillId;
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(String statusInfo) {
        this.statusInfo = statusInfo;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "seckillId=" + seckillId +
                ", status=" + status +
                ", statusInfo='" + statusInfo + '\'' +
                ", successKilled=" + successKilled +
                '}';
    }
}
