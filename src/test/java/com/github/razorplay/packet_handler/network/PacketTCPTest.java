package com.github.razorplay.packet_handler.network;

import com.github.razorplay.packet_handler.exceptions.PacketInstantiationException;
import com.github.razorplay.packet_handler.exceptions.PacketRegistrationException;
import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.network_util.PacketDataSerializer;
import com.github.razorplay.packet_handler.network.packet.EmptyPacket;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class PacketTCPTest {

    @BeforeEach
    public void setup() {
        PacketTCP.PACKET_REGISTRY.clear();
    }

    @Test
    public void testRegisterAndSerializeEmptyPacket() throws PacketSerializationException, PacketInstantiationException {
        PacketTCP.registerPackets(EmptyPacket.class);

        EmptyPacket packet = new EmptyPacket();
        byte[] data = PacketTCP.write(packet);

        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        IPacket result = PacketTCP.read(in);

        assertTrue(result instanceof EmptyPacket);
    }

    @Test
    public void testDuplicateRegistration() {
        PacketTCP.registerPackets(EmptyPacket.class);
        assertThrows(PacketRegistrationException.class, () ->
                PacketTCP.registerPacket("EmptyPacket", EmptyPacket.class));
    }

    @Test
    public void testCustomPacket() throws PacketSerializationException, PacketInstantiationException {
        PacketTCP.registerPackets(TestPacket.class);

        TestPacket original = new TestPacket("TestData");
        byte[] data = PacketTCP.write(original);

        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        IPacket result = PacketTCP.read(in);

        assertTrue(result instanceof TestPacket);
        assertEquals("TestData", ((TestPacket) result).getData());
    }

    @Test
    public void testMultiplePacketRegistration() throws PacketSerializationException, PacketInstantiationException {
        PacketTCP.registerPackets(EmptyPacket.class, TestPacket.class);

        // Prueba EmptyPacket
        EmptyPacket emptyPacket = new EmptyPacket();
        byte[] emptyData = PacketTCP.write(emptyPacket);
        IPacket emptyResult = PacketTCP.read(ByteStreams.newDataInput(emptyData));
        assertTrue(emptyResult instanceof EmptyPacket);

        // Prueba TestPacket
        TestPacket testPacket = new TestPacket("MultiTest");
        byte[] testData = PacketTCP.write(testPacket);
        IPacket testResult = PacketTCP.read(ByteStreams.newDataInput(testData));
        assertTrue(testResult instanceof TestPacket);
        assertEquals("MultiTest", ((TestPacket) testResult).getData());
    }

    @Test
    public void testUnregisteredPacket() {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        PacketDataSerializer serializer = new PacketDataSerializer(out);
        serializer.writeString("UnregisteredPacket"); // ID no registrado
        serializer.writeInt(0); // Tamaño de datos (vacío)

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        assertThrows(PacketInstantiationException.class, () -> PacketTCP.read(in));
    }

    @Test
    public void testCorruptedPacketData() {
        PacketTCP.registerPackets(TestPacket.class);

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("TestPacket"); // Escribe el ID del paquete en formato writeUTF
        PacketDataSerializer serializer = new PacketDataSerializer(out);
        serializer.writeInt(4); // Longitud de la cadena (4 bytes)
        out.write(0x01); // Solo 1 byte de datos, insuficiente

        ByteArrayDataInput in = ByteStreams.newDataInput(out.toByteArray());
        assertThrows(PacketSerializationException.class, () -> PacketTCP.read(in));
    }

    @Test
    public void testWriteNullPacket() {
        assertThrows(NullPointerException.class, () -> PacketTCP.write(null));
    }

    @Test
    public void testLargeCustomPacket() throws PacketSerializationException, PacketInstantiationException {
        PacketTCP.registerPackets(TestPacket.class);

        StringBuilder largeData = new StringBuilder(65535);
        for (int i = 0; i < 65535; i++) {
            largeData.append('A');
        }
        TestPacket largePacket = new TestPacket(largeData.toString());
        byte[] data = PacketTCP.write(largePacket);

        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        IPacket result = PacketTCP.read(in);

        assertTrue(result instanceof TestPacket);
        assertEquals(largeData.toString(), ((TestPacket) result).getData());
    }

    @Test
    public void testRegisterPacketsWithNull() {
        assertThrows(IllegalArgumentException.class, () -> PacketTCP.registerPackets((Class<? extends IPacket>[]) null));
    }

    @Test
    public void testRegisterPacketsWithEmptyArray() {
        assertThrows(IllegalArgumentException.class, () -> PacketTCP.registerPackets());
    }

    // Tests para compresión/descompresión

    @Test
    public void testCompressedEmptyPacket() throws PacketSerializationException, PacketInstantiationException {
        PacketTCP.registerPackets(EmptyPacket.class);

        EmptyPacket packet = new EmptyPacket();
        byte[] compressedData = PacketTCP.writeCompressed(packet);

        IPacket result = PacketTCP.readCompressed(compressedData);

        assertTrue(result instanceof EmptyPacket);
    }

    @Test
    public void testCompressedCustomPacket() throws PacketSerializationException, PacketInstantiationException {
        PacketTCP.registerPackets(TestPacket.class);

        TestPacket original = new TestPacket("CompressedTestData");
        byte[] compressedData = PacketTCP.writeCompressed(original);

        IPacket result = PacketTCP.readCompressed(compressedData);

        assertTrue(result instanceof TestPacket);
        assertEquals("CompressedTestData", ((TestPacket) result).getData());
    }

    @Test
    public void testCompressedLargePacket() throws PacketSerializationException, PacketInstantiationException {
        PacketTCP.registerPackets(TestPacket.class);

        // Usar un tamaño manejable que se beneficie de la compresión pero no exceda límites iniciales
        StringBuilder largeData = new StringBuilder(50000);
        for (int i = 0; i < 50000; i++) {
            largeData.append('B');
        }
        TestPacket largePacket = new TestPacket(largeData.toString());
        byte[] compressedData = PacketTCP.writeCompressed(largePacket);
        byte[] uncompressedData = PacketTCP.write(largePacket);

        // Verificar que los datos comprimidos son más pequeños
        assertTrue(compressedData.length < uncompressedData.length, "Compressed data should be smaller than uncompressed");

        IPacket result = PacketTCP.readCompressed(compressedData);

        assertTrue(result instanceof TestPacket);
        assertEquals(largeData.toString(), ((TestPacket) result).getData());
    }

    @Test
    public void testCompressedPacketExceedsSizeLimit() throws PacketSerializationException {
        PacketTCP.registerPackets(TestPacket.class);

        // Generar datos aleatorios para minimizar la compresión
        Random random = new Random();
        StringBuilder largeData = new StringBuilder(2000000); // 2M caracteres
        for (int i = 0; i < 2000000; i++) {
            largeData.append((char) (random.nextInt(94) + 32)); // Caracteres imprimibles ASCII aleatorios
        }
        TestPacket largePacket = new TestPacket(largeData.toString());

        assertThrows(PacketSerializationException.class, () -> {
            PacketTCP.writeCompressed(largePacket);
        });
    }

    @Test
    public void testCorruptedCompressedData() throws PacketSerializationException {
        PacketTCP.registerPackets(TestPacket.class);

        // Crear datos comprimidos válidos
        TestPacket packet = new TestPacket("TestData");
        byte[] compressedData = PacketTCP.writeCompressed(packet);

        // Corromper los datos (modificar un byte en el medio)
        compressedData[compressedData.length / 2] = (byte) 0xFF;

        assertThrows(PacketSerializationException.class, () -> PacketTCP.readCompressed(compressedData));
    }

    @Test
    public void testWriteCompressedNullPacket() {
        assertThrows(NullPointerException.class, () -> PacketTCP.writeCompressed(null));
    }
}

// Paquete personalizado para pruebas
class TestPacket implements IPacket {
    private String data;

    public TestPacket() {
    } // Constructor vacío requerido

    public TestPacket(String data) {
        this.data = data;
    }

    @Override
    public void read(PacketDataSerializer serializer) throws PacketSerializationException {
        this.data = serializer.readString();
    }

    @Override
    public void write(PacketDataSerializer serializer) throws PacketSerializationException {
        serializer.writeString(this.data);
    }

    public String getData() {
        return data;
    }
}