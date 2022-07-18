package org.pjj.list_.set_;

import java.util.HashSet;

/**
 * HashSet源码解读
 *
 * @author PengJiaJun
 * @Date 2022/07/06 19:05
 */
public class HashSetSource {
    @SuppressWarnings({"all"})
    public static void main(String[] args) {

        HashSet<Object> hashSet = new HashSet<>();
        hashSet.add("java");
        hashSet.add("php");
        hashSet.add("java");
        System.out.println(hashSet);
    }
}
