package com.pla.efclash_blade.event;

import com.pla.efclash_blade.config.EFClashBladeConfig;
import com.pla.efclash_blade.util.ScreenShakeUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
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
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;

import java.util.List;
import java.util.Locale;
import java.util.Random;

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

    // clashBy value
    // 0: regular clash, clashing in front of mob while swinging attack animation
    // 1: special clash, clashing in front of mob without swinging attack animation, can clash against any type of damageSource
    // 2: force clash,clash without in front of mob, against any type of damage source
    private static void clashBlade(LivingAttackEvent livingAttackEvent, LivingEntityPatch<?> defenderLivingEntityPatch,
                                   AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation,
                                   EntityState defenderEntityState, Entity attackerEntity, Entity defenderEntity, ServerLevel serverLevel, int clashBy) {
        if (new Random().nextFloat() > EFClashBladeConfig.CLASH_CHANCE.get()) return;
        customPreAdditionClashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity, clashBy);
        livingAttackEvent.setCanceled(true);
        if (conditionToPlayClashSound(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity)) {
            defenderLivingEntityPatch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
        }

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
        if (EFClashBladeConfig.SHAKE_SCREEN.get() && attackerEntity instanceof Player player) {
            ScreenShakeUtil.applyScreenShake(serverLevel, player.getOnPos().getCenter(), 1.0, 20, 4);
        }
        customPostAdditionClashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity, clashBy);
    }

    private static boolean customAdditionClashBladeLogic(LivingAttackEvent livingAttackEvent,
                                                         LivingEntityPatch<?> defenderLivingEntityPatch,
                                                         AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation,
                                                         EntityState defenderEntityState, Entity attacker, Entity defender) {
        return false;
    }

    private static boolean forceClashBlade(LivingAttackEvent livingAttackEvent,
                                                         LivingEntityPatch<?> defenderLivingEntityPatch,
                                                         AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation,
                                                         EntityState defenderEntityState, Entity attacker, Entity defender) {
        return false;
    }

    private static boolean blacklistClashBladeAnimation(LivingAttackEvent livingAttackEvent,
                                                         LivingEntityPatch<?> defenderLivingEntityPatch,
                                                         AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation,
                                                         EntityState defenderEntityState, Entity attacker, Entity defender) {
        return true;
    }

    private static void customPreAdditionClashBlade(LivingAttackEvent livingAttackEvent,
                                                    LivingEntityPatch<?> defenderLivingEntityPatch,
                                                    AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation,
                                                    EntityState defenderEntityState, Entity attacker, Entity defender, int clashBy) {
    }

    private static void customPostAdditionClashBlade(LivingAttackEvent livingAttackEvent,
                                                     LivingEntityPatch<?> defenderLivingEntityPatch,
                                                     AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation,
                                                     EntityState defenderEntityState, Entity attacker, Entity defender, int clashBy) {
    }

    private static boolean conditionToPlayClashSound(LivingAttackEvent livingAttackEvent,
                                                     LivingEntityPatch<?> defenderLivingEntityPatch,
                                                     AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation,
                                                     EntityState defenderEntityState, Entity attacker, Entity defender) {
        return true;
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
        if (defenderLivingEntityPatch == null) {
            return;
        }

        AnimationPlayer defenderAnimationPlayer = defenderLivingEntityPatch.getAnimator().getPlayerFor(null);
        if (defenderAnimationPlayer == null) {
            return;
        }

        AssetAccessor<? extends StaticAnimation> defenderDynamicAnimation = defenderAnimationPlayer.getRealAnimation();

        float defenderElapsedTimeFloat = defenderAnimationPlayer.getElapsedTime();
        EntityState defenderEntityState = defenderDynamicAnimation.get().getState(defenderLivingEntityPatch, defenderElapsedTimeFloat);

        if (forceClashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity)) {
            clashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity, serverLevel, 2);
            return;
        }

        Vec3 entityPosition = attackerEntity.position();
        Vec3 entityViewVector = defenderEntity.getViewVector(1.0F);
        Vec3 entitySubtract = entityPosition.subtract(defenderEntity.getEyePosition()).normalize();

        if (entitySubtract.dot(entityViewVector) > 0.0D) {
            if (defenderDynamicAnimation.get() instanceof AttackAnimation
                    && defenderEntityState.getLevel() < 3
                    && blacklistClashBladeAnimation(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity)
                    && defenderLivingEntityPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() != WeaponCategories.FIST
                    && !damageSource.is(DamageTypes.MAGIC)
                    && !damageSource.is(DamageTypeTags.IS_EXPLOSION)
                    && !damageSource.is(DamageTypes.ON_FIRE)
                    && !damageSource.is(DamageTypes.IN_FIRE)
                    && !damageSource.is(DamageTypes.FALL)) {
                clashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity, serverLevel, 0);
            } else if (customAdditionClashBladeLogic(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity)) {
                clashBlade(livingAttackEvent, defenderLivingEntityPatch, defenderDynamicAnimation, defenderEntityState, attackerEntity, defenderEntity, serverLevel, 1);
            }
        }
    }
}
