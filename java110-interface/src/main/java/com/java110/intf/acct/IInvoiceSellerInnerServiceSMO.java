package com.java110.intf.acct;

import com.java110.config.feign.FeignConfiguration;
import com.java110.po.invoiceSeller.InvoiceSellerPo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "acct-service", configuration = {FeignConfiguration.class})
@RequestMapping("/invoiceSellerV1Api")
public interface IInvoiceSellerInnerServiceSMO {

    @RequestMapping(value = "/saveInvoiceSeller", method = RequestMethod.POST)
    int saveInvoiceSellerInfo(InvoiceSellerPo invoiceSellerPo);

    @RequestMapping(value = "/listInvoiceSeller", method = RequestMethod.POST)
    List<InvoiceSellerPo> findInvoiceSellers(InvoiceSellerPo invoiceSellerPo);

    @RequestMapping(value = "/findInvoiceSellerCounts", method = RequestMethod.POST)
    int findInvoiceSellerCounts(InvoiceSellerPo info);

    @RequestMapping(value = "/deleteInvoiceSellerById", method = RequestMethod.POST)
    int deleteInvoiceSellerById(InvoiceSellerPo info);


}
