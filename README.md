# Better Smithing Template

[中文](./README.zh.md) | English

Rewrites the smithing mechanism to give you a better smithing experience.

## What does this mod do?

In vanilla Minecraft, smithing templates used in the smithing table are consumed in one use. This mod makes templates behave like tools — they can be reused multiple times and only disappear when worn out.

## Main Features

- **Reusable templates**: Adds a durability system to templates. Default durability is 3; the template disappears once depleted.
- **Auto-detection**: Automatically detects all smithing templates in the game, including those added by other mods. No manual configuration required.
- **Flexible configuration**: If a template cannot be auto-detected, you can specify it manually via a list.

## Configuration

The config file is located at `config/better_smithing_template.toml` in the game directory. You can edit it with any text editor.

| Option | Default | Description |
|------|--------|------|
| `auto_detect_templates` | `true` | Auto-detect all smithing templates in the game. When disabled, you must specify them in the list below. |
| `target_items` | built-in examples | List of templates to manually add durability to, one per line, in `mod_id:item_name` format. |
| `max_damage` | `3` | Number of uses per smithing template. Set to `0` to disable the durability feature (restore vanilla behavior). Range 0-5. |

## Compatibility

- Currently supports Minecraft 1.21.1 + NeoForge
- Theoretically compatible with most mods that add new smithing templates
- Does not modify vanilla recipes or affect other mods' functionality

## FAQ

**Q: Can templates still stack in the inventory?**
A: Yes. Identical templates can stack in the inventory.

**Q: Can I set the number of uses higher?**
A: Yes. If you need more uses, adjust the `max_damage` value in the config.

**Q: How do I manually add a smithing template that wasn't auto-detected?**
A: First, find the template's registry name, e.g. `cataclysm:ignitium_upgrade_smithing_template`. Then add it to the `target_items` list in the config file.

## License

This project is licensed under the [GNU General Public License v3.0](./LICENSE) (GPL-3.0). Derivative works must be released under the same license.
