package com.guessbuild.screen;

import com.guessbuild.screen.SettingsScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class InputCaptureScreen extends Screen {

    public InputCaptureScreen() {
        super(Component.literal("GTB Input"));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        OverlayRenderer.updateMouse(mouseX, mouseY);

        int btnW = 60;
        int btnH = 14;
        int btnX = this.width - btnW - 6;
        int btnY = this.height - btnH - 6;
        boolean hovered = mouseX >= btnX && mouseX <= btnX + btnW
            && mouseY >= btnY && mouseY <= btnY + btnH;
        int bg = hovered ? 0xFF00A884 : 0xCC0C0C14;
        int border = hovered ? 0xFF00D4AA : 0xFF008060;
        gfx.fill(btnX, btnY, btnX + btnW, btnY + btnH, bg);
        gfx.fill(btnX, btnY, btnX + btnW, btnY + 1, border);
        String label = "Settings";
        int lw = this.font.width(label);
        gfx.text(this.font, label, btnX + (btnW - lw) / 2, btnY + 3, 0xFFFFFFFF);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        OverlayRenderer.onMouseScroll((int) verticalAmount);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int btnW = 60;
        int btnH = 14;
        int btnX = this.width - btnW - 6;
        int btnY = this.height - btnH - 6;
        if (event.x() >= btnX && event.x() <= btnX + btnW
            && event.y() >= btnY && event.y() <= btnY + btnH) {
            minecraft.setScreen(new SettingsScreen());
            return true;
        }
        OverlayRenderer.onMouseClick((int) event.x(), (int) event.y());
        return true;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        OverlayRenderer.setVisible(false);
        return true;
    }
}
