@echo off
echo 正在清理构建文件...

echo 停止Gradle守护进程...
call gradlew.bat --stop

echo 删除构建目录...
rd /s /q "%~dp0app\build"
rd /s /q "%~dp0build"

echo 删除.gradle缓存...
rd /s /q "%~dp0.gradle"

echo 清理完成！
echo 现在可以尝试重新构建项目。

pause