@echo off
chcp 65001 >nul

echo ========== 启动基础服务 ==========

echo [1/4] 启动 Elasticsearch...
start "elasticsearch" cmd /k "cd /d E:\ES\elasticsearch-7.17.18\bin && elasticsearch.bat"
timeout /t 5 >nul

echo [2/4] 启动 Nacos...
start "nacos" cmd /k "cd /d E:\Nacos\nacos\bin && startup.cmd -m standalone"
timeout /t 5 >nul

echo [3/4] 启动 Kibana...
start "kibana" cmd /k "cd /d E:\Kibana\kibana-7.17.18-windows-x86_64\bin && kibana.bat"
timeout /t 5 >nul

echo [4/4] 启动 Filebeat...
start "filebeat" cmd /k "cd /d E:\FB\filebeat-7.17.18-windows-x86_64 && filebeat.exe -e -c mall-filebeat.yml"
timeout /t 5 >nul

echo ========== 基础服务启动完成，等待就绪 ==========
echo 等待 Elasticsearch 和 Nacos 启动...
timeout /t 10 >nul

echo ========== 启动 Mall 微服务 ==========

start "mall-gateway" cmd /k "cd /d E:\Mall\mall-gateway && mvn spring-boot:run"
timeout /t 8 >nul

start "mall-auth" cmd /k "cd /d E:\Mall\mall-auth && mvn spring-boot:run"
start "mall-user" cmd /k "cd /d E:\Mall\mall-user && mvn spring-boot:run"
start "mall-product" cmd /k "cd /d E:\Mall\mall-product && mvn spring-boot:run"
start "mall-order" cmd /k "cd /d E:\Mall\mall-order && mvn spring-boot:run"
start "mall-cart" cmd /k "cd /d E:\Mall\mall-cart && mvn spring-boot:run"
start "mall-seckill" cmd /k "cd /d E:\Mall\mall-seckill && mvn spring-boot:run"
start "mall-search" cmd /k "cd /d E:\Mall\mall-search && mvn spring-boot:run"

echo ========== 全部启动完成 ==========
pause