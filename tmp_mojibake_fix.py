import re
from pathlib import Path
files = [
    r'F:\school_project_manager\frontend\src\views\weekly-report\WeeklyReportListView.vue',
    r'F:\school_project_manager\frontend\src\views\training-batch\TrainingBatchListView.vue',
    r'F:\school_project_manager\frontend\src\views\topic-application\TopicApplicationListView.vue',
    r'F:\school_project_manager\frontend\src\views\sys-user\SysUserListView.vue',
    r'F:\school_project_manager\frontend\src\views\stage-task\StageTaskListView.vue',
    r'F:\school_project_manager\frontend\src\views\stage-submission\StageSubmissionListView.vue',
    r'F:\school_project_manager\frontend\src\views\score-record\ScoreRecordListView.vue',
    r'F:\school_project_manager\frontend\src\views\review-record\ReviewRecordListView.vue',
    r'F:\school_project_manager\frontend\src\views\project-topic\ProjectTopicListView.vue',
    r'F:\school_project_manager\frontend\src\views\project-group\ProjectGroupListView.vue',
    r'F:\school_project_manager\frontend\src\views\project-group-member\ProjectGroupMemberListView.vue',
    r'F:\school_project_manager\frontend\src\views\project-group-application\ProjectGroupApplicationListView.vue',
    r'F:\school_project_manager\frontend\src\views\operation-log\OperationLogListView.vue',
    r'F:\school_project_manager\frontend\src\views\notice\NoticeListView.vue',
    r'F:\school_project_manager\frontend\src\views\edu-course\EduCourseListView.vue',
    r'F:\school_project_manager\frontend\src\views\edu-class\EduClassListView.vue',
    r'F:\school_project_manager\frontend\src\views\defense-record\DefenseRecordListView.vue',
    r'F:\school_project_manager\frontend\src\views\defense-schedule\DefenseScheduleListView.vue',
]
pattern = re.compile(r'[^\x00-\x7f\r\n]{2,}')
for file_path in files:
    path = Path(file_path)
    text = path.read_text(encoding='utf-8')
    def fix_match(match):
        s = match.group(0)
        try:
            fixed = s.encode('gb18030').decode('utf-8')
        except Exception:
            return s
        return fixed if fixed != s else s
    updated = pattern.sub(fix_match, text)
    if updated != text:
        path.write_text(updated, encoding='utf-8', newline='\n')
