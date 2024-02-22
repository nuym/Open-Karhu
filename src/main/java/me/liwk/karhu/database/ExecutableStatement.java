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
         Integer var7;
         try {
            var7 = this.statement.executeUpdate();
         } finally {
            NetUtil.close(this.statement);
         }

         return var7;
      } catch (Throwable var6) {
         Throwable $ex = var6;
         throw $ex;
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
         Throwable $ex = var7;
         throw $ex;
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
               iterator.next((ResultSet)null);
            }
         } finally {
            NetUtil.close(this.statement, rs);
         }

      } catch (Throwable var7) {
         Throwable $ex = var7;
         throw $ex;
      }
   }

   public ResultSet executeQuery() {
      try {
         return this.statement.executeQuery();
      } catch (Throwable var2) {
         Throwable $ex = var2;
         throw $ex;
      }
   }

   public ExecutableStatement append(Object obj) {
      try {
         this.statement.setObject(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(String obj) {
      try {
         this.statement.setString(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(UUID uuid) {
      try {
         if (uuid != null) {
            this.statement.setString(this.pos++, uuid.toString().replace("-", ""));
         } else {
            this.statement.setString(this.pos++, (String)null);
         }

         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Array obj) {
      try {
         this.statement.setArray(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Integer obj) {
      try {
         this.statement.setInt(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Short obj) {
      try {
         this.statement.setShort(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Long obj) {
      try {
         this.statement.setLong(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Float obj) {
      try {
         this.statement.setFloat(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Double obj) {
      try {
         this.statement.setDouble(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Date obj) {
      try {
         this.statement.setDate(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Timestamp obj) {
      try {
         this.statement.setTimestamp(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Time obj) {
      try {
         this.statement.setTime(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(Blob obj) {
      try {
         this.statement.setBlob(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }

   public ExecutableStatement append(byte[] obj) {
      try {
         this.statement.setBytes(this.pos++, obj);
         return this;
      } catch (Throwable var3) {
         Throwable $ex = var3;
         throw $ex;
      }
   }
}
