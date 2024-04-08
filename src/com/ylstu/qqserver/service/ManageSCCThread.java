package com.ylstu.qqserver.service;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author YI
 * @version 1.0
 */
public class ManageSCCThread {

    private static HashMap<String,ServerConnectClientThread> hm = new HashMap<>();

    public static void addCCST(String userID,ServerConnectClientThread scct){
        hm.put(userID,scct);
    }

    public static ServerConnectClientThread getSCCT(String userID){
        return hm.get(userID);
    }

    public static void removeSCCT(String userID){
        hm.remove(userID);
    }

    public static HashMap<String,ServerConnectClientThread> getHm(){
        return hm;
    }
    public static String getUserOnline(){
        //遍历哈希表，把里面的用户名接成一个字符串
        Iterator it = hm.keySet().iterator();
        String usersOnline = "";

        while (it.hasNext()){
            usersOnline+=(it.next().toString()+" ");
        }

        return usersOnline;
    }
}
