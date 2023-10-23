package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.model.Container;

import java.util.List;

public interface HtsLineListService {
    void extractHtsData(List<IdWrapper> mongoContainers);
}
