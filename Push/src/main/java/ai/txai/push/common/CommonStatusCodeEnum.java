/*
 * Copyright 2022 txai.com, Inc. All rights reserved.
 */
package ai.txai.push.common;

/**
 * 常用状态码
 *
 * @author <a href="{+}xiaolong.deng@ctechm.com+">ronnie</a>
 * @version 1.0.0 2022/3/2
 */
public enum CommonStatusCodeEnum {

    // OK
    RPC_OK(0, "RPC SUCCESS"),
    HTTP_OK(200, "HTTP SUCCESS"),
    // 数据未找到
    DATA_NOT_FOUND(10000, "Data not found"),
    // 数据不存在
    DATA_NOT_EXIST(10001, "Data does not exist"),
    // 数据不合法
    DATA_ILLEGAL(10002, "Illegal data"),
    // 数据已过期
    DATA_EXPIRED(10003, "Data out of date"),
    // 数据已失效
    DATA_FAILURE(10004, "Data invalid"),
    // 数据类型非法
    DATA_TYPE_ILLEGAL(10005, "Invalid data type"),
    // 数据不完整
    DATA_UNCOMPLETED(10006, "Incomplete data"),
    // 数据已存在
    DATA_EXISTED(10007, "Data already exists"),

    // 参数缺失
    PARAM_MISSED(20000, "Parameter is missing"),
    // 参数为NULL
    PARAM_NULL(20001, "Parameter is NULL"),
    // 参数非法
    PARAM_ILLEGAL(20002, "Parameter is illegal"),
    // 参数无效
    PARAM_INVAILD(20003, "Parameter is invalid"),
    // 参数不全
    PARAM_UNCOMPLETED(20004, "Parameter is not complete"),
    // 参数类型错误
    PARAM_TYPE_ERROR(20004, "Parameter type error"),


    // 文件不存在
    FILE_NOT_FOUND(30000, "File does not exist"),
    // 写文件异常
    WRITE_FILE_ERROR(30001, "File writing exception"),
    // 读文件异常
    READ_FILE_ERROR(30002, "File reading exception"),
    // 非法的Key
    KEY_ILLEGAL(30003, "Key is illegal"),
    // KEY不正确
    KEY_ERROR(30004, "Key is not correct"),
    // 无法获取KEY
    KEY_GET_ERROR(30005, "Unable to obtain KEY"),
    // 加密(码)失败
    ENCRYPT_FAILURE(30006, "Failed to encrypt"),
    // 解密（码）失败
    DECRYPT_FAILURE(30007, "Failed to decrypt"),


    // 令牌已失效
    TOKEN_INVAILD(40000, "The token is invalid"),
    // 令牌校验失败
    TOEKN_CHECK_FAILURE(40001, "Token verification failed"),
    // 令牌被禁用
    TOKEN_FORBIDDEN(40002, "The token is disabled"),
    // 令牌数据非法
    TOKEN_DATA_ILLEGAL(40003, "The token data is invalid"),


    // DB相关
    DB_UPDATE_FAILED(50001, "DB update failed"),

    // Adapter-gate 异常错误码相关
    PROVIDER_REQUEST_NO_FOUND(60001, "Provider client not found"),
    PROVIDER_API_ERROR(60002, "Provider API ResponseStatusCode Not OK"),

    // APP相关
    // APP已经存在
    APP_HAS_EXIST(61001, "App has exist"),
    // APP找不到
    APP_NOT_FOUND(61002, "App not found"),

    // 长链接认证异常相关
    CONNAUTH_AUTH_KEY_NOT_MATCH(62001, "Auth Key Error"),
    CONNAUTH_MAX_CONNECTION_EXCEEDED(62002, "Max Connection Exceeded"),

    // 鉴权相关
    AUTH_SERVER_EXCEPTION(63001, "Auth Server Exception"),

    // 接口调用异常
    API_INVOKE_ERROR(70000, "Api invoked exception"),
    // 接口调用超时
    API_INVOKE_TIMEOUT(70001, "Api invoked timeout"),
    // 接口禁止调用
    API_FORBBIDEN(70002, "Api is forbbiden"),
    // 接口地址无效
    API_NOT_FOUND(70003, "Invalid api URL"),
    // 接口负载过高
    API_OVERLOAD(70004, "Api's load is too high"),
    // 接口维护中
    API_MAINTANCE(70005, "Api maintenance"),
    // 接口繁忙
    API_BUSY(70006, "Api is busy"),

    // 系统内部错误
    SYS_INNER_ERROR(90000, "Internal system error"),
    // 系统禁止访问
    SYS_FORBIDDEN(90001, "System access forbidden"),
    // 系统维护中
    SYS_MAINTANCE(90002, "System maintenance"),
    // 系统繁忙
    SYS_BUSY(90003, "System is busy"),
    // 未知错误
    SYS_UNKNOW_ERROR(90004, "Unknown error"),

    // order-center 订单中心异常错误码相关
    ORDER_CREATE_FAILURE(210000, "Current user can't create order"),
    ORDER_IN_PROGRESS_EXISTED(210001, "In progress order already exists"),
    ORDER_WAITING_QUEUE_EXCEED(210002, "Order waiting queue exceed"),
    ORDER_CANCELLATION_FAILURE(210003, "Order cancellation failure"),
    ORDER_DISPATCHER_FAILURE(210004, "Order dispatcher failure"),
    ORDER_PROVIDER_FAILURE(210005, "Invoke provider failure"),
    ORDER_NOT_FOUND(210006, "Order can't find"),
    ORDER_PAYMENT_FAILURE(210007, "Order payment failure"),

    // push-server 长连接服务异常错误码相关
    PUSH_AUTH_FAILURE(310001, "Push server auth failure"),
    PUSH_ENCRYPT_FAILURE(310002, "Push server encrypt failure"),
    PUSH_DECRYPT_FAILURE(310003, "Push server decrypt failure"),
    PUSH_SMS_FAILURE(310004, "Push sms failure"),

    ;

    private final int code;
    private final String message;

    CommonStatusCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据code获得枚举类型
     *
     * @param code
     * @return ResponseStatusCodeEnum
     */
    public static CommonStatusCodeEnum fromCode(int code) {
        for (CommonStatusCodeEnum value : CommonStatusCodeEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new RuntimeException("返回状态码不合法");
    }
}
