package com.github.razorplay.packet_handler.network;

import com.github.razorplay.packet_handler.exceptions.PacketInstantiationException;
import com.github.razorplay.packet_handler.exceptions.PacketNotFoundException;
import com.github.razorplay.packet_handler.exceptions.PacketRegistrationException;
import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.packet.SimplePacket;
import com.github.razorplay.packet_handler.network.packet.annotation.PacketIdentifier;
import com.github.razorplay.packet_handler.network.reflection.ClassSerializer;
import com.github.razorplay.packet_handler.util.StringUtil;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Handles TCP packet registration, serialization, and deserialization for network communication.
 * This utility class manages a registry of packet types and provides methods for packet handling,
 * including support for compressed packets.
 */
public class PacketTCP {
    public static final Logger LOGGER = LoggerFactory.getLogger("PacketTCP");
    public static final BiMap<String, Class<? extends IPacket>> PACKET_REGISTRY = HashBiMap.create();
    public static final int MAX_COMPRESSED_SIZE = 1024 * 1024; // 1MB límite por defecto para paquetes comprimidos

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
    @SafeVarargs
    public static void registerPackets(Class<? extends IPacket>... packetClasses) {
        if (packetClasses == null || packetClasses.length == 0) {
            throw new IllegalArgumentException("Packet classes cannot be null or empty.");
        }

        for (Class<? extends IPacket> packetClass : packetClasses) {
            if (!IPacket.class.isAssignableFrom(packetClass)) {
                throw new PacketRegistrationException("Class " + packetClass.getName() + " does not implement IPacket.");
            }

            String packetId = PacketTCP.getPacketId(packetClass);
            if (PACKET_REGISTRY.containsKey(packetId)) {
                throw new PacketRegistrationException("Duplicate Packet ID \"" + packetId + "\" found in " + packetClass.getName());
            }

            registerPacket(packetId, packetClass);
            LOGGER.info("Registered packet: {} with ID \"{}\"", packetClass.getSimpleName(), packetId);
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
     * Serializes a packet into a compressed byte array using GZIP
     *
     * @param packet The packet to serialize and compress
     * @return byte array containing the compressed serialized packet data
     * @throws PacketSerializationException if there's an error during serialization or compression
     */
    public static byte[] writeCompressed(IPacket packet) throws PacketSerializationException {
        try {
            // Serializar el paquete normalmente
            byte[] uncompressedData = write(packet);

            // Comprimir los datos
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
                gzip.write(uncompressedData);
            }
            byte[] compressedData = baos.toByteArray();

            // Verificar tamaño
            if (compressedData.length > MAX_COMPRESSED_SIZE) {
                throw new PacketSerializationException("Compressed packet size exceeds limit: " + compressedData.length + " bytes (max: " + MAX_COMPRESSED_SIZE + " bytes)");
            }

            return compressedData;
        } catch (IOException e) {
            throw new PacketSerializationException("Error compressing packet with ID " + getPacketType(packet), e);
        }
    }

    /**
     * Deserializes a packet from a byte array input
     *
     * @param buf The input buffer containing the packet data
     * @return The deserialized packet instance
     * @throws PacketInstantiationException if there's an error creating the packet instance
     * @throws PacketSerializationException if there's an error during deserialization
     */
    @SuppressWarnings("unchecked")
    public static <T extends IPacket> T read(ByteArrayDataInput buf) throws PacketInstantiationException, PacketSerializationException {
        String packetType = buf.readUTF();
        Class<T> packetClass = (Class<T>) PACKET_REGISTRY.get(packetType);

        if (packetClass == null) {
            throw new PacketInstantiationException("Could not find packet with ID " + packetType, null);
        }

        PacketDataSerializer serializer = new PacketDataSerializer(buf);
        if (packetClass.isAssignableFrom(SimplePacket.class)) {
            // Handle custom deserialization process for SimplePacket
            return ClassSerializer.decode(serializer, packetClass);
        }

        try {
            Constructor<T> declaredConstructor = packetClass.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            T packet = declaredConstructor.newInstance();
            
            packet.read(serializer);
            return packet;
        } catch (ReflectiveOperationException e) {
            throw new PacketInstantiationException("Error instantiating packet with ID " + packetType, e);
        } catch (Exception genericException) {
            throw new PacketSerializationException("Unexpected error deserializing packet with ID " + packetType, genericException);
        }
    }

    /**
     * Deserializes a packet from a compressed byte array input using GZIP
     *
     * @param compressedData The compressed input buffer containing the packet data
     * @return The deserialized packet instance
     * @throws PacketInstantiationException if there's an error creating the packet instance
     * @throws PacketSerializationException if there's an error during deserialization or decompression
     */
    public static IPacket readCompressed(byte[] compressedData) throws PacketInstantiationException, PacketSerializationException {
        try {
            // Descomprimir los datos
            ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPInputStream gzip = new GZIPInputStream(bais)) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzip.read(buffer)) > 0) {
                    baos.write(buffer, 0, len);
                }
            }
            byte[] uncompressedData = baos.toByteArray();

            // Deserializar el paquete
            ByteArrayDataInput input = ByteStreams.newDataInput(uncompressedData);
            return read(input);
        } catch (IOException e) {
            throw new PacketSerializationException("Error decompressing packet data", e);
        }
    }

    /**
     * Retrieves the packet unique identifier for a given packet class.
     *
     * <p>If the class is annotated with {@link PacketIdentifier}, the value of the {@code id()}
     * field is returned. Otherwise, the class name is converted to snake_case and returned
     * as a fallback identifier.</p>
     *
     * <p>This method is typically used for registering or resolving packets by ID during
     * serialization and deserialization.</p>
     *
     * <pre>{@code
     * @PacketIdentifier(id = "example:my_packet")
     * class MyPacket {}
     *
     * getPacketId(MyPacket.class); // returns "example:my_packet"
     *
     * class LoginPacket {}
     * getPacketId(LoginPacket.class); // returns "login_packet"
     * }</pre>
     *
     * @param packetClass the class representing the packet.
     * @return the resolved packet ID, either from the annotation or derived from the class name.
     */
    public static String getPacketId(Class<?> packetClass) {
        if (packetClass.isAnnotationPresent(PacketIdentifier.class)) {
            PacketIdentifier identifierAnnotation = packetClass.getAnnotation(PacketIdentifier.class);
            return identifierAnnotation.id();
        }
        return StringUtil.toSnakeCase(packetClass.getSimpleName());
    }

    /**
     * Clears the packet registry, removing all registered packet types.<br>
     * This method should be used with caution as it may cause issues with packet handling.
     */
    public static void clearRegisteredPackets() {
        PACKET_REGISTRY.clear();
    }
}