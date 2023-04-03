# ChatItem

The official repository of the ChatItem Spigot/Bukkit plugin.

You can:

- [Download latest dev build](https://nightly.link/dadus33-plugins/ChatItem/workflows/build/v2/ChatItem.jar.zip)
- [ ![Come on discord](https://img.shields.io/badge/chat-on_discord-7289da.svg) ](https://discord.gg/yng5PPf62h)
- [Check the wiki](https://github.com/dadus33-plugins/ChatItem/wiki)

# Item not well showed in chat

You can try to do `/chatitem admin` then select different paper item. It will change how the plugin try to add item in chat.

**If you already have issue, please do**:
1) Do `/chatitem admin` then select on "Debug" item at top right
2) Try to use ChatItem in chat
3) Come on discord [here](https://discord.gg/yng5PPf62h) or in private message: `Elikill58#0743`
4) Send the **full file `logs/latest.log`**

PS: Yes you can disable the debug behavior after.

# Building

Building this is simple as we use Maven.

To build it, simply clone this repository and then run `mvn clean install`.

You can add the parameter `export_dir` to set the URL where the JAR will be exported.
