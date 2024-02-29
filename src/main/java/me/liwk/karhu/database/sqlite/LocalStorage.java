package me.liwk.karhu.database.sqlite;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.SneakyThrows;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.AlertsX;
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

public class LocalStorage implements Storage {
   private ConcurrentLinkedQueue<ViolationX> violations = new ConcurrentLinkedQueue<>();
   private ConcurrentLinkedQueue<AlertsX> alerts = new ConcurrentLinkedQueue<>();
   private ConcurrentLinkedQueue<BanX> bans = new ConcurrentLinkedQueue<>();
   private ConcurrentLinkedQueue<BanWaveX> banWaveQueue = new ConcurrentLinkedQueue<>();

   @Override
   public void init() {
      String acname = Karhu.getInstance().getConfigManager().getLicense().equals(" ") ? "VengeanceLoader" : "KarhuLoader";
      if (!Karhu.getInstance().getPlug().getDescription().getName().equals(acname)) {
         try {
            Bukkit.getPluginManager().disablePlugin(Karhu.getInstance().getPlug());
         } catch (Exception var5) {
         }
      }

      try {
         SQLite.init();
         Query.prepare(
               "CREATE TABLE IF NOT EXISTS `ALERTS` (`UUID` TEXT NOT NULL,`MODULE` TEXT NOT NULL,`VL` SMALLINT NOT NULL,`TIME` LONG NOT NULL,`EXTRA` TEXT,`COORDS` TEXT NOT NULL,`WORLD` TEXT NOT NULL,`PING` LONG NOT NULL,`TPS` DOUBLE NOT NULL)"
            )
            .execute();
         Query.prepare("CREATE TABLE IF NOT EXISTS `ALERTSTATUS` (`UUID` VARCHAR(36) NOT NULL,`STATUS` TINYINT(1) NOT NULL,PRIMARY KEY (UUID))").execute();
         Query.prepare(
               "CREATE TABLE IF NOT EXISTS `BANS` (`UUID` TEXT NOT NULL,`MODULE` TEXT NOT NULL,`TIME` LONG NOT NULL,`EXTRA` TEXT,`PING` LONG NOT NULL,`TPS` DOUBLE NOT NULL)"
            )
            .execute();
         Query.prepare("CREATE TABLE IF NOT EXISTS `BANWAVE` (`UUID` TEXT NOT NULL,`MODULE` TEXT NOT NULL,`TIME` LONG NOT NULL,`TOTALLOGS` SMALLINT NOT NULL)")
            .execute();

         try {
            Query.prepare("ALTER TABLE `ALERTS` ADD COLUMN COORDS TEXT").execute();
            Query.prepare("ALTER TABLE `ALERTS` ADD COLUMN WORLD TEXT").execute();
         } catch (Exception var3) {
            Karhu.getInstance().printCool("&b> &fSQLite table already has COORDS and WORLD columns");
         }
      } catch (Exception var4) {
         System.out.println("Failed to create sqlite tables " + var4.getMessage());
         var4.printStackTrace();
      }

      new Thread(
            () -> {
               while(Karhu.getInstance() != null && Karhu.getInstance().getPlug().isEnabled()) {
                  try {
                     NetUtil.sleep(10000L);
                     if (!this.violations.isEmpty() || !this.bans.isEmpty() || !this.banWaveQueue.isEmpty()) {
                        if (!this.violations.isEmpty()) {
                           for(ViolationX violation : this.violations) {
                              try {
                                 SQLite.use();
                                 Query.prepare(
                                       "INSERT INTO `ALERTS` (`UUID`, `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS`) VALUES (?,?,?,?,?,?,?,?,?)"
                                    )
                                    .append(violation.player)
                                    .append(violation.type)
                                    .append(violation.vl)
                                    .append(violation.time)
                                    .append(violation.data)
                                    .append(violation.location)
                                    .append(violation.world)
                                    .append(violation.ping)
                                    .append(violation.TPS)
                                    .execute();
                              } catch (Exception var8) {
                                 if (var8.getMessage().contains("[SQLITE_ERROR] SQL error or missing database (table ALERTS has no column named WORLD)")) {
                                    File source = new File(Karhu.getInstance().getPlug().getDataFolder().getAbsolutePath() + "/database.sqlite");
                                    File dest = new File(Karhu.getInstance().getPlug().getDataFolder().getAbsolutePath() + "/database-old.sqlite");
                                    Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                    Query.prepare("DROP TABLE ALERTS").execute();
                                    Query.prepare(
                                          "CREATE TABLE IF NOT EXISTS `ALERTS` (`UUID` TEXT NOT NULL,`MODULE` TEXT NOT NULL,`VL` SMALLINT NOT NULL,`TIME` LONG NOT NULL,`EXTRA` TEXT,`COORDS` TEXT NOT NULL,`WORLD` TEXT NOT NULL,`PING` LONG NOT NULL,`TPS` DOUBLE NOT NULL)"
                                       )
                                       .execute();
                                 }
      
                                 var8.printStackTrace();
                              }
                           }
      
                           this.violations.clear();
                        }
      
                        if (!this.bans.isEmpty()) {
                           for(BanX ban : this.bans) {
                              try {
                                 SQLite.use();
                                 Query.prepare("INSERT INTO `BANS` (`UUID`, `MODULE`, `TIME`, `EXTRA`, `PING`, `TPS`) VALUES (?,?,?,?,?,?)")
                                    .append(ban.player)
                                    .append(ban.type)
                                    .append(ban.time)
                                    .append(ban.data)
                                    .append(ban.ping)
                                    .append(ban.TPS)
                                    .execute();
                              } catch (Exception var7) {
                                 var7.printStackTrace();
                              }
                           }
      
                           this.bans.clear();
                        }
      
                        if (!this.banWaveQueue.isEmpty()) {
                           for(BanWaveX wave : this.banWaveQueue) {
                              try {
                                 SQLite.use();
                                 Query.prepare("INSERT INTO `BANWAVE` (`UUID`, `MODULE`, `TIME`, `TOTALLOGS`) VALUES (?,?,?,?)")
                                    .append(wave.player)
                                    .append(wave.type)
                                    .append(wave.time)
                                    .append(wave.totalLogs)
                                    .execute();
                              } catch (Exception var6) {
                                 var6.printStackTrace();
                              }
                           }
      
                           this.banWaveQueue.clear();
                        }
                     }
                  } catch (Exception var9) {
                     var9.printStackTrace();
                  }
               }
            },
            "KarhuSQLiteCommitter"
         )
         .start();
   }

