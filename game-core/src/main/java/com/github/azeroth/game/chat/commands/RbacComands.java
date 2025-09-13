package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.account.RBACData;
import com.github.azeroth.game.chat.AccountIdentifier;
import com.github.azeroth.game.chat.CommandHandler;


class RbacComands {

    private static boolean handleRBACListPermissionsCommand(CommandHandler handler, Integer permId) {
        if (permId == null) {
            var permissions = global.getAccountMgr().getRBACPermissionList();
            handler.sendSysMessage(SysMessage.RbacListPermissionsHeader);


            for (var(_, permission) : permissions) {
                handler.sendSysMessage(SysMessage.RbacListElement, permission.id, permission.name);
            }
        } else {
            var permission = global.getAccountMgr().getRBACPermission(permId.intValue());

            if (permission == null) {
                handler.sendSysMessage(SysMessage.RbacWrongParameterId, permId.intValue());

                return false;
            }

            handler.sendSysMessage(SysMessage.RbacListPermissionsHeader);
            handler.sendSysMessage(SysMessage.RbacListElement, permission.getId(), permission.getName());
            handler.sendSysMessage(SysMessage.RbacListPermsLinkedHeader);

            for (var linkedPerm : permission.getLinkedPermissions()) {
                var rbacPermission = global.getAccountMgr().getRBACPermission(linkedPerm);

                if (rbacPermission != null) {
                    handler.sendSysMessage(SysMessage.RbacListElement, rbacPermission.getId(), rbacPermission.getName());
                }
            }
        }

        return true;
    }

    private static RBACCommandData getRBACData(AccountIdentifier account) {
        if (account.isConnected()) {
            RBACCommandData tempVar = new RBACCommandData();
            tempVar.rbac = account.getConnectedSession().getRBACData();
            tempVar.needDelete = false;
            return tempVar;
        }

        RBACData rbac = new RBACData(account.getID(), account.getName(), (int) global.getWorldMgr().getRealmId().index, (byte) global.getAccountMgr().getSecurity(account.getID(), (int) global.getWorldMgr().getRealmId().index).getValue());
        rbac.loadFromDB();

        RBACCommandData tempVar2 = new RBACCommandData();
        tempVar2.rbac = rbac;
        tempVar2.needDelete = true;
        return tempVar2;
    }


    private static class RbacAccountCommands {

        private static boolean handleRBACPermDenyCommand(CommandHandler handler, AccountIdentifier account, int permId, Integer realmId) {
            if (account == null) {
                account = AccountIdentifier.fromTarget(handler);
            }

            if (account == null) {
                return false;
            }

            if (handler.hasLowerSecurityAccount(null, account.getID(), true)) {
                return false;
            }

            if (realmId == null) {
                realmId = -1;
            }

            var data = getRBACData(account);

            var result = data.rbac.denyPermission(permId, realmId.intValue());
            var permission = global.getAccountMgr().getRBACPermission(permId);

            switch (result) {
                case CantAddAlreadyAdded:
                    handler.sendSysMessage(SysMessage.RbacPermDeniedInList, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case InGrantedList:
                    handler.sendSysMessage(SysMessage.RbacPermDeniedInGrantedList, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case OK:
                    handler.sendSysMessage(SysMessage.RbacPermDenied, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case IdDoesNotExists:
                    handler.sendSysMessage(SysMessage.RbacWrongParameterId, permId);

                    break;
                default:
                    break;
            }

            return true;
        }


        private static boolean handleRBACPermGrantCommand(CommandHandler handler, AccountIdentifier account, int permId, Integer realmId) {
            if (account == null) {
                account = AccountIdentifier.fromTarget(handler);
            }

            if (account == null) {
                return false;
            }

            if (handler.hasLowerSecurityAccount(null, account.getID(), true)) {
                return false;
            }

            if (realmId == null) {
                realmId = -1;
            }

            var data = getRBACData(account);

            var result = data.rbac.grantPermission(permId, realmId.intValue());
            var permission = global.getAccountMgr().getRBACPermission(permId);

            switch (result) {
                case CantAddAlreadyAdded:
                    handler.sendSysMessage(SysMessage.RbacPermGrantedInList, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case InDeniedList:
                    handler.sendSysMessage(SysMessage.RbacPermGrantedInDeniedList, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case OK:
                    handler.sendSysMessage(SysMessage.RbacPermGranted, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case IdDoesNotExists:
                    handler.sendSysMessage(SysMessage.RbacWrongParameterId, permId);

                    break;
                default:
                    break;
            }

            return true;
        }


        private static boolean handleRBACPermListCommand(CommandHandler handler, AccountIdentifier account) {
            if (account == null) {
                account = AccountIdentifier.fromTarget(handler);
            }

            if (account == null) {
                return false;
            }

            var data = getRBACData(account);

            handler.sendSysMessage(SysMessage.RbacListHeaderGranted, data.rbac.getId(), data.rbac.getName());
            var granted = data.rbac.getGrantedPermissions();

            if (granted.isEmpty()) {
                handler.sendSysMessage(SysMessage.RbacListEmpty);
            } else {
                for (var id : granted) {
                    var permission = global.getAccountMgr().getRBACPermission(id);
                    handler.sendSysMessage(SysMessage.RbacListElement, permission.getId(), permission.getName());
                }
            }

            handler.sendSysMessage(SysMessage.RbacListHeaderDenied, data.rbac.getId(), data.rbac.getName());
            var denied = data.rbac.getDeniedPermissions();

            if (denied.isEmpty()) {
                handler.sendSysMessage(SysMessage.RbacListEmpty);
            } else {
                for (var id : denied) {
                    var permission = global.getAccountMgr().getRBACPermission(id);
                    handler.sendSysMessage(SysMessage.RbacListElement, permission.getId(), permission.getName());
                }
            }

            handler.sendSysMessage(SysMessage.RbacListHeaderBySecLevel, data.rbac.getId(), data.rbac.getName(), data.rbac.getSecurityLevel());
            var defaultPermissions = global.getAccountMgr().getRBACDefaultPermissions(data.rbac.getSecurityLevel());

            if (defaultPermissions.isEmpty()) {
                handler.sendSysMessage(SysMessage.RbacListEmpty);
            } else {
                for (var id : defaultPermissions) {
                    var permission = global.getAccountMgr().getRBACPermission(id);
                    handler.sendSysMessage(SysMessage.RbacListElement, permission.getId(), permission.getName());
                }
            }

            return true;
        }


        private static boolean handleRBACPermRevokeCommand(CommandHandler handler, AccountIdentifier account, int permId, Integer realmId) {
            if (account == null) {
                account = AccountIdentifier.fromTarget(handler);
            }

            if (account == null) {
                return false;
            }

            if (handler.hasLowerSecurityAccount(null, account.getID(), true)) {
                return false;
            }

            if (realmId == null) {
                realmId = -1;
            }

            var data = getRBACData(account);

            var result = data.rbac.revokePermission(permId, realmId.intValue());
            var permission = global.getAccountMgr().getRBACPermission(permId);

            switch (result) {
                case CantRevokeNotInList:
                    handler.sendSysMessage(SysMessage.RbacPermRevokedNotInList, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case OK:
                    handler.sendSysMessage(SysMessage.RbacPermRevoked, permId, permission.getName(), realmId.intValue(), data.rbac.getId(), data.rbac.getName());

                    break;
                case IdDoesNotExists:
                    handler.sendSysMessage(SysMessage.RbacWrongParameterId, permId);

                    break;
                default:
                    break;
            }

            return true;
        }
    }

    private static class RBACCommandData {
        public RBACData rbac;
        public boolean needDelete;
    }
}
