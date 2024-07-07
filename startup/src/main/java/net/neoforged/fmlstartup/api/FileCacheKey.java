/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fmlstartup.api;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public record FileCacheKey(String filename, long size, long lastModified) implements Serializable {
    public FileCacheKey {
        filename = Objects.requireNonNull(filename, "filename");
    }

    public static FileCacheKey read(DataInput in) throws IOException {
        return new FileCacheKey(in.readUTF(), in.readLong(), in.readLong());
    }

    public void write(DataOutput out) throws IOException {
        out.writeUTF(filename);
        out.writeLong(size);
        out.writeLong(lastModified);
    }

    public Object[] parcel() {
        return new Object[] { filename, size, lastModified };
    }

    public static FileCacheKey unparcel(Object[] parcel) {
        return new FileCacheKey(
                (String) parcel[0],
                (long) parcel[1],
                (long) parcel[2]);
    }
}
