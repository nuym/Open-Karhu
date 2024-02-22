package me.liwk.karhu.database;

import java.util.List;
import me.liwk.karhu.check.api.BanWaveX;
import me.liwk.karhu.check.api.BanX;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.data.KarhuPlayer;

public interface Storage {
   void init();

   void addAlert(ViolationX var1);

   void addBan(BanX var1);

   List getViolations(String var1, Check var2, int var3, int var4, long var5, long var7);

   List getAllViolations(String var1);

   List getBanwaveList();

   boolean isInBanwave(String var1);

   void addToBanWave(BanWaveX var1);

   void removeFromBanWave(String var1);

   int getViolationAmount(String var1);

   void loadActiveViolations(String var1, KarhuPlayer var2);

   void purge(String var1, boolean var2);

   int getAllViolationsInStorage();

   List getRecentBans();

   void checkFiles();

   void setAlerts(String var1, int var2);

   boolean getAlerts(String var1);
}
