package com.java110.acct.dao.impl;

import com.alibaba.fastjson.JSON;
import com.java110.acct.dao.IInvoiceDetailSettingV1ServiceDao;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.core.factory.GenerateCodeFactory;
import com.java110.po.invoiceDetailSetting.InvoiceDetailSettingPo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("invoiceDetailSettingV1ServiceDaoImpl")
public class InvoiceDetailSettingV1ServiceDaoImpl extends BaseServiceDao implements IInvoiceDetailSettingV1ServiceDao {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceDetailSettingV1ServiceDaoImpl.class);

    public static final String CODE_PREFIX_ID = "10";

    public static final String EXPENSE_PREFIX_ID = "20";
    @Override
    public int saveInvoiceDetailSetting(InvoiceDetailSettingPo info) {
        logger.info("saveInvoiceDetailSetting_param:{}", JSON.toJSONString(info));
        int count;
        if(StringUtils.isBlank(info.getId())){
            info.setId(GenerateCodeFactory.getGeneratorId(CODE_PREFIX_ID));
            if(StringUtils.isBlank(info.getExpenseNum())){
                info.setExpenseNum(GenerateCodeFactory.getGeneratorId(EXPENSE_PREFIX_ID));
            }
            info.setCreatedTime(new Date());
            count = sqlSessionTemplate.insert("invoiceDetailSettingV1ServiceDaoImpl.insertInvoiceDetailSetting", info);
        } else {
            info.setUpdatedBy(info.getCreatedBy());
            info.setUpdatedTime(new Date());
            count = sqlSessionTemplate.delete("invoiceDetailSettingV1ServiceDaoImpl.updateInvoiceDetailSetting", info);
        }
        return count;
    }

    @Override
    public List<InvoiceDetailSettingPo> findDetailSettingsByCondition(InvoiceDetailSettingPo info) {
        logger.info("findDetailSettingsByCondition_param:{}", JSON.toJSONString(info));
        return sqlSessionTemplate.selectList("invoiceDetailSettingV1ServiceDaoImpl.findDetailSettingsByCondition", info);
    }
}
