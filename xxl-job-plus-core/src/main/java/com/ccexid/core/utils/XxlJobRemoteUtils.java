package com.ccexid.core.utils;

import com.ccexid.core.biz.model.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * XXL-Job远程调用工具类
 * 负责处理与XXL-Job调度中心的HTTP/HTTPS通信
 *
 * @author xuxueli 2018-11-25 00:55:31
 */
public class XxlJobRemoteUtils {
    private static final Logger logger = LoggerFactory.getLogger(XxlJobRemoteUtils.class);
    public static final String XXL_JOB_ACCESS_TOKEN_HEADER = "XXL-JOB-ACCESS-TOKEN";

    // HTTPS证书信任配置（信任所有证书）
    private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[]{new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // 信任所有客户端证书
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            // 信任所有服务器证书
        }
    }};

    private static final HostnameVerifier TRUST_ALL_HOSTNAMES = (hostname, session) -> true;

    /**
     * 发送POST请求（JSON格式请求体）
     *
     * @param url               请求URL地址
     * @param accessToken       访问令牌
     * @param timeoutSeconds    超时时间（秒）
     * @param requestObj        请求对象（将被序列化为JSON）
     * @param returnTargetClass 响应数据的目标类型
     * @return 远程调用结果封装
     */
    public static <T> ApiResponse<T> postJson(String url, String accessToken, int timeoutSeconds,
                                              Object requestObj, Class<T> returnTargetClass) {
        HttpURLConnection connection = null;

        try {
            // 创建URL连接
            URL requestUrl = new URL(url);
            connection = (HttpURLConnection) requestUrl.openConnection();

            // 配置HTTPS连接（如果需要）
            if (url.startsWith("https")) {
                configureHttpsConnection((HttpsURLConnection) connection);
            }

            // 配置连接参数
            configureConnection(connection, timeoutSeconds, accessToken);

            // 发送请求体
            sendRequestBody(connection, requestObj);

            // 检查响应状态码
            int statusCode = connection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return ApiResponse.fail(
                        String.format("远程调用失败，状态码(%d)无效。URL: %s", statusCode, url));
            }

            // 读取并解析响应
            String responseJson = readResponse(connection);
            return parseResponse(responseJson, url, returnTargetClass);

        } catch (Exception e) {
            logger.error("远程调用发生异常", e);
            return ApiResponse.fail(
                    String.format("远程调用错误(%s)，URL: %s", e.getMessage(), url));
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 配置HTTPS连接，信任所有证书和主机名
     */
    private static void configureHttpsConnection(HttpsURLConnection connection) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, TRUST_ALL_CERTS, new java.security.SecureRandom());
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setHostnameVerifier(TRUST_ALL_HOSTNAMES);
        } catch (Exception e) {
            logger.error("配置HTTPS连接失败", e);
        }
    }

    /**
     * 配置HTTP连接的基础参数
     */
    private static void configureConnection(HttpURLConnection connection, int timeoutSeconds, String accessToken) throws ProtocolException {
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setReadTimeout(timeoutSeconds * 1000);
        connection.setConnectTimeout(timeoutSeconds * 1000);
        connection.setRequestProperty("connection", "Keep-Alive");
        connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

        // 可选添加访问令牌
        Optional.ofNullable(accessToken)
                .filter(token -> !token.trim().isEmpty())
                .ifPresent(token -> connection.setRequestProperty(XXL_JOB_ACCESS_TOKEN_HEADER, token));
    }

    /**
     * 发送请求体（使用JDK8的try-with-resources自动关闭流）
     */
    private static void sendRequestBody(HttpURLConnection connection, Object requestObj) throws IOException {
        if (requestObj == null) {
            return;
        }

        String requestBody = GsonUtils.toJson(requestObj);

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            outputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
        }
    }

    /**
     * 读取响应内容（使用Stream API简化代码）
     */
    private static String readResponse(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {

            return reader.lines().collect(Collectors.joining());
        }
    }

    /**
     * 解析响应JSON为ReturnT对象
     */
    private static <T> ApiResponse<T> parseResponse(String responseJson, String url, Class<T> targetClass) {
        try {
            return GsonUtils.fromJsonWithGeneric(responseJson, ApiResponse.class, targetClass);
        } catch (Exception e) {
            logger.error("解析响应内容失败 (URL: {}), 响应内容: {}", url, responseJson, e);
            return ApiResponse.fail(
                    String.format("解析响应内容无效 (URL: %s), 响应: %s", url, responseJson));
        }
    }
}
