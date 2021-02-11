package com.aeon.wallet.app;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aeon.wallet.app.models.HashMapPos;
import com.aeon.wallet.app.models.HashMapPosSaved;
import com.aeon.wallet.app.models.Transaction;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static com.aeon.wallet.app.MainActivity.preferences;
import static com.aeon.wallet.app.models.WalletThread.transactionPendingManager;
public class TransferFragment extends Fragment {
    private static final String TAG = TransferFragment.class.getName();
    private Button button_wallet, button_send, button_receive, button_contacts;
    private RecyclerView recentList;
    public static RecentAdapter recentAdapter;
    private static TextView text_pending, text_available, text_wallet_height, text_network_height,
            text_node_height, text_wallet_height_label, text_network_height_label,
            text_node_height_label, text_blocks_label;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transfer, container, false);
        collect(view);
        button_wallet.setOnClickListener(v -> MainActivity.goToFragment(R.id.navigation_wallet,v));
        button_send.setOnClickListener(v -> MainActivity.goToFragment(R.id.navigation_send,v));
        button_receive.setOnClickListener(v -> MainActivity.goToFragment(R.id.navigation_receive,v));
        button_contacts.setOnClickListener(v -> MainActivity.goToFragment(R.id.navigation_contact,v));
        text_wallet_height_label.setText(preferences.getString(R.string.title_wallet));
        text_node_height_label.setText(preferences.getString(R.string.title_node));
        text_network_height_label.setText(preferences.getString(R.string.row_node_target));
        text_blocks_label.setText(preferences.getString(R.string.text_blocks));
        recentAdapter = new RecentAdapter();
        recentList.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        recentList.setAdapter(recentAdapter);
        return view;
    }
    private void collect(View view){
        text_available = view.findViewById(R.id.text_available);
        text_pending = view.findViewById(R.id.text_balance);
        text_wallet_height = view.findViewById(R.id.text_wallet_height);
        text_node_height = view.findViewById(R.id.text_node_height);
        text_network_height = view.findViewById(R.id.text_network_height);
        text_wallet_height_label = view.findViewById(R.id.text_wallet_height_label);
        text_node_height_label = view.findViewById(R.id.text_node_height_label);
        text_network_height_label= view.findViewById(R.id.text_network_height_label);
        text_blocks_label= view.findViewById(R.id.text_blocks_label);
        button_wallet = view.findViewById(R.id.transfer_button_wallet);
        button_send = view.findViewById(R.id.transfer_button_send);
        button_receive = view.findViewById(R.id.transfer_button_receive);
        button_contacts = view.findViewById(R.id.transfer_button_contacts);
        recentList = view.findViewById(R.id.rv_recent_item_list);
    }
    @Override public void onResume() {
        super.onResume();
        updateHeight(
                preferences.getString("text_wallet_height",TAG),
                preferences.getString("text_node_height",TAG),
                preferences.getString("text_network_height",TAG)
        );
        updateBalance(
                preferences.getString("text_available",TAG),
                preferences.getString("text_pending",TAG)
        );
        transactionPendingManager.disposeTransaction();
    }
    public static void clear(){
        if(text_wallet_height !=null) {
            updateHeight("","","");
            updateBalance("","");
        }
    }
    public static void updateBalance(String available, String unconfirmed){
        if(text_available !=null) {
            text_available.setText( addCommas(available));
            preferences.putString("text_available",available,TAG);
            if(unconfirmed.equals("0") || unconfirmed.equals("")){
                text_pending.setText(null);
                preferences.putString("text_pending","0",TAG);
                if(text_pending.getVisibility() == View.VISIBLE) {
                    text_pending.setVisibility(View.GONE);
                }
            } else {
                if(text_pending.getVisibility() == View.GONE) {
                    text_pending.setVisibility(View.VISIBLE);
                }
                preferences.putString("text_pending",addCommas(unconfirmed),TAG);
                text_pending.setText(
                        new StringBuilder()
                                .append("+")
                                .append(addCommas(unconfirmed))
                );
            }
        }
    }
    public static String addCommas(String digits) {
        if(digits.contains(",")){
            return digits;
        }
        String result = "";
        for (int i=1; i <= digits.length(); ++i) {
            char ch = digits.charAt(digits.length() - i);
            if (i % 3 == 1 && i > 1) {
                result = "," + result;
            }
            result = ch + result;
        }

        return result;
    }
    public static void updateHeight(String walletHeight, String nodeHeight, String networkHeight){
        if(text_wallet_height !=null) {
            text_wallet_height.setText(walletHeight);
            preferences.putString("text_wallet_height",walletHeight,TAG);
            text_node_height.setText(nodeHeight);
            preferences.putString("text_node_height",nodeHeight,TAG);
            if(!networkHeight.equals("")&&!nodeHeight.equals("")&&Long.parseLong(networkHeight)<Long.parseLong(nodeHeight)){
                text_network_height.setText(nodeHeight);
                preferences.putString("text_network_height",nodeHeight,TAG);
            } else {
                text_network_height.setText(networkHeight);
                preferences.putString("text_network_height",networkHeight,TAG);
            }
        }
    }
    public static class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
        public static final HashMapPosSaved ITEMS = new HashMapPosSaved(
                RecentAdapter.class.getName(),Comparator.reverseOrder()
        );
        @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_recent_item, parent, false);
            return new ViewHolder(view);
        }
        @Override public void onBindViewHolder(final ViewHolder holder, int position) {
            String key = ITEMS.getKey(position);
            holder.mItem = (Transaction) ITEMS.get(key);
            String amount;
            if(holder.mItem.get("direction").equals(Transaction.Direction.In.name())) {
                amount = "+"+addCommas(String.valueOf(holder.mItem.get("amount")));
            } else {
                amount = "-"+addCommas(String.valueOf((Long)holder.mItem.get("amount")+(Long)holder.mItem.get("fee")));
            }
            if((Long)holder.mItem.get("height") == 0) {
                holder.mDateView.setText(
                        new StringBuilder()
                                .append(amount)
                                .append("::")
                                .append("Pending").toString()
                );
            } else {
                holder.mDateView.setText(
                        new StringBuilder()
                                .append(amount)
                                .append("::")
                                .append(holder.mItem.get("height"))
                );
            }
        }
        public static void setData(HashMapPos newData) {
            final RecentDiff diffCallback = new RecentDiff(ITEMS, newData);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            ITEMS.clear();
            ITEMS.putAll(newData);
            if(TransferFragment.recentAdapter!=null) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> diffResult.dispatchUpdatesTo(TransferFragment.recentAdapter));
            }
        }
        @Override public int getItemCount() {
            return ITEMS.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mDateView;
            public Transaction mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mDateView = view.findViewById(R.id.text_tx);
            }
        }
    }
    private static class RecentDiff extends HashMapPos.DiffCallback{

        public RecentDiff(HashMapPosSaved oldEmployeeList, HashMapPos newEmployeeList) {
            super(oldEmployeeList, newEmployeeList);
        }
        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final String oldString = String.valueOf(((Transaction)mOldList.get(mOldList.getKey(oldItemPosition))).get("height"));
            final String newString = String.valueOf(((Transaction)mNewList.get(mNewList.getKey(newItemPosition))).get("height"));
            return oldString.equals(newString);
        }
    }
}