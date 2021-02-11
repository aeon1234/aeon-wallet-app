package com.aeon.wallet.app.models;
import android.util.Log;

import java.math.BigDecimal;
import java.util.HashMap;

public class Transaction extends HashMap<String, Object> {
    private static final String TAG = Transaction.class.getName();
    private final long handle;
    public enum Direction {
        In,
        Out,
        Unknown
    }
    public Transaction(long handle){
        Log.v(TAG, "Info");
        this.handle = handle;
        put("confirmations",0L);
        refresh();
    }
    public long getHandle(){
        return handle;
    }
    public void refresh(){
        Log.v(TAG, "refresh >> "+ handle);
        put("direction",Direction.values()[getDirectionJNI()].name());
        put("isPending", isPendingJNI());
        put("isFailed", isFailedJNI());
        put("amount",getAmountJNI());
        put("fee",getFeeJNI());
        put("height", getHeightJNI());
        put("confirmations", getConfirmationsJNI());
        put("unlockTime", getUnlockTimeJNI());
        put("hash", getHashJNI());
        put("paymentID", getPaymentIdJNI());
        put("timestamp", getTimestampJNI());
    }
    private native int getDirectionJNI();
    private native boolean isPendingJNI();
    private native boolean isFailedJNI();
    private native long getAmountJNI();
    private native long getFeeJNI();
    private native long getHeightJNI();
    private native long getConfirmationsJNI();
    private native long getUnlockTimeJNI();
    private native String getHashJNI() ;
    private native String getPaymentIdJNI() ;
    private native long getTimestampJNI();
}
