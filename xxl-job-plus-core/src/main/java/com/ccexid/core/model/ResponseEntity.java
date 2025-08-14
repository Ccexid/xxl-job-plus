package com.ccexid.core.model;

import com.ccexid.core.enums.ResponseCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类，用于封装接口返回的数据
 *
 * @param <T> 响应数据的类型
 */
@Data
public class ResponseEntity<T> implements Serializable {
    private static final long serialVersionUID = 42L;

    /**
     * 响应状态码
     */
    private int code;
    /**
     * 响应消息
     */
    private String msg;
    /**
     * 响应数据内容
     */
    private T content;

    /**
     * 无参构造函数
     */
    private ResponseEntity() {

    }

    /**
     * 有参构造函数，用于创建包含状态码、消息和数据的响应实体
     *
     * @param code    响应状态码
     * @param msg     响应消息
     * @param content 响应数据内容
     */
    private ResponseEntity(int code, String msg, T content) {
        this.code = code;
        this.msg = msg;
        this.content = content;
    }

    /**
     * 创建ResponseEntity实例的通用方法
     *
     * @param code    响应状态码
     * @param msg     响应消息
     * @param content 响应数据内容
     * @param <T>     响应数据的类型
     * @return ResponseEntity实例
     */
    public static <T> ResponseEntity<T> of(int code, String msg, T content) {
        return new ResponseEntity<>(code, msg, content);
    }

    /**
     * 根据ResponseCode创建ResponseEntity实例
     *
     * @param responseCode 响应码枚举
     * @param content      响应数据内容
     * @param <T>          响应数据的类型
     * @return ResponseEntity实例
     */
    public static <T> ResponseEntity<T> of(ResponseCode responseCode, T content) {
        return of(responseCode.getCode(), responseCode.getMessage(), content);
    }

    /**
     * 根据ResponseCode创建不带数据的ResponseEntity实例
     *
     * @param responseCode 响应码枚举
     * @param <T>          响应数据的类型
     * @return ResponseEntity实例
     */
    public static <T> ResponseEntity<T> of(ResponseCode responseCode) {
        return of(responseCode, null);
    }

    /**
     * 创建表示成功的ResponseEntity实例
     *
     * @param content 响应数据内容
     * @param <T>     响应数据的类型
     * @return ResponseEntity实例
     */
    public static <T> ResponseEntity<T> success(T content) {
        return of(ResponseCode.SUCCESS, content);
    }

    /**
     * 创建表示成功的ResponseEntity实例（无数据）
     *
     * @param <T> 响应数据的类型
     * @return ResponseEntity实例
     */
    public static <T> ResponseEntity<T> success() {
        return of(ResponseCode.SUCCESS);
    }

    /**
     * 创建表示失败的ResponseEntity实例
     *
     * @param content 响应数据内容
     * @param <T>     响应数据的类型
     * @return ResponseEntity实例
     */
    public static <T> ResponseEntity<T> fail(T content) {
        return of(ResponseCode.FAIL, content);
    }

    /**
     * 创建表示失败的ResponseEntity实例（无数据）
     *
     * @param <T> 响应数据的类型
     * @return ResponseEntity实例
     */
    public static <T> ResponseEntity<T> fail() {
        return of(ResponseCode.FAIL);
    }


    /**
     * 判断响应实体是否表示成功状态
     *
     * @param responseEntity 响应实体对象
     * @return true表示成功状态，false表示失败状态
     */
    public static boolean isSuccess(ResponseEntity<?> responseEntity) {
        return responseEntity.getCode() == ResponseCode.SUCCESS.getCode();
    }
}