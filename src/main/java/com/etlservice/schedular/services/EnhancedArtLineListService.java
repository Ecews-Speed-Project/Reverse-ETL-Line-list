package com.etlservice.schedular.services;

import com.etlservice.schedular.model.Container;

import java.util.List;

public interface EnhancedArtLineListService {
    void buildEnhancedArtLineList(List<Container> mongoContainers);
}
