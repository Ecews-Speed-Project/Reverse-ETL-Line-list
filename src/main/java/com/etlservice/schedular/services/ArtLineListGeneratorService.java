package com.etlservice.schedular.services;

import com.etlservice.schedular.entities.linelists.ArtLinelist;
import com.etlservice.schedular.entities.linelists.CustomArtLineList;
import com.etlservice.schedular.entities.Facility;
import com.etlservice.schedular.model.Container;

import java.util.Date;

public interface ArtLineListGeneratorService {
    ArtLinelist mapARTLineList(Container container, Facility facility, Date cutOff, String quarter);
    CustomArtLineList mapCustomArtLineList(Container container, Facility facility, Date cutOff, String quarter);
}
