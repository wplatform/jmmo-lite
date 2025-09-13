package com.github.azeroth.game.account;


import java.util.ArrayList;

public class RBACData {

    private final int id; // Account id
    private final String name; // Account name
    private final int realmId; // RealmId Affected

    private final ArrayList<Integer> grantedPerms = new ArrayList<>(); // Granted permissions

    private final ArrayList<Integer> deniedPerms = new ArrayList<>(); // Denied permissions

    private byte secLevel; // Account SecurityLevel

    private ArrayList<Integer> globalPerms = new ArrayList<>(); // Calculated permissions

    // Gets the Name of the Object

    public RBACData(int id, String name, int realmId) {
        this(id, name, realmId, (byte) 255);
    }
    // Gets the Id of the Object

    public RBACData(int id, String name, int realmId, byte secLevel) {
        this.id = id;
        this.name = name;
        this.realmId = realmId;
        this.secLevel = secLevel;
    }

    // Returns all the granted permissions (after computation)

    public final String getName() {
        return name;
    }
    // Returns all the granted permissions


    public final int getId() {
        return id;
    }
    // Returns all the denied permissions


    public final ArrayList<Integer> getPermissions() {
        return globalPerms;
    }


    public final ArrayList<Integer> getGrantedPermissions() {
        return grantedPerms;
    }


    public final ArrayList<Integer> getDeniedPermissions() {
        return deniedPerms;
    }

    public final RBACCommandResult grantPermission(int permissionId) {
        return grantPermission(permissionId, 0);
    }

    public final RBACCommandResult grantPermission(int permissionId, int realmId) {
        // Check if permission Id exists
        var perm = global.getAccountMgr().getRBACPermission(permissionId);

        if (perm == null) {
            Log.outDebug(LogFilter.Rbac, "RBACData.GrantPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Permission does not exists", getId(), getName(), permissionId, realmId);

            return RBACCommandResult.IdDoesNotExists;
        }

        // Check if already added in denied list
        if (hasDeniedPermission(permissionId)) {
            Log.outDebug(LogFilter.Rbac, "RBACData.GrantPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Permission in deny list", getId(), getName(), permissionId, realmId);

            return RBACCommandResult.InDeniedList;
        }

        // Already added?
        if (hasGrantedPermission(permissionId)) {
            Log.outDebug(LogFilter.Rbac, "RBACData.GrantPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Permission already granted", getId(), getName(), permissionId, realmId);

            return RBACCommandResult.CantAddAlreadyAdded;
        }

        addGrantedPermission(permissionId);

        // Do not save to db when loading data from DB (realmId = 0)
        if (realmId != 0) {
            Log.outDebug(LogFilter.Rbac, "RBACData.GrantPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Ok and DB updated", getId(), getName(), permissionId, realmId);

            savePermission(permissionId, true, realmId);
            calculateNewPermissions();
        } else {
            Log.outDebug(LogFilter.Rbac, "RBACData.GrantPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Ok", getId(), getName(), permissionId, realmId);
        }

        return RBACCommandResult.OK;
    }


    public final RBACCommandResult denyPermission(int permissionId) {
        return denyPermission(permissionId, 0);
    }

    public final RBACCommandResult denyPermission(int permissionId, int realmId) {
        // Check if permission Id exists
        var perm = global.getAccountMgr().getRBACPermission(permissionId);

        if (perm == null) {
            Log.outDebug(LogFilter.Rbac, "RBACData.DenyPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Permission does not exists", getId(), getName(), permissionId, realmId);

            return RBACCommandResult.IdDoesNotExists;
        }

        // Check if already added in granted list
        if (hasGrantedPermission(permissionId)) {
            Log.outDebug(LogFilter.Rbac, "RBACData.DenyPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Permission in grant list", getId(), getName(), permissionId, realmId);

            return RBACCommandResult.InGrantedList;
        }

        // Already added?
        if (hasDeniedPermission(permissionId)) {
            Log.outDebug(LogFilter.Rbac, "RBACData.DenyPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Permission already denied", getId(), getName(), permissionId, realmId);

            return RBACCommandResult.CantAddAlreadyAdded;
        }

        addDeniedPermission(permissionId);

        // Do not save to db when loading data from DB (realmId = 0)
        if (realmId != 0) {
            Log.outDebug(LogFilter.Rbac, "RBACData.DenyPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Ok and DB updated", getId(), getName(), permissionId, realmId);

            savePermission(permissionId, false, realmId);
            calculateNewPermissions();
        } else {
            Log.outDebug(LogFilter.Rbac, "RBACData.DenyPermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Ok", getId(), getName(), permissionId, realmId);
        }

        return RBACCommandResult.OK;
    }


    public final RBACCommandResult revokePermission(int permissionId) {
        return revokePermission(permissionId, 0);
    }

    public final RBACCommandResult revokePermission(int permissionId, int realmId) {
        // Check if it's present in any list
        if (!hasGrantedPermission(permissionId) && !hasDeniedPermission(permissionId)) {
            Log.outDebug(LogFilter.Rbac, "RBACData.RevokePermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Not granted or revoked", getId(), getName(), permissionId, realmId);

            return RBACCommandResult.CantRevokeNotInList;
        }

        removeGrantedPermission(permissionId);
        removeDeniedPermission(permissionId);

        // Do not save to db when loading data from DB (realmId = 0)
        if (realmId != 0) {
            Log.outDebug(LogFilter.Rbac, "RBACData.RevokePermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Ok and DB updated", getId(), getName(), permissionId, realmId);

            var stmt = DB.Login.GetPreparedStatement(LoginStatements.DEL_RBAC_ACCOUNT_PERMISSION);
            stmt.AddValue(0, getId());
            stmt.AddValue(1, permissionId);
            stmt.AddValue(2, realmId);
            DB.Login.execute(stmt);

            calculateNewPermissions();
        } else {
            Log.outDebug(LogFilter.Rbac, "RBACData.RevokePermission [Id: {0} Name: {1}] (Permission {2}, RealmId {3}). Ok", getId(), getName(), permissionId, realmId);
        }

        return RBACCommandResult.OK;
    }

