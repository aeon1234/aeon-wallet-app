package com.aeon.wallet.app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import static com.aeon.wallet.app.MainActivity.goToFragment;
import static com.aeon.wallet.app.MainActivity.walletThread;
import static com.aeon.wallet.app.models.WalletThread.transactionPendingManager;

public class SendPendingFragment extends Fragment {
    private static final String TAG = SendPendingFragment.class.getName();
    private TextView text_transfer_info;
    private TextView text_pending_amount,text_amount_label,text_fee_label, text_pending_fee,text_transfer_plus;
    private EditText text_transfer_password;
    private ProgressBar progressBar;
    private Button understood;
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_send_pending, container, false);
        collect(v);
        new Thread(() -> {
            while (transactionPendingManager.isTransactionQueued()&&!transactionPendingManager.isTransactionReady() ) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(transactionPendingManager.isTransactionQueued()&&transactionPendingManager.isTransactionReady()){
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    text_transfer_info.setText(transactionPendingManager.getAddressInfo());
                    text_pending_amount.setVisibility(View.VISIBLE);
                    text_amount_label.setVisibility(View.VISIBLE);
                    text_pending_amount.setText(TransferFragment.addCommas(transactionPendingManager.getAmountInfo()));
                    text_pending_fee.setVisibility(View.VISIBLE);
                    text_fee_label.setVisibility(View.VISIBLE);
                    text_transfer_plus.setVisibility(View.VISIBLE);
                    text_transfer_plus.setText("+");
                    text_pending_fee.setText(TransferFragment.addCommas(transactionPendingManager.getFeeInfo()));
                    understood.setOnClickListener(this::confirmTransaction);
                });
            }
        }).start();
        return v;
    }
    private void collect(View v){
        text_transfer_info = v.findViewById(R.id.text_transfer_info);
        text_pending_amount = v.findViewById(R.id.text_pending_amount);
        text_amount_label = v.findViewById(R.id.text_amount_label);
        text_pending_fee = v.findViewById(R.id.text_pending_fee);
        text_fee_label = v.findViewById(R.id.text_fee_label);
        text_transfer_plus = v.findViewById(R.id.text_transfer_plus);
        text_transfer_password = v.findViewById(R.id.text_transfer_password);
        progressBar = v.findViewById(R.id.progressBar);
        understood = v.findViewById(R.id.button_confirm_send);
    }
    public void confirmTransaction(View v) {
        if (text_transfer_password.getText().toString().equals(walletThread.getPassword())) {
            transactionPendingManager.confirmTransaction();
            text_transfer_info.setText(transactionPendingManager.getAddressInfo());
            text_pending_amount.setText(transactionPendingManager.getAmountInfo());
            progressBar.setVisibility(View.VISIBLE);
            text_pending_amount.setVisibility(View.GONE);
            text_pending_fee.setVisibility(View.GONE);
            text_amount_label.setVisibility(View.GONE);
            text_fee_label.setVisibility(View.GONE);
            text_transfer_plus.setVisibility(View.GONE);
            goToFragment(R.id.navigation_transfer, v);
        }
        text_transfer_password.setText("");
    }
}