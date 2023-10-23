/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.entities.ContainerList;
import com.etlservice.schedular.model.Container;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author MORRISON.I
 */


public interface ARTLineListETL {
    
    void buildArtLineList(List<Container> mongoContainers);
    void buildDailyArtLineList(List<Container> containers);
    void buildDailyArtLineList(List<IdWrapper> containers, LocalDate cutOffDate);
    void buildCustomArtLineList(List<IdWrapper> containers, LocalDate cutOffDate);
    void buildNewQuarterArtLineList(List<IdWrapper> containers);
    Page<Container> getContainers (int pageNum, int pageSize);
    ContainerList getContainers ();

    String getContainersByIdentifierType(int identifierType);

//    String refreshQuarterlyArtLineList();
//    String refreshDailyArtLineList();
//    ArtLinelist mapARTLineList(Container container, Facility facility, Date cutOff, String quarter);

}
