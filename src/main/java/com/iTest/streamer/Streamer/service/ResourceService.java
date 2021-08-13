package com.iTest.streamer.Streamer.service;

import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ResourceService {
    public ResourceRegion resourceRegion ( UrlResource video, String range ) throws IOException {
        long contentLength = video.contentLength();
        String[] ranges = range.substring("bytes=".length()).split("-");
        int rangeLength;

        int from = Integer.parseInt(ranges[0]);
        int to;
        int localLen;
        if(ranges.length > 1) {
            to = Integer.parseInt(ranges[1]);
            localLen = (to - from + 1);
        } else {
            localLen = (int)contentLength;
        }

        rangeLength = Math.min(1024 * 1024, localLen);
        return new ResourceRegion(video, from, rangeLength);

    }
}
