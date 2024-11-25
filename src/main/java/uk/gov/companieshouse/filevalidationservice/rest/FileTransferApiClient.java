package uk.gov.companieshouse.filevalidationservice.rest;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.filetransfer.FileDetailsApi;

import java.io.IOException;

@Component
public class FileTransferApiClient {

    private static final String UPLOAD = "upload";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";
    private static final String HEADER_API_KEY = "x-api-key";

    private final RestTemplate restTemplate;

    private final String fileTransferApiUrl;

    private final String fileTransferApiKey;


    public FileTransferApiClient(RestTemplate restTemplate,
                                 @Value("${file.transfer.api.url}") String fileTransferApiUrl,
                                 @Value("${file.transfer.api.key}") String fileTransferApiKey) {
        this.restTemplate = restTemplate;
        this.fileTransferApiUrl = fileTransferApiUrl;
        this.fileTransferApiKey = fileTransferApiKey;
    }

    public ResponseEntity<String> upload(MultipartFile fileToUpload) throws IOException {
        HttpHeaders headers = createFileTransferApiHttpHeaders();
        LinkedMultiValueMap<String, String> fileHeaderMap = createUploadFileHeader(fileToUpload);
        HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);
        LinkedMultiValueMap<String, Object> body = createUploadBody(fileHttpEntity);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(fileTransferApiUrl, requestEntity, String.class);
    }

    public ResponseEntity<FileDetailsApi> details(String fileId) {
        String detailsUrl = String.format("%s/%s", fileTransferApiUrl,  fileId);
        return restTemplate.exchange(detailsUrl, HttpMethod.GET, new HttpEntity<>(createApiKeyHeader()), FileDetailsApi.class);
    }

    public ResponseEntity<byte[]> download(String fileId) {
        String downloadUrl = String.format("%s/%s/download", fileTransferApiUrl,  fileId);
        return restTemplate.exchange(downloadUrl, HttpMethod.GET, new HttpEntity<>(createApiKeyHeader()), byte[].class);
    }

    private HttpHeaders createApiKeyHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_API_KEY, fileTransferApiKey);
        return headers;
    }

    private HttpHeaders createFileTransferApiHttpHeaders() {
        HttpHeaders headers = createApiKeyHeader();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private LinkedMultiValueMap<String, String> createUploadFileHeader(MultipartFile fileToUpload) {
        LinkedMultiValueMap<String, String> fileHeaderMap = new LinkedMultiValueMap<>();
        fileHeaderMap.add(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE, UPLOAD, fileToUpload.getOriginalFilename()));
        return fileHeaderMap;
    }

    private LinkedMultiValueMap<String, Object> createUploadBody(HttpEntity<byte[]> fileHttpEntity) {
        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add(UPLOAD, fileHttpEntity);
        return multipartReqMap;
    }
}
