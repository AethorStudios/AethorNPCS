# AethorNPCS

**Production-quality NPC Platform for Minecraft Paper 1.21.8**

AethorNPCS is a powerful NPC management system that leverages **MythicMobs** for entity spawning and **ModelEngine** for advanced visual models and animations. It provides a clean, stable API for quest plugins and other systems to interact with NPCs through a service-based architecture.

## 🎯 Features

- ✅ **MythicMobs Integration** - Use any MythicMob as an NPC
- ✅ **ModelEngine Integration** - Apply custom models and animations
- ✅ **Clean API** - Service-based API via Bukkit ServicesManager
- ✅ **Multi-Module Maven** - Separate API and implementation modules
- ✅ **Event System** - Interaction, proximity, spawn/despawn events
- ✅ **Persistence** - Automatic save/load of NPC definitions
- ✅ **Chunk Management** - Smart spawn/despawn on chunk load/unload
- ✅ **Proximity Detection** - Configurable radius and check frequency
- ✅ **PDC & Scoreboard Tagging** - Reliable NPC entity identification
- ✅ **Command Interface** - In-game NPC management
- ✅ **Metadata System** - Store arbitrary data on NPCs for quest systems

## 📋 Requirements

- **Minecraft**: Paper 1.21.4+ (tested on 1.21.8)
- **Java**: 21
- **Dependencies**: 
  - **MythicMobs** 5.7.1+ (required)
  - **ModelEngine** R4.0.6+ (optional)

## 🏗️ Project Structure

```
AethorNPCS/
├── pom.xml                          # Parent POM
├── aethornpcs-api/                  # Pure Java API module
│   ├── pom.xml
│   └── src/main/java/com/aethor/aethornpcs/api/
│       ├── AethorNpcApi.java        # Main API interface
│       ├── dto/                     # Data transfer objects
│       │   ├── Npc.java
│       │   ├── NpcLocation.java
│       │   └── NpcSpawnRequest.java
│       └── payload/                 # Event payloads
│           ├── InteractPayload.java
│           ├── ProximityPayload.java
│           ├── SpawnPayload.java
│           └── DespawnPayload.java
└── aethornpcs-plugin/               # Paper plugin implementation
    ├── pom.xml
    └── src/main/java/com/aethor/aethornpcs/plugin/
        ├── AethorNPCSPlugin.java    # Main plugin class
        ├── adapter/                 # Integration adapters
        │   ├── MythicMobAdapter.java
        │   └── ModelEngineAdapter.java
        ├── command/                 # Command handlers
        │   └── NpcCommand.java
        ├── config/
        │   └── PluginConfiguration.java
        ├── event/                   # Bukkit events
        │   ├── AethorNpcInteractEvent.java
        │   ├── AethorNpcProximityEvent.java
        │   ├── AethorNpcSpawnEvent.java
        │   └── AethorNpcDespawnEvent.java
        ├── listener/                # Event listeners
        │   ├── InteractionListener.java
        │   └── ChunkListener.java
        ├── model/
        │   └── NpcImpl.java
        ├── persistence/
        │   └── NpcPersistence.java
        ├── proximity/
        │   └── ProximityDetector.java
        ├── registry/
        │   └── NpcRegistry.java
        └── service/
            └── AethorNpcApiImpl.java
```

## 🔧 Building the Project

### Prerequisites

Install MythicMobs and ModelEngine dependencies (if not already in Maven repos):

```bash
# Download MythicMobs and ModelEngine JARs from their respective sources
# Then install them to your local Maven repository:

mvn install:install-file \
  -Dfile=Mythic-Dist-5.7.1.jar \
  -DgroupId=io.lumine \
  -DartifactId=Mythic-Dist \
  -Dversion=5.7.1 \
  -Dpackaging=jar

mvn install:install-file \
  -Dfile=ModelEngine-R4.0.6.jar \
  -DgroupId=com.ticxo.modelengine \
  -DartifactId=ModelEngine \
  -Dversion=R4.0.6 \
  -Dpackaging=jar
```

