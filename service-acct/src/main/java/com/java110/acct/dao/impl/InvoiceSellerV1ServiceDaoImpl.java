package com.java110.acct.dao.impl;

import com.alibaba.fastjson.JSON;
import com.java110.acct.dao.IInvoiceSellerV1ServiceDao;
import com.java110.core.base.dao.BaseServiceDao;
import com.java110.po.invoiceSeller.InvoiceSellerPo;
import com.java110.utils.exception.DAOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 类表述：
 * @author heshegnfu1211
 * @since 2024-05-29
 * open source address: https://gitee.com/wuxw7/MicroCommunity
 * 官网：http://www.homecommunity.cn
 * 温馨提示：如果您对此文件进行修改 请不要删除原有作者及注释信息，请补充您的 修改的原因以及联系邮箱如下
 * // modify by 张三 at 2021-09-12 第10行在某种场景下存在某种bug 需要修复，注释10至20行 加入 20行至30行
 */
@Service("invoiceSellerV1ServiceDaoImpl")
public class InvoiceSellerV1ServiceDaoImpl extends BaseServiceDao implements IInvoiceSellerV1ServiceDao {
    private static final Logger logger = LoggerFactory.getLogger(InvoiceSellerV1ServiceDaoImpl.class);
    @Override
    public int saveInvoiceSeller(InvoiceSellerPo info) throws DAOException {
        logger.info("saveInvoiceSeller_param:{}", JSON.toJSONString(info));
        return sqlSessionTemplate.insert("invoiceSellerV1ServiceDaoImpl.saveInvoiceSeller", info);
    }

    @Override
    public InvoiceSellerPo findSellerPoByName(String sellerName) throws DAOException {
        logger.info("findSellerPoByName_param:{}", sellerName);
        return sqlSessionTemplate.selectOne("invoiceSellerV1ServiceDaoImpl.findSellerPoByName", sellerName);
    }

    @Override
    public int updateInvoiceSeller(InvoiceSellerPo info) throws DAOException {
        logger.info("updateInvoiceSeller_param:{}", JSON.toJSONString(info));
        return sqlSessionTemplate.insert("invoiceSellerV1ServiceDaoImpl.updateInvoiceSeller", info);
    }

    @Override
    public List<InvoiceSellerPo> findInvoiceSellers(InvoiceSellerPo info) throws DAOException {
        logger.info("findInvoiceSellers_param:{}", JSON.toJSONString(info));
        info.setOffset((info.getCurrPage()-1)*info.getPageSize());
        return sqlSessionTemplate.selectList("invoiceSellerV1ServiceDaoImpl.findInvoiceSellers", info);
    }

    @Override
    public int findInvoiceSellerCounts(InvoiceSellerPo info) throws DAOException {
        logger.info("findInvoiceSellerCounts_param:{}", JSON.toJSONString(info));
        return sqlSessionTemplate.selectOne("invoiceSellerV1ServiceDaoImpl.findInvoiceSellerCounts", info);
    }

    @Override
    public int deleteInvoiceSellerById(String oiId) throws DAOException {
        logger.info("deleteInvoiceSellerById_oiId:{}", oiId);
        return sqlSessionTemplate.delete("invoiceSellerV1ServiceDaoImpl.deleteInvoiceSellerById", oiId);
    }
}
