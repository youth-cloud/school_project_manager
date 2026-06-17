import re
from pathlib import Path
base = Path(r'F:\school_project_manager\frontend\src\views')
pattern = re.compile(r'[^\x00-\x7f\r\n]{2,}')
markers = set('鍏璇鏆鎵鐘閫鍒瀹缁狅鐎涳闁閻褰宸寰閲闅鎿灞炴壒娆哄簭鐢寵癑悕绉圭洰缁勫垪琛欏笀璁板綍鏃堕棿鏍规嫨答辩組長成员项目课题审核')
for path in base.rglob('*.vue'):
    text = path.read_text(encoding='utf-8')
    def fix_match(match):
        s = match.group(0)
        if not any(ch in markers for ch in s):
            return s
        try:
            fixed = s.encode('gb18030').decode('utf-8')
        except Exception:
            return s
        return fixed if fixed != s else s
    updated = pattern.sub(fix_match, text)
    if updated != text:
        path.write_text(updated, encoding='utf-8', newline='\n')
