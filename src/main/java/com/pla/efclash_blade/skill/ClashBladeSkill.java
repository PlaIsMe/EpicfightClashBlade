package com.pla.efclash_blade.skill;

import com.pla.efclash_blade.config.EFClashBladeConfig;
import com.pla.efclash_blade.util.ScreenShakeUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.EntityState;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.utils.AttackResult.ResultType;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.particle.EpicFightParticles;
import yesman.epicfight.particle.HitParticleType;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.passive.PassiveSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.entity.eventlistener.PlayerEventListener.EventType;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public class ClashBladeSkill extends PassiveSkill {
    private static final UUID EVENT_UUID = UUID.fromString("a6f630fd-45e1-4d3d-9542-2c91a8cf9833");

    public ClashBladeSkill(SkillBuilder<? extends PassiveSkill> skillBuilder) {
        super(skillBuilder);
    }

    private static boolean blacklistClashBladeAnimation(AssetAccessor<? extends StaticAnimation> dynamicAnimation,
                                                        EntityState entityState, ServerPlayer serverPlayer) {
        return true;
    }

    private static int getWeaponDestroyValueOnClash(AssetAccessor<? extends StaticAnimation> dynamicAnimation, DamageSource damageSource, PlayerPatch<?> playerPatch, ServerLevel serverLevel) {
        return EFClashBladeConfig.BREAK_WEAPON_VALUE_ON_CLASH.get();
    }

    private static void moreLogicAfterClashing(AssetAccessor<? extends StaticAnimation> dynamicAnimation, DamageSource damageSource, PlayerPatch<?> playerPatch, ServerLevel serverLevel) {
        return;
    }

    @Override
    public void onInitiate(SkillContainer skillcontainer) {
        super.onInitiate(skillcontainer);
        skillcontainer.getDataManager();
        skillcontainer.getExecutor().getEventListener().addEventListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, ClashBladeSkill.EVENT_UUID, (pre) -> {
            PlayerPatch<?> playerPatch = pre.getPlayerPatch();
            ServerPlayer serverPlayer = pre.getPlayerPatch().getOriginal();
            DamageSource damageSource = pre.getDamageSource();

            AnimationPlayer animationPlayer =
                    Objects.requireNonNull(playerPatch.getAnimator().getPlayerFor(null));
            AssetAccessor<? extends StaticAnimation> dynamicAnimation = animationPlayer.getRealAnimation();

            float elapsedTimeFloat = animationPlayer.getElapsedTime();
            EntityState entityState = dynamicAnimation.get().getState(playerPatch, elapsedTimeFloat);

            if ((playerPatch.getHoldingItemCapability(InteractionHand.MAIN_HAND).getWeaponCategory() != WeaponCategories.FIST)
                    && !damageSource.is(DamageTypes.MAGIC) && !damageSource.is(DamageTypeTags.IS_EXPLOSION)
                    && !damageSource.is(DamageTypes.ON_FIRE) && !damageSource.is(DamageTypes.IN_FIRE)
                    && !damageSource.is(DamageTypes.FALL) && dynamicAnimation.get() instanceof AttackAnimation
                    && entityState.getLevel() < 3 && blacklistClashBladeAnimation(dynamicAnimation, entityState, serverPlayer)) {
                Entity entity = damageSource.getEntity();

                if (entity != null) {
                    Vec3 entityPosition = entity.position();
                    Vec3 entityViewVector = pre.getPlayerPatch().getOriginal().getViewVector(1.0F);
                    Vec3 entitySubtract = entityPosition.subtract(pre.getPlayerPatch().getOriginal().getEyePosition()).normalize();

                    if (entitySubtract.dot(entityViewVector) > 0.0D) {
                        if (new Random().nextFloat() > EFClashBladeConfig.CLASH_CHANCE.get()) return;
                        pre.setCanceled(true);
                        pre.setResult(ResultType.BLOCKED);
                        playerPatch.playSound(EpicFightSounds.CLASH.get(), -0.05F, 0.1F);
                        entity.setDeltaMovement(new Vec3(entity.getLookAngle().x * -0.2D, 0.0D, entity.getLookAngle().z * -0.2D));

                        serverPlayer.setDeltaMovement(new Vec3(serverPlayer.getLookAngle().x * -0.2D, 0.0D, serverPlayer.getLookAngle().z * -0.2D));
                        if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                            EpicFightParticles.HIT_BLUNT.get().spawnParticleWithArgument(serverLevel, HitParticleType.FRONT_OF_EYES, HitParticleType.ZERO, serverPlayer, damageSource.getEntity());
                            boolean damaged = false;
                            if ((serverPlayer.getOffhandItem().getItem() instanceof SwordItem
                                    || serverPlayer.getOffhandItem().getItem() instanceof AxeItem)
                                    && new Random().nextBoolean()) {
                                damaged = true;
                                serverPlayer.getOffhandItem().hurtAndBreak(getWeaponDestroyValueOnClash(dynamicAnimation, damageSource, playerPatch, serverLevel), serverPlayer, (player) -> {
                                    player.broadcastBreakEvent(InteractionHand.OFF_HAND);
                                });
                            }
                            if (!damaged) {
                                serverPlayer.getMainHandItem().hurtAndBreak(getWeaponDestroyValueOnClash(dynamicAnimation, damageSource, playerPatch, serverLevel), serverPlayer, (player) -> {
                                    player.broadcastBreakEvent(InteractionHand.MAIN_HAND);
                                });
                            }
                            if (EFClashBladeConfig.SHAKE_SCREEN.get()) {
                                ScreenShakeUtil.applyScreenShake(serverLevel, playerPatch.getOriginal().getOnPos().getCenter(), 1.0, 20, 4);
                            }
                            moreLogicAfterClashing(dynamicAnimation, damageSource, playerPatch, serverLevel);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onRemoved(SkillContainer skillcontainer) {
        super.onRemoved(skillcontainer);
        skillcontainer.getExecutor().getEventListener().removeListener(EventType.TAKE_DAMAGE_EVENT_ATTACK, ClashBladeSkill.EVENT_UUID);
    }
}