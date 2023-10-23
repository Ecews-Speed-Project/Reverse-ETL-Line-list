package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface ViralLoadLineListService {
    void fetchViralLoadLineList();
    void buildViralLoadLineList(List<IdWrapper> containers);
}
