package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface RegimenLineListService {
    void processRegimenLineList();
    void processRegimenLineList(List<IdWrapper> containers);
}
