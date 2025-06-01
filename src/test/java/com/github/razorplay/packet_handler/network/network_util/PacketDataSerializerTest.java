package com.github.razorplay.packet_handler.network.network_util;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class PacketDataSerializerTest {

    private PacketDataSerializer prepareSerializer(ByteArrayDataOutput out) {
        return new PacketDataSerializer(out);
    }

    private PacketDataSerializer prepareDeserializer(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        return new PacketDataSerializer(in);
    }

    @Test
    public void testPrimitiveTypes() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        // Valores extremos y típicos
        serializer.writeByte(Byte.MAX_VALUE); // 127
        serializer.writeByte(Byte.MIN_VALUE); // -128
        serializer.writeShort(Short.MAX_VALUE); // 32767
        serializer.writeShort(Short.MIN_VALUE); // -32768
        serializer.writeInt(Integer.MAX_VALUE); // 2147483647
        serializer.writeInt(Integer.MIN_VALUE); // -2147483648
        serializer.writeLong(Long.MAX_VALUE); // 9223372036854775807
        serializer.writeLong(Long.MIN_VALUE); // -9223372036854775808
        serializer.writeFloat(Float.MAX_VALUE);
        serializer.writeFloat(Float.MIN_VALUE);
        serializer.writeDouble(Double.MAX_VALUE);
        serializer.writeDouble(Double.MIN_VALUE);
        serializer.writeChar(Character.MAX_VALUE); // '\uFFFF'
        serializer.writeChar(Character.MIN_VALUE); // ''
        serializer.writeBoolean(true);
        serializer.writeBoolean(false);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertEquals(Byte.MAX_VALUE, deserializer.readByte());
        assertEquals(Byte.MIN_VALUE, deserializer.readByte());
        assertEquals(Short.MAX_VALUE, deserializer.readShort());
        assertEquals(Short.MIN_VALUE, deserializer.readShort());
        assertEquals(Integer.MAX_VALUE, deserializer.readInt());
        assertEquals(Integer.MIN_VALUE, deserializer.readInt());
        assertEquals(Long.MAX_VALUE, deserializer.readLong());
        assertEquals(Long.MIN_VALUE, deserializer.readLong());
        assertEquals(Float.MAX_VALUE, deserializer.readFloat(), 0.00001f);
        assertEquals(Float.MIN_VALUE, deserializer.readFloat(), 0.00001f);
        assertEquals(Double.MAX_VALUE, deserializer.readDouble(), 0.00001);
        assertEquals(Double.MIN_VALUE, deserializer.readDouble(), 0.00001);
        assertEquals(Character.MAX_VALUE, deserializer.readChar());
        assertEquals(Character.MIN_VALUE, deserializer.readChar());
        assertTrue(deserializer.readBoolean());
        assertFalse(deserializer.readBoolean());
    }

    @Test
    public void testStringWithNull() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);
        assertThrows(NullPointerException.class, () -> serializer.writeString(null));
    }

    @Test
    public void testString() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        // Casos: normal, vacío, largo
        serializer.writeString("Hello, Minecraft!");
        serializer.writeString(""); // Vacío

        // Generar una cadena larga compatible con Java 8
        StringBuilder longStringBuilder = new StringBuilder(65535);
        for (int i = 0; i < 65535; i++) {
            longStringBuilder.append('A');
        }
        String longString = longStringBuilder.toString();
        serializer.writeString(longString);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertEquals("Hello, Minecraft!", deserializer.readString());
        assertEquals("", deserializer.readString());
        assertEquals(longString, deserializer.readString());
    }

    @Test
    public void testCorruptedDataPrimitives() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(0x01); // Solo 1 byte, insuficiente para short, float, etc.
        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        assertThrows(PacketSerializationException.class, deserializer::readShort);
        assertThrows(PacketSerializationException.class, deserializer::readFloat);
        assertThrows(PacketSerializationException.class, deserializer::readDouble);
        assertThrows(PacketSerializationException.class, deserializer::readChar);
    }

    @Test
    public void testCorruptedDataPrimitivesEmpty() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        // Buffer vacío
        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        assertThrows(PacketSerializationException.class, deserializer::readByte);
        assertThrows(PacketSerializationException.class, deserializer::readBoolean);
        assertThrows(PacketSerializationException.class, deserializer::readShort);
        assertThrows(PacketSerializationException.class, deserializer::readFloat);
        assertThrows(PacketSerializationException.class, deserializer::readDouble);
        assertThrows(PacketSerializationException.class, deserializer::readChar);
    }

    @Test
    public void testEnum() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        // Normal, null
        serializer.writeEnum(TestEnum.VALUE1);
        serializer.writeEnum(null);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertEquals(TestEnum.VALUE1, deserializer.readEnum(TestEnum.class));
        assertNull(deserializer.readEnum(TestEnum.class));

        // Probar un valor inválido en deserialización
        ByteArrayDataOutput outInvalid = ByteStreams.newDataOutput();
        PacketDataSerializer serializerInvalid = prepareSerializer(outInvalid);
        serializerInvalid.writeString("INVALID_VALUE"); // No existe en TestEnum
        PacketDataSerializer deserializerInvalid = prepareDeserializer(outInvalid.toByteArray());
        assertThrows(PacketSerializationException.class, () -> deserializerInvalid.readEnum(TestEnum.class));
    }

    @Test
    public void testList() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        // Normal, vacío, y grande
        List<String> normalList = Arrays.asList("Apple", "Banana");
        List<String> emptyList = Collections.emptyList();
        List<Integer> largeList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) largeList.add(i);

        dataSerializer.writeList(normalList, PacketDataSerializer::writeString);
        dataSerializer.writeList(emptyList, PacketDataSerializer::writeString);
        dataSerializer.writeList(largeList, PacketDataSerializer::writeInt);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        assertEquals(normalList, deserializer.readList(PacketDataSerializer::readString));
        assertEquals(emptyList, deserializer.readList(PacketDataSerializer::readString));
        assertEquals(largeList, deserializer.readList(PacketDataSerializer::readInt));
    }

    @Test
    public void testListOrder() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        List<String> original = Arrays.asList("A", "B", "C");
        dataSerializer.writeList(original, PacketDataSerializer::writeString);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        List<String> readList = deserializer.readList(PacketDataSerializer::readString);
        assertEquals(original, readList);

        // Verificar que un orden diferente falla
        List<String> wrongOrder = Arrays.asList("C", "B", "A");
        assertNotEquals(wrongOrder, readList);
    }

    @Test
    public void testSet() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        // Normal, vacío, con duplicados (ignorados por Set)
        Set<String> normalSet = new HashSet<>(Arrays.asList("One", "Two"));
        Set<String> emptySet = new HashSet<>();
        Set<Integer> largeSet = new HashSet<>();
        for (int i = 0; i < 100; i++) largeSet.add(i);

        dataSerializer.writeSet(normalSet, PacketDataSerializer::writeString);
        dataSerializer.writeSet(emptySet, PacketDataSerializer::writeString);
        dataSerializer.writeSet(largeSet, PacketDataSerializer::writeInt);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertEquals(normalSet, deserializer.readSet(PacketDataSerializer::readString));
        assertEquals(emptySet, deserializer.readSet(PacketDataSerializer::readString));
        assertEquals(largeSet, deserializer.readSet(PacketDataSerializer::readInt));
    }

    @Test
    public void testLargeByteArray() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        byte[] massiveArray = new byte[1024 * 1024]; // 1 MB
        Arrays.fill(massiveArray, (byte) 0xAA);
        serializer.writeByteArray(massiveArray);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        assertArrayEquals(massiveArray, deserializer.readByteArray());
    }

    @Test
    public void testMap() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        // Normal, vacío, y grande
        Map<String, Integer> normalMap = new HashMap<>();
        normalMap.put("One", 1);
        normalMap.put("Two", 2);
        Map<String, String> emptyMap = new HashMap<>();
        Map<Integer, String> largeMap = new HashMap<>();
        for (int i = 0; i < 100; i++) largeMap.put(i, "Value" + i);

        dataSerializer.writeMap(normalMap, PacketDataSerializer::writeString, PacketDataSerializer::writeInt);
        dataSerializer.writeMap(emptyMap, PacketDataSerializer::writeString, PacketDataSerializer::writeString);
        dataSerializer.writeMap(largeMap, PacketDataSerializer::writeInt, PacketDataSerializer::writeString);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertEquals(normalMap, deserializer.readMap(PacketDataSerializer::readString, PacketDataSerializer::readInt));
        assertEquals(emptyMap, deserializer.readMap(PacketDataSerializer::readString, PacketDataSerializer::readString));
        assertEquals(largeMap, deserializer.readMap(PacketDataSerializer::readInt, PacketDataSerializer::readString));
    }

    @Test
    public void testMapWithNullKey() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        Map<String, Integer> mapWithNull = new HashMap<>();
        mapWithNull.put(null, 1); // Clave null
        assertThrows(NullPointerException.class, () ->
                serializer.writeMap(mapWithNull, PacketDataSerializer::writeString, PacketDataSerializer::writeInt));
    }

    @Test
    public void testUUID() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        UUID uuid = UUID.randomUUID();
        UUID zeroUUID = new UUID(0L, 0L); // Caso límite

        serializer.writeUUID(uuid);
        serializer.writeUUID(zeroUUID);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertEquals(uuid, deserializer.readUUID());
        assertEquals(zeroUUID, deserializer.readUUID());
    }

    @Test
    public void testOptional() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        Optional<String> present = Optional.of("Present");
        Optional<String> empty = Optional.empty();

        dataSerializer.writeOptional(present, PacketDataSerializer::writeString);
        dataSerializer.writeOptional(empty, PacketDataSerializer::writeString);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        Optional<String> readPresent = deserializer.readOptional(PacketDataSerializer::readString);
        assertTrue(readPresent.isPresent());
        assertEquals("Present", readPresent.get());


        Optional<String> readEmpty = deserializer.readOptional(PacketDataSerializer::readString);
        assertFalse(readEmpty.isPresent());
    }

    @Test
    public void testOptionalList() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        List<String> testingList = new ArrayList<>();
        testingList.add("One");
        testingList.add("Two");

        Optional<List<String>> optionalList = Optional.of(testingList);
        dataSerializer.writeOptional(optionalList, (serializer, list) ->
                serializer.writeList(list, PacketDataSerializer::writeString)
        );

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        Optional<List<String>> readOptionalList = deserializer.readOptional(serializer ->
                serializer.readList(PacketDataSerializer::readString)
        );

        assertTrue(readOptionalList.isPresent());

        assertEquals(2, readOptionalList.get().size());
        assertEquals("One", readOptionalList.get().get(0));
        assertEquals("Two", readOptionalList.get().get(1));
    }

    @Test
    public void testByteArray() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        byte[] normalArray = new byte[]{0x01, 0x02, 0x03};
        byte[] emptyArray = new byte[0];
        byte[] largeArray = new byte[1024];
        Arrays.fill(largeArray, (byte) 0xFF);

        serializer.writeByteArray(normalArray);
        serializer.writeByteArray(emptyArray);
        serializer.writeByteArray(largeArray);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertArrayEquals(normalArray, deserializer.readByteArray());
        assertArrayEquals(emptyArray, deserializer.readByteArray());
        assertArrayEquals(largeArray, deserializer.readByteArray());
    }

    @Test
    public void testQueue() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        Queue<String> normalQueue = new ArrayDeque<>(Arrays.asList("First", "Second"));
        Queue<String> emptyQueue = new ArrayDeque<>();
        Queue<Integer> largeQueue = new ArrayDeque<>();
        for (int i = 0; i < 100; i++) largeQueue.add(i);

        dataSerializer.writeQueue(normalQueue, PacketDataSerializer::writeString);
        dataSerializer.writeQueue(emptyQueue, PacketDataSerializer::writeString);
        dataSerializer.writeQueue(largeQueue, PacketDataSerializer::writeInt);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertIterableEquals(normalQueue, deserializer.readQueue(PacketDataSerializer::readString));
        assertIterableEquals(emptyQueue, deserializer.readQueue(PacketDataSerializer::readString));
        assertIterableEquals(largeQueue, deserializer.readQueue(PacketDataSerializer::readInt));
    }

    @Test
    public void testCustomSerializable() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        TestCustomObject normalObject = new TestCustomObject(42, "Custom");
        TestCustomObject emptyObject = new TestCustomObject(0, "");

        serializer.writeCustom(normalObject);
        serializer.writeCustom(emptyObject);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        TestCustomObject readNormal = deserializer.readCustom(TestCustomObject.class);
        assertEquals(42, readNormal.intValue);
        assertEquals("Custom", readNormal.stringValue);

        TestCustomObject readEmpty = deserializer.readCustom(TestCustomObject.class);
        assertEquals(0, readEmpty.intValue);
        assertEquals("", readEmpty.stringValue);
    }

    @Test
    public void testModeExceptions() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer writeSerializer = new PacketDataSerializer(out);
        ByteArrayDataInput in = ByteStreams.newDataInput(new byte[]{});
        PacketDataSerializer readSerializer = new PacketDataSerializer(in);

        // Intentar leer en modo escritura
        assertThrows(IllegalStateException.class, writeSerializer::readByte);
        assertThrows(IllegalStateException.class, writeSerializer::readString);

        // Intentar escribir en modo lectura
        assertThrows(IllegalStateException.class, () -> readSerializer.writeByte((byte) 1));
        assertThrows(IllegalStateException.class, () -> readSerializer.writeString("test"));
    }

    @Test
    public void testCorruptedData() {
        // Probar con datos insuficientes para un int
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeByte(0x01); // Solo 1 byte, un int necesita 4
        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        assertThrows(PacketSerializationException.class, deserializer::readInt);
    }

    @Test
    public void testCorruptedDataExtended() {
        // Datos insuficientes para long (8 bytes)
        ByteArrayDataOutput outLong = ByteStreams.newDataOutput();
        outLong.writeInt(0x1234); // Solo 4 bytes
        PacketDataSerializer deserializerLong = prepareDeserializer(outLong.toByteArray());
        assertThrows(PacketSerializationException.class, deserializerLong::readLong);

        // Datos insuficientes para string
        ByteArrayDataOutput outString = ByteStreams.newDataOutput();
        PacketDataSerializer serializerString = prepareSerializer(outString);
        serializerString.writeInt(3); // Longitud de 3 bytes
        outString.writeByte(0x41); // Solo 1 byte en lugar de 3
        PacketDataSerializer deserializerString = prepareDeserializer(outString.toByteArray());
        assertThrows(PacketSerializationException.class, deserializerString::readString);
    }

    @Test
    public void testConcurrentAccess() throws Exception {
        List<byte[]> buffers = Collections.synchronizedList(new ArrayList<>());

        Runnable writeTask = () -> {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            PacketDataSerializer serializer = prepareSerializer(out);
            for (int i = 0; i < 100; i++) {
                serializer.writeInt(i);
            }
            buffers.add(out.toByteArray());
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(writeTask);
        executor.submit(writeTask);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertEquals(2, buffers.size());

        for (byte[] buffer : buffers) {
            PacketDataSerializer deserializer = prepareDeserializer(buffer);
            for (int i = 0; i < 100; i++) {
                int value = deserializer.readInt();
                assertTrue(value >= 0 && value < 100, "Value " + value + " should be between 0 and 99");
            }
        }
    }

    @Test
    public void testModeExceptionsExtended() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer writeSerializer = new PacketDataSerializer(out);
        ByteArrayDataInput in = ByteStreams.newDataInput(new byte[]{});
        PacketDataSerializer readSerializer = new PacketDataSerializer(in);

        // Leer en modo escritura
        assertThrows(IllegalStateException.class, writeSerializer::readByte);
        assertThrows(IllegalStateException.class, writeSerializer::readString);
        assertThrows(IllegalStateException.class, writeSerializer::readInt);
        assertThrows(IllegalStateException.class, () -> writeSerializer.readList(PacketDataSerializer::readString), "Reading list in write mode");

        // Escribir en modo lectura
        assertThrows(IllegalStateException.class, () -> readSerializer.writeByte((byte) 1));
        assertThrows(IllegalStateException.class, () -> readSerializer.writeString("test"));
        assertThrows(IllegalStateException.class, () -> readSerializer.writeInt(42));
        assertThrows(IllegalStateException.class, () -> readSerializer.writeList(Collections.emptyList(), PacketDataSerializer::writeString));
    }

    @Test
    public void testListEdgeCases() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        // Lista vacía
        List<String> emptyList = Collections.emptyList();
        dataSerializer.writeList(emptyList, PacketDataSerializer::writeString);

        // Lista con un solo elemento
        List<String> singleList = Collections.singletonList("Single");
        dataSerializer.writeList(singleList, PacketDataSerializer::writeString);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        assertEquals(emptyList, deserializer.readList(PacketDataSerializer::readString));
        assertEquals(singleList, deserializer.readList(PacketDataSerializer::readString));
    }

    @Test
    public void testPerformance() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer dataSerializer = prepareSerializer(out);

        long startTime = System.nanoTime();
        List<Integer> largeList = new ArrayList<>();
        for (int i = 0; i < 10000; i++) largeList.add(i);
        dataSerializer.writeList(largeList, PacketDataSerializer::writeInt);
        long writeTime = System.nanoTime() - startTime;

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());
        startTime = System.nanoTime();
        deserializer.readList(PacketDataSerializer::readInt);
        long readTime = System.nanoTime() - startTime;

        System.out.printf("Write time: %d ns, Read time: %d ns%n", writeTime, readTime);
    }

    @Test
    public void testNullable() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = prepareSerializer(out);

        // Casos: valor presente, nulo
        String presentValue = "Present";
        String nullValue = null;

        serializer.writeNullable(presentValue, PacketDataSerializer::writeString);
        serializer.writeNullable(nullValue, PacketDataSerializer::writeString);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        String readPresent = deserializer.readNullable(PacketDataSerializer::readString);
        assertEquals("Present", readPresent);

        String readNull = deserializer.readNullable(PacketDataSerializer::readString);
        assertNull(readNull);

        // Probar datos corruptos (buffer insuficiente para la cadena)
        ByteArrayDataOutput outCorrupted = ByteStreams.newDataOutput();
        PacketDataSerializer serializerCorrupted = prepareSerializer(outCorrupted);
        serializerCorrupted.writeBoolean(true); // Indica valor presente
        serializerCorrupted.writeInt(3); // Longitud de 3 bytes
        outCorrupted.writeByte(0x41); // Solo 1 byte en lugar de 3
        PacketDataSerializer deserializerCorrupted = prepareDeserializer(outCorrupted.toByteArray());
        assertThrows(PacketSerializationException.class, () -> deserializerCorrupted.readNullable(PacketDataSerializer::readString));
    }

    @Test
    public void reflectionSerializer() throws PacketSerializationException {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        PacketDataSerializer serializer = prepareSerializer(out);
        TestCustomObject customObject = new TestCustomObject(42, "Test");

        PacketSerializer.serialize(customObject, serializer);

        PacketDataSerializer deserializer = prepareDeserializer(out.toByteArray());

        assertEquals(42, deserializer.readInt());
        assertEquals("Test", deserializer.readString());
    }
}

// Enum para pruebas
enum TestEnum {
    VALUE1, VALUE2
}

// Objeto personalizado para pruebas
class TestCustomObject implements CustomSerializable {
    int intValue;
    String stringValue;

    public TestCustomObject() {
    }

    public TestCustomObject(int intValue, String stringValue) {
        this.intValue = intValue;
        this.stringValue = stringValue;
    }

    @Override
    public void serialize(PacketDataSerializer serializer) {
        serializer.writeInt(intValue);
        serializer.writeString(stringValue);
    }

    @Override
    public void deserialize(PacketDataSerializer serializer) throws PacketSerializationException {
        this.intValue = serializer.readInt();
        this.stringValue = serializer.readString();
    }
}