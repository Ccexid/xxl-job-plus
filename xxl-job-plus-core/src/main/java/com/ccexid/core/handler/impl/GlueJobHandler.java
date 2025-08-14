package com.ccexid.core.handler.impl;

import com.ccexid.core.context.JobPlusHelper;
import com.ccexid.core.handler.AbstractJobHandler;

public class GlueJobHandler extends AbstractJobHandler {

    private final long glueUpdateTime;
    private final AbstractJobHandler jobHandler;

    public GlueJobHandler(AbstractJobHandler jobHandler, long glueUpdateTime) {
        this.jobHandler = jobHandler;
        this.glueUpdateTime = glueUpdateTime;
    }

    public long getCurrentGlueUpdateTime() {
        return glueUpdateTime;
    }

    @Override
    public void execute() throws Exception {
        JobPlusHelper.log("----------- glue.version:{} -----------", glueUpdateTime);
        jobHandler.execute();
    }

    @Override
    public void init() throws Exception {
        this.jobHandler.init();
    }

    @Override
    public void destroy() throws Exception {
        this.jobHandler.destroy();
    }
}