**Note**: The project is configured to use Lumine's Maven repository, so if MythicMobs and ModelEngine are available there, you can skip the manual installation.

### Build Commands

```bash
# Build entire project (both modules)
mvn clean package

# Build only the plugin (and its dependencies)
mvn clean package -pl aethornpcs-plugin -am

# Build without tests
mvn clean package -DskipTests

# Install to local Maven repository
mvn clean install
```

The compiled plugin JAR will be in:
```
aethornpcs-plugin/target/AethorNPCS-1.0.0-SNAPSHOT.jar
```

## 📦 Installation

1. Ensure **MythicMobs** is installed on your server
2. (Optional) Install **ModelEngine** if you want custom models
3. Place **ONLY** `AethorNPCS-1.0.0-SNAPSHOT.jar` in your `plugins/` folder
   - ⚠️ **Do NOT** install `aethornpcs-api-1.0.0-SNAPSHOT.jar` - the API is already included in the main plugin jar
4. Restart the server
5. Configure `plugins/AethorNPCS/config.yml` as needed

> **Note:** The API module is already shaded into the main plugin jar. The separate `aethornpcs-api-1.0.0-SNAPSHOT.jar` is only for quest plugin developers to compile against.

## ⚙️ Configuration

**config.yml**:
```yaml
# Proximity detection settings
proximity:
  enabled: true
  radius: 3.0
  periodTicks: 10

# Chunk unload strategy
# DESPAWN_ON_UNLOAD: Remove entities when chunk unloads, respawn on load
# KEEP_LOADED: Keep chunks loaded (not recommended)
chunkStrategy: DESPAWN_ON_UNLOAD

# Default flags applied to all NPCs
defaultFlags:
  invulnerable: true
  silent: true
  noAI: true
  lookAtPlayers: true

# Enable debug logging
debugLogging: false
```

## 🎮 Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/npc create <id> <mythicMob> [model]` | Create an NPC at your location | `aethornpcs.admin` |
| `/npc remove <id>` | Remove an NPC | `aethornpcs.admin` |
| `/npc move <id>` | Move an NPC to your location | `aethornpcs.admin` |
| `/npc respawn <id>` | Respawn an NPC | `aethornpcs.admin` |
| `/npc list` | List all NPCs | `aethornpcs.admin` |

### Examples

```bash
# Create a questgiver NPC using MythicMob "SkeletonKing" with model "questgiver_male"
/npc create questgiver_1 SkeletonKing questgiver_male

# Create a merchant without a model
/npc create merchant_1 Villager

# Move an existing NPC to your location
/npc move questgiver_1

# Remove an NPC
/npc remove merchant_1
```

## 📚 API Documentation

### Overview

AethorNPCS provides a comprehensive API for managing NPCs programmatically. The API is exposed through Bukkit's ServicesManager and consists of:

- **Core Interface**: `AethorNpcApi` - Main API entry point
- **DTOs**: Immutable data objects for NPC data and requests
- **Events**: Bukkit events for interaction, proximity, spawn/despawn
- **Payloads**: Event data containers with detailed information

---

## 🔌 API Integration

### Getting the API

```java
import com.aethor.aethornpcs.api.AethorNpcApi;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class YourQuestPlugin extends JavaPlugin {
    
    private AethorNpcApi npcApi;
    
    @Override
    public void onEnable() {
        // Get API from ServicesManager
        RegisteredServiceProvider<AethorNpcApi> provider = 
            Bukkit.getServicesManager().getRegistration(AethorNpcApi.class);
        
        if (provider != null) {
            npcApi = provider.getProvider();
            getLogger().info("AethorNPCS API hooked successfully!");
        } else {
            getLogger().severe("AethorNPCS not found!");
            getServer().getPluginManager().disablePlugin(this);
        }
    }
}
```

