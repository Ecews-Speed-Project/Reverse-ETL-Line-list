package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface TargetedLineListService {

    void processTargetedLineList(String fileName);
    void processTargetedLineList(String fileName, List<IdWrapper> idWrappers);
}
