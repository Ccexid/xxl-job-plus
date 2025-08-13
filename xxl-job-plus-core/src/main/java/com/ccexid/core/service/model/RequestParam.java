package com.ccexid.core.service.model;

import com.ccexid.core.utils.XxlJobRemoteUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import lombok.Getter;

@Getter
public class RequestParam {

    private final ChannelHandlerContext ctx;
    private final String requestData;
    private final String uri;
    private final HttpMethod method;
    private final boolean keepAlive;
    private final String requestAccessToken;

    private RequestParam(ChannelHandlerContext ctx, String requestData, String uri,
                         HttpMethod method, boolean keepAlive, String requestAccessToken) {
        this.ctx = ctx;
        this.requestData = requestData;
        this.uri = uri;
        this.method = method;
        this.keepAlive = keepAlive;
        this.requestAccessToken = requestAccessToken;
    }

    /**
     * 解析请求参数
     */
    public static RequestParam parse(ChannelHandlerContext ctx, FullHttpRequest request) {
        return new RequestParam(
                ctx,
                request.content().toString(CharsetUtil.UTF_8),
                request.uri(),
                request.method(),
                HttpUtil.isKeepAlive(request),
                request.headers().get(XxlJobRemoteUtils.XXL_JOB_ACCESS_TOKEN_HEADER)
        );
    }
}
