package com.guessbuild.screen;

import com.guessbuild.GTBConfig;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class SettingsScreen extends Screen {

    private GTBConfig cfg;

    public SettingsScreen() {
        super(Component.literal("GTB Settings"));
    }

    @Override
    protected void init() {
        this.cfg = GTBConfig.get();
        this.clearWidgets();

        int colW = Math.min(200, this.width / 2 - 16);
        int leftX = this.width / 2 - colW - 4;
        int rightX = this.width / 2 + 4;
        int y = 40;
        int spacing = 26;

        this.addRenderableWidget(CycleButton.onOffBuilder(cfg.instantSend)
            .create(leftX, y, colW, 20,
                Component.literal("Instant Send"),
                (btn, val) -> { cfg.instantSend = val; cfg.save(); }));

        this.addRenderableWidget(CycleButton.onOffBuilder(cfg.pasteInChat)
            .create(rightX, y, colW, 20,
                Component.literal("Paste In Chat"),
                (btn, val) -> { cfg.pasteInChat = val; cfg.save(); }));

        y += spacing;

        this.addRenderableWidget(CycleButton.onOffBuilder(cfg.autoSend)
            .create(leftX, y, colW, 20,
                Component.literal("Auto Send"),
                (btn, val) -> { cfg.autoSend = val; cfg.save(); }));

        this.addRenderableWidget(new AutoSendDelaySlider(
            rightX, y, colW, 20,
            Component.literal("Auto Send Delay: " + cfg.autoSendDelay)));

        y += spacing;

        this.addRenderableWidget(CycleButton.onOffBuilder(cfg.showHints)
            .create(leftX, y, colW, 20,
                Component.literal("Show Hints"),
                (btn, val) -> { cfg.showHints = val; cfg.save(); }));

        this.addRenderableWidget(CycleButton.onOffBuilder(cfg.overlayAnimations)
            .create(rightX, y, colW, 20,
                Component.literal("Overlay Animations"),
                (btn, val) -> { cfg.overlayAnimations = val; cfg.save(); }));

        y += spacing;

        this.addRenderableWidget(new OverlayOpacitySlider(
            leftX, y, colW, 20,
            Component.literal("Overlay Opacity: " + (int)(cfg.overlayOpacity * 100) + "%")));

        this.addRenderableWidget(CycleButton.onOffBuilder(cfg.soundEnabled)
            .create(leftX, y, colW, 20,
                Component.literal("Sound Effects"),
                (btn, val) -> { cfg.soundEnabled = val; cfg.save(); }));

        this.addRenderableWidget(CycleButton.onOffBuilder(cfg.legacyOverlay)
            .create(rightX, y, colW, 20,
                Component.literal("Legacy Overlay"),
                (btn, val) -> { cfg.legacyOverlay = val; cfg.save(); }));

        y += spacing + 8;

        int resetW = 100;
        this.addRenderableWidget(Button.builder(
                Component.literal("Reset All"),
                btn -> { resetDefaults(); })
            .pos(this.width / 2 - resetW / 2, y)
            .width(resetW)
            .build());

        y += 32;

        int doneW = 100;
        this.addRenderableWidget(Button.builder(
                Component.literal("Done"),
                btn -> this.onClose())
            .pos(this.width / 2 - doneW / 2, y)
            .width(doneW)
            .build());
    }

    private void resetDefaults() {
        cfg.instantSend = true;
        cfg.pasteInChat = false;
        cfg.autoSend = true;
        cfg.autoSendDelay = 40;
        cfg.showHints = true;
        cfg.overlayAnimations = true;
        cfg.overlayOpacity = 1.0f;
        cfg.soundEnabled = true;
        cfg.legacyOverlay = false;
        cfg.save();
        this.init();
    }

    @Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        super.extractRenderState(gfx, mouseX, mouseY, delta);

        String title = "Guess The Build Settings";
        int tw = this.font.width(title);
        gfx.text(this.font, title, this.width / 2 - tw / 2, 14, 0xFFFFFF);

        String sub = "by Syntax";
        int sw = this.font.width(sub);
        gfx.text(this.font, sub, this.width / 2 - sw / 2, 14 + this.font.lineHeight + 2, 0x808080);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private class AutoSendDelaySlider extends AbstractSliderButton {

        AutoSendDelaySlider(int x, int y, int w, int h, Component message) {
            super(x, y, w, h, message, cfg.autoSendDelay / 120.0);
        }

        @Override
        protected void applyValue() {
            cfg.autoSendDelay = Math.max(5, Math.min(120, (int) (this.value * 120)));
            cfg.save();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Auto Send Delay: " + Math.max(5, Math.min(120, (int) (this.value * 120)))));
        }
    }

    private class OverlayOpacitySlider extends AbstractSliderButton {

        OverlayOpacitySlider(int x, int y, int w, int h, Component message) {
            super(x, y, w, h, message, cfg.overlayOpacity);
        }

        @Override
        protected void applyValue() {
            cfg.overlayOpacity = (float) Math.max(0.1, Math.min(1.0, this.value));
            cfg.save();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Overlay Opacity: " + (int) (Math.max(0.1, Math.min(1.0, this.value)) * 100) + "%"));
        }
    }
}
