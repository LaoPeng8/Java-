package org.pjj.map1_8.concurrent;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author PengJiaJun
 * @Date 2022/08/22 15:31
 */
public class HashMapSource {
    public static void main(String[] args) {
        ConcurrentHashMap<String, String> hashMap = new ConcurrentHashMap<>();
        hashMap.put("name", "李四");

        new HashMap<>();

//        hashMap.put(null, "zs");
//        hashMap.remove("name");

        System.out.println(2 / 8);

        hashMap.size();
    }
}
