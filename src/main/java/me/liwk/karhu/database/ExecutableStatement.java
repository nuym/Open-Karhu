package me.liwk.karhu.database;

import java.sql.Array;
import java.sql.Blob;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.UUID;
import me.liwk.karhu.util.NetUtil;

public class ExecutableStatement {
   private PreparedStatement statement;
   private int pos = 1;

   public ExecutableStatement(PreparedStatement statement) {
      this.statement = statement;
   }

   public Integer execute() {
      try {
         Integer $ex;
         try {
            $ex = this.statement.executeUpdate();
         } finally {
            NetUtil.close(this.statement);
         }

         return $ex;
      } catch (Throwable var6) {
         throw var6;
      }
   }

   public void execute(ResultSetIterator iterator) {
      try {
         ResultSet rs = null;

         try {
            rs = this.statement.executeQuery();

            while(rs.next()) {
               iterator.next(rs);
            }
         } finally {
            NetUtil.close(this.statement, rs);
         }
      } catch (Throwable var7) {
         throw var7;
      }
   }

   public void executeSingle(ResultSetIterator iterator) {
      try {
         ResultSet rs = null;

         try {
            rs = this.statement.executeQuery();
            if (rs.next()) {
               iterator.next(rs);
            } else {
               iterator.next(null);
            }
         } finally {
            NetUtil.close(this.statement, rs);
         }
      } catch (Throwable var7) {
         throw var7;
      }
   }

   public ResultSet executeQuery() {
      try {
         return this.statement.executeQuery();
      } catch (Throwable var2) {
         throw var2;
      }
   }

   public ExecutableStatement append(Object obj) {
      try {
         this.statement.setObject(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(String obj) {
      try {
         this.statement.setString(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(UUID uuid) {
      try {
         if (uuid != null) {
            this.statement.setString(this.pos++, uuid.toString().replace("-", ""));
         } else {
            this.statement.setString(this.pos++, null);
         }

         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Array obj) {
      try {
         this.statement.setArray(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Integer obj) {
      try {
         this.statement.setInt(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Short obj) {
      try {
         this.statement.setShort(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Long obj) {
      try {
         this.statement.setLong(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Float obj) {
      try {
         this.statement.setFloat(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Double obj) {
      try {
         this.statement.setDouble(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Date obj) {
      try {
         this.statement.setDate(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Timestamp obj) {
      try {
         this.statement.setTimestamp(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Time obj) {
      try {
         this.statement.setTime(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(Blob obj) {
      try {
         this.statement.setBlob(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }

   public ExecutableStatement append(byte[] obj) {
      try {
         this.statement.setBytes(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         throw var3;
      }
   }
}
