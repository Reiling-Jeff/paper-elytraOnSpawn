# ElytraOnSpawn

Ever dreamed of soaring through the skies with an Elytra right after spawning? With the "ElytraOnSpawn" plugin, that dream becomes reality! This plugin automatically equips you with an Elytra when you spawn or respawn, without replacing your armor. It's the perfect addition for your SMP server and works seamlessly with most other plugins.

## Features

- **Automatic Elytra on Spawn**: Receive an (invisible) Elytra when you spawn or respawn in the designated world.
- **Boost with Offhand Items**: Switch items in your offhand and press "F" (default key) to boost through the air.
- **Fancy Particles**: Enjoy stylish particle effects while flying.
- **Highly Configurable**: Adjust settings through the `config.yml` or in-game commands to personalize the experience.
- **Multiverse Support**: Server admins can specify which world the plugin should operate in.
- **Ease of Use**: Just install the plugin, set your world spawn with `/setworldspawn`, and you're ready to fly!

This plugin is perfect for players who love exploring the skies or getting from point A to B faster while enjoying a unique flying experience.

## Installation

1. Download the latest version of the plugin from the [SpigotMC page](https://www.spigotmc.org/resources/elytra-on-spawn.120079/).
2. Place the `.jar` file into your server's `plugins` folder.
3. Restart or reload your server.
4. Set the world spawn using `/setworldspawn`.
5. Double jump to start flying!

## Usage

- Double jump to activate flight with the Elytra.
- Switch items in your offhand (default: "F") to boost.
- Adjust settings via `config.yml` or commands for further customization.

## Commands

| Command                          | Description                                                                 |
|----------------------------------|-----------------------------------------------------------------------------|
| `/elytraonspawn help [command]`  | Display general help or details for a specific command.                    |
| `/elytraonspawn reload`          | Reload the plugin configuration and restart the plugin.                     |
| `/elytraonspawn config reset`    | Reset the configuration to default settings.                                |
| `/elytraonspawn config check`    | Check the configuration for errors.                                         |
| `/elytraonspawn config <key>`    | View the current value of a specific configuration key.                     |
| `/elytraonspawn config <key> <value>` | Update the value of a specific configuration key.                         |

### Configurable Keys

| Key                               | Description                                                                 |
|-----------------------------------|-----------------------------------------------------------------------------|
| `verbose`                        | Enable/disable verbose logging (true/false).                               |
| `spawnradius`                    | Radius around spawn for unlimited elytra boost (positive integer).          |
| `flyboostmultiplier`             | Multiplier for flight boost strength (float, 1.0 or higher).                |
| `startboostmultiplier`           | Multiplier for initial boost strength (float, 1.0 or higher).               |
| `world`                          | World name where the plugin is active (string).                            |
| `boostsoundsetter`               | Enable/disable sound on boost (true/false).                                 |
| `boostsound`                     | Sound effect for boost (string).                                            |
| `switchgamemodecancelsoundsetter`| Enable/disable sound when boost is canceled due to gamemode switch (true/false). |
| `switchgamemodecancelsound`      | Sound effect for boost cancellation due to gamemode switch (string).        |
| `particle`                       | Enable/disable particle effects during flight (true/false).                 |

## Reporting Issues

If you encounter any issues or have feature requests, please let us know via our [GitHub Issues page](https://github.com/Reiling-Jeff/paper-elytraOnSpawn/issues).

## License

Distributed under the BSD 2-Clause License. See the [LICENSE](LICENSE) file for details.

## Contact

Plugin Author: [Reiling-Jeff](https://www.spigotmc.org/members/reiling-jeff.120079/)

- SpigotMC Profile: [Reiling-Jeff](https://www.spigotmc.org/members/reiling-jeff.120079/)
- Discord: [Join Support Server](https://discord.gg/zd5TNQE97Y)
- Email: [contact@yuuto.me](mailto:contact@yuuto.me)
