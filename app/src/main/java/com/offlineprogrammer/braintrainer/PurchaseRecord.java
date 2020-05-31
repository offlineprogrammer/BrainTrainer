package com.offlineprogrammer.braintrainer;

public class PurchaseRecord {
    private IAPDataSource.PurchaseStatus status;
    private String receiptId;
    private String userId;

    public IAPDataSource.PurchaseStatus getStatus() {
        return status;
    }

    public void setStatus(final IAPDataSource.PurchaseStatus status) {
        this.status = status;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(final String receiptId) {
        this.receiptId = receiptId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }
}
