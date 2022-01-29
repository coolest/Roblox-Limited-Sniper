package me.projects.itemsniperv2;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ThreadInfo;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Items {
    public static LinkedHashMap<String, Integer> items = new LinkedHashMap<String, Integer>();
    public static final String rolimons_url = "https://www.rolimons.com/item/%s";
    public static final String classname = "card-title mb-1 text-light text-truncate stat-data";
    private static final int delayTimeMS = 5000;
    
    private static void saveToFile(){
        try {
            FileOutputStream fos = new FileOutputStream(new File("C:/Users/Thezi/VisualStudioWorkspace/Java-Workspace/item-sniper-v2/src/main/java/me/projects/itemsniperv2/itemData.ser"));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(items);
            oos.close();
            fos.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }
    // for rolimons webscraping
    public static void updateValues(){
        while (true)
            for (final String itemId : Items.items.keySet()){
                CloseableHttpClient httpclient = HttpClients.createDefault();
                String url = String.format(rolimons_url, itemId);
                HttpGet getRequest = new HttpGet(url);
                try (CloseableHttpResponse response = httpclient.execute(getRequest)){
                    HttpEntity entity = response.getEntity();
                    InputStream data = new DataInputStream(entity.getContent());
                    Document document = Jsoup.parse(data, "UTF-8", url);
                    double cachedValue = Items.items.get(itemId);
                    double value = 0;
                    try{
                        Elements infos = document.getElementsByClass("d-flex value-stat-box bg-primary");
                        Iterator<Element> it = infos.iterator();
                        while (it.hasNext()){
                            Element info = it.next();
                            String header = info.getElementsByClass("value-stat-header").first().text();
                            if (header.equals("Value")){
                                value = Double.parseDouble(info.getElementsByClass("value-stat-data").first().text().replace(",", ""));
                            }
                        }
                    }catch(NullPointerException e){}
                    //System.out.println(cachedValue + " | " + value);
                    
                    if (cachedValue != value && value > 10){
                        NotificationHandler.CreateNotification(itemId, null, (int)value, cachedValue, true, "SUS");
                        if (cachedValue*2 < value)
                            return;
                        
                        items.put(itemId, (int)value);
                        System.out.println("NEW VALUE");
                        saveToFile();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                    System.out.println("ROLIMONS VALUE GETTING");
                }
                
                try{
                    TimeUnit.MILLISECONDS.sleep(delayTimeMS);
                }catch(InterruptedException e){
                    e.printStackTrace();
                    System.out.println("THREAD:"+ itemId);
                }
            }
    }
    
    static {
        try {
            FileInputStream fis = new FileInputStream(new File("C:/Users/Thezi/VisualStudioWorkspace/Java-Workspace/item-sniper-v2/src/main/java/me/projects/itemsniperv2/itemData.ser"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            // pushback inputstream to check if it is empty then do not change value if it is empty
            items = (LinkedHashMap<String, Integer>) ois.readObject();
            ois.close();
            fis.close();
         } catch( EOFException e){} catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        items.put("33337038", 480_000);
        
        if (items.size() == 0){
            System.out.println("Cannot find file.");
            /*
            // (item id, item value)
            items.put("21070012", 20_000_000);      // Emp
            items.put("48545806", 18_000_000);      // Frig
            items.put("162067148", 12_500_000);     // Astra
            items.put("88885069", 10_000_000);      // Lotf
            items.put("31101391", 8_000_000);       // Inf
            items.put("1031429", 6_000_000);        // Dc
            items.put("207207782", 5_000_000);      // Laotf
            items.put("33171947", 4_500_000);      // Bling Necklace
            items.put("26943368", 4_500_000);    // Est --  because there is a pois copy 
            items.put("72082328", 3_000_000);      // Rstf
            items.put("1114768", 3_500_000);       // Kleos
            items.put("9910070", 3_500_000);       // Wwc
            //items.put("148791559", 3_500_000);     // Aa
            items.put("64082730", 3_400_000);       // Rs
            items.put("23705521", 3_000_000);       // Euro
            items.put("63043890", 2_600_000);    // Pstf
            items.put("1158416", 3_000_000);       // Eerie
            items.put("22850569", 2_200_000);       // Sql
            //items.put("97078419", 2_500_000);      // Starry
            items.put("293316452", 2_400_000);      // Aotf
            items.put("119916949", 2_400_000);   // Mbstf
            //items.put("31312357", 2_250_000);       // Tmh
            //items.put("55907562", 2_250_000);      // Mm
            //items.put("52458643", 2_200_000);      // Bbr
            items.put("124730194", 2_000_000);     // Bv
            //items.put("73791866", 1_300_000);      // Da
            items.put("128158708", 1_400_000);   // Dotf
            //items.put("2799053", 1_050_000);        // Grc
            items.put("64444871", 1_000_000);      // Messor
            items.put("100929604", 1_200_000);     // Gstf
            items.put("138932314", 1_400_000);       // Aur
            //items.put("207207025", 900_000);       // Cts
            items.put("250395631", 975_000);       // Rex
            items.put("16652251", 900_000);         // Rt
            //items.put("416832622", 775_000);       // Wol
            //items.put("25308781", 750_000);         // Chronold
            items.put("334663683", 1_050_000);       // Pistf
            items.put("493476042", 900_000);       // Sbstf
            items.put("2830437685", 1_100_000);     // Ev
            items.put("445115703", 700_000);       // Patriot
            //items.put("31765091", 675_000);         // Bharama
            //items.put("17449820", 650_000);         // Cor
            //items.put("24487029", 650_000);         // Chryso
            //items.put("32567578", 650_000);         // Deck
            items.put("47697285", 530_000);         // Indy
            items.put("259423244", 900_000);       // Bstf
            //items.put("69226736", 650_000);        // Nyc
            items.put("1180433861", 670_000);      // Stv
            //items.put("14671091", 625_000);         // Socom
            items.put("42211680", 680_000);        // Rdc
            //items.put("323417812", 600_000);        // Wcuaa
            //items.put("100425940", 575_000);       // Tgc
            items.put("20573086", 650_000);         // Rg
            items.put("215751161", 675_000);       // Ostf
            items.put("16895215", 550_000);         // Dh
            items.put("35292167", 540_000);         // Dpdj
            items.put("147180077", 620_000);       // Tstf
            //items.put("8795521", 520_000);         // Grmva
            items.put("26011378", 450_000);         // Ts
            //items.put("51352983", 500_000);         // Umad
            //items.put("62720797", 500_000);        // Spartan
            items.put("94794774", 370_000);         // Cres
            items.put("144507154", 450_000);        // HFiL
            items.put("1285307", 520_000);          // Stf
            //items.put("562478132", 425_000);       // Vw
            //items.put("25740034", 390_000);         // Sc
            items.put("16641274", 250_000);      // Ill
            items.put("22546563", 400_000);         // Ollie
            //items.put("26769281", 270_000);         // Ds
            items.put("40493240", 530_000);         // Mags
            items.put("63692675", 260_000);         // Hsl
            items.put("96079550", 200_000);         // Sl
            items.put("96103379", 490_000);        // Vesp
            items.put("188003914", 275_000);        // Cotf
            items.put("1016143686", 540_000);       // Wstf
            items.put("11748356", 560_000);         // Cws
            items.put("146134358", 420_000);        // Eotf
            items.put("180660043", 500_000);        // Rge
            items.put("1323384", 320_000);          // Ic
            items.put("1180419124", 350_000);       // Baron
            items.put("557057917", 280_000);     // Stellars
            items.put("17735316", 250_000);        // Frc
            //items.put("27477255", 250_000);        // Rb
            items.put("98346834", 420_000);         // Bsf
            //items.put("140484519", 175_000);       // Bobbie
            items.put("489196035", 250_000);        // Bsb
            items.put("23301681", 200_000);         // Mss
            items.put("37816777", 230_000);        // Gw
            items.put("74891545", 180_000);        // Sh
            items.put("161211371", 240_000);       // Chronight
            items.put("17109706", 260_000);         // Lotv
            //items.put("169444515", 380_000);        // Rbad
            items.put("26019070", 250_000);         // Yum
            items.put("527365852", 240_000);        // Prae
            //items.put("32199211", 210_000);        // Cri
            //items.put("136802867", 210_000);       // Hfl
            items.put("416828455", 200_000);        // Pm
            //items.put("14462955", 200_000);        // Ci
            //items.put("16598513", 200_000);        // Gbtie
            items.put("49493376", 240_000);         // Eoc
            items.put("1125510", 340_000);          // Void
            items.put("124745913", 190_000);        // Chronew
            items.put("125013769", 190_000);        // Ls
            items.put("439984665", 190_000);        // Duotf
            items.put("13793866", 190_000);         // Mmoz
            //items.put("14404355", 185_000);        // x
            //items.put("27858560", 180_000);        // Enth
            //items.put("31046644", 180_000);        // Cotuh
            items.put("71499623", 230_000);         // Dhop
            items.put("33337038", 180_000);        // Tls
            //items.put("114385498", 180_000);       // Rsb
            //items.put("398674241", 180_000);       // Troll
            items.put("398676450", 280_000);        // Gge
            items.put("416846000", 280_000);        // Yge
            //items.put("97852103", 175_000);        // Pts
            //items.put("55909280", 170_000);        // Ops
            items.put("108149175", 150_000);        // Ddh
            items.put("1235488", 300_000);          // Cwhp
            //items.put("12908164", 160_000);        // Roman
            items.put("68258723", 230_000);        // Bsdc
            //items.put("23727705", 140_000);        // Spec Epsilon
            items.put("29844011", 150_000);         // Retros
            items.put("93136746", 140_000);         // Tela
            items.put("183468963", 190_000);       // Ghos
            items.put("321346550", 160_000);        // Fs
            items.put("638089422", 140_000);     // Vb
            items.put("17999992", 150_000);         // Eoa
            //items.put("493477472", 135_000);       // Star
            items.put("24123795", 200_000);        // Al
            items.put("39247441", 220_000);      // Sac
            items.put("39247498", 210_000);      // Ac
            items.put("83704165", 130_000);        // Id
            items.put("9910420", 170_000);          // Th
            items.put("77443704", 70_000);         // Wf
            items.put("20052135", 160_000);            // prank 
            //items.put("93136802", 45_000);         // Kappa (Amethyst Periastron Kappa)
            items.put("215719463", 120_000);        // Malum
            //items.put("298084718", 120_000);       // Deth
            //items.put("69344107", 115_000);        // CrCr
            items.put("15095717", 110_000);        // Interns
            items.put("20980138", 220_000);         // Ff
            //items.put("74939231", 110_000);        // Ecc
            //items.put("321346616", 110_000);       // Ngcc
            //items.put("417458345", 110_000);       // Rfhs
            items.put("1678356850", 110_000);    // Falch
            items.put("493486164", 100_000);     // Ss
            //items.put("15731113", 100_000);        // Skyfleet
            items.put("16385361", 100_000);         // Cth
            //items.put("18482398", 100_000);        // Aken
            items.put("23962538", 100_000);         // Sean's
            //items.put("102617707", 100_000);       // Scythe
            //items.put("108158379", 100_000);        // Ivory (Ivory Periastron)
            items.put("209994352", 160_000);        // Eoe
            items.put("250394771", 150_000);     // Gsw
            items.put("362029620", 100_000);     // Boss
            items.put("383605854", 150_000);     // Prs
            items.put("568920951", 100_000);        // Ice Slasher
            items.put("878889105", 100_000);     // Adotf
            */
        }
    }
}