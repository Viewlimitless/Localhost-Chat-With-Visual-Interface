package com.javarush.task.task30.task3008.client;

import com.javarush.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class BotClient extends Client {
    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() throws IOException {
        return "date_bot_"+ (int)(Math.random()*100);
    }

    public static void main(String[] args) { new BotClient().run();}

    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            try {
                if (message.contains(":")){
                String[] splitedMessage = message.split(": ");
                Calendar calendar = Calendar.getInstance();
                Date t = calendar.getTime();
                String timeInf="";
                SimpleDateFormat dateFormat=null;
                    HashMap<String, SimpleDateFormat> textFormat= new HashMap<>();
                    textFormat.put("дата", new SimpleDateFormat("d.MM.YYYY"));
                    textFormat.put("день", new SimpleDateFormat("d"));
                    textFormat.put("месяц", new SimpleDateFormat("MMMM"));
                    textFormat.put("год", new SimpleDateFormat("YYYY"));
                    textFormat.put("время", new SimpleDateFormat("H:mm:ss"));
                    textFormat.put("час", new SimpleDateFormat("H"));
                    textFormat.put("минуты", new SimpleDateFormat("m"));
                    textFormat.put("секунды", new SimpleDateFormat("s"));
                    if (textFormat.containsKey(splitedMessage[1])){
                        timeInf=textFormat.get(splitedMessage[1]).format(t);
                        sendTextMessage("Информация для "+splitedMessage[0]+": "+timeInf);
                    }
//
                }
            }catch (Exception e){e.printStackTrace();}



        }
    }
}
