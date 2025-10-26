package com.github.azeroth.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@Getter
@RequiredArgsConstructor
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
