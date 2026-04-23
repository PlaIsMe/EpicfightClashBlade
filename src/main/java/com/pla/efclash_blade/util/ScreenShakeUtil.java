package com.pla.efclash_blade.util;

import com.pla.efclash_blade.EFClashBlade;
import com.pla.efclash_blade.network.CPApplyShake;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

public class ScreenShakeUtil {
    public static void applyScreenShake(ServerLevel level, Vec3 center, double range,
                                        int durationTicks, int amplifier) {
        double rangeSq = range * range;
        AABB box = new AABB(center, center).inflate(range);

        for (Player player : level.getEntitiesOfClass(Player.class, box,
                player -> player.isAlive() && !player.isSpectator())) {

            double distSq = player.distanceToSqr(center.x, center.y, center.z);
            if (distSq > rangeSq) continue;

            EFClashBlade.PACKET_HANDLER.send(
                    PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new CPApplyShake(durationTicks, amplifier, (float) durationTicks / 10, 1)
            );
        }
    }
}
