package com.guessbuild;

import com.guessbuild.screen.InputCaptureScreen;
import com.guessbuild.screen.OverlayRenderer;
import com.guessbuild.screen.SettingsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import com.mojang.blaze3d.platform.InputConstants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GuessTheBuild implements ClientModInitializer {

    private static final java.util.regex.Pattern FORMATTING = java.util.regex.Pattern.compile("\u00a7[0-9a-fk-or]");
    private static final List<String> WORDLIST = new ArrayList<>();

    private static String lastHint = "";
    private static List<String> lastWords = new ArrayList<>();
    private static String lastMessage = "";
    private static KeyMapping openGuiKey;
    private static KeyMapping openSettingsKey;
    private static int autoSendTick = -1;
    private static String autoSendWord = "";

    @Override
    public void onInitializeClient() {
        GTBConfig.get().load();
        loadWordlist();
        System.out.println("[GTB] Ready - " + WORDLIST.size() + " words loaded");

        OverlayRenderer.init();

        KeyMapping.Category gtbCategory = KeyMapping.Category.register(
            net.minecraft.resources.Identifier.fromNamespaceAndPath("guessbuild", "category"));

        openGuiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.guessbuild.opengui",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_RSHIFT,
            gtbCategory
        ));

        openSettingsKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.guessbuild.opensettings",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F8,
            gtbCategory
        ));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft mc) {
        GTBConfig cfg = GTBConfig.get();

        if (autoSendTick > 0) {
            autoSendTick--;
        } else if (autoSendTick == 0) {
            autoSendTick = -1;
            if (!autoSendWord.isEmpty()) {
                String w = autoSendWord;
                autoSendWord = "";
                sendOrPaste(w);
            }
        }

        if (openGuiKey.consumeClick() && mc.screen == null) {
            if (OverlayRenderer.isVisible()) {
                OverlayRenderer.setVisible(false);
            } else if (!lastWords.isEmpty()) {
                OverlayRenderer.show(lastHint, lastWords);
                mc.setScreen(new InputCaptureScreen());
            }
        }

        if (openSettingsKey.consumeClick() && mc.screen == null) {
            mc.setScreen(new SettingsScreen());
        }
    }

    public static void sendOrPaste(String word) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        OverlayRenderer.setVisible(false);

        GTBConfig cfg = GTBConfig.get();
        if (cfg.soundEnabled) {
            mc.execute(() -> {
                if (mc.player != null) {
                    mc.player.playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 1.2f);
                }
            });
        }

        if (cfg.pasteInChat) {
            mc.execute(() -> {
                if (mc.screen == null) {
                    mc.player.connection.sendChat(word);
                }
            });
        } else {
            mc.execute(() -> mc.player.connection.sendChat(word));
        }
    }

    public static String componentToLegacyString(Component component) {
        StringBuilder sb = new StringBuilder();
        appendLegacy(component, sb);
        return sb.toString();
    }

    private static void appendLegacy(Component component, StringBuilder sb) {
        Style style = component.getStyle();
        net.minecraft.network.chat.TextColor textColor = style.getColor();
        if (textColor != null) {
            for (ChatFormatting f : ChatFormatting.values()) {
                if (f.isColor() && f.getColor() != null && f.getColor().equals(textColor.getValue())) {
                    sb.append("\u00a7").append(f.getChar());
                    break;
                }
            }
        }
        if (style.isBold()) sb.append("\u00a7l");
        if (style.isItalic()) sb.append("\u00a7o");
        if (style.isUnderlined()) sb.append("\u00a7n");
        if (style.isStrikethrough()) sb.append("\u00a7m");
        if (style.isObfuscated()) sb.append("\u00a7k");
        sb.append(component.getString());
        for (Component sibling : component.getSiblings()) {
            appendLegacy(sibling, sb);
        }
    }

    public static void handleMessage(String raw) {
        if (raw == null || raw.isEmpty()) return;

        String stripped = FORMATTING.matcher(raw).replaceAll(" ");
        stripped = stripped.replaceAll("\\s+", " ").trim();
        System.out.println("[GTB] TEXT: " + stripped);

        if (!stripped.contains("_")) {
            System.out.println("[GTB] Skipped (no underscores)");
            return;
        }

        if (stripped.equals(lastMessage)) {
            System.out.println("[GTB] Skipped (duplicate)");
            return;
        }
        lastMessage = stripped;

        String word = extractWord(stripped);
        System.out.println("[GTB] WORD: " + word);

        List<String> matches = matchWords(word);
        System.out.println("[GTB] MATCHES: " + matches.size());

        if (matches.isEmpty()) {
            System.out.println("[GTB] No matches, skipping GUI");
            return;
        }

        lastHint = word != null ? word : stripped;
        lastWords = matches;

        GTBConfig cfg = GTBConfig.get();

        if (matches.size() == 1 && cfg.autoSend) {
            String match = matches.get(0);
            System.out.println("[GTB] Auto-sending: " + match);
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || mc.player == null) return;
            mc.execute(() -> OverlayRenderer.showToast("\u2714 Found: " + match));
            if (cfg.instantSend) {
                autoSendWord = match;
                autoSendTick = 1;
            } else {
                autoSendWord = match;
                autoSendTick = cfg.autoSendDelay;
            }
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;
        mc.execute(() -> {
            OverlayRenderer.show(lastHint, lastWords);
            mc.setScreen(new InputCaptureScreen());
        });
    }

    private static String extractWord(String stripped) {
        String lower = stripped.toLowerCase(Locale.ROOT);
        String[] parts = lower.split("\\s+");
        List<String> hintWords = new ArrayList<>();
        boolean collecting = false;
        for (String part : parts) {
            if (part.contains("_")) {
                hintWords.add(part);
                collecting = true;
            } else if (collecting) {
                break;
            }
        }
        if (hintWords.isEmpty()) return null;
        return String.join(" ", hintWords);
    }

    private static List<String> matchWords(String word) {
        if (word == null || word.isEmpty()) return new ArrayList<>();

        List<String> exactMatches = matchWordsExact(word);

        String noSpaces = word.replace(" ", "");
        List<String> fusedMatches = new ArrayList<>();
        if (!noSpaces.equals(word)) {
            for (String m : matchWordsExact(noSpaces)) {
                if (!m.contains(" ") && !exactMatches.contains(m)) {
                    fusedMatches.add(m);
                }
            }
        }

        List<String> combined = new ArrayList<>(exactMatches);
        combined.addAll(fusedMatches);
        return combined;
    }

    private static List<String> matchWordsExact(String word) {
        int len = word.length();
        int spaces = 0;
        for (char c : word.toCharArray()) if (c == ' ') spaces++;

        List<String> candidates = new ArrayList<>();
        for (String w : WORDLIST) {
            if (w.length() == len) {
                int wSpaces = 0;
                for (char c : w.toCharArray()) if (c == ' ') wSpaces++;
                if (wSpaces == spaces) candidates.add(w);
            }
        }

        List<int[]> revealed = new ArrayList<>();
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            if (c != '_' && c != ' ') revealed.add(new int[]{i, c});
        }
        if (revealed.isEmpty()) return candidates;

        List<String> result = new ArrayList<>();
        for (String w : candidates) {
            String wl = w.toLowerCase(Locale.ROOT);
            boolean ok = true;
            for (int[] pair : revealed) {
                if (pair[0] >= wl.length() || wl.charAt(pair[0]) != (char) pair[1]) {
                    ok = false;
                    break;
                }
            }
            if (ok) result.add(w);
        }
        return result;
    }

    private void loadWordlist() {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(GuessTheBuild.class.getResourceAsStream("/wordlist.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                String t = line.trim();
                if (!t.isEmpty()) WORDLIST.add(t);
            }
            reader.close();
        } catch (Exception e) {
            System.err.println("[GTB] Failed to load wordlist: " + e.getMessage());
        }
    }
}
