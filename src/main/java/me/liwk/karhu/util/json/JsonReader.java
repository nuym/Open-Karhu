package me.liwk.karhu.util.json;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import javax.net.ssl.HttpsURLConnection;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.util.haste.Hastebin;
import me.liwk.karhu.util.text.TextUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class JsonReader {
   private static JsonObject readIp(String address) throws IOException {
      String sURL = "http://proxycheck.io/v2/" + address + "?key=0q2s38-80419r-466076-h10l90&risk=1&vpn=1&asn=1";
      URL url = new URL(sURL);
      URLConnection request = url.openConnection();
      request.setRequestProperty("User-Agent", "KarhuAC");
      request.setReadTimeout(4000);
      request.connect();
      JsonParser jp = new JsonParser();
      JsonElement root = jp.parse(new InputStreamReader((InputStream)request.getContent()));
      return root.getAsJsonObject();
   }

   public static String[] getData(String address) throws IOException {
      JsonObject object = readIp(address);
      if (object.entrySet() != null) {
         for(Entry<String, JsonElement> obj : object.entrySet()) {
            if (obj.getKey().equalsIgnoreCase(address)) {
               JsonObject data = ((JsonElement)obj.getValue()).getAsJsonObject();
               String proxy = data.get("proxy").getAsString().equalsIgnoreCase("yes") ? "true" : "false";
               String geoLocation = data.get("country").getAsString();
               JsonElement testRisk = data.get("risk");
               int riskLevel = testRisk == null ? 0 : testRisk.getAsInt();
               String risk = riskLevel > 50 ? "true" : "false";
               return new String[]{proxy, geoLocation, risk};
            }
         }
      }

      return new String[]{null, null, null};
   }
}
