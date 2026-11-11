Add-Type -AssemblyName System.Drawing
$ErrorActionPreference = 'Stop'
$dest = $PSScriptRoot
function Brush($hex) { [System.Drawing.SolidBrush]::new([System.Drawing.ColorTranslator]::FromHtml($hex)) }
function Rect($x,$y,$w,$h,$color) { $b=Brush $color; $script:g.FillRectangle($b,$x,$y,$w,$h); $b.Dispose() }
function Text($text,$x,$y,$size=28,$color='#172D50',$bold=$false) {
  $style = if($bold){[System.Drawing.FontStyle]::Bold}else{[System.Drawing.FontStyle]::Regular}
  $font=[System.Drawing.Font]::new('Microsoft YaHei',$size,$style,[System.Drawing.GraphicsUnit]::Pixel)
  $b=Brush $color
  $script:g.DrawString($text,$font,$b,[single]$x,[single]$y)
  $font.Dispose(); $b.Dispose()
}
function Line($x1,$y1,$x2,$y2,$color='#ADC4EB',$width=3) {
  $pen=[System.Drawing.Pen]::new([System.Drawing.ColorTranslator]::FromHtml($color),$width)
  $script:g.DrawLine($pen,$x1,$y1,$x2,$y2); $pen.Dispose()
}
function Arrow($x1,$y1,$x2,$y2) {
  $pen=[System.Drawing.Pen]::new([System.Drawing.ColorTranslator]::FromHtml('#326BEB'),4)
  $pen.CustomEndCap=[System.Drawing.Drawing2D.AdjustableArrowCap]::new(5,6)
  $script:g.DrawLine($pen,$x1,$y1,$x2,$y2); $pen.Dispose()
}
function Card($x,$y,$w,$h,$fill='#FFFFFF') {
  Rect ($x+3) ($y+5) $w $h '#E4EBF6'
  Rect $x $y $w $h $fill
}
function StartSlide($num,$section,$title,$subtitle) {
  $script:bmp=[System.Drawing.Bitmap]::new(1920,1080)
  $script:g=[System.Drawing.Graphics]::FromImage($script:bmp)
  $script:g.SmoothingMode=[System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $script:g.TextRenderingHint=[System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
  $script:g.Clear([System.Drawing.ColorTranslator]::FromHtml('#F3F7FD'))
  Rect 0 0 1920 12 '#326BEB'
  Text "学校实训管理平台  /  $section" 90 55 24 '#48648C'
  Text $num 1725 42 56 '#326BEB' $true
  Text $title 86 125 62 '#172D50' $true
  Text $subtitle 90 218 28 '#60738E'
  Line 90 994 1830 994 '#D4DEEE' 2
  Text '校园实训 · 项目实践 · 全流程协作' 90 1014 22 '#60738E'
  Text '项目介绍 / 奖学金申请材料' 1528 1014 22 '#60738E'
}
function SaveSlide($name) { $script:bmp.Save((Join-Path $dest $name),[System.Drawing.Imaging.ImageFormat]::Png); $script:g.Dispose(); $script:bmp.Dispose() }

StartSlide '01' '项目概览' '让实训全过程有序协同' '面向校园实训场景，构建统一的项目管理入口'
Card 90 310 1090 380 '#173D7B'
Text '学校实训管理平台' 132 349 49 '#FFFFFF' $true
Text '连接学生、教师与管理员' 135 427 32 '#CFDFFF'
Text '选题组队  →  过程跟踪  →  成果评价' 135 504 38 '#FFFFFF' $true
Rect 135 590 256 50 '#326BEB'
Text '已完成云端部署' 154 598 26 '#FFFFFF' $true
Text '浏览器统一访问' 420 600 26 '#CFDFFF'
Card 1215 310 615 380
Text '聚焦真实教学需求' 1250 341 33 '#172D50' $true
$needs=@(@('资料集中','统一提交阶段成果与附件'),@('过程留痕','保留周报、审核与反馈'),@('评价有据','关联答辩记录与最终成绩'))
for($i=0;$i -lt 3;$i++){
  $y=410+$i*83
  Text ('0'+($i+1)) 1252 $y 32 '#326BEB' $true
  Text $needs[$i][0] 1320 $y 28 '#172D50' $true
  Text $needs[$i][1] 1320 ($y+39) 23 '#60738E'
}
$roles=@(@('学生','选题 · 组队 · 提交成果','查看任务、反馈与成绩'),@('教师','指导 · 审核 · 答辩评分','发布课题、任务与评价'),@('管理员','账号 · 课程 · 班级管理','维护实训基础数据'))
for($i=0;$i -lt 3;$i++){
  $x=90+$i*590; Card $x 733 560 215
  Rect $x 733 6 215 '#326BEB'
  Text $roles[$i][0] ($x+32) 757 36 '#173D7B' $true
  Text $roles[$i][1] ($x+32) 818 28 '#172D50'
  Text $roles[$i][2] ($x+32) 868 24 '#60738E'
}
SaveSlide '01-项目概览.png'

StartSlide '02' '业务流程' '从选题到答辩，全程可追溯' '贯通实训关键环节，让任务、材料与评价有据可查'
$steps=@(@('课题发布','教师发布实训课题','明确要求与人数'),@('选题组队','学生申请 · 教师审核','形成正式项目组'),@('阶段任务','设置任务与截止时间','明确阶段交付要求'),@('成果提交','阶段材料 · 附件 · 周报','集中记录项目进展'),@('教师评审','查看成果与审核反馈','支持修改与再次提交'),@('答辩评分','答辩安排与成绩记录','统一记录实训结果'))
for($i=0;$i -lt 6;$i++){
  $x=90+($i%3)*595; $y=320+[math]::Floor($i/3)*285
  Card $x $y 550 225
  Text ('0'+($i+1)) ($x+28) ($y+24) 39 '#326BEB' $true
  Text $steps[$i][0] ($x+115) ($y+24) 38 '#172D50' $true
  Text $steps[$i][1] ($x+30) ($y+98) 29 '#172D50'
  Text $steps[$i][2] ($x+30) ($y+151) 25 '#60738E'
  if(($i%3)-lt 2){Arrow ($x+557) ($y+112) ($x+583) ($y+112)}
}
Line 1810 547 1810 572 '#326BEB' 4
Line 1810 572 111 572 '#326BEB' 4
Arrow 111 572 111 602
Rect 90 875 1740 79 '#DFEAFE'
Text '申请与审核有记录' 157 895 29 '#173D7B' $true
Text '材料与周报可查询' 746 895 29 '#173D7B' $true
Text '答辩与成绩统一管理' 1302 895 29 '#173D7B' $true
SaveSlide '02-业务流程.png'

StartSlide '03' '技术架构' '前后端分离，支撑云端运行' '从页面交互、业务处理到数据存储，形成完整工程链路'
$layers=@(@('访问入口','浏览器 / Nginx','静态资源与接口转发'),@('前端交互','Vue 3 · TypeScript · Element Plus','Pinia · Vue Router · Axios'),@('后端服务','Spring Boot 4 · MyBatis-Plus','Spring Security · JWT'),@('数据与文件','MySQL 8  /  Redis 7  /  服务器文件目录','业务数据  /  令牌状态  /  成果附件'))
for($i=0;$i -lt 4;$i++){
  $y=310+$i*163
  Card 90 $y 1160 137
  Rect 90 $y 210 137 '#173D7B'
  Text $layers[$i][0] 113 ($y+46) 31 '#FFFFFF' $true
  Text $layers[$i][1] 330 ($y+23) 33 '#172D50' $true
  Text $layers[$i][2] 331 ($y+80) 25 '#60738E'
  if($i -lt 3){Arrow 671 ($y+141) 671 ($y+158)}
}
$features=@(@('01','三类角色权限','学生 · 教师 · 管理员','按角色与业务归属控制访问'),@('02','容器化部署配置','Docker Compose','提供前后端及数据库服务编排'),@('03','自动构建检查','GitHub Actions','前端构建与后端打包检查'))
for($i=0;$i -lt 3;$i++){
  $y=310+$i*219
  Card 1290 $y 540 188
  Text $features[$i][0] 1320 ($y+23) 26 '#326BEB' $true
  Text $features[$i][1] 1380 ($y+19) 32 '#172D50' $true
  Text $features[$i][2] 1320 ($y+76) 29 '#173D7B'
  Text $features[$i][3] 1320 ($y+128) 23 '#60738E'
}
SaveSlide '03-技术架构.png'
Get-ChildItem -LiteralPath $dest -Filter '*.png' | Select-Object Name,Length
