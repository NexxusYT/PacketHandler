package com.github.razorplay.packet_handler.network;

import com.github.razorplay.packet_handler.exceptions.PacketInstantiationException;
import com.github.razorplay.packet_handler.exceptions.PacketNotFoundException;
import com.github.razorplay.packet_handler.exceptions.PacketRegistrationException;
import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

/**
 * Handles TCP packet registration, serialization, and deserialization for network communication.
 * This utility class manages a registry of packet types and provides methods for packet handling.
 */
public class PacketTCP {
    public static final Logger LOGGER = LoggerFactory.getLogger("PacketTCP");
    public static final BiMap<String, Class<? extends IPacket>> PACKET_REGISTRY = HashBiMap.create();

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private PacketTCP() {
        // Utility class, no instantiation needed
    }

    /**
     * Registers all packet classes provided explicitly.
     *
     * @param packetClasses Array of packet classes to register
     * @throws IllegalArgumentException if packetClasses is null or empty
     */
    public static void registerPackets(Class<? extends IPacket>... packetClasses) {
        if (packetClasses == null || packetClasses.length == 0) {
            throw new IllegalArgumentException("Packet classes cannot be null or empty.");
        }

        for (Class<? extends IPacket> packetClass : packetClasses) {
            if (!IPacket.class.isAssignableFrom(packetClass)) {
                throw new PacketRegistrationException("Class " + packetClass.getName() + " does not implement IPacket.");
            }

            try {
                IPacket tempPacket = packetClass.getDeclaredConstructor().newInstance();
                String packetId = tempPacket.getPacketId();

                if (PACKET_REGISTRY.containsKey(packetId)) {
                    throw new PacketRegistrationException("Duplicate Packet ID \"" + packetId + "\" found in " + packetClass.getName());
                }

                registerPacket(packetId, packetClass);
                LOGGER.info("Registered packet: {} with ID \"{}\"", packetClass.getSimpleName(), packetId);
            } catch (ReflectiveOperationException e) {
                throw new PacketRegistrationException("Failed to instantiate packet class " + packetClass.getName());
            }
        }
    }

    /**
     * Registers a new packet type with the specified ID
     *
     * @param id          The unique identifier for the packet type
     * @param packetClass The class of the packet to register
     * @throws PacketRegistrationException if the packet ID is already registered
     * @throws IllegalArgumentException    if the packet class doesn't implement IPacket
     */
    public static void registerPacket(String id, Class<? extends IPacket> packetClass) {
        if (PACKET_REGISTRY.containsKey(id)) {
            throw new PacketRegistrationException("Packet ID \"" + id + "\" is already registered.");
        }

        if (!IPacket.class.isAssignableFrom(packetClass)) {
            throw new IllegalArgumentException("Class " + packetClass.getName() + " does not implement IPacket.");
        }

        PACKET_REGISTRY.put(id, packetClass);
    }

    /**
     * Retrieves the packet type identifier for a given packet instance
     *
     * @param packet The packet instance
     * @return The packet type identifier
     * @throws PacketNotFoundException if the packet class is not registered
     */
    private static String getPacketType(IPacket packet) {
        return Optional.ofNullable(PACKET_REGISTRY.inverse().get(packet.getClass()))
                .orElseThrow(() -> new PacketNotFoundException("Packet class not registered: " + packet.getClass().getName()));
    }

    /**
     * Serializes a packet into a byte array
     *
     * @param packet The packet to serialize
     * @return byte array containing the serialized packet data
     * @throws PacketSerializationException if there's an error during serialization
     */
    public static byte[] write(IPacket packet) throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        String packetType = getPacketType(packet);
        out.writeUTF(packetType);
        PacketDataSerializer serializer = new PacketDataSerializer(out);
        packet.write(serializer);
        return out.toByteArray();
    }

    /**
     * Deserializes a packet from a byte array input
     *
     * @param buf The input buffer containing the packet data
     * @return The deserialized packet instance
     * @throws PacketInstantiationException if there's an error creating the packet instance
     * @throws PacketSerializationException if there's an error during deserialization
     */
    public static IPacket read(ByteArrayDataInput buf) throws PacketInstantiationException, PacketSerializationException {
        String packetType = buf.readUTF();
        Class<? extends IPacket> packetClass = PACKET_REGISTRY.get(packetType);

        if (packetClass == null) {
            throw new PacketInstantiationException("Could not find packet with ID " + packetType, null);
        }

        try {
            IPacket packet = packetClass.getDeclaredConstructor().newInstance();
            PacketDataSerializer serializer = new PacketDataSerializer(buf);
            packet.read(serializer);
            return packet;
        } catch (ReflectiveOperationException e) {
            throw new PacketInstantiationException("Error instantiating packet with ID " + packetType, e);
        } catch (PacketSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw new PacketSerializationException("Unexpected error deserializing packet with ID " + packetType, e);
        }
    }
}