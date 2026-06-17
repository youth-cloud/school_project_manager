/*
 Navicat Premium Dump SQL

 Source Server         : youthd
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : schoolprojectmanager

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 11/06/2026 22:47:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for defense_record
-- ----------------------------
DROP TABLE IF EXISTS `defense_record`;
CREATE TABLE `defense_record`  (
  `id` bigint(20) NOT NULL COMMENT '答辩记录ID',
  `schedule_id` bigint(20) NOT NULL COMMENT '答辩安排ID',
  `teacher_id` bigint(20) NOT NULL COMMENT '教师ID',
  `presentation_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '展示分',
  `answer_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '问答分',
  `completion_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '完成度分',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总分',
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '评语',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_defense_record_schedule_id`(`schedule_id` ASC) USING BTREE,
  INDEX `idx_defense_record_teacher_id`(`teacher_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '答辩记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for defense_schedule
-- ----------------------------
DROP TABLE IF EXISTS `defense_schedule`;
CREATE TABLE `defense_schedule`  (
  `id` bigint(20) NOT NULL COMMENT '答辩安排ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `group_id` bigint(20) NOT NULL COMMENT '项目组ID',
  `defense_date` date NULL DEFAULT NULL COMMENT '答辩日期',
  `defense_time` time NULL DEFAULT NULL COMMENT '答辩时间',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '答辩地点',
  `order_no` int(11) NULL DEFAULT NULL COMMENT '答辩顺序',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1已安排 0未安排 2已完成',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_defense_schedule_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_defense_schedule_group_id`(`group_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '答辩安排表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for edu_class
-- ----------------------------
DROP TABLE IF EXISTS `edu_class`;
CREATE TABLE `edu_class`  (
  `id` bigint(20) NOT NULL COMMENT '班级ID',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '班级名称',
  `major_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '专业名称',
  `grade` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '年级',
  `counselor_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '辅导员',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1正常 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_edu_class_name`(`class_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '班级表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for edu_course
-- ----------------------------
DROP TABLE IF EXISTS `edu_course`;
CREATE TABLE `edu_course`  (
  `id` bigint(20) NOT NULL COMMENT '课程ID',
  `course_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课程名称',
  `course_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '课程编码',
  `credit` decimal(4, 1) NULL DEFAULT NULL COMMENT '学分',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1正常 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_edu_course_code`(`course_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课程表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` bigint(20) NOT NULL COMMENT '公告ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '内容',
  `publisher_id` bigint(20) NOT NULL COMMENT '发布人ID',
  `target_role` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '目标角色',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1已发布 0草稿 2下线',
  `publish_time` datetime NULL DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_publisher_id`(`publisher_id` ASC) USING BTREE,
  INDEX `idx_notice_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '公告表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint(20) NOT NULL COMMENT '日志ID',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '模块名称',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '操作类型',
  `operator_id` bigint(20) NULL DEFAULT NULL COMMENT '操作人ID',
  `request_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求方式',
  `request_uri` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '请求地址',
  `ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `operation_desc` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作描述',
  `result` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '操作结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operation_log_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_operation_log_module_name`(`module_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for project_group
-- ----------------------------
DROP TABLE IF EXISTS `project_group`;
CREATE TABLE `project_group`  (
  `id` bigint(20) NOT NULL COMMENT '项目组ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `topic_id` bigint(20) NOT NULL COMMENT '课题ID',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '小组名称',
  `leader_id` bigint(20) NOT NULL COMMENT '组长ID',
  `teacher_id` bigint(20) NOT NULL COMMENT '指导教师ID',
  `project_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目名称',
  `project_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目简介',
  `repo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '代码仓库地址',
  `deploy_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部署地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1正常 0解散 2已结项',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_group_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_project_group_topic_id`(`topic_id` ASC) USING BTREE,
  INDEX `idx_project_group_leader_id`(`leader_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_group_application
-- ----------------------------
DROP TABLE IF EXISTS `project_group_application`;
CREATE TABLE `project_group_application`  (
  `id` bigint(20) NOT NULL COMMENT '申请ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `topic_id` bigint(20) NOT NULL COMMENT '课题ID',
  `leader_id` bigint(20) NOT NULL COMMENT '申请组长ID',
  `group_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '项目组名称',
  `project_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '项目名称',
  `project_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '项目简介',
  `repo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仓库地址',
  `deploy_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部署地址',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '建组申请理由',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING APPROVED REJECTED CANCELED',
  `reviewer_id` bigint(20) NULL DEFAULT NULL COMMENT '审核教师ID',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核意见',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `generated_group_id` bigint(20) NULL DEFAULT NULL COMMENT '审批通过后生成的正式项目组ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_pga_batch_id`(`batch_id`) USING BTREE,
  INDEX `idx_pga_topic_id`(`topic_id`) USING BTREE,
  INDEX `idx_pga_leader_id`(`leader_id`) USING BTREE,
  INDEX `idx_pga_status`(`status`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目组申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_group_application_member
-- ----------------------------
DROP TABLE IF EXISTS `project_group_application_member`;
CREATE TABLE `project_group_application_member`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `application_id` bigint(20) NOT NULL COMMENT '申请单ID',
  `user_id` bigint(20) NOT NULL COMMENT '学生ID',
  `is_leader` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否组长 1是 0否',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_pg_app_member`(`application_id`, `user_id`) USING BTREE,
  INDEX `idx_pg_app_member_user_id`(`user_id`) USING BTREE
) ENGINE = MyISAM AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目组申请成员表' ROW_FORMAT = Fixed;

-- ----------------------------
-- Table structure for project_group_member
-- ----------------------------
DROP TABLE IF EXISTS `project_group_member`;
CREATE TABLE `project_group_member`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `group_id` bigint(20) NOT NULL COMMENT '项目组ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `is_leader` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否组长 1是 0否',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1正常 0退出',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_project_group_member_group_user`(`group_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_project_group_member_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '项目组成员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for project_topic
-- ----------------------------
DROP TABLE IF EXISTS `project_topic`;
CREATE TABLE `project_topic`  (
  `id` bigint(20) NOT NULL COMMENT '课题ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `teacher_id` bigint(20) NOT NULL COMMENT '发布教师ID',
  `topic_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '课题名称',
  `topic_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '课题描述',
  `difficulty_level` tinyint(4) NULL DEFAULT 2 COMMENT '难度等级 1简单 2中等 3困难',
  `tech_requirements` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '技术要求',
  `max_members` int(11) NOT NULL DEFAULT 1 COMMENT '最大成员数',
  `selected_count` int(11) NOT NULL DEFAULT 0 COMMENT '当前已选人数',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1可选 0关闭 2已满',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_project_topic_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_project_topic_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_project_topic_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '课题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for review_record
-- ----------------------------
DROP TABLE IF EXISTS `review_record`;
CREATE TABLE `review_record`  (
  `id` bigint(20) NOT NULL COMMENT '审核记录ID',
  `submission_id` bigint(20) NOT NULL COMMENT '提交记录ID',
  `reviewer_id` bigint(20) NOT NULL COMMENT '审核人ID',
  `review_result` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '审核结果',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '分数',
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核评语',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_record_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_review_record_reviewer_id`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_review_record_result`(`review_result` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '审核记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for score_record
-- ----------------------------
DROP TABLE IF EXISTS `score_record`;
CREATE TABLE `score_record`  (
  `id` bigint(20) NOT NULL COMMENT '成绩记录ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `group_id` bigint(20) NULL DEFAULT NULL COMMENT '项目组ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `process_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '过程分',
  `report_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '报告分',
  `submission_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '材料分',
  `defense_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '答辩分',
  `final_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终总分',
  `grade_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '等级',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_score_record_batch_student`(`batch_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_score_record_group_id`(`group_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '成绩表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for stage_submission
-- ----------------------------
DROP TABLE IF EXISTS `stage_submission`;
CREATE TABLE `stage_submission`  (
  `id` bigint(20) NOT NULL COMMENT '阶段提交ID',
  `task_id` bigint(20) NOT NULL COMMENT '阶段任务ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `group_id` bigint(20) NOT NULL COMMENT '项目组ID',
  `submitter_id` bigint(20) NOT NULL COMMENT '提交人ID',
  `version_no` int(11) NOT NULL DEFAULT 1 COMMENT '版本号',
  `summary` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '提交摘要',
  `report_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '报告文本',
  `repo_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '仓库地址',
  `deploy_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '部署地址',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1已提交 0草稿 2已撤回',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_stage_submission_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_stage_submission_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_stage_submission_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_stage_submission_submitter_id`(`submitter_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '阶段提交表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for stage_task
-- ----------------------------
DROP TABLE IF EXISTS `stage_task`;
CREATE TABLE `stage_task`  (
  `id` bigint(20) NOT NULL COMMENT '阶段任务ID',
  `batch_id` bigint(20) NOT NULL COMMENT '实训批次ID',
  `teacher_id` bigint(20) NOT NULL COMMENT '教师ID',
  `task_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务标题',
  `task_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '任务说明',
  `stage_no` int(11) NOT NULL COMMENT '阶段序号',
  `deadline` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `need_report` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否需要报告 1是 0否',
  `need_source_code` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否需要源代码 1是 0否',
  `need_pdf` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否需要PDF 1是 0否',
  `need_screenshot` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否需要截图 1是 0否',
  `need_demo_url` tinyint(4) NOT NULL DEFAULT 0 COMMENT '是否需要演示地址 1是 0否',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1启用 0停用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_stage_task_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_stage_task_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_stage_task_stage_no`(`stage_no` ASC) USING BTREE,
  INDEX `idx_stage_task_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '阶段任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for submission_file
-- ----------------------------
DROP TABLE IF EXISTS `submission_file`;
CREATE TABLE `submission_file`  (
  `id` bigint(20) NOT NULL COMMENT '文件ID',
  `submission_id` bigint(20) NOT NULL COMMENT '提交记录ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储文件名',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '原始文件名',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件类型',
  `file_size` bigint(20) NOT NULL DEFAULT 0 COMMENT '文件大小(字节)',
  `file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '文件路径',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '访问地址',
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '业务类型',
  `upload_user_id` bigint(20) NOT NULL COMMENT '上传人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_submission_file_submission_id`(`submission_id` ASC) USING BTREE,
  INDEX `idx_submission_file_upload_user_id`(`upload_user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '提交文件表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL COMMENT '角色ID',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '角色表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录账号',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '登录密码(加密后)',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '真实姓名',
  `student_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学号',
  `teacher_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '工号',
  `gender` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '性别 M男 F女',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '头像地址',
  `class_id` bigint(20) NULL DEFAULT NULL COMMENT '班级ID',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1正常 0禁用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除 0未删 1已删',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_student_no`(`student_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_sys_user_teacher_no`(`teacher_no` ASC) USING BTREE,
  INDEX `idx_sys_user_class_id`(`class_id` ASC) USING BTREE,
  INDEX `idx_sys_user_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_role_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_sys_user_role_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for topic_application
-- ----------------------------
DROP TABLE IF EXISTS `topic_application`;
CREATE TABLE `topic_application`  (
  `id` bigint(20) NOT NULL COMMENT '申请ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `topic_id` bigint(20) NOT NULL COMMENT '课题ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `apply_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请理由',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'PENDING' COMMENT '申请状态 PENDING APPROVED REJECTED CANCELED',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `reviewer_id` bigint(20) NULL DEFAULT NULL COMMENT '审核人ID',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '审核意见',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_topic_application_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_topic_application_topic_id`(`topic_id` ASC) USING BTREE,
  INDEX `idx_topic_application_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_topic_application_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '选题申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for training_batch
-- ----------------------------
DROP TABLE IF EXISTS `training_batch`;
CREATE TABLE `training_batch`  (
  `id` bigint(20) NOT NULL COMMENT '批次ID',
  `batch_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '批次名称',
  `course_id` bigint(20) NOT NULL COMMENT '课程ID',
  `teacher_id` bigint(20) NOT NULL COMMENT '教师ID',
  `class_id` bigint(20) NOT NULL COMMENT '班级ID',
  `term_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '学期名称',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `defense_time` datetime NULL DEFAULT NULL COMMENT '答辩时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '批次说明',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1进行中 0未启用 2已结束',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_training_batch_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_training_batch_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_training_batch_class_id`(`class_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '实训批次表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for weekly_report
-- ----------------------------
DROP TABLE IF EXISTS `weekly_report`;
CREATE TABLE `weekly_report`  (
  `id` bigint(20) NOT NULL COMMENT '周报ID',
  `batch_id` bigint(20) NOT NULL COMMENT '批次ID',
  `group_id` bigint(20) NOT NULL COMMENT '项目组ID',
  `student_id` bigint(20) NOT NULL COMMENT '学生ID',
  `week_index` int(11) NOT NULL COMMENT '第几周',
  `completed_work` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '已完成工作',
  `problem_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '问题描述',
  `next_plan` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '下周计划',
  `teacher_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '教师评语',
  `score` decimal(5, 2) NULL DEFAULT NULL COMMENT '评分',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `status` tinyint(4) NOT NULL DEFAULT 1 COMMENT '状态 1已提交 0草稿 2已点评',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_weekly_report_batch_id`(`batch_id` ASC) USING BTREE,
  INDEX `idx_weekly_report_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_weekly_report_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '周报表' ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
