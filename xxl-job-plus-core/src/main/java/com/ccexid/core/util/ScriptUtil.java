package com.ccexid.core.util;

import com.ccexid.core.context.JobPlusHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 1、内嵌编译器如"PythonInterpreter"无法引用扩展包，因此推荐使用java调用控制台进程方式"Runtime.getRuntime().exec()"来运行脚本(shell或python)；
 * 2、因为通过java调用控制台进程方式实现，需要保证目标机器PATH路径正确配置对应编译器；
 * 3、暂时脚本执行日志只能在脚本执行结束后一次性获取，无法保证实时性；因此为确保日志实时性，可改为将脚本打印的日志存储在指定的日志文件上；
 * 4、python 异常输出优先级高于标准输出，体现在Log文件中，因此推荐通过logging方式打日志保持和异常信息一致；否则用prinf日志顺序会错乱
 * <p>
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
        // 使用FileUtils简化文件写入操作
        FileUtils.write(new File(scriptFileName), content, StandardCharsets.UTF_8);
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

        // 使用try-with-resources确保资源正确关闭
        try (FileOutputStream fileOutputStream = new FileOutputStream(logFile, true)) {

            // command
            List<String> cmdArray = new ArrayList<>();
            cmdArray.add(command);
            cmdArray.add(scriptFile);
            if (ArrayUtils.isNotEmpty(params)) {
                // 使用ArrayUtils.addAll简化数组合并
                cmdArray.addAll(Arrays.asList(params));
            }
            String[] cmdArrayFinal = cmdArray.toArray(new String[0]);

            // process-exec
            final Process process = Runtime.getRuntime().exec(cmdArrayFinal);

            // 使用线程池管理线程
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // log-thread
            Future<?> inputFuture = executor.submit(() -> {
                try {
                    copy(process.getInputStream(), fileOutputStream);
                } catch (IOException e) {
                    JobPlusHelper.log(e);
                }
            });

            Future<?> errorFuture = executor.submit(() -> {
                try {
                    copy(process.getErrorStream(), fileOutputStream);
                } catch (IOException e) {
                    JobPlusHelper.log(e);
                }
            });

            try {
                // process-wait
                int exitValue = process.waitFor();      // exit code: 0=success, 1=error

                // 等待日志线程完成
                inputFuture.get(5, TimeUnit.SECONDS);
                errorFuture.get(5, TimeUnit.SECONDS);

                return exitValue;
            } catch (Exception e) {
                JobPlusHelper.log(e);
                return -1;
            } finally {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 数据流Copy（Input自动关闭，Output不处理）
     *
     * @param inputStream
     * @param outputStream
     * @return
     * @throws IOException
     */
    private static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
        return IOUtils.copyLarge(inputStream, outputStream);
    }

}
