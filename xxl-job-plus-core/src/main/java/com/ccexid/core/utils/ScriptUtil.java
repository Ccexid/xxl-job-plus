package com.ccexid.core.utils;


import com.ccexid.core.context.XxlJobHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *  1、内嵌编译器如"PythonInterpreter"无法引用扩展包，因此推荐使用java调用控制台进程方式"Runtime.getRuntime().exec()"来运行脚本(shell或python)；
 *  2、因为通过java调用控制台进程方式实现，需要保证目标机器PATH路径正确配置对应编译器；
 *  3、暂时脚本执行日志只能在脚本执行结束后一次性获取，无法保证实时性；因此为确保日志实时性，可改为将脚本打印的日志存储在指定的日志文件上；
 *  4、python 异常输出优先级高于标准输出，体现在Log文件中，因此推荐通过logging方式打日志保持和异常信息一致；否则用prinf日志顺序会错乱
 *
 * Created by xuxueli on 17/2/25.
 */
public class ScriptUtil {

    /**
     * make script file
     *
     * @param scriptFileName
     * @param content
     * @throws IOException
     */
    public static void markScriptFile(String scriptFileName, String content) throws IOException {
        Objects.requireNonNull(scriptFileName, "脚本文件名不能为null");
        Objects.requireNonNull(content, "脚本内容不能为null");

        // make file,   filePath/gluesource/666-123456789.py
        try (FileOutputStream fileOutputStream = new FileOutputStream(scriptFileName)) {
            fileOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * 脚本执行，日志文件实时输出
     *
     * @param command
     * @param scriptFile
     * @param logFile
     * @param params
     * @return
     * @throws IOException
     */
    public static int execToFile(String command, String scriptFile, String logFile, String... params) throws IOException {
        Objects.requireNonNull(command, "命令不能为null");
        Objects.requireNonNull(scriptFile, "脚本文件不能为null");
        Objects.requireNonNull(logFile, "日志文件不能为null");

        try (FileOutputStream fileOutputStream = new FileOutputStream(logFile, true)) {
            // command
            List<String> cmdarray = new ArrayList<>();
            cmdarray.add(command);
            cmdarray.add(scriptFile);
            if (params != null && params.length > 0) {
                for (String param : params) {
                    cmdarray.add(param);
                }
            }
            String[] cmdarrayFinal = cmdarray.toArray(new String[0]);

            // process-exec
            final Process process = Runtime.getRuntime().exec(cmdarrayFinal);

            // log-thread
            final FileOutputStream finalFileOutputStream = fileOutputStream;
            Thread inputThread = new Thread(() -> {
                try {
                    copy(process.getInputStream(), finalFileOutputStream, new byte[1024]);
                } catch (IOException e) {
                    XxlJobHelper.log(e);
                }
            });
            Thread errThread = new Thread(() -> {
                try {
                    copy(process.getErrorStream(), finalFileOutputStream, new byte[1024]);
                } catch (IOException e) {
                    XxlJobHelper.log(e);
                }
            });
            inputThread.start();
            errThread.start();

            // process-wait
            int exitValue;
            try {
                exitValue = process.waitFor();      // exit code: 0=success, 1=error

                // log-thread join
                inputThread.join();
                errThread.join();
            } catch (InterruptedException e) {
                XxlJobHelper.log(e);
                Thread.currentThread().interrupt();
                return -1;
            }

            return exitValue;
        } catch (Exception e) {
            XxlJobHelper.log(e);
            return -1;
        }
    }

    /**
     * 数据流Copy（Input自动关闭，Output不处理）
     *
     * @param inputStream
     * @param outputStream
     * @param buffer
     * @return
     * @throws IOException
     */
    private static long copy(InputStream inputStream, OutputStream outputStream, byte[] buffer) throws IOException {
        Objects.requireNonNull(inputStream, "输入流不能为null");
        Objects.requireNonNull(buffer, "缓冲区不能为null");

        try {
            long total = 0;
            for (; ; ) {
                int res = inputStream.read(buffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    total += res;
                    if (outputStream != null) {
                        outputStream.write(buffer, 0, res);
                    }
                }
            }
            if (outputStream != null) {
                outputStream.flush();
            }
            return total;
        } finally {
            inputStream.close();
        }
    }
}