package com.github.razorplay.packet_handler.network.reflection.field.criteria;

import com.github.razorplay.packet_handler.network.reflection.field.FieldEncoderGetter;

public interface FieldCriteriaSerializer<T> extends FieldCriteriaMatcher, FieldEncoderGetter<T> {

    int getPriority();
}
