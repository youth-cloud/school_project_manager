from pathlib import Path
base = Path(r'F:\school_project_manager\frontend\src\views')
replacements = {
    '褰撳墠鍒楄〃共': '当前列表共',
    '条褰': '条记录',
    '项目组勫悕绉?': '项目组名称',
    '项目组勫悕绉': '项目组名称',
    '项目组勫垪琛': '项目组列表',
    '状态?': '状态',
    '状态': '状态',
    '璇烽夋嫨项目组': '请选择项目组',
    '璇烽夋嫨状态': '请选择状态',
    '璇烽夋嫨鐩爣瑙掕壊': '请选择目标角色',
    '鑽夌/停用': '草稿/停用',
    '璇峰厛杈撳叆项目组ID': '请先输入项目组 ID',
    '鎵灞炴壒娆?': '所属批次',
    '鎵灞炴壒娆': '所属批次',
    '璇烽夋嫨瀹炶鎵规': '请选择实训批次',
    '璇烽夋嫨缁勯暱': '请选择组长',
    '璇烽夋嫨鏁欏笀': '请选择教师',
    '璇烽夋嫨璇鹃': '请选择课题',
    '瀛︾敓鍙姞鍏ユ寮忛」鐩粍锛屼篃鍙鍑鸿嚜宸辩殑鎴愬憳璁板綍': '学生可加入正式项目组，也可退出自己的成员记录',
}
for path in base.rglob('*.vue'):
    text = path.read_text(encoding='utf-8')
    updated = text
    for old, new in replacements.items():
        updated = updated.replace(old, new)
    if updated != text:
        path.write_text(updated, encoding='utf-8', newline='\n')
