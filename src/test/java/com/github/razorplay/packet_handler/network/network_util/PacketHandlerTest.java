package com.github.razorplay.packet_handler.network.network_util;

import com.github.razorplay.packet_handler.exceptions.PacketInstantiationException;
import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;
import com.github.razorplay.packet_handler.network.PacketTCP;
import com.github.razorplay.packet_handler.network.packet.SimplePacket;
import com.github.razorplay.packet_handler.network.packet.annotation.PacketHandler;
import com.github.razorplay.packet_handler.network.packet.listener.PacketHandlerRegistry;
import com.github.razorplay.packet_handler.network.packet.listener.PacketListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PacketHandlerTest {

    // Shared state to track handler invocations
    public static int calledCode = -1;

    /**
     * Resets the shared state before each test to ensure isolation.
     */
    @BeforeEach
    public void setup() {
        calledCode = -1;
        // Clear any existing listeners to avoid interference
        PacketHandlerRegistry.clear();
        PacketTCP.clearRegisteredPackets();

        PacketTCP.registerPackets(DefaultPacket.class);

    }

    /**
     * Tests basic packet registration and invocation with a single listener.
     */
    @Test
    public void testSingleListenerInvocation() {
        // Arrange: Register a listener
        PacketHandlerRegistry.register(new TestListener());

        // Act: Invoke a packet
        PacketHandlerRegistry.invoke(new DefaultPacket(1));

        // Assert: Verify the listener was called with the correct code
        assertEquals(1, calledCode, "Listener should be invoked with packet code 1");
    }

    /**
     * Tests that multiple listeners are invoked for the same packet.
     */
    @Test
    public void testMultipleListenersInvocation() {
        // Arrange: Register two listeners
        PacketHandlerRegistry.register(new TestListener());
        PacketHandlerRegistry.register(new SecondTestListener());

        // Act: Invoke a packet
        PacketHandlerRegistry.invoke(new DefaultPacket(2));

        // Assert: Verify both listeners were called
        assertEquals(2, calledCode, "First listener should be invoked with packet code 2");
        assertEquals(2, SecondTestListener.secondCalledCode, "Second listener should be invoked with packet code 2");
    }

    /**
     * Tests that invoking a packet with no registered listeners does not cause errors.
     */
    @Test
    public void testNoListenersRegistered() {
        // Act: Invoke a packet with no listeners
        PacketHandlerRegistry.invoke(new DefaultPacket(3));

        // Assert: Verify no side effects (calledCode remains unchanged)
        assertEquals(-1, calledCode, "No listeners should be invoked when none are registered");
    }

    /**
     * Tests that registering a null listener throws an appropriate exception.
     */
    @Test
    public void testRegisterNullListener() {
        // Act & Assert: Verify null listener registration fails
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            PacketHandlerRegistry.register(null);
        }, "Registering a null listener should throw NullPointerException");

        assertEquals("Listener cannot be null", exception.getMessage(), "Exception message should be descriptive");
    }

    /**
     * Tests that invoking a null packet throws an appropriate exception.
     */
    @Test
    public void testInvokeNullPacket() {
        // Arrange: Register a listener
        PacketHandlerRegistry.register(new TestListener());

        // Act & Assert: Verify null packet invocation fails
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            PacketHandlerRegistry.invoke(null);
        }, "Invoking a null packet should throw NullPointerException");

        assertEquals("Packet cannot be null", exception.getMessage(), "Exception message should be descriptive");
    }

    /**
     * Tests packet serialization and deserialization.
     */
    @Test
    public void testPacketSerialization() throws PacketSerializationException, PacketInstantiationException {
        // Arrange: Create a packet and a serializer
        DefaultPacket packet = new DefaultPacket(42);
        byte[] data = PacketTCP.write(packet);

        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        DefaultPacket result = PacketTCP.read(in);

        // Assert: Verify the deserialized packet has the correct code
        assertEquals(42, result.code, "Deserialized packet should have the original code");
    }

    /**
     * Tests that a listener with no @PacketHandler methods is registered but does not affect invocation.
     */
    @Test
    public void testListenerWithNoPacketHandler() {
        // Arrange: Register a listener with no @PacketHandler methods
        PacketHandlerRegistry.register(new EmptyListener());

        // Act: Invoke a packet
        PacketHandlerRegistry.invoke(new DefaultPacket(5));

        // Assert: Verify no side effects
        assertEquals(-1, calledCode, "No handlers should be invoked for a listener with no @PacketHandler methods");
    }

    /**
     * Tests that a packet of an unregistered type does not trigger any listeners.
     */
    @Test
    public void testUnregisteredPacketType() {
        // Arrange: Register a listener for DefaultPacket
        PacketHandlerRegistry.register(new TestListener());

        // Act: Invoke a different packet type
        PacketHandlerRegistry.invoke(new OtherPacket(6));

        // Assert: Verify the listener was not called
        assertEquals(-1, calledCode, "Listener should not be invoked for an unregistered packet type");
    }

    // Test listener implementation
    static class TestListener implements PacketListener {

        /**
         * Handles a {@link DefaultPacket} and updates the shared state with its code.
         *
         * @param defaultPacket the packet to handle
         */
        @PacketHandler
        public void onPacket(DefaultPacket defaultPacket) {
            System.out.println("Packet received with code " + defaultPacket.code);
            PacketHandlerTest.calledCode = defaultPacket.code;
        }
    }

    // Second listener for testing multiple listeners
    static class SecondTestListener implements PacketListener {
        public static int secondCalledCode = -1;

        /**
         * Handles a {@link DefaultPacket} and updates the second listener's state.
         *
         * @param defaultPacket the packet to handle
         */
        @PacketHandler
        public void onPacket(DefaultPacket defaultPacket) {
            System.out.println("Second listener received packet with code " + defaultPacket.code);
            secondCalledCode = defaultPacket.code;
        }
    }

    // Listener with no @PacketHandler methods
    static class EmptyListener implements PacketListener {
        // No @PacketHandler methods
        public void onPacket(DefaultPacket packet) {
            // This method should not be invoked
        }
    }

    // Different packet type for testing unregistered packets
    @AllArgsConstructor
    @NoArgsConstructor
    static class OtherPacket implements SimplePacket {
        private int code;

        @Override
        public void read(PacketDataSerializer serializer) throws PacketSerializationException {
            code = serializer.readInt();
        }

        @Override
        public void write(PacketDataSerializer serializer) throws PacketSerializationException {
            serializer.writeInt(code);
        }
    }

    // Existing DefaultPacket implementation
    @AllArgsConstructor
    @NoArgsConstructor
    static class DefaultPacket implements SimplePacket {
        private int code;

        /**
         * Reads the packet's code from the serializer.
         *
         * @param serializer the serializer to read from
         * @throws PacketSerializationException if reading fails
         */
        @Override
        public void read(PacketDataSerializer serializer) throws PacketSerializationException {
            code = serializer.readInt();
        }

        /**
         * Writes the packet's code to the serializer.
         *
         * @param serializer the serializer to write to
         * @throws PacketSerializationException if writing fails
         */
        @Override
        public void write(PacketDataSerializer serializer) throws PacketSerializationException {
            serializer.writeInt(code);
        }
    }
}