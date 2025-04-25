package com.wsosearch.searchapi.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wsosearch.searchapi.Service.SearchApiService;

@RestController
@RequestMapping("/api")
public class SearchController {

    @Autowired
    private SearchApiService searchApiService;

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchApis(@RequestParam String query) {
        return searchApiService.searchApis(query);
    }
}

