package com.aeon.wallet.app.models;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import com.aeon.wallet.app.ReceiveFragment;
import com.aeon.wallet.app.TransferFragment;
import com.aeon.wallet.app.WalletFragment;
import static com.aeon.wallet.app.MainActivity.preferences;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Objects;
public class WalletThread extends Thread{
    private final String TAG = WalletThread.class.getName();
    public static final TransactionPendingManager transactionPendingManager = new TransactionPendingManager();
    private Wallet wallet;
    private int cycleCount;
    private Status status;
    public enum Status{
        STANDBY,
        MANAGING,
        DESTROYED
    }
    public WalletThread() {
        this.setStatus(Status.STANDBY);
        final String path = preferences.getString("path", this.TAG);
        final String password = preferences.getString("password", this.TAG);
        if(!path.equals("") && path!=null){
            this.start();
            this.queueWallet(path,password);
        }
    }
    public void run() {
        Log.v(this.TAG, "isRunning");
        while(!Thread.interrupted()){
            if(this.getStatus() == Status.MANAGING &&
                    this.wallet.get("status")!= Wallet.Status.CLOSED) {
                switch ((Wallet.Status) Objects.requireNonNull(this.wallet.get("status"))){
                    case EXISTS:
                        this.onCreate();
                        break;
                    case FILE_CREATED:
                        this.onCreated();
                        break;
                    default:
                        this.onResume();
                }
            }
            try {
                this.onPause();
            } catch (final InterruptedException e) {
                interrupt();
                e.printStackTrace();
            }
        }
        this.onDestroy();
    }
    private void onDestroy(){
        Log.v(this.TAG, "Thread.interrupted()");
        this.wallet.put("status",Wallet.Status.CLOSED);
        preferences.putString("path","", this.TAG);
        WalletFragment.WalletAdapter.setData(new HashMapPos());
        ReceiveFragment.AddressAdapter.setData(new HashMapPos());
        TransferFragment.RecentAdapter.ITEMS.clear();
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(TransferFragment::clear);
        this.setStatus(Status.DESTROYED);
        Log.v(this.TAG, "!isManaging");
        Log.v(this.TAG, "!isRunning");
    }
    private void onResume(){
        if(this.cycleCount %100==0) {
            this.wallet.refresh();
            HashMapPos hashMapPos = new HashMapPos();
            hashMapPos.putAll(this.wallet);
            WalletFragment.WalletAdapter.setData(hashMapPos);
            this.updateTransactions();
            this.updateAddresses();
            this.postUIUpdate();
            transactionPendingManager.clearTransactionQueue(wallet);
        }
    }
    private void onCreate(){
        this.wallet.createFiles();
    }
    private void onCreated(){
        this.wallet.init();
        HashMapPos hashMapPos = new HashMapPos();
        hashMapPos.putAll(this.wallet);
        WalletFragment.WalletAdapter.setData(hashMapPos);
    }
    private void onPause() throws InterruptedException {
        if(transactionPendingManager.isTransactionPending()) {
            transactionPendingManager.clearTransactionQueue(wallet);
        } else{
            Thread.sleep(50);
        }
        this.cycleCount++;
    }
    public void queueWallet(final String path, final String password){
        Log.v(this.TAG, "queueWallet");
        this.queueWallet(path, password, null,0);
    }
    public void queueWallet(final String path, final String password, final String seed, final long restoreHeight){
        Log.v(this.TAG, "queueWallet");
        this.queueWallet(path, password, seed, restoreHeight,null,null,null);
    }
    public void queueWallet(final String path, final String password, final String seed, final long restoreHeight,
                            final String accountAddress, final String viewKey, final String spendKey) {
        Log.v(this.TAG, "queueWallet");
        this.wallet = new Wallet(path, password,"English",seed,restoreHeight,
                accountAddress, viewKey, spendKey);
        preferences.putString("path",path, this.TAG);
        preferences.putString("password",password, this.TAG);
        this.setStatus(Status.MANAGING);
    }
    private void updateTransactions(){
        Log.v(this.TAG, "updateTransactions");
        if(this.wallet.getTransactionHistory().size()<TransferFragment.RecentAdapter.ITEMS.size()){
            return;
        }
        HashMapPos hashMapPos = new HashMapPos(Comparator.reverseOrder());
        for(final Transaction tx : this.wallet.getTransactionHistory().values()){
            hashMapPos.put(String.valueOf(tx.get("timestamp"))+tx.get("hash"), tx);
        }
        TransferFragment.RecentAdapter.setData(hashMapPos);
    }
    private void updateAddresses(){
        Log.v(this.TAG, "updateAddresses");
        HashMapPos hashMapPos = new HashMapPos();
        hashMapPos.putAll(ReceiveFragment.AddressAdapter.ITEMS);
        while(this.wallet.getAddresses().size() > hashMapPos.size()){
            hashMapPos.put(
                    String.valueOf(hashMapPos.size()),
                    this.wallet.getAddresses().get(hashMapPos.size())
            );
        }
        if(hashMapPos.size()>ReceiveFragment.AddressAdapter.ITEMS.size()) {
            ReceiveFragment.AddressAdapter.setData(hashMapPos);
        }
    }
    private void postUIUpdate(){
        Log.v(this.TAG, "postUIUpdate");
        final Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            TransferFragment.updateBalance(
                    String.valueOf(this.wallet.get("unlockedBalance")),
                    String.valueOf((Long)this.wallet.get("balance") - (Long)this.wallet.get("unlockedBalance")));
            if (Objects.equals(this.wallet.get("connectionStatus"), Wallet.ConnectionStatus.CONNECTED)) {
                TransferFragment.updateHeight(
                        String.valueOf(this.wallet.get("walletHeight")),
                        String.valueOf(this.wallet.get("nodeHeight")),
                        String.valueOf(this.wallet.get("nodeTarget"))
                );
            } else {
                TransferFragment.clear();
            }
        });
    }
    public Status getStatus() {
        return this.status;
    }
    public void setStatus(final Status status) {
        this.status = status;
    }
    public String getPassword() {
        return preferences.getString("password", this.TAG);
    }
}