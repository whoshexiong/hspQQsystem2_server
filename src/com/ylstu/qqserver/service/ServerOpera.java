package com.ylstu.qqserver.service;

import com.ylstu.qqcommon.Message;
import com.ylstu.qqcommon.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Scanner;

/**
 * @author YI
 * @version 1.0
 */
public class ServerOpera extends Thread{

    public void run(){
        while(true){
            System.out.println("请输入你要推送的消息");

            Scanner scanner = new Scanner(System.in);
            //scanner.useDelimiter("\n");
            String ServerSaid = scanner.nextLine();


            Message message = new Message();
            message.setMesType(MessageType.Message_To_Client);
            message.setContent(ServerSaid);

            //遍历所有在线用户，将消息发出去
            Iterator it = ManageSCCThread.getHm().keySet().iterator();

            while (it.hasNext()) {
                String u = it.next().toString();

                ObjectOutputStream oos = null;

                try {
                    oos = new ObjectOutputStream(
                                ManageSCCThread.getSCCT(u).getSocket().getOutputStream());

                    System.out.println(u + "已收到消息");

                    oos.writeObject(message);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }

            System.out.println("公告消息已发出...");


        }
    }
}
