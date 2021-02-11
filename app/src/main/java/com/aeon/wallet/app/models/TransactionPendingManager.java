package com.aeon.wallet.app.models;

import android.util.Log;

import java.util.ArrayList;

public class TransactionPendingManager {
    private final String TAG = TransactionPendingManager.class.getName();
    private final ArrayList<TransactionPending> pendingTransactions = new ArrayList<>();
    public boolean queueTransaction(final String dst_address, final long amount, final TransactionPending.Priority priority){
        return this.queueTransaction(dst_address,amount, priority,"");
    }
    public boolean queueTransaction(final String dst_address, final long amount, final TransactionPending.Priority priority, final String paymentID){
        Log.v(this.TAG, "queueTransaction");
        getPendingTransactions().add(new TransactionPending(dst_address, amount, priority, paymentID));
        return true;
    }
    public void confirmTransaction(){
        Log.v(this.TAG, "confirmTransaction");
        if(getPendingTransactions().size()>0) {
            getPendingTransactions().get(0).status = TransactionPending.Status.CONFIRMED_BY_USER;
        }
    }
    public void disposeTransaction(){
        if(getPendingTransactions().size()>0) {
            Log.v(this.TAG, "disposeTransaction");
            for (int i = 0; i< getPendingTransactions().size(); i++) {
                if(getPendingTransactions().get(i).status != TransactionPending.Status.CONFIRMED_BY_USER){
                    getPendingTransactions().get(i).status = TransactionPending.Status.DISPOSED_BY_USER;
                }
            }
        }
    }
    public void clearTransactionQueue(Wallet wallet){
        if (getPendingTransactions().size()==0) {
            return;
        }
        Log.v(TAG, "clearTransactionQueue");
        switch (getPendingTransactions().get(0).status){
            case UNCREATED:
                getPendingTransactions().get(0).setHandle(
                        wallet.createTransaction(getPendingTransactions().get(0))
                );
                getPendingTransactions().get(0).status = TransactionPending.Status.CREATED;
                break;
            case CONFIRMED_BY_USER:
                getPendingTransactions().get(0).commit();
                break;
            case DISPOSED_BY_USER:
                wallet.disposeTransaction(getPendingTransactions().get(0));
                getPendingTransactions().remove(0);
                break;
            default:
                getPendingTransactions().get(0).refresh();
        }
    }
    public ArrayList<TransactionPending> getPendingTransactions() {
        return pendingTransactions;
    }
    public boolean isTransactionPending(){
        return getPendingTransactions().size()>0 &&
                (getPendingTransactions().get(0).status == TransactionPending.Status.UNCREATED ||
                        getPendingTransactions().get(0).status == TransactionPending.Status.CONFIRMED_BY_USER ||
                        getPendingTransactions().get(0).status == TransactionPending.Status.DISPOSED_BY_USER);
    }
    public boolean isTransactionReady(){
        return getPendingTransactions().size()>0 &&getLastTransaction().fee !=0;
    }
    public boolean isTransactionQueued(){
        return getPendingTransactions().size()>0 &&
                getLastTransaction().status != TransactionPending.Status.DISPOSED_BY_USER &&
                getLastTransaction().status != TransactionPending.Status.CONFIRMED_BY_USER;
    }
    public TransactionPending getLastTransaction(){
        return getPendingTransactions().get(getPendingTransactions().size()-1);
    }
    public String getAmountInfo(){
        return String.valueOf(getLastTransaction().amount);
    }
    public String getFeeInfo(){
        return String.valueOf(getLastTransaction().fee);
    }
    public String getAddressInfo(){
        return getLastTransaction().recipient;
    }

}
