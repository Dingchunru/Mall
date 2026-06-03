@echo off
chcp 65001 >nul
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