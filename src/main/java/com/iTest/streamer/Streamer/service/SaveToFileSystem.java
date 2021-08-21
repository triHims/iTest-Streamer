package com.iTest.streamer.Streamer.service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class SaveToFileSystem {

    public static void save(String fileName, String sessionId , String prefix, byte[] data) throws IOException {
         SaveToFileSystem.save(fileName,sessionId , prefix, ByteBuffer.wrap(data));
    }

    public static void save(String fileName, String sessionId , String prefix, ByteBuffer bufferedBytes) throws IOException {
        Path basePath = Paths.get(".", "uploads", prefix,sessionId);
        Files.createDirectories(basePath);

        FileChannel channel =  new FileOutputStream(Paths.get(basePath.toString(), fileName).toFile(), false).getChannel();
        channel.write(bufferedBytes);
        channel.close();
    }
}
