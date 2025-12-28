package com.pla.efclash_blade.gameasset;

import yesman.epicfight.skill.SkillCategory;
import yesman.epicfight.skill.SkillSlot;

public enum EFClashBladeSkillSlots implements SkillSlot {

    CLASH_BLADE(EFClashBladeSkillCategories.CLASH_BLADE);

    final EFClashBladeSkillCategories category;
    final int id;

    EFClashBladeSkillSlots(EFClashBladeSkillCategories avSkillCategories) {
        this.category = avSkillCategories;
        this.id = SkillSlot.ENUM_MANAGER.assign(this);
    }

    public SkillCategory category() {
        return this.category;
    }

    public int universalOrdinal() {
        return this.id;
    }
}
