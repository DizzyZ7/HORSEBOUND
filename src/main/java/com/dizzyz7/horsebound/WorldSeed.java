// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import java.security.SecureRandom;

record WorldSeed(long value) {
    private static final SecureRandom RANDOM = new SecureRandom();

    static WorldSeed random() {
        long value = RANDOM.nextLong();
        return new WorldSeed(value == 0L ? 1L : value);
    }
}
