package com.ccexid.core.service.model;

import com.ccexid.core.enums.ResultCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * 通用返回结果模型，用于统一API接口的响应格式
 *
 * @param <T> 响应数据类型
 * @author xuxueli 2015-12-4 16:32:31
 */
@Getter
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 42L;

    // 通用实例（无数据的成功/失败）
    public static final ApiResponse<Void> SUCCESS = new ApiResponse<>(ResultCode.SUCCESS, null);
    public static final ApiResponse<Void> FAIL = new ApiResponse<>(ResultCode.FAIL, null);

    // 只保留getter方法（无setter，确保不可变性）
    // 不可变成员变量
    private final int code;
    private final String msg;
    private final T content;

    /**
     * 私有构造函数（通过静态工厂方法创建实例）
     *
     * @param resultCode 结果状态码枚举
     * @param content    响应数据
     */
    private ApiResponse(ResultCode resultCode, T content) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg() != null ? resultCode.getMsg() : "";
        this.content = content;
    }

    /**
     * 私有构造函数（通过静态工厂方法创建实例）
     *
     * @param resultCode  结果状态码枚举
     * @param customMsg   自定义消息
     * @param content     响应数据
     */
    private ApiResponse(ResultCode resultCode, String customMsg, T content) {
        this.code = resultCode.getCode();
        this.msg = customMsg != null ? customMsg : (resultCode.getMsg() != null ? resultCode.getMsg() : "");
        this.content = content;
    }

    /**
     * 私有构造函数（通过静态工厂方法创建实例）
     *
     * @param code    自定义状态码
     * @param msg     消息内容
     * @param content 响应数据
     */
    private ApiResponse(int code, String msg, T content) {
        this.code = code;
        this.msg = msg == null ? "" : msg;
        this.content = content;
    }

    /**
     * 创建成功响应（无数据）
     *
     * @param <T> 响应数据类型
     * @return 成功响应对象
     */
    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> success() {
        return (ApiResponse<T>) SUCCESS;
    }

    /**
     * 创建成功响应（带数据）
     *
     * @param content 响应数据
     * @param <T>     响应数据类型
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T content) {
        return new ApiResponse<>(ResultCode.SUCCESS, content);
    }

    /**
     * 创建指定状态的响应（带数据）
     *
     * @param resultCode 结果状态码枚举
     * @param content    响应数据
     * @param <T>        响应数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> of(ResultCode resultCode, T content) {
        return new ApiResponse<>(resultCode, content);
    }

    /**
     * 创建指定状态的响应（带自定义消息）
     *
     * @param resultCode 结果状态码枚举
     * @param customMsg  自定义消息
     * @param <T>        响应数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> of(ResultCode resultCode, String customMsg) {
        return new ApiResponse<>(resultCode, customMsg, null);
    }

    /**
     * 创建指定状态的响应（带自定义消息和数据）
     *
     * @param resultCode 结果状态码枚举
     * @param customMsg  自定义消息
     * @param content    响应数据
     * @param <T>        响应数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> of(ResultCode resultCode, String customMsg, T content) {
        return new ApiResponse<>(resultCode, customMsg, content);
    }

    /**
     * 创建失败响应（使用默认消息）
     *
     * @param <T> 响应数据类型
     * @return 失败响应对象
     */
    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> fail() {
        return (ApiResponse<T>) FAIL;
    }

    /**
     * 创建失败响应（带自定义消息）
     *
     * @param customMsg 自定义消息
     * @param <T>       响应数据类型
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> fail(String customMsg) {
        return new ApiResponse<>(ResultCode.FAIL, customMsg, null);
    }

    /**
     * 创建自定义状态码响应（特殊场景使用）
     *
     * @param code    自定义状态码
     * @param msg     消息内容
     * @param content 响应数据
     * @param <T>     响应数据类型
     * @return 响应对象
     */
    public static <T> ApiResponse<T> custom(int code, String msg, T content) {
        return new ApiResponse<>(code, msg, content);
    }

    /**
     * 判断是否成功
     *
     * @return 如果成功则返回true，否则返回false
     */
    public boolean isSuccess() {
        return code == ResultCode.SUCCESS.getCode();
    }

    @Override
    public String toString() {
        return "ReturnT [code=" + code + ", msg=" + msg + ", content=" + content + "]";
    }
}
