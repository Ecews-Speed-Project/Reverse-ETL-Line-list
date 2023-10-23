package com.etlservice.schedular.repository.mongo_repository;

import com.etlservice.schedular.dtos.IdWrapper;
import com.etlservice.schedular.model.Container;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ContainerRepository extends MongoRepository<Container,String> {
    List<Container> findByMessageDataPatientIdentifiersIdentifierType(int identifierType);
//    @Query("{'messageData.patientIdentifiers.identifierType': ?0}")
//    @Query("{'messageData.patientIdentifiers': {$elemMatch: {'identifierType': ?0}}}")
//    @Aggregation(pipeline = {
//            "{ $match: { 'messageData.patientIdentifiers.identifierType': ?0 } }"
//    })
    Page<Container> findByMessageDataPatientIdentifiersIdentifierType(int identifierType, Pageable pageable);
    List<Container> findByMessageDataEncountersEncounterDatetimeBetweenAndMessageDataObsConceptId(Date startDate, Date endDate, int conceptId);
//    Page<Container> findByMessageDataEncountersEncounterDatetimeBetweenAndMessageDataObsConceptId(Date startDate, Date endDate, int conceptId, Pageable pageable);
    @Query("{'messageData.encounters': {$elemMatch: {'encounterDatetime': {$gte: ?0, $lte: ?1}}}, 'messageData.obs': {$elemMatch: {'conceptId': ?2}}}")
//    @Query("{'messageData.encounters.encounterDatetime': {$gte: ?0, $lte: ?1}, 'messageData.obs.conceptId': ?2}")
    Page<Container> findByMessageDataEncountersEncounterDatetimeBetweenAndMessageDataObsConceptId(
            Date startDate, Date endDate, int conceptId, Pageable pageable
    );

    Page<Container> findContainersByMessageDataEncountersEncounterDatetimeGreaterThanAndMessageDataObsConceptId(Date startDate, int conceptId, Pageable pageable);
    Page<Container> findContainersByMessageDataObsValueCoded(int valueCoded, Pageable pageable);

    void deleteAllByMessageHeaderFacilityDatimCode(String datimCode);
    @Query(value = "{'messageHeader.facilityDatimCode':{$in : ?0}, 'messageData.patientIdentifiers.identifierType': ?1}",
            fields = "{messageHeader: 1, 'messageData.demographics': 1, 'messageData.obs': 1,'messageData.patientIdentifiers': 1, 'messageData.encounters': 1}")
    Page<Container> findContainersByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(List<String> datimCodes, int identifierType, Pageable pageable);

    @Query(value = "{'messageData.encounters.formId':{$in : ?0}}",
            fields = "{messageHeader: 1, 'messageData.demographics': 1, 'messageData.obs': 1,'messageData.patientIdentifiers': 1, 'messageData.encounters': 1}")
    Page<Container> findContainersByMessageDataEncountersFormIdIn(List<Integer> formIds, Pageable pageable);
    @Query(value = "{'messageData.encounters.formId':{$in : ?0}}", fields = "{_id: 1}")
    Page<IdWrapper> findContainerIdsByMessageDataEncountersFormIdIn(List<Integer> formIds, Pageable pageable);

    @Query(value = "{'messageHeader.facilityDatimCode':{$in : ?0}, 'messageData.patientIdentifiers.identifierType': ?1}",
            fields = "{_id: 1}")
    Page<IdWrapper> findContainerIdsByMessageHeaderFacilityDatimCodeInAndMessageDataPatientIdentifiersIdentifierType(
            List<String> datimCodes, int identifierType, Pageable pageable
    );
}
