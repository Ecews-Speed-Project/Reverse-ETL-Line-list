package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface SampleDataService {
    void fetchSampleData();
    void buildSampleData(List<IdWrapper> idWrappers);
}
