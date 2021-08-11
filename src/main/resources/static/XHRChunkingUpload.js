function XHRChunkingUploder(beginUrl,transferUrl,endUrl) {
  this.transferUrl = transferUrl;
  this.beginUrl = beginUrl;
  this.endUrl = endUrl;
}

XHRChunkingUploder.prototype.uploadFile = function (files) {
  var file = files[0];
  console.log("XHRChunkingUploder Sending " + file.name);
  this.initaiteChunkUpload(file);
};

XHRChunkingUploder.prototype.initaiteChunkUpload = function(file) {
     //console.log("begin---------------------")
  metadata ={
    fileName: file.name,
    seq: 0,
    start: 0,
    chunkSize: 1024*1024,
    totalSent: 0,
    sessionId: getUniqueSessionId(file.name),
  };

  var xhr = new XMLHttpRequest();   
    xhr.open("POST", this.beginUrl);
    xhr.setRequestHeader("X-File-Name", metadata.fileName);
    xhr.setRequestHeader("X-Upload-Session-id", `${metadata.sessionId}`);
    xhr.send();

       xhr.onreadystatechange = ()=> {//Call a function when the state changes.
       if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
           this.uploadChunk(file,metadata);
       }
   };

}





XHRChunkingUploder.prototype.uploadChunk = function (file, metadata) {
  //console.log("begin---------------------")
  metadata = metadata || {
    fileName: file.name,
    seq: 0,
    start: 0,
    chunkSize: 1024*1024,
    totalSent: 0,
    sessionId: getUniqueSessionId(file.name),
  };


  var chunkParts = Math.ceil(file.size / metadata.chunkSize);
  for (let i = 0; i < chunkParts; i += 1) {
    var xhr = new XMLHttpRequest();    
    xhr.open("POST", this.transferUrl);
    xhr.setRequestHeader("X-File-Name", metadata.fileName);
    xhr.setRequestHeader("X-Upload-Session-id", `${metadata.sessionId}`);
    xhr.setRequestHeader("X-File-Chunk-Sequence", metadata.seq);

    if (i == chunkParts - 1) {
        xhr.setRequestHeader("X-File-Last", true + "");
        metadata.end = file.size;
      } else {
        xhr.setRequestHeader("X-File-Last", false + "");
        metadata.end = metadata.chunkSize * (i + 1);
      }
  
    var chunk = file.slice(metadata.start,metadata.end);

    xhr.send(chunk);

    
    metadata.seq += 1;
    totalSent = metadata.end;
    metadata.start=totalSent;
  }

  setTimeout(() => {
      this.finishChunkUpload(file,metadata);
  }, 100);

};


XHRChunkingUploder.prototype.finishChunkUpload = function(file,metadata) {
  //console.log("begin---------------------")
  var xhr = new XMLHttpRequest();   
 xhr.open("POST", this.endUrl);
 xhr.setRequestHeader("X-File-Name", metadata.fileName);
 xhr.setRequestHeader("X-Upload-Session-id", `${metadata.sessionId}`);
 xhr.setRequestHeader("X-File-Chunk-Parts", metadata.seq);
 xhr.send();

    xhr.onreadystatechange = function() {//Call a function when the state changes.
    if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
      console.log('File Uploaded '+file.name, xhr.responseText);
    }
};

}



window.xhrChunker = new XHRChunkingUploder(
  "http://" + window.location.host + "/start-chunk-upload",
  "http://" + window.location.host + "/chunk-upload",
  "http://" + window.location.host + "/end-chunk-upload"
);

//
//XHRChunkingUploder.prototype.uploadChunk = function(file, metadata) {
//console.log("begin---------------------")
//    metadata = metadata || { fileName: file.name, seq: 0, start: 0, chunkSize: Math.min(4096, file.size), totalSent: 0, sessionId: getUniqueSessionId(file.name)};
//    console.log(metadata);
//
//    console.log()
//
//    var end = metadata.start + metadata.chunkSize - 1;
//    var chunk = file.slice(metadata.start, end);
//
//    var isLast = (file.size === end);
//
//    var xhr = new XMLHttpRequest();
//    xhr.open("POST", this.url);
//    xhr.setRequestHeader("X-File-Name", metadata.fileName);
//    xhr.setRequestHeader("X-Upload-Session-id", metadata.sessionId);
//    xhr.setRequestHeader("X-File-Chunk-Sequence", metadata.seq+"");
//    xhr.setRequestHeader("X-File-Last", isLast+"");
//    console.log(chunk);
//    xhr.send(chunk);
//    xhr.onreadystatechange = function() {//Call a function when the state changes.
//        if(xhr.readyState == XMLHttpRequest.DONE && xhr.status == 200) {
//            console.log('Chunk Uploaded '+file.name, xhr.responseText);
//        }
//    };
//
//    console.log(metadata.start, end, metadata);
//console.log("Reloop---------------------")
//    if (!isLast) {
//        metadata.totalSent+=chunk.size;
//        metadata.start = metadata.start +  metadata.chunkSize;
//        metadata.chunkSize = Math.min(metadata.chunkSize, file.size - metadata.totalSent);
//        metadata.seq++;
//        this.uploadChunk(file, metadata);
//    }
//
//
//};
