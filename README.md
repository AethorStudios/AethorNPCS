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

## 📚 API Usage for Quest Plugins

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

### Spawning NPCs Programmatically

```java
import com.aethor.aethornpcs.api.dto.NpcLocation;
import com.aethor.aethornpcs.api.dto.NpcSpawnRequest;
import com.aethor.aethornpcs.api.dto.Npc;

// Create spawn request
NpcLocation location = new NpcLocation("world", 100.5, 64.0, 200.5, 90.0f, 0.0f);

NpcSpawnRequest request = NpcSpawnRequest.builder()
    .id("quest_npc_1")
    .location(location)
    .mythicMobInternalName("SkeletonKing")
    .modelEngineModelId("questgiver_male")
    .addMetadata("quest_id", "main_quest_1")
    .addMetadata("dialogue_set", "friendly")
    .invulnerable(true)
    .silent(true)
    .noAI(true)
    .lookAtPlayers(true)
    .build();

// Spawn the NPC
Npc npc = npcApi.spawn(request);
```

### Querying NPCs

```java
import java.util.Optional;
import java.util.UUID;

// Get NPC by ID
Optional<Npc> npc = npcApi.getNpc("quest_npc_1");

// Get NPC by entity UUID
UUID entityUuid = entity.getUniqueId();
Optional<Npc> npc = npcApi.getNpcByEntity(entityUuid);

// Check if entity is an NPC
boolean isNpc = npcApi.isNpc(entityUuid);

// Get all NPCs
Collection<Npc> allNpcs = npcApi.getAllNpcs();
```

### Reading NPC Data

```java
Npc npc = npcApi.getNpc("quest_npc_1").orElseThrow();

String id = npc.getId();
UUID entityUuid = npc.getEntityUuid();
NpcLocation location = npc.getLocation();
String mythicMob = npc.getMythicMobInternalName();
String model = npc.getModelEngineModelId();
Map<String, String> metadata = npc.getMetadata();

// Check metadata
String questId = metadata.get("quest_id");
```

### Listening to NPC Events

```java
import com.aethor.aethornpcs.plugin.event.AethorNpcInteractEvent;
import com.aethor.aethornpcs.plugin.event.AethorNpcProximityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class QuestListener implements Listener {
    
    @EventHandler
    public void onNpcInteract(AethorNpcInteractEvent event) {
        Player player = event.getPlayer();
        Npc npc = event.getNpc();
        InteractPayload payload = event.getPayload();
        
        // Check if this is a quest NPC
        if (npc.getMetadata().containsKey("quest_id")) {
            String questId = npc.getMetadata().get("quest_id");
            
            if (payload.getClickType() == InteractPayload.ClickType.RIGHT_CLICK) {
                // Handle quest interaction
                openQuestDialogue(player, questId);
            }
        }
    }
    
    @EventHandler
    public void onNpcProximity(AethorNpcProximityEvent event) {
        Player player = event.getPlayer();
        Npc npc = event.getNpc();
        ProximityPayload payload = event.getPayload();
        
        if (payload.getAction() == ProximityPayload.ProximityAction.ENTER) {
            // Player entered proximity
            player.sendMessage("You approach " + npc.getId());
        } else {
            // Player exited proximity
            player.sendMessage("You leave " + npc.getId());
        }
    }
}
```

### Maven Dependency (for Quest Plugins)

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
        <version>1.0.0-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Then place `aethornpcs-api-1.0.0-SNAPSHOT.jar` in your project's `libs/` folder, or install it to your local Maven repository:

```bash
mvn install:install-file \
  -Dfile=aethornpcs-api-1.0.0-SNAPSHOT.jar \
  -DgroupId=com.aethor \
  -DartifactId=aethornpcs-api \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackaging=jar
```

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

This project is provided as-is for educational and production use.

## 🤝 Contributing

Contributions are welcome! This is production-ready code, but improvements are always possible:

- Additional integration adapters
- Performance optimizations
- More event types
- Extended API methods

## 📞 Support

For issues, questions, or feature requests, please refer to the project documentation or create an issue in your version control system.

---

**Built with ❤️ for the Minecraft Paper community**
