package me.projects.itemsniperv2;

import java.io.IOException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.awt.*;
import java.awt.TrayIcon.MessageType;

public class NotificationHandler {
    final private static String dealWebhook = "https://discord.com/api/webhooks/";
    final private static String valueChangeWebhook = "https://discordapp.com/api/webhooks/";
    final private static String priceChangeWebhook = "https://discordapp.com/api/webhooks/";
    final private static String autobuyLogWebhook = "https://discordapp.com/api/webhooks/";
    final private static String dealRapChangeWebhook = "https://discord.com/api/webhooks/";
    final private static CloseableHttpClient client = HttpClients.createDefault();
    final private static int minimum_request_delay = 1;
    private static LinkedList<HttpPost> requestQueue = new LinkedList<HttpPost>();
    private static LinkedHashMap<String, Integer> prevNotificationInfo = new LinkedHashMap<String, Integer>();
    
    private static void doNotification(BasicNameValuePair content, boolean bypassQueue, String msgTitle, String msgContent, String webhook){
        HttpPost postRequest = new HttpPost(webhook);
        /*
        if (!bypassQueue){
            requestQueue.addLast(postRequest);
            while (requestQueue.getFirst() != postRequest)
                try{
                    TimeUnit.MILLISECONDS.sleep(10);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
        }
        */
        try{
            postRequest.setEntity(new UrlEncodedFormEntity(Arrays.asList(content), "UTF-8"));
            if (msgContent != null & msgTitle != null){
                TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage("icon.png"), "Deal Notification");
                trayIcon.setImageAutoSize(true);
                trayIcon.setToolTip("DEAL NOTIFICATION");
                SystemTray.getSystemTray().add(trayIcon);
        
                trayIcon.displayMessage(msgTitle, msgContent, MessageType.INFO);
            }
            
            client.execute(postRequest);
        }catch(final IOException | AWTException e){
            e.printStackTrace();
        }
    
        try{TimeUnit.SECONDS.sleep(minimum_request_delay);}catch(InterruptedException e){e.printStackTrace();}
        //requestQueue.removeFirstOccurrence(postRequest);
    }
    
    public static void RapChangeNotif(double sale, double deal, String link){
        doNotification(
            new BasicNameValuePair("content", String.format(">**SOMEONE COP SUM**\n >*price bought: %d*\n >*deal percent: %d*\n >%s\n", (int) sale, deal, link)), 
            false,
            "deal cop",
            "see if someone got comp",
            dealRapChangeWebhook
        );
    }
    
    public static void CreateNotification(String itemId, Integer price, int value, double deal, boolean flag, String message){
        String link = String.format(App.ROBLOX_BASE_URL, itemId);
        
        if (flag != true){
            Integer prevPrice = prevNotificationInfo.get(itemId);
            if (prevPrice == null || price.intValue() != prevPrice.intValue()){
                try{
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c","start chrome "+link});
                }catch(IOException e){e.printStackTrace();}
                
                prevNotificationInfo.put(itemId, price.intValue());
                doNotification(
                    new BasicNameValuePair("content", String.format("@everyone@everyone@everyone@everyone@everyone@everyone@everyone@everyone@everyone@everyone>NEW DEAL\n >DEAL OF **%f**%%\n >*%d* / *%d*\n >%s", deal, price, value, link)), 
                    false,
                    "ITEM DEAL NOTIFICATION",
                    String.valueOf(deal),
                    dealWebhook
                );
            }
         }else if (price != null){
            doNotification(
                new BasicNameValuePair("content", String.format(">**PRICE CHANGE**\n >*%d*\n >%s\n", price, link)), 
                false,
                null,
                null,
                priceChangeWebhook
            );
         }else if (message.equals("SUS")){
            boolean sus = deal*1.6 < (double)value;
            doNotification(
                new BasicNameValuePair("content", String.format(">**VALUE CHANGE**\n >OLD VALUE:%f \n >NEW VALUE: *%d*\n >%s\n", deal, value, link)), 
                false,
                sus ? "SUS VALUE CHANGE" : null,
                sus ? "Shutting down vc scans" : null,
                valueChangeWebhook
            );
        }else if (message != null) {
            doNotification(
                new BasicNameValuePair("content", message), 
                false,
                "I AUTO BUY SOME SHIT",
                "..............................",
                autobuyLogWebhook
            );
        }
    }
}