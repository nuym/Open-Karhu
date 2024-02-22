package me.liwk.karhu.handler.global;

import java.util.HashMap;
import java.util.Map;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.data.potion.PotionData;
import me.liwk.karhu.data.potion.PotionEffect;

public class EffectManager {
   private final KarhuPlayer data;
   private final Map effects = new HashMap();

   public void addPotionEffect(int id, int amp) {
      PotionEffect potionEffect = PotionEffect.values()[id - 1];
      this.effects.put(id, new PotionData(potionEffect, amp));
   }

   public void removePotionEffect(int id) {
      this.effects.remove(id);
   }

   public PotionData getEffect(PotionEffect potionEffect) {
      return (PotionData)this.effects.get(potionEffect.getId());
   }

   public int getEffectStrenght(PotionEffect potionEffect) {
      return !this.hasEffect(potionEffect) ? 0 : ((PotionData)this.effects.get(potionEffect.getId())).getAmplifier();
   }

   public boolean hasEffect(PotionEffect potionEffect) {
      return this.effects.get(potionEffect.getId()) != null;
   }

   public EffectManager(KarhuPlayer data) {
      this.data = data;
   }

   public Map getEffects() {
      return this.effects;
   }
}
