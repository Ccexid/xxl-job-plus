package com.ccexid.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode implements IEnum {
    // 通用状态码
    SUCCESS(200, "操作成功"),
    FAIL(500, "操作失败"),

    // 参数相关
    PARAM_ERROR(400, "参数错误"),
    PARAM_EMPTY(401, "参数为空"),

    // 权限相关
    NO_PERMISSION(403, "没有权限"),
    UNAUTHORIZED(402, "未授权"),

    // 业务相关
    RESOURCE_NOT_FOUND(404, "资源不存在"),
    BUSINESS_ERROR(501, "业务逻辑错误");

    private final int code;
    private final String msg;

}
