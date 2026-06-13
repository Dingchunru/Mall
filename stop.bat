@echo off
chcp 65001 >nul

echo ========== 关闭 Mall 微服务 ==========
echo 正在关闭所有微服务窗口...

taskkill /fi "WINDOWTITLE eq mall-gateway*" /f 2>nul
taskkill /fi "WINDOWTITLE eq mall-auth*" /f 2>nul
taskkill /fi "WINDOWTITLE eq mall-user*" /f 2>nul
taskkill /fi "WINDOWTITLE eq mall-product*" /f 2>nul
taskkill /fi "WINDOWTITLE eq mall-order*" /f 2>nul
taskkill /fi "WINDOWTITLE eq mall-cart*" /f 2>nul
taskkill /fi "WINDOWTITLE eq mall-seckill*" /f 2>nul
taskkill /fi "WINDOWTITLE eq mall-search*" /f 2>nul

echo ========== 关闭基础服务 ==========
echo 关闭 Filebeat...
taskkill /fi "WINDOWTITLE eq filebeat*" /f 2>nul

echo 关闭 Kibana...
taskkill /fi "WINDOWTITLE eq kibana*" /f 2>nul

echo 关闭 Nacos...
taskkill /fi "WINDOWTITLE eq nacos*" /f 2>nul

echo 关闭 Elasticsearch...
taskkill /fi "WINDOWTITLE eq elasticsearch*" /f 2>nul

echo ========== 清理 Java 进程 ==========
echo 强制结束所有 mvn spring-boot 进程...
wmic process where "commandline like '%%spring-boot:run%%'" delete 2>nul

echo ========== 全部关闭完成 ==========
pause