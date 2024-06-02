package com.java110.acct.cmd.invoice;

import com.alibaba.fastjson.JSONObject;
import com.java110.core.annotation.Java110Cmd;
import com.java110.core.context.ICmdDataFlowContext;
import com.java110.core.event.cmd.Cmd;
import com.java110.core.event.cmd.CmdEvent;
import com.java110.intf.acct.IInvoiceSellerInnerServiceSMO;
import com.java110.po.invoiceSeller.InvoiceSellerPo;
import com.java110.utils.exception.CmdException;
import com.java110.vo.ResultVo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Java110Cmd(serviceCode = "invoice.listSellerInfo")
public class ListInvoiceSellerCmd extends Cmd {

    @Resource
    private IInvoiceSellerInnerServiceSMO invoiceSellerInnerServiceSMOImpl;

    private static final String UNDEFINED = "undefined";
    @Override
    public void validate(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        super.validatePageInfo(reqJson);
    }

    @Override
    public void doCmd(CmdEvent event, ICmdDataFlowContext context, JSONObject reqJson) throws CmdException, ParseException {
        InvoiceSellerPo invoiceSellerPo = new InvoiceSellerPo();
        invoiceSellerPo.setSellerName(!UNDEFINED.equals(reqJson.getString("sellerName"))?reqJson.getString("sellerName"):"");
        invoiceSellerPo.setSellerTaxNo(!UNDEFINED.equals(reqJson.getString("sellerTaxNo"))?reqJson.getString("sellerTaxNo"):"");
        invoiceSellerPo.setSellerPhone(!UNDEFINED.equals(reqJson.getString("sellerPhone"))?reqJson.getString("sellerPhone"):"");
        invoiceSellerPo.setCurrPage(reqJson.getInteger("page"));
        invoiceSellerPo.setPageSize(reqJson.getInteger("row"));
        int count = invoiceSellerInnerServiceSMOImpl.findInvoiceSellerCounts(invoiceSellerPo);
        List<InvoiceSellerPo> list;
        if(count>0){
            list = invoiceSellerInnerServiceSMOImpl.findInvoiceSellers(invoiceSellerPo);
        } else {
            list = new ArrayList<>();
        }
        int pages = count % invoiceSellerPo.getPageSize()==0?count/(invoiceSellerPo.getPageSize()):count/(invoiceSellerPo.getPageSize())+1;
        ResultVo resultVo = new ResultVo(pages, count, list);
        ResponseEntity<String> responseEntity = new ResponseEntity<>(resultVo.toString(), HttpStatus.OK);
        context.setResponseEntity(responseEntity);

    }
}
