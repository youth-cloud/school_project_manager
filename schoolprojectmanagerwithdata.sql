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

 Date: 16/06/2026 22:30:37
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
-- Records of defense_record
-- ----------------------------
INSERT INTO `defense_record` VALUES (2063592444257501185, 2063589717657976834, 35, 30.00, 30.50, 29.00, 89.50, '修改后表现优秀，答辩通过。', '2026-06-07 20:02:33', '2026-06-07 20:07:44');
INSERT INTO `defense_record` VALUES (2063624811328303105, 2063589717657976834, 35, 30.00, 30.00, 29.00, 89.00, '回归测试：教师正常修改答辩记录。', '2026-06-07 22:11:09', '2026-06-07 22:11:42');
INSERT INTO `defense_record` VALUES (2063970271637164033, 2063589717657976834, 35, 30.00, 29.50, 28.50, 88.00, '接口回归测试：教师正常登记答辩记录', '2026-06-08 21:03:54', '2026-06-08 21:03:54');
INSERT INTO `defense_record` VALUES (2064248634327044097, 2064240060146257922, 35, 30.00, 30.00, 35.00, 95.00, '还行嗷asd', '2026-06-09 15:30:00', '2026-06-09 15:30:31');
INSERT INTO `defense_record` VALUES (2064706986068828162, 2064706739682828290, 2064696038218162177, 30.00, 30.00, 30.00, 90.00, '', '2026-06-10 21:51:20', '2026-06-10 21:51:20');

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
-- Records of defense_schedule
-- ----------------------------
INSERT INTO `defense_schedule` VALUES (2063589717657976834, 2062755007931305985, 2063247545389703170, '2026-07-01', '14:30:00', '实验楼A302', 2, 1, '2026-06-07 19:51:42', '2026-06-07 19:54:49', 0);
INSERT INTO `defense_schedule` VALUES (2063836462954528770, 2062755007931305985, 2063247545389703170, '2026-07-13', '16:00:00', '实训楼A103', 5, 1, '2026-06-08 12:12:11', '2026-06-08 12:14:05', 0);
INSERT INTO `defense_schedule` VALUES (2064240060146257922, 2062755007931305985, 2063247545389703170, '2026-06-15', '14:55:32', '应用楼302', 1, 1, '2026-06-09 14:55:56', '2026-06-09 14:55:56', 0);
INSERT INTO `defense_schedule` VALUES (2064706739682828290, 2064697112421359618, 2064698112418930689, '2026-06-30', '21:50:05', '应用楼320', NULL, 1, '2026-06-10 21:50:21', '2026-06-10 21:50:21', 0);

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
-- Records of edu_class
-- ----------------------------
INSERT INTO `edu_class` VALUES (1, '一个测试班级', '计算机科学与技术', '3', '孔xx', 1, '2026-06-04 23:10:35', '2026-06-04 23:10:35', 0);
INSERT INTO `edu_class` VALUES (2062559209335439362, '软件工程2401-修改后', '软件工程', '2024', '王老师', 1, '2026-06-04 23:36:50', '2026-06-08 11:53:56', 0);
INSERT INTO `edu_class` VALUES (2064282230190395394, '计算机1班', '计算机科学与技术', '2025', 'krb', 1, '2026-06-09 17:43:30', '2026-06-09 17:43:30', 0);
INSERT INTO `edu_class` VALUES (2064670472165351426, '计算机6班', '计算机科学与技术', '2025', '孔若冰', 1, '2026-06-10 19:26:14', '2026-06-10 19:26:14', 0);

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
-- Records of edu_course
-- ----------------------------
INSERT INTO `edu_course` VALUES (2, '一个测试课程', 'TEST002', 3.0, NULL, 1, '2026-06-05 12:33:28', '2026-06-05 12:33:28', 0);
INSERT INTO `edu_course` VALUES (2062738831117283330, 'Java Web开发实训-修改后', 'JAVA002', 4.0, '课程备注已修改', 1, '2026-06-05 11:30:35', '2026-06-08 11:53:51', 0);
INSERT INTO `edu_course` VALUES (2064663962483908610, '网络程序设计', 'JAVA2026', 3.0, '', 1, '2026-06-10 19:00:22', '2026-06-10 19:00:22', 0);

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
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (123456, 'zxcvxzcbasdf', 'zxcvxzcv', 35, 'ALL', 1, NULL, '2026-06-09 10:29:24', '2026-06-09 10:57:34', 1);
INSERT INTO `notice` VALUES (2063598045775687681, '第一阶段提交通知-回归测试更新', '验证更新公告时不传 publishTime 不会被清空。', 35, 'STUDENT', 1, '2026-06-07 20:00:00', '2026-06-07 20:24:48', '2026-06-07 22:09:56', 0);
INSERT INTO `notice` VALUES (2063624298197151745, '回归测试公告-教师正常发布', '用于验证公告发布权限仍然正常。', 35, 'STUDENT', 1, '2026-06-07 22:09:08', '2026-06-07 22:09:07', '2026-06-07 22:09:07', 0);
INSERT INTO `notice` VALUES (2063806917035606017, 'JWT阶段公告测试-教师修改后', '这是教师通过当前登录用户身份修改后的公告内容。', 35, 'STUDENT', 1, '2026-06-08 10:20:00', '2026-06-08 10:14:47', '2026-06-08 10:15:48', 0);
INSERT INTO `notice` VALUES (2064164940073648129, '前端联调测试公告asdf', '这是一条由前端公告弹窗新增的测试公告。', 35, 'ALL', 1, '2026-06-09 09:57:27', '2026-06-09 09:57:26', '2026-06-09 10:41:04', 0);
INSERT INTO `notice` VALUES (2064704231405150210, '感觉终于要完成了！', '我草 累死我了 好累 好累', 1, 'ALL', 1, '2026-06-10 21:40:24', '2026-06-10 21:40:23', '2026-06-10 21:40:23', 0);

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
-- Records of operation_log
-- ----------------------------
INSERT INTO `operation_log` VALUES (2063808574200954881, 'JWT测试模块-更新后', 'UPDATE', 35, 'PUT', '/api/operation-logs', '127.0.0.1', '测试修改时保留原操作人', 'SUCCESS', '2026-06-08 10:21:22');

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
-- Records of project_group
-- ----------------------------
INSERT INTO `project_group` VALUES (2063247545389703170, 2062755007931305985, 2062837498155278337, '第一开发小组-修改后', 50, 35, '实训管理平台升级版', '修改后的项目简介', 'https://github.com/youth-cloud/school_project_manager', 'http://your-domain.com', 1, '2026-06-06 21:12:02', '2026-06-06 21:25:28', 0);
INSERT INTO `project_group` VALUES (2064686927283904514, 2064670827271905281, 2064671065374154754, '230寝', 2064638382317199362, 35, '无敌公积金', '感觉真无敌了', '', '', 1, '2026-06-10 20:31:38', '2026-06-10 20:31:38', 0);
INSERT INTO `project_group` VALUES (2064698112418930689, 2064697112421359618, 2064697230285496321, '赵科帆', 2064695798345916417, 2064696038218162177, '公积金', '无敌公积金', '', '', 1, '2026-06-10 21:16:04', '2026-06-10 21:16:04', 0);
INSERT INTO `project_group` VALUES (2064947247525027841, 2064697112421359618, 2064697230285496321, '黄符昊Xgzs', 2064945005803782146, 2064696038218162177, '个人公积金系统', '', '', '', 1, '2026-06-11 13:46:03', '2026-06-11 13:46:03', 0);

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
-- Records of project_group_application
-- ----------------------------
INSERT INTO `project_group_application` VALUES (2064274380735008769, 2063831985245573122, 2064257600251670529, 50, '前端学生建组申请', '前端学生建组申请', '前端学生建组申请', '', '', '前端学生建组申请', 'REJECTED', 35, '你好像有组了啊', '2026-06-09 17:13:49', NULL, '2026-06-09 17:12:19', '2026-06-09 17:12:19');
INSERT INTO `project_group_application` VALUES (2064673607353065473, 2064670827271905281, 2064671065374154754, 2064638382317199362, '230寝', '无敌公积金', '感觉真无敌了', '', '', '', 'APPROVED', 35, '', '2026-06-10 20:31:38', 2064686927283904514, '2026-06-10 19:38:42', '2026-06-10 19:38:42');
INSERT INTO `project_group_application` VALUES (2064698009838837761, 2064697112421359618, 2064697230285496321, 2064695798345916417, '赵科帆', '公积金', '无敌公积金', '', '', '', 'APPROVED', 2064696038218162177, '', '2026-06-10 21:16:05', 2064698112418930689, '2026-06-10 21:15:40', '2026-06-10 21:15:40');
INSERT INTO `project_group_application` VALUES (2064947077206925314, 2064697112421359618, 2064697230285496321, 2064945005803782146, '黄符昊Xgzs', '个人公积金系统', '', '', '', '', 'APPROVED', 2064696038218162177, '', '2026-06-11 13:46:03', 2064947247525027841, '2026-06-11 13:45:22', '2026-06-11 13:45:22');

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
-- Records of project_group_application_member
-- ----------------------------
INSERT INTO `project_group_application_member` VALUES (2064274380835672065, 2064274380735008769, 50, 1, '2026-06-09 17:12:19');
INSERT INTO `project_group_application_member` VALUES (2064673607428562946, 2064673607353065473, 2064638382317199362, 1, '2026-06-10 19:38:42');
INSERT INTO `project_group_application_member` VALUES (2064698009897558017, 2064698009838837761, 2064695798345916417, 1, '2026-06-10 21:15:40');
INSERT INTO `project_group_application_member` VALUES (2064947077257256962, 2064947077206925314, 2064945005803782146, 1, '2026-06-11 13:45:22');

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
-- Records of project_group_member
-- ----------------------------
INSERT INTO `project_group_member` VALUES (2063607542799859713, 2063247545389703170, 50, 1, '2026-06-07 21:02:32', 1);
INSERT INTO `project_group_member` VALUES (2064686927355207682, 2064686927283904514, 2064638382317199362, 1, '2026-06-10 20:31:38', 1);
INSERT INTO `project_group_member` VALUES (2064698112427319298, 2064698112418930689, 2064695798345916417, 1, '2026-06-10 21:16:05', 1);
INSERT INTO `project_group_member` VALUES (2064710613256814593, 2064698112418930689, 2064709935335653378, 0, '2026-06-10 22:05:45', 1);
INSERT INTO `project_group_member` VALUES (2064947247575359489, 2064947247525027841, 2064945005803782146, 1, '2026-06-11 13:46:03', 1);
INSERT INTO `project_group_member` VALUES (2064955821231841281, 2064947247525027841, 2064950503395434497, 0, '2026-06-11 14:20:07', 1);

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
-- Records of project_topic
-- ----------------------------
INSERT INTO `project_topic` VALUES (2062837498155278337, 2062755007931305985, 35, '实训管理平台课题-修改后', '修改后的课题说明', 2, 'Spring Boot、Vue3、MySQL', 5, 1, 1, '2026-06-05 18:02:39', '2026-06-11 13:59:55', 0);
INSERT INTO `project_topic` VALUES (2063609831459528705, 2062755007931305985, 35, '基于Vue3和Spring Boot的实训管理平台优化课题', '完成系统模块优化、角色权限完善与前后端联调。', 2, 'Spring Boot、Vue3、MySQL、Redis', 5, 1, 1, '2026-06-07 21:11:38', '2026-06-11 13:59:55', 0);
INSERT INTO `project_topic` VALUES (2064257600251670529, 2063831985245573122, 35, '前端教师创建课题测试', '测试测试', 2, 'vue3', 5, 1, 1, '2026-06-09 16:05:38', '2026-06-11 13:59:55', 0);
INSERT INTO `project_topic` VALUES (2064671065374154754, 2064670827271905281, 35, '公积金实训', '', 2, 'javaspring+vue', 40, 1, 1, '2026-06-10 19:28:36', '2026-06-11 13:59:54', 0);
INSERT INTO `project_topic` VALUES (2064697230285496321, 2064697112421359618, 2064696038218162177, '公积金系统', '做个公积金系统', 3, 'spring+mybatis', 40, 4, 1, '2026-06-10 21:12:34', '2026-06-11 14:45:07', 0);
INSERT INTO `project_topic` VALUES (2064702164104011777, 2064697112421359618, 2064696038218162177, '自选系统', '自选', 4, '要求spring+mybatis', 40, 0, 1, '2026-06-10 21:32:10', '2026-06-10 21:36:15', 0);

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
-- Records of review_record
-- ----------------------------
INSERT INTO `review_record` VALUES (2063581429537095681, 2063438735540133889, 35, 'APPROVED', 95.00, 'ReviewRecord 深层校验修改测试', '2026-06-07 19:18:47', '2026-06-07 19:18:46');
INSERT INTO `review_record` VALUES (2063948751367979009, 2063438735540133889, 35, 'APPROVED', 92.00, 'ReviewRecord 深层校验正常新增测试', '2026-06-08 19:38:23', '2026-06-08 19:38:23');
INSERT INTO `review_record` VALUES (2063970036303155201, 2063438735540133889, 35, 'APPROVED', 91.00, '接口回归测试：教师正常审核', '2026-06-08 21:02:58', '2026-06-08 21:02:58');
INSERT INTO `review_record` VALUES (2063984498380779521, 2063438735540133889, 35, 'APPROVED', 90.00, '大回归测试：教师正常审核', '2026-06-08 22:00:26', '2026-06-08 22:00:26');
INSERT INTO `review_record` VALUES (2064203287953309698, 2064194916126720002, 35, 'APPROVED', 95.00, '前端审核测试', '2026-06-09 12:29:50', '2026-06-09 12:29:49');
INSERT INTO `review_record` VALUES (2064706622313619458, 2064706226434236418, 2064696038218162177, 'APPROVED', 95.00, '没问题', '2026-06-10 21:49:54', '2026-06-10 21:49:53');

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
-- Records of score_record
-- ----------------------------
INSERT INTO `score_record` VALUES (2063595362545233921, 2062755007931305985, 2063247545389703170, 50, 93.00, 94.00, 95.00, 96.00, 94.50, '优秀', '接口回归测试：修改成绩记录', '2026-06-07 20:14:08', '2026-06-09 14:42:50');
INSERT INTO `score_record` VALUES (2064707952436469761, 2064697112421359618, 2064698112418930689, 2064695798345916417, 30.00, 30.00, 30.00, 10.00, 100.00, '优秀', '', '2026-06-10 21:55:10', '2026-06-10 21:55:10');

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
-- Records of stage_submission
-- ----------------------------
INSERT INTO `stage_submission` VALUES (2063438735540133889, 2063271464540737537, 2062755007931305985, 2063247545389703170, 50, 2, '回归测试：更新现有阶段提交', '验证学生修改自己的阶段提交。', 'https://github.com/youth-cloud/school_project_manager', 'http://localhost:5173', 1, '2026-06-07 09:51:46', '2026-06-07 09:51:46', '2026-06-07 22:03:00', 0);
INSERT INTO `stage_submission` VALUES (2063622448064491522, 2063271464540737537, 2062755007931305985, 2063247545389703170, 50, 99, '回归测试：学生新增阶段提交', '本次主要验证角色校验是否仍然有效。', 'https://github.com/youth-cloud/school_project_manager', 'http://localhost:5173', 1, '2026-06-07 22:01:47', '2026-06-07 22:01:46', '2026-06-07 22:01:46', 0);
INSERT INTO `stage_submission` VALUES (2063969913389076482, 2063271464540737537, 2062755007931305985, 2063247545389703170, 50, 100, '', '', 'https://github.com/youth-cloud/school_project_manager', '', 1, '2026-06-08 21:02:29', '2026-06-08 21:02:28', '2026-06-09 11:54:10', 0);
INSERT INTO `stage_submission` VALUES (2063984242364657665, 2063271464540737537, 2062755007931305985, 2063247545389703170, 50, 101, NULL, NULL, 'https://github.com/youth-cloud/school_project_manager', NULL, 1, '2026-06-08 21:59:25', '2026-06-08 21:59:25', '2026-06-08 21:59:25', 0);
INSERT INTO `stage_submission` VALUES (2064194916126720002, 2063608116215742465, 2062755007931305985, 2063247545389703170, 50, 2, '我先随便说点话', '？新建阶段提交能成功吗', '', '', 1, '2026-06-09 11:56:34', '2026-06-09 11:56:33', '2026-06-09 11:56:33', 0);
INSERT INTO `stage_submission` VALUES (2064687219509452802, 2064675349167824897, 2064670827271905281, 2064686927283904514, 2064638382317199362, 1, '完成了基本框架的搭建', '芜湖', '', '', 1, '2026-06-10 20:32:48', '2026-06-10 20:32:47', '2026-06-10 20:32:47', 0);
INSERT INTO `stage_submission` VALUES (2064705771318697986, 2064704797497778178, 2064697112421359618, 2064698112418930689, 2064695798345916417, 1, '搭了个框架', 'AI干的 感觉很可以啊', '', '', 1, '2026-06-10 21:46:31', '2026-06-10 21:46:30', '2026-06-10 21:47:26', 1);
INSERT INTO `stage_submission` VALUES (2064706226434236418, 2064704797497778178, 2064697112421359618, 2064698112418930689, 2064695798345916417, 1, 'asd', 'asdf', '', '', 1, '2026-06-10 21:48:19', '2026-06-10 21:48:19', '2026-06-10 21:48:19', 0);
INSERT INTO `stage_submission` VALUES (2064962327859863553, 2064704797497778178, 2064697112421359618, 2064947247525027841, 2064950503395434497, 1, 'gzs', 'gzs', '', '', 1, '2026-06-11 14:45:59', '2026-06-11 14:45:58', '2026-06-11 14:45:58', 0);
INSERT INTO `stage_submission` VALUES (2064962863908691970, 2064950101992153089, 2064697112421359618, 2064947247525027841, 2064945005803782146, 1, 'hfh', 'hfh', '', '', 1, '2026-06-11 14:48:07', '2026-06-11 14:48:06', '2026-06-11 14:48:06', 0);

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
-- Records of stage_task
-- ----------------------------
INSERT INTO `stage_task` VALUES (2063271464540737537, 2062755007931305985, 35, '第一阶段需求分析与原型设计-已更新', '补充需求分析、页面原型和数据库设计说明', 1, '2026-06-25 23:59:59', 1, 0, 1, 1, 0, 1, '2026-06-06 22:47:05', '2026-06-06 23:07:24', 0);
INSERT INTO `stage_task` VALUES (2063608116215742465, 2062755007931305985, 35, 'asdf', '完成需求分析文档、功能清单和页面原型设计。', 2, '2026-06-20 23:59:59', 1, 0, 1, 1, 0, 1, '2026-06-07 21:04:49', '2026-06-07 21:04:49', 0);
INSERT INTO `stage_task` VALUES (2064188549823365121, 2063831985245573122, 35, '前端新建任务测试asd', '教师前端新建任务测试', 1, NULL, 0, 1, 1, 1, 0, 1, '2026-06-09 11:31:15', '2026-06-09 11:36:08', 1);
INSERT INTO `stage_task` VALUES (2064675349167824897, 2064670827271905281, 35, '程序设计第一周任务', '随便搭个框架啦', 1, NULL, 0, 0, 0, 1, 0, 1, '2026-06-10 19:45:37', '2026-06-10 19:45:37', 0);
INSERT INTO `stage_task` VALUES (2064704797497778178, 2064697112421359618, 2064696038218162177, '基础框架搭建', '', 1, '2026-06-13 00:00:00', 0, 0, 0, 1, 0, 1, '2026-06-10 21:42:38', '2026-06-10 21:42:38', 0);
INSERT INTO `stage_task` VALUES (2064949978583146498, 2064697112421359618, 2064696038218162177, '个人信息注册', '来个任务吧', 2, NULL, 0, 0, 0, 1, 0, 1, '2026-06-11 13:56:54', '2026-06-11 13:56:54', 0);
INSERT INTO `stage_task` VALUES (2064950101992153089, 2064697112421359618, 2064696038218162177, '项目最后提交', '准备交任务', 3, '2026-06-30 00:00:00', 1, 1, 1, 0, 0, 1, '2026-06-11 13:57:23', '2026-06-11 13:57:23', 0);

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
-- Records of submission_file
-- ----------------------------
INSERT INTO `submission_file` VALUES (2063576620121661441, 2063438735540133889, 'report_20260607_002.pdf', '实训报告-修改版.pdf', 'REPORT_PDF', 307200, 'uploads/report/report_20260607_002.pdf', 'http://localhost:8080/uploads/report/report_20260607_002.pdf', 'REPORT', 50, '2026-06-07 18:59:40');
INSERT INTO `submission_file` VALUES (2063969981726871553, 2063438735540133889, 'regression_report.pdf', '回归测试报告.pdf', 'REPORT_PDF', 204800, 'uploads/report/regression_report.pdf', 'http://localhost:8080/uploads/report/regression_report.pdf', 'REPORT', 50, '2026-06-08 21:02:45');
INSERT INTO `submission_file` VALUES (2063984333347500033, 2063438735540133889, 'big_regression_report.pdf', '大回归测试报告.pdf', 'REPORT_PDF', 123456, 'uploads/report/big_regression_report.pdf', 'http://localhost:8080/uploads/report/big_regression_report.pdf', 'REPORT', 50, '2026-06-08 21:59:46');
INSERT INTO `submission_file` VALUES (2063997204739526657, 2063438735540133889, 'dbcc66be-03e8-44f8-a656-62ec7e1a0517.docx', '6班7组原理实践报告.docx', 'DOCX', 16904, 'submission-files/2063438735540133889/dbcc66be-03e8-44f8-a656-62ec7e1a0517.docx', 'http://localhost:8080/uploads/submission-files/2063438735540133889/dbcc66be-03e8-44f8-a656-62ec7e1a0517.docx', 'REPORT', 50, '2026-06-08 22:50:55');
INSERT INTO `submission_file` VALUES (2064201271189340162, 2064194916126720002, '001a6b30-1a49-449d-a5ac-13faf52b4148.docx', '马克思.docx', 'DOCX', 26549, 'submission-files/2064194916126720002/001a6b30-1a49-449d-a5ac-13faf52b4148.docx', 'http://localhost:8080/uploads/submission-files/2064194916126720002/001a6b30-1a49-449d-a5ac-13faf52b4148.docx', 'REPORT', 50, '2026-06-09 12:21:48');
INSERT INTO `submission_file` VALUES (2064687432701730817, 2064687219509452802, '2b283fb0-0977-4913-8926-322865d0f12f.jpg', 'NSHM_PHOTO_2026_5_7_21_22_02.jpg', 'JPG', 1874486, 'submission-files/2064687219509452802/2b283fb0-0977-4913-8926-322865d0f12f.jpg', 'http://localhost:8080/uploads/submission-files/2064687219509452802/2b283fb0-0977-4913-8926-322865d0f12f.jpg', 'SCREENSHOT', 2064638382317199362, '2026-06-10 20:33:38');
INSERT INTO `submission_file` VALUES (2064705852113575938, 2064705771318697986, '5cd6c15d-340d-4543-942d-61abc056841b.jpg', 'NSHM_PHOTO_2026_5_11_23_42_26.jpg', 'JPG', 2305556, 'submission-files/2064705771318697986/5cd6c15d-340d-4543-942d-61abc056841b.jpg', 'http://localhost:8080/uploads/submission-files/2064705771318697986/5cd6c15d-340d-4543-942d-61abc056841b.jpg', 'SCREENSHOT', 2064695798345916417, '2026-06-10 21:46:50');
INSERT INTO `submission_file` VALUES (2064706331304419329, 2064706226434236418, '24d9a3e9-31f0-49ef-83bf-994b0994495e.jpg', 'NSHM_PHOTO_2026_5_11_23_42_26.jpg', 'JPG', 2305556, 'submission-files/2064706226434236418/24d9a3e9-31f0-49ef-83bf-994b0994495e.jpg', 'http://localhost:8080/uploads/submission-files/2064706226434236418/24d9a3e9-31f0-49ef-83bf-994b0994495e.jpg', 'SCREENSHOT', 2064695798345916417, '2026-06-10 21:48:44');
INSERT INTO `submission_file` VALUES (2064962575835504641, 2064962327859863553, '4ccf6028-4bf5-4b74-835f-c129b94d014b.jpg', 'NSHM_PHOTO_2026_5_4_20_29_26.jpg', 'JPG', 3617810, 'submission-files/2064962327859863553/4ccf6028-4bf5-4b74-835f-c129b94d014b.jpg', 'http://localhost:8080/uploads/submission-files/2064962327859863553/4ccf6028-4bf5-4b74-835f-c129b94d014b.jpg', 'SCREENSHOT', 2064950503395434497, '2026-06-11 14:46:57');
INSERT INTO `submission_file` VALUES (2064963011615301634, 2064962863908691970, 'c2b5d61c-3cd6-44ad-a92b-874e42584432.jpg', 'NSHM_PHOTO_2026_5_11_23_42_26.jpg', 'JPG', 2305556, 'submission-files/2064962863908691970/c2b5d61c-3cd6-44ad-a92b-874e42584432.jpg', 'http://localhost:8080/uploads/submission-files/2064962863908691970/c2b5d61c-3cd6-44ad-a92b-874e42584432.jpg', 'SCREENSHOT', 2064945005803782146, '2026-06-11 14:48:41');

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
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ADMIN', '管理员', '系统管理员', 1, '2026-06-04 22:37:15', '2026-06-04 22:37:15');
INSERT INTO `sys_role` VALUES (2, 'TEACHER', '教师', '实训指导教师', 1, '2026-06-04 22:37:15', '2026-06-04 22:37:15');
INSERT INTO `sys_role` VALUES (3, 'STUDENT', '学生', '实训学生', 1, '2026-06-04 22:37:15', '2026-06-04 22:37:15');

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
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$bqkvc6pJ1Fx2UHHL3lfxuewoYiDCT.0MQk5kZgA0FRrZqeAYgNhNW', 'testadmin', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-06-08 11:26:54', '2026-06-08 11:28:56', 0);
INSERT INTO `sys_user` VALUES (35, 'teacher1', '$2a$10$n6a9BlXg9aaY6Jl5AqM6vOG/F5kMAKkRIJkuySCMUu.UxMpuq92hK', 'wangxiang', NULL, '20251111', 'M', '15044447777', 'suibian@123.com', NULL, 1, 1, NULL, '2026-06-05 18:01:51', '2026-06-08 11:14:40', 0);
INSERT INTO `sys_user` VALUES (36, 'teacher3', '$2a$10$kZB3a2P8qB.zxOibf9T9aeoQZ5.3SddqNLx89qL5EYqiEmsl.h1HS', 'teacher3', NULL, NULL, 'F', NULL, NULL, NULL, 1, 1, NULL, '2026-06-08 22:17:54', '2026-06-08 22:22:10', 0);
INSERT INTO `sys_user` VALUES (50, 'wang', '$2a$10$XStWPLn47G8FSI2pmhm2QuqteArXbi.bmQSpnktDz0aX4zzKqND9y', 'wang', '202553xx', NULL, 'M', '17955556666', 'busuibian@126.com', NULL, 1, 1, NULL, '2026-06-06 20:18:11', '2026-06-10 21:38:44', 0);
INSERT INTO `sys_user` VALUES (51, 'zhao', '$2a$10$xpbHk43vxp608RSbMlMnV.gv5SGGG3bx0c7yImk/maKZPa89q1Omi', 'zhaoss', '2025533x', NULL, 'F', NULL, NULL, NULL, 1, 1, NULL, '2026-06-08 11:30:12', '2026-06-10 18:56:32', 0);
INSERT INTO `sys_user` VALUES (2064638382317199362, '20255359', '$2a$10$aISZXj4grSFTQrOBHt0XX.vXlsbXwgbUd43qSxd.nxXvPiqn521H2', '王智弘', '20255359', NULL, 'M', '15027847453', '1290414790@qq.com', NULL, 2064670472165351426, 1, NULL, '2026-06-10 17:18:44', '2026-06-11 23:33:30', 0);
INSERT INTO `sys_user` VALUES (2064695798345916417, '20255370', '$2a$10$JmIyM496zsmRli0.4ZGjCuTAqJY16FSkYfR7HyXp8WXaw8nt9UACO', '赵科帆', '20255370', NULL, NULL, NULL, NULL, NULL, 2064670472165351426, 1, NULL, '2026-06-10 21:06:53', '2026-06-10 22:42:53', 0);
INSERT INTO `sys_user` VALUES (2064696038218162177, 'wanglu', '$2a$10$fkxoxy/Gg5mo0lcn61splO6JDPM9a4I0FqDNJ64kIWYepoNL8FHyO', '王璐', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-06-10 21:07:50', '2026-06-10 21:07:50', 0);
INSERT INTO `sys_user` VALUES (2064709935335653378, '20255371', '$2a$10$FKXtb/ieEIhxFo8fcqbqRO5D2/6SnTKGL7Mfmy30lFFKYk8ehpOGi', '赵泉', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, '2026-06-10 22:03:03', '2026-06-10 22:03:48', 0);
INSERT INTO `sys_user` VALUES (2064716747409649665, '20255372', '$2a$10$J/i5F2mCWGWMQe9TRFuPrelTpsFN9n9pvimTvzp9pqXInds5PAyw.', '赵永琦', '20255372', NULL, NULL, NULL, NULL, NULL, 2064670472165351426, 1, NULL, '2026-06-10 22:30:07', '2026-06-10 22:30:07', 0);
INSERT INTO `sys_user` VALUES (2064945005803782146, '20255344', '$2a$10$MZ7F5AtzwAojdznwUzKNDuVTCN9Xq2AnjqHqNyDWPV8.9tCPasWL.', '黄福昊', '20255344', NULL, NULL, NULL, NULL, NULL, 2064670472165351426, 1, NULL, '2026-06-11 13:37:08', '2026-06-11 13:37:08', 0);
INSERT INTO `sys_user` VALUES (2064950503395434497, '20255341', '$2a$10$IyV4dg4EPeEm036F6j3xwefgUTJrSXuTe9OfJpRI21zsyF02diEKW', '高梓晟', '20255341', NULL, NULL, NULL, NULL, NULL, 2064670472165351426, 1, NULL, '2026-06-11 13:58:59', '2026-06-11 13:58:59', 0);

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
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (3, 1, 1);
INSERT INTO `sys_user_role` VALUES (1, 35, 2);
INSERT INTO `sys_user_role` VALUES (5, 36, 2);
INSERT INTO `sys_user_role` VALUES (2064703816030642177, 50, 3);
INSERT INTO `sys_user_role` VALUES (2064662756868972545, 51, 3);
INSERT INTO `sys_user_role` VALUES (2064638382354948098, 2064638382317199362, 3);
INSERT INTO `sys_user_role` VALUES (2064695798362693634, 2064695798345916417, 3);
INSERT INTO `sys_user_role` VALUES (2064696038234939394, 2064696038218162177, 2);
INSERT INTO `sys_user_role` VALUES (2064709935356624897, 2064709935335653378, 3);
INSERT INTO `sys_user_role` VALUES (2064716747430621185, 2064716747409649665, 3);
INSERT INTO `sys_user_role` VALUES (2064945005854113793, 2064945005803782146, 3);
INSERT INTO `sys_user_role` VALUES (2064950503462543362, 2064950503395434497, 3);

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
-- Records of topic_application
-- ----------------------------
INSERT INTO `topic_application` VALUES (2063234123860148225, 2062755007931305985, 2062837498155278337, 50, '我对这个课题方向比较熟悉，愿意负责后端开发部分。', 'APPROVED', '2026-06-06 20:24:53', 35, '选题理由充分，同意申请。', '2026-06-06 20:18:42', '2026-06-06 20:24:52');
INSERT INTO `topic_application` VALUES (2063606921929621505, 2062755007931305985, 2062837498155278337, 50, '我对这个课题的技术方向比较熟悉，想申请参与。', 'APPROVED', '2026-06-07 21:01:49', 35, 'test。', '2026-06-07 21:00:04', '2026-06-07 21:01:48');
INSERT INTO `topic_application` VALUES (2063620613949878273, 2062755007931305985, 2063609831459528705, 50, '回归测试：学生正常申请新课题。', 'APPROVED', '2026-06-07 21:58:53', 35, '回归测试：教师正常审核通过。', '2026-06-07 21:54:29', '2026-06-07 21:58:52');
INSERT INTO `topic_application` VALUES (2064255192389812225, 2062755007931305985, 2063609831459528705, 50, '前端学生申请发起', 'APPROVED', '2026-06-09 15:57:29', 35, '前端老师通过选题申请', '2026-06-09 15:56:04', '2026-06-09 15:57:28');
INSERT INTO `topic_application` VALUES (2064258007761858561, 2063831985245573122, 2064257600251670529, 50, '我申请一个试试', 'REJECTED', '2026-06-09 16:07:55', 35, '拒绝一下测试一下', '2026-06-09 16:07:15', '2026-06-09 16:07:55');
INSERT INTO `topic_application` VALUES (2064274157904220162, 2063831985245573122, 2064257600251670529, 50, '我真得申请一下了', 'APPROVED', '2026-06-09 17:11:50', 35, 'go', '2026-06-09 17:11:26', '2026-06-09 17:11:50');
INSERT INTO `topic_application` VALUES (2064671566530568193, 2064670827271905281, 2064671065374154754, 2064638382317199362, '感觉我可以干啊  无敌的', 'APPROVED', '2026-06-10 19:37:20', 35, '', '2026-06-10 19:30:35', '2026-06-10 19:37:20');
INSERT INTO `topic_application` VALUES (2064697546980614145, 2064697112421359618, 2064697230285496321, 2064695798345916417, '期末实训', 'APPROVED', '2026-06-10 21:14:09', 2064696038218162177, '', '2026-06-10 21:13:50', '2026-06-10 21:14:08');
INSERT INTO `topic_application` VALUES (2064945215456067585, 2064697112421359618, 2064697230285496321, 2064945005803782146, 'aa', 'APPROVED', '2026-06-11 13:38:21', 2064696038218162177, 'zhun', '2026-06-11 13:37:58', '2026-06-11 13:38:21');
INSERT INTO `topic_application` VALUES (2064955821240229889, 2064697112421359618, 2064697230285496321, 2064950503395434497, '学生加入项目组后系统自动补建选题申请', 'APPROVED', '2026-06-11 14:20:07', 2064696038218162177, '学生加入项目组后系统自动通过对应课题选题申请', '2026-06-11 14:20:07', '2026-06-11 14:20:07');
INSERT INTO `topic_application` VALUES (2064962111974842370, 2064697112421359618, 2064697230285496321, 2064709935335653378, '历史项目组成员数据自愈时系统自动补建选题申请', 'APPROVED', '2026-06-10 22:05:45', 2064696038218162177, '历史项目组成员数据自愈时系统自动通过对应课题选题申请', '2026-06-11 14:45:07', '2026-06-11 14:45:07');

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
-- Records of training_batch
-- ----------------------------
INSERT INTO `training_batch` VALUES (2062755007931305985, '2026春季Java实训批次', 2, 35, 1, '2025-2026-2', '2026-06-10 09:00:00', '2026-07-10 18:00:00', '2026-07-15 14:00:00', '用于测试的实训批次', 1, '2026-06-05 12:34:52', '2026-06-05 18:33:06', 0);
INSERT INTO `training_batch` VALUES (2063625137842286594, '回归测试批次-教师合法-已更新', 2064284255804346370, 35, 1, '2025-2026-2', '2026-06-12 09:00:00', '2026-07-12 18:00:00', '2026-07-16 14:00:00', '回归测试：教师身份合法时修改批次。', 1, '2026-06-07 22:12:27', '2026-06-09 18:16:06', 0);
INSERT INTO `training_batch` VALUES (2063831985245573122, '教师自建批次测试', 2062738831117283330, 35, 2062559209335439362, '2025-2026-2', '2026-06-20 09:00:00', '2026-07-20 18:00:00', '2026-07-22 14:00:00', '方案B测试：教师创建绑定给自己的批次', 1, '2026-06-08 11:54:24', '2026-06-08 11:54:24', 0);
INSERT INTO `training_batch` VALUES (2064280501008879617, '前端新建实训批次', 2, 35, 1, '2025-2026-2', '2026-06-09 00:00:00', '2026-06-30 00:00:00', NULL, '前端测试', 1, '2026-06-09 17:36:38', '2026-06-09 17:36:38', 0);
INSERT INTO `training_batch` VALUES (2064670827271905281, '网络程序设计实训', 2064663962483908610, 35, 2064670472165351426, '2025-2026-2', '2026-06-10 00:00:00', '2026-06-30 00:00:00', '2026-06-30 00:00:00', '', 1, '2026-06-10 19:27:39', '2026-06-10 19:27:39', 0);
INSERT INTO `training_batch` VALUES (2064696626020507649, '网络程序设计期末实训', 2064663962483908610, 2064696038218162177, 2064670472165351426, '2025-2026-2', '2026-06-09 00:00:00', '2026-06-30 00:00:00', '2026-06-30 00:00:00', '', 1, '2026-06-10 21:10:10', '2026-06-10 21:11:39', 1);
INSERT INTO `training_batch` VALUES (2064697112421359618, '网络程序设计计算机六班期末实训', 2064663962483908610, 2064696038218162177, 2064670472165351426, '2025-2026-2', '2026-06-09 00:00:00', '2026-06-30 00:00:00', '2026-06-30 00:00:00', '', 1, '2026-06-10 21:12:06', '2026-06-10 21:30:18', 0);

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

-- ----------------------------
-- Records of weekly_report
-- ----------------------------
INSERT INTO `weekly_report` VALUES (1, 1, 1, 1, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1, '2026-06-07 19:35:03', '2026-06-07 19:35:10', 1);
INSERT INTO `weekly_report` VALUES (2063584603337527298, 2062755007931305985, 2063247545389703170, 50, 2, '更新后的周报内容：补完 WeeklyReport 深层校验测试', '暂时没有新问题', '继续推进下一模块', '进度不错，继续推进', 88.50, '2026-06-07 19:31:24', NULL, 1, '2026-06-07 19:31:23', '2026-06-08 19:46:59', 0);
INSERT INTO `weekly_report` VALUES (2063623162962636801, 2062755007931305985, 2063247545389703170, 50, 99, '尝试注入教师字段', '测试 teacherComment / score', '继续', NULL, NULL, '2026-06-07 22:04:37', NULL, 1, '2026-06-07 22:04:36', '2026-06-07 22:08:09', 0);
INSERT INTO `weekly_report` VALUES (2063950798163177473, 2062755007931305985, 2063247545389703170, 50, 3, '完成了周报深层校验联调', '暂无阻塞问题', '继续推进下一批权限补强', NULL, NULL, '2026-06-08 19:46:31', NULL, 1, '2026-06-08 19:46:31', '2026-06-08 19:46:31', 0);
INSERT INTO `weekly_report` VALUES (2063969648917237761, 2062755007931305985, 2063247545389703170, 50, 8, '接口回归测试：新增周报', '暂无', '继续做整轮回归', NULL, NULL, '2026-06-08 21:01:26', NULL, 1, '2026-06-08 21:01:25', '2026-06-08 21:01:25', 0);
INSERT INTO `weekly_report` VALUES (2063984189004722177, 2062755007931305985, 2063247545389703170, 50, 10, '大回归测试：新增周报', '暂无', '继续回归', NULL, NULL, '2026-06-08 21:59:12', NULL, 1, '2026-06-08 21:59:12', '2026-06-08 21:59:12', 0);
INSERT INTO `weekly_report` VALUES (2064238548858834945, 2062755007931305985, 2063247545389703170, 50, 2, '前端测试周报新建', '？test', '我草 计划是啥啊', NULL, NULL, '2026-06-09 14:49:56', NULL, 1, '2026-06-09 14:49:56', '2026-06-09 14:49:56', 0);
INSERT INTO `weekly_report` VALUES (2064698632902696962, 2064697112421359618, 2064698112418930689, 2064695798345916417, 1, '搭好了基本架构 写了文档', '乱套了', '让AI狠狠干活', NULL, NULL, '2026-06-10 21:18:09', NULL, 1, '2026-06-10 21:18:08', '2026-06-10 21:18:08', 0);

SET FOREIGN_KEY_CHECKS = 1;
