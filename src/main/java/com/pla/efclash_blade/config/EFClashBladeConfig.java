package com.pla.efclash_blade.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class EFClashBladeConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACK_LIST;
    public static ForgeConfigSpec.ConfigValue<Integer> BREAK_WEAPON_VALUE_ON_CLASH;
    public static ForgeConfigSpec.ConfigValue<Double> CLASH_CHANCE;
    public static ForgeConfigSpec.ConfigValue<Boolean> SHAKE_SCREEN;

    static {
        BLACK_LIST = BUILDER.comment(
                        "A list of entities that can't do Clash Blade",
                        "Input: [\"minecraft\"] will disable Clash Blade for all of entities from Minecraft",
                        "Input: [\"minecraft:zombie\"] will disable Clash Blade for zombie entity from Minecraft.")
                .defineList("blackList", List.of("nightfall_invade"), entry -> entry instanceof String);
        BREAK_WEAPON_VALUE_ON_CLASH = BUILDER.comment(
                        "The value for breaking weapon value for each successfullly clashing",
                        "Set to 0 to disable.")
                .defineInRange("breakWeaponValueOnClash", 1, 0, 10000);
        CLASH_CHANCE = BUILDER.comment(
                        "Chance for clashing")
                .defineInRange("clashChance", 1.0, 0, 1);
        SHAKE_SCREEN = BUILDER.comment(
                        "Shake your screen on clashing")
                .define("shakeScreen", true);
        SPEC = BUILDER.build();
    }
}
