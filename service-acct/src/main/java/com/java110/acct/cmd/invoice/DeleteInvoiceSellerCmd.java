package com.java110.acct.cmd.invoice;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.intf.acct.IInvoiceSellerInnerServiceSMO;
import com.java110.po.invoiceSeller.InvoiceSellerPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.vo.ResultVo;

import javax.annotation.Resource;
import java.text.ParseException;

@Java110Cmd(serviceCode = "invoice.delInvoiceSellerById")
public class DeleteInvoiceSellerCmd extends Cmd {

    @Resource
    private IInvoiceSellerInnerServiceSMO invoiceSellerInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        Assert.hasKeyAndValue(reqJson, "oiId", "oiId不能为空");
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        InvoiceSellerPo sellerPo = new InvoiceSellerPo();
        sellerPo.setOiId(reqJson.getString("oiId"));
        int delCount = invoiceSellerInnerServiceSMOImpl.deleteInvoiceSellerById(sellerPo);
        if(delCount==0){
            throw new CmdException("删除数据失败");
        }
        context.setResponseEntity(ResultVo.success());
    }
}
