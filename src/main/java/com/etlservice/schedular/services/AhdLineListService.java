package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface AhdLineListService {
    void processAhdLineList();
    void buildAhdLineList(List<IdWrapper> containerIds);
}
