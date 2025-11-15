package com.github.azeroth.auth.realm;

import lombok.Data;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;


import java.util.List;

@Data
public class Realm {

    public static final int[] CONFIG_ID_BY_TYPE = new int[]{ //size MAX_CLIENT_REALM_TYPE
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14
    };

    public static final int REALM_TYPE_NORMAL = 0;
    public static final int REALM_TYPE_PVP = 1;
    public static final int REALM_TYPE_NORMAL2 = 4;
    public static final int REALM_TYPE_RP = 6;
    public static final int REALM_TYPE_RPPVP = 8;
    public static final int MAX_CLIENT_REALM_TYPE = 14;
    public static final int REALM_TYPE_FFA_PVP = 16;

    public static final int REALM_FLAG_NONE = 0x00;
    public static final int REALM_FLAG_VERSION_MISMATCH = 0x01;
    public static final int REALM_FLAG_OFFLINE = 0x02;
    public static final int REALM_FLAG_SPECIFY_BUILD = 0x04;
    public static final int REALM_FLAG_UNK1 = 0x08;
    public static final int REALM_FLAG_UNK2 = 0x10;
    public static final int REALM_FLAG_RECOMMENDED = 0x20;
    public static final int REALM_FLAG_NEW = 0x40;
    public static final int REALM_FLAG_FULL = 0x80;


    private RealmKey id;
    private int build;
    private List<InetAddress> addresses;
    private InetAddress localSubnetMask;
    private int port;
    private String name;
    private String normalizedName;
    private int type;
    private int flags;
    private int timezone;
    private int allowedSecurityLevel;
    private RealmPopulationState populationLevel;


    public InetAddress getAddressForClient(InetAddress clientAddress) {
        InetAddress localIpv6 = null;
        InetAddress externalIpv6 = null;
        InetAddress loopbackIpv6 = null;
        InetAddress localIpv4 = null;
        InetAddress externalIpv4 = null;
        InetAddress loopbackIpv4 = null;

        for (int i = 0; i < addresses.size(); i++) {
            InetAddress inetAddress = addresses.get(i);

            if (inetAddress.isLoopbackAddress()) {
                if (inetAddress instanceof Inet6Address && loopbackIpv6 == null) {
                    loopbackIpv6 = inetAddress;
                }

                if (inetAddress instanceof Inet4Address && loopbackIpv4 == null) {
                    loopbackIpv4 = inetAddress;
                }
            } else if (isInLocalNetwork(clientAddress)) {
                if (inetAddress instanceof Inet6Address && localIpv6 == null)
                    localIpv6 = inetAddress;

                if (inetAddress instanceof Inet6Address && localIpv4 == null)
                    localIpv4 = inetAddress;
            } else {
                if (inetAddress instanceof Inet6Address && externalIpv6 == null)
                    externalIpv6 = inetAddress;

                if (inetAddress instanceof Inet4Address && externalIpv4 == null)
                    externalIpv4 = inetAddress;
            }
        }

        if (isInLocalNetwork(clientAddress) || clientAddress.isLoopbackAddress()) {
            // client is in the same network as this process, prefer local addresses

            // first, try finding a local ipv6 address
            if (clientAddress instanceof Inet6Address && localIpv6 != null) {
                // we have a usable ipv6 local address
                return localIpv6;
            }

            // we dont have a local v6, return local v4
            if (localIpv4 != null)
                return localIpv4;
        }

        if (clientAddress.isLoopbackAddress()) {
            // fallback, search for a loopback address in configuration
            if (clientAddress instanceof Inet6Address && loopbackIpv6 != null)
                return loopbackIpv6;

            if (loopbackIpv4 != null)
                return loopbackIpv4;
        }

        // client is NOT in the same network as this process
        if (clientAddress instanceof Inet6Address && externalIpv6 != null)
            return externalIpv6;


        if (externalIpv4 != null) {
            return externalIpv4;
        }


        if (addresses.size() > 1 && clientAddress.isLoopbackAddress())
            return addresses.get(1);

        return addresses.getFirst();
    }


    public static boolean isInLocalNetwork(InetAddress client) {
        return false;
    }
}
