package com.pla.efclash_blade.client;

import com.yesman.epicskills.client.gui.screen.CategorySlotTexture;

public enum EFClashBladeCategorySlotTextures implements CategorySlotTexture {

    CLASH_BLADE(6, 6, 44, 44);

    private final int offsetX;
    private final int offsetY;
    private final int texWidth;
    private final int texHeight;
    private final int universalOrder;

    EFClashBladeCategorySlotTextures(int i, int j, int k, int l) {
        this.offsetX = i;
        this.offsetY = j;
        this.texWidth = k;
        this.texHeight = l;
        this.universalOrder = CategorySlotTexture.ENUM_MANAGER.assign(this);
    }

    public int offsetX() {
        return this.offsetX;
    }

    public int offsetY() {
        return this.offsetY;
    }

    public int texWidth() {
        return this.texWidth;
    }

    public int texHeight() {
        return this.texHeight;
    }

    public int universalOrdinal() {
        return this.universalOrder;
    }
}