### Maven Dependency

Add to your plugin's `pom.xml`:

```xml
<repositories>
    <repository>
        <id>local-repo</id>
        <url>file://${project.basedir}/libs</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.aethor</groupId>
        <artifactId>aethornpcs-api</artifactId>
        <version>1.0.1</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Place `aethornpcs-api-1.0.1.jar` in your project's `libs/` folder, or install it to your local Maven repository.

---

## 📖 API Reference

### AethorNpcApi Interface

The main API interface provides all NPC management operations.

#### NPC Creation & Spawning

```java
/**
 * Spawns a new NPC at the specified location.
 * 
 * @param request NpcSpawnRequest containing all spawn parameters
 * @return Npc object representing the spawned NPC
 * @throws IllegalArgumentException if NPC ID already exists
 * @throws IllegalStateException if MythicMob type not found
 */
Npc spawn(NpcSpawnRequest request);

/**
 * Despawns an NPC by ID. Removes the entity and persistence data.
 * 
 * @param id NPC identifier
 */
void despawn(String id);

/**
 * Respawns an existing NPC. Despawns and spawns at the same location.
 * 
 * @param id NPC identifier
 * @throws IllegalArgumentException if NPC not found
 */
void respawn(String id);

/**
 * Moves an NPC to a new location. Despawns and respawns at new position.
 * 
 * @param id NPC identifier
 * @param newLocation New location for the NPC
 * @throws IllegalArgumentException if NPC not found
 */
void move(String id, NpcLocation newLocation);

/**
 * Updates an NPC's display name hologram.
 * 
 * @param id NPC identifier
 * @param displayName Display name (supports MiniMessage format)
 * @throws IllegalArgumentException if NPC not found
 */
void updateDisplayName(String id, String displayName);
```

#### NPC Queries

```java
/**
 * Retrieves an NPC by its unique identifier.
 * 
 * @param id NPC identifier
 * @return Optional containing NPC if found, empty otherwise
 */
Optional<Npc> getNpc(String id);

/**
 * Retrieves an NPC by its entity UUID.
 * 
 * @param entityUuid Entity UUID
 * @return Optional containing NPC if found, empty otherwise
 */
Optional<Npc> getNpcByEntity(UUID entityUuid);

/**
 * Checks if an entity UUID belongs to an NPC.
 * 
 * @param entityUuid Entity UUID to check
 * @return true if entity is an NPC, false otherwise
 */
boolean isNpc(UUID entityUuid);

/**
 * Gets all registered NPCs.
 * 
 * @return Unmodifiable collection of all NPCs
 */
Collection<Npc> getAllNpcs();
```

#### Bulk Operations

```java
/**
 * Despawns all NPCs without removing persistence data.
 * Useful for reloads.
 */
void despawnAll();

/**
 * Loads all NPCs from persistence and spawns them.
 * Chunk-aware: only spawns NPCs in loaded chunks.
 */
void loadAndSpawnAll();

/**
 * Cleans up orphaned holograms from deleted NPCs.
 * Should be called periodically to prevent hologram leaks.
 */
void cleanupOrphanedHolograms();
```

---

### NpcSpawnRequest (Builder Pattern)

Immutable request object for spawning NPCs.

```java
NpcSpawnRequest request = NpcSpawnRequest.builder()
    .id("quest_npc_1")                              // Required: Unique identifier
    .location(location)                             // Required: Spawn location
    .mythicMobInternalName("SkeletonKing")          // Required: MythicMob type
    .modelEngineModelId("questgiver_male")          // Optional: ModelEngine model
    .displayName("<gradient:#00FF00:#FF0000>Quest Giver</gradient>")  // Optional: Hologram name
    .addMetadata("quest_id", "main_quest_1")        // Optional: Custom metadata
    .addMetadata("dialogue_set", "friendly")        // Optional: More metadata
    .invulnerable(true)                             // Optional: Entity flags
    .silent(true)
    .noAI(true)
    .lookAtPlayers(true)
    .build();
