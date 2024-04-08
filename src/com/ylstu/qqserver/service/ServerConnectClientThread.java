package com.ylstu.qqserver.service;

import com.ylstu.qqcommon.Message;
import com.ylstu.qqcommon.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;

/**
 * @author YI
 * @version 1.0
 */
public class ServerConnectClientThread extends Thread {

    private Socket socket;
    private String userID;

    public ServerConnectClientThread(Socket socket, String userID) {
        this.socket = socket;
        this.userID = userID;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void run() {
        //不停地从客户端读
        try {
            while (true) {
                System.out.println("等待从客户端接收消息");
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                Message ms = (Message) objectInputStream.readObject();

                //如果信息是请求在线用户列表
                if (ms.getMesType().equals(MessageType.Message_Get_Online_Friend)) {
                    System.out.println("用户" + ms.getSender() + "请求在线用户列表");
                    //获得在线用户组成的字符串
                    String usersOnline = ManageSCCThread.getUserOnline();

                    //创建message，将字符串加进去
                    Message ms2 = new Message();
                    ms2.setMesType(MessageType.Message_Ret_Online_Friend);
                    ms2.setContent(usersOnline);
                    ms2.setGetter(ms.getSender());

                    //创建输出流，将message传过去
                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(ms2);
                }

                //如果信息是用户退出
                if (ms.getMesType().equals(MessageType.Message_Client_Exit)) {
                    System.out.println("用户" + ms.getSender() + "请求退出");

                    //从线程列表中删除此线程,并关闭此线程的socket
                    ManageSCCThread.removeSCCT(ms.getSender());
                    socket.close();
                    //退出线程
                    break;

                }

                if (ms.getMesType().equals(MessageType.Message_Comm_Mes)) {

                    //先打开到消息接收者的输出流
                    ObjectOutputStream oos = new ObjectOutputStream(ManageSCCThread.getSCCT(ms.getGetter()).socket.getOutputStream());

                    System.out.println(ms.getSendTime() + " 将消息从 " + ms.getSender() + " 发到 " + ms.getGetter());
                    oos.writeObject(ms);


                }

                if (ms.getMesType().equals(MessageType.getMessage_Comm_Mes_All)) {
                    System.out.println("用户" + ms.getSender() + "群发了消息");
                    //循环打开所有的socket,将信息发过去
                    Iterator it = ManageSCCThread.getHm().keySet().iterator();

                    while (it.hasNext()) {
                        String u = it.next().toString();

                        //如果是发送者，就跳过
                        if (u.equals(ms.getSender())) {
                            continue;
                        }

                        ObjectOutputStream oos = new ObjectOutputStream(
                                ManageSCCThread.getSCCT(u).socket.getOutputStream());
                        System.out.println(u + "已收到消息");

                        oos.writeObject(ms);

                    }

                }

                if(ms.getMesType().equals(MessageType.Message_File_TO_Server)){
                    System.out.println("用户"+ms.getSender()+"向"+ms.getGetter()+"发送了文件");

                    //先打开到消息接收者的输出流
                    ObjectOutputStream oos = new ObjectOutputStream(ManageSCCThread.getSCCT(ms.getGetter()).socket.getOutputStream());

                    oos.writeObject(ms);
                }


            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
