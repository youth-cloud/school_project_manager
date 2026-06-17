import re
from pathlib import Path
path = Path(r'F:\school_project_manager\frontend\src\views\topic-application\TopicApplicationListView.vue')
out = Path(r'F:\school_project_manager\tmp_mojibake_out.txt')
text = path.read_text(encoding='utf-8')
seen = set()
rows = []
for m in re.finditer(r'[^\x00-\x7f\r\n]{2,}', text):
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
out.write_text('\n'.join(rows), encoding='utf-8')
