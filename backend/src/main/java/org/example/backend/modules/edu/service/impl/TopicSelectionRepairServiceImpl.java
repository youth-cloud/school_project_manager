package org.example.backend.modules.edu.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.example.backend.modules.edu.entity.ProjectGroup;
import org.example.backend.modules.edu.entity.ProjectGroupMember;
import org.example.backend.modules.edu.entity.ProjectTopic;
import org.example.backend.modules.edu.entity.TopicApplication;
import org.example.backend.modules.edu.service.ProjectGroupMemberService;
import org.example.backend.modules.edu.service.ProjectGroupService;
import org.example.backend.modules.edu.service.ProjectTopicService;
import org.example.backend.modules.edu.service.TopicApplicationService;
import org.example.backend.modules.edu.service.TopicSelectionRepairService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TopicSelectionRepairServiceImpl implements TopicSelectionRepairService {

    private static final Logger log = LoggerFactory.getLogger(TopicSelectionRepairServiceImpl.class);

    private final ProjectGroupService projectGroupService;
    private final ProjectGroupMemberService projectGroupMemberService;
    private final ProjectTopicService projectTopicService;
    private final TopicApplicationService topicApplicationService;

    public TopicSelectionRepairServiceImpl(ProjectGroupService projectGroupService,
                                           ProjectGroupMemberService projectGroupMemberService,
                                           ProjectTopicService projectTopicService,
                                           TopicApplicationService topicApplicationService) {
        this.projectGroupService = projectGroupService;
        this.projectGroupMemberService = projectGroupMemberService;
        this.projectTopicService = projectTopicService;
        this.topicApplicationService = topicApplicationService;
    }

    @Override
    @Transactional
    public void repairHistoricalTopicSelections() {
        List<ProjectGroup> groups = projectGroupService.list();
        if (groups == null || groups.isEmpty()) {
            refreshAllTopicSelectedCounts();
            return;
        }

        Map<Long, ProjectGroup> groupMap = groups.stream()
                .collect(Collectors.toMap(ProjectGroup::getId, item -> item, (left, right) -> left, LinkedHashMap::new));

        List<ProjectGroupMember> members = projectGroupMemberService.list();
        int repairedCount = 0;
        if (members != null && !members.isEmpty()) {
            for (ProjectGroupMember member : members) {
                ProjectGroup group = groupMap.get(member.getGroupId());
                if (group == null || group.getTopicId() == null || member.getUserId() == null) {
                    continue;
                }

                TopicApplication existing = topicApplicationService.getOne(
                        Wrappers.<TopicApplication>lambdaQuery()
                                .eq(TopicApplication::getBatchId, group.getBatchId())
                                .eq(TopicApplication::getTopicId, group.getTopicId())
                                .eq(TopicApplication::getStudentId, member.getUserId())
                                .orderByDesc(TopicApplication::getCreateTime)
                                .last("limit 1")
                );

                if (existing != null) {
                    continue;
                }

                TopicApplication autoApprovedApplication = new TopicApplication();
                autoApprovedApplication.setBatchId(group.getBatchId());
                autoApprovedApplication.setTopicId(group.getTopicId());
                autoApprovedApplication.setStudentId(member.getUserId());
                autoApprovedApplication.setApplyReason("历史项目组成员数据自愈时系统自动补建选题申请");
                autoApprovedApplication.setStatus("APPROVED");
                autoApprovedApplication.setReviewerId(group.getTeacherId());
                autoApprovedApplication.setReviewTime(resolveReviewTime(member.getJoinTime()));
                autoApprovedApplication.setReviewComment("历史项目组成员数据自愈时系统自动通过对应课题选题申请");
                topicApplicationService.save(autoApprovedApplication);
                repairedCount++;
            }
        }

        refreshAllTopicSelectedCounts();

        if (repairedCount > 0) {
            log.info("历史选题申请自愈完成，本次自动补齐 {} 条记录", repairedCount);
        } else {
            log.info("历史选题申请自愈完成，本次没有需要补齐的记录");
        }
    }

    private void refreshAllTopicSelectedCounts() {
        List<ProjectTopic> topics = projectTopicService.list();
        if (topics == null || topics.isEmpty()) {
            return;
        }

        Map<Long, Set<Long>> approvedStudentMap = new LinkedHashMap<>();
        topicApplicationService.list(
                Wrappers.<TopicApplication>lambdaQuery()
                        .eq(TopicApplication::getStatus, "APPROVED")
        ).forEach(item -> {
            if (item.getTopicId() == null || item.getStudentId() == null) {
                return;
            }
            approvedStudentMap
                    .computeIfAbsent(item.getTopicId(), key -> new LinkedHashSet<>())
                    .add(item.getStudentId());
        });

        for (ProjectTopic topic : topics) {
            int approvedCount = approvedStudentMap.getOrDefault(topic.getId(), Set.of()).size();
            Integer currentSelectedCount = topic.getSelectedCount();
            if (currentSelectedCount != null && currentSelectedCount == approvedCount) {
                continue;
            }

            ProjectTopic updateTopic = new ProjectTopic();
            updateTopic.setId(topic.getId());
            updateTopic.setSelectedCount(approvedCount);
            projectTopicService.updateById(updateTopic);
        }
    }

    private LocalDateTime resolveReviewTime(LocalDateTime joinTime) {
        return joinTime != null ? joinTime : LocalDateTime.now();
    }
}