```

**Builder Methods:**
- `id(String id)` - Sets NPC identifier (required)
- `location(NpcLocation location)` - Sets spawn location (required)
- `mythicMobInternalName(String name)` - Sets MythicMob type (required)
- `modelEngineModelId(String id)` - Sets ModelEngine model (optional)
- `displayName(String name)` - Sets hologram display name with MiniMessage support (optional)
- `addMetadata(String key, String value)` - Adds custom metadata (optional)
- `metadata(Map<String, String> map)` - Sets entire metadata map (optional)
- `invulnerable(boolean)` - Sets invulnerability flag (optional)
- `silent(boolean)` - Sets silent flag (optional)
- `noAI(boolean)` - Disables entity AI (optional)
- `lookAtPlayers(boolean)` - Makes NPC look at nearby players (optional)

---

### Npc Interface

Represents a spawned NPC with read-only properties.

```java
public interface Npc {
    /**
     * Gets the unique identifier of this NPC.
     * @return NPC identifier
     */
    String getId();
    
    /**
     * Gets the entity UUID of the spawned NPC.
     * @return Entity UUID, or null if not spawned
     */
    UUID getEntityUuid();
    
    /**
     * Gets the current location of this NPC.
     * @return NPC location
     */
    NpcLocation getLocation();
    
    /**
     * Gets the MythicMob internal name.
     * @return MythicMob type
     */
    String getMythicMobInternalName();
    
    /**
     * Gets the ModelEngine model ID.
     * @return Model ID, or null if not using a model
     */
    String getModelEngineModelId();
    
    /**
     * Gets the display name shown in hologram.
     * @return Display name with MiniMessage formatting
     */
    String getDisplayName();
    
    /**
     * Gets custom metadata associated with this NPC.
     * @return Unmodifiable map of metadata key-value pairs
     */
    Map<String, String> getMetadata();
    
    /**
     * Checks if the NPC entity is invulnerable.
     * @return true if invulnerable
     */
    boolean isInvulnerable();
    
    /**
     * Checks if the NPC entity is silent.
     * @return true if silent
     */
    boolean isSilent();
    
    /**
     * Checks if the NPC has AI disabled.
     * @return true if AI disabled
     */
    boolean hasNoAI();
    
    /**
     * Checks if the NPC looks at nearby players.
     * @return true if looking at players
     */
    boolean doesLookAtPlayers();
}
```

**Usage Example:**
```java
Npc npc = npcApi.getNpc("quest_npc_1").orElseThrow();

String id = npc.getId();
UUID entityUuid = npc.getEntityUuid();
NpcLocation location = npc.getLocation();
String mythicMob = npc.getMythicMobInternalName();
String model = npc.getModelEngineModelId();
String displayName = npc.getDisplayName();
Map<String, String> metadata = npc.getMetadata();

// Check metadata
String questId = metadata.get("quest_id");
boolean isInvulnerable = npc.isInvulnerable();
```

---

### NpcLocation

Immutable location data object.

```java
public class NpcLocation {
    /**
     * Creates a new NPC location.
     * 
     * @param worldName World name
     * @param x X coordinate
     * @param y Y coordinate
     * @param z Z coordinate
     * @param yaw Yaw rotation
     * @param pitch Pitch rotation
     */
    public NpcLocation(String worldName, double x, double y, double z, float yaw, float pitch);
    
    public String getWorldName();
    public double getX();
    public double getY();
    public double getZ();
    public float getYaw();
    public float getPitch();
}
```

**Usage Example:**
```java
NpcLocation location = new NpcLocation("world", 100.5, 64.0, 200.5, 90.0f, 0.0f);

