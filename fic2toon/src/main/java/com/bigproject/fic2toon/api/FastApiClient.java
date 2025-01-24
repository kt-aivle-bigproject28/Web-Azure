package com.bigproject.fic2toon.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "fastApiClient", url = "${webtoon.api.host}")
public interface FastApiClient {
    @PostMapping(value = "/text_to_webtoon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    String textToWebtoon(@RequestPart("text") MultipartFile text);
}