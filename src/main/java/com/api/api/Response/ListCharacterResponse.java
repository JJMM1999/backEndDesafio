package com.api.api.Response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.List;
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ListCharacterResponse {
    private InfoResponse info;
    private List<CharacterResponse> results;

    public List<CharacterResponse> getResults() {
        return results;
    }

    public void setResults(List<CharacterResponse> results) {
        this.results =results;
    }

    public InfoResponse getInfo() {
        return info;
    }

    public void setInfo(InfoResponse info) {
        this.info = info;
    }
}
