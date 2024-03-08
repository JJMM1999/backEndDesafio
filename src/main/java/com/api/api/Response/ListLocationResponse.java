package com.api.api.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ListLocationResponse {
    private InfoResponse info;
    private List<LocationResponse> results;

    public List<LocationResponse> getResults() {
        return results;
    }

    public InfoResponse getInfo() {
        return info;
    }

    public void setInfo(InfoResponse info) {
        this.info = info;
    }

    public void setResults(List<LocationResponse> results) {
        this.results = results;
    }
}
