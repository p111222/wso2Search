package com.wsosearch.searchapi.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class SearchApiService {

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    private String token; // Token needs to be declared at class level

    public SearchApiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Map<String, Object>> searchApis(String query) {
        String[] searchFields = {
                "name", "description", "context", "version", "provider", "type",
                "audience", "lifeCycleStatus", "workflowStatus", "updatedBy",
                "gatewayVendor", "advertiseOnly"
        };

        token = fetchNewToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        List<Map<String, Object>> combinedResults = new ArrayList<>();

        for (String field : searchFields) {
            String encodedQuery = UriUtils.encodeQueryParam(field + ":" + query, StandardCharsets.UTF_8);
            String url = "https://api.kriate.co.in:8344/api/am/publisher/v4/apis?query=" + encodedQuery;

            try {
                ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

                if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                    Map<String, Object> jsonResponse = objectMapper.readValue(response.getBody(), Map.class);
                    List<Map<String, Object>> list = (List<Map<String, Object>>) jsonResponse.get("list");

                    if (list != null) {
                        combinedResults.addAll(list);
                    }
                }

            } catch (Exception e) {
                System.out.println("Failed to search field: " + field + " | Error: " + e.getMessage());
            }
        }

        // Deduplicate using 'id'
        Map<String, Map<String, Object>> uniqueResults = new LinkedHashMap<>();
        for (Map<String, Object> item : combinedResults) {
            uniqueResults.put((String) item.get("id"), item);
        }

        return ResponseEntity.ok(Map.of(
                "count", uniqueResults.size(),
                "list", new ArrayList<>(uniqueResults.values())
        ));
    }

    private String fetchNewToken() {
        String tokenUrl = "https://api.kriate.co.in:8344/oauth2/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization",
                "Basic YVFDcjR4ajhnVU9WUXBBcTFra3ozbWR5WkZvYTpmbHRZaHFrcG90NEY3R2VXZmp1QVRXU1BjY1lh");

        String requestBody = "grant_type=password&username=admin&password=admin&scope=apim:api_create apim:api_manage";
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            String accessToken = response.getBody().get("access_token").toString();
            System.out.println("fetchToken: " + accessToken);
            return accessToken;
        } else {
            throw new RuntimeException("Failed to fetch token");
        }
    }
}
