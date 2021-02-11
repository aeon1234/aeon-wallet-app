package com.aeon.wallet.app;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aeon.wallet.app.models.HashMapPos;
import com.aeon.wallet.app.models.HashMapPosSaved;
import com.aeon.wallet.app.util.qr.BitmapUtils;
import com.google.zxing.WriterException;
public class ReceiveFragment extends Fragment {
    public static AddressAdapter addressAdapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive, container, false);
        RecyclerView rv = view.findViewById(R.id.rv_address_list);
        rv.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        addressAdapter = new AddressAdapter();
        rv.setAdapter(addressAdapter);
        return view;
    }
    public static class AddressAdapter extends RecyclerView.Adapter<AddressAdapter.ViewHolderAddress> {
        public static final HashMapPosSaved ITEMS = new HashMapPosSaved(AddressAdapter.class.getName());
        @Override public ViewHolderAddress onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_address_item, parent, false);
            return new ViewHolderAddress(view);
        }
        @Override public void onBindViewHolder(final ViewHolderAddress holder, int position) {
            String key = ITEMS.getKey(position);
            holder.mAddress.setText( (String) ITEMS.get(key));
            holder.mLabel.setText(
                    holder.getAdapterPosition() == 0 ?
                            "Account" :
                            "Address " +
                                    holder.getAdapterPosition()
            );
            holder.mView.setOnClickListener(v -> {
                if (holder.mQRView.getVisibility() == View.GONE) {
                    holder.mQRView.setVisibility(View.VISIBLE);
                    try {
                        holder.mQRView.setImageBitmap(BitmapUtils.textToImage((String) ITEMS.get(key)));
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                } else {
                    holder.mQRView.setVisibility(View.GONE);
                }
            });
        }
        @Override public int getItemCount() {
            return ITEMS.size();
        }
        public static class ViewHolderAddress extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mLabel, mAddress;
            public final ImageView mQRView;
            public ViewHolderAddress(View view) {
                super(view);
                mView = view;
                mLabel = view.findViewById(R.id.address_item_label);
                mAddress = view.findViewById(R.id.address_item_text);
                mQRView = view.findViewById(R.id.imageView_qr);
            }
        }
        public static void setData(HashMapPos newData) {
            final HashMapPos.DiffCallback diffCallback = new HashMapPos.DiffCallback(ITEMS, newData);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            ITEMS.clear();
            ITEMS.putAll(newData);
            if(ReceiveFragment.addressAdapter!=null) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> diffResult.dispatchUpdatesTo(ReceiveFragment.addressAdapter));
            }
        }
    }
}