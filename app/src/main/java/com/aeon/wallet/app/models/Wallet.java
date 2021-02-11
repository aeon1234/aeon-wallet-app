package com.aeon.wallet.app.models;
import android.util.Log;

import com.aeon.wallet.app.ReceiveFragment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
public class Wallet extends HashMap<String, Serializable>{
    static {
        System.loadLibrary("wallet2_api_jni");
    }
    private static final String TAG = Wallet.class.getName();
    private static final int ADDRESS_COUNT = 21;
    private long handle;
    private TransactionHistory transactionHistory;
    private final ArrayList<String> addresses = new ArrayList<>();
    enum NativeStatus {
        Ok,
        Error,
        Critical
    }
    public enum Status {
        EXISTS,
        FILE_CREATED,
        INIT,
        SYNCHRONIZING,
        SYNCHRONIZED,
        CLOSED
    }
    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTED,
        WRONG_VERSION
    }
    public Wallet(final String path){
        Log.v(Wallet.TAG, "Wallet");
        this.put("status",Status.EXISTS);
        final String[] pathArray = path.split("/");
        this.put("name",pathArray[pathArray.length-1]);
        this.put("path",path);
        this.put("node","127.0.0.1");
    }
    public Wallet(final String path, final String password, final String language){
        this(path);
        this.put("password",password);
        this.put("language",language);
    }
    public Wallet(final String path, final String password, final String language,
                  final String seed, final long restoreHeight){
        this(path,password,language);
        this.put("seed",seed);
        this.put("restoreHeight",restoreHeight);
    }
    public Wallet(final String path, final String password, final String language,
                  final String seed, final long restoreHeight,
                  final String account, final String view, final String spend) {
        this(path,password,language,seed,restoreHeight);
        this.put("account",account);
        this.put("viewPrivateKey",view);
        this.put("spendPrivateKey",spend);
    }
    public void createFiles(){
        Log.v(Wallet.TAG, "create");
        if(this.isExistsJNI((String) this.get("path"))){
            Log.v(Wallet.TAG, "isExistsJNI");
            this.handle = this.openWalletJNI(
                    (String) this.get("path"),
                    (String) this.get("password")
            );
        } else if(this.get("seed") != null ){
            Log.v(Wallet.TAG, "this.seed != null");
            this.handle = this.createFromSeedJNI(
                    (String) this.get("path"),
                    (String) this.get("password"),
                    (String) this.get("seed"),
                    (Long) this.get("restoreHeight")
            );
        } else if(this.get("secretViewKey") != null){
            Log.v(Wallet.TAG, "this.secretViewKey != null");
            this.handle = this.createFromKeysJNI(
                    (String) this.get("path"),
                    (String) this.get("password"),
                    (String) this.get("account"),
                    (String) this.get("viewPrivateKey"),
                    (String) this.get("spendPrivateKey"),
                    (Long) this.get("restoreHeight")
            );
        } else {
            Log.v(Wallet.TAG, "createJNI");
            this.handle = this.createJNI(
                    (String) this.get("path"),
                    (String) this.get("password"),
                    (String) this.get("language")
            );
        }
        if(this.isExistsJNI((String) this.get("path"))){
            this.put("status",Status.FILE_CREATED);
        }
        this.setTransactionHistory(new TransactionHistory(this.getTransactionHistoryJNI()));
        this.put("nativeStatus",NativeStatus.values()[this.getStatusJNI()]);
        this.put("address", this.getAddressJNI(0,0));
        this.put("account", this.getAddressJNI(0,0));
        this.put("seed", this.getSeedJNI());
        this.put("spendPublicKey", this.getPublicSpendKeyJNI());
        this.put("viewPublicKey", this.getPublicViewKeyJNI());
        this.put("spendPrivateKey", this.getSecretSpendKeyJNI());
        this.put("viewPrivateKey", this.getSecretViewKeyJNI());
        this.put("balance",0L);
        this.put("unlockedBalance",0L);
        this.storeJNI("");
    }
    public long createTransaction(final TransactionPending tx) {
        Log.v(Wallet.TAG, "createTransaction");
        long balance = this.getUnlockedBalanceJNI(0);
        if(Math.abs(tx.amount - balance) < 1){ // the difference is less than 1 atomic unit
            return this.createSweepAllJNI(tx.recipient,tx.paymentID,3, tx.priority.ordinal());
        } else {
            return this.createTransactionJNI(tx.recipient, tx.paymentID, tx.amount, 3, tx.priority.ordinal());
        }
    }
    public void disposeTransaction(final TransactionPending pendingTransaction){
        Log.v(Wallet.TAG, "disposeTransaction");
        this.disposeTransactionJNI(pendingTransaction);
    }
    public void init(){
        Log.v(Wallet.TAG, "init");
        if(this.initJNI(
                "127.0.0.1:11181",
                0,
                "",
                "")){
            this.put("status",Status.INIT);
        }
    }
    public void refresh(){
        Log.v(Wallet.TAG, "refresh >> "+ this.handle);
        this.getWalletInfo();
        this.getNodeInfo();
        this.loadNewAddresses();
    }
    private void getWalletInfo(){
        this.put("status", this.isSynchronizedJNI()? Status.SYNCHRONIZED: Status.SYNCHRONIZING);
        this.put("balance",this.getBalanceJNI(0));
        this.put("unlockedBalance",this.getUnlockedBalanceJNI(0));
        if(this.get("status") == Status.SYNCHRONIZED) {
            if (this.getTransactionHistory().hasNewTransaction()) {
                this.storeJNI("");
                this.getTransactionHistory().switchHasNewTransaction();
            }
        }
    }
    private void getNodeInfo(){
        this.put("connectionStatus" , ConnectionStatus.values()[this.getConnectionStatusJNI()]);
        if(this.get("connectionStatus") == ConnectionStatus.CONNECTED) {
            this.startRefreshJNI();
            if(this.get("status") == Status.SYNCHRONIZED) {
                this.getTransactionHistory().refresh();
            }
            this.put("nodeHeight", this.getDaemonBlockChainHeightJNI());
            this.put("nodeTarget", this.getDaemonBlockChainTargetHeightJNI());
            this.put("nodeVersion", this.getDaemonVersionJNI());
            this.put("walletHeight", this.getBlockChainHeightJNI());
            this.put("restoreHeight", this.getRefreshFromBlockHeightJNI());
        }
    }
    private void loadNewAddresses(){
        if(this.getAddresses().size()< Wallet.ADDRESS_COUNT){
            if (ReceiveFragment.AddressAdapter.ITEMS.size() > this.getAddresses().size()) {
                this.getAddresses().clear();
            }
            if (this.getAddresses().size() < Wallet.ADDRESS_COUNT) {
                this.getAddresses().add(this.getAddressJNI(0, this.getAddresses().size()));
            }
        }
    }
    public TransactionHistory getTransactionHistory() {
        return this.transactionHistory;
    }
    public void setTransactionHistory(final TransactionHistory transactionHistory) {
        this.transactionHistory = transactionHistory;
    }
    public ArrayList<String> getAddresses() {
        return this.addresses;
    }
    private native long createTransactionJNI(String dst_address, String payment_id, long amount,
                                             int ring_size, int priority);
    private native long createJNI(String path, String password, String language);
    private native long createFromSeedJNI(String path, String password, String seed,
                                          long restoreHeight);
    private native long createFromKeysJNI(String path, String password, String address,String view,
                                          String spend, long restoreHeight);
    private native long createSweepAllJNI(String dst_address, String payment_id, int ring_size,
                                          int priority);
    private native void disposeTransactionJNI(TransactionPending pendingTransaction);
    private native boolean initJNI(String daemon_address, long upper_transaction_size_limit,
                                   String daemon_username, String daemon_password);
    private native boolean isExistsJNI(String path);
    private native boolean isSynchronizedJNI();
    private native String getAddressJNI(int accountIndex, int addressIndex);
    private native long getBalanceJNI(int accountIndex);
    private native long getBlockChainHeightJNI();
    private native int getConnectionStatusJNI();
    private native long getDaemonBlockChainHeightJNI();
    private native long getDaemonBlockChainTargetHeightJNI();
    private native int getDaemonVersionJNI();
    private native String getPublicSpendKeyJNI();
    private native String getPublicViewKeyJNI();
    private native long getRefreshFromBlockHeightJNI();
    private native String getSecretSpendKeyJNI();
    private native String getSecretViewKeyJNI();
    private native String getSeedJNI();
    private native int getStatusJNI();
    private native long getTransactionHistoryJNI();
    private native long getUnlockedBalanceJNI(int accountIndex);
    private native long openWalletJNI(String path,String password);
    private native void startRefreshJNI();
    private native void storeJNI(String path);
}
