package com.java110.acct.cmd.invoice;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.intf.acct.IInvoiceDetailSettingV1InnerServiceSMO;
import com.java110.po.invoiceDetailSetting.InvoiceDetailSettingPo;
import com.java110.utils.exception.CmdException;
import com.java110.vo.ResultVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.List;

@Java110Cmd(serviceCode = "invoice.listDetailSetting")
public class ListInvoiceDetailSettingCmd extends Cmd {
    @Resource
    private IInvoiceDetailSettingV1InnerServiceSMO invoiceDetailSettingSMOImpl;
    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {

    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        InvoiceDetailSettingPo settingPo = new InvoiceDetailSettingPo();
        settingPo.setId(reqJson.getString("id"));
        settingPo.setExpenseName(reqJson.getString("invoiceItemName"));
        settingPo.setExpenseName(reqJson.getString("expenseName"));
        List<InvoiceDetailSettingPo> list  = invoiceDetailSettingSMOImpl.findInvoiceDetailSettingsByCondition(settingPo);
        ResultVo resultVo = new ResultVo(list);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(resultVo.toString(), HttpStatus.OK);
        context.setResponseEntity(responseEntity);
    }
}
