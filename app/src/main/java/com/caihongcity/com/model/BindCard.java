package com.caihongcity.com.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2015/10/27 0027.
 */
public class BindCard implements Serializable{
    private String bankName;
    private String idCardNumber;
    private String bankAccount;
    private String bankAccountName;
    private String increaseLimitStatus;
    private List<CardImg> images;
    private String singleLimit;
    private String examineResult;

    public String getExamineResult() {
        return examineResult;
    }

    public void setExamineResult(String examineResult) {
        this.examineResult = examineResult;
    }

    public String getSingleLimit() {
        return singleLimit;
    }

    public void setSingleLimit(String singleLimit) {
        this.singleLimit = singleLimit;
    }

    private static final long serialVersionUID = 1L;

    public List<CardImg> getImages() {
        return images;
    }

    public void setImages(List<CardImg> images) {
        this.images = images;
    }


    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getIdCardNumber() {
        return idCardNumber;
    }

    public void setIdCardNumber(String idCardNumber) {
        this.idCardNumber = idCardNumber;
    }

    public String getBankAccount() {
        return bankAccount;
    }

    public void setBankAccount(String bankAccount) {
        this.bankAccount = bankAccount;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getIncreaseLimitStatus() {
        return increaseLimitStatus;
    }

    public void setIncreaseLimitStatus(String increaseLimitStatus) {
        this.increaseLimitStatus = increaseLimitStatus;
    }
}
