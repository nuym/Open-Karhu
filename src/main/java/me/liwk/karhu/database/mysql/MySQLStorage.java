package me.liwk.karhu.database.mysql;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.BanWaveX;
import me.liwk.karhu.check.api.BanX;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.database.Query;
import me.liwk.karhu.database.Storage;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.NetUtil;
import org.bukkit.Bukkit;

public class MySQLStorage implements Storage {
   private ConcurrentLinkedQueue violations = new ConcurrentLinkedQueue();
   private ConcurrentLinkedQueue bans = new ConcurrentLinkedQueue();
   private ConcurrentLinkedQueue banWaveQueue = new ConcurrentLinkedQueue();

   public void init() {
      MySQL.init();
      Query.prepare("CREATE TABLE IF NOT EXISTS `ALERTS` (`UUID` TEXT NOT NULL,`MODULE` TEXT NOT NULL,`VL` SMALLINT NOT NULL,`TIME` LONG NOT NULL,`EXTRA` TEXT,`COORDS` TEXT,`WORLD` TEXT,`PING` LONG NOT NULL,`TPS` DOUBLE NOT NULL)").execute();
      Query.prepare("CREATE TABLE IF NOT EXISTS `ALERTSTATUS` (`UUID` VARCHAR(36) NOT NULL,`STATUS` TINYINT(1) NOT NULL,PRIMARY KEY (UUID))").execute();
      Query.prepare("CREATE TABLE IF NOT EXISTS `BANS` (`UUID` TEXT NOT NULL,`MODULE` TEXT NOT NULL,`TIME` LONG NOT NULL,`EXTRA` TEXT,`PING` LONG NOT NULL,`TPS` DOUBLE NOT NULL)").execute();
      Query.prepare("CREATE TABLE IF NOT EXISTS `BANWAVE` (`UUID` TEXT NOT NULL,`MODULE` TEXT NOT NULL,`TIME` LONG NOT NULL,`TOTALLOGS` SMALLINT NOT NULL)").execute();
      Query.prepare("ALTER TABLE ALERTS ADD COLUMN IF NOT EXISTS COORDS TEXT").execute();
      Query.prepare("ALTER TABLE ALERTS ADD COLUMN IF NOT EXISTS WORLD TEXT").execute();
      (new Thread(() -> {
         while(Karhu.getInstance() != null && Karhu.getInstance().getPlug().isEnabled()) {
            try {
               NetUtil.sleep(5000L);
               if (!this.violations.isEmpty() || !this.bans.isEmpty() || !this.banWaveQueue.isEmpty()) {
                  Exception ex;
                  Iterator var8;
                  if (!this.violations.isEmpty()) {
                     var8 = this.violations.iterator();

                     while(var8.hasNext()) {
                        ViolationX violation = (ViolationX)var8.next();

                        try {
                           MySQL.use();
                           Query.prepare("INSERT INTO `ALERTS` (`UUID`, `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS`) VALUES (?,?,?,?,?,?,?,?,?)").append(violation.player).append(violation.type).append(violation.vl).append(violation.time).append(violation.data).append(violation.location).append(violation.world).append(violation.ping).append(violation.TPS).execute();
                        } catch (Exception var6) {
                           ex = var6;
                           ex.printStackTrace();
                        }
                     }

                     this.violations.clear();
                  }

                  if (!this.bans.isEmpty()) {
                     var8 = this.bans.iterator();

                     while(var8.hasNext()) {
                        BanX ban = (BanX)var8.next();

                        try {
                           MySQL.use();
                           Query.prepare("INSERT INTO `BANS` (`UUID`, `MODULE`, `TIME`, `EXTRA`, `PING`, `TPS`) VALUES (?,?,?,?,?,?)").append(ban.player).append(ban.type).append(ban.time).append(ban.data).append(ban.ping).append(ban.TPS).execute();
                        } catch (Exception var5) {
                           ex = var5;
                           ex.printStackTrace();
                        }
                     }

                     this.bans.clear();
                  }

                  if (!this.banWaveQueue.isEmpty()) {
                     var8 = this.banWaveQueue.iterator();

                     while(var8.hasNext()) {
                        BanWaveX wave = (BanWaveX)var8.next();

                        try {
                           MySQL.use();
                           Query.prepare("INSERT INTO `BANWAVE` (`UUID`, `MODULE`, `TIME`, `TOTALLOGS`) VALUES (?,?,?,?)").append(wave.player).append(wave.type).append(wave.time).append(wave.totalLogs).execute();
                        } catch (Exception var4) {
                           ex = var4;
                           ex.printStackTrace();
                        }
                     }

                     this.banWaveQueue.clear();
                  }
               }
            } catch (Exception var7) {
               Exception e = var7;
               e.printStackTrace();
            }
         }

      }, "KarhuMySQLCommitter")).start();
   }

   public void addAlert(ViolationX violation) {
      this.violations.add(violation);
   }

   public void addBan(BanX ban) {
      this.bans.add(ban);
   }

   public void setAlerts(String uuid, int status) {
      MySQL.use();
      Query.prepare("INSERT INTO `ALERTSTATUS` (`UUID`, `STATUS`) VALUES (?,?) ON DUPLICATE KEY UPDATE STATUS=" + status).append(uuid).append(status).execute();
   }

