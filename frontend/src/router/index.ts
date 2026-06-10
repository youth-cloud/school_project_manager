import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '@/layouts/AdminLayout.vue'
import { getToken } from '@/utils/token'
import LoginView from '@/views/auth/LoginView.vue'
import DashboardView from '@/views/dashboard/DashboardView.vue'
import ProfileView from '@/views/profile/ProfileView.vue'
import NoticeListView from '@/views/notice/NoticeListView.vue'
import StageTaskListView from '@/views/stage-task/StageTaskListView.vue'
import StageSubmissionListView from '@/views/stage-submission/StageSubmissionListView.vue'
import SubmissionFileListView from '@/views/submission-file/SubmissionFileListView.vue'
import ReviewRecordListView from '@/views/review-record/ReviewRecordListView.vue'
import ScoreRecordListView from '@/views/score-record/ScoreRecordListView.vue'
import WeeklyReportListView from '@/views/weekly-report/WeeklyReportListView.vue'
import DefenseScheduleListView from '@/views/defense-schedule/DefenseScheduleListView.vue'
import DefenseRecordListView from '@/views/defense-record/DefenseRecordListView.vue'
import TopicApplicationListView from '@/views/topic-application/TopicApplicationListView.vue'
import ProjectTopicListView from '@/views/project-topic/ProjectTopicListView.vue'
import ProjectGroupListView from '@/views/project-group/ProjectGroupListView.vue'
import ProjectGroupApplicationListView from '@/views/project-group-application/ProjectGroupApplicationListView.vue'
import ProjectGroupMemberListView from '@/views/project-group-member/ProjectGroupMemberListView.vue'
import TrainingBatchListView from '@/views/training-batch/TrainingBatchListView.vue'
import EduClassListView from '@/views/edu-class/EduClassListView.vue'
import EduCourseListView from '@/views/edu-course/EduCourseListView.vue'
import OperationLogListView from '@/views/operation-log/OperationLogListView.vue'
import SysUserListView from '@/views/sys-user/SysUserListView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      component: AdminLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'dashboard',
          component: DashboardView,
        },
        {
          path: 'profile',
          name: 'profile',
          component: ProfileView,
        },
        {
          path: 'notices',
          name: 'notice-list',
          component: NoticeListView,
        },
        {
          path: 'stage-tasks',
          name: 'stage-task-list',
          component: StageTaskListView,
        },
        {
          path: 'stage-submissions',
          name: 'stage-submission-list',
          component: StageSubmissionListView,
        },
        {
          path: 'submission-files',
          name: 'submission-file-list',
          component: SubmissionFileListView,
        },
        {
          path: 'review-records',
          name: 'review-record-list',
          component: ReviewRecordListView,
        },
        {
          path: 'score-records',
          name: 'score-record-list',
          component: ScoreRecordListView,
        },
        {
          path: 'weekly-reports',
          name: 'weekly-report-list',
          component: WeeklyReportListView,
        },
        {
          path: 'defense-schedules',
          name: 'defense-schedule-list',
          component: DefenseScheduleListView,
        },
        {
          path: 'defense-records',
          name: 'defense-record-list',
          component: DefenseRecordListView,
        },
        {
          path: 'topic-applications',
          name: 'topic-application-list',
          component: TopicApplicationListView,
        },
        {
          path: 'project-topics',
          name: 'project-topic-list',
          component: ProjectTopicListView,
        },
        {
          path: 'project-groups',
          name: 'project-group-list',
          component: ProjectGroupListView,
        },
        {
          path: 'project-group-applications',
          name: 'project-group-application-list',
          component: ProjectGroupApplicationListView,
        },
        {
          path: 'project-group-members',
          name: 'project-group-member-list',
          component: ProjectGroupMemberListView,
        },
        {
          path: 'training-batches',
          name: 'training-batch-list',
          component: TrainingBatchListView,
        },
        {
          path: 'edu-classes',
          name: 'edu-class-list',
          component: EduClassListView,
        },
        {
          path: 'edu-courses',
          name: 'edu-course-list',
          component: EduCourseListView,
        },
        {
          path: 'operation-logs',
          name: 'operation-log-list',
          component: OperationLogListView,
        },
        {
          path: 'sys-users',
          name: 'sys-user-list',
          component: SysUserListView,
        },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const token = getToken()
  const requiresAuth = to.matched.some((record) => record.meta.requiresAuth)

  if (requiresAuth && !token) {
    return '/login'
  }

  if (to.path === '/login' && token) {
    return '/'
  }

  return true
})

export default router
