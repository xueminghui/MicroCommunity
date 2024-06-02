package com.java110.acct.cmd.invoice;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.CmdContextUtils;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.dto.user.UserDto;
import com.java110.intf.acct.IInvoiceSellerInnerServiceSMO;
import com.java110.intf.user.IUserV1InnerServiceSMO;
import com.java110.po.invoiceSeller.InvoiceSellerPo;
import com.java110.utils.exception.CmdException;
import com.java110.utils.util.Assert;
import com.java110.vo.ResultVo;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Java110Cmd(serviceCode = "invoice.saveSellerInfo")
public class SaveInvoiceSellerCmd extends Cmd {

    @Resource
    private IUserV1InnerServiceSMO userV1InnerServiceSMOImpl;
    @Resource
    private IInvoiceSellerInnerServiceSMO invoiceSellerInnerServiceSMOImpl;

    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        Assert.hasKeyAndValue(reqJson, "sellerName", "请求报文中未包含sellerName");
        Assert.hasKeyAndValue(reqJson, "sellerTaxNo", "请求报文中未包含sellerTaxNo");
        Assert.hasKeyAndValue(reqJson, "sellerPhone", "请求报文中未包含sellerPhone");
        Assert.hasKeyAndValue(reqJson, "sellerAddress", "请求报文中未包含sellerAddress");
        Assert.hasKeyAndValue(reqJson, "sellerBranchBank", "请求报文中未包含sellerBranchBank");
        Assert.hasKeyAndValue(reqJson, "sellerBankAccount", "请求报文中未包含sellerBankAccount");
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        String userId = CmdContextUtils.getUserId(context);
        UserDto userDto = new UserDto();
        userDto.setUserId(userId);
        List<UserDto> userDtos = userV1InnerServiceSMOImpl.queryUsers(userDto);
        Assert.listOnlyOne(userDtos, "用户未登录");
        InvoiceSellerPo invoiceSellerPo = new InvoiceSellerPo();
        invoiceSellerPo.setOiId(reqJson.getString("oiId"));
        invoiceSellerPo.setSellerName(reqJson.getString("sellerName"));
        invoiceSellerPo.setSellerPhone(reqJson.getString("sellerPhone"));
        invoiceSellerPo.setSellerTaxNo(reqJson.getString("sellerTaxNo"));
        invoiceSellerPo.setSellerAddress(reqJson.getString("sellerAddress"));
        invoiceSellerPo.setSellerBranchBank(reqJson.getString("sellerBranchBank"));
        invoiceSellerPo.setSellerBankAccount(reqJson.getLong("sellerBankAccount"));
        invoiceSellerPo.setCreatedBy(userId);
        invoiceSellerPo.setCreatedTime(new Date());
        int saveCount = invoiceSellerInnerServiceSMOImpl.saveInvoiceSellerInfo(invoiceSellerPo);
        if(saveCount==0){
            throw new CmdException("保存销方数据失败");
        }
        context.setResponseEntity(ResultVo.success());
    }
}
