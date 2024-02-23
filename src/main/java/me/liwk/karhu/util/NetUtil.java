package me.liwk.karhu.util;

import com.github.retrooper.packetevents.manager.server.ServerVersion;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.channels.Channels;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import me.liwk.karhu.Karhu;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class NetUtil {

   public static void download(File file, String from) throws Exception {
      FileOutputStream out = new FileOutputStream(file);
      out.getChannel().transferFrom(Channels.newChannel((new URL(from)).openStream()), 0L, Long.MAX_VALUE);
   }

   public static void injectURL(URL url) {

   }

   public static int accessFile() {
      try {
         String resource = "LICENSE.md";
         String acname = Karhu.getInstance().getConfigManager().getLicense().equals(" ") ? "VengeanceLoader" : "KarhuLoader";
         JavaPlugin plugin = (JavaPlugin)Bukkit.getServer().getPluginManager().getPlugin(acname);
         Method getFileMethod = JavaPlugin.class.getDeclaredMethod("getFile");
         getFileMethod.setAccessible(true);
         if (plugin == null) {
            Bukkit.shutdown();
            return 1;
         } else {
            String bigdig = plugin.getConfig().getString("license-key");
            String nig = bigdig.replaceAll("-", "");
            if (plugin.getConfig().getString("license-key").length() != 29) {
               Bukkit.shutdown();
               return 2;
            } else if (nig.length() != 25) {
               Bukkit.shutdown();
               return 3;
            } else if (plugin.getConfig().getString("license-key").equalsIgnoreCase("null")) {
               Bukkit.shutdown();
               return 4;
            } else {
               InputStream input = Karhu.getInstance().getPlug().getClass().getResourceAsStream(resource);
               JarFile jar = new JarFile(Karhu.getInstance().getPlug().getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
               JarEntry entry = jar.getJarEntry("LICENSE.md");
               if (entry == null) {
                  Bukkit.shutdown();
                  return 5;
               } else if (!plugin.getDescription().getVersion().equals("1.2")) {
                  Karhu.getInstance().printCool("&c> &fLoader version is unsupported >= 1.2 required, download latest from discord with /download");
                  Bukkit.getPluginManager().disablePlugin(Karhu.getInstance().getPlug());
                  return 0;
               } else {
                  return Karhu.SERVER_VERSION.isOlderThan(ServerVersion.V_1_8_8) && input == null ? 0 : 0;
               }
            }
         }
      } catch (IOException | NoSuchMethodException var9) {
         return 0;
      }
   }

   public static void close(AutoCloseable... closeables) {
      try {
         AutoCloseable[] var6 = closeables;
         int var2 = closeables.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            AutoCloseable closeable = var6[var3];
            if (closeable != null) {
               closeable.close();
            }
         }
      } catch (Exception var5) {
         Exception e = var5;
         e.printStackTrace();
      }

   }

   public static void sleep(long time) {
      try {
         Thread.sleep(time);
      } catch (Throwable var3) {
      }

   }
}
