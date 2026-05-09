<p align="center">
    <img src="https://cdn.modrinth.com/data/5RQMBtQG/751ef8394583803f190a4e8d8cf9e9eb9e344406.png" alt="Infection Logo" width="256">
</p>

<h1 align="center">Infection</h1>
<p align="center">A Minecraft plugin that adds the Hypixel minigame Infection to your server.</p>

## Overview
- How to use
- Permissions

## How to use

1. Use `/infection wand` to get the area selection wand.
2. Use the Selection Wand to select a map area.
3. Create a map using `/infection createmap <map-name>`.
4. Set a queue spawn using `/infection setqueue-spawn <map-name>`.
5. Optionally change the maximum queue size using `/infection setqueue-limit <map-name> <limit>`.
6. Set the spawnpoints where survivors/infected players will spawn using `/infection addspawn <map-name> <role>`.
7. You can now join the map using `/infection join <map-name>`.

Use `/infection help guide` and `/infection help commands` for more information.

## Permissions
```yml
permissions:
  infection.join:
    description: Allows a player to run '/infection join <map-name>'
    default: true
  infection.leave:
    description: Allows a player to run '/infection leave'
    default: true

  infection.admin:
    description: Allows a player to access all of the '/infection <args>' commands.
```

