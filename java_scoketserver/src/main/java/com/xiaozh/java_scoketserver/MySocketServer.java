package com.xiaozh.java_scoketserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Socket连接的服务端
 */
public class MySocketServer {
    private static final int SOCKET_PORT = 1234;//端口号
    private ServerSocket serverSocket = null;
    private boolean flag = true;
    private BufferedReader reader;
    private BufferedWriter writer;
    private InputStreamReader inputStreamReader;
    private OutputStreamWriter outputStreamWriter;

    public static void main(String[] args) {
        MySocketServer socketServer = new MySocketServer();
        socketServer.initSocket();
    }

    private void initSocket() {
        try {
            serverSocket = new ServerSocket(SOCKET_PORT);
            System.out.println("服务已启动,端口号为:" + SOCKET_PORT);
            while (flag) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("有客户端连接");
                SocketThread socketThread = new SocketThread(clientSocket);
                socketThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SocketThread extends Thread {
        private Socket socket;

        public SocketThread(Socket clientSocket) {
            this.socket = clientSocket;
        }

        @Override
        public void run() {
            super.run();
            InputStream inputStream;
            OutputStream outputStream;
            try {
                //获取输入流
                inputStream = socket.getInputStream();
                //获取输出流
                outputStream = socket.getOutputStream();

                inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
                //得到读取BufferedReader对象
                reader = new BufferedReader(inputStreamReader);
                //得到写入BufferedWriter对象
                writer = new BufferedWriter(outputStreamWriter);
                //循环读取客户端发过来的消息
                while (flag) {
                    if (reader.ready()) {
                        String result = reader.readLine();
                        System.out.println("客户端发过来的消息为:" + result);
                        //将服务端发过来的消息发送到客户端
                        writer.write("服务端发过来的消息为:" + result + "\n");
                        writer.flush();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
