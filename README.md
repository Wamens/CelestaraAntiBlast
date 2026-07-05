# CelestaraAntiBlast

CelestaraAntiBlast is a small Paper plugin that stops selected dropped items from being destroyed by the blast sources players usually care about: TNT, end crystals, respawn anchors, creepers, and wither skulls.

It does not try to protect items from every possible damage source. Fire, lava, lightning, beds, direct wither damage, and other non-blast damage types are left alone on purpose.

## Version

- Minecraft/Paper: `1.21.11`
- Java: `21`
- Build tool: Maven

## What it does

The plugin watches item entities when they take damage. If the dropped item type is in the protected list, and the damage came from an enabled blast source, the damage gets cancelled.

Protected items are stored in `config.yml`, so you can keep the list exactly how you want it. The plugin caches the list after loading, so normal item checks stay fast.

## Commands

Main command:

```text
/celestaraantiblast
```

Aliases:

```text
/antiblast
/cab
```

Available commands:

```text
/cab add
/cab add <material>
/cab remove
/cab remove <material>
/cab list
/cab reload
```

`/cab add` and `/cab remove` use the item in your main hand if you do not type a material name.

Examples:

```text
/cab add DIAMOND
/cab remove NETHERITE_SWORD
/cab list
/cab reload
```

## Permissions

Admin permission:

```text
celestaraantiblast.admin
```

Per-command permissions:

```text
celestaraantiblast.command.add
celestaraantiblast.command.remove
celestaraantiblast.command.list
celestaraantiblast.command.reload
```

All permissions default to operators. If you use LuckPerms or another permissions plugin, give your admin group `celestaraantiblast.admin`.

## Config

The important part looks like this:

```yaml
protection:
  end-crystals: true
  respawn-anchors: true
  tnt: true
  creepers: true
  wither-skulls: true
```

The item list is under:

```yaml
protected-items:
  - DIAMOND
  - NETHERITE_INGOT
  - ELYTRA
```

Material names must match Bukkit/Paper material names. Invalid entries are ignored when the plugin loads.

## Building

Use Java 21:

```text
mvn clean package
```

The jar will be created in:

```text
target/celestaraantiblast-1.0-ALPHA-SNAPSHOT.jar
```

Drop that jar into your server's `plugins` folder and restart the server.