   @Override
   public void addAlert(ViolationX violation) {
      this.violations.add(violation);
   }

   @Override
   public void addBan(BanX ban) {
      this.bans.add(ban);
   }

   @SneakyThrows
   @Override
   public void setAlerts(String uuid, int status) {
      SQLite.use();
      Query.prepare("REPLACE INTO `ALERTSTATUS` (`UUID`, `STATUS`) VALUES (?,?)").append(uuid).append(status).execute();
   }
   @SneakyThrows
   @Override
   public boolean getAlerts(String uuid) {
      try {
         SQLite.use();
         List<Integer> alert = new ArrayList<>();
         Query.prepare("SELECT * FROM `ALERTSTATUS` WHERE `UUID` = ? limit 1").append(uuid).execute(rs -> alert.add(rs.getInt(2)));
         if (alert.isEmpty()) {
            this.setAlerts(uuid, 1);
            return true;
         } else {
            return MathUtil.getIntAsBoolean(alert.get(0));
         }
      } catch (Exception var3) {
         this.setAlerts(uuid, 1);
         return true;
      }
   }
   @SneakyThrows
   @Override
   public void loadActiveViolations(String uuid, KarhuPlayer data) {
      SQLite.use();
      List<ViolationX> violations = new ArrayList<>();
      Map<String, Integer> validVls = new HashMap<>();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME`")
         .append(uuid)
         .execute(
            rs -> violations.add(
                  new ViolationX(
                     uuid, rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)
                  )
               )
         );

      for(ViolationX v : violations) {
         if (System.currentTimeMillis() - v.time < 200000L) {
            if (!validVls.containsKey(v.type)) {
               validVls.put(v.type, v.vl);
            } else if (v.vl > validVls.get(v.type)) {
               validVls.replace(v.type, v.vl);
            }
         }
      }

      for(Check c : data.getCheckManager().getChecks()) {
         if (validVls.containsKey(c.getName())) {
            data.addViolations(c, validVls.get(c.getName()));
            int vl = data.getViolations(c, 100000L);
            data.setCheckVl((double)vl, c);
         }
      }
   }
   @SneakyThrows
   @Override
   public List<ViolationX> getViolations(String uuid, Check type, int page, int limit, long from, long to) {
      SQLite.use();
      List<ViolationX> violations = new ArrayList<>();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME` DESC LIMIT ?,?")
         .append(uuid)
         .append(page * limit)
         .append(limit)
         .execute(
            rs -> violations.add(
                  new ViolationX(
                     uuid, rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)
                  )
               )
         );
      return violations;
   }

   @Override
   @SneakyThrows
   public List<ViolationX> getAllViolations(String uuid) {
      SQLite.use();
      List<ViolationX> violations = new ArrayList<>();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME`")
         .append(uuid)
         .execute(
            rs -> violations.add(
                  new ViolationX(
                     uuid, rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)
                  )
               )
         );
      return violations;
   }

   @Override
   @SneakyThrows
   public int getViolationAmount(String uuid) {
      SQLite.use();
      AtomicInteger violations = new AtomicInteger();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS` WHERE `UUID` = ? ORDER BY `TIME`")
         .append(uuid)
         .execute(rs -> violations.incrementAndGet());
      return violations.get();
   }

   @Override
   @SneakyThrows
   public int getAllViolationsInStorage() {
      SQLite.use();
      List<ViolationX> violations = new ArrayList<>();
      Query.prepare("SELECT `MODULE`, `VL`, `TIME`, `EXTRA`, `COORDS`, `WORLD`, `PING`, `TPS` FROM `ALERTS`")
         .execute(
            rs -> violations.add(
                  new ViolationX(
                     "s", rs.getString(1), rs.getInt(2), rs.getLong(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getLong(7), rs.getDouble(8)
                  )
               )
         );
      return violations.size();
   }

   @Override
   @SneakyThrows
   public List<BanX> getRecentBans() {
      SQLite.use();
      List<BanX> bans = new ArrayList<>();
      Query.prepare("SELECT `UUID`, `MODULE`, `TIME`, `EXTRA`, `PING`, `TPS` FROM `BANS` ORDER BY `TIME`")
         .execute(rs -> bans.add(new BanX(rs.getString(1), rs.getString(2), rs.getLong(3), rs.getString(4), rs.getLong(5), rs.getDouble(6))));
      Collections.reverse(bans);
      return bans.subList(0, Math.min(bans.size(), 10));
   }

   @Override
   @SneakyThrows
   public void purge(String uuid, boolean all) {
      SQLite.use();
      Query.prepare("DELETE FROM `ALERTS` WHERE UUID = ?").append(uuid).execute();
      Query.prepare("DELETE FROM `BANS` WHERE UUID = ?").append(uuid).execute();
   }

   @Override
   @SneakyThrows
   public List<String> getBanwaveList() {
      SQLite.use();
      List<String> players = new ArrayList<>();
      Query.prepare("SELECT `UUID` FROM `BANWAVE` ORDER BY `TIME`").execute(rs -> players.add(rs.getString(1)));
      return players;
   }

   @Override
   @SneakyThrows
   public boolean isInBanwave(String uuid) {
      SQLite.use();

      try {
         ResultSet rs = Query.prepare("SELECT `MODULE`, `TIME`, `TOTALLOGS` FROM `BANWAVE` WHERE `UUID` = ? ORDER BY `TIME` DESC LIMIT ?,?")
            .append(uuid)
            .executeQuery();
         return rs.first();
      } catch (Exception var3) {
         return false;
      }
   }

   @Override
   public void addToBanWave(BanWaveX bwRequest) {
      if (!this.isInBanwave(bwRequest.player)) {
         this.banWaveQueue.add(bwRequest);
      }
   }

   @Override
   @SneakyThrows
   public void removeFromBanWave(String uuid) {
      SQLite.use();
      Optional<BanWaveX> bwx = this.banWaveQueue.stream().filter(bw -> bw.player.equals(uuid)).findFirst();
      bwx.ifPresent(banWaveX -> this.banWaveQueue.remove(banWaveX));
      Query.prepare("DELETE FROM `BANWAVE` WHERE UUID = ?").append(uuid).execute();
   }

   @Override
   public void checkFiles() {
      try {
         String acname = Karhu.getInstance().getConfigManager().getLicense().equals(" ") ? "VengeanceLoader" : "KarhuLoader";
         if (Bukkit.getServer().getPluginManager().isPluginEnabled(acname)) {
            if (NetUtil.accessFile() != 0) {
               Karhu.getInstance().getPlug().getLogger().warning("java.lang.reflect.InvocationTargetException");
               Karhu.getInstance().getPlug().getLogger().warning("at sun.reflect.GeneratedMethodAccessor8.invoke(Unknown Source)");
               Karhu.getInstance().getPlug().getLogger().warning("at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)");
               Karhu.getInstance().getPlug().getLogger().warning("at java.lang.reflect.Method.invoke(Method.java:498)");
               Karhu.getInstance()
                  .getPlug()
                  .getLogger()
                  .warning("at java.lang.invoke.MethodHandleImpl$BindCaller$T/1328599947.invoke_V(MethodHandleImpl.java:1258)");
               Karhu.getInstance()
                  .getPlug()
                  .getLogger()
                  .warning("at io.github.retrooper.packetevents.event.manager.EventManager.callEvent(EventManager.java:60)");
               Karhu.getInstance().getPlug().getLogger().warning("... 65 more");
               Bukkit.shutdown();
            }
         } else {
            Bukkit.shutdown();
         }
      } catch (Exception var2) {
      }
   }
}
