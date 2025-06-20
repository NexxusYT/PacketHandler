package com.github.razorplay.packet_handler.network.network_util;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Utility class for advanced serialization and deserialization of complex data types,
 * including all primitive types, collections, maps, and custom serializable objects.
 */
public class PacketDataSerializer {
    private static final String NOT_WRITING_ERROR = "Not in writing mode";
    private static final String NOT_READING_ERROR = "Not in reading mode";
    private final DataOutput output;
    private final DataInput input;

    public PacketDataSerializer(DataOutput output) {
        this.output = output;
        this.input = null;
    }

    public PacketDataSerializer(DataInput input) {
        this.input = input;
        this.output = null;
    }

    private boolean isNotWriting() {
        return output == null;
    }

    private boolean isNotReading() {
        return input == null;
    }

    /**
     * Writes a byte value to the output buffer.
     *
     * @param value The byte value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeByte(byte value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeByte(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a byte value from the input buffer.
     *
     * @return The byte value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public byte readByte() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readByte();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading byte", e);
            }
            throw new PacketSerializationException("Error reading byte", e);
        }
    }

    /**
     * Writes a short value to the output buffer.
     *
     * @param value The short value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeShort(short value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeShort(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a short value from the input buffer.
     *
     * @return The short value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public short readShort() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readShort();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading short", e);
            }
            throw new PacketSerializationException("Error reading short", e);
        }
    }

    /**
     * Writes an int value to the output buffer.
     *
     * @param value The int value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeInt(int value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeInt(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads an int value from the input buffer.
     *
     * @return The int value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public int readInt() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readInt();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading int", e);
            }
            throw new PacketSerializationException("Error reading int", e);
        }
    }

    /**
     * Writes a long value to the output buffer.
     *
     * @param value The long value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeLong(long value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeLong(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a long value from the input buffer.
     *
     * @return The long value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public long readLong() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readLong();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading long", e);
            }
            throw new PacketSerializationException("Error reading long", e);
        }
    }

    /**
     * Writes a float value to the output buffer.
     *
     * @param value The float value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeFloat(float value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeFloat(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a float value from the input buffer.
     *
     * @return The float value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public float readFloat() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readFloat();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading float", e);
            }
            throw new PacketSerializationException("Error reading float", e);
        }
    }

    /**
     * Writes a double value to the output buffer.
     *
     * @param value The double value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeDouble(double value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeDouble(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a double value from the input buffer.
     *
     * @return The double value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public double readDouble() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readDouble();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading double", e);
            }
            throw new PacketSerializationException("Error reading double", e);
        }
    }

    /**
     * Writes a char value to the output buffer.
     *
     * @param value The char value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeChar(char value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeChar(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a char value from the input buffer.
     *
     * @return The char value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public char readChar() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readChar();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading char", e);
            }
            throw new PacketSerializationException("Error reading char", e);
        }
    }

    /**
     * Writes a boolean value to the output buffer.
     *
     * @param value The boolean value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeBoolean(boolean value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.writeBoolean(value);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a boolean value from the input buffer.
     *
     * @return The boolean value read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public boolean readBoolean() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            return input.readBoolean();
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading boolean", e);
            }
            throw new PacketSerializationException("Error reading boolean", e);
        }
    }

    /**
     * Writes a String value to the output buffer.
     * The length of the string's UTF-8 encoded bytes is written as an integer,
     * followed by the raw bytes of the string.
     *
     * @param value The String value to write
     * @throws IllegalStateException if not in writing mode
     */
    public void writeString(String value) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        writeInt(bytes.length);
        try {
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a string from the input buffer.
     *
     * @return The string read
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if an error occurs during reading
     */
    public String readString() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        int length = readInt();
        if (length < 0) {
            throw new PacketSerializationException("Invalid string length: " + length);
        }
        byte[] bytes = new byte[length];
        try {
            input.readFully(bytes);
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading string", e);
            }
            throw new PacketSerializationException("Error reading string", e);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * Writes an enum value to the output buffer by serializing its name as a UTF-8 string.
     *
     * @param enumValue The enum value to write
     * @param <T>       The type of the enum
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public <T extends Enum<T>> void writeEnum(T enumValue) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeString(enumValue != null ? enumValue.name() : "null");
    }

