package org.pjj.map1_7;

import java.util.HashMap;

/**
 * @author PengJiaJun
 * @Date 2022/07/06 20:54
 */
public class HashMapSource {
    public static void main(String[] args) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("123", "2");// key --> key.hashcode() --> 1450572480 --> 1450572480 % table.length --> index-0-7
        String str = hashMap.get("123");

        hashMap.put("name", "laopeng");// debug 查看经过 HashMap中hash() 后 hash为    3156183
        System.out.println("name".hashCode());//直接调用 hashcode() 后 hash为         1073741824

        System.out.println(1 << 30);
    }
}
