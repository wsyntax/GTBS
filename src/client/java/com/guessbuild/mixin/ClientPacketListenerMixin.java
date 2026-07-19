package com.guessbuild.mixin;

import com.guessbuild.GuessTheBuild;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.network.protocol.game.ClientboundDisguisedChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "setActionBarText", at = @At("HEAD"), cancellable = false)
    private void onSetActionBarText(ClientboundSetActionBarTextPacket packet, CallbackInfo ci) {
        System.out.println("[GTB MIXIN] setActionBarText fired!");
        Component text = packet.text();
        if (text != null) {
            String content = GuessTheBuild.componentToLegacyString(text);
            System.out.println("[GTB MIXIN] content: " + content);
            if (content != null && !content.isEmpty()) {
                GuessTheBuild.handleMessage(content);
            }
        }
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"), cancellable = false)
    private void onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        System.out.println("[GTB MIXIN] handleSystemChat fired!");
        Component text = packet.content();
        if (text != null) {
            String content = GuessTheBuild.componentToLegacyString(text);
            System.out.println("[GTB MIXIN] content: " + content);
            if (content != null && !content.isEmpty()) {
                GuessTheBuild.handleMessage(content);
            }
        }
    }

    @Inject(method = "handleDisguisedChat", at = @At("HEAD"), cancellable = false)
    private void onDisguisedChat(ClientboundDisguisedChatPacket packet, CallbackInfo ci) {
        System.out.println("[GTB MIXIN] handleDisguisedChat fired!");
        Component text = packet.message();
        if (text != null) {
            String content = GuessTheBuild.componentToLegacyString(text);
            System.out.println("[GTB MIXIN] content: " + content);
            if (content != null && !content.isEmpty()) {
                GuessTheBuild.handleMessage(content);
            }
        }
    }
}