// Convert from Bukkit Location
Location bukkitLoc = player.getLocation();
NpcLocation npcLoc = new NpcLocation(
    bukkitLoc.getWorld().getName(),
    bukkitLoc.getX(),
    bukkitLoc.getY(),
    bukkitLoc.getZ(),
    bukkitLoc.getYaw(),
    bukkitLoc.getPitch()
);
```

---

## 🎯 Events API

AethorNPCS fires four main Bukkit events that plugins can listen to.

### AethorNpcInteractEvent

Fired when a player interacts with an NPC (right-click or left-click).

```java
import com.aethor.aethornpcs.plugin.event.AethorNpcInteractEvent;
import com.aethor.aethornpcs.api.payload.InteractPayload;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestListener implements Listener {
    
    @EventHandler
    public void onNpcInteract(AethorNpcInteractEvent event) {
        Player player = event.getPlayer();
        Npc npc = event.getNpc();
        InteractPayload payload = event.getPayload();
        
        // Get interaction details
        InteractPayload.ClickType clickType = payload.getClickType();  // RIGHT_CLICK or LEFT_CLICK
        long timestamp = payload.getTimestamp();
        
        // Check if this is a quest NPC
        if (npc.getMetadata().containsKey("quest_id")) {
            String questId = npc.getMetadata().get("quest_id");
            
            if (clickType == InteractPayload.ClickType.RIGHT_CLICK) {
                // Handle quest interaction
                openQuestDialogue(player, questId);
            } else {
                // Handle left-click (e.g., combat check)
                checkQuestCombatRequirement(player, questId);
            }
        }
        
        // Cancel event to prevent further processing
        event.setCancelled(true);
    }
}
```

**Event Properties:**
- `getPlayer()` - Player who interacted
- `getNpc()` - NPC that was interacted with
- `getPayload()` - Interaction details
- `isCancelled()` / `setCancelled(boolean)` - Event cancellation

**InteractPayload Properties:**
- `getClickType()` - `RIGHT_CLICK` or `LEFT_CLICK`
- `getTimestamp()` - System timestamp of interaction

---

### AethorNpcProximityEvent

Fired when a player enters or exits an NPC's proximity radius.

```java
import com.aethor.aethornpcs.plugin.event.AethorNpcProximityEvent;
import com.aethor.aethornpcs.api.payload.ProximityPayload;

@EventHandler
public void onNpcProximity(AethorNpcProximityEvent event) {
    Player player = event.getPlayer();
    Npc npc = event.getNpc();
    ProximityPayload payload = event.getPayload();
    
    // Get proximity details
    ProximityPayload.ProximityAction action = payload.getAction();  // ENTER or EXIT
    double distance = payload.getDistance();
    
    if (action == ProximityPayload.ProximityAction.ENTER) {
        // Player entered proximity
        if (npc.getMetadata().containsKey("greeting")) {
            String greeting = npc.getMetadata().get("greeting");
            player.sendMessage(greeting);
        }
        
        // Play sound or show title
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1.0f, 1.0f);
        
    } else {
        // Player exited proximity
        player.sendActionBar(Component.text("You walk away from " + npc.getId()));
    }
}
```

**Event Properties:**
- `getPlayer()` - Player in proximity
- `getNpc()` - NPC being approached/left
- `getPayload()` - Proximity details

**ProximityPayload Properties:**
- `getAction()` - `ENTER` or `EXIT`
- `getDistance()` - Current distance between player and NPC

**Configuration:**
```yaml
proximity:
  enabled: true
  radius: 3.0        # Detection radius in blocks
  periodTicks: 10    # Check frequency (10 ticks = 0.5 seconds)
```

---

### AethorNpcSpawnEvent

Fired when an NPC is spawned.

```java
import com.aethor.aethornpcs.plugin.event.AethorNpcSpawnEvent;
import com.aethor.aethornpcs.api.payload.SpawnPayload;

