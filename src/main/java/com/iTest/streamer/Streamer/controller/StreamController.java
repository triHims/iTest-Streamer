package com.iTest.streamer.Streamer.controller;


import com.iTest.streamer.Streamer.exception.BadInputStream;
import com.iTest.streamer.Streamer.service.ChunkedFileUploadInFlight;
import com.iTest.streamer.Streamer.service.SaveToFileSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@Slf4j
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:14060", "http://10.81.64.15:14060", "http://10.81.64.15:14059", "http://10.81.64.15:14059", "http://localhost:8080"})
public class StreamController {
    static Map <String, ChunkedFileUploadInFlight> sessionToFileMap = new ConcurrentHashMap <>();

    @PostMapping(value = "/start-chunk-upload", produces = "application/json")
    @ResponseBody
    public ResponseEntity <String> beginChunkedUpload (
            @RequestHeader("X-Upload-Session-id") String uploadSessionId,
            @RequestHeader("X-File-Name") String fileName
    ) throws IOException {
        ChunkedFileUploadInFlight inFlightUpload = new ChunkedFileUploadInFlight(fileName, uploadSessionId);
        sessionToFileMap.put(uploadSessionId, inFlightUpload);
        log.info(String.format("Ready for upload of fine %s  session id - %s", fileName, uploadSessionId));
        return ResponseEntity.ok().header(
                HttpHeaders.ACCEPT,
                "sessionId=\"" + uploadSessionId + "\"").body("{\"success\": true}");
    }


    @PostMapping(value = "/chunk-upload", produces = "application/json")
    @ResponseBody
    public ResponseEntity <String> handleChunkedUpload (
            @RequestBody byte[] data,
            @RequestHeader("X-Upload-Session-id") String uploadSessionId,
            @RequestHeader("X-File-Name") String fileName,
            @RequestHeader("X-File-Chunk-Sequence") Integer fileChunkSequence
    ) throws IOException, BadInputStream {

//        log.info(String.format("File name is : %s , chunk sequence is %d", fileName,fileChunkSequence));

        ChunkedFileUploadInFlight inFlightUpload = sessionToFileMap.get(uploadSessionId);
        if (inFlightUpload == null) {
            throw new BadInputStream("InputStream is not Authorized");
        }

        inFlightUpload.addChunk(fileChunkSequence, data);


        if (inFlightUpload.getIsCompleted() &&
                (inFlightUpload.chunksWritten.get() >= inFlightUpload.getTotalParts())) {

            SaveToFileSystem.save(fileName, "chunk-upload", inFlightUpload.getAllBytes(inFlightUpload.chunksWritten.get()));
            sessionToFileMap.remove(uploadSessionId);

            log.info("Chunked Upload Final " + fileName);



            return ResponseEntity.ok().header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"").body("{\"success\": true}");

        }

        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"").body("{\"chunk-success\": true}");


    }

    @PostMapping(value = "/end-chunk-upload", produces = "application/json")
    @ResponseBody
    public ResponseEntity <String> handleChunkedUpload (
            @RequestHeader("X-Upload-Session-id") String uploadSessionId,
            @RequestHeader("X-File-Name") String fileName,
            @RequestHeader("X-File-Chunk-Parts") Integer noOfParts
    ) throws IOException, BadInputStream {
        if (! sessionToFileMap.containsKey(uploadSessionId)) {
            throw new BadInputStream("No Video Stream Exists");
        }

        ChunkedFileUploadInFlight inFlightUpload = sessionToFileMap.get(uploadSessionId);

        inFlightUpload.setIsCompleted(true);
        inFlightUpload.setTotalParts(noOfParts);
        if (inFlightUpload.chunksWritten.get() >= inFlightUpload.getTotalParts()) {

            SaveToFileSystem.save(fileName, "chunk-upload",
                    inFlightUpload.getAllBytes(inFlightUpload.chunksWritten.get()));


            sessionToFileMap.remove(uploadSessionId);
            log.info("Chunked Upload Final " + fileName);



            return ResponseEntity.ok().header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + fileName + "\"").body("{\"success\": true}");

        }


        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + fileName + "\"").body("{\"chunk-success\": true}");
    }

}