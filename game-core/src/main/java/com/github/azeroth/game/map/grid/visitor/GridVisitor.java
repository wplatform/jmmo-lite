package com.github.azeroth.game.map.grid.visitor;


import com.github.azeroth.game.entity.object.WorldObject;

@FunctionalInterface
public interface GridVisitor {

    GridVisitorResult visit(WorldObject source);

}
