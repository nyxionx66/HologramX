# HologramX

Advanced hologram plugin for Minecraft Paper 1.21+ servers using display entities.

## Features

### 🎯 **Core Functionality**
- **Multiple Hologram Types**: TEXT, ITEM, BLOCK displays
- **Display Entity Technology**: Uses modern display entities (1.19.4+) for optimal performance
- **Rich Text Formatting**: Full MiniMessage support with gradients, colors, and styling
- **PlaceholderAPI Integration**: Dynamic content with placeholder support
- **Advanced Visibility System**: Permission-based, world-based, and distance-based visibility

## Advanced Features

### 🎨 **Color System**
- **Named Colors**: red, blue, green, yellow, purple, orange, pink, cyan, etc.
- **Extended Names**: dark_red, light_blue, dark_purple, light_gray, etc.
- **Hex Colors**: #FF0000, #00FF00, #0000FF, #AARRGGBB (with alpha)
- **RGB Format**: rgb(255,0,0), rgb(0,255,0), rgb(0,0,255)
- **Transparency**: transparent keyword for no background
- **Tab Completion**: Smart suggestions for all color formats

### 📝 **Command Examples**

#### Color Usage Examples
```bash
# Named colors
/hx edit welcomeServer background red
/hx edit welcomeServer background dark_blue

# Hex colors
/hx edit welcomeServer background #FF0000
/hx edit welcomeServer background #80FF0000  # Semi-transparent red

# RGB colors
/hx edit welcomeServer background rgb(255,0,0)
/hx edit welcomeServer background rgb(128,255,128)

# Transparent background
/hx edit welcomeServer background transparent
```

#### Edit Command Examples
```bash
# Position and rotation
/hx edit myHolo moveHere
/hx edit myHolo moveTo 100 64 200 90 0
/hx edit myHolo rotate 45
/hx edit myHolo scale 1.5

# Text editing
/hx edit myHolo addLine "<gold>New line of text</gold>"
/hx edit myHolo setLine 1 "<red>Updated first line</red>"
/hx edit myHolo insertAfter 2 "<blue>Inserted line</blue>"

# Visual properties
/hx edit myHolo background rgb(64,128,255)
/hx edit myHolo textShadow true
/hx edit myHolo billboard center
/hx edit myHolo visibility PERMISSION_NEEDED

# Auto-refresh for placeholders
/hx edit myHolo updateTextInterval 20t    # 20 ticks
/hx edit myHolo updateTextInterval 5s     # 5 seconds  
/hx edit myHolo updateTextInterval 2m     # 2 minutes
```

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

#### Basic Management
```
/hx create <name> <type>        - Create a new hologram
/hx delete <name>               - Delete a hologram
/hx list [page]                 - List all holograms
/hx info <name>                 - Show hologram information
/hx toggle <name>               - Toggle hologram visibility
/hx clone <name> <new_name>     - Clone existing hologram
/hx near [distance]             - Show nearby holograms
/hx tp <name>                   - Teleport to hologram
/hx reload                      - Reload plugin configuration
```

#### Unified Edit Command
```
/hx edit <name> <property> [args...]  - Edit hologram properties
```

**General Properties:**
```
/hx edit <name> moveHere                           - Move to your location
/hx edit <name> moveTo <x> <y> <z> [yaw] [pitch]  - Set exact position
/hx edit <name> rotate <degrees>                   - Rotate Y-axis
/hx edit <name> rotatePitch <degrees>              - Rotate X-axis
/hx edit <name> visibilityDistance <distance>      - Set view distance
/hx edit <name> visibility <ALL|MANUAL|PERMISSION_NEEDED>  - Set visibility
/hx edit <name> scale <factor>                     - Resize hologram
/hx edit <name> billboard <center|fixed|vertical|horizontal>  - Set orientation
/hx edit <name> shadowStrength <value>             - Shadow quality
/hx edit <name> shadowRadius <radius>              - Shadow spread
```

**Text Properties:**
```
/hx edit <name> setLine <line> <text...>           - Replace text line
/hx edit <name> addLine <text...>                  - Add new text line
/hx edit <name> removeLine <line>                  - Remove text line
/hx edit <name> insertBefore <line> <text...>      - Insert line above
/hx edit <name> insertAfter <line> <text...>       - Insert line below
/hx edit <name> clearText                          - Clear all text
/hx edit <name> background <color>                 - Set background color
/hx edit <name> textShadow <true|false>            - Toggle text shadow
/hx edit <name> textAlignment <center|left|right>  - Set text alignment
/hx edit <name> updateTextInterval <time>          - Set auto-refresh rate
```

#### Direct Commands (Legacy Support)
All edit commands are also available as direct commands:
```
/hx moveHere <name>             - Move hologram to your location
/hx addLine <name> <text...>    - Add text line
/hx scale <name> <factor>       - Scale hologram
# ... and all other edit commands
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