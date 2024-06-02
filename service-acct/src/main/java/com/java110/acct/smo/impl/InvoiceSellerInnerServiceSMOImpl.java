package com.java110.acct.smo.impl;

import com.java110.acct.dao.IInvoiceSellerV1ServiceDao;
import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.intf.acct.IInvoiceSellerInnerServiceSMO;
import com.java110.po.invoiceSeller.InvoiceSellerPo;
import com.java110.utils.exception.CmdException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RestController
public class InvoiceSellerInnerServiceSMOImpl extends BaseServiceSMO implements IInvoiceSellerInnerServiceSMO {

    @Resource
    private IInvoiceSellerV1ServiceDao invoiceSellerV1ServiceDaoImpl;

    public static final String CODE_PREFIX_ID = "10";
    @Override
    public int saveInvoiceSellerInfo(@RequestBody InvoiceSellerPo invoiceSellerPo) {
        if(invoiceSellerPo.getOiId()==null){
            // 添加
            invoiceSellerPo.setOiId(GenerateCodeFactory.getGeneratorId(CODE_PREFIX_ID));
            checkDuplicateException(invoiceSellerPo);
            return invoiceSellerV1ServiceDaoImpl.saveInvoiceSeller(invoiceSellerPo);
        } else {
            // 修改
            invoiceSellerPo.setUpdatedBy(invoiceSellerPo.getUpdatedBy());
            invoiceSellerPo.setUpdatedTime(new Date());
            return invoiceSellerV1ServiceDaoImpl.updateInvoiceSeller(invoiceSellerPo);
        }

    }

    private void checkDuplicateException(InvoiceSellerPo invoiceSellerPo){
        InvoiceSellerPo sellerPo = invoiceSellerV1ServiceDaoImpl.findSellerPoByName(invoiceSellerPo.getSellerName());
        if(sellerPo!=null){
            logger.error("invoice seller already exist, sellerName:{}", invoiceSellerPo.getSellerName());
            throw new CmdException("sellerName:"+invoiceSellerPo.getSellerName()+" duplicate, please use a new sellerName");
        }
    }

    @Override
    public List<InvoiceSellerPo> findInvoiceSellers(@RequestBody InvoiceSellerPo invoiceSellerPo) {
        return invoiceSellerV1ServiceDaoImpl.findInvoiceSellers(invoiceSellerPo);
    }

    @Override
    public int findInvoiceSellerCounts(@RequestBody InvoiceSellerPo info) {
        return invoiceSellerV1ServiceDaoImpl.findInvoiceSellerCounts(info);
    }

    @Override
    public int deleteInvoiceSellerById(@RequestBody InvoiceSellerPo info) {
        return invoiceSellerV1ServiceDaoImpl.deleteInvoiceSellerById(info.getOiId());
    }
}
