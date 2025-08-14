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
     * 执行指定的脚本命令，并将标准输出和错误输出实时写入到日志文件中。
     * <p>
     * 该方法通过调用系统命令执行脚本，支持传入额外参数，并将执行过程中的输出信息
     * （包括正常输出和错误信息）异步写入到指定的日志文件中。执行完成后返回脚本的退出码。
     * </p>
     *
     * @param command    要执行的命令（如：bash、python等）
     * @param scriptFile 脚本文件路径
     * @param logFile    日志输出文件路径
     * @param params     传递给脚本的可变参数列表
     * @return 脚本执行的退出码，0 表示成功，非 0 表示失败，-1 表示执行过程中发生异常
     * @throws IOException 如果在执行过程中发生IO异常
     */
    public static int execToFile(String command, String scriptFile, String logFile, String... params) throws IOException {

        // 使用try-with-resources确保日志文件输出流正确关闭
        try (FileOutputStream fileOutputStream = new FileOutputStream(logFile, true)) {

            // 构建完整的命令行参数数组
            List<String> cmdArray = new ArrayList<>();
            cmdArray.add(command);
            cmdArray.add(scriptFile);
            if (ArrayUtils.isNotEmpty(params)) {
                // 将可变参数添加到命令列表中
                cmdArray.addAll(Arrays.asList(params));
            }
            String[] cmdArrayFinal = cmdArray.toArray(new String[0]);

            // 启动进程执行命令
            final Process process = Runtime.getRuntime().exec(cmdArrayFinal);

            // 创建线程池用于异步读取标准输出和错误输出
            ExecutorService executor = Executors.newFixedThreadPool(2);

            // 提交标准输出处理任务：将进程的标准输出内容写入日志文件
            Future<?> inputFuture = executor.submit(() -> {
                try {
                    copy(process.getInputStream(), fileOutputStream);
                } catch (IOException e) {
                    JobPlusHelper.log(e);
                }
            });

            // 提交错误输出处理任务：将进程的错误输出内容写入日志文件
            Future<?> errorFuture = executor.submit(() -> {
                try {
                    copy(process.getErrorStream(), fileOutputStream);
                } catch (IOException e) {
                    JobPlusHelper.log(e);
                }
            });

            try {
                // 等待脚本执行完成并获取退出码
                int exitValue = process.waitFor();      // exit code: 0=success, 1=error

                // 等待日志写入线程完成，最多等待5秒
                inputFuture.get(5, TimeUnit.SECONDS);
                errorFuture.get(5, TimeUnit.SECONDS);

                return exitValue;
            } catch (Exception e) {
                // 记录执行过程中的异常信息
                JobPlusHelper.log(e);
                return -1;
            } finally {
                // 关闭线程池并等待任务完成
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
