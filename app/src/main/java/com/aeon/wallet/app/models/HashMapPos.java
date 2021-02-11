package com.aeon.wallet.app.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class HashMapPos extends HashMap<String,Serializable> {
    private final HashMap<Integer,String> POSITION_MAP;
    private final Comparator<String> comparator;
    public HashMapPos(Comparator<String> comparator){
        POSITION_MAP = new HashMap<>();
        this.comparator = comparator;
    }
    public HashMapPos(){
        this( Comparator.naturalOrder());
    }
    @Nullable
    @Override
    public Serializable put(String key, Serializable value) {
        super.put(key,value);
        if(!POSITION_MAP.containsValue(key)){
            POSITION_MAP.put(POSITION_MAP.size(),key);
        }
        sortPositions();
        return value;
    }
    public String getKey(int position){
        return POSITION_MAP.get(position);
    }
    @Override
    public void putAll(@NonNull Map<? extends String, ? extends Serializable> m) {
        this.clear();
        for(String key: m.keySet()){
            put(key,m.get(key));
        }
    }

    @Nullable
    @Override
    public Serializable remove(@Nullable Object key) {
        Serializable value = super.remove(key);
        POSITION_MAP.clear();
        for (String k : keySet()) {
            POSITION_MAP.put(POSITION_MAP.size(),k);
        }
        return value;
    }

    public void clear() {
        POSITION_MAP.clear();
        super.clear();
    }

    public void sortPositions() {
        ArrayList<String> keys = new ArrayList<>(keySet());
        keys.sort(comparator);
        POSITION_MAP.clear();
        for (String k : keys) {
            POSITION_MAP.put(POSITION_MAP.size(), k);
        }
    }

    public static class DiffCallback extends DiffUtil.Callback {

        protected final HashMapPosSaved mOldList;
        protected final HashMapPos mNewList;

        public DiffCallback(HashMapPosSaved oldEmployeeList, HashMapPos newEmployeeList) {
            this.mOldList = oldEmployeeList;
            this.mNewList = newEmployeeList;
        }

        @Override
        public int getOldListSize() {
            return mOldList.size();
        }

        @Override
        public int getNewListSize() {
            return mNewList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return mOldList.getKey(oldItemPosition).equals(mNewList.getKey(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            final String oldString = String.valueOf(mOldList.get(mOldList.getKey(oldItemPosition)));
            final String newString = String.valueOf(mNewList.get(mNewList.getKey(newItemPosition)));

            return oldString.equals(newString);
        }
    }
}
