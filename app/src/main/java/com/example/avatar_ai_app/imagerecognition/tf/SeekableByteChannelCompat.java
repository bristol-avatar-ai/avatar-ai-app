package com.example.avatar_ai_app.imagerecognition.tf;
/* Copyright 2020 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;

/**
 * A byte channel that maintains a current <i>position</i> and allows the position to be changed.
 * {@link SeekableByteChannelCompat} is compatible with {@link
 * java.nio.channels.SeekableByteChannel}.
 *
 * <p>{@link java.nio.channels.SeekableByteChannel} is not available in Android API 23 and under.
 * Therefore, {@link SeekableByteChannelCompat} is introduced here to make the interfaces used in
 * the MetadtaExtractor library consistent with the common used Java libraries.
 */
interface SeekableByteChannelCompat extends Channel {
    /**
     * Reads a sequence of bytes from this channel into the given buffer.

     * @throws IOException If some other I/O error occurs
     */
    int read(ByteBuffer dst) throws IOException;

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * @param src The buffer from which bytes are to be retrieved
     * @return The number of bytes written, possibly zero
     * @throws IOException If some other I/O error occurs
     */
    int write(ByteBuffer src) throws IOException;

    /**
     * Returns this channel's position.
     *
     * @return This channel's position, a non-negative integer counting the number of bytes from the
     *     beginning of the entity to the current position
     * @throws IOException If some other I/O error occurs
     */
    long position() throws IOException;

    /**
     * Sets this channel's position.
     *
     * @param newPosition The new position, a non-negative integer counting the number of bytes from
     *     the beginning of the entity
     * @return This channel
     * @throws IOException If some other I/O error occurs
     */
    SeekableByteChannelCompat position(long newPosition) throws IOException;

    /**
     * Returns the current size of entity to which this channel is connected.
     *
     * @return The current size, measured in bytes
     * @throws IOException If some other I/O error occurs
     */
    long size() throws IOException;

    /**
     * Truncates the entity, to which this channel is connected, to the given size.
     *
     * @param size The new size, a non-negative byte count
     * @return This channel
     * @throws IllegalArgumentException If the new size is negative
     * @throws IOException If some other I/O error occurs
     */
    SeekableByteChannelCompat truncate(long size) throws IOException;
}