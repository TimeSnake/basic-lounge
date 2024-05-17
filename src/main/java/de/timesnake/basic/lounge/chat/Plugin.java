/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.chat;

public class Plugin extends de.timesnake.basic.bukkit.util.chat.Plugin {

  public static final Plugin LOUNGE = new Plugin("Lounge", "BLG");

  protected Plugin(String name, String code) {
    super(name, code);
  }
}
