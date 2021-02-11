package com.aeon.wallet.app.models;

import androidx.annotation.Nullable;

import com.aeon.wallet.app.util.ObjectSerializer;

import java.io.Serializable;
import java.util.Comparator;

import static com.aeon.wallet.app.MainActivity.preferences;

public class HashMapPosSaved extends HashMapPos {
    private final String TAG;
    public HashMapPosSaved(String TAG, Comparator<String> comparator){
        super(comparator);
        this.TAG = TAG;
        for (String key : preferences.getKeys(TAG)) {
            String value = preferences.getString(key,TAG);
            if(!value.equals("")) {
                put(key, ObjectSerializer.stringToObject(value));
            }
        }
    }
    public HashMapPosSaved(String TAG){
        this(TAG, Comparator.naturalOrder());
    }

    @Nullable
    @Override
    public Serializable put(String key, Serializable value) {
        super.put(key,value);
        preferences.putString(key, ObjectSerializer.objectToString(value), TAG);
        return value;
    }
    @Nullable
    @Override
    public Serializable remove(@Nullable Object key) {
        preferences.removeString((String)key, TAG);
        return super.remove(key);
    }

    public void clear() {
        for(String key: keySet()){
            preferences.removeString(key, TAG);
        }
        super.clear();
    }
}
