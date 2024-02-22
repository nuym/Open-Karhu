package me.liwk.karhu.database;

import java.sql.Connection;
import org.intellij.lang.annotations.Language;

public class Query {
   private static Connection conn;

   public static void use(Connection conn) {
      Query.conn = conn;
   }

   public static ExecutableStatement prepare(@Language("MySQL") String query) {
      try {
         return new ExecutableStatement(conn.prepareStatement(query));
      } catch (Throwable var2) {
         Throwable $ex = var2;
         throw $ex;
      }
   }

   public static ExecutableStatement prepare(@Language("MySQL") String query, Connection con) {
      try {
         return new ExecutableStatement(con.prepareStatement(query));
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }
}