   public boolean getAlerts(String uuid) {
      try {
         MySQL.use();
         List alert = new ArrayList();
         Query.prepare("SELECT * FROM `ALERTSTATUS` WHERE `UUID` = ? limit 1").append(uuid).execute((rs) -> {
            alert.add(rs.getInt(2));
         });
         if (alert.isEmpty()) {
            this.setAlerts(uuid, 1);
            return true;
         } else {
            return MathUtil.getIntAsBoolean((Integer)alert.get(0));
         }
      } catch (Exception var3) {
         this.setAlerts(uuid, 1);
         return true;
      }
   }

   public void loadActiveViolations(String uuid, KarhuPlayer data) {
      MySQL.use();
      List violations = new ArrayList();
      Map validVls = new HashMap();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME`").append(uuid).execute((rs) -> {
         violations.add(new ViolationX(uuid, rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)));
      });
      Iterator var5 = violations.iterator();

      while(var5.hasNext()) {
         ViolationX v = (ViolationX)var5.next();
         if (System.currentTimeMillis() - v.time < 200000L) {
            if (!validVls.containsKey(v.type)) {
               validVls.put(v.type, v.vl);
            } else if (v.vl > (Integer)validVls.get(v.type)) {
               validVls.replace(v.type, v.vl);
            }
         }
      }

      Check[] var10 = data.getCheckManager().getChecks();
      int var11 = var10.length;

      for(int var7 = 0; var7 < var11; ++var7) {
         Check c = var10[var7];
         if (validVls.containsKey(c.getName())) {
            data.addViolations(c, (Integer)validVls.get(c.getName()));
            int vl = data.getViolations(c, 100000L);
            data.setCheckVl((double)vl, c);
         }
      }

   }

   public List getViolations(String uuid, Check type, int page, int limit, long from, long to) {
      MySQL.use();
      List violations = new ArrayList();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME` DESC LIMIT ?,?").append(uuid).append(page * limit).append(limit).execute((rs) -> {
         violations.add(new ViolationX(uuid, rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)));
      });
      return violations;
   }

   public int getViolationAmount(String uuid) {
      MySQL.use();
      AtomicInteger violations = new AtomicInteger();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME`").append(uuid).execute((rs) -> {
         violations.incrementAndGet();
      });
      return violations.get();
   }

   public List getAllViolations(String uuid) {
      MySQL.use();
      List violations = new ArrayList();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME`").append(uuid).execute((rs) -> {
         violations.add(new ViolationX(uuid, rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)));
      });
      return violations;
   }

   public int getAllViolationsInStorage() {
      List violations = new ArrayList();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` ORDER BY `TIME`").execute((rs) -> {
         violations.add(new ViolationX("s", rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)));
      });
      return violations.size();
   }

   public List getRecentBans() {
      List bans = new ArrayList();
      Query.prepare("SELECT `UUID`, `MODULE`, `TIME`, `EXTRA`, `COORDS`, `PING`, `TPS` FROM `BANS` ORDER BY `TIME`").execute((rs) -> {
         bans.add(new BanX(rs.getString(1), rs.getString(2), rs.getLong(3), rs.getString(4), rs.getLong(5), rs.getDouble(6)));
      });
      Collections.reverse(bans);
      return bans.subList(0, Math.min(bans.size(), 10));
   }

   public void purge(String uuid, boolean all) {
      Query.prepare("DELETE FROM `ALERTS` WHERE UUID = ?").append(uuid).execute();
      Query.prepare("DELETE FROM `BANS` WHERE UUID = ?").append(uuid).execute();
   }

   public List getBanwaveList() {
      MySQL.use();
      List players = new ArrayList();
      Query.prepare("SELECT `UUID` FROM `BANWAVE` ORDER BY `TIME`").execute((rs) -> {
         players.add(rs.getString(1));
      });
      return players;
   }

   public boolean isInBanwave(String uuid) {
      try {
         ResultSet rs = Query.prepare("SELECT `MODULE`, `TIME`, `TOTALLOGS` FROM `BANWAVE` WHERE `UUID` = ? ORDER BY `TIME` DESC LIMIT ?,?").append(uuid).executeQuery();
         return rs.first();
      } catch (Exception var3) {
         return false;
      }
   }

   public void addToBanWave(BanWaveX bwRequest) {
      if (!this.isInBanwave(bwRequest.player)) {
         this.banWaveQueue.add(bwRequest);
      }

   }

   public void removeFromBanWave(String uuid) {
      Optional bwx = this.banWaveQueue.stream().filter((bw) -> {
         return bw.player.equals(uuid);
      }).findFirst();
      bwx.ifPresent((banWaveX) -> {
         this.banWaveQueue.remove(banWaveX);
      });
      Query.prepare("DELETE FROM `BANWAVE` WHERE UUID = ?").append(uuid).execute();
   }

   public void checkFiles() {
      try {
         String acname = Karhu.getInstance().getConfigManager().getLicense().equals(" ") ? "VengeanceLoader" : "KarhuLoader";
         if (Bukkit.getServer().getPluginManager().isPluginEnabled(acname)) {
         } else {
            Bukkit.shutdown();
         }
      } catch (Exception var2) {
      }

   }
}
