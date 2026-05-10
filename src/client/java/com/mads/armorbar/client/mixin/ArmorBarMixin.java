package com.mads.armorbar.client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(Gui.class)
public abstract class ArmorBarMixin {

    @Shadow @Final private Minecraft minecraft;

    @Unique
    private static final Identifier BLACK_LINE = Identifier.fromNamespaceAndPath("armorbar", "textures/gui/black_line.png");

    @Unique
    private Identifier getTex(String type) {
        return Identifier.fromNamespaceAndPath("armorbar", "textures/gui/" + type + "_full.png");
    }

    @Unique
    private String getMat(ItemStack stack) {
        if (stack.isEmpty()) return "iron";
        String n = stack.getItem().toString().toLowerCase();

        if (n.contains("netherite")) return "netherite";
        if (n.contains("diamond")) return "diamond";
        if (n.contains("gold")) return "gold";
        if (n.contains("iron")) return "iron";
        if (n.contains("copper")) return "copper";
        if (n.contains("chainmail")) return "chainmail";
        if (n.contains("leather")) return "leather";
        if (n.contains("turtle")) return "turtle";

        return "iron";
    }

    @Unique
    private int getPoints(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        String n = stack.getItem().toString().toLowerCase();

        if (n.contains("diamond") || n.contains("netherite")) {
            if (n.contains("chestplate")) return 8;
            if (n.contains("leggings")) return 6;
            if (n.contains("helmet") || n.contains("boots")) return 3;
        }
        else if (n.contains("iron")) {
            if (n.contains("chestplate")) return 6;
            if (n.contains("leggings")) return 5;
            if (n.contains("helmet") || n.contains("boots")) return 2;
        }
        else if (n.contains("gold")) {
            if (n.contains("chestplate")) return 5;
            if (n.contains("leggings")) return 3;
            if (n.contains("helmet")) return 2;
            if (n.contains("boots")) return 1;
        }
        else if (n.contains("copper")) {
            if (n.contains("chestplate")) return 4;
            if (n.contains("leggings")) return 3;
            if (n.contains("helmet")) return 2;
            if (n.contains("boots")) return 1;
        }
        else if (n.contains("chainmail")) {
            if (n.contains("chestplate")) return 5;
            if (n.contains("leggings")) return 4;
            if (n.contains("helmet")) return 2;
            if (n.contains("boots")) return 1;
        }
        else if (n.contains("leather")) {
            if (n.contains("chestplate")) return 3;
            if (n.contains("leggings")) return 2;
            if (n.contains("helmet") || n.contains("boots")) return 1;
        }
        else if (n.contains("turtle")) {
            return 2;
        }

        return 0;
    }

    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    private void onRenderCustomArmor(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = this.minecraft.player;
        if (player == null || this.minecraft.options.hideGui || player.isCreative() || player.isSpectator()) return;

        int totalArmor = player.getArmorValue();
        if (totalArmor > 0) {
            int xStart = graphics.guiWidth() / 2 - 91;
            int yIcon = graphics.guiHeight() - 49;

            List<String> halves = new ArrayList<>();
            for (int i = 36; i <= 39; i++) {
                ItemStack stack = player.getInventory().getItem(i);
                int pts = getPoints(stack);
                String mat = getMat(stack);
                for (int j = 0; j < pts; j++) {
                    halves.add(mat);
                }
            }

            for (int i = 0; i < 10; i++) {
                int x = xStart + (i * 8);
                int leftIdx = i * 2;
                int rightIdx = i * 2 + 1;

                String leftMat = (leftIdx < halves.size()) ? halves.get(leftIdx) : null;
                String rightMat = (rightIdx < halves.size()) ? halves.get(rightIdx) : null;

                if (leftMat != null && rightMat != null) {
                    if (leftMat.equals(rightMat)) {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, getTex(leftMat), x, yIcon, 0f, 0f, 9, 9, 9, 9);
                    } else {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, getTex(leftMat), x, yIcon, 0f, 0f, 4, 9, 9, 9);
                        graphics.blit(RenderPipelines.GUI_TEXTURED, BLACK_LINE, x + 4, yIcon + 2, 0f, 0f, 1, 7, 1, 7);
                        graphics.blit(RenderPipelines.GUI_TEXTURED, getTex(rightMat), x + 5, yIcon, 5f, 0f, 4, 9, 9, 9);
                    }
                } else if (leftMat != null) {
                    graphics.blit(RenderPipelines.GUI_TEXTURED, getTex(leftMat), x, yIcon, 0f, 0f, 4, 9, 9, 9);
                    graphics.blit(RenderPipelines.GUI_TEXTURED, BLACK_LINE, x + 4, yIcon + 2, 0f, 0f, 1, 7, 1, 7);
                }
            }
        }
    }

    @Inject(method = "extractArmor", at = @At("HEAD"), cancellable = true, remap = false)
    private static void stopVanilla(CallbackInfo ci) {
        ci.cancel();
    }
}
