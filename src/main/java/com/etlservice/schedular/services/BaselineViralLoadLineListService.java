package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface BaselineViralLoadLineListService {
    void processBaselineViralLoadLineList();
    void processBaselineViralLoadLineList(List<IdWrapper> idWrappers);
}
