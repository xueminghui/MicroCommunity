package com.java110.po.invoiceSeller;

import java.io.Serializable;
import java.util.Date;

public class InvoiceSellerPo implements Serializable {

    private String oiId;

    private String sellerName;

    private String sellerTaxNo;

    private String sellerPhone;

    private String sellerAddress;

    private String sellerBranchBank;

    private Long sellerBankAccount;

    private String remark;

    private String createdBy;

    private Date createdTime;

    private String updatedBy;

    private Date updatedTime;

    private Integer currPage = 1; // 当前页

    private Integer pageSize = 10; // 每页记录数

    private Integer offset = 0; // 偏移量


    public String getOiId() {
        return oiId;
    }

    public void setOiId(String oiId) {
        this.oiId = oiId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerTaxNo() {
        return sellerTaxNo;
    }

    public void setSellerTaxNo(String sellerTaxNo) {
        this.sellerTaxNo = sellerTaxNo;
    }

    public String getSellerPhone() {
        return sellerPhone;
    }

    public void setSellerPhone(String sellerPhone) {
        this.sellerPhone = sellerPhone;
    }

    public String getSellerAddress() {
        return sellerAddress;
    }

    public void setSellerAddress(String sellerAddress) {
        this.sellerAddress = sellerAddress;
    }

    public String getSellerBranchBank() {
        return sellerBranchBank;
    }

    public void setSellerBranchBank(String sellerBranchBank) {
        this.sellerBranchBank = sellerBranchBank;
    }

    public Long getSellerBankAccount() {
        return sellerBankAccount;
    }

    public void setSellerBankAccount(Long sellerBankAccount) {
        this.sellerBankAccount = sellerBankAccount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Integer getCurrPage() {
        return currPage;
    }

    public void setCurrPage(Integer currPage) {
        if(currPage==null || currPage <1){
            currPage = 1;
        }
        this.currPage = currPage;
    }

    public Integer getPageSize() {
        if(pageSize>500){
            pageSize = 500;
        }
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        if(pageSize==null || pageSize<10){
            pageSize = 10;
        }
        this.pageSize = pageSize;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }
}
