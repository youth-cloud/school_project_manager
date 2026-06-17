package org.example.backend.modules.edu.config;

import org.example.backend.modules.edu.service.TopicSelectionRepairService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TopicSelectionRepairRunner implements ApplicationRunner {

    private final TopicSelectionRepairService topicSelectionRepairService;

    public TopicSelectionRepairRunner(TopicSelectionRepairService topicSelectionRepairService) {
        this.topicSelectionRepairService = topicSelectionRepairService;
    }

    @Override
    public void run(ApplicationArguments args) {
        topicSelectionRepairService.repairHistoricalTopicSelections();
    }
}
