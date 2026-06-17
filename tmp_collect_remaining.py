import re
from pathlib import Path
base = Path(r'F:\school_project_manager\frontend\src\views')
pattern = re.compile(r'[^\x00-\x7f\r\n]{2,}')
markers = ('鍏','璇','鎵','鐘','褰','宸','寰','鏆','闁','閻','鐎','缁','椤','绛','鎻','鎶','闂','灞','鍙','閲','鎿','璁','','')
rows = []
seen = set()
for path in sorted(base.rglob('*.vue')):
    text = path.read_text(encoding='utf-8')
    for lineno, line in enumerate(text.splitlines(), 1):
        if any(m in line for m in markers):
            key = line.strip()
            if key and key not in seen:
                seen.add(key)
                rows.append(f'{path}:{lineno}\t{key}')
Path(r'F:\school_project_manager\tmp_remaining_lines.txt').write_text('\n'.join(rows), encoding='utf-8')
