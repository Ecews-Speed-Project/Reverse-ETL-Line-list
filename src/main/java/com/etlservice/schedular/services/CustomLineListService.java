package com.etlservice.schedular.services;

import com.etlservice.schedular.model.Container;

import java.util.List;

public interface CustomLineListService {
    void processLineList();
    void processMortalityLineList(List<Container> containerList);
    void processFctDataSetLineList(List<Container> containerList);
}
