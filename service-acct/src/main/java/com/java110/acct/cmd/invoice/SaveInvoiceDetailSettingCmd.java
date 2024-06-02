package com.java110.acct.cmd.invoice;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.CmdContextUtils;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.user.UserDto;
import com.java110.intf.acct.IInvoiceDetailSettingV1InnerServiceSMO;
import com.java110.intf.user.IUserV1InnerServiceSMO;
import com.java110.po.invoiceDetailSetting.InvoiceDetailSettingPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.vo.ResultVo;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;

@Java110Cmd(serviceCode = "invoice.saveDetailSetting")
public class SaveInvoiceDetailSettingCmd extends Cmd {
    @Resource
    private IUserV1InnerServiceSMO userV1InnerServiceSMOImpl;
    @Resource
    private IInvoiceDetailSettingV1InnerServiceSMO invoiceDetailSettingInnerServiceSMOImpl;
    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        Assert.hasKeyAndValue(reqJson, "invoiceItemName", "请求报文中未包含invoiceItemName");
        Assert.hasKeyAndValue(reqJson, "expenseName", "请求报文中未包含expenseName");
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        String userId = CmdContextUtils.getUserId(context);
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        List<UserDto> userDtos = userV1InnerServiceSMOImpl.queryUsers(userDto);
        Assert.listOnlyOne(userDtos, "用户未登录");
        InvoiceDetailSettingPo detailSettingPo = new InvoiceDetailSettingPo();
        detailSettingPo.setId(reqJson.getString("id"));
        detailSettingPo.setInvoiceItemName(reqJson.getString("invoiceItemName"));
        detailSettingPo.setExpenseName(reqJson.getString("expenseName"));
        detailSettingPo.setExpenseNum(reqJson.getString("expenseNum"));
        detailSettingPo.setExpenseRate(reqJson.getDouble("expenseRate"));
        detailSettingPo.setRemark(reqJson.getString("remark"));
        detailSettingPo.setCreatedBy(userId);
        int count = invoiceDetailSettingInnerServiceSMOImpl.saveInvoiceDetailSetting(detailSettingPo);
        if(count==0){
            throw new CmdException("保存数据失败");
        }
        context.setResponseEntity(ResultVo.success());
    }
}
