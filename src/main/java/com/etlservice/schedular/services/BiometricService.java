package com.etlservice.schedular.services;

import com.etlservice.schedular.dtos.IdWrapper;

import java.util.List;

public interface BiometricService {
    void processBiometricLineList();
    void processBiometricLineList(List<IdWrapper> idWrappers);
}
