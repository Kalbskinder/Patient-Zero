# Infection
A Minecraft plugin that adds the Hypixel minigame Infection to your server.

## Overview
- Commands
- Permissions

## Commands
A list of available commands that the plugin adds
<br><br>
![List of commands](https://cdn.modrinth.com/data/5RQMBtQG/images/a8cb401775f6b95bbe4072deaf7d0703eed8c057.png)

- `/infection help`
- `/infection createmap <map-name>`
- `/infection pos1`
- `/infection pos2`
- `/infection discardSelection`
- `/infection deletemap <map-name>`
- `/infection addspawn <map-name> <role>`
- `/infection setqueue-spawn <map-name>`
- `/infection setqueue-limit <map-name> <int-max>`
- `/infection list`
- `/infection join <map-name>`
- `/infection leave`

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

