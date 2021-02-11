package com.aeon.wallet.app.util;
import java.util.HashSet;
import java.util.Set;
public class Base58 {
    private static final String alphabet = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    public static Set<Character> stringToCharacterSet(String s) {
        Set<Character> set = new HashSet<>();
        for (char c : s.toCharArray()) {
            set.add(c);
        }
        return set;
    }
    private static boolean containsAllChars
            (String container, String containee) {
        return stringToCharacterSet(container).containsAll
                (stringToCharacterSet(containee));
    }
    public static boolean isValidAddress(String address){
        return containsAllChars(alphabet, address) && (address.startsWith("W") || address.startsWith("X"))
                && address.length() == 97;
    }
    
}
