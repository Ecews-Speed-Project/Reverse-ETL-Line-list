package com.etlservice.schedular;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableRabbit
@SpringBootApplication
@EnableScheduling
@RequiredArgsConstructor
public class SchedularApplication extends SpringBootServletInitializer {
//    private final LineListTrackerRepository lineListTrackerRepository;
//    private final ContainerRepository containerRepository;
//    private final RabbitTemplate rabbitTemplate;
//    @Value("${daily.etl.queue}")
//    private String dailyEtlQueue;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(SchedularApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(SchedularApplication.class, args);
    }

//    @PostConstruct
//    public void init() {
//        log.info("Running scheduled task to build custom line list :: start");
//        LineListTracker lineListTracker = lineListTrackerRepository.findByStatusAndLineListType(PROCESSING.name(), CUSTOM.name())
//                .orElse(null);
//        if (lineListTracker == null) {
//            lineListTracker = LineListTracker.builder()
//                    .status(PROCESSING.name())
//                    .pageSize(1000)
//                    .currentPage(0)
//                    .lineListType(CUSTOM.name())
//                    .dateStarted(LocalDateTime.now())
//                    .build();
//            lineListTrackerRepository.save(lineListTracker);
//        }
//        int currentPage = lineListTracker.getCurrentPage();
//        int pageSize = lineListTracker.getPageSize();
//        Pageable pageable = PageRequest.of(currentPage, pageSize);
//        Page<Container> containerPage = containerRepository.findContainersByMessageDataObsValueCoded(165889, pageable);
//        log.info("Total pages :: {}", containerPage.getTotalPages());
//        log.info("Total elements :: {}", containerPage.getTotalElements());
//        List<Container> containerList = containerPage.getContent();
//        List<List<Container>> partitionedContainerList = Partition.ofSize(containerList, 100);
//        partitionedContainerList.forEach(containers -> rabbitTemplate.convertAndSend(dailyEtlQueue, containers));
//        long totalPatients = containerPage.getTotalElements();
//        lineListTracker.setCurrentPage(currentPage);
//        lineListTracker.setTotalPatientsProcessed(lineListTracker.getTotalPatientsProcessed() + containerList.size());
//        lineListTracker.setTotalPatients(totalPatients);
//        lineListTracker.setTotalPages(containerPage.getTotalPages());
//        lineListTrackerRepository.save(lineListTracker);
//        while (containerPage.hasNext()) {
//            currentPage++;
//            log.info("Current page :: {}", currentPage);
//            pageable = PageRequest.of(currentPage, pageSize);
//            containerPage = containerRepository.findContainersByMessageDataObsValueCoded(165889, pageable);
//            containerList = containerPage.getContent();
//            partitionedContainerList = Partition.ofSize(containerList, 100);
//            partitionedContainerList.forEach(containers -> rabbitTemplate.convertAndSend(dailyEtlQueue, containers));
//            if (containerPage.isLast()) {
//                log.info("No more results");
//                lineListTracker.setStatus(LineListStatus.PROCESSED.name());
//                lineListTracker.setDateCompleted(LocalDateTime.now());
//                lineListTrackerRepository.save(lineListTracker);
//            } else {
//                lineListTracker.setCurrentPage(currentPage);
//                lineListTracker.setTotalPatientsProcessed(lineListTracker.getTotalPatientsProcessed() + containerList.size());
//                lineListTracker.setTotalPatients(totalPatients);
//                lineListTracker.setTotalPages(containerPage.getTotalPages());
//                lineListTrackerRepository.save(lineListTracker);
//            }
//        }
//        if (containerPage.isLast()) {
//            log.info("No more results");
//            lineListTracker.setStatus(LineListStatus.PROCESSED.name());
//            lineListTracker.setDateCompleted(LocalDateTime.now());
//            lineListTrackerRepository.save(lineListTracker);
//        }
//    }

}
