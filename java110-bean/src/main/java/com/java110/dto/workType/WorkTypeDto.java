package com.java110.dto.workType;

import com.java110.dto.PageDto;
import java.io.Serializable;
import java.util.Date;

/**
 * @ClassName FloorDto
 * @Description 工作类型数据层封装
 * @Author wuxw
 * @Date 2019/4/24 8:52
 * @Version 1.0
 * add by wuxw 2019/4/24
 **/
public class WorkTypeDto extends PageDto implements Serializable {

    private String typeName;
private String remark;
private String communityId;
private String storeId;
private String timeout;
private String wtId;


    private Date createTime;

    private String statusCd = "0";


    public String getTypeName() {
        return typeName;
    }
public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
public String getRemark() {
        return remark;
    }
public void setRemark(String remark) {
        this.remark = remark;
    }
public String getCommunityId() {
        return communityId;
    }
public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }
public String getStoreId() {
        return storeId;
    }
public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
public String getTimeout() {
        return timeout;
    }
public void setTimeout(String timeout) {
        this.timeout = timeout;
    }
public String getWtId() {
        return wtId;
    }
public void setWtId(String wtId) {
        this.wtId = wtId;
    }


    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(String statusCd) {
        this.statusCd = statusCd;
    }
}