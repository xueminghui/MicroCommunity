package com.java110.acct.smo.impl;

import com.java110.acct.dao.IInvoiceDetailSettingV1ServiceDao;
import com.java110.core.base.smo.BaseServiceSMO;
import com.java110.intf.acct.IInvoiceDetailSettingV1InnerServiceSMO;
import com.java110.po.invoiceDetailSetting.InvoiceDetailSettingPo;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class InvoiceDetailSettingInnerServiceSMOImpl extends BaseServiceSMO implements IInvoiceDetailSettingV1InnerServiceSMO {

    @Resource
    private IInvoiceDetailSettingV1ServiceDao invoiceDetailSettingV1ServiceDaoImpl;
    @Override
    public int saveInvoiceDetailSetting(@RequestBody InvoiceDetailSettingPo info) {
        return invoiceDetailSettingV1ServiceDaoImpl.saveInvoiceDetailSetting(info);
    }

    @Override
    public List<InvoiceDetailSettingPo> findInvoiceDetailSettingsByCondition(@RequestBody InvoiceDetailSettingPo info) {
        return invoiceDetailSettingV1ServiceDaoImpl.findDetailSettingsByCondition(info);
    }
}
