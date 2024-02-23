package me.liwk.karhu.database.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import me.liwk.karhu.Karhu;
import me.liwk.karhu.check.api.AlertsX;
import me.liwk.karhu.check.api.BanWaveX;
import me.liwk.karhu.check.api.BanX;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.database.Storage;
import me.liwk.karhu.manager.ConfigManager;
import me.liwk.karhu.util.MathUtil;
import me.liwk.karhu.util.NetUtil;
import me.liwk.karhu.util.task.Tasker;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bukkit.Bukkit;

public class MongoStorage implements Storage {
   private final CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(new CodecRegistry[]{MongoClient.getDefaultCodecRegistry(), CodecRegistries.fromProviders(new CodecProvider[]{PojoCodecProvider.builder().automatic(true).build()})});
   private MongoCollection loggedViolations;
   private MongoCollection loggedBans;
   private MongoCollection loggedStatus;
   private MongoCollection loggedBanwavePlayers;
   private final ConcurrentLinkedQueue violations = new ConcurrentLinkedQueue();
   private final ConcurrentLinkedQueue bans = new ConcurrentLinkedQueue();
   private final ConcurrentLinkedQueue banWaveQueue = new ConcurrentLinkedQueue();
   public String host;
   public String database;
   public String username;
   public String password;
   public int port;
   public boolean auth;

   public MongoStorage() {
      ConfigManager cfg = Karhu.getInstance().getConfigManager();
      this.host = cfg.getConfig().getString("mongo.host");
      this.port = cfg.getConfig().getInt("mongo.port");
      this.database = cfg.getConfig().getString("mongo.database");
      this.auth = cfg.getConfig().getBoolean("mongo.authentication.enabled");
      this.username = cfg.getConfig().getString("mongo.authentication.username");
      this.password = cfg.getConfig().getString("mongo.authentication.password");
   }

   public void init() {
      MongoClient client;
      if (this.auth) {
         MongoCredential credentials = MongoCredential.createCredential(this.username, this.database, this.password.toCharArray());
         client = new MongoClient(new ServerAddress(this.host, this.port), credentials, MongoClientOptions.builder().codecRegistry(this.pojoCodecRegistry).build());
      } else {
         client = new MongoClient(new ServerAddress(this.host, this.port), MongoClientOptions.builder().codecRegistry(this.pojoCodecRegistry).build());
      }

      MongoDatabase mongodb = client.getDatabase(this.database);
      this.loggedViolations = mongodb.getCollection("violations", ViolationX.class);
      this.loggedBans = mongodb.getCollection("bans", BanX.class);
      this.loggedStatus = mongodb.getCollection("status", AlertsX.class);
      this.loggedBanwavePlayers = mongodb.getCollection("banwave", BanWaveX.class);
      (new Thread(() -> {
         while(Karhu.getInstance() != null && Karhu.getInstance().getPlug().isEnabled()) {
            Exception e;
            try {
               NetUtil.sleep(10000L);
               if (!this.violations.isEmpty() || !this.bans.isEmpty() || !this.banWaveQueue.isEmpty()) {
                  if (!this.violations.isEmpty()) {
                     try {
                        this.loggedViolations.insertMany(new ArrayList(this.violations));
                     } catch (Exception var4) {
                        e = var4;
                        e.printStackTrace();
                     }

                     this.violations.clear();
                  }

                  if (!this.bans.isEmpty()) {
                     try {
                        this.loggedBans.insertMany(new ArrayList(this.bans));
                     } catch (Exception var3) {
                        e = var3;
                        e.printStackTrace();
                     }

                     this.bans.clear();
                  }

                  if (!this.banWaveQueue.isEmpty()) {
                     try {
                        this.loggedBanwavePlayers.insertMany(new ArrayList(this.banWaveQueue));
                     } catch (Exception var2) {
                        e = var2;
                        e.printStackTrace();
                     }

                     this.banWaveQueue.clear();
                  }
               }
            } catch (Exception var5) {
               e = var5;
               e.printStackTrace();
            }
         }

      }, "KarhuMongoCommitter")).start();
   }

   public void addAlert(ViolationX violation) {
      this.violations.add(violation);
   }

   public void addBan(BanX ban) {
      this.bans.add(ban);
   }

   public void setAlerts(String uuid, int status) {
      this.loggedStatus.replaceOne(Filters.eq("player", uuid), new AlertsX(uuid, status));
   }

   public boolean getAlerts(String uuid) {
      AlertsX alertsX = (AlertsX)this.loggedStatus.find(Filters.eq("player", uuid)).limit(1).first();
      if (alertsX != null) {
         return MathUtil.getIntAsBoolean(alertsX.status);
      } else {
         this.loggedStatus.replaceOne(Filters.eq("player", uuid), new AlertsX(uuid, 1));
         return true;
      }
   }

   public void loadActiveViolations(String uuid, KarhuPlayer data) {
      Tasker.taskAsync(() -> {
         List violations = new ArrayList();
         Map validVls = new HashMap();
         this.loggedViolations.find(Filters.eq("player", uuid)).sort(new Document("time", -1)).forEach(violations::add);
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
            if (validVls.containsKey(c.getCheckInfo().name())) {
               data.addViolations(c, (Integer)validVls.get(c.getName()));
               int vl = data.getViolations(c, 100000L);
               data.setCheckVl((double)vl, c);
            }
         }

      });
   }

   public List getViolations(String uuid, Check type, int page, int limit, long from, long to) {
      List violations = new ArrayList();
      this.loggedViolations.find(Filters.eq("player", uuid)).skip(page * limit).limit(limit).sort(new Document("time", -1)).forEach(violations::add);
      return violations;
   }

   public int getViolationAmount(String uuid) {
      AtomicInteger violations = new AtomicInteger();
      this.loggedViolations.find(Filters.eq("player", uuid)).sort(new Document("time", -1)).forEach((v) -> {
         violations.incrementAndGet();
      });
      return violations.get();
   }

   public List getAllViolations(String uuid) {
      List violations = new ArrayList();
      this.loggedViolations.find(Filters.eq("player", uuid)).sort(new Document("time", -1)).forEach(violations::add);
      return violations;
   }

   public List getBanwaveList() {
      List players = new ArrayList();
      this.loggedBanwavePlayers.find().forEach((huora) -> {
         players.add(huora.player);
      });
      return players;
   }

   public int getAllViolationsInStorage() {
      List violations = new ArrayList();
      this.loggedViolations.find().forEach(violations::add);
      return violations.size();
   }

   public List getRecentBans() {
      List bans = new ArrayList();
      this.loggedBans.find().limit(10).forEach(bans::add);
      return bans;
   }

   public void purge(String uuid, boolean all) {
      if (all) {
         this.loggedViolations.drop();
      } else {
         this.loggedViolations.deleteMany(Filters.eq("player", uuid));
      }

   }

   public void addToBanWave(BanWaveX bwRequest) {
      if (!this.isInBanwave(bwRequest.player)) {
         this.banWaveQueue.add(bwRequest);
      }

   }

   public boolean isInBanwave(String uuid) {
      BanWaveX bw = (BanWaveX)this.loggedBanwavePlayers.find(Filters.eq("player", uuid)).first();
      return bw != null;
   }

   public void removeFromBanWave(String uuid) {
      this.loggedBanwavePlayers.findOneAndDelete(Filters.eq("player", uuid));
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