@EventHandler
public void onNpcSpawn(AethorNpcSpawnEvent event) {
    Npc npc = event.getNpc();
    SpawnPayload payload = event.getPayload();
    
    SpawnPayload.SpawnCause cause = payload.getCause();  // PLUGIN_LOAD, CHUNK_LOAD, MANUAL, API
    
    // Log spawn
    plugin.getLogger().info("NPC " + npc.getId() + " spawned due to: " + cause);
    
    // Apply custom effects
    if (cause == SpawnPayload.SpawnCause.CHUNK_LOAD) {
        // NPC respawned due to chunk load
        checkQuestStateForNearbyPlayers(npc);
    }
}
```

**Event Properties:**
- `getNpc()` - NPC that was spawned
- `getPayload()` - Spawn details

**SpawnPayload Properties:**
- `getCause()` - `PLUGIN_LOAD`, `CHUNK_LOAD`, `MANUAL`, or `API`

---

### AethorNpcDespawnEvent

Fired when an NPC is despawned.

```java
import com.aethor.aethornpcs.plugin.event.AethorNpcDespawnEvent;
import com.aethor.aethornpcs.api.payload.DespawnPayload;

@EventHandler
public void onNpcDespawn(AethorNpcDespawnEvent event) {
    Npc npc = event.getNpc();
    DespawnPayload payload = event.getPayload();
    
    DespawnPayload.DespawnCause cause = payload.getCause();  // CHUNK_UNLOAD, REMOVAL, API
    
    // Log despawn
    plugin.getLogger().info("NPC " + npc.getId() + " despawned due to: " + cause);
    
    // Clean up quest states
    if (cause == DespawnPayload.DespawnCause.REMOVAL) {
        // NPC was permanently removed
        cleanupQuestReferencesToNpc(npc.getId());
    }
}
```

**Event Properties:**
- `getNpc()` - NPC that was despawned
- `getPayload()` - Despawn details

**DespawnPayload Properties:**
- `getCause()` - `CHUNK_UNLOAD`, `REMOVAL`, or `API`

---

## 💡 Complete Usage Examples

### Example 1: Quest NPC System

```java
public class QuestNpcManager {
    private final AethorNpcApi npcApi;
    private final QuestManager questManager;
    
    public void createQuestGiver(String questId, Location location) {
        NpcLocation npcLoc = new NpcLocation(
            location.getWorld().getName(),
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch()
        );
        
        NpcSpawnRequest request = NpcSpawnRequest.builder()
            .id("quest_giver_" + questId)
            .location(npcLoc)
            .mythicMobInternalName("SkeletonKing")
            .modelEngineModelId("questgiver_male")
            .displayName("<gradient:#FFD700:#FFA500>Quest Giver</gradient>")
            .addMetadata("quest_id", questId)
            .addMetadata("type", "quest_giver")
            .invulnerable(true)
            .silent(true)
            .noAI(true)
            .lookAtPlayers(true)
            .build();
        
        Npc npc = npcApi.spawn(request);
    }
    
    @EventHandler
    public void onInteract(AethorNpcInteractEvent event) {
        Npc npc = event.getNpc();
        Player player = event.getPlayer();
        
        if (npc.getMetadata().get("type").equals("quest_giver")) {
            String questId = npc.getMetadata().get("quest_id");
            
            if (event.getPayload().getClickType() == InteractPayload.ClickType.RIGHT_CLICK) {
                questManager.startQuestDialogue(player, questId);
                event.setCancelled(true);
            }
        }
    }
}
```

### Example 2: Merchant System

```java
public class MerchantNpcManager {
    private final AethorNpcApi npcApi;
    
    public void createMerchant(String merchantId, Location loc, String shopType) {
        NpcLocation npcLoc = convertLocation(loc);
        
        NpcSpawnRequest request = NpcSpawnRequest.builder()
            .id("merchant_" + merchantId)
            .location(npcLoc)
            .mythicMobInternalName("Villager")
            .modelEngineModelId("merchant_model")
            .displayName("<#00FF00>Merchant")
            .addMetadata("type", "merchant")
            .addMetadata("shop_type", shopType)
            .invulnerable(true)
            .lookAtPlayers(true)
            .build();
        
        npcApi.spawn(request);
    }
    
