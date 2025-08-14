package com.ccexid.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 响应码枚举类，用于定义系统中各种响应状态码及其对应的消息
 */
@Getter
@AllArgsConstructor
public enum ResponseCode implements IEnums {
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),
    NOT_SUPPORT(405, "请求方式不支持"),
    TIMEOUT(502, "请求超时"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_SERVER_ERROR(500, "服务器内部错误"),
    BAD_GATEWAY(502, "网关错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    GATEWAY_TIMEOUT(504, "网关超时"),
    ;
    /**
     * 状态码
     */
    private final int code;
    /**
     * 状态消息
     */
    private final String message;

    /**
     * 根据状态码获取对应的枚举值
     *
     * @param code 状态码
     * @return 对应的枚举值，如果未找到则返回null
     */
    public static ResponseCode fromCode(int code) {
        return Arrays.stream(values())
                .filter(responseCode -> responseCode.code == code)
                .findFirst()
                .orElse(null);
    }
}