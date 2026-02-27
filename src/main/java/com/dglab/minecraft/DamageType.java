package com.dglab.minecraft;

public enum DamageType {
    GENERIC("generic", 50, 1.0, 1.0, WaveType.STABLE),
    FIRE("fire", 80, 1.2, 1.5, WaveType.BURNING),
    LAVA("lava", 100, 1.5, 2.0, WaveType.BURNING),
    FALL("fall", 60, 1.0, 0.8, WaveType.IMPACT),
    DROWN("drown", 30, 0.5, 2.0, WaveType.GRADUAL),
    STARVE("starve", 20, 0.3, 3.0, WaveType.GRADUAL),
    CACTUS("cactus", 50, 0.8, 1.0, WaveType.SHARP),
    WITHER("wither", 70, 1.3, 1.5, WaveType.FLUCTUATE),
    MAGIC("magic", 90, 1.5, 1.2, WaveType.ELECTRIC),
    THORNS("thorns", 60, 1.0, 1.0, WaveType.SHARP),
    EXPLOSION("explosion", 100, 2.0, 0.5, WaveType.IMPACT),
    LIGHTNING("lightning", 100, 2.5, 0.3, WaveType.ELECTRIC),
    HOT_FLOOR("hot_floor", 40, 0.6, 1.0, WaveType.BURNING),
    CRAMMING("cramming", 50, 1.0, 1.0, WaveType.GRADUAL),
    DRAGON_BREATH("dragon_breath", 80, 1.5, 1.5, WaveType.FLUCTUATE),
    FREEZE("freeze", 60, 1.0, 1.5, WaveType.GRADUAL),
    MOB_ATTACK("mob_attack", 70, 1.2, 1.0, WaveType.IMPACT),
    PLAYER_ATTACK("player_attack", 80, 1.3, 1.0, WaveType.IMPACT),
    ARROW("arrow", 50, 0.8, 0.8, WaveType.SHARP),
    TRIDENT("trident", 70, 1.2, 1.0, WaveType.SHARP),
    PROJECTILE("projectile", 50, 0.9, 0.8, WaveType.SHARP),
    FALLING_BLOCK("falling_block", 80, 1.5, 0.5, WaveType.IMPACT),
    ANVIL("anvil", 100, 2.0, 0.3, WaveType.IMPACT),
    SUFFOCATION("suffocation", 40, 0.5, 1.5, WaveType.GRADUAL),
    VOID("void", 100, 3.0, 0.1, WaveType.ELECTRIC),
    SWEET_BERRY("sweet_berry", 30, 0.5, 0.5, WaveType.SHARP),
    STING("sting", 50, 0.8, 0.8, WaveType.SHARP);
    
    public final String name;
    public final int baseStrength;
    public final double strengthMultiplier;
    public final double durationMultiplier;
    public final WaveType waveType;
    
    DamageType(String name, int baseStrength, double strengthMultiplier, double durationMultiplier, WaveType waveType) {
        this.name = name;
        this.baseStrength = baseStrength;
        this.strengthMultiplier = strengthMultiplier;
        this.durationMultiplier = durationMultiplier;
        this.waveType = waveType;
    }
    
    public static DamageType fromDamageSource(String damageSourceName) {
        if (damageSourceName == null) return GENERIC;
        
        String name = damageSourceName.toLowerCase().replace(" ", "_");
        
        if (name.contains("fire") || name.contains("flame")) return FIRE;
        if (name.contains("lava")) return LAVA;
        if (name.contains("fall")) return FALL;
        if (name.contains("drown")) return DROWN;
        if (name.contains("starve") || name.contains("hunger")) return STARVE;
        if (name.contains("cactus")) return CACTUS;
        if (name.contains("wither")) return WITHER;
        if (name.contains("magic") || name.contains("spell")) return MAGIC;
        if (name.contains("thorn")) return THORNS;
        if (name.contains("explos") || name.contains("tnt") || name.contains("fireball")) return EXPLOSION;
        if (name.contains("lightning")) return LIGHTNING;
        if (name.contains("hot_floor") || name.contains("magma")) return HOT_FLOOR;
        if (name.contains("cramming")) return CRAMMING;
        if (name.contains("dragon")) return DRAGON_BREATH;
        if (name.contains("freeze") || name.contains("powder_snow")) return FREEZE;
        if (name.contains("mob") || name.contains("monster")) return MOB_ATTACK;
        if (name.contains("player")) return PLAYER_ATTACK;
        if (name.contains("arrow")) return ARROW;
        if (name.contains("trident")) return TRIDENT;
        if (name.contains("projectile")) return PROJECTILE;
        if (name.contains("falling_block") || name.contains("falling")) return FALLING_BLOCK;
        if (name.contains("anvil") || name.contains("falling")) return ANVIL;
        if (name.contains("suffocat") || name.contains("wall")) return SUFFOCATION;
        if (name.contains("void") || name.contains("out_of_world")) return VOID;
        if (name.contains("sweet_berry")) return SWEET_BERRY;
        if (name.contains("sting") || name.contains("bee")) return STING;
        
        return GENERIC;
    }
}
