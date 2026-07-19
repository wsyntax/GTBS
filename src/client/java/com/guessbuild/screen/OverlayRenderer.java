package com.guessbuild.screen;

import com.guessbuild.GTBConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class OverlayRenderer {

    private static final Identifier ID = Identifier.fromNamespaceAndPath("guessbuild", "overlay");

    private static final int ACCENT = 0xFF00D4AA;
    private static final int ACCENT_DIM = 0xFF00A884;
    private static final int ACCENT_GLOW = 0xFF00FFBB;
    private static final int BG_PRIMARY = 0xE6101018;
    private static final int BG_SECONDARY = 0xCC0C0C14;
    private static final int BG_HOVER = 0x4000D4AA;
    private static final int BG_ROW_EVEN = 0x18FFFFFF;
    private static final int BG_ROW_ODD = 0x0CFFFFFF;
    private static final int TEXT_PRIMARY = 0xFFFFFFFF;
    private static final int TEXT_SECONDARY = 0xFFB0B0B0;
    private static final int TEXT_MUTED = 0xFF707070;
    private static final int TEXT_GREEN = 0xFF00FF88;
    private static final int TEXT_YELLOW = 0xFFFFCC00;

    private static boolean visible = false;
    private static String hint = "";
    private static List<String> words = new ArrayList<>();
    private static float scrollOffset = 0;
    private static float targetScroll = 0;
    private static int contentHeight = 0;

    private static float animProgress = 0f;
    private static boolean animatingIn = false;

    private static float toastAlpha = 0f;
    private static String toastMessage = "";
    private static int toastTicks = 0;
    private static float toastSlideY = -20f;

    private static int lastMouseX = -1;
    private static int lastMouseY = -1;
    private static int hoveredIndex = -1;

    private static int globalTick = 0;
    private static float[] entryAnimProgress;
    private static float panelWidthAnim = 0f;
    private static float titleGlow = 0f;

    public static void init() {
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.OVERLAY_MESSAGE,
            ID,
            OverlayRenderer::render
        );
        ClientTickEvents.END_CLIENT_TICK.register(OverlayRenderer::onTick);
    }

    private static void onTick(Minecraft mc) {
        GTBConfig cfg = GTBConfig.get();
        boolean anims = cfg.overlayAnimations;

        globalTick++;

        if (visible) {
            float speed = anims ? 0.18f : 1.0f;
            animProgress = Math.min(1f, animProgress + speed);

            panelWidthAnim = Math.min(1f, panelWidthAnim + (anims ? 0.12f : 0.5f));

            if (entryAnimProgress != null) {
                int count = entryAnimProgress.length;
                float maxDelay = count > 1 ? 0.6f : 0f;
                for (int i = 0; i < count; i++) {
                    float delay = count > 1 ? (float) i / (count - 1) * maxDelay : 0f;
                    float t = Math.max(0, animProgress - delay);
                    entryAnimProgress[i] = Math.min(1f, t * 3f);
                }
            }

            titleGlow = (float)(0.6f + 0.4f * Math.sin(globalTick * 0.06));

            if (toastTicks > 0) {
                toastTicks--;
                toastAlpha = Math.min(1f, toastAlpha + 0.12f);
                toastSlideY += (0f - toastSlideY) * 0.15f;
                if (toastTicks == 0) toastAlpha = 0f;
            } else {
                toastAlpha = Math.max(0f, toastAlpha - 0.08f);
                toastSlideY += (-20f - toastSlideY) * 0.1f;
            }
        } else {
            float speed = anims ? 0.22f : 1.0f;
            animProgress = Math.max(0f, animProgress - speed);
            panelWidthAnim = Math.max(0f, panelWidthAnim - speed);
            toastAlpha = Math.max(0f, toastAlpha - 0.1f);
            titleGlow *= 0.9f;
        }

        scrollOffset += (targetScroll - scrollOffset) * (anims ? 0.25f : 1.0f);
        if (Math.abs(scrollOffset - targetScroll) < 0.5f) scrollOffset = targetScroll;
    }

    public static void setVisible(boolean v) { visible = v; }
    public static boolean isVisible() { return visible; }

    public static void show(String newHint, List<String> newWords) {
        hint = newHint;
        words = new ArrayList<>(newWords);
        scrollOffset = 0;
        targetScroll = 0;
        animProgress = 0f;
        panelWidthAnim = 0f;
        animatingIn = true;
        hoveredIndex = -1;
        entryAnimProgress = new float[words.size()];
        for (int i = 0; i < entryAnimProgress.length; i++) entryAnimProgress[i] = 0f;
        visible = true;
    }

    public static void showToast(String message) {
        toastMessage = message;
        toastTicks = 80;
        toastAlpha = 0f;
        toastSlideY = -20f;
    }

    public static void updateMouse(int mouseX, int mouseY) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public static void onMouseScroll(int direction) {
        if (!visible) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;
        GTBConfig cfg = GTBConfig.get();
        Font font = mc.font;
        int lh = font.lineHeight + 2;
        int screenH = mc.getWindow().getGuiScaledHeight();
        int viewH;
        if (cfg.legacyOverlay) {
            int headerH = lh * 3 + 8;
            viewH = Math.min(screenH - headerH - 8, 160);
            viewH = Math.min(viewH, words.isEmpty() ? lh * 2 : Math.min(words.size() * lh, viewH));
        } else {
            viewH = Math.min(screenH / 2, 160);
        }
        contentHeight = words.size() * lh;
        int maxScroll = Math.max(0, contentHeight - viewH);
        targetScroll = Math.max(0, Math.min(maxScroll, targetScroll - (int)(direction * 16)));
    }

    public static void onMouseClick(int mouseX, int mouseY) {
        if (!visible) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || words.isEmpty()) return;
        GTBConfig cfg = GTBConfig.get();
        Font font = mc.font;
        int lh = font.lineHeight + 2;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int panelW, panelX, listTop, viewH;
        if (cfg.legacyOverlay) {
            panelW = Math.min(screenW - 16, 220);
            panelX = (screenW - panelW) / 2;
            int headerH = lh * 3 + 8;
            viewH = Math.min(screenH - headerH - 8, 160);
            viewH = Math.min(viewH, words.isEmpty() ? lh * 2 : Math.min(words.size() * lh, viewH));
            int panelH = headerH + viewH + 4;
            int panelY = (screenH - panelH) / 2;
            if (panelY < 4) panelY = 4;
            listTop = panelY + headerH;
        } else {
            panelW = Math.min(screenW - 8, 240);
            panelX = (screenW - panelW) / 2;
            int panelY = screenH / 4;
            viewH = Math.min(screenH / 2, 160);
            listTop = panelY + lh * 3 + 10;
        }

        int y = listTop - (int) scrollOffset;
        for (int i = 0; i < words.size(); i++) {
            if (mouseX >= panelX && mouseX <= panelX + panelW
                && mouseY >= y - 1 && mouseY <= y + font.lineHeight + 2
                && mouseY >= listTop && mouseY <= listTop + viewH) {
                com.guessbuild.GuessTheBuild.sendOrPaste(words.get(i));
                return;
            }
            y += lh;
        }
    }

    private static void render(GuiGraphicsExtractor gfx, DeltaTracker delta) {
        if (animProgress <= 0f && !visible) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;

        GTBConfig cfg = GTBConfig.get();
        if (cfg.legacyOverlay) {
            renderLegacy(gfx, mc, cfg);
        } else {
            renderModern(gfx, mc, cfg);
        }
    }

    private static void renderLegacy(GuiGraphicsExtractor gfx, Minecraft mc, GTBConfig cfg) {
        Font font = mc.font;
        int sW = mc.getWindow().getGuiScaledWidth();
        int sH = mc.getWindow().getGuiScaledHeight();
        int lh = font.lineHeight + 2;
        int alpha = (int)(animProgress * 255);

        int panelW = Math.min(sW - 16, 220);
        int panelX = (sW - panelW) / 2;

        int headerH = lh * 3 + 8;
        int maxListH = Math.min(sH - headerH - 8, 160);
        int listH = Math.min(maxListH, words.isEmpty() ? lh * 2 : Math.min(words.size() * lh, maxListH));
        int panelH = headerH + listH + 4;

        int panelY = (sH - panelH) / 2;
        if (panelY < 4) panelY = 4;

        int listTop = panelY + headerH;
        int listBottom = listTop + listH;

        contentHeight = words.size() * lh;
        int cx = panelX + panelW / 2;

        int bgColor = withAlpha(0xC0101010, alpha);
        int borderColor = withAlpha(0xFF555555, alpha);

        gfx.fill(panelX - 1, panelY - 1, panelX + panelW + 1, listBottom + 1, borderColor);
        gfx.fill(panelX, panelY, panelX + panelW, listBottom, bgColor);

        gfx.nextStratum();

        int titleY = panelY + 4;
        if (cfg.showHints) {
            drawText(gfx, font, "GTB", cx, titleY, withAlpha(0xFFFFFF, alpha));
            String hintDisplay = hint.isEmpty() ? "waiting..." : formatHint(hint);
            String hintText = hintDisplay;
            if (font.width(hintText) > panelW - 12) hintText = truncate(hintText, panelW - 12, font);
            drawText(gfx, font, hintText, cx, titleY + lh, withAlpha(0xFFFF55, alpha));
        }

        String countText = words.size() + " word" + (words.size() != 1 ? "s" : "");
        drawText(gfx, font, countText, cx, titleY + lh * 2 + (cfg.showHints ? 0 : -lh), withAlpha(0xAAAAAA, alpha));

        if (words.isEmpty()) {
            drawText(gfx, font, "No matches", cx, listTop + listH / 2, withAlpha(0xFF5555, alpha));
        } else if (listH > 0) {
            gfx.enableScissor(panelX, listTop, panelX + panelW, listBottom);

            hoveredIndex = -1;
            if (lastMouseX >= 0 && lastMouseY >= 0) {
                int hy = listTop - (int) scrollOffset;
                for (int i = 0; i < words.size(); i++) {
                    if (lastMouseX >= panelX && lastMouseX <= panelX + panelW
                        && lastMouseY >= hy && lastMouseY <= hy + font.lineHeight + 2
                        && lastMouseY >= listTop && lastMouseY <= listBottom) {
                        hoveredIndex = i;
                        break;
                    }
                    hy += lh;
                }
            }

            int y = listTop - (int) scrollOffset;
            for (int i = 0; i < words.size(); i++) {
                if (y + lh >= listTop - lh && y <= listBottom) {
                    String word = words.get(i);
                    if (font.width(word) > panelW - 12) word = truncate(word, panelW - 12, font);

                    boolean hovered = (i == hoveredIndex);
                    boolean single = words.size() == 1;

                    if (hovered) {
                        gfx.fill(panelX + 2, y - 1, panelX + panelW - 2, y + font.lineHeight + 2, withAlpha(0x403366CC, alpha));
                    } else if (single) {
                        gfx.fill(panelX + 2, y - 1, panelX + panelW - 2, y + font.lineHeight + 2, withAlpha(0x4055FF55, alpha));
                    }

                    int textColor;
                    if (single) {
                        textColor = withAlpha(0x55FF55, alpha);
                    } else if (hovered) {
                        textColor = withAlpha(0xFFFF55, alpha);
                    } else {
                        textColor = withAlpha(0xFFFFFF, alpha);
                    }
                    int wordWidth = font.width(word);
                    int wordX = panelX + (panelW - wordWidth) / 2;
                    gfx.text(font, word, wordX, y + 1, textColor);
                }
                y += lh;
            }
            gfx.disableScissor();
        }

        if (contentHeight > listH && listH > 0) {
            int barH = Math.max(8, (int)((float) listH * listH / contentHeight));
            int maxScroll = contentHeight - listH;
            if (maxScroll > 0) {
                int barY = listTop + (int)((float)(listH - barH) * scrollOffset / maxScroll);
                gfx.fill(panelX + panelW - 3, barY, panelX + panelW - 1, barY + barH, withAlpha(0x80FFFFFF, alpha));
            }
        }

        if (hoveredIndex >= 0 && hoveredIndex < words.size()) {
            String tooltip = "Click: " + words.get(hoveredIndex);
            int tw = font.width(tooltip) + 8;
            int th = font.lineHeight + 4;
            int tx = (sW - tw) / 2;
            int ty = listBottom + 4;
            if (ty + th > sH - 2) ty = panelY - th - 4;
            if (ty < 2) ty = 2;
            gfx.fill(tx - 1, ty - 1, tx + tw + 1, ty + th + 1, withAlpha(0xFF000000, alpha));
            gfx.fill(tx, ty, tx + tw, ty + th, withAlpha(0xE0101010, alpha));
            gfx.nextStratum();
            gfx.text(font, tooltip, tx + 4, ty + 2, withAlpha(0xAAAAAA, alpha));
        }

        renderToast(gfx, font, sW, sH, alpha);
    }

    private static void renderModern(GuiGraphicsExtractor gfx, Minecraft mc, GTBConfig cfg) {
        Font font = mc.font;
        int sW = mc.getWindow().getGuiScaledWidth();
        int sH = mc.getWindow().getGuiScaledHeight();
        int lh = font.lineHeight + 2;

        int basePanelW = Math.min(sW - 8, 240);
        int panelW = (int)(basePanelW * easeOutCubic(panelWidthAnim));
        if (panelW < 20) return;
        int panelX = (sW - panelW) / 2;

        float openEase = easeOutBack(animProgress);
        int slideOffset = (int)((1f - openEase) * -40);
        int panelBaseY = sH / 4;
        int panelY = panelBaseY + slideOffset;

        int viewH = Math.min(sH / 2, 160);
        int listTop = panelY + lh * 3 + 10;
        int listBottom = listTop + viewH;
        if (listBottom > sH - 4) { listBottom = sH - 4; viewH = listBottom - listTop; }
        if (viewH < 0) viewH = 0;

        contentHeight = words.size() * lh;
        int cx = panelX + panelW / 2;
        int alpha = (int)(animProgress * 255);

        int glowPulse = (int)(180 + 75 * Math.sin(globalTick * 0.1));
        gfx.fill(panelX - 2, panelY - 3, panelX + panelW + 2, panelY - 1, withAlpha(ACCENT_GLOW, (int)(alpha * glowPulse / 255f)));
        gfx.fill(panelX - 1, panelY - 2, panelX + panelW + 1, panelY, withAlpha(ACCENT, alpha));

        gfx.fill(panelX, panelY, panelX + panelW, listBottom + 2, withAlpha(BG_PRIMARY, alpha));
        gfx.fill(panelX, panelY + 1, panelX + 1, listBottom + 1, withAlpha(0x30000000, alpha));
        gfx.fill(panelX + panelW - 1, panelY + 1, panelX + panelW, listBottom + 1, withAlpha(0x30000000, alpha));

        gfx.fill(panelX - 1, listBottom + 1, panelX + panelW + 1, listBottom + 2, withAlpha(ACCENT_DIM, (int)(alpha * 0.6f)));

        gfx.nextStratum();

        int titleY = panelY + 6;
        if (cfg.showHints) {
            int glowAlpha = (int)(titleGlow * alpha);
            drawText(gfx, font, "\u25b6 GTB", cx, titleY, withAlpha(ACCENT_GLOW, (int)(glowAlpha * 0.4f)));
            drawText(gfx, font, "\u25b6 GTB", cx, titleY, withAlpha(ACCENT, alpha));
        }

        if (cfg.showHints) {
            String hintDisplay = hint.isEmpty() ? "waiting for hint..." : formatHint(hint);
            String hintText = hintDisplay;
            if (font.width(hintText) > panelW - 20) hintText = truncate(hintText, panelW - 20, font);
            drawText(gfx, font, hintText, cx, titleY + lh, withAlpha(TEXT_YELLOW, alpha));
        }

        String countText = words.size() + " possible word" + (words.size() != 1 ? "s" : "");
        drawText(gfx, font, countText, cx, titleY + lh * 2 + (cfg.showHints ? 0 : -lh), withAlpha(TEXT_SECONDARY, alpha));

        if (words.isEmpty()) {
            String noMatch = "No matches";
            int noAlpha = (int)(0.7f + 0.3f * Math.sin(globalTick * 0.1)) * alpha / 255;
            drawText(gfx, font, noMatch, cx, listTop + viewH / 2, withAlpha(0xFFFF5555, noAlpha));
        } else if (viewH > 0) {
            gfx.enableScissor(0, listTop, sW, listBottom);

            hoveredIndex = -1;
            if (lastMouseX >= 0 && lastMouseY >= 0) {
                int hy = listTop - (int) scrollOffset;
                for (int i = 0; i < words.size(); i++) {
                    if (lastMouseX >= panelX && lastMouseX <= panelX + panelW
                        && lastMouseY >= hy && lastMouseY <= hy + font.lineHeight + 2
                        && lastMouseY >= listTop && lastMouseY <= listBottom) {
                        hoveredIndex = i;
                        break;
                    }
                    hy += lh;
                }
            }

            int y = listTop - (int) scrollOffset;
            for (int i = 0; i < words.size(); i++) {
                if (y + lh >= listTop - lh && y <= listBottom) {
                    String word = words.get(i);
                    if (font.width(word) > panelW - 24) word = truncate(word, panelW - 24, font);

                    boolean hovered = (i == hoveredIndex);
                    boolean single = words.size() == 1;

                    float entryAlpha = 0f;
                    if (entryAnimProgress != null && i < entryAnimProgress.length) {
                        entryAlpha = easeOutCubic(entryAnimProgress[i]);
                    }
                    int entryA = (int)(entryAlpha * alpha);
                    if (entryA <= 0) { y += lh; continue; }

                    int entrySlideX = (int)((1f - entryAlpha) * -20);

                    if (hovered) {
                        int hPulse = (int)(200 + 55 * Math.sin(globalTick * 0.12));
                        gfx.fill(panelX + 3, y - 1, panelX + panelW - 3, y + font.lineHeight + 3, withAlpha(BG_HOVER, entryA));
                        gfx.fill(panelX + 3, y - 1, panelX + 7, y + font.lineHeight + 3, withAlpha(ACCENT, entryA));
                        gfx.fill(panelX + 5, y - 1, panelX + 8, y + font.lineHeight + 3, withAlpha(ACCENT_GLOW, (int)(entryA * hPulse / 255f * 0.3f)));
                    } else {
                        int bgColor2 = (i % 2 == 0) ? BG_ROW_EVEN : BG_ROW_ODD;
                        gfx.fill(panelX + 4, y, panelX + panelW - 4, y + font.lineHeight + 1, withAlpha(bgColor2, entryA));
                    }

                    int textColor;
                    if (single) {
                        int greenPulse = (int)(220 + 35 * Math.sin(globalTick * 0.1));
                        textColor = withAlpha(TEXT_GREEN, (int)(entryA * greenPulse / 255f));
                    } else if (hovered) {
                        textColor = withAlpha(TEXT_GREEN, entryA);
                    } else {
                        textColor = withAlpha(TEXT_PRIMARY, entryA);
                    }
                    int wordWidth = font.width(word);
                    int wordX = panelX + (panelW - wordWidth) / 2 + entrySlideX;
                    gfx.text(font, word, wordX, y + 1, textColor);
                }
                y += lh;
            }
            gfx.disableScissor();
        }

        if (contentHeight > viewH && viewH > 0) {
            int barH = Math.max(10, (int)((float) viewH * viewH / contentHeight));
            int maxScroll = contentHeight - viewH;
            if (maxScroll > 0) {
                int barY = listTop + (int)((float)(viewH - barH) * scrollOffset / maxScroll);
                int barGlow = (int)(200 + 55 * Math.sin(globalTick * 0.08));
                gfx.fill(panelX + panelW - 5, barY, panelX + panelW - 2, barY + barH, withAlpha(ACCENT, (int)(alpha * barGlow / 255f)));
            }
        }

        if (hoveredIndex >= 0 && hoveredIndex < words.size()) {
            renderHoverTooltip(gfx, font, sW, sH, words.get(hoveredIndex));
        }

        renderToast(gfx, font, sW, sH, alpha);
    }

    private static void renderHoverTooltip(GuiGraphicsExtractor gfx, Font font, int sW, int sH, String word) {
        String tooltip = "\u27a4 Click to send: " + word;
        int tw = font.width(tooltip) + 16;
        int th = font.lineHeight + 6;
        int tx = (sW - tw) / 2;
        int ty = sH / 4 - 20;
        int ta = 220;

        int slideEase = (int)((1f - Math.min(1f, animProgress * 2)) * 10);
        ty += slideEase;

        gfx.fill(tx - 1, ty - 1, tx + tw + 1, ty, withAlpha(ACCENT, ta));
        gfx.fill(tx, ty, tx + tw, ty + th, withAlpha(0xF00A0A12, ta));
        gfx.nextStratum();
        gfx.text(font, tooltip, tx + 8, ty + 3, withAlpha(TEXT_GREEN, ta));
    }

    private static void renderToast(GuiGraphicsExtractor gfx, Font font, int sW, int sH, int baseAlpha) {
        if (toastAlpha <= 0f || toastMessage.isEmpty()) return;

        int toastW = font.width(toastMessage) + 24;
        int toastH = font.lineHeight + 10;
        int toastX = (sW - toastW) / 2;
        int toastBaseY = sH / 2 - toastH / 2;
        int toastY = toastBaseY + (int) toastSlideY;
        int ta = (int)(toastAlpha * 255);

        int glowA = (int)(ta * (0.7f + 0.3f * Math.sin(globalTick * 0.15)));
        gfx.fill(toastX - 2, toastY - 2, toastX + toastW + 2, toastY, withAlpha(ACCENT_GLOW, (int)(glowA * 0.5f)));
        gfx.fill(toastX - 1, toastY - 1, toastX + toastW + 1, toastY, withAlpha(ACCENT, ta));
        gfx.fill(toastX, toastY, toastX + toastW, toastY + toastH, withAlpha(0xE60A0F14, ta));

        gfx.nextStratum();
        gfx.text(font, toastMessage, toastX + 12, toastY + 5, withAlpha(TEXT_GREEN, ta));
    }

    // --- Easing functions ---

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float)Math.pow(t - 1, 3) + c1 * (float)Math.pow(t - 1, 2);
    }

    private static float easeOutCubic(float t) {
        float c1 = 1f - t;
        return 1f - c1 * c1 * c1;
    }

    private static int withAlpha(int color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static void drawText(GuiGraphicsExtractor gfx, Font font, String text, int centerX, int y, int color) {
        int textWidth = font.width(text);
        gfx.text(font, text, centerX - textWidth / 2, y, color);
    }

    private static String truncate(String text, int maxWidth, Font font) {
        String suffix = "\u2026";
        for (int i = text.length() - 1; i > 0; i--) {
            if (font.width(text.substring(0, i) + suffix) <= maxWidth) {
                return text.substring(0, i) + suffix;
            }
        }
        return suffix;
    }

    private static String formatHint(String raw) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == ' ') {
                sb.append("  ");
            } else {
                if (i > 0 && raw.charAt(i - 1) != ' ') sb.append(' ');
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
