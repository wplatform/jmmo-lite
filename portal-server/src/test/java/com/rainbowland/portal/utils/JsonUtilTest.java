package com.rainbowland.portal.utils;

import com.rainbowland.portal.model.RealmProto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JsonUtilTest {


    @Test
    void toJson() {

        RealmProto.ClientInformation test = new RealmProto.ClientInformation();

        test.setSecret(new int[] {});
        String s = JsonUtil.toJson(new Integer[]{1, 2, 3});
        System.out.println(s);

    }

    @Test
    void fromAttributeJsonValue() {
        String value = "JSONRealmListTicketClientInformation:{\"info\":{\"platform\":1466840628,\"currentTime\":1615570708,\"clientArch\":7878196,\"systemVersion\":\"10.0.0.17134\",\"buildVariant\":\"win32-x86_64-vc141-release\",\"timeZone\":\"Etc/UTC\",\"versionDataBuild\":36532,\"audioLocale\":2053653326,\"version\":{\"versionMajor\":9,\"versionBuild\":37474,\"versionMinor\":0,\"versionRevision\":2},\"secret\":[150,194,171,243,193,3,87,229,48,65,37,90,35,37,26,233,182,8,236,252,52,185,126,30,115,27,65,69,1,185,12,65],\"type\":5730135,\"textLocale\":2053653326,\"platformType\":5728622,\"systemArch\":7878196}}";

        RealmProto.RealmListTicketClientInformation data = JsonUtil.fromAttributeJsonValue(value, RealmProto.RealmListTicketClientInformation.class);
        Assertions.assertNotNull(data);
        Assertions.assertNotNull(data.getInfo().getSecret());

    }
}