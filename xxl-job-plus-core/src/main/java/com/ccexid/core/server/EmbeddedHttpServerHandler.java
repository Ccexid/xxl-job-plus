package com.ccexid.core.server;

import com.ccexid.core.enums.ResultCode;
import com.ccexid.core.service.ExecutorService;
import com.ccexid.core.service.model.*;
import com.ccexid.core.utils.GsonUtils;
import com.ccexid.core.utils.ThrowableUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * HTTP请求处理器
 * 负责解析请求、处理业务逻辑并返回响应
 */
public class EmbeddedHttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedHttpServerHandler.class);
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";

    private final ExecutorService executorService;
    private final String accessToken;
    private final ThreadPoolExecutor businessThreadPool;

    public EmbeddedHttpServerHandler(ExecutorService executorService, String accessToken, ThreadPoolExecutor businessThreadPool) {
        this.executorService = executorService;
        this.accessToken = accessToken;
        this.businessThreadPool = businessThreadPool;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        RequestParam params = RequestParam.parse(ctx, request);
        businessThreadPool.execute(() -> processRequest(params));
    }

    /**
     * 处理请求主流程
     */
    private void processRequest(RequestParam params) {
        try {
            ApiResponse<?> validationResult = validateRequest(params);
            if (validationResult.getCode() != ResultCode.SUCCESS.getCode()) {
                writeResponse(params, GsonUtils.toJson(validationResult));
                return;
            }

            Object response = handleBusiness(params);
            writeResponse(params, GsonUtils.toJson(response));
        } catch (Throwable e) {
            handleRequestError(params, e);
        }
    }

    /**
     * 验证请求合法性
     */
    private ApiResponse<?> validateRequest(RequestParam params) {
        if (!HttpMethod.POST.equals(params.getMethod())) {
            return ApiResponse.fail("不支持的HTTP方法");
        }
        if (isUriInvalid(params.getUri())) {
            return ApiResponse.custom(ResultCode.RESOURCE_NOT_FOUND.getCode(), ResultCode.RESOURCE_NOT_FOUND.getMsg(), null);
        }
        if (isAccessTokenInvalid(params.getRequestAccessToken())) {
            return ApiResponse.custom(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg(), null);
        }
        return ApiResponse.SUCCESS;
    }

    private boolean isUriInvalid(String uri) {
        return uri == null || uri.trim().isEmpty();
    }

    private boolean isAccessTokenInvalid(String requestToken) {
        return accessToken != null && !accessToken.trim().isEmpty()
                && !accessToken.equals(requestToken);
    }

    /**
     * 处理业务逻辑
     */
    private Object handleBusiness(RequestParam params) {
        switch (params.getUri()) {
            case "/beat":
                return executorService.beat();
            case "/idleBeat":
                return handleIdleBeat(params.getRequestData());
            case "/run":
                return handleRun(params.getRequestData());
            case "/kill":
                return handleKill(params.getRequestData());
            case "/log":
                return handleLog(params.getRequestData());
            default:
                return ApiResponse.custom(ResultCode.RESOURCE_NOT_FOUND.getCode(), "未找到URI映射: " + params.getUri(), null);
        }
    }

    private Object handleIdleBeat(String requestData) {
        IdleBeatParam param = GsonUtils.fromJson(requestData, IdleBeatParam.class);
        return executorService.idleBeat(param);
    }

    private Object handleRun(String requestData) {
        TriggerParam param = GsonUtils.fromJson(requestData, TriggerParam.class);
        return executorService.run(param);
    }

    private Object handleKill(String requestData) {
        KillParam param = GsonUtils.fromJson(requestData, KillParam.class);
        return executorService.kill(param);
    }

    private Object handleLog(String requestData) {
        LogParam param = GsonUtils.fromJson(requestData, LogParam.class);
        return executorService.log(param);
    }

    /**
     * 处理请求异常
     */
    private void handleRequestError(RequestParam params, Throwable e) {
        logger.error("处理HTTP请求失败", e);
        String errorMsg = "请求处理异常: " + ThrowableUtils.toString(e);
        writeResponse(params, GsonUtils.toJson(ApiResponse.fail(errorMsg)));
    }

    /**
     * 写入响应
     */
    private void writeResponse(RequestParam params, String responseJson) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.copiedBuffer(responseJson, CharsetUtil.UTF_8)
        );

        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, CONTENT_TYPE_JSON)
                .set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

        if (params.isKeepAlive()) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        params.getCtx().writeAndFlush(response);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(">>>>>>>>>>> XXL-Job服务器捕获异常", cause);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ctx.channel().close();
            logger.debug(">>>>>>>>>>> XXL-Job服务器关闭空闲连接");
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
