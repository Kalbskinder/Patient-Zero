# Patient-Zero
Patient-Zero is a remake of the popular game Infection on Hypixel. There are 3 roles. Patient-Zero, Corrupted and Survivor.

## Overview
- Commands
- Permissions
- Configuration

## Commands
A list of available commands that the plugin adds
<br><br>
![List of commands](https://cdn.modrinth.com/data/5RQMBtQG/images/a8cb401775f6b95bbe4072deaf7d0703eed8c057.png)

- `/ptz help`
- `/ptz createmap <map-name> <x1> <y1> <z1> <x2> <y2> <z2>`
- `/ptz deletemap <map-name>`
- `/ptz addspawn <map-name> <role>`
- `/ptz setqueue-spawn <map-name>`
- `/ptz setqueue-limit <map-name> <int-max>`
- `/ptz listmaps`
- `/ptz join <map-name>`
- `/ptz leave`

## Permissions
```yml
permissions:
  ptz.join:
    description: Allows a player to run '/ptz join <map-name>'
    default: true
  ptz.leave:
    description: Allows a player to run '/ptz leave'
    default: true

  ptz.admin:
    description: Allows a player to access all of the '/ptz <args>' commands.
```

## Configuration
```yml
messages:
  prefix: "§f[§3Game§f] " # "[ptz] " colored

  playerjoin: "§a+ §7You joined the queue §e(§b%player-count%§e/§b%max-player-count%§e)"
  playerleave: "§c- §7You left the queue §e(§b%player-count%§e/§b%max-player-count%§e)"

  broadcast-playerjoin: "§a%player% joined the queue §e(§b%player-count%§e/§b%max-player-count%§e)"
  broadcast-playerleave: "§e%player% left the queue §e(§b%player-count%§e/§b%max-player-count%§e)"
  end-message:
    - "§6§l--------------------------------------------"
    - "               §e§lPatient-Zero                 "
    - "               §fWinners: %winnsers%            "
    - "                                                "
    - "§6§l--------------------------------------------"

titles:
  roles:
    survivor:
      title: "§aSurvivor"
      subtitle: "§eStay alive"
    patientzero:
      title: "§cPatient-Zero"
      subtitle: "§eKill other players"
    corrupted:
      title: "§cCorrupted"
      subtitle: "§eYou have been corrupted!"
  win:
    title: "§c§lVICTORY!"
    subtitle: ""
  lose:
    title: "§c§lYou lost!"
    subtitle: ""

scoreboard:
  title: "§e§lPatient-Zero"
  lines:
    - "§a "
    - "Role: %role%"
    - "§b "
    - "Time left: §a%timer%"
    - "§c "
    - "Survivors: §a%survivors%"
    - "Corrupted: §c%corrupted%"
    - "§d "
    - "Kills: §a%kills%"
    - "§e "
    - "Map: §a%map%"

settings:
  executes:
    playerOnLeaveQueue: "/me Teleport me!" # Replace with /hub or /spawn
    afterGameOver: "/me Teleport me!" # Replace with /hub or /spawn

  quit-item:
    item: "minecraft:magma_cream"
    name: "§cQuit §7(Right-click)"
    lore:
      - "§eRight-click while holding the item to leave the queue."

  double-jump:
    enabled: false
    velocity: 0.5
    cooldown: 10 # Cooldown in seconds
    cooldown-message: "§cYou must wait §e%time% &cseconds before double jumping again!"

# Your maps will be saved here
maps: []

```
