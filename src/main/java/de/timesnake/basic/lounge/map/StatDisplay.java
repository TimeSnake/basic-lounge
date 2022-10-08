/*
 * basic-lounge.main
 * Copyright (C) 2022 timesnake
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; If not, see <http://www.gnu.org/licenses/>.
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

    public StatDisplay(ExBlock block, BlockSide facing, BlockSide orientation, Color titleColor, Color statNameColor,
                       Color statFirstColor, Color statSecondColor, Color statThirdColor) {
        this.block = block;
        this.facing = facing;
        this.orientation = orientation;
        this.titleColor = titleColor;
        this.statNameColor = statNameColor;
        this.statFirstColor = statFirstColor;
        this.statSecondColor = statSecondColor;
        this.statThirdColor = statThirdColor;
    }

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
