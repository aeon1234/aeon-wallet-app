package com.aeon.wallet.app.models;
import android.util.Log;

import java.util.HashMap;

public class TransactionHistory extends HashMap<Long, Transaction> {
    private static final String TAG = TransactionHistory.class.getName();
    private final long handle;
    private int count;
    private boolean hasNewTransaction;
    public TransactionHistory(long handle){
        Log.v(TAG, "History");
        this.handle = handle;
        count = getCountJNI();
        for(int i =0; i<count; i++){
            Transaction newTransaction = new Transaction(getTransactionJNI(i));
            this.put(newTransaction.getHandle(),newTransaction);
        }
        hasNewTransaction = true;
    }
    public void refresh(){
        Log.v(TAG, "refresh >> "+ handle);
        refreshJNI();
        this.clear();
        if(getCountJNI()>count){
            count = getCountJNI();
            hasNewTransaction = true;
        }
        for(int i =0; i<count; i++){
            Transaction newTransaction = new Transaction(getTransactionJNI(i));
            this.put(newTransaction.getHandle(), newTransaction);
        }
    }
    public boolean hasNewTransaction(){
        return hasNewTransaction;
    }
    public void switchHasNewTransaction(){
        hasNewTransaction = !hasNewTransaction;
    }
    private native int getCountJNI();
    private native long getTransactionJNI(int index);
    private native void refreshJNI();
}
