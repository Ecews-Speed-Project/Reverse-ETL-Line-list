package com.etlservice.schedular.services;

public interface RefreshLineListService {
    String refreshQuarterlyArtLineList();
    String refreshDailyArtLineList();
    void cleanArtLineList();
}
