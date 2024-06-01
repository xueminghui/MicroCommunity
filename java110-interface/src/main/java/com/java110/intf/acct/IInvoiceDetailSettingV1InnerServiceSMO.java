package com.java110.intf.acct;

import com.java110.config.feign.FeignConfiguration;
import com.java110.po.invoiceDetailSetting.InvoiceDetailSettingPo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@FeignClient(name = "acct-service", configuration = {FeignConfiguration.class})
@RequestMapping("/invoiceDetailSettingV1Api")
public interface IInvoiceDetailSettingV1InnerServiceSMO {
    @RequestMapping(value = "/saveInvoiceDetailSetting", method = RequestMethod.POST)
    int saveInvoiceDetailSetting(@RequestBody InvoiceDetailSettingPo info);

    @RequestMapping(value = "/listInvoiceDetailSetting", method = RequestMethod.POST)
    List<InvoiceDetailSettingPo> findInvoiceDetailSettingsByCondition(@RequestBody InvoiceDetailSettingPo info);
}
