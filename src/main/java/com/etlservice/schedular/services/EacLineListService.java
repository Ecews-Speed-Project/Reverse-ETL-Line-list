package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface EacLineListService {
    void processEacLineList();
    void processEacLineList(List<IdWrapper> containers);
}
