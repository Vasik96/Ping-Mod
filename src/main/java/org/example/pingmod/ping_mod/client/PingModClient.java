package org.example.pingmod.ping_mod.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PingModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        registerCommands();
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(
                    ClientCommandManager.literal("ping").executes(context -> {
                        ClientPlayerEntity player = MinecraftClient.getInstance().player;
                        if (player != null) {
                            PlayerListEntry entry = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(player.getUuid());
                            if (entry != null) {
                                int latency = entry.getLatency();
                                Text message = latency == 0 ?
                                        Text.of("§8[§2PingMod§8] §cLatency information not received from the server") :
                                        Text.of("§8[§2PingMod§8] §7Your current latency is §a" + latency + " §ams");
                                player.sendMessage(message, false);
                            } else {
                                MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§8[§2PingMod§8] §cFailed to get your latency information"));
                            }
                        } else {
                            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§8[§2PingMod§8]§cYou are not currently connected to a server"));
                        }
                        return 1;
                    })
            );

            dispatcher.register(
                    ClientCommandManager.literal("ping").then(ClientCommandManager.argument("player", StringArgumentType.string())
                            .suggests((context, builder) -> suggestPlayerNames(builder))
                            .executes(context -> {
                                String playerName = StringArgumentType.getString(context, "player");
                                ClientPlayerEntity player = MinecraftClient.getInstance().player;

                                if (player != null) {
                                    PlayerListEntry entry = Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerListEntry(playerName);
                                    if (entry != null) {
                                        int latency = entry.getLatency();
                                        Text message = latency == 0 ?
                                                Text.of("§8[§2PingMod§8] §cLatency information not received from the server") :
                                                Text.of("§8[§2PingMod§8] §a" + playerName + "'s §7latency is §a" + latency + " §ams");
                                        player.sendMessage(message, false);
                                    } else {
                                        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§8[§2PingMod§8] §cPlayer not found"));
                                    }
                                } else {
                                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.of("§8[§2PingMod§8] §cYou are not currently connected to a server"));
                                }
                                return 1;
                            })
                    )
            );
        });
    }

    private CompletableFuture<Suggestions> suggestPlayerNames(SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        for (PlayerListEntry player : Objects.requireNonNull(MinecraftClient.getInstance().getNetworkHandler()).getPlayerList()) {
            String playerName = player.getProfile().getName();
            if (playerName.toLowerCase().startsWith(input)) {
                builder.suggest(playerName);
            }
        }
        return builder.buildFuture();
    }
}
