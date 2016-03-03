package com.caihongcity.com.model;

import java.io.Serializable;

public class QueryModel implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String tradeTime;
	public String tradeStatus;
	public String tradeType;
	public String tradeMoney;
	public String cardNo;
	public String bankName;
	public String orderNo;

	public String getMaxFee() {
		return maxFee;
	}

	public void setMaxFee(String maxFee) {
		this.maxFee = maxFee;
	}

	public String signUrl;//签名图片的url
	public String imageUrl;//交易小票的url
	public String acqAuthNo;//授权码
	public String termianlVoucherNo;//流水号
	public String terminalBatchNo;//批次号
	public String feeRate;//交易费率
	public String maxFee;//封顶费率
	public String payStatus;//提现状态 10A 提现受理失败 10B 提现中 10C 提现成功 10D 提现失败

	public String getPayStatus() {
		return payStatus;
	}

	public void setPayStatus(String payStatus) {
		this.payStatus = payStatus;
	}

	public String getPayResMsg() {
		return payResMsg;
	}

	public void setPayResMsg(String payResMsg) {
		this.payResMsg = payResMsg;
	}

	public String payResMsg;//提现状态描述 一般当payStatus为 10A或10D时会返回值

	public String getSettleCycle() {
		return settleCycle;
	}

	public void setSettleCycle(String settleCycle) {
		this.settleCycle = settleCycle;
	}

	public String settleCycle;//T+0交易 0代表是T+0交易，无需交易提现 1代表不是T+0交易，可以进行交易提现

	
	public String getFeeRate() {
		return feeRate;
	}
	public void setFeeRate(String feeRate) {
		this.feeRate = feeRate;
	}
	public String getTradeTime() {
		return tradeTime;
	}
	public void setTradeTime(String tradeTime) {
		this.tradeTime = tradeTime;
	}
	public String getTradeStatus() {
		return tradeStatus;
	}
	public void setTradeStatus(String tradeStatus) {
		this.tradeStatus = tradeStatus;
	}
	public String getTradeType() {
		return tradeType;
	}
	public void setTradeType(String tradeType) {
		this.tradeType = tradeType;
	}
	public String getTradeMoney() {
		return tradeMoney;
	}
	public void setTradeMoney(String tradeMoney) {
		this.tradeMoney = tradeMoney;
	}
	public String getCardNo() {
		return cardNo;
	}
	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}
	public String getBankName() {
		return bankName;
	}
	public void setBankName(String bankName) {
		this.bankName = bankName;
	}
	public String getOrderNo() {
		return orderNo;
	}
	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}
	public String getSignUrl() {
		return signUrl;
	}
	public void setSignUrl(String signUrl) {
		this.signUrl = signUrl;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getAcqAuthNo() {
		return acqAuthNo;
	}
	public void setAcqAuthNo(String acqAuthNo) {
		this.acqAuthNo = acqAuthNo;
	}
	public String getTermianlVoucherNo() {
		return termianlVoucherNo;
	}
	public void setTermianlVoucherNo(String termianlVoucherNo) {
		this.termianlVoucherNo = termianlVoucherNo;
	}
	public String getTerminalBatchNo() {
		return terminalBatchNo;
	}
	public void setTerminalBatchNo(String terminalBatchNo) {
		this.terminalBatchNo = terminalBatchNo;
	}
	

}
