package com.pla.efclash_blade.event;

import com.pla.efclash_blade.config.EFClashBladeConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.ForgeRegistries;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;

import java.util.List;
import java.util.Locale;

@EventBusSubscriber
public class MobClashBladeEvent {
    public static boolean isBlacklisted(Entity entity) {
        ResourceLocation resourceLocation = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        if (resourceLocation == null) return false;
        String fullId = resourceLocation.toString();
        String modId = resourceLocation.getNamespace();

        List<? extends String> list = EFClashBladeConfig.BLACK_LIST.get();
        if (list == null || list.isEmpty()) return false;

        for (String item : list) {
            if (item == null) continue;
            String stringItem = item.trim().toLowerCase(Locale.ROOT);
            if (stringItem.isEmpty()) continue;

            if (stringItem.indexOf(':') == -1) {
                if (stringItem.equals(modId)) return true;
            } else {
                if (stringItem.equals(fullId)) return true;
            }
        }
        return false;
    }

    private static void clashBlade(LivingAttackEvent livingAttackEvent, LivingEntityPatch<?> defenderLivingEntityPatch,
                                   AssetAccessor<? extends DynamicAnimation> defenderDynamicAnimation,
                                   EntityState defenderEntityState, Entity attackerEntity, Entity defenderEntity, ServerLevel serverLevel) {
        customPreAdditionClashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity);
        livingAttackEvent.setCanceled(true);
        defenderLivingEntityPatch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);

        attackerEntity.setDeltaMovement(new Vec3(
                attackerEntity.getLookAngle().x * -0.2D,
                0.0D,
                attackerEntity.getLookAngle().z * -0.2D
        ));

        defenderEntity.setDeltaMovement(new Vec3(
                defenderEntity.getLookAngle().x * -0.2D,
                0.0D,
                defenderEntity.getLookAngle().z * -0.2D
        ));

        EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(
                serverLevel,
                HitParticleType.FRONT_OF_EYES,
                HitParticleType.ZERO,
                defenderEntity,
                attackerEntity
        );
        customPostAdditionClashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity);
    }

    private static boolean customAdditionClashBladeLogic(LivingAttackEvent livingAttackEvent,
                                                         LivingEntityPatch<?> defenderLivingEntityPatch,
                                                         AssetAccessor<? extends DynamicAnimation> defenderDynamicAnimation,
                                                         EntityState defenderEntityState, Entity attacker, Entity defender) {
        return false;
    }

    private static boolean blacklistClashBladeAnimation(LivingAttackEvent livingAttackEvent,
                                                         LivingEntityPatch<?> defenderLivingEntityPatch,
                                                         AssetAccessor<? extends DynamicAnimation> defenderDynamicAnimation,
                                                         EntityState defenderEntityState, Entity attacker, Entity defender) {
        return true;
    }

    private static void customPreAdditionClashBlade(LivingAttackEvent livingAttackEvent,
                                                    LivingEntityPatch<?> defenderLivingEntityPatch,
                                                    AssetAccessor<? extends DynamicAnimation> defenderDynamicAnimation,
                                                    EntityState defenderEntityState, Entity attacker, Entity defender) {
    }

    private static void customPostAdditionClashBlade(LivingAttackEvent livingAttackEvent,
                                                     LivingEntityPatch<?> defenderLivingEntityPatch,
                                                     AssetAccessor<? extends DynamicAnimation> defenderDynamicAnimation,
                                                     EntityState defenderEntityState, Entity attacker, Entity defender) {
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent livingAttackEvent) {
        Entity defenderEntity = livingAttackEvent.getEntity();
        DamageSource damageSource = livingAttackEvent.getSource();
        Entity attackerEntity = damageSource.getEntity();
        Level level = defenderEntity.level();

        if (!(level instanceof ServerLevel serverLevel)
                || defenderEntity instanceof Player
                || attackerEntity == null
                || isBlacklisted(defenderEntity)) {
            return;
        }

        LivingEntityPatch<?> defenderLivingEntityPatch = EpicFightCapabilities.getEntityPatch(defenderEntity, LivingEntityPatch.class);

        if (defenderLivingEntityPatch == null
                || defenderLivingEntityPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() == WeaponCategories.FIST
                || damageSource.is(DamageTypes.MAGIC)
                || damageSource.is(DamageTypes.EXPLOSION)
                || damageSource.is(DamageTypes.ON_FIRE)
                || damageSource.is(DamageTypes.IN_FIRE)
                || damageSource.is(DamageTypes.FALL)) {
            return;
        }

        AnimationPlayer defenderAnimationPlayer = defenderLivingEntityPatch.getAnimator().getPlayerFor(null);
        if (defenderAnimationPlayer == null) {
            return;
        }

        AssetAccessor<? extends DynamicAnimation> defenderDynamicAnimation = defenderAnimationPlayer.getAnimation();

        float defenderElapsedTimeFloat = defenderAnimationPlayer.getElapsedTime();
        EntityState defenderEntityState = defenderDynamicAnimation.get().getState(defenderLivingEntityPatch, defenderElapsedTimeFloat);

        Vec3 entityPosition = attackerEntity.position();
        Vec3 entityViewVector = defenderEntity.getViewVector(1.0F);
        Vec3 entitySubtract = entityPosition.subtract(defenderEntity.getEyePosition()).normalize();

        if (entitySubtract.dot(entityViewVector) > 0.0D) {
            if ((defenderDynamicAnimation.get() instanceof AttackAnimation
                    && defenderEntityState.getLevel() < 3
                    && blacklistClashBladeAnimation(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity))
                    || customAdditionClashBladeLogic(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity)) {
                clashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity, serverLevel);
            }
        }
    }
}
