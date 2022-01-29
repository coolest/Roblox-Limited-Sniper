package me.projects.itemsniperv2;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {

    // Organize URLs to make requests easier
    private static final String ACCOUNT_COOKIE = "";
    private static final String ACCOUNT_COOKIE_2 = "";
    public static final String ROBLOX_BASE_URL = "https://www.roblox.com/catalog/%s";
    public static final String ROBLOX_PURCHASE_REQUEST_URL = "https://economy.roblox.com/v1/purchases/products/%s";
    public static final String ROBLOX_SELLER_LIST_URL = "https://economy.roblox.com/v1/assets/%s/resellers?cursor=&limit=10";
    public static final String RESALE_DATA_URL = "https://economy.roblox.com/v1/assets/%s/resale-data";
    
    private static HashMap<String, Integer> itemRap = new HashMap<String, Integer>();
    private static HashMap<String, Double> customDeals = new HashMap<String, Double>();
    private static List<String> blacklistedIds = Arrays.asList("1843618765", "1326183322");
    
    private static String lastdeal = "-1";
    public static int requestsMade = 0;
    public static int totalYieldTime = 0;
    public static boolean AUTOBOUGHT = false;
    
    public static final double AUTOBUY_AT_PERCENT = 35.00;
    
    public static String accountToken2 = null;
    public static void setXCSRF(CloseableHttpClient httpclient){
        HttpGet get = new HttpGet("https://www.roblox.com/home");
        get.addHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE_2+";");
        
        try (CloseableHttpResponse response = httpclient.execute(get)){
            HttpEntity entity = response.getEntity();
            InputStream data = new DataInputStream(entity.getContent());
            Document document = Jsoup.parse(data, "UTF-8", "https://www.roblox.com/home");
            Elements metaTags = document.getElementsByTag("meta");
            for (Element meta : metaTags) {
                String dataToken = meta.attr("data-token");
                if (!dataToken.equals(""))
                    accountToken2 = dataToken;
            }
        }catch(IOException e) {e.printStackTrace();}
    }
    
    public static void rapCheckaa(String itemId, CloseableHttpClient httpclient){
        int rap = -1;
        HttpGet rapGet =  new HttpGet(String.format(RESALE_DATA_URL, itemId));
        rapGet.setHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE+";");
        
        try(CloseableHttpResponse response2 = httpclient.execute(rapGet)){
            JSONObject obj = new JSONObject(EntityUtils.toString(response2.getEntity(), "UTF-8"));
            try{
                rap = Integer.parseInt(obj.get("recentAveragePrice").toString());
            }catch(NumberFormatException e){};
            
            int oldRap = itemRap.get(itemId);
            if (rap != oldRap){
                double sale = (rap - (oldRap * .9)) / 0.1;
                double value = Items.items.get(itemId);
                double deal = (1 - (sale / value)) * 100;
                NotificationHandler.RapChangeNotif(sale, deal, String.format(ROBLOX_BASE_URL, itemId));
            }
            
            itemRap.put(itemId, rap);
        }catch(IOException e){System.out.println("RAP GET ERROR "+itemId + " - "+e.getMessage());}catch(JSONException e){};
    }
    
    public static double scanItem(String itemId, CloseableHttpClient httpclient, HttpGet getRequest, String url, boolean targeted){
        long start = System.currentTimeMillis();
        try (CloseableHttpResponse response = httpclient.execute(getRequest)){
            App.requestsMade++;
            App.totalYieldTime += (System.currentTimeMillis() - start);
            
            HttpEntity entity = response.getEntity();
            InputStream data = new DataInputStream(entity.getContent());
            Document document = Jsoup.parse(data, "UTF-8", url);
            double value = Items.items.get(itemId);
            double price = value;
            int rap = -1;
            response.close();
            
            if (itemRap.get(itemId) == null){
                HttpGet rapGet =  new HttpGet(String.format(RESALE_DATA_URL, itemId));
                rapGet.setHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE+";");
                try(CloseableHttpResponse response2 = httpclient.execute(rapGet)){
                    JSONObject obj = new JSONObject(EntityUtils.toString(response2.getEntity(), "UTF-8"));
                    try{
                        rap = Integer.parseInt(obj.get("recentAveragePrice").toString());
                    }catch(NumberFormatException e){System.out.println("NPE");};
                    itemRap.put(itemId, rap);
                }catch(IOException e){System.out.println("RAP GET ERROR "+itemId + " - "+e.getMessage());}catch(JSONException e){};
            } else 
                rap = itemRap.get(itemId);
                
            try{
                String p = document.getElementsByAttributeValue("class", "price-info").first().text().replace(",", "");
                if (!p.isEmpty())
                    price = Double.parseDouble(p);
            }catch(NullPointerException e){}
            double deal = (1 - (price / value)) * 100;
            if (deal > AUTOBUY_AT_PERCENT){
                Element HTMLData = document.getElementById("item-container");
                
                double dealRequirement = (customDeals.get(itemId) == null) ? AUTOBUY_AT_PERCENT : customDeals.get(itemId);
                if (deal >= dealRequirement && value < 21000000 && price < 2500000 && rap*2 > price && deal < 80.00){
                    if (AUTOBOUGHT){
                        if (lastdeal.equals(itemId) == false){
                            System.out.println("USER ID: " +HTMLData.attr("data-expected-seller-id"));
                            System.out.println("UUID: " + HTMLData.attr("data-expected-seller-id"));
                        }
                        //NotificationHandler.CreateNotification(itemId, Integer.valueOf((int)price), (int)value, deal, false, null);
                        return price;
                    }
                    
                    System.out.println("AUTOBUY = TRUE");
                    AUTOBOUGHT = true;
                    
                    HttpPost postRequest = new HttpPost(String.format(ROBLOX_PURCHASE_REQUEST_URL, itemId));
                    postRequest.addHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE+";");
                    
                    String token = null;
                    Elements metaTags = document.getElementsByTag("meta");
                    for (Element meta : metaTags) {
                        String dataToken = meta.attr("data-token");
                        if (!dataToken.equals(""))
                            token = dataToken;
                    }
                    
                    postRequest.addHeader("X-CSRF-TOKEN", token);
                    postRequest.addHeader("Content-Type", "application/json; charset=UTF-8");
                    if (blacklistedIds.contains(HTMLData.attr("data-lowest-private-sale-userasset-id")) == false){
                        try {
                            JSONObject jsonEntity = new JSONObject();
                            jsonEntity.put("expectedCurrency", "1");
                            jsonEntity.put("expectedPrice", String.valueOf((int)price));
                            jsonEntity.put("expectedSellerId", HTMLData.attr("data-expected-seller-id"));
                            jsonEntity.put("userAssetId", HTMLData.attr("data-lowest-private-sale-userasset-id"));
                            postRequest.setEntity(new StringEntity(jsonEntity.toString()));
                            
                            try (CloseableHttpResponse autobuyResponse = httpclient.execute(postRequest)) {
                                StatusLine statusLine = autobuyResponse.getStatusLine();
                                System.out.println(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                                String responseBody = EntityUtils.toString(autobuyResponse.getEntity(), "UTF-8");
                                System.out.println("Response body: " + responseBody);
                                NotificationHandler.CreateNotification(itemId, null, (int)0, 0, true, "BOUGHT ITEM: "+url+"\nDEAL: "+deal+"\nRESPONSE: "+responseBody);
                            }catch(IOException e){e.printStackTrace();};
                            
                            String offsaleEntity = String.format("{assetId: %s, userAssetId: %s, price: 0, sell: false}", itemId, HTMLData.attr("data-lowest-private-sale-userasset-id"));
                            HttpPost offsaleRequest = new HttpPost(String.format(ROBLOX_PURCHASE_REQUEST_URL, itemId));
                            offsaleRequest.setHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE+";");
                            offsaleRequest.setHeader("X-CSRF-TOKEN", token);
                            offsaleRequest.setHeader("Content-Type", "application/json");
                            offsaleRequest.setEntity(new StringEntity(offsaleEntity));
                            httpclient.execute(offsaleRequest);
                        } catch (IOException e) {}
                    }
                    
                    try{
                        TimeUnit.MILLISECONDS.sleep(250);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                        System.out.println("THREAD:"+ itemId);
                    }
                    
                    System.out.println("AUTOBUY = FALSE");
                    AUTOBOUGHT = false;
                }else if (deal > 65.0 && !itemId.equals("4390875496")){
                    HttpPost postRequest = new HttpPost(String.format(ROBLOX_PURCHASE_REQUEST_URL, itemId));
                    postRequest.addHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE_2+";");
                    postRequest.addHeader("X-CSRF-TOKEN", accountToken2);
                    postRequest.addHeader("Content-Type", "application/json; charset=UTF-8");
                    try {
                        JSONObject jsonEntity = new JSONObject();
                        jsonEntity.put("expectedCurrency", "1");
                        jsonEntity.put("expectedPrice", String.valueOf((int)price));
                        jsonEntity.put("expectedSellerId", HTMLData.attr("data-expected-seller-id"));
                        jsonEntity.put("userAssetId", HTMLData.attr("data-lowest-private-sale-userasset-id"));
                        postRequest.setEntity(new StringEntity(jsonEntity.toString()));
                        
                        try (CloseableHttpResponse autobuyResponse = httpclient.execute(postRequest)) {
                            StatusLine statusLine = autobuyResponse.getStatusLine();
                            System.out.println(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
                            String responseBody = EntityUtils.toString(autobuyResponse.getEntity(), "UTF-8");
                            System.out.println("Response body: " + responseBody);
                            System.out.println("Request body: " + jsonEntity.toString());
                            if (!itemId.equals("4390875496"))
                                NotificationHandler.CreateNotification(itemId, null, (int)0, 0, true, "BOUGHT ITEM: "+url+"\nDEAL: "+deal+"\nRESPONSE: "+responseBody);
                        }catch(IOException e){e.printStackTrace();};
                        
                    }catch(IOException e) {e.printStackTrace();}
                }else
                    System.out.println("TOO LOW FOR BUYING");
                
                System.out.println("USER ID: " +HTMLData.attr("data-expected-seller-id"));
                lastdeal = itemId;
                NotificationHandler.CreateNotification(itemId, Integer.valueOf((int)price), (int)value, deal, false, null);
            }
            
            return price;
        }catch(IOException e){
            try{
                TimeUnit.MILLISECONDS.sleep(5000);
            }catch(InterruptedException e2){
                e2.printStackTrace();
                
                System.out.println("THREAD:"+ itemId);
            }
            
            e.printStackTrace();
        }
        return -1;
    }
    
    private static final List<String> trackPriceChange = Arrays.asList();
    private static final ArrayList<String> NO_YIELD_IDS = new ArrayList<String>(Arrays.asList("11748356", "1235488", "64444871", "527365852", "11625510", "1365767", "4390891467", "124730194", "119916949", "72082328", "1029025", "74891470", "215718515", "9910070", "26943368", "33171947", "1073690"));
    private static HashMap<String, Integer> ITEM_THREADS = new HashMap<String, Integer>();
    static {
        ITEM_THREADS.put("4390891467", 2);
        ITEM_THREADS.put("1365767", 3);
        ITEM_THREADS.put("1125510", 3);
        ITEM_THREADS.put("527365852", 2);
        ITEM_THREADS.put("1235488", 2);
        ITEM_THREADS.put("11748356", 2);
        ITEM_THREADS.put("1029025", 2);
        ITEM_THREADS.put("124730194", 2);
        ITEM_THREADS.put("119916949", 3);
        ITEM_THREADS.put("72082328", 3);
        ITEM_THREADS.put("1029025", 3);
        ITEM_THREADS.put("74891470", 2);
        ITEM_THREADS.put("215718515", 2);
        ITEM_THREADS.put("9910070", 2);
        ITEM_THREADS.put("26943368", 2);
        ITEM_THREADS.put("33171947", 2);
        ITEM_THREADS.put("1073690", 3);
        
        customDeals.put("11748356", 32.0);
        customDeals.put("1235488", 32.0);
        customDeals.put("1365767", 41.0);
        customDeals.put("4390891467", 36.5);
        customDeals.put("124730194", 20.0);
        customDeals.put("1029025", 34.0);
        customDeals.put("74891470", 24.0);
        customDeals.put("215718515", 20.0);
    }
    
    public static void main(String[] args) {
        final boolean targeted = true;
        if (targeted){
            System.out.println("TARGET SCAN");
            ArrayList<String> targeted_ids = new ArrayList<String>(Arrays.asList("11748356", "1235488", "1365767", "4390891467", "124730194", "1029025", "74891470", "215718515"));
            for (final String itemId : targeted_ids){
                final String url = String.format(ROBLOX_BASE_URL, itemId);
                for (int i = 0; i < 1; i++){
                    final int i2 = i;
                    new Thread(String.valueOf(i2)) {
                        public void run() {
                            CloseableHttpClient httpclient = HttpClients.createDefault();
                            HttpGet get = new HttpGet(url);
                            get.addHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE+";");
                            get.addHeader("Content-Type", "text/html; charset=UTF-8");
                            while(true){
                                scanItem(itemId, httpclient, get, url, true);
                                
                                try{
                                    TimeUnit.MILLISECONDS.sleep(1300);
                                }catch(InterruptedException e){
                                    e.printStackTrace();
                                    System.out.println("THREAD:"+ itemId);
                                }
                            }
                            
                        }
                    }.start();
                }
            }
            
            for (final String itemId : trackPriceChange){
                final String itemId2 = itemId;
                final String url = String.format(ROBLOX_BASE_URL, itemId2);
                new Thread(String.valueOf(itemId)) {
                    public void run() {
                        CloseableHttpClient httpclient = HttpClients.createDefault();
                        HttpGet get = new HttpGet(url);
                        get.addHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE+";");
                        //get.addHeader("Content-Type", "text/html; charset=UTF-8");
                        double lastPrice = -1;
                        while (true){
                        
                            double price = scanItem(itemId, httpclient, get, url, true);
                            if (lastPrice == -1)
                                lastPrice = price;
                            else if (lastPrice != price && trackPriceChange.contains(itemId) && (int)price != (int)Items.items.get(itemId)){
                                lastPrice = price;
                                NotificationHandler.CreateNotification(itemId, Integer.valueOf((int)price), (int)0, 0, true, null);
                            }
                            
                            try{
                                TimeUnit.MILLISECONDS.sleep(5000);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                                System.out.println("THREAD:"+ itemId);
                            }
                        }
                    }
                }.start();
            }
        } else {
            //final CloseableHttpClient rapHttpClient = HttpClients.createDefault();
            //for (int j = 0; j < 5; j++)
            for (final String itemId : Items.items.keySet()){
                Integer amount = ITEM_THREADS.get(itemId);
                for (int i = 0; i < ((amount != null) ? amount.intValue() : 1); i++){
                    final int final_i = i;
                    new Thread(itemId + final_i){
                        public void run() {
                            double lastPrice = -1;
                            CloseableHttpClient httpclient = HttpClients.createDefault();
                            String url = String.format(ROBLOX_BASE_URL, itemId);
                            HttpGet getRequest = new HttpGet(url);
                            getRequest.addHeader("Cookie", ".ROBLOSECURITY=" + ACCOUNT_COOKIE+";");
                          
                            //getRequest.addHeader("Content-Type", "text/html; charset=UTF-8");
                            while (true){
                                double price = scanItem(itemId, httpclient, getRequest, url, false);
                                if (lastPrice == -1)
                                    lastPrice = price;
                                else if (lastPrice != price && trackPriceChange.contains(itemId) && (int)price != (int)Items.items.get(itemId)){
                                    lastPrice = price;
                                    NotificationHandler.CreateNotification(itemId, Integer.valueOf((int)price), (int)0, 0, true, null);
                                }
                                
                                
                                if (!NO_YIELD_IDS.contains(itemId))
                                    try{
                                        TimeUnit.MILLISECONDS.sleep(200);
                                    }catch(InterruptedException e){
                                        e.printStackTrace();
                                        System.out.println("THREAD:"+ itemId);
                                    }
                                
                            }
                        }
                    }.start();
                }
                /*
                new Thread(itemId + "_rap scan"){
                    public void run(){
                        try{
                            TimeUnit.SECONDS.sleep(100);
                        }catch(InterruptedException e){
                            e.printStackTrace();
                            System.out.println("THREAD:"+ itemId);
                        }
                        
                        while (true){
                            rapCheckaa(itemId, rapHttpClient);
                            App.requestsMade++;
                            try{
                                TimeUnit.SECONDS.sleep(60);
                            }catch(InterruptedException e){
                                e.printStackTrace();
                                System.out.println("THREAD:"+ itemId);
                            }
                        }
                    }
                }.start();
                */
            }
        }
        new Thread(){public void run() {Items.updateValues();}}.start();
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        
        // Log Requests
        while (true){
            try{
                TimeUnit.SECONDS.sleep(10);
            }catch(InterruptedException e){
                e.printStackTrace();
                System.exit(0);
            }
            
            setXCSRF(httpclient);
            System.out.println("In 10 seconds there were " + requestsMade + " requests made. And the average yield for each request was: " + ((double) totalYieldTime /  (double) requestsMade) + "ms");
            totalYieldTime = 0;
            requestsMade = 0;
        }
    }
}


