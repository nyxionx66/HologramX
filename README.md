# HologramX

Advanced hologram plugin for Minecraft Paper 1.21+ servers using display entities.

## Features

### 🎯 **Core Functionality**
- **Multiple Hologram Types**: TEXT, ITEM, BLOCK displays
- **Display Entity Technology**: Uses modern display entities (1.19.4+) for optimal performance
- **Rich Text Formatting**: Full MiniMessage support with gradients, colors, and styling
- **PlaceholderAPI Integration**: Dynamic content with placeholder support
- **Advanced Visibility System**: Permission-based, world-based, and distance-based visibility

### 🎨 **Display Features**
- **Scaling & Translation**: Precise control over hologram size and position
- **Billboard Modes**: Fixed, vertical, horizontal, center alignment options
- **Text Alignment**: Left, center, right text alignment
- **Shadow Effects**: Configurable shadow radius and strength
- **Background Colors**: Transparent or custom colored backgrounds
- **Text Effects**: Shadow, see-through, and other visual enhancements

### ⚡ **Performance Optimizations**
- **Chunk-based Loading**: Holograms load/unload with chunks
- **View Distance Culling**: Automatic hiding beyond configured distances
- **Async Operations**: Database and heavy operations run asynchronously
- **Memory Management**: Efficient entity cleanup and garbage collection

### 🛠 **Management System**
- **Persistent Storage**: YAML file or MySQL/SQLite database storage
- **Easy Commands**: Intuitive command system for all operations
- **Import/Export**: Backup and migration functionality
- **Live Editing**: Real-time hologram editing without restarts

## Commands

### Admin Commands (`/hologramx`, `/hx`)
```
/hx create <name> <type>        - Create a new hologram
/hx delete <name>               - Delete a hologram
/hx list [page]                 - List all holograms
/hx info <name>                 - Show hologram information
/hx move <name>                 - Move hologram to your location
/hx toggle <name>               - Toggle hologram visibility
/hx clone <name> <new_name>     - Clone existing hologram
/hx near [distance]             - Show nearby holograms
/hx tp <name>                   - Teleport to hologram
/hx text <name> <action> [args] - Edit hologram text
/hx reload                      - Reload plugin configuration
```

### Text Editing Commands
```
/hx text <name> add <text>         - Add text line
/hx text <name> remove <line>      - Remove text line
/hx text <name> clear              - Clear all text lines
```

### User Commands (`/hologram`, `/holo`)
```
/holo list [page]              - List visible holograms
/holo info <name>              - Show hologram info
/holo near [distance]          - Show nearby holograms
/holo tp <name>                - Teleport to hologram (if permitted)
```

## Configuration

### Example Hologram Configuration
```yaml
welcomeServer:
  type: TEXT
  location:
    world: the_map
    x: -3.5
    y: 66.11650085449219
    z: 0.5
    yaw: -90.0
    pitch: 0.0
  visibility_distance: -1
  visibility: ALL
  persistent: true
  scale_x: 1.5
  scale_y: 1.5
  scale_z: 1.5
  translation_x: 0.0
  translation_y: 0.0
  translation_z: 0.0
  shadow_radius: 0.0
  shadow_strength: 1.0
  text:
    - "<gradient:#FFD700:#FFA500><b>WELCOME, %player_name%!</b></gradient>"
    - "<gray>You have entered the</gray> <bold><gradient:red:dark_red>ANARCHY MINES</gradient></bold>"
    - "<white>The rule is simple: <red><b>SURVIVE.</b></red></white>"
  text_shadow: false
  see_through: false
  text_alignment: center
  update_text_interval: -1
  background: transparent
  billboard: fixed
```

### Main Configuration Options
- **Database**: YAML, SQLite, or MySQL storage
- **Performance**: View distance, update intervals, chunk loading
- **Defaults**: Default settings for new holograms
- **PlaceholderAPI**: Integration settings and cache options
- **Animations**: Animation system configuration

## Permissions

```yaml
hologramx.*              - Full administrative access
hologramx.admin          - Administrative access
hologramx.use            - Basic command access
hologramx.user           - User command access
hologramx.create         - Create holograms
hologramx.delete         - Delete holograms
hologramx.edit           - Edit holograms
hologramx.move           - Move holograms
hologramx.reload         - Reload plugin
hologramx.interact.*     - Interact with all holograms
hologramx.view.*         - View all holograms
hologramx.interact.<id>  - Interact with specific hologram
hologramx.view.<id>      - View specific hologram
```

## Developer API

```java
// Get API instance
HologramXAPI api = HologramX.getAPI();

// Create text hologram
Hologram hologram = api.createTextHologram("my_hologram", location);
hologram.getTextLines().add("<gold>Hello World!</gold>");
hologram.spawn();

// Get existing hologram
Hologram existing = api.getHologram("my_hologram");

// Get nearby holograms
List<Hologram> nearby = api.getNearbyHolograms(location, 50.0);
```

## Installation

1. **Requirements**:
   - Paper 1.21+ server
   - Java 21+
   - PlaceholderAPI (optional but recommended)

2. **Installation**:
   - Place `HologramX.jar` in your plugins folder
   - Restart the server
   - Configure `config.yml` and `messages.yml` as needed
   - Use `/hx reload` to apply configuration changes

## Building

```bash
# Clone the repository
git clone https://github.com/yourname/HologramX.git
cd HologramX

# Build with Maven
mvn clean package

# The plugin JAR will be in target/HologramX-1.0.0.jar
```

## Support

- **Issues**: Report bugs on GitHub Issues
- **Wiki**: Comprehensive documentation on the wiki
- **Discord**: Join our Discord server for support

## License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**HologramX** - Advanced hologram plugin for modern Minecraft servers