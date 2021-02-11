package com.aeon.wallet.app;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aeon.wallet.app.models.HashMapPos;
import com.aeon.wallet.app.models.HashMapPosSaved;
import com.aeon.wallet.app.util.Base58;
import com.aeon.wallet.app.util.qr.BitmapUtils;
import com.google.zxing.WriterException;

import static com.aeon.wallet.app.MainActivity.goToFragment;
public class ContactFragment extends Fragment {
    public static ContactAdapter contactAdapter;
    private EditText contactLabel, contactAddress;
    private Button deleteContact, addContact;
    private ImageButton scanQR;
    private RecyclerView contact_list;
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact, container, false);
        collect(view);
        Context context = view.getContext();
        deleteContact.setOnClickListener(this::deleteContact);
        addContact.setOnClickListener(this::addContact);
        scanQR.setOnClickListener(this::scanQR);
        if(getArguments()!=null && !getArguments().isEmpty()){
            addContact(view);
            contactAddress.setText(getArguments().getString("barcode"));
        }
        contactAdapter = new ContactAdapter();
        contact_list.setLayoutManager(new GridLayoutManager(context, 1));
        contact_list.setAdapter(contactAdapter);
        return view;
    }
    private void collect(View view){
        contact_list = view.findViewById(R.id.rv_contact_item_list);
        contactLabel = view.findViewById(R.id.text_contact_label);
        contactAddress = view.findViewById(R.id.text_contact_address);
        deleteContact = view.findViewById(R.id.button_delete_contact);
        addContact = view.findViewById(R.id.button_add_contact);
        scanQR = view.findViewById(R.id.button_contact_qr);
    }
    public void addContact(View v) {
        contactLabel.setEnabled(true);
        contactAddress.setEnabled(true);
        contactLabel.setVisibility(View.VISIBLE);
        contactAddress.setVisibility(View.VISIBLE);
        scanQR.setVisibility(View.VISIBLE);
        deleteContact.setVisibility(View.GONE);
        contactLabel.requestFocus();
        addContact.setOnClickListener(v1 -> {
            if (!contactLabel.getText().toString().equals("") &&
                    !contactAddress.getText().toString().equals("")) {
                if (Base58.isValidAddress(contactAddress.getText().toString())) {
                    HashMapPos hashMapPos = new HashMapPos();
                    hashMapPos.putAll(contactAdapter.ITEMS);
                    hashMapPos.put(
                            contactLabel.getText().toString(),
                            contactAddress.getText().toString());
                    ContactAdapter.setData(hashMapPos);
                }
                contactLabel.setEnabled(false);
                contactAddress.setEnabled(false);
                contactLabel.setText(null);
                contactAddress.setText(null);
                contactLabel.setVisibility(View.GONE);
                contactAddress.setVisibility(View.GONE);
                scanQR.setVisibility(View.GONE);
                deleteContact.setVisibility(View.VISIBLE);
                addContact.setOnClickListener(this::addContact);

            }
        });
    }
    public void deleteContact(View v) {
        contactLabel.setEnabled(true);
        contactLabel.setVisibility(View.VISIBLE);
        addContact.setVisibility(View.GONE);
        contactLabel.requestFocus();
        deleteContact.setOnClickListener(v1 -> {
            if (!contactLabel.getText().toString().equals("")&&
                    contactAdapter.ITEMS.containsKey(contactLabel.getText().toString())) {
                HashMapPos hashMapPos = new HashMapPos();
                hashMapPos.putAll(contactAdapter.ITEMS);
                hashMapPos.remove(contactLabel.getText().toString());
                ContactAdapter.setData(hashMapPos);
            }
            contactLabel.setEnabled(false);
            contactLabel.setText(null);
            contactLabel.setVisibility(View.GONE);
            addContact.setVisibility(View.VISIBLE);
            deleteContact.setOnClickListener(this::deleteContact);
        });
    }
    private void scanQR(View v) {
        Bundle bundle = new Bundle();
        bundle.putInt("return", R.id.navigation_contact);
        goToFragment(R.id.navigation_qr,v,bundle);
    }
    public static class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
        public static final HashMapPosSaved ITEMS = new HashMapPosSaved(ContactAdapter.class.getName());
        @Override public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_address_item, parent, false);
            return new ContactAdapter.ViewHolder(view);
        }
        @Override public void onBindViewHolder(final ContactAdapter.ViewHolder holder, int position) {
            String key = ITEMS.getKey(position);
            holder.mAddress.setText( (String) ITEMS.get(key));
            holder.mLabel.setText(key);
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
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mLabel, mAddress;
            public final ImageView mQRView;
            public ViewHolder(View view) {
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
            if(ContactFragment.contactAdapter!=null) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> diffResult.dispatchUpdatesTo(ContactFragment.contactAdapter));
            }
        }
    }
}