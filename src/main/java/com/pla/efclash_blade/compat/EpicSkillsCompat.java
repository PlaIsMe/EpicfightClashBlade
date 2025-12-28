package com.pla.efclash_blade.compat;

import com.pla.efclash_blade.client.EFClashBladeCategorySlotTextures;
import com.yesman.epicskills.client.gui.screen.CategorySlotTexture;

public class EpicSkillsCompat {

    public static void registerCategorySlotTexture() {
        CategorySlotTexture.ENUM_MANAGER.registerEnumCls("annoyingvillagers", EFClashBladeCategorySlotTextures.class);
    }
}