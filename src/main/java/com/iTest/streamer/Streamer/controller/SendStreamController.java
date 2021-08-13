package com.iTest.streamer.Streamer.controller;

import com.iTest.streamer.Streamer.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@CrossOrigin(origins = {"http://localhost:4400","http://localhost:4200", "http://localhost:14060", "http://10.81.64.15:14060", "http://10.81.64.15:14059", "http://10.81.64.15:14059", "http://localhost:8080"})
public class SendStreamController {
    @Autowired
    private ResourceService resourceService;
    @GetMapping("/recording/{folder}/{name}")
    ResponseEntity <ResourceRegion> getFullVideo(@PathVariable String folder, @PathVariable String name, @RequestHeader("Range") String range ,@RequestHeader HttpHeaders headers) throws IOException {

        Path basePath = Paths.get(".", "uploads", "chunk-upload", folder,name);
        UrlResource video = new UrlResource(basePath.toUri());
        ResourceRegion region = resourceService.resourceRegion(video, range);
        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaTypeFactory
                        .getMediaType(video)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM))
                .body(region);
    }



}
