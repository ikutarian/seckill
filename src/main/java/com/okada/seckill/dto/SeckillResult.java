package com.okada.seckill.dto;

/**
 * 所有ajax请求返回类型，封装JSON结果
 */
public class SeckillResult {

    private boolean success;
    private Object data;
    private String error;

    public SeckillResult(boolean success, Object data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
