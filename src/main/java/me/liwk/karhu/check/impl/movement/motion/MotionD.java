package me.liwk.karhu.check.impl.movement.motion;

import me.liwk.karhu.Karhu;
import me.liwk.karhu.api.check.Category;
import me.liwk.karhu.api.check.CheckInfo;
import me.liwk.karhu.api.check.SubCategory;
import me.liwk.karhu.check.type.PositionCheck;
import me.liwk.karhu.data.KarhuPlayer;
import me.liwk.karhu.util.update.MovementUpdate;

@CheckInfo(
   name = "Motion (D)",
   category = Category.MOVEMENT,
   subCategory = SubCategory.MOTION,
   experimental = true
)
public final class MotionD extends PositionCheck {
   public MotionD(KarhuPlayer data, Karhu karhu) {
      super(data, karhu);
   }

   @Override
   public void handle(MovementUpdate e) {
   }
}
