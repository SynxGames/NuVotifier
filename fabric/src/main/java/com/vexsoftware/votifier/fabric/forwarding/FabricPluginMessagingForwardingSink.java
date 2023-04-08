package com.vexsoftware.votifier.fabric.forwarding;

import com.vexsoftware.votifier.fabric.NuVotifier;
import com.vexsoftware.votifier.support.forwarding.AbstractPluginMessagingForwardingSink;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;

public class FabricPluginMessagingForwardingSink extends AbstractPluginMessagingForwardingSink {

    private final ResourceLocation channel;

    public FabricPluginMessagingForwardingSink(NuVotifier plugin, String channel, ForwardedVoteListener listener) {
        super(listener);
        this.channel = new ResourceLocation(channel);

        ServerPlayNetworking.registerGlobalReceiver(this.channel, (server, player, handler, buf, responseSender) -> {

           byte[] arr = buf.accessByteBufWithCorrectSize();
           try {
               this.handlePluginMessage(arr);
           } catch (Exception e) {
                plugin.getPluginLogger().error("There was an unknown error when processing a forwarded vote.", e);
           }

        });

    }

    @Override
    public void halt() {
        ServerPlayNetworking.unregisterGlobalReceiver(this.channel);
    }
}
