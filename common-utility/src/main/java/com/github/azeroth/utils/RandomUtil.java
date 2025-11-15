package com.github.azeroth.utils;

import com.github.azeroth.common.Assert;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RandomUtil {

    private static volatile SecureRandom SECURE_RANDOM;

    private static Random random() {
        if(SECURE_RANDOM == null) {
            synchronized (RandomUtil.class) {
                if(SECURE_RANDOM == null) {
                    try {
                        SECURE_RANDOM = SecureRandom.getInstanceStrong();
                    } catch (NoSuchAlgorithmException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return SECURE_RANDOM;
    }

    public static boolean randomBoolean() {
        return random().nextBoolean();
    }

    public static byte[] randomBytes(int count) {
        byte[] bytes = new byte[count];
        random().nextBytes(bytes);
        return bytes;
    }

    public static byte[] randomBytes(byte[] bytes) {
        random().nextBytes(bytes);
        return bytes;
    }

    public static double randomDouble() {
        return random().nextDouble();
    }
    public static double randomDouble(double startInclusive, double endExclusive) {
        return random().nextDouble() * (endExclusive - startInclusive);
    }
    public static float randomFloat() {
        return random().nextFloat();
    }


    public static float randomFloat(final float startInclusive, final float endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + (endExclusive - startInclusive) * random().nextFloat();
    }

    public static int randomInt() {
        return random().nextInt();
    }
    public static int randomInt(int startInclusive, int endExclusive) {
        Assert.isTrue(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        Assert.isTrue(startInclusive >= 0, "Both range values must be non-negative.");
        if (startInclusive == endExclusive) {
            return startInclusive;
        }
        return startInclusive + random().nextInt(endExclusive - startInclusive);
    }

    public static <T> T random(List<T> collection) {
        if(collection == null ||collection.isEmpty()) {
            return null;
        }
        // If only one element, ignore the probability (even if 0)
        if(collection.size() == 1) {
            return collection.getFirst();
        }

        int index = randomInt(0, collection.size());
        return collection.get(index);
    }

    public static <T> T randomByWeight(Collection<T> collection, Function<T, Float> eleWeight) {
        if(collection == null ||collection.isEmpty()) {
            return null;
        }
        // If only one element, ignore the probability (even if 0)
        if(collection.size() == 1) {
            return collection.iterator().next();
        }
        float totalWeight = collection.stream().map(eleWeight).filter(weight -> weight >= 0f).reduce(0f, Float::sum);

        if (totalWeight <= 0f) {
            return null;
        }

        float randomFloat = randomFloat(0f, totalWeight);
        float currentWeight = 0f;

        for (T element : collection) {
            float weight = eleWeight.apply(element);
            if (weight >= 0f) {
                currentWeight += weight;
                if (randomFloat <= currentWeight) {
                    return element;
                }
            }
        }

        return null;
    }

    public static boolean randChance(int chance) {
        return chance > randomInt(0, 100);
    }

    public static boolean randChance(float chance) {
        return chance > randomFloat(0f, 100f);
    }
}
