package com.ylstu.qqserver.service;

import com.ylstu.qqcommon.Message;
import com.ylstu.qqcommon.MessageType;
import com.ylstu.qqcommon.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author YI
 * @version 1.0
 */
public class QQServer {
    ServerSocket ss = null;
    ServerOpera so = new ServerOpera();

    //创建hashmap存储有效的用户信息，模拟数据库
    public static ConcurrentHashMap<String,User> validUser = new ConcurrentHashMap<>();

    static {
        validUser.put("100",new User("100","123456"));
        validUser.put("200",new User("200","123456"));
        validUser.put("300",new User("300","123456"));
        validUser.put("400",new User("400","123456"));
        validUser.put("500",new User("500","123456"));
        validUser.put("600",new User("600","123456"));
    }

    public Boolean checkValidUser(String userID,String passwd){
        User u = validUser.get(userID);

        //若没有对应userID的对象
        if(u == null){
            System.out.println("用户名有误");
            return false;
        }

        //若用户已在线
        if(u.isOnline){
            System.out.println("该用户已在线");
            return false;
        }

        //若没有对应的密码不符
        if(!u.getPasswd().equals(passwd)){
            System.out.println("密码错误");
            return false;
        }

        u.isOnline = true;
        return true;
    }

    public QQServer(){
        try {
            ss = new ServerSocket(9999);
            so.start();

            while (true){
                System.out.println("服务端在9999端口等待连接");
                //等待端口接收
                Socket socket = ss.accept();

                //接收成功，创建输入输出流
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                //从输入流得到user信息，在建一个message等会发过去
                User u = (User) objectInputStream.readObject();
                Message message = new Message();

                //比对user信息
                if(checkValidUser(u.getUsername(),u.getPasswd())){

                    System.out.println("用户："+u.getUsername()+"密码："+u.getPasswd()+"验证成功");
                    //user比对成功，发成功信息
                    message.setMesType(MessageType.Message_Login_Succeed);

                    objectOutputStream.writeObject(message);

                    //创建一个服务端的线程，其包含一个socket
                    ServerConnectClientThread serverConnectClientThread = new ServerConnectClientThread(socket,u.getUsername());
                    serverConnectClientThread.start();

                    //把此线程放入集合中
                    ManageSCCThread.addCCST(u.getUsername(),serverConnectClientThread);

                }else {
                    System.out.println("用户："+u.getUsername()+"密码："+u.getPasswd()+"验证失败");
                    //比对失败，发失败信息,并关闭socket
                    message.setMesType(MessageType.Message_Login_Fail);

                    objectOutputStream.writeObject(message);

                    socket.close();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }finally {
            //如果走到这里，说明服务端不再循环监听，意味着可以关闭监听了
            try {
                ss.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
