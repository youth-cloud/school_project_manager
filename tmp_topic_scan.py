import re
from pathlib import Path
path = Path(r'F:\school_project_manager\frontend\src\views\topic-application\TopicApplicationListView.vue')
text = path.read_text(encoding='utf-8')
pattern = re.compile(r'[^\x00-\x7f\r\n]{2,}')
rows = []
seen = set()
for m in pattern.finditer(text):
    s = m.group(0)
    if s in seen:
        continue
    seen.add(s)
    try:
        fixed = s.encode('gb18030').decode('utf-8')
    except Exception:
        continue
    if fixed != s:
        rows.append('SRC: ' + s)
        rows.append('FIX: ' + fixed)
        rows.append('---')
Path(r'F:\school_project_manager\tmp_topic_remaining.txt').write_text('\n'.join(rows), encoding='utf-8')
