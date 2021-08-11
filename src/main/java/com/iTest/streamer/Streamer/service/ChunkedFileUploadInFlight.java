package com.iTest.streamer.Streamer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChunkedFileUploadInFlight {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    String name;
    String uploadSessionId;
    Map <Integer, byte[]> orderedChunks = new ConcurrentHashMap <>();
    public AtomicInteger chunksWritten;
    private AtomicBoolean isCompleted;
    private AtomicInteger totalParts;

    /**
     * @param name
     * @param uploadSession
     */
    public ChunkedFileUploadInFlight ( String name, String uploadSession ) {
        this.name = name;
        this.uploadSessionId = uploadSession;

        this.chunksWritten = new AtomicInteger(0);




        this.isCompleted = new AtomicBoolean(false);
        this.totalParts = new AtomicInteger(0);
        logger.info("Preparing upload for " + this.name + " uploadSessionId " + uploadSessionId);
    }

    public void addChunk ( Integer packetNumber, byte[] chunk ) {

        this.orderedChunks.put(packetNumber, chunk);
        this.chunksWritten.incrementAndGet();
    }

    public byte[] getAllBytes ( Integer size ) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        this.orderedChunks.values()
//                .forEach(
//                        bytes ->
//                        {
//                            try {
//                                bos.write(bytes);
//                            } catch (IOException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                );


        for ( int i = 0 ; i < size ; i++ ) {
            try {
                bos.write(this.orderedChunks.get(i));
            } catch (IOException e) {
                e.printStackTrace();
                ;
            }
        }
        return bos.toByteArray();
    }


    public boolean getIsCompleted(){
        return isCompleted.get();
    }

    public void setIsCompleted(boolean value){
        isCompleted.set(value);
    }


    public Integer getTotalParts () {
        return totalParts.get();
    }

    public void setTotalParts ( Integer totalParts ) {
        this.totalParts.set(totalParts);
    }
}
