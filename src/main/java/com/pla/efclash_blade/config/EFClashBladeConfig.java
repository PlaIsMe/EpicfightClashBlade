package com.pla.efclash_blade.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class EFClashBladeConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACK_LIST;

    static {
        BLACK_LIST = BUILDER.comment(
                        "A list of entities that can't do Clash Blade",
                        "Input: [\"minecraft\"] will disable Clash Blade for all of entities from Minecraft",
                        "Input: [\"minecraft:zombie\"] will disable Clash Blade for zombie entity from Minecraft.")
                .defineList("blackList", List.of("nightfall_invade"), entry -> entry instanceof String);
        SPEC = BUILDER.build();
    }
}
