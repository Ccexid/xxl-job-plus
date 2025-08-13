package com.ccexid.core.handler.impl;


import com.ccexid.core.context.XxlJobContext;
import com.ccexid.core.context.XxlJobHelper;
import com.ccexid.core.enums.GlueTypeEnum;
import com.ccexid.core.handler.IJobHandler;
import com.ccexid.core.log.XxlJobFileAppender;
import com.ccexid.core.utils.ScriptUtil;

import java.io.File;

/**
 * Created by xuxueli on 17/4/27.
 */
public class ScriptJobHandler extends IJobHandler {

    private int jobId;
    private long glueUpdatetime;
    private String gluesource;
    private GlueTypeEnum glueType;

    public ScriptJobHandler(int jobId, long glueUpdatetime, String gluesource, GlueTypeEnum glueType) {
        this.jobId = jobId;
        this.glueUpdatetime = glueUpdatetime;
        this.gluesource = gluesource;
        this.glueType = glueType;

        // clean old script file
        File glueSrcPath = new File(XxlJobFileAppender.getGlueSrcPath());
        if (glueSrcPath.exists()) {
            File[] glueSrcFileList = glueSrcPath.listFiles();
            if (glueSrcFileList != null && glueSrcFileList.length > 0) {
                for (File glueSrcFileItem : glueSrcFileList) {
                    if (glueSrcFileItem.getName().startsWith(String.valueOf(jobId) + "_")) {
                        glueSrcFileItem.delete();
                    }
                }
            }
        }

    }

    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }

    @Override
    public void execute() throws Exception {

        if (!glueType.isScript()) {
            XxlJobHelper.handleFail("glueType[" + glueType + "] invalid.");
            return;
        }

        // cmd
        String cmd = glueType.getCmd();

        // make script file
        String scriptFileName = XxlJobFileAppender.getGlueSrcPath()
                .concat(File.separator)
                .concat(String.valueOf(jobId))
                .concat("_")
                .concat(String.valueOf(glueUpdatetime))
                .concat(glueType.getSuffix());
        File scriptFile = new File(scriptFileName);
        if (!scriptFile.exists()) {
            ScriptUtil.markScriptFile(scriptFileName, gluesource);
        }

        // log file
        String logFileName = XxlJobContext.getContext().getJobLogFileName();

        // script params：0=param、1=分片序号、2=分片总数
        String[] scriptParams = new String[3];
        scriptParams[0] = XxlJobHelper.getJobParam();
        scriptParams[1] = String.valueOf(XxlJobContext.getContext().getShardIndex());
        scriptParams[2] = String.valueOf(XxlJobContext.getContext().getShardTotal());

        // invoke
        XxlJobHelper.log("----------- script file:" + scriptFileName + " -----------");
        int exitValue = ScriptUtil.execToFile(cmd, scriptFileName, logFileName, scriptParams);

        if (exitValue == 0) {
            XxlJobHelper.handleSuccess();
            return;
        } else {
            XxlJobHelper.handleFail("script exit value(" + exitValue + ") is failed");
            return;
        }

    }

    /**
     * 任务初始化方法
     * <p>
     * 任务线程初始化时调用，用于执行资源加载等初始化操作
     *
     * @throws Exception 初始化过程中发生的异常，会导致任务启动失败
     */
    @Override
    public void init() throws Exception {

    }

    /**
     * 任务销毁方法
     * <p>
     * 任务线程销毁时调用，用于执行资源释放等清理操作
     *
     * @throws Exception 销毁过程中发生的异常，框架会记录但不影响线程终止
     */
    @Override
    public void destroy() throws Exception {

    }
}
