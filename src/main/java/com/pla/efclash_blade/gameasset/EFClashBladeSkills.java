package com.pla.efclash_blade.gameasset;

import com.pla.efclash_blade.EFClashBlade;
import com.pla.efclash_blade.skill.ClashBladeSkill;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.passive.PassiveSkill;

@Mod.EventBusSubscriber(modid = EFClashBlade.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EFClashBladeSkills {
    public static Skill CLASH_BLADE;

    @SubscribeEvent
    public static void buildSkillEvent(SkillBuildEvent skillbuildevent) {
        SkillBuildEvent.ModRegistryWorker modRegistry = skillbuildevent.createRegistryWorker(EFClashBlade.MOD_ID);
        EFClashBladeSkills.CLASH_BLADE = modRegistry.build("clash_blade", ClashBladeSkill::new, PassiveSkill.createPassiveBuilder());
    }
}
