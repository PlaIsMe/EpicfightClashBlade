package com.pla.efclash_blade;
import com.mojang.logging.LogUtils;
import com.pla.efclash_blade.client.CameraEngine;
import com.pla.efclash_blade.config.EFClashBladeConfig;
import com.pla.efclash_blade.gameasset.EFClashBladeSkillCategories;
import com.pla.efclash_blade.gameasset.EFClashBladeSkillSlots;
import com.pla.efclash_blade.network.CPApplyShake;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.slf4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(EFClashBlade.MOD_ID)
public class EFClashBlade
{
    public static final String MOD_ID = "efclash_blade";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final SimpleChannel PACKET_HANDLER = NetworkRegistry
            .newSimpleChannel(ResourceLocation.fromNamespaceAndPath(EFClashBlade.MOD_ID, "main"), () -> "1", "1"::equals, "1"::equals);
    private static int messageID = 0;

    public EFClashBlade(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        context.registerConfig(ModConfig.Type.COMMON, EFClashBladeConfig.SPEC, "efclash_blade-server.toml");
        EFClashBladeSkillSlots.ENUM_MANAGER.registerEnumCls("efclash_blade", EFClashBladeSkillSlots.class);
        EFClashBladeSkillCategories.ENUM_MANAGER.registerEnumCls("efclash_blade", EFClashBladeSkillCategories.class);
        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(this::clientSetup);
        }
    }

    public static <T> void addNetworkMessage(Class<T> oclass, BiConsumer<T, FriendlyByteBuf> biconsumer, Function<FriendlyByteBuf, T> function, BiConsumer<T, Supplier<NetworkEvent.Context>> biconsumer1) {
        EFClashBlade.PACKET_HANDLER.registerMessage(EFClashBlade.messageID, oclass, biconsumer, function, biconsumer1);
        ++EFClashBlade.messageID;
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        new CameraEngine();
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class initer {
        @SubscribeEvent
        public static void init(FMLCommonSetupEvent fmlCommonSetupEvent) {
            EFClashBlade.addNetworkMessage(
                    CPApplyShake.class,
                    CPApplyShake::encode,
                    CPApplyShake::new,
                    CPApplyShake::handle
            );
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
        }
    }
}