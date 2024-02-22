package me.liwk.karhu.check.impl.movement.fly;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PacketCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.event.Event;

@CheckInfo(
   name = "Fly (A)",
   category = Category.MOVEMENT,
   subCategory = SubCategory.FLY,
   experimental = false
)
public final class FlyA extends PacketCheck {
   public FlyA(KarhuPlayer data, Karhu karhu) {
      super(data, karhu);
   }

   public void handle(Event p) {
   }
}
