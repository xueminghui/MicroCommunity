package com.java110.acct.dao;

import com.java110.po.invoiceSeller.InvoiceSellerPo;
import com.java110.utils.exception.DAOException;

import java.util.List;


public interface IInvoiceSellerV1ServiceDao {

    int saveInvoiceSeller(InvoiceSellerPo info) throws DAOException;

    InvoiceSellerPo findSellerPoByName(String sellerName) throws DAOException;

    int updateInvoiceSeller(InvoiceSellerPo info) throws DAOException;

    List<InvoiceSellerPo> findInvoiceSellers(InvoiceSellerPo info) throws DAOException;

    int findInvoiceSellerCounts(InvoiceSellerPo info) throws DAOException;

    int deleteInvoiceSellerById(String oiId) throws DAOException;
}
