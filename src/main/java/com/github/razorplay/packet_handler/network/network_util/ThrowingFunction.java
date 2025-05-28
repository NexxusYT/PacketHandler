package com.github.razorplay.packet_handler.network.network_util;

import com.github.razorplay.packet_handler.exceptions.PacketSerializationException;

@FunctionalInterface
public interface ThrowingFunction<T, R> {
    R apply(T t) throws PacketSerializationException;
}