    /**
     * Reads an enum value from the input buffer by deserializing its name and converting it back to the enum type.
     *
     * @param enumClass The class of the enum to read
     * @param <T>       The type of the enum
     * @return The enum value read from the buffer, or null if "null" was written
     * @throws IllegalStateException    if the serializer is not in reading mode
     * @throws IllegalArgumentException if the enum name is invalid for the provided enum class
     */
    public <T extends Enum<T>> T readEnum(Class<T> enumClass) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        String name = readString();
        if ("null".equals(name)) {
            return null; // Maneja el caso de null
        }
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            throw new PacketSerializationException("Invalid enum value '" + name + "' for " + enumClass.getSimpleName(), e);
        }
    }


    /**
     * Writes a list to the output buffer using the provided element writer.
     * The size of the list is written first as an integer, followed by each element
     * serialized using the provided {@code elementWriter}.
     * <p>
     * Example usage:
     * <pre>
     * List<String> names = Arrays.asList("Alice", "Bob", "Charlie");
     * serializer.writeList(names, PacketDataSerializer::writeString);
     * </pre>
     *
     * @param list          The list to write to the output buffer
     * @param elementWriter The writer function for serializing individual elements of the list
     * @param <T>           The type of elements in the list
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public <T> void writeList(List<T> list, BiConsumer<PacketDataSerializer, T> elementWriter) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeInt(list.size());
        for (T element : list) {
            elementWriter.accept(this, element);
        }
    }

    /**
     * Reads a list from the input buffer using the provided element reader.
     * The size of the list is read first as an integer, followed by each element
     * deserialized using the provided {@code elementReader}.
     * <p>
     * Example usage:
     * <pre>
     * List<String> names = serializer.readList(PacketDataSerializer::readString);
     * // names will contain the deserialized list of strings
     * </pre>
     *
     * @param elementReader The reader function for deserializing individual elements of the list
     * @param <T>           The type of elements in the list
     * @return The list read from the input buffer
     * @throws IllegalStateException if the serializer is not in reading mode
     */
    public <T> List<T> readList(ThrowingFunction<PacketDataSerializer, T> elementReader) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        int size = readInt();
        if (size < 0) {
            throw new PacketSerializationException("Invalid list size: " + size);
        }
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(elementReader.apply(this));
        }
        return list;
    }


    /**
     * Writes a set to the output buffer using the provided element writer.
     * The size of the set is written first as an integer, followed by each element
     * serialized using the provided {@code elementWriter}.
     * <p>
     * Example usage:
     * <pre>
     * Set<String> roles = Set.of("ADMIN", "USER", "GUEST");
     * serializer.writeSet(roles, PacketDataSerializer::writeString);
     * </pre>
     *
     * @param set           The set to write to the output buffer
     * @param elementWriter The writer function for serializing individual elements of the set
     * @param <T>           The type of elements in the set
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public <T> void writeSet(Set<T> set, BiConsumer<PacketDataSerializer, T> elementWriter) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeInt(set.size());
        for (T element : set) {
            elementWriter.accept(this, element);
        }
    }

    /**
     * Reads a set from the input buffer using the provided element reader.
     * The size of the set is read first as an integer, followed by each element
     * deserialized using the provided {@code elementReader}.
     * <p>
     * Example usage:
     * <pre>
     * Set<String> roles = serializer.readSet(PacketDataSerializer::readString);
     * // roles will contain the deserialized set of strings
     * </pre>
     *
     * @param elementReader The reader function for deserializing individual elements of the set
     * @param <T>           The type of elements in the set
     * @return The set read from the input buffer, implemented as a {@link HashSet}
     * @throws IllegalStateException if the serializer is not in reading mode
     */
    public <T> Set<T> readSet(ThrowingFunction<PacketDataSerializer, T> elementReader) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        int size = readInt();
        if (size < 0) {
            throw new PacketSerializationException("Invalid set size: " + size);
        }
        Set<T> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(elementReader.apply(this));
        }
        return set;
    }

    /**
     * Writes a map to the output buffer using the provided key and value writers.
     * The size of the map is written first as an integer, followed by each key-value pair
     * serialized using the provided {@code keyWriter} and {@code valueWriter}.
     * <p>
     * Example usage:
     * <pre>
     * Map<String, Integer> scores = Map.of("Alice", 100, "Bob", 200);
     * serializer.writeMap(scores,
     *     PacketDataSerializer::writeString,
     *     PacketDataSerializer::writeInt);
     * </pre>
     *
     * @param map         The map to write to the output buffer
     * @param keyWriter   The writer function for serializing map keys
     * @param valueWriter The writer function for serializing map values
     * @param <K>         The type of keys in the map
     * @param <V>         The type of values in the map
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public <K, V> void writeMap(Map<K, V> map,
                                BiConsumer<PacketDataSerializer, K> keyWriter,
                                BiConsumer<PacketDataSerializer, V> valueWriter) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeInt(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            keyWriter.accept(this, entry.getKey());
            valueWriter.accept(this, entry.getValue());
        }
    }

    /**
     * Reads a map from the input buffer using the provided key and value readers.
     * The size of the map is read first as an integer, followed by each key-value pair
     * deserialized using the provided {@code keyReader} and {@code valueReader}.
     * <p>
     * Example usage:
     * <pre>
     * Map<String, Integer> scores = serializer.readMap(
     *     PacketDataSerializer::readString,
     *     PacketDataSerializer::readInt);
     * // scores will contain the deserialized map of strings to integers
     * </pre>
     *
     * @param keyReader   The reader function for deserializing map keys
     * @param valueReader The reader function for deserializing map values
     * @param <K>         The type of keys in the map
     * @param <V>         The type of values in the map
     * @return The map read from the input buffer
     * @throws IllegalStateException if the serializer is not in reading mode
     */
    public <K, V> Map<K, V> readMap(ThrowingFunction<PacketDataSerializer, K> keyReader,
                                    ThrowingFunction<PacketDataSerializer, V> valueReader) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        int size = readInt();
        if (size < 0) {
            throw new PacketSerializationException("Invalid map size: " + size);
        }
        Map<K, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            K key = keyReader.apply(this);
            V value = valueReader.apply(this);
            map.put(key, value);
        }
        return map;
    }


    /**
     * Writes a UUID to the output buffer.
     * The UUID is serialized by writing its most significant bits and least significant bits
     * as two long values.
     * <p>
     * Example usage:
     * <pre>
     * UUID playerId = UUID.randomUUID();
     * serializer.writeUUID(playerId);
     * </pre>
     *
     * @param uuid The UUID to write to the output buffer
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public void writeUUID(UUID uuid) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * Reads a UUID from the input buffer.
     * The UUID is deserialized by reading its most significant bits and least significant bits
     * as two long values and reconstructing the UUID.
     * <p>
     * Example usage:
     * <pre>
     * UUID playerId = serializer.readUUID();
     * // playerId will contain the deserialized UUID
     * </pre>
     *
     * @return The UUID read from the input buffer
     * @throws IllegalStateException if the serializer is not in reading mode
     */
    public UUID readUUID() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        long mostSigBits = readLong();
        long leastSigBits = readLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    /**
     * Writes an Optional value to the output buffer using the provided value writer.
     * A boolean is written first to indicate whether the value is present. If the value
     * is present, it is serialized using the provided {@code valueWriter}.
     * <p>
     * Example usage:
     * <pre>
     * Optional<String> nickname = Optional.of("Alice");
     * serializer.writeOptional(nickname, PacketDataSerializer::writeString);
     * </pre>
     *
     * @param optional    The Optional value to write to the output buffer
     * @param valueWriter The writer function for serializing the value, if present
     * @param <T>         The type of the value in the Optional
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public <T> void writeOptional(@SuppressWarnings("all") Optional<T> optional, BiConsumer<PacketDataSerializer, T> valueWriter) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeBoolean(optional.isPresent());
        optional.ifPresent(t -> valueWriter.accept(this, t));
    }

    /**
     * Reads an Optional value from the input buffer using the provided value reader.
     * A boolean is read first to determine whether the value is present. If the value
     * is present, it is deserialized using the provided {@code valueReader}.
     * <p>
     * Example usage:
     * <pre>
     * Optional<String> nickname = serializer.readOptional(PacketDataSerializer::readString);
     * // nickname will contain the deserialized Optional value
     * </pre>
     *
     * @param valueReader The reader function for deserializing the value, if present
     * @param <T>         The type of the value in the Optional
     * @return The Optional value read from the input buffer
     * @throws IllegalStateException if the serializer is not in reading mode
     */
    public <T> Optional<T> readOptional(ThrowingFunction<PacketDataSerializer, T> valueReader) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        boolean isPresent = readBoolean();
        return isPresent ? Optional.of(valueReader.apply(this)) : Optional.empty();
    }

    /**
     * Writes a byte array to the output buffer.
     * The length of the array is written first as an integer, followed by each byte
     * in the array.
     * <p>
     * Example usage:
     * <pre>
     * byte[] data = new byte[] { 0x01, 0x02, 0x03 };
     * serializer.writeByteArray(data);
     * </pre>
     *
     * @param bytes The byte array to write to the output buffer
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public void writeByteArray(byte[] bytes) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeInt(bytes.length);
        try {
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * Reads a byte array from the input buffer.
     * The length of the array is read first as an integer, followed by each byte
     * in the array.
     * <p>
     * Example usage:
     * <pre>
     * byte[] data = serializer.readByteArray();
     * // data will contain the deserialized byte array
     * </pre>
     *
     * @return The byte array read from the input buffer
     * @throws IllegalStateException if the serializer is not in reading mode
     */
    public byte[] readByteArray() throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        int length = readInt();
        if (length < 0) {
            throw new PacketSerializationException("Invalid byte array length: " + length);
        }
        byte[] bytes = new byte[length];
        try {
            input.readFully(bytes);
        } catch (Exception e) {
            if (e instanceof EOFException) {
                throw new PacketSerializationException("Unexpected end of input while reading byte array", e);
            }
            throw new PacketSerializationException("Error reading byte array", e);
        }
        return bytes;
    }

    /**
     * Writes a queue to the output buffer using the provided element writer.
     * The size of the queue is written first as an integer, followed by each element
     * serialized using the provided {@code elementWriter} in the order they are polled.
     * <p>
     * Example usage:
     * <pre>
     * Queue<String> messages = new ArrayDeque<>(Arrays.asList("First", "Second", "Third"));
     * serializer.writeQueue(messages, PacketDataSerializer::writeString);
     * </pre>
     *
     * @param queue         The queue to write to the output buffer
     * @param elementWriter The writer function for serializing individual elements of the queue
     * @param <T>           The type of elements in the queue
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public <T> void writeQueue(Queue<T> queue, BiConsumer<PacketDataSerializer, T> elementWriter) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeInt(queue.size());
        for (T element : queue) {
            elementWriter.accept(this, element);
        }
    }

    /**
     * Reads a queue from the input buffer using the provided element reader.
     * The size of the queue is read first as an integer, followed by each element
     * deserialized using the provided {@code elementReader} and added to the queue.
     * <p>
     * Example usage:
     * <pre>
     * Queue<String> messages = serializer.readQueue(PacketDataSerializer::readString);
     * // messages will contain the deserialized queue of strings
     * </pre>
     *
     * @param elementReader The reader function for deserializing individual elements of the queue
     * @param <T>           The type of elements in the queue
     * @return The queue read from the input buffer, implemented as an {@link ArrayDeque}
     * @throws IllegalStateException if the serializer is not in reading mode
     */
    public <T> Queue<T> readQueue(ThrowingFunction<PacketDataSerializer, T> elementReader) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        int size = readInt();
        if (size < 0) {
            throw new PacketSerializationException("Invalid queue size: " + size);
        }
        Queue<T> queue = new ArrayDeque<>(size);
        for (int i = 0; i < size; i++) {
            queue.add(elementReader.apply(this));
        }
        return queue;
    }

    /**
     * Writes a nullable value to the output buffer using the provided value writer.
     * A boolean is written first to indicate whether the value is present (true) or null (false).
     * If the value is not null, it is serialized using the provided {@code valueWriter}.
     * <p>
     * Example usage:
     * <pre>
     * String nickname = "Alice";
     * serializer.writeNullable(nickname, PacketDataSerializer::writeString);
     * serializer.writeNullable(null, PacketDataSerializer::writeString);
     * </pre>
     *
     * @param value       The nullable value to write to the output buffer
     * @param valueWriter The writer function for serializing the value, if not null
     * @param <T>         The type of the value
     * @throws IllegalStateException if the serializer is not in writing mode
     */
    public <T> void writeNullable(T value, ThrowingBiConsumer<PacketDataSerializer, T> valueWriter) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        writeBoolean(value != null);
        if (value != null) {
            try {
                valueWriter.accept(this, value);
            } catch (PacketSerializationException e) {
                e.printStackTrace(System.out);
            }
        }
    }

    /**
     * Reads a nullable value from the input buffer using the provided value reader.
     * A boolean is read first to determine whether the value is present (true) or null (false).
     * If the value is present, it is deserialized using the provided {@code valueReader}; otherwise, null is returned.
     * <p>
     * Example usage:
     * <pre>
     * String nickname = serializer.readNullable(PacketDataSerializer::readString);
     * // nickname will be the deserialized value or null
     * </pre>
     *
     * @param valueReader The reader function for deserializing the value, if present
     * @param <T>         The type of the value
     * @return The nullable value read from the input buffer, or null if no value was written
     * @throws IllegalStateException        if the serializer is not in reading mode
     * @throws PacketSerializationException if an error occurs during deserialization
     */
    public <T> T readNullable(ThrowingFunction<PacketDataSerializer, T> valueReader) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        boolean isPresent = readBoolean();
        return isPresent ? valueReader.apply(this) : null;
    }

    /**
     * Writes a custom serializable object to the output buffer.
     *
     * @param object The custom serializable object to write
     * @param <T>    The type of the custom serializable object
     * @throws IllegalStateException if not in writing mode
     */
    public <T extends CustomSerializable> void writeCustom(T object) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        object.serialize(this);
    }

    /**
     * Reads a custom serializable object from the input buffer.
     * The provided class must implement CustomSerializable and have a no-args constructor.
     * Accessibility is enforced via reflection, so the constructor need not be public.
     *
     * @param clazz The class of the custom serializable object
     * @param <T>   The type of the custom serializable object
     * @return The custom serializable object read from the buffer
     * @throws IllegalStateException        if not in reading mode
     * @throws PacketSerializationException if there is an error instantiating the custom object
     */
    public <T extends CustomSerializable> T readCustom(Class<T> clazz) throws PacketSerializationException {
        if (isNotReading()) throw new IllegalStateException(NOT_READING_ERROR);
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
            instance.deserialize(this);
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new PacketSerializationException("Failed to instantiate custom object", e);
        }
    }

    public void write(byte[] content) {
        if (isNotWriting()) throw new IllegalStateException(NOT_WRITING_ERROR);
        try {
            output.write(content);
        } catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }
}