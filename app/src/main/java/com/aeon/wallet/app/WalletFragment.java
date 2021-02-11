package com.aeon.wallet.app;
import android.content.Context;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.aeon.wallet.app.models.HashMapPos;
import com.aeon.wallet.app.models.HashMapPosSaved;
import com.aeon.wallet.app.models.WalletThread;
import com.aeon.wallet.app.util.SeedWords;

import static com.aeon.wallet.app.MainActivity.goToFragment;
import static com.aeon.wallet.app.MainActivity.walletThread;

public class WalletFragment extends Fragment {
    public static WalletAdapter walletAdapter;
    private Context context;
    private TextView passwordInfo;
    private ConstraintLayout walletFragmentLayout, seedLayout,secretInfoLayout;
    private EditText password, passwordConfirm, text_restore_height_seed;
    private Button confirmNewWallet, confirmDelete, confirmShowSecrets, createNew, fromSeed,
            showSecrets, deleteWallet,showSeedLayout;
    private MultiAutoCompleteTextView seedInput;
    public static void openWalletView(View view){
        Group g = view.findViewById(R.id.group_wallet_off);
        g.setVisibility(View.GONE);
        g = view.findViewById(R.id.group_wallet_on);
        g.setVisibility(View.VISIBLE);
    }
    public static void closeWalletView(View view){
        Group g = view.findViewById(R.id.group_wallet_off);
        g.setVisibility(View.VISIBLE);
        g = view.findViewById(R.id.group_wallet_on);
        g.setVisibility(View.GONE);
    }
    public static void hideUI(View view){
        Group g = view.findViewById(R.id.group_wallet_off);
        g.setVisibility(View.GONE);
        g = view.findViewById(R.id.group_wallet_on);
        g.setVisibility(View.GONE);
    }
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallet, container, false);
        collect(view);
        showSeedLayout.setOnClickListener(this::showSeedLayout);
        fromSeed.setOnClickListener(this::createWalletFromSeed);
        createNew.setOnClickListener(this::createWallet);
        deleteWallet.setOnClickListener(this::removeWallet);
        showSecrets.setOnClickListener(this::showSecretInfo);
        if(walletThread.getStatus() == WalletThread.Status.MANAGING){
            openWalletView(view);
        } else {
            closeWalletView(view);
        }
        context = view.getContext();
        RecyclerView rv = view.findViewById(R.id.rv_wallet_info_list);
        rv.setLayoutManager(new GridLayoutManager(context, 1));
        walletAdapter = new WalletAdapter();
        rv.setAdapter(walletAdapter);
        return view;
    }
    private void collect(View view){
        walletFragmentLayout = view.findViewById(R.id.layout_wallet);
        seedLayout = view.findViewById(R.id.layout_seed_input);
        secretInfoLayout = view.findViewById(R.id.layout_secret_info);
        passwordInfo = view.findViewById(R.id.text_wallet_password_info);
        password = view.findViewById(R.id.text_wallet_password);
        passwordConfirm = view.findViewById(R.id.text_wallet_password_confirm);
        confirmNewWallet = view.findViewById(R.id.button_confirm_new_wallet);
        confirmDelete = view.findViewById(R.id.button_confirm_delete);
        confirmShowSecrets = view.findViewById(R.id.button_confirm_show_secret_info);
        createNew = view.findViewById(R.id.button_create_wallet);
        showSeedLayout = view.findViewById(R.id.button_from_seed);
        fromSeed = view.findViewById(R.id.button_create_wallet_from_seed);
        deleteWallet = view.findViewById(R.id.button_delete_wallet);
        showSecrets = view.findViewById(R.id.button_show_secret_info);
        text_restore_height_seed = seedLayout.findViewById(R.id.text_restore_height_seed);
        seedInput = seedLayout.findViewById(R.id.text_seed_input);
    }
    public void createWallet(View v) {
        if (walletThread.getStatus() == WalletThread.Status.STANDBY ||
                walletThread.getStatus() == WalletThread.Status.DESTROYED) {
            WalletFragment.hideUI(walletFragmentLayout);
            passwordInfo.setVisibility(View.VISIBLE);
            password.setVisibility(View.VISIBLE);
            passwordConfirm.setVisibility(View.VISIBLE);
            confirmNewWallet.setVisibility(View.VISIBLE);
            password.requestFocus();
            confirmNewWallet.setOnClickListener(v1 -> {
                if (password.getText().toString().equals(passwordConfirm.getText().toString())
                        && !password.getText().toString().equals("")) {
                    walletThread = new WalletThread();
                    walletThread.start();
                    walletThread.queueWallet(context.getFilesDir().getAbsolutePath() + "/" +
                            System.currentTimeMillis(), password.getText().toString());
                    WalletFragment.openWalletView(walletFragmentLayout);
                    seedLayout.setVisibility(View.GONE);
                } else {
                    WalletFragment.closeWalletView(walletFragmentLayout);
                }
                password.setText("");
                passwordConfirm.setText("");
                passwordInfo.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                passwordConfirm.setVisibility(View.GONE);
                confirmNewWallet.setVisibility(View.INVISIBLE);
            });
        }
    }
    public void createWalletFromSeed(View v) {
        if (!text_restore_height_seed.getText().toString().equals("") &&
                !seedInput.getText().toString().equals("")) {
            String seed = seedInput.getText().toString().trim();
            long restoreHeight = Long.parseLong(text_restore_height_seed.getText().toString());
            if (walletThread.getStatus() == WalletThread.Status.STANDBY ||
                    walletThread.getStatus() == WalletThread.Status.DESTROYED) {
                seedLayout.setVisibility(View.GONE);
                WalletFragment.hideUI(walletFragmentLayout);
                passwordInfo.setVisibility(View.VISIBLE);
                password.setVisibility(View.VISIBLE);
                passwordConfirm.setVisibility(View.VISIBLE);
                confirmNewWallet.setVisibility(View.VISIBLE);
                password.requestFocus();
                confirmNewWallet.setOnClickListener(v1 -> {
                    if (password.getText().toString().equals(passwordConfirm.getText().toString())
                            && !password.getText().toString().equals("")) {
                        walletThread = new WalletThread();
                        walletThread.start();
                        walletThread.queueWallet(
                                context.getFilesDir().getAbsolutePath() +
                                        "/" +
                                        System.currentTimeMillis(), password.getText().toString(),
                                seed,
                                restoreHeight
                        );
                        goToFragment(R.id.navigation_wallet,v);
                        WalletFragment.openWalletView(walletFragmentLayout);
                        seedLayout.setVisibility(View.GONE);
                    } else {
                        WalletFragment.closeWalletView(walletFragmentLayout);
                    }
                    password.setText("");
                    passwordConfirm.setText("");
                    passwordInfo.setVisibility(View.GONE);
                    password.setVisibility(View.GONE);
                    passwordConfirm.setVisibility(View.GONE);
                    confirmNewWallet.setVisibility(View.INVISIBLE);
                });
            }
        }
    }
    public void showSeedLayout(View v) {
        seedLayout.setVisibility(View.VISIBLE);
        WalletFragment.hideUI(walletFragmentLayout);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, SeedWords.words);
        seedInput.setAdapter(adapter);
        seedInput.setTokenizer(new SpaceTokenizer());
        seedInput.requestFocus();
    }
    public void showSecretInfo(View v) {
        WalletFragment.hideUI(walletFragmentLayout);
        password.setVisibility(View.VISIBLE);
        confirmShowSecrets.setVisibility(View.VISIBLE);
        password.requestFocus();
        confirmShowSecrets.setOnClickListener(v1 -> {
            if (password.getText().toString().equals(walletThread.getPassword())) {
                secretInfoLayout.setVisibility(View.VISIBLE);
                WalletFragment.hideUI(walletFragmentLayout);
                TextView seed = secretInfoLayout.findViewById(R.id.text_seed_window);
                TextView vpk = secretInfoLayout.findViewById(R.id.text_vpk);
                TextView spk = secretInfoLayout.findViewById(R.id.text_spk);
                seed.setText((String)WalletAdapter.ITEMS.get("seed"));
                vpk.setText((String)WalletAdapter.ITEMS.get("viewPrivateKey"));
                spk.setText((String)WalletAdapter.ITEMS.get("spendPrivateKey"));
            } else {
                WalletFragment.openWalletView(walletFragmentLayout);
            }
            password.setText("");
            password.setVisibility(View.GONE);
            confirmShowSecrets.setVisibility(View.GONE);
        });
    }
    public void removeWallet(View v) {
        WalletFragment.hideUI(walletFragmentLayout);
        password.setVisibility(View.VISIBLE);
        confirmDelete.setVisibility(View.VISIBLE);
        password.requestFocus();
        confirmDelete.setOnClickListener(v1 -> {
            if (password.getText().toString().equals(walletThread.getPassword())) {
                WalletFragment.closeWalletView(walletFragmentLayout);
                walletThread.interrupt();
            } else {
                WalletFragment.openWalletView(walletFragmentLayout);
            }
            password.setText("");
            password.setVisibility(View.GONE);
            confirmDelete.setVisibility(View.GONE);
        });
    }
    public static class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.ViewHolder> {
        public static final HashMapPosSaved ITEMS = new HashMapPosSaved(WalletAdapter.class.getName());
        @Override public WalletAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.layout_wallet_item, parent, false);
            return new WalletAdapter.ViewHolder(view);
        }
        @Override public void onBindViewHolder(final WalletAdapter.ViewHolder holder, int position) {
            String key = ITEMS.getKey(position);
            if(isSecretInfo(key)) {
                holder.mIdView.setVisibility(View.GONE);
                holder.mContentView.setVisibility(View.GONE);
            } else {
                holder.mIdView.setText(key);
                holder.mContentView.setText(String.valueOf(ITEMS.get(key)));
            }
        }
        private boolean isSecretInfo(String id){
            return id.equals("seed")||
                    id.equals("viewPrivateKey")||
                    id.equals("spendPrivateKey")||
                    id.equals("password");
        }
        @Override public int getItemCount() {
            return ITEMS.keySet().size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = view.findViewById(R.id.item_number);
                mContentView = view.findViewById(R.id.text_tx_amount);
            }
        }
        public static void setData(HashMapPos newData) {
            final HashMapPos.DiffCallback diffCallback = new HashMapPos.DiffCallback(ITEMS, newData);
            final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);
            ITEMS.clear();
            ITEMS.putAll(newData);
            if(WalletFragment.walletAdapter!=null) {
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> diffResult.dispatchUpdatesTo(WalletFragment.walletAdapter));
            }
        }
    }
    private class SpaceTokenizer implements MultiAutoCompleteTextView.Tokenizer {
        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;

            while (i > 0 && text.charAt(i - 1) != ' ') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }

            return i;
        }
        public int findTokenEnd(CharSequence text, int cursor) {
            int i = cursor;
            int len = text.length();

            while (i < len) {
                if (text.charAt(i) == ' ') {
                    return i;
                } else {
                    i++;
                }
            }

            return len;
        }
        public CharSequence terminateToken(CharSequence text) {
            int i = text.length();

            while (i > 0 && text.charAt(i - 1) == ' ') {
                i--;
            }

            if (i > 0 && text.charAt(i - 1) == ' ') {
                return text;
            } else {
                if (text instanceof Spanned) {
                    SpannableString sp = new SpannableString(text + " ");
                    TextUtils.copySpansFrom((Spanned) text, 0, text.length(),
                            Object.class, sp, 0);
                    return sp;
                } else {
                    return text + " ";
                }
            }
        }
    }
}