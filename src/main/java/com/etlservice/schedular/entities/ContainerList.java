package com.etlservice.schedular.entities;

import com.etlservice.schedular.model.Container;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class ContainerList {
    private List<Container> containers;
    private int totalPages;
    private static final ContainerList containerList = new ContainerList();
    private ContainerList(){}

    public static ContainerList getInstance() {
        return containerList;
    }

}
