package com.github.azeroth.game.account;

import java.util.ArrayList;


public class RBACPermission {
    private final int id; // id of the object
    private final String name; // name of the object
    private final ArrayList<Integer> perms = new ArrayList<>(); // Set of permissions

    // Gets the Name of the Object

    public RBACPermission(int id) {
        this(id, "");
    }

    // Gets the Id of the Object

    public RBACPermission() {
        this(0, "");
    }

    // Gets the Permissions linked to this permission

    public RBACPermission(int id, String name) {
        id = id;
        name = name;
    }

    public final String getName() {
        return name;
    }

    public final int getId() {
        return id;
    }

    public final ArrayList<Integer> getLinkedPermissions() {
        return perms;
    }

    // Adds a new linked Permission
    public final void addLinkedPermission(int id) {
        perms.add(id);
    }

    // Removes a linked Permission
    public final void removeLinkedPermission(int id) {
        perms.remove((Integer) id);
    }
}
