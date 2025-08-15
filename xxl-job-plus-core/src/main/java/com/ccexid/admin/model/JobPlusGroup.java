package com.ccexid.admin.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * JobPlusGroup 执行器信息
 *
 * @author xuxueli 2016-10-20 20:20:55
 */
@Data
@TableName("xxl_job_group")
public class JobPlusGroup {

    private Integer id;
    private String appName;
    private String title;
    /**
     * 执行器地址类型：0=自动注册、1=手动录入
     */
    private Integer addressType;
    /**
     * 执行器地址列表，多地址逗号分隔(手动录入)
     */
    private String addressList;
    private Date updateTime;

    private List<String> registryList;  // 缓存执行器地址列表

    /**
     * 获取执行器地址列表
     *
     * @return 执行器地址列表
     */
    public List<String> getRegistryList() {
        if (StringUtils.isNotBlank(addressList)) {
            registryList = new ArrayList<>(Arrays.asList(StringUtils.split(addressList, ",")));
        }
        return registryList;
    }

    public JobPlusGroup() {
    }

    public JobPlusGroup(String title, Integer id, String appName, Integer addressType, String addressList, Date updateTime, List<String> registryList) {
        this.title = title;
        this.id = id;
        this.appName = appName;
        this.addressType = addressType;
        this.addressList = addressList;
        this.updateTime = updateTime;
        this.registryList = registryList;
    }
}
