package com.java110.store.cmd.resourceStore;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.allocationStorehouse.AllocationStorehouseApplyDto;
import com.java110.dto.workflow.WorkflowDto;
import com.java110.dto.workflow.WorkflowStepStaffDto;
import com.java110.entity.audit.AuditUser;
import com.java110.intf.common.IAllocationStorehouseUserInnerServiceSMO;
import com.java110.intf.common.IWorkflowStepStaffInnerServiceSMO;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.vo.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Java110Cmd(serviceCode = "resourceStore.listAllocationStoreAuditOrders")
public class ListAllocationStoreAuditOrdersCmd extends Cmd {

    @Autowired
    private IAllocationStorehouseUserInnerServiceSMO allocationStorehouseUserInnerServiceSMOImpl;

    @Autowired
    private IWorkflowStepStaffInnerServiceSMO workflowStepStaffInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException {
        Assert.hasKeyAndValue(reqJson, "storeId", "必填，请填写商户ID");
        Assert.hasKeyAndValue(reqJson, "userId", "必填，请填写用户ID");
        Assert.hasKeyAndValue(reqJson, "row", "必填，请填写每页显示数");
        Assert.hasKeyAndValue(reqJson, "page", "必填，请填写页数");

        super.validatePageInfo(reqJson);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        AuditUser auditUser = new AuditUser();
        auditUser.setUserId(reqJson.getString("userId"));
        auditUser.setPage(reqJson.getInteger("page"));
        auditUser.setRow(reqJson.getInteger("row"));
        auditUser.setStoreId(reqJson.getString("storeId"));
        //调拨待办（默认只查询和当前登录用户相关并且是审批或者结束待办事项）
        long count = allocationStorehouseUserInnerServiceSMOImpl.getUserTaskCount(auditUser);

        List<AllocationStorehouseApplyDto> allocationStorehouseApplyDtos = null;

        if (count > 0) {
            allocationStorehouseApplyDtos = allocationStorehouseUserInnerServiceSMOImpl.getUserTasks(auditUser);
        } else {
            allocationStorehouseApplyDtos = new ArrayList<>();
        }

        //todo 计算  仓库管理员
        computeStoreManager(allocationStorehouseApplyDtos, auditUser);

        ResponseEntity responseEntity
                = ResultVo.createResponseEntity((int) Math.ceil((double) count / (double) reqJson.getInteger("row")),
                (int) count,
                allocationStorehouseApplyDtos);
        context.setResponseEntity(responseEntity);
    }

    private void computeStoreManager(List<AllocationStorehouseApplyDto> allocationStorehouseApplyDtos, AuditUser auditUser) {

        if (allocationStorehouseApplyDtos == null || allocationStorehouseApplyDtos.size() < 1) {
            return;
        }

        for(AllocationStorehouseApplyDto allocationStorehouseApplyDto: allocationStorehouseApplyDtos){
            allocationStorehouseApplyDto.setStoreManager("N");
        }

        //todo 查询调拨 中是否为管理员
        WorkflowStepStaffDto workflowStepStaffDto = new WorkflowStepStaffDto();
        workflowStepStaffDto.setFlowType(WorkflowDto.FLOW_TYPE_ALLOCATION_STOREHOUSE);
        workflowStepStaffDto.setStaffId(auditUser.getUserId());
        List<WorkflowStepStaffDto> workflowStepStaffDtos = workflowStepStaffInnerServiceSMOImpl.queryWorkflowStepStaffs(workflowStepStaffDto);

        if(workflowStepStaffDtos == null || workflowStepStaffDtos.size() < 1){
            return;
        }



        for(AllocationStorehouseApplyDto allocationStorehouseApplyDto: allocationStorehouseApplyDtos){
            for(WorkflowStepStaffDto tmpWorkflowStepStaffDto : workflowStepStaffDtos) {
                if (allocationStorehouseApplyDto.getProcessDefinitionKey().equals("java110_"+tmpWorkflowStepStaffDto.getFlowId())){
                    allocationStorehouseApplyDto.setStoreManager("Y");
                }
            }
        }
    }
}
