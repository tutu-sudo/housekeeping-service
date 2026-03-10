package com.example.housekeepingservice.common;

/**
 * 100%确保构造器与调用匹配：手动定义所有方法，无Lombok/泛型
 */
public class Result {
    private int code;
    private String message;
    private Object data;

    // 无参构造器（必须存在）
    public Result() {
    }

    // 全参构造器（与静态方法的参数完全对应）
    public Result(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功（带数据）
    public static Result success(Object data) {
        return new Result(200, "操作成功", data);
    }

    // 成功（无数据）
    public static Result success() {
        return new Result(200, "操作成功", null);
    }

    // 失败（自定义状态）
    public static Result error(int code, String message) {
        return new Result(code, message, null);
    }

    // 手动getter/setter（无Lombok依赖）
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}