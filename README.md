# Minecraft Packet Handler

![Java](https://img.shields.io/badge/Java-8+-orange)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue)
![Paper](https://img.shields.io/badge/Paper-1.21.1-brightgreen)
![License](https://img.shields.io/badge/License-MPL--2.0-yellow)

## Overview

The **Minecraft Packet Handler** is a lightweight and robust library for managing custom network packets in Minecraft 1.21.1 servers and clients. Built for use with **Paper** (and compatible with **Fabric** clients), this project provides a flexible system for serializing, deserializing, and transmitting custom data through Minecraft's plugin messaging channels (`sendPluginMessage` and `PluginMessageListener`). It is designed to simplify client-server communication in Minecraft plugins, with features like GZIP compression, error handling, and dynamic packet registration.

The library is ideal for developers building complex Minecraft plugins that require custom network communication, such as cross-server messaging, player data synchronization, or custom protocol extensions.

## Features

- **Custom Packet System**: Define and register custom packets implementing the `IPacket` interface.
- **Serialization/Deserialization**: Robust serialization of primitive types, collections, and custom objects using `PacketDataSerializer`.
- **GZIP Compression**: Compress packets to optimize network usage, with a configurable size limit (default: 1 MB).
- **Error Handling**: Comprehensive exception handling for corrupted data, invalid packets, and serialization errors.
- **Dynamic Packet Registration**: Register packet types at runtime with unique IDs, preventing conflicts.
- **Tested and Stable**: Includes unit tests (`PacketTCPTest`, `PacketDataSerializerTest`) to ensure reliability.
- **Minecraft 1.21.1 Compatible**: Designed for Paper 1.21.1 servers and compatible with Fabric clients (including mods like XXL Packets).

## Use Cases

- Synchronizing player data (e.g., inventory, stats) between a Paper server and Fabric clients.
- Implementing custom protocols for cross-server communication (e.g., via Velocity or BungeeCord).
- Sending large datasets (e.g., world data, configuration files) with compression to stay within the 2 MB packet limit.
- Building secure communication systems (future support for encryption planned).

## Configuration

- **Compression Limit**: The default limit for compressed packets is 1 MB (`MAX_COMPRESSED_SIZE`). Adjust in `PacketTCP.java` if needed:
  ```java
  public static final int MAX_COMPRESSED_SIZE = 2 * 1024 * 1024; // 2MB
  ```
- **Channel Name**: Use a custom channel (e.g., `your:channel`) for plugin messaging.
- **Logging**: Uses SLF4J for logging packet registration and errors.

## Limitations

- **Packet Size**: Limited to 2 MB per packet due to Minecraft's protocol (extendable with mods like XXL Packets on Fabric clients).
- **No Encryption**: Encryption is planned for future releases but not currently implemented.
- **Paper-Centric**: Primarily designed for Paper servers, though compatible with Fabric clients.

## Planned Features

- **Extended Tests**: Additional unit tests for edge cases (e.g., very large packets, corrupted data).

## Contributing

Contributions are welcome! To contribute:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature/my-feature`).
3. Commit your changes (`git commit -m "Add my feature"`).
4. Push to the branch (`git push origin feature/my-feature`).
5. Open a Pull Request.

Please ensure your code follows the existing style and includes unit tests.

## License

This project is licensed under the Mozilla Public License 2.0.

## Contact

For questions, issues, or suggestions, please open an issue on the [GitHub repository](https://github.com/razorplay/minecraft-packet-handler) or contact the maintainer at [your-email@example.com].

---

*Built with ❤️ for the Minecraft community.*
