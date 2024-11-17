/*
 * Copyright (C) 2023 timesnake
 */

package de.timesnake.basic.lounge.map;

import de.timesnake.basic.bukkit.util.world.ExBlock;
import de.timesnake.basic.bukkit.util.world.ExWorld;
import de.timesnake.database.util.game.DbLoungeMapDisplay;
import de.timesnake.database.util.object.BlockSide;

import java.awt.*;

public class StatDisplay {

  private final ExBlock block;
  private final BlockSide facing;
  private final BlockSide orientation;
  private final Color titleColor;
  private final Color statNameColor;
  private final Color statFirstColor;
  private final Color statSecondColor;
  private final Color statThirdColor;

  public StatDisplay(ExWorld world, DbLoungeMapDisplay display) {
    this.block = new ExBlock(world.getBlockAt(display.getX(), display.getY(), display.getZ()));
    this.facing = display.getFacing();
    this.orientation = display.getOrientation();
    this.titleColor = display.getTitleColor();
    this.statNameColor = display.getStatNameColor();
    this.statFirstColor = display.getStatFirstColor();
    this.statSecondColor = display.getStatSecondColor();
    this.statThirdColor = display.getStatThirdColor();
  }

  public ExBlock getBlock() {
    return block;
  }

  public BlockSide getFacing() {
    return facing;
  }

  public BlockSide getOrientation() {
    return orientation;
  }

  public Color getTitleColor() {
    return titleColor;
  }

  public Color getStatNameColor() {
    return statNameColor;
  }

  public Color getStatFirstColor() {
    return statFirstColor;
  }

  public Color getStatSecondColor() {
    return statSecondColor;
  }

  public Color getStatThirdColor() {
    return statThirdColor;
  }
}
