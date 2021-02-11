package com.aeon.wallet.app;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.aeon.wallet.app.models.WalletThread;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import java.util.Set;
public class MainActivity extends AppCompatActivity {
    public static Resources res;
    public static Preferences preferences;
    public static String packageName;
    public static WalletThread walletThread;
    private static final NavOptions navOptions =  new NavOptions.Builder()
            .setEnterAnim(R.anim.nav_default_enter_anim)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
                .build();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        res = getResources();
        packageName = getPackageName();
        preferences = new Preferences();
        walletThread = new WalletThread();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (item.getItemId() == android.R.id.home) {
            Navigation.findNavController(
                    this,
                    R.id.nav_host_fragment
            ).navigate(
                    R.id.navigation_transfer,
                    null,
                    getNavOptions()
            );
            return super.onOptionsItemSelected(item);
        } else {
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        }
    }
    public static NavOptions getNavOptions(){
        return navOptions;
    }
    public static void goToFragment(int id, View v) {
        goToFragment(id,v,null);
    }
    public static void goToFragment(int id, View v, Bundle args) {
        androidx.navigation.Navigation.findNavController(
                v
        ).navigate(
                id,
                args,
                getNavOptions()
        );
    }
    @Override
    public void onBackPressed() {
        Navigation.findNavController(
                this,
                R.id.nav_host_fragment
        ).navigate(
                R.id.navigation_transfer,
                null,
                getNavOptions()
        );
    }
    public class Preferences {
        public void putString(String key, String value, String prefsName) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefsName, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(key, value);
            editor.apply();
        }
        public void removeString(String key, String prefsName) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefsName, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove(key);
            editor.apply();
        }
        public String getString(String key, String prefsName) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefsName, MODE_PRIVATE);
            return sharedPref.getString(key, "");
        }
        public Set<String> getKeys(String prefsName) {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(prefsName, MODE_PRIVATE);
            return sharedPref.getAll().keySet();
        }
        public String getString(int key) {
            return getApplicationContext().getResources().getString(key);
        }
    }
}