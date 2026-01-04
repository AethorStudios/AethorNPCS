package com.aethor.aethornpcs.plugin;

import com.aethor.aethornpcs.api.AethorNpcApi;
import com.aethor.aethornpcs.plugin.adapter.ModelEngineAdapter;
import com.aethor.aethornpcs.plugin.adapter.MythicMobAdapter;
import com.aethor.aethornpcs.plugin.integrations.HologramAdapter;
import com.aethor.aethornpcs.plugin.command.NpcCommand;
import com.aethor.aethornpcs.plugin.config.PluginConfiguration;
import com.aethor.aethornpcs.plugin.gui.GuiManager;
import com.aethor.aethornpcs.plugin.listener.ChunkListener;
import com.aethor.aethornpcs.plugin.listener.InteractionListener;
import com.aethor.aethornpcs.plugin.persistence.NpcPersistence;
import com.aethor.aethornpcs.plugin.proximity.ProximityDetector;
import com.aethor.aethornpcs.plugin.registry.NpcRegistry;
import com.aethor.aethornpcs.plugin.service.AethorNpcApiImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Main plugin class for AethorNPCS.
 */
public final class AethorNPCSPlugin extends JavaPlugin {

    private static AethorNPCSPlugin instance;
    
    private PluginConfiguration configuration;
    private MythicMobAdapter mythicMobAdapter;
    private ModelEngineAdapter modelEngineAdapter;
    private HologramAdapter hologramAdapter;
    private NpcRegistry npcRegistry;
    private NpcPersistence npcPersistence;
    private AethorNpcApiImpl apiImplementation;
    private ProximityDetector proximityDetector;
    private GuiManager guiManager;
        
    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Initializing AethorNPCS...");

        // Save default config
        saveDefaultConfig();

        // Load configuration
        configuration = new PluginConfiguration(getConfig());
        
        // Check dependencies
        if (!checkDependencies()) {
            getLogger().severe("Missing required dependencies! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize adapters
        mythicMobAdapter = new MythicMobAdapter(this);
        modelEngineAdapter = new ModelEngineAdapter(this);
        hologramAdapter = new HologramAdapter(this);

        // Initialize registry
        npcRegistry = new NpcRegistry();

        // Initialize persistence
        npcPersistence = new NpcPersistence(this, getDataFolder());

        // Initialize API implementation
        apiImplementation = new AethorNpcApiImpl(
                this,
                npcRegistry,
                npcPersistence,
                mythicMobAdapter,
                modelEngineAdapter,
                hologramAdapter,
                configuration
        );

        // Register API service
        Bukkit.getServicesManager().register(
                AethorNpcApi.class,
                apiImplementation,
                this,
                ServicePriority.Normal
        );

        // Load persisted NPCs
        npcPersistence.load();
        
        // Spawn loaded NPCs after a delay to ensure chunks/worlds are ready
        // NPCs in unloaded chunks will spawn when those chunks load (via ChunkListener)
        Bukkit.getScheduler().runTaskLater(this, () -> {
            apiImplementation.loadAndSpawnAll();
        }, 20L); // 20 ticks = 1 second delay

        // Register listeners
        getServer().getPluginManager().registerEvents(new InteractionListener(this, apiImplementation, npcRegistry), this);
        getServer().getPluginManager().registerEvents(new ChunkListener(this, apiImplementation, npcRegistry), this);

        // Initialize GUI manager
        guiManager = new GuiManager(this);

        // Initialize proximity detector
        if (configuration.isProximityEnabled()) {
            proximityDetector = new ProximityDetector(this, npcRegistry, configuration);
            proximityDetector.start();
        }

        // Register command
        NpcCommand npcCommand = new NpcCommand(this, apiImplementation, configuration, guiManager);
        getCommand("npc").setExecutor(npcCommand);
        getCommand("npc").setTabCompleter(npcCommand);

        getLogger().info("AethorNPCS enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling AethorNPCS...");

        // Stop proximity detector
        if (proximityDetector != null) {
            proximityDetector.stop();
        }

        // Despawn all NPCs
        if (apiImplementation != null) {
            apiImplementation.despawnAll();
        }
        
        // Remove all holograms
        if (hologramAdapter != null) {
            hologramAdapter.removeAllHolograms();
        }

        // Save NPCs
        if (npcPersistence != null) {
            npcPersistence.save();
        }

        // Unregister API service
        Bukkit.getServicesManager().unregisterAll(this);

        getLogger().info("AethorNPCS disabled.");
    }

    private boolean checkDependencies() {
        boolean hasAllDependencies = true;

        if (getServer().getPluginManager().getPlugin("MythicMobs") == null) {
            getLogger().severe("MythicMobs not found! This plugin is required.");
            hasAllDependencies = false;
        } else {
            getLogger().info("MythicMobs found.");
        }

        if (getServer().getPluginManager().getPlugin("ModelEngine") == null) {
            getLogger().warning("ModelEngine not found. Model features will be disabled.");
        } else {
            getLogger().info("ModelEngine found.");
        }

        return hasAllDependencies;
    }

    public PluginConfiguration getConfiguration() {
        return configuration;
    }

    public MythicMobAdapter getMythicMobAdapter() {
        return mythicMobAdapter;
    }

    public ModelEngineAdapter getModelEngineAdapter() {
        return modelEngineAdapter;
    }

    public NpcRegistry getNpcRegistry() {
        return npcRegistry;
    }

    public void debug(String message) {
        if (configuration != null && configuration.isDebugLogging()) {
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }
    
    public static AethorNPCSPlugin getInstance() {
        return instance;
    }
    
    public AethorNpcApiImpl getApiImpl() {
        return apiImplementation;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }
}
