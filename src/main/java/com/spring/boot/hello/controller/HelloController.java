package com.spring.boot.hello.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello World 控制器
 * @author <a href="https://waylau.com">Way Lau</a> 
 * @date 2017年1月26日
 */
@RestController
public class HelloController {

	@RequestMapping("/hello")
	public String hello() {
	    return "Hello World! Welcome to visit waylau.com!!s";
	}
}




// test：

//server.java

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Server {
    private Set<ClientHandler> clients = new HashSet<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("服务器已启动，等待客户端连接...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter writer;
        private String username;
        private Date connectedTime;
        private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            this.connectedTime = new Date();
        }

        @Override
        public void run() {
            try {
                Scanner scanner = new Scanner(clientSocket.getInputStream(), "UTF-8");
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                //writer.println("请输入你的用户名：");
                username = scanner.nextLine();
                broadcast(username + " 加入聊天室 (" + timeFormat.format(connectedTime) + ")");

                // 发送 "USERNAME_SET" 消息给客户端
                writer.println("USERNAME_SET");

                String inputLine;
                while (!(inputLine = scanner.nextLine()).equalsIgnoreCase("QUIT")) {
                    if (inputLine.equalsIgnoreCase("WHOISIN")) {
                        writer.println("当前在线用户：");
                        for (ClientHandler client : clients) {
                            writer.println("- " + client.getUsername());
                        }
                    } else {
                        String message = username + ": " + inputLine;
                        broadcast(message);
                    }
                }

                removeClient(this);
                broadcast(username + " 离开聊天室 (" + timeFormat.format(new Date()) + ")");

                writer.close();
                scanner.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
        }

        public String getUsername() {
            return username;
        }
    }
}



// client.java
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private String username;
    private PrintWriter writer;

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }

    public void start() {
        try {
            // 将 "localhost" 换成 "服务器的局域网 IP 地址"
            Socket socket = new Socket("192.168.3.12", 12345);
            Scanner scanner = new Scanner(socket.getInputStream(), "UTF-8");
            writer = new PrintWriter(socket.getOutputStream(), true);

            Thread userInputThread = new Thread(new UserInputHandler());
            userInputThread.start();

            // 提示消息
            System.out.println("请输入你的用户名：");
            String serverMessage = scanner.nextLine();
            System.out.println(serverMessage);

            while (true) {
                serverMessage = scanner.nextLine();
                if (serverMessage.equalsIgnoreCase("USERNAME_SET")) {
                    System.out.println("直接输入文字并回车即可发送消息给聊天室所有人");
                    System.out.println("输入 WHOISIN 查询当前在线人数");
                    System.out.println("输入 QUIT 退出");
                } else {
                    System.out.println(serverMessage);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class UserInputHandler implements Runnable {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);

            username = scanner.nextLine();
            writer.println(username);

            String userInput;
            while (!(userInput = scanner.nextLine()).equalsIgnoreCase("QUIT")) {
                writer.println(userInput);
            }

            scanner.close();
        }
    }
}