    @EventHandler
    public void onProximity(AethorNpcProximityEvent event) {
        Npc npc = event.getNpc();
        
        if ("merchant".equals(npc.getMetadata().get("type"))) {
            if (event.getPayload().getAction() == ProximityPayload.ProximityAction.ENTER) {
                event.getPlayer().sendMessage("§aWelcome to my shop!");
            }
        }
    }
    
    @EventHandler
    public void onInteract(AethorNpcInteractEvent event) {
        Npc npc = event.getNpc();
        
        if ("merchant".equals(npc.getMetadata().get("type"))) {
            String shopType = npc.getMetadata().get("shop_type");
            openShopGUI(event.getPlayer(), shopType);
            event.setCancelled(true);
        }
    }
}
```

### Example 3: Dynamic NPC State Updates

```java
public class DynamicNpcManager {
    private final AethorNpcApi npcApi;
    
    // Update NPC based on server state
    public void updateQuestNpcState(String npcId, boolean questCompleted) {
        npcApi.getNpc(npcId).ifPresent(npc -> {
            if (questCompleted) {
                // Change display name to show completed state
                npcApi.updateDisplayName(npcId, "<#808080>Quest Complete");
                
                // Move NPC to different location
                NpcLocation newLoc = new NpcLocation("world", 150, 64, 250, 0, 0);
                npcApi.move(npcId, newLoc);
            } else {
                npcApi.updateDisplayName(npcId, "<#FFD700>Active Quest");
            }
        });
    }
    
    // Bulk update all NPCs
    public void reloadAllNpcs() {
        npcApi.despawnAll();
        npcApi.loadAndSpawnAll();
    }
    
    // Periodic cleanup
    public void performMaintenance() {
        npcApi.cleanupOrphanedHolograms();
    }
}
```

---

## 🎨 Display Name Formatting

AethorNPCS supports full MiniMessage formatting for NPC display names via DecentHolograms.

### Supported Formats

```java
// Solid colors
"<red>Red Name"
"<#FF0000>Hex Color Name"

// Gradients
"<gradient:#00FF00:#FF0000>Rainbow Gradient"
"<gradient:red:blue:green>Multi-Color Gradient"

// Rainbow
"<rainbow>Rainbow Text</rainbow>"
"<rainbow:!>Reversed Rainbow"
"<rainbow:2>Phase 2 Rainbow"

// Bold, Italic, Underline, Strikethrough
"<bold>Bold Name</bold>"
"<italic>Italic Name</italic>"
"<underlined>Underlined</underlined>"
"<strikethrough>Strikethrough</strikethrough>"

// Combined
"<gradient:#FFD700:#FFA500><bold>Golden Quest Giver</bold></gradient>"
```

### Updating Display Names

```java
// Update via API
npcApi.updateDisplayName("quest_npc_1", "<gradient:#00FF00:#0000FF>Updated Name</gradient>");

