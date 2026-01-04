package com.aethor.aethornpcs.plugin.command;

import com.aethor.aethornpcs.api.dto.Npc;
import com.aethor.aethornpcs.api.dto.NpcLocation;
import com.aethor.aethornpcs.api.dto.NpcSpawnRequest;
import com.aethor.aethornpcs.plugin.AethorNPCSPlugin;
import com.aethor.aethornpcs.plugin.config.PluginConfiguration;
import com.aethor.aethornpcs.plugin.service.AethorNpcApiImpl;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Command handler for /npc commands.
 */
public class NpcCommand implements CommandExecutor, TabCompleter {

    private final AethorNPCSPlugin plugin;
    private final AethorNpcApiImpl api;
    private final PluginConfiguration config;

    public NpcCommand(AethorNPCSPlugin plugin, AethorNpcApiImpl api, PluginConfiguration config) {
        this.plugin = plugin;
        this.api = api;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("aethornpcs.admin")) {
            sender.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "create":
                return handleCreate(sender, args);
            case "remove":
                return handleRemove(sender, args);
            case "move":
                return handleMove(sender, args);
            case "respawn":
                return handleRespawn(sender, args);
            case "list":
                return handleList(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleCreate(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /npc create <id> <mythicMob> [model] [displayName...]", NamedTextColor.RED));
            sender.sendMessage(Component.text("Example: /npc create guard_1 SkeletonKing royal_guard <gradient:red:blue>Elite Guard", NamedTextColor.GRAY));
            return true;
        }

        Player player = (Player) sender;
        String id = args[1];
        String mythicMob = args[2];
        String model = args.length > 3 && !args[3].startsWith("<") && !args[3].startsWith("#") && !args[3].startsWith("&") ? args[3] : null;
        
        // Collect display name from remaining args
        String displayName = null;
        int nameStartIndex = model != null ? 4 : 3;
        if (args.length > nameStartIndex) {
            displayName = String.join(" ", java.util.Arrays.copyOfRange(args, nameStartIndex, args.length));
        }

        // Check if NPC already exists
        if (api.getNpc(id).isPresent()) {
            sender.sendMessage(Component.text("NPC with ID '" + id + "' already exists!", NamedTextColor.RED));
            return true;
        }

        // Validate MythicMob exists
        if (!plugin.getMythicMobAdapter().mobTypeExists(mythicMob)) {
            sender.sendMessage(Component.text("MythicMob type '" + mythicMob + "' does not exist!", NamedTextColor.RED));
            return true;
        }

        // Validate model if specified
        if (model != null && plugin.getModelEngineAdapter().isAvailable()) {
            if (!plugin.getModelEngineAdapter().modelExists(model)) {
                sender.sendMessage(Component.text("Model '" + model + "' does not exist!", NamedTextColor.YELLOW));
                sender.sendMessage(Component.text("Continuing without model...", NamedTextColor.YELLOW));
                model = null;
            }
        }

        // Create spawn request
        Location loc = player.getLocation();
        NpcLocation npcLoc = new NpcLocation(
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );

        NpcSpawnRequest request = NpcSpawnRequest.builder()
                .id(id)
                .location(npcLoc)
                .mythicMobInternalName(mythicMob)
                .modelEngineModelId(model)
                .displayName(displayName)
                .invulnerable(config.getDefaultInvulnerable())
                .silent(config.getDefaultSilent())
                .noAI(config.getDefaultNoAI())
                .lookAtPlayers(config.getDefaultLookAtPlayers())
                .build();

        try {
            api.spawn(request);
            sender.sendMessage(Component.text("Created NPC '" + id + "' successfully!", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Failed to create NPC: " + e.getMessage(), NamedTextColor.RED));
            plugin.getLogger().severe("Error creating NPC: " + e.getMessage());
        }

        return true;
    }

    private boolean handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /npc remove <id>", NamedTextColor.RED));
            return true;
        }

        String id = args[1];

        if (api.despawn(id)) {
            sender.sendMessage(Component.text("Removed NPC '" + id + "'.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("NPC '" + id + "' not found.", NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleMove(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("This command can only be used by players.", NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /npc move <id>", NamedTextColor.RED));
            return true;
        }

        Player player = (Player) sender;
        String id = args[1];

        Optional<Npc> npcOpt = api.getNpc(id);
        if (npcOpt.isEmpty()) {
            sender.sendMessage(Component.text("NPC '" + id + "' not found.", NamedTextColor.RED));
            return true;
        }

        // Remove old NPC
        api.despawn(id);

        // Create new one at player's location
        Npc oldNpc = npcOpt.get();
        Location loc = player.getLocation();
        NpcLocation npcLoc = new NpcLocation(
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );

        NpcSpawnRequest request = NpcSpawnRequest.builder()
                .id(id)
                .location(npcLoc)
                .mythicMobInternalName(oldNpc.getMythicMobInternalName())
                .displayName(oldNpc.getDisplayName())
                .modelEngineModelId(oldNpc.getModelEngineModelId())
                .metadata(oldNpc.getMetadata())
                .invulnerable(oldNpc.isInvulnerable())
                .silent(oldNpc.isSilent())
                .noAI(oldNpc.hasNoAI())
                .lookAtPlayers(oldNpc.shouldLookAtPlayers())
                .build();

        try {
            api.spawn(request);
            sender.sendMessage(Component.text("Moved NPC '" + id + "' to your location.", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Failed to move NPC: " + e.getMessage(), NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleRespawn(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /npc respawn <id>", NamedTextColor.RED));
            return true;
        }

        String id = args[1];

        if (api.respawn(id)) {
            sender.sendMessage(Component.text("Respawned NPC '" + id + "'.", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("NPC '" + id + "' not found.", NamedTextColor.RED));
        }

        return true;
    }

    private boolean handleList(CommandSender sender) {
        Collection<Npc> npcs = api.getAllNpcs();

        if (npcs.isEmpty()) {
            sender.sendMessage(Component.text("No NPCs registered.", NamedTextColor.YELLOW));
            return true;
        }

        sender.sendMessage(Component.text("=== Registered NPCs ===", NamedTextColor.GOLD));
        for (Npc npc : npcs) {
            String status = npc.isSpawned() ? "SPAWNED" : "NOT SPAWNED";
            NamedTextColor color = npc.isSpawned() ? NamedTextColor.GREEN : NamedTextColor.GRAY;
            
            sender.sendMessage(Component.text(
                    "  " + npc.getId() + " [" + status + "] - " + npc.getMythicMobInternalName(),
                    color
            ));
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== AethorNPCS Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/npc create <id> <mythicMob> [model] [displayName...] - Create an NPC", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("  Example: /npc create guard_1 Villager <#FF0000>Red Guard", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("  Supports MiniMessage: <gradient:red:blue>, <rainbow>, hex colors", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/npc remove <id> - Remove an NPC", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/npc move <id> - Move an NPC to your location", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/npc respawn <id> - Respawn an NPC", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/npc list - List all NPCs", NamedTextColor.YELLOW));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("aethornpcs.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("create", "remove", "move", "respawn", "list");
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("remove") || 
                                  args[0].equalsIgnoreCase("move") || 
                                  args[0].equalsIgnoreCase("respawn"))) {
            return api.getAllNpcs().stream()
                    .map(Npc::getId)
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
