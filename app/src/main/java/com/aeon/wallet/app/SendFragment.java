package com.aeon.wallet.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aeon.wallet.app.models.TransactionPending;
import com.aeon.wallet.app.util.Base58;

import static com.aeon.wallet.app.MainActivity.goToFragment;
import static com.aeon.wallet.app.models.WalletThread.transactionPendingManager;

public class SendFragment extends Fragment {
    private EditText recipientInfo;
    private EditText amountInfo, paymentID;
    private CheckBox checkPaymentId;
    private Button startTransaction, button_zeros;
    private RadioGroup radioGroup;
    private ImageButton scanQR;
    private RecyclerView rv;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_send, container, false);
        collect(view);
        SearchAdapter searchAdapter = new SearchAdapter(recipientInfo, view.findViewById(R.id.transfer_amount_info));
        rv.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        rv.setAdapter(searchAdapter);
        recipientInfo.addTextChangedListener(searchAdapter);
        if(getArguments()!=null && !getArguments().isEmpty()){
            recipientInfo.setText(getArguments().getString("barcode"));
        }
        checkPaymentId.setOnCheckedChangeListener(this::switchPaymentID);
        startTransaction.setOnClickListener(this::createTransaction);
        scanQR.setOnClickListener(this::scanQR);
        button_zeros.setOnClickListener(this::addZeroes);
        return view;
    }
    private void collect(View view){
        rv = view.findViewById(R.id.rv_send_contact_list);
        recipientInfo = view.findViewById(R.id.transfer_recipient_info);
        amountInfo = view.findViewById(R.id.transfer_amount_info);
        checkPaymentId = view.findViewById(R.id.check_payment_id);
        paymentID = view.findViewById(R.id.text_payment_id);
        startTransaction = view.findViewById(R.id.button_start_transaction);
        scanQR = view.findViewById(R.id.button_send_qr);
        button_zeros = view.findViewById(R.id.button_zeros);
        radioGroup = view.findViewById(R.id.radioGroup);
    }
    @Override public void onResume() {
        paymentID.setVisibility(View.GONE);
        transactionPendingManager.disposeTransaction();
        recipientInfo.requestFocus();
        super.onResume();
    }
    private void switchPaymentID(View v, boolean isChecked) {
        if(isChecked){
            paymentID.setVisibility(View.VISIBLE);
        } else {
            paymentID.setVisibility(View.GONE);
        }
    }
    private void addZeroes(View v){
        String amount = amountInfo.getText().toString();
        if(!amount.equals("")){
            amountInfo.setText(amount+"000");
            amountInfo.setSelection(amount.length()+3);
        }
    }
    private void scanQR(View v) {
        Bundle bundle = new Bundle();
        bundle.putInt("return", R.id.navigation_send);
        goToFragment(R.id.navigation_qr,v,bundle);
    }
    private void createTransaction(View v) {
        if (!recipientInfo.getText().toString().equals("") &&
                !amountInfo.getText().toString().equals("")) {
            boolean success;
            TransactionPending.Priority priority;
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.radio_auto:
                    priority = TransactionPending.Priority.AUTOMATIC;
                    break;
                case R.id.radio_high:
                    priority = TransactionPending.Priority.HIGH;
                    break;
                case R.id.radio_low:
                    priority = TransactionPending.Priority.LOW;
                    break;
                default:
                    priority = TransactionPending.Priority.AUTOMATIC;
            }
            if (Base58.isValidAddress(recipientInfo.getText().toString())) {
                if (paymentID.getVisibility() == View.VISIBLE && !paymentID.getText().toString().equals("")) {
                    success = transactionPendingManager.queueTransaction(
                            recipientInfo.getText().toString(),
                            Long.parseLong(amountInfo.getText().toString()),
                            priority,
                            paymentID.getText().toString()
                    );
                } else {
                    success = transactionPendingManager.queueTransaction(
                            recipientInfo.getText().toString(),
                            Long.parseLong(amountInfo.getText().toString()),
                            priority
                    );
                }
                if (success) {
                    goToFragment(R.id.navigation_transaction_pending,v);
                } else {
                }
            } else {
            }
        }
    }
    private class SearchAdapter extends ContactFragment.ContactAdapter implements TextWatcher {
        private final EditText recipient, amount;
        private String query;
        public SearchAdapter(EditText recipient, EditText amount) {
            this.recipient = recipient;
            this.amount = amount;
        }
        @Override public void onBindViewHolder(final ContactFragment.ContactAdapter.ViewHolder holder, int position) {
            String key = ITEMS.getKey(position);
            String address =  (String) ContactFragment.ContactAdapter.ITEMS.get(key);
            holder.mLabel.setText(key);
            holder.mAddress.setText(address);
            holder.mAddress.setSelectAllOnFocus(false);
            holder.mAddress.setClickable(false);
            holder.mAddress.setFocusable(false);
            holder.mAddress.setTextIsSelectable(false);
            if(query !=null && query.length()>0) {
                if (address.length() >= query.length() && address.startsWith(query)) {
                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(address);
                    spanText.setSpan(new BackgroundColorSpan(holder.mAddress.getHighlightColor()),
                            0, query.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.mAddress.setText(spanText);
                    holder.mView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(
                            new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT));
                } else if (key.length() >= query.length() && key.startsWith(query)) {
                    Spannable spanText = Spannable.Factory.getInstance().newSpannable(key);
                    spanText.setSpan(new BackgroundColorSpan(holder.mAddress.getHighlightColor()),
                            0, query.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                    holder.mLabel.setText(spanText);
                    holder.mView.setVisibility(View.VISIBLE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                } else {
                    holder.mView.setVisibility(View.GONE);
                    holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            } else {
                holder.mView.setVisibility(View.VISIBLE);
                holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            holder.mView.setOnClickListener(v -> {
                recipient.setText(address);
                amount.requestFocus();
            });
        }
        private void setQuery(String query){
            this.query = query;
            notifyDataSetChanged();
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override public void afterTextChanged(Editable s) {
            setQuery(s.toString());

        }
    }
}

