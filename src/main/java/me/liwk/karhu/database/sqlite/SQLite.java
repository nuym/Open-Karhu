package me.liwk.karhu.database.sqlite;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.database.Query;

public class SQLite {
   public static Connection conn;

   public static void init() {
      try {
         Class.forName("org.sqlite.JDBC");
         String url = "jdbc:sqlite:" + Karhu.getInstance().getPlug().getDataFolder().getAbsolutePath() + File.separator + "database.sqlite";
         conn = DriverManager.getConnection(url);
         Query.use(conn);
         Karhu.getInstance().printCool("&b> &aConnection to SQLite has been established.");
      } catch (Exception var1) {
         Karhu.getInstance().printCool("&b> &cConnection to SQLite has failed.");
         var1.printStackTrace();
      }
   }

   public static void use() {
      try {
         if (conn.isClosed()) {
            String url = "jdbc:sqlite:" + Karhu.getInstance().getPlug().getDataFolder().getAbsolutePath() + File.separator + "database.sqlite";
            conn = DriverManager.getConnection(url);
            Query.use(conn);
         }
      } catch (Exception var1) {
         var1.printStackTrace();
      }
   }
}
