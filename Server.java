package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
            Connection connection = entry.getValue();

            try {
                connection.send(message);
            } catch (IOException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Сообщение не отправлено");
            }
        }

    }


    public static void main(String[] args) throws IOException {
        try (ServerSocket server = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Сервер запущен");
            while (true) {
                Socket socket = server.accept();
                new Handler(socket).start();
                continue;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class Handler extends Thread {

        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            String name;
            Message answer;
            do {
                connection.send(new Message(MessageType.NAME_REQUEST));
                answer = connection.receive();
                name = answer.getData();
            } while (answer.getType() != MessageType.USER_NAME || name.isEmpty() || connectionMap.containsKey(name));
            connectionMap.put(name, connection);
            connection.send(new Message(MessageType.NAME_ACCEPTED));
            return name;
        }
        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{

            Message message;
            do{message=connection.receive();
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            if(message.getType()==MessageType.TEXT){
                Message chatMessage = new Message(MessageType.TEXT, userName + ": " + message.getData());
                sendBroadcastMessage(chatMessage);

            }else ConsoleHelper.writeMessage("Тип сообщения не является текстом.");
            }while (true);
        }

        private void notifyUsers(Connection connection, String userName) throws IOException{
            for (Map.Entry<String, Connection> entry : connectionMap.entrySet()) {
                Connection iterConnection = entry.getValue();
                String iterName = entry.getKey();

                if (!entry.getKey().equals(userName))
                    connection.send(new Message(MessageType.USER_ADDED, entry.getKey()));

            }
        }

        @Override
        public void run() {
            ConsoleHelper.writeMessage(socket.getRemoteSocketAddress().toString());
            Connection connection;
            String name;
            try {
                connection = new Connection(socket);
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, name));
                connectionMap.put(name,connection);
                notifyUsers(connection,name);
                serverMainLoop(connection, name);
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED,name));

            } catch (IOException e) {
                e.printStackTrace();
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            } catch (ClassNotFoundException e){
                e.printStackTrace();
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }
            ConsoleHelper.writeMessage("Cоединение с удаленным адресом закрыто.");
        }
    }
}
