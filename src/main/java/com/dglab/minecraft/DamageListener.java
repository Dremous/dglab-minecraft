package com.dglab.minecraft;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DamageListener {
    private static final Logger LOGGER = LoggerFactory.getLogger("dglab-minecraft");
    
    public static void register() {
        LOGGER.info("Damage listener registered (using mixin)");
    }
}
