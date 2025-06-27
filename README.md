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
roles:
  survivor: "<green>Survivor"
  corrupted: "<red>Corrupted"
  patientzero: "<red>Patient Zero"

messages:
  prefix: ""

  ptz-dead: "<red><bold>Patient-Zero died! <reset><red>It was %player%"
  ptz-dead-info: "<yellow>Corrupted players will no longer respawn!"

  gamestart: "<yellow>Game is starting in <red>%time%<yellow>s"

  roleassign: "<yellow>Roles are assigned in <red>%time%<yellow>s"

  playerjoin: "<green>+ <gray>You joined the queue (<green>%player-count%<gray>/<green>%max-player-count%<gray>)"
  playerleave: "<red>- <gray>You left the queue (<red>%player-count%<gray>/<green>%max-player-count%<gray>)"

  broadcast-playerjoin: "<green>+ <yellow>%player% <gray>joined the queue (<green>%player-count%<gray>/<green>%max-player-count%<gray>)"
  broadcast-playerleave: "<red>- <yellow>%player% <gray>left the queue (<red>%player-count%<gray>/<green>%max-player-count%<gray>)"

  winners:
    survivors: "<green>Survivors"
    ptz: "<red>Corrupted"

  # <center> - A custom tag to center text (only works with the end-message)
  end-message:
    - "<green><bold>--------------------------------------"
    - " "
    - "<center><yellow><bold>Patient-Zero              "
    - " "
    - "<center><white>Winners: %winners%          "
    - "<center><white>Kills: <green>%kills%           "
    - " "
    - "<green><bold>--------------------------------------"

titles:
  roles:
    survivor:
      title: "<green>ROLE: Survivor"
      subtitle: "<yellow>Stay alive as long as possible!"
    patientzero:
      title: "<red>ROLE: Patient-Zero"
      subtitle: "<yellow>Secretly corrupt other players!"
    corrupted:
      title: "<red>Corrupted"
      subtitle: "<yellow>You have been corrupted!"
    corrupted-respawn:
      title: "<red>You died!"
      subtitle: "<yellow>Respawning in <red>%time%s<yellow>!" # %time% - Returns the time until the player respawns

    # Title for when the player can no longer respawn
    final-death:
      title: "<red>You died!"
      subtitle: "<yellow>You can't respawn anymore!"
  win:
    title: "<green>YOU WIN!"
    subtitle: ""
  lose:
    title: "<red>YOU LOSE!"
    subtitle: ""

scoreboard:
  title: "<yellow><bold>Patient-Zero"
  lines:
    - "<green> "
    - "Role: %role%"
    - "<red> "
    - "Time left: <green>%timer%"
    - "<dark_green> "
    - "Survivors: <green>%survivors%"
    - "Corrupted: <red>%corrupted%"
    - "<dark_red> "
    - "Kills: <green>%kills%"
    - "<yellow> "
    - "Map: <green>%map%"
settings:
  gametime: 120

  executes:
    playerOnLeaveQueue: "/me Teleport me!"
    playerOnGameEnd: "/me Teleport me!"
    playerOnFinalDeath: "/me Teleport me!"

  quit-item:
    item: "minecraft:red_bed"
    name: "<red>Quit <gray>(Right-click)"
    amount: 1
    slot: 8 # Hotbar slot 9
    lore:
      - "<aqua> "
      - "<yellow>Right-click while holding the item"
      - "<yellow>to leave the queue."

  game-items:
    # Survivor Role
    survivor:
      bow:
        name: "<green>Basic Bow"
        lore:
          - "<gray>Defend yourself!"
      arrows:
        name: "<green>Arrow"
        amount: 32
        lore:
          - "<gray>Deadly arrows"
    corrupted:
      sword:
        type: "minecraft:iron_sword"
        name: "<green>Sword"
        lore:
          - "<gray>Eliminate survivors!"
      armor:
        helmet:
          type: "minecraft:iron_helmet"
          name: "<red>Iron Helmet"
        leggings:
          type: "minecraft:iron_leggings"
          name: "<red>Iron Leggings"
        boots:
          type: "minecraft:iron_boots"
          name: "<red>Iron Boots"
    # Patient-Zero Role
    patientzero:
      bow:
        name: "<green>Basic Bow"
        lore:
          - "<gray>Defend yourself!"
      sword:
        type: "minecraft:iron_sword"
        name: "<green>Sword"
        lore:
          - "<gray>Eliminate survivors!"

  double-jump:
    enabled: false
    velocity: 0.5
    cooldown: 10 # Cooldown in seconds

    cooldown-message: "<red>You must wait <yellow>%time% <red>seconds before double jumping again!"

# Your maps will be saved here
maps: []

```