    public final void loadFromDB() {
        clearData();

        Log.outDebug(LogFilter.Rbac, "RBACData.LoadFromDB [Id: {0} Name: {1}]: Loading permissions", getId(), getName());
        // Load account permissions (granted and denied) that affect current realm
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_RBAC_ACCOUNT_PERMISSIONS);
        stmt.AddValue(0, getId());
        stmt.AddValue(1, getRealmId());

        loadFromDBCallback(DB.Login.query(stmt));
    }

    public final QueryCallback loadFromDBAsync() {
        clearData();

        Log.outDebug(LogFilter.Rbac, "RBACData.LoadFromDB [Id: {0} Name: {1}]: Loading permissions", getId(), getName());
        // Load account permissions (granted and denied) that affect current realm
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_RBAC_ACCOUNT_PERMISSIONS);
        stmt.AddValue(0, getId());
        stmt.AddValue(1, getRealmId());

        return DB.Login.AsyncQuery(stmt);
    }

    public final void loadFromDBCallback(SQLResult result) {
        if (!result.isEmpty()) {
            do {
                if (result.<Boolean>Read(1)) {
                    grantPermission(result.<Integer>Read(0));
                } else {
                    denyPermission(result.<Integer>Read(0));
                }
            } while (result.NextRow());
        }

        // Add default permissions
        var permissions = global.getAccountMgr().getRBACDefaultPermissions(secLevel);

        for (var id : permissions) {
            grantPermission(id);
        }

        // Force calculation of permissions
        calculateNewPermissions();
    }


    public final void addPermissions(ArrayList<Integer> permsFrom, ArrayList<Integer> permsTo) {
        for (var id : permsFrom) {
            permsTo.add(id);
        }
    }

    public final boolean hasPermission(RBACPermissions permission) {
        return globalPerms.contains((int) permission.getValue());
    }

    public final byte getSecurityLevel() {
        return secLevel;
    }

    public final void setSecurityLevel(byte id) {
        secLevel = id;
        loadFromDB();
    }

    private void savePermission(int permission, boolean granted, int realmId) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_RBAC_ACCOUNT_PERMISSION);
        stmt.AddValue(0, getId());
        stmt.AddValue(1, permission);
        stmt.AddValue(2, granted);
        stmt.AddValue(3, realmId);
        DB.Login.execute(stmt);
    }

    private void calculateNewPermissions() {
        Log.outDebug(LogFilter.Rbac, "RBACData.CalculateNewPermissions [Id: {0} Name: {1}]", getId(), getName());

        // Get the list of granted permissions
        globalPerms = getGrantedPermissions();
        expandPermissions(globalPerms);
        var revoked = getDeniedPermissions();
        expandPermissions(revoked);
        removePermissions(globalPerms, revoked);
    }

    /**
     * Removes a list of permissions from another list
     *
     * @param permsFrom
     * @param permsToRemove
     */
    private void removePermissions(ArrayList<Integer> permsFrom, ArrayList<Integer> permsToRemove) {
        for (var id : permsToRemove) {
            permsFrom.remove((Integer) id);
        }
    }


    private void expandPermissions(ArrayList<Integer> permissions) {
        ArrayList<Integer> toCheck = new ArrayList<Integer>(permissions);
        permissions.clear();

        while (!toCheck.isEmpty()) {
            // remove the permission from original list
            var permissionId = toCheck.FirstOrDefault();
            toCheck.remove(0);

            var permission = global.getAccountMgr().getRBACPermission(permissionId);

            if (permission == null) {
                continue;
            }

            // insert into the final list (expanded list)
            permissions.add(permissionId);

            // add all linked permissions (that are not already expanded) to the list of permissions to be checked
            var linkedPerms = permission.getLinkedPermissions();

            for (var id : linkedPerms) {
                if (!permissions.contains(id)) {
                    toCheck.add(id);
                }
            }
        }

        //Log.outDebug(LogFilter.General, "RBACData:ExpandPermissions: Expanded: {0}", GetDebugPermissionString(permissions));
    }

    private void clearData() {
        grantedPerms.clear();
        deniedPerms.clear();
        globalPerms.clear();
    }

    private int getRealmId() {
        return realmId;
    }

    // Checks if a permission is granted
    private boolean hasGrantedPermission(int permissionId) {
        return grantedPerms.contains(permissionId);
    }

    // Checks if a permission is denied
    private boolean hasDeniedPermission(int permissionId) {
        return deniedPerms.contains(permissionId);
    }

    // Adds a new granted permission
    private void addGrantedPermission(int permissionId) {
        grantedPerms.add(permissionId);
    }

    // Removes a granted permission
    private void removeGrantedPermission(int permissionId) {
        grantedPerms.remove((Integer) permissionId);
    }

    // Adds a new denied permission
    private void addDeniedPermission(int permissionId) {
        deniedPerms.add(permissionId);
    }

    // Removes a denied permission
    private void removeDeniedPermission(int permissionId) {
        deniedPerms.remove((Integer) permissionId);
    }
}
