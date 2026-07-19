package com.guessbuild.screen;

import com.guessbuild.GuessTheBuild;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ResultsScreen extends Screen {

    private List<String> results;
    private String hint;
    private int scrollOffset = 0;
    private int contentHeight = 0;

    private int listTop;
    private int listBottom;

    public ResultsScreen(String hint, List<String> results) {
        super(Component.literal("GTB Solver"));
        this.hint = hint;
        this.results = new ArrayList<>(results);
    }

    public void updateResults(String newHint, List<String> newResults) {
        this.hint = newHint;
        this.results = new ArrayList<>(newResults);
        this.scrollOffset = 0;
    }

    @Override
    protected void init() {
        System.out.println("[GTB GUI] init() called! words=" + results.size());
        int lh = this.font.lineHeight;
        int spacing = lh + 2;

        int headerLines = 3;
        listTop = 4 + spacing * headerLines;
        listBottom = this.height - lh - 8;

        if (listBottom <= listTop) {
            listBottom = listTop + spacing * 2;
        }

        Button closeBtn = Button.builder(
                Component.literal("Close"),
                btn -> this.onClose()
            )
            .pos(Math.max(4, this.width / 2 - 50), this.height - lh - 4)
            .width(Math.min(100, this.width - 8))
            .build();
        addRenderableWidget(closeBtn);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
        System.out.println("[GTB GUI] extractRenderState called! words=" + results.size() + " w=" + this.width + " h=" + this.height);
        super.extractRenderState(gfx, mouseX, mouseY, delta);

        gfx.enableScissor(0, 0, this.width, this.height);

        int centerX = this.width / 2;
        int lh = this.font.lineHeight;
        int spacing = lh + 2;
        int pad = 2;
        int availWidth = this.width - pad * 2;

        drawClippedText(gfx, "GTB Solver", centerX, 0, 0x55FF55, availWidth);

        String hintDisplay = hint.isEmpty() ? "waiting for hint..." : formatHint(hint);
        drawClippedText(gfx, "Hint: " + hintDisplay, centerX, spacing, 0xFFFF55, availWidth);

        drawClippedText(gfx, results.size() + " possible word" + (results.size() != 1 ? "s" : ""),
            centerX, spacing * 2, 0xAAAAAA, availWidth);

        int viewHeight = listBottom - listTop;
        contentHeight = results.size() * spacing;

        if (viewHeight > 0) {
            gfx.fill(0, listTop - 1, this.width, listBottom + 1, 0x80000000);

            gfx.enableScissor(0, listTop, this.width, listBottom);

            int y = listTop - scrollOffset;
            for (int i = 0; i < results.size(); i++) {
                if (y + lh >= listTop - lh && y <= listBottom + lh) {
                    boolean isHovered = mouseX >= 0 && mouseX <= this.width
                        && mouseY >= y - 1 && mouseY <= y + lh + 1;
                    int bgColor = isHovered ? 0x50FFFFFF : ((i % 2 == 0) ? 0x30FFFFFF : 0x18FFFFFF);
                    gfx.fill(0, y - 1, this.width, y + lh + 1, bgColor);

                    boolean isSingleResult = results.size() == 1;
                    int textColor = isSingleResult ? 0x55FF55 : (isHovered ? 0x55FF55 : 0xFFFFFF);
                    String word = results.get(i);
                    if (this.font.width(word) > availWidth) {
                        word = truncateText(word, availWidth);
                    }
                    gfx.text(this.font, word, pad, y, textColor);
                }
                y += spacing;
            }

            gfx.disableScissor();

            if (contentHeight > viewHeight) {
                int barHeight = Math.max(6, (int)((double) viewHeight * viewHeight / contentHeight));
                int maxScroll = contentHeight - viewHeight;
                int barY = listTop + (int)((double)(viewHeight - barHeight) * scrollOffset / maxScroll);
                gfx.fill(this.width - 4, barY, this.width, barY + barHeight, 0x80FFFFFF);
            }
        }

        if (results.isEmpty()) {
            gfx.centeredText(this.font, Component.literal("No matches found"),
                centerX, listTop + Math.max(0, viewHeight) / 2, 0xFF5555);
        }

        gfx.disableScissor();
    }

    private void drawClippedText(GuiGraphicsExtractor gfx, String text, int centerX, int y, int color, int availWidth) {
        int textWidth = this.font.width(text);
        int textX;
        if (textWidth <= availWidth) {
            textX = centerX - textWidth / 2;
        } else {
            text = truncateText(text, availWidth);
            textX = 2;
        }
        gfx.text(this.font, Component.literal(text), textX, y, color);
    }

    private String truncateText(String text, int maxWidth) {
        String suffix = "...";
        int suffixWidth = this.font.width(suffix);
        if (this.font.width(text) <= maxWidth) return text;
        for (int i = text.length() - 1; i > 0; i--) {
            if (this.font.width(text.substring(0, i) + suffix) <= maxWidth) {
                return text.substring(0, i) + suffix;
            }
        }
        return suffix;
    }

    private String formatHint(String raw) {
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

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int viewHeight = listBottom - listTop;
        if (viewHeight <= 0) return true;
        int maxScroll = Math.max(0, contentHeight - viewHeight);
        scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (int)(verticalAmount * 14)));
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!results.isEmpty()) {
            int spacing = this.font.lineHeight + 2;
            int y = listTop - scrollOffset;
            for (int i = 0; i < results.size(); i++) {
                if (event.y() >= y - 1 && event.y() <= y + this.font.lineHeight + 1
                    && event.x() >= 0 && event.x() <= this.width
                    && listTop <= event.y() && event.y() <= listBottom) {
                    GuessTheBuild.sendOrPaste(results.get(i));
                    return true;
                }
                y += spacing;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
