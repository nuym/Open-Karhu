package me.liwk.karhu.event;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

public class ActionEvent extends Event {
   private final WrapperPlayClientEntityAction.Action action;

   public ActionEvent(WrapperPlayClientEntityAction.Action action) {
      this.action = action;
   }

   public WrapperPlayClientEntityAction.Action getAction() {
      return this.action;
   }
}
