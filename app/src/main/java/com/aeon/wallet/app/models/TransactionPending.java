package com.aeon.wallet.app.models;
import android.util.Log;

public class TransactionPending {
    private static final String TAG = TransactionPending.class.getName();
    private long handle;
    public String recipient;
    public String paymentID = "";
    public long fee;
    public long amount;
    public long dust;
    public Priority priority;
    public Result result;
    public Status status;
    public enum Status {
        UNCREATED,
        CREATED,
        DISPOSED_BY_USER,
        CONFIRMED_BY_USER,
        COMMITTED,
    }
    public enum Result {
        Ok,
        Error,
        Critical
    }
    public enum Priority {
        AUTOMATIC,
        LOW,
        MEDIUM,
        HIGH,
        LAST
    }
    public TransactionPending(String recipient, long amountAtomic, Priority priority, String paymentID){
        Log.v(TAG, "Pending");
        this.recipient = recipient;
        this.amount = amountAtomic;
        this.priority = priority;
        this.paymentID = paymentID;
        status = Status.UNCREATED;
    }
    public void setHandle(long handle){
        Log.v(TAG, "setHandle");
        this.handle = handle;
        refresh();
    }
    public void refresh(){
        Log.v(TAG, "refresh");
        this.result = Result.values()[getStatusJNI()];
        this.fee = getFeeJNI();
        this.amount = getAmountJNI();
        this.dust = getDustJNI();
    }
    public void commit(){
        Log.v(TAG, "commit");
        commitJNI();
        this.status = Status.COMMITTED;
    }
    private native boolean commitJNI();
    private native int getStatusJNI();
    public native long getFeeJNI();
    public native long getAmountJNI();
    public native long getDustJNI();
}
