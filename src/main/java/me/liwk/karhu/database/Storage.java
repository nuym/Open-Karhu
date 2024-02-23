package me.liwk.karhu.database;

import java.util.List;
import me.liwk.karhu.check.api.BanWaveX;
import me.liwk.karhu.check.api.BanX;
import me.liwk.karhu.check.api.Check;
import me.liwk.karhu.check.api.ViolationX;
import me.liwk.karhu.data.KarhuPlayer;

public interface Storage {
   void init() throws Throwable;

   void addAlert(ViolationX var1);

   void addBan(BanX var1);

   List getViolations(String var1, Check var2, int var3, int var4, long var5, long var7) throws Throwable;

   List getAllViolations(String var1) throws Throwable;

   List getBanwaveList() throws Throwable;

   boolean isInBanwave(String var1);

   void addToBanWave(BanWaveX var1);

   void removeFromBanWave(String var1) throws Throwable;

   int getViolationAmount(String var1) throws Throwable;

   void loadActiveViolations(String var1, KarhuPlayer var2);

   void purge(String var1, boolean var2) throws Throwable;

   int getAllViolationsInStorage() throws Throwable;

   List getRecentBans() throws Throwable;

   void checkFiles();

   void setAlerts(String var1, int var2);

   boolean getAlerts(String var1);
}
