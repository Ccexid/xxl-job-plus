package com.ccexid.core.handler.impl;

import com.ccexid.core.context.JobPlusContext;
import com.ccexid.core.context.JobPlusHelper;
import com.ccexid.core.enums.GlueType;
import com.ccexid.core.handler.AbstractJobHandler;
import com.ccexid.core.log.JobLogFileAppender;
import com.ccexid.core.util.ScriptUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 脚本任务处理器
 * 用于执行各种脚本类型的任务，如Shell、Python、PHP等
 *
 * @author xuxueli
 * @since 1.0.0
 */
public class ScriptJobHandler extends AbstractJobHandler {

    private final int jobId;
    private final long glueUpdateTime;
    private final String glueSource;
    private final GlueType glueType;

    public ScriptJobHandler(int jobId, long glueUpdateTime, String glueSource, GlueType glueType) {
        this.jobId = jobId;
        this.glueUpdateTime = glueUpdateTime;
        this.glueSource = glueSource;
        this.glueType = glueType;

        // 清理旧的脚本文件
        cleanOldScriptFiles();
    }

    /**
     * 清理旧的脚本文件
     */
    private void cleanOldScriptFiles() {
        String glueSrcPathStr = JobLogFileAppender.getGlueSrcPath();
        Path glueSrcPath = Paths.get(glueSrcPathStr);
        
        if (Files.exists(glueSrcPath)) {
            try {
                Files.list(glueSrcPath)
                    .filter(path -> path.getFileName().toString().startsWith(jobId + "_"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            JobPlusHelper.log("Failed to delete old script file: {}", path.toString());
                        }
                    });
            } catch (IOException e) {
                JobPlusHelper.log("Failed to list script files for cleanup: {}", glueSrcPathStr);
            }
        }
    }

    public long getCurrentGlueUpdateTime() {
        return glueUpdateTime;
    }

    @Override
    public void execute() throws Exception {
        if (!glueType.isScript()) {
            JobPlusHelper.handleFail("glueType[" + glueType + "] invalid.");
            return;
        }

        // 获取执行命令
        String cmd = glueType.getCmd();

        // 创建脚本文件
        String scriptFileName = buildScriptFileName();
        File scriptFile = new File(scriptFileName);
        if (!scriptFile.exists()) {
            ScriptUtil.markScriptFile(scriptFileName, glueSource);
        }

        // 获取日志文件名
        String logFileName = JobPlusContext.getInstance().getJobLogFileName();

        // 设置脚本参数：0=任务参数、1=分片索引、2=分片总数
        String[] scriptParams = buildScriptParams();

        // 执行脚本
        JobPlusHelper.log("----------- script file: {} -----------", scriptFileName);
        int exitValue = ScriptUtil.execToFile(cmd, scriptFileName, logFileName, scriptParams);

        // 处理执行结果
        handleExecutionResult(exitValue);
    }

    /**
     * 构建脚本文件名
     *
     * @return 脚本文件名
     */
    private String buildScriptFileName() {
        return JobLogFileAppender.getGlueSrcPath()
                .concat(File.separator)
                .concat(String.valueOf(jobId))
                .concat("_")
                .concat(String.valueOf(glueUpdateTime))
                .concat(glueType.getSuffix());
    }

    /**
     * 构建脚本参数
     *
     * @return 脚本参数数组
     */
    private String[] buildScriptParams() {
        String[] scriptParams = new String[3];
        scriptParams[0] = JobPlusHelper.getJobParam();
        scriptParams[1] = String.valueOf(JobPlusContext.getInstance().getShardIndex());
        scriptParams[2] = String.valueOf(JobPlusContext.getInstance().getShardTotal());
        return scriptParams;
    }

    /**
     * 处理脚本执行结果
     *
     * @param exitValue 脚本退出值
     */
    private void handleExecutionResult(int exitValue) {
        if (exitValue == 0) {
            JobPlusHelper.handleSuccess();
        } else {
            JobPlusHelper.handleFail("script exit value(" + exitValue + ") is failed");
        }
    }

    /**
     * 初始化作业处理器
     * 在作业处理器开始执行前调用，用于执行必要的初始化操作
     *
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void init() throws Exception {
        // 暂时无需特殊初始化操作
    }

    /**
     * 销毁作业处理器
     * 在作业处理器结束时调用，用于释放资源和执行清理操作
     *
     * @throws Exception 销毁过程中可能抛出的异常
     */
    @Override
    public void destroy() throws Exception {
        // 暂时无需特殊销毁操作
    }
}
