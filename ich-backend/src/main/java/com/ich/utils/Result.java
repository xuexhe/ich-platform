package com.ich.utils;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }

    public static Result<?> success() {
        return new Result<>(200, "操作成功", null);
    }

    public static Result<?> error(String message) {
        return new Result<>(500, message, null);
    }

    public static Result<?> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    public static Result<?> unauthorized() {
        return new Result<>(401, "未授权，请登录", null);
    }

    public static Result<?> forbidden() {
        return new Result<>(403, "权限不足", null);
    }
}