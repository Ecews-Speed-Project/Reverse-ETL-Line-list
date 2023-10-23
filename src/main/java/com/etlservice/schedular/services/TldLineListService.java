package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface TldLineListService {
    void createTldLineList();
    void createTldLineList(List<IdWrapper> idWrappers);
}
