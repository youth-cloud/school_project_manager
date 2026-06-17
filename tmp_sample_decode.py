samples = [
    '椤圭洰缁勫悕绉',
    '鎵灞炴壒娆',
    '璇烽夋嫨瀹炶鎵规',
    '璇烽夋嫨缁勯暱',
    '鐘舵',
    '鍏?{{ total }} 鏉',
    '褰撳墠鍒楄〃鍏?{{ total }} 个正式项目组',
]
for s in samples:
    try:
        fixed = s.encode('gb18030').decode('utf-8')
    except Exception as e:
        fixed = f'ERR: {e}'
    print('SRC:', s)
    print('FIX:', fixed)
    print('---')