// Update via command
/npc setname quest_npc_1 <gradient:#FF0000:#00FF00>Gradient Name</gradient>
```

---

## ⚠️ Best Practices

### 1. Always Check for API Availability

```java
if (npcApi == null) {
    getLogger().warning("AethorNPCS API not available");
    return;
}
```

### 2. Use Metadata for Custom Data

```java
// Store quest state
.addMetadata("quest_id", "main_quest_1")
.addMetadata("quest_step", "2")
.addMetadata("dialogue_set", "friendly")
.addMetadata("custom_data", jsonData)
```

### 3. Handle Optional Returns Safely

```java
npcApi.getNpc("quest_npc_1").ifPresentOrElse(
    npc -> handleNpc(npc),
    () -> getLogger().warning("NPC not found")
);
```

### 4. Cancel Events When Handled

```java
@EventHandler
public void onInteract(AethorNpcInteractEvent event) {
    // Handle interaction
    openQuestMenu(event.getPlayer());
    
    // Prevent further processing
    event.setCancelled(true);
}
```

### 5. Clean Up on Disable

```java
@Override
public void onDisable() {
    // Clean up any cached NPC references
    npcCache.clear();
}
```

---

## 🔧 Advanced Features

### Chunk-Aware Spawning

NPCs automatically despawn when their chunk unloads and respawn when the chunk loads. This is handled internally and requires no additional code.

```yaml
chunkStrategy: DESPAWN_ON_UNLOAD  # Recommended
```

### Collision Control

NPCs are automatically added to a no-collision scoreboard team to prevent players from pushing them.

### Hologram Integration

DecentHolograms is automatically used for display names when available. The plugin converts MiniMessage format to DecentHolograms format internally.

### Persistent Metadata

All metadata is automatically saved to `npcs.yml` and restored on server restart.

---

## 📁 NPC Persistence

NPCs are automatically saved to `plugins/AethorNPCS/npcs.yml`:

```yaml
npcs:
  questgiver_1:
    world: world
    x: 100.5
    y: 64.0
    z: 200.5
    yaw: 90.0
    pitch: 0.0
    mythicMob: SkeletonKing
    model: questgiver_male
    displayName: "<gradient:#FFD700:#FFA500>Quest Giver</gradient>"
    metadata:
      quest_id: main_quest_1
      npc_type: questgiver
    flags:
      invulnerable: true
      silent: true
      noAI: true
      lookAtPlayers: true
```

You can manually edit this file while the server is offline.

---

## 🚀 Running AethorNPCS Locally

### 1. Set Up Test Server

```bash
# Create test server directory
mkdir test-server
cd test-server

# Download Paper 1.21.4+
wget https://api.papermc.io/v2/projects/paper/versions/1.21.4/builds/[BUILD]/downloads/paper-1.21.4-[BUILD].jar -O paper.jar

# Accept EULA
echo "eula=true" > eula.txt
```

### 2. Install Dependencies

- Download **MythicMobs** from https://www.spigotmc.org/resources/mythicmobs.5702/
- (Optional) Download **ModelEngine** from https://mythiccraft.io/index.php?resources/model-engine—ultimate-entity-model-manager.389/
- Place JARs in `plugins/` folder

### 3. Install AethorNPCS

```bash
# Copy built plugin
cp ../aethornpcs-plugin/target/AethorNPCS-1.0.0-SNAPSHOT.jar plugins/
```

### 4. Start Server

```bash
java -Xmx2G -Xms2G -jar paper.jar nogui
```

### 5. Test In-Game

1. Join the server
2. Give yourself admin: `/op YourUsername`
3. Create a test NPC: `/npc create test_npc SkeletonKing`
4. Interact with the NPC (right-click)
5. Check logs for event firing

### 6. Verify API

Create a simple test plugin:

```java
@EventHandler
public void onNpcInteract(AethorNpcInteractEvent event) {
    event.getPlayer().sendMessage("You clicked NPC: " + event.getNpc().getId());
}
```

## 🔍 Debugging

Enable debug logging in `config.yml`:

```yaml
debugLogging: true
```

This will log:
- NPC spawn/despawn operations
- Entity UUID linkage
- Proximity detection enter/exit
- Interaction events
- Chunk load/unload NPC handling

## 📝 License

Copyright (c) 2026 Aethor. All rights reserved.

This software is proprietary and confidential. Unauthorized copying, modification, distribution, or use of this software, via any medium, is strictly prohibited without express written permission from Aethor.

## 🤝 Contributing

Contributions are welcome! This is production-ready code, but improvements are always possible:

- Additional integration adapters
- Performance optimizations
- More event types
- Extended API methods

## 📞 Support

For issues, questions, or feature requests, please refer to the project documentation or create an issue in your version control system.

---

**Omega Brain Rize Moment**
