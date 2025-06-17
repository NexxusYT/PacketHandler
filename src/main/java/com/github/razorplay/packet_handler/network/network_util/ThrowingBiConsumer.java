package com.github.razorplay.packet_handler.network.network_util;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;

@FunctionalInterface
public interface ThrowingBiConsumer<T, R> {
    
    void accept(T t, R r) throws PacketSerializationException;
}