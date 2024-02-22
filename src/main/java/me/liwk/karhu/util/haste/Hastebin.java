package me.liwk.karhu.util.haste;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Hastebin {
   private static final String PASTE_URL = "https://paste.md-5.net/";
   private static final String PASTE_UPLOAD_URL = "https://paste.md-5.net/documents";

   public static String uploadPaste(String contents) {
      try {
         HttpURLConnection connection = (HttpURLConnection)(new URL("https://paste.md-5.net/documents")).openConnection();
         connection.setRequestMethod("POST");
         connection.setDoInput(true);
         connection.setDoOutput(true);
         connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
         OutputStream os = connection.getOutputStream();
         Throwable var3 = null;

         try {
            os.write(contents.getBytes(Charsets.UTF_8));
         } catch (Throwable var13) {
            var3 = var13;
            throw var13;
         } finally {
            if (os != null) {
               if (var3 != null) {
                  try {
                     os.close();
                  } catch (Throwable var12) {
                     var3.addSuppressed(var12);
                  }
               } else {
                  os.close();
               }
            }

         }

         Gson gson = new Gson();
         JsonObject object = (JsonObject)gson.fromJson(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8), JsonObject.class);
         String pasteUrl = "https://paste.md-5.net/" + object.get("key").getAsString();
         connection.disconnect();
         return pasteUrl;
      } catch (Exception var15) {
         return null;
      }
   }
}
