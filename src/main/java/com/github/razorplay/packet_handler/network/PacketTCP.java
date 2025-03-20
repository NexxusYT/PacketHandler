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
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;

/**
 * Handles TCP packet registration, serialization, and deserialization for network communication.
 * This utility class manages a registry of packet types and provides methods for packet handling.
 */
public class PacketTCP {
    public static final Logger LOGGER = LoggerFactory.getLogger("PacketTCP");
    public static final String PACKET_CHANNEL = "razorplay01:packets_channel";
    private static final BiMap<String, Class<? extends IPacket>> packetRegistry = HashBiMap.create();

    /**
     * Private constructor to prevent instantiation of utility class
     */
    private PacketTCP() {
        // Utility class, no instantiation needed
    }

    /**
     * Scans the specified package and automatically registers all packet classes annotated with @Packet
     *
     * @param basePackage The base package to scan for packet classes
     * @throws IllegalArgumentException if basePackage is null or empty
     */
    public static void scanAndRegisterPackets(String basePackage) {
        try {
            if (basePackage == null || basePackage.isEmpty()) {
                throw new IllegalArgumentException("Base package cannot be null or empty.");
            }

            // Convertir el paquete a una ruta de directorio (p.ej., "com.example" -> "com/example")
            String packagePath = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Set<Class<?>> packetClasses = new HashSet<>();

            // Obtener todos los recursos (directorios o jars) que coincidan con el paquete
            Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                String protocol = resource.getProtocol();

                if ("file".equals(protocol)) {
                    // Escanear clases en un directorio (útil en desarrollo)
                    File directory = new File(resource.getFile());
                    if (directory.exists()) {
                        packetClasses.addAll(findClasses(directory, basePackage));
                    }
                } else if ("jar".equals(protocol)) {
                    // Escanear clases en un archivo JAR (útil en producción)
                    String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                    packetClasses.addAll(findClassesInJar(jarPath, basePackage));
                }
            }

            // Procesar las clases encontradas
            for (Class<?> clazz : packetClasses) {
                if (!IPacket.class.isAssignableFrom(clazz)) {
                    LOGGER.warn("Class {} is annotated with @Packet but does not implement IPacket. Skipping.", clazz.getName());
                    continue;
                }

                if (clazz.isAnnotationPresent(Packet.class)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends IPacket> packetClass = (Class<? extends IPacket>) clazz;

                    IPacket tempPacket = packetClass.getDeclaredConstructor().newInstance();
                    String packetId = tempPacket.getPacketId();

                    if (packetRegistry.containsKey(packetId)) {
                        throw new PacketRegistrationException("Duplicate Packet ID \"" + packetId + "\" found in " + packetClass.getName());
                    }

                    registerPacket(packetId, packetClass);
                    LOGGER.info("Registered packet: {} with ID \"{}\"", packetClass.getSimpleName(), packetId);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Packet registration exception:", e);
        }
    }

    /**
     * Finds all classes in a directory recursively.
     *
     * @param directory   The base directory to scan
     * @param packageName The package name for classes in this directory
     * @return A set of classes found
     */
    private static Set<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                // Recursivamente buscar en subdirectorios
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                // Cargar la clase desde el archivo .class
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    /**
     * Finds all classes in a JAR file that belong to the specified package.
     *
     * @param jarPath     The path to the JAR file
     * @param packageName The package to scan
     * @return A set of classes found
     */
    private static Set<Class<?>> findClassesInJar(String jarPath, String packageName) throws IOException, ClassNotFoundException {
        Set<Class<?>> classes = new HashSet<>();
        java.util.jar.JarFile jarFile = new java.util.jar.JarFile(jarPath);

        Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry entry = entries.nextElement();
            String entryName = entry.getName();

            if (entryName.endsWith(".class") && entryName.startsWith(packageName.replace('.', '/'))) {
                String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                classes.add(Class.forName(className));
            }
        }
        jarFile.close();
        return classes;
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
        if (packetRegistry.containsKey(id)) {
            throw new PacketRegistrationException("Packet ID \"" + id + "\" is already registered.");
        }

        if (!IPacket.class.isAssignableFrom(packetClass)) {
            throw new IllegalArgumentException("Class " + packetClass.getName() + " does not implement IPacket.");
        }

        packetRegistry.put(id, packetClass);
    }

    /**
     * Retrieves the packet type identifier for a given packet instance
     *
     * @param packet The packet instance
     * @return The packet type identifier
     * @throws PacketNotFoundException if the packet class is not registered
     */
    private static String getPacketType(IPacket packet) {
        return Optional.ofNullable(packetRegistry.inverse().get(packet.getClass()))
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
        Class<? extends IPacket> packetClass = packetRegistry.get(packetType);

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
        }
    }
}