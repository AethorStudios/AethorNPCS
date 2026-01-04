# Quick Start Guide - AethorNPCS

This guide will get you up and running with AethorNPCS in under 5 minutes.

## Step 1: Build the Plugin

### Windows:
```batch
build.bat
```

### Linux/Mac:
```bash
chmod +x build.sh
./build.sh
```

Or manually:
```bash
mvn clean package -pl aethornpcs-plugin -am
```

## Step 2: Install on Server

1. Copy `aethornpcs-plugin/target/AethorNPCS-1.0.0-SNAPSHOT.jar` to your Paper server's `plugins/` folder
   - ⚠️ **Important:** Do NOT copy the `aethornpcs-api` jar - it's already included
2. Ensure **MythicMobs** is installed
3. (Optional) Install **ModelEngine** for model support
4. Start/restart server

## Step 3: Create Your First NPC

1. Join your server as an operator
2. Stand where you want the NPC
3. Run: `/npc create my_first_npc SkeletonKing`

That's it! You now have a functional NPC.

## Step 4: Test Interactions

Right-click the NPC - check your server console for interaction logs.

If you have `debugLogging: true` in config, you'll see detailed logs.

## Step 5: Create a Quest Plugin (Optional)

### 1. Add dependency to your quest plugin's `pom.xml`:

```xml
<dependency>
    <groupId>com.aethor</groupId>
    <artifactId>aethornpcs-api</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

### 2. Get the API in your plugin:

```java
import com.aethor.aethornpcs.api.AethorNpcApi;
import org.bukkit.Bukkit;

@Override
public void onEnable() {
    RegisteredServiceProvider<AethorNpcApi> provider = 
        Bukkit.getServicesManager().getRegistration(AethorNpcApi.class);
    
    if (provider == null) {
        getLogger().severe("AethorNPCS not found!");
        return;
    }
    
    AethorNpcApi api = provider.getProvider();
    getLogger().info("Hooked into AethorNPCS!");
}
```

### 3. Listen to NPC events:

```java
@EventHandler
public void onNpcInteract(AethorNpcInteractEvent event) {
    Player player = event.getPlayer();
    Npc npc = event.getNpc();
    
    player.sendMessage("You clicked: " + npc.getId());
    
    // Your quest logic here
}
```

## Common Commands

```bash
# Create NPC with model
/npc create guard_1 SkeletonKing royal_guard

# List all NPCs
/npc list

# Move NPC to your location
/npc move guard_1

# Remove NPC
/npc remove guard_1

# Respawn NPC (useful after changes)
/npc respawn guard_1
```

## Configuration Quick Reference

**config.yml**:
```yaml
proximity:
  enabled: true      # Enable proximity detection
  radius: 3.0        # Detection radius in blocks
  periodTicks: 10    # Check every N ticks

chunkStrategy: DESPAWN_ON_UNLOAD  # Despawn NPCs when chunk unloads

defaultFlags:
  invulnerable: true   # NPCs can't be damaged
  silent: true         # No entity sounds
  noAI: true          # No AI pathfinding
  lookAtPlayers: true  # NPCs look at nearby players

debugLogging: false   # Enable for troubleshooting
```

## Metadata for Quest Systems

Add custom data to NPCs:

```java
NpcSpawnRequest request = NpcSpawnRequest.builder()
    .id("questgiver_1")
    .location(location)
    .mythicMobInternalName("SkeletonKing")
    .addMetadata("quest_id", "main_quest_1")
    .addMetadata("quest_stage", "introduction")
    .addMetadata("dialogue_set", "friendly")
    .build();

Npc npc = api.spawn(request);
```

Read metadata:
```java
Npc npc = api.getNpc("questgiver_1").orElseThrow();
String questId = npc.getMetadata().get("quest_id");
```

## Troubleshooting

### Plugin won't load
- Check Java version: `java -version` (must be 21)
- Ensure MythicMobs is installed
- Check server logs for errors

### NPC doesn't spawn
- Verify MythicMob type exists: `/mm mobs list`
- Check world is loaded
- Enable debug logging

### Model doesn't appear
- Ensure ModelEngine is installed
- Verify model ID exists: `/meg models`
- Check server logs for model errors

### Events not firing
- Ensure your quest plugin depends on AethorNPCS in `plugin.yml`:
  ```yaml
  depend:
    - AethorNPCS
  ```

## Next Steps

- Read the full [README.md](README.md) for comprehensive documentation
- Check [FILE_TREE.md](FILE_TREE.md) for project structure
- Explore example NPCs in `plugins/AethorNPCS/npcs-example.yml`
- Review API javadocs in the source code

## Getting Help

1. Enable debug logging: `debugLogging: true` in config.yml
2. Check server console for detailed logs
3. Verify MythicMobs and ModelEngine versions are compatible
4. Review API usage examples in README.md

---

**Happy NPC creating! 🎮**
