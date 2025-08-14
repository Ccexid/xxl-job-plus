package com.ccexid.core.util;

import com.ccexid.core.enums.ResponseCode;
import com.ccexid.core.model.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author xuxueli 2018-11-25 00:55:31
 */
@Slf4j
public class XxlJobRemotingUtil {
    public static final String XXL_JOB_ACCESS_TOKEN = "XXL-JOB-ACCESS-TOKEN";


    // trust-https start

    /**
     * 设置HttpsURLConnection信任所有主机和证书，跳过SSL验证
     *
     * @param connection HttpsURLConnection连接对象
     */
    private static void trustAllHosts(HttpsURLConnection connection) {
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();

            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        connection.setHostnameVerifier((hostname, session) -> true);
    }

    private static final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }};
    // trust-https end


    /**
     * 发送POST请求，支持HTTPS协议，携带JSON格式请求体
     *
     * @param url                请求地址
     * @param accessToken        访问令牌，用于身份验证
     * @param timeout            超时时间（单位：秒）
     * @param requestObj         请求参数对象，将被序列化为JSON格式
     * @param returnTargClassOfT 响应结果的泛型类型Class对象
     * @param <T>                泛型类型
     * @return ResponseEntity<T> 封装的响应结果对象
     */
    public static <T> ResponseEntity<T> postBody(String url, String accessToken, int timeout, Object requestObj, Class<T> returnTargClassOfT) {
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            // connection
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            // trust-https
            boolean useHttps = url.startsWith("https");
            if (useHttps) {
                HttpsURLConnection https = (HttpsURLConnection) connection;
                trustAllHosts(https);
            }

            // connection setting
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(timeout * 1000);
            connection.setConnectTimeout(timeout * 1000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            if (StringUtils.isNotBlank(accessToken)) {
                connection.setRequestProperty(XXL_JOB_ACCESS_TOKEN, accessToken);
            }

            // do connection
            connection.connect();

            // write requestBody
            if (requestObj != null) {
                String requestBody = GsonTool.toJson(requestObj);

                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(requestBody.getBytes(StandardCharsets.UTF_8));
                dataOutputStream.flush();
                dataOutputStream.close();
            }

            // valid StatusCode
            int statusCode = connection.getResponseCode();
            if (statusCode != 200) {
                return ResponseEntity.of(ResponseCode.FAIL.getCode(), "xxl-job remoting fail, StatusCode(" + statusCode + ") invalid. for url : " + url, null);
            }

            // result
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String resultJson = result.toString();

            // parse returnT
            try {
                return GsonTool.fromJson(resultJson, ResponseEntity.class, returnTargClassOfT);
            } catch (Exception e) {
                log.error("xxl-job remoting (url={}) response content invalid({}).", url, resultJson, e);
                return ResponseEntity.of(ResponseCode.FAIL.getCode(), "xxl-job remoting (url=" + url + ") response content invalid(" + resultJson + ").", null);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return ResponseEntity.of(ResponseCode.FAIL.getCode(), "xxl-job remoting error(" + e.getMessage() + "), for url : " + url, null);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                log.error(e2.getMessage(), e2);
            }
        }
    }

}
