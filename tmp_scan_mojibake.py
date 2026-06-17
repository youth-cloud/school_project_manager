import re
from pathlib import Path
base = Path(r'F:\school_project_manager\frontend\src\views')
pattern = re.compile(r'[^\x00-\x7f\r\n]{2,}')
rows = []
for path in sorted(base.rglob('*.vue')):
    text = path.read_text(encoding='utf-8')
    count = 0
    for m in pattern.finditer(text):
        s = m.group(0)
        try:
            fixed = s.encode('gb18030').decode('utf-8')
        except Exception:
            continue
        if fixed != s:
            count += 1
    if count:
        rows.append(f'{count}\t{path}')
Path(r'F:\school_project_manager\tmp_remaining_mojibake.txt').write_text('\n'.join(rows), encoding='utf-8')
