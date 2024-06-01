package com.java110.acct.dao;

import com.java110.po.invoiceDetailSetting.InvoiceDetailSettingPo;

import java.util.List;

public interface IInvoiceDetailSettingV1ServiceDao {
    int saveInvoiceDetailSetting(InvoiceDetailSettingPo info);

    List<InvoiceDetailSettingPo> findDetailSettingsByCondition(InvoiceDetailSettingPo info);
}
