#!/bin/bash
# Oracle Cloud 免费VM 一键部署脚本
# 系统：Ubuntu 22.04（Oracle 免费VM默认系统）
# 使用方式：ssh 进服务器后执行 bash deploy.sh

set -e
echo "========================================="
echo "  宿舍管理系统 - Oracle Cloud 部署脚本"
echo "========================================="

# -------- 1. 安装依赖 --------
echo "[1/6] 安装 Java 17 + MySQL + Nginx..."
sudo apt-get update -q
sudo apt-get install -y openjdk-17-jdk mysql-server nginx unzip curl

# -------- 2. 配置 MySQL --------
echo "[2/6] 配置 MySQL 数据库..."
sudo systemctl start mysql
sudo systemctl enable mysql

# 创建数据库和用户
sudo mysql -e "
CREATE DATABASE IF NOT EXISTS dorm_system DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'dorm'@'localhost' IDENTIFIED BY 'Dorm@2024!';
GRANT ALL PRIVILEGES ON dorm_system.* TO 'dorm'@'localhost';
FLUSH PRIVILEGES;
"
echo "  ✅ 数据库创建完成 (用户: dorm, 密码: Dorm@2024!)"

# -------- 3. 部署后端 JAR --------
echo "[3/6] 部署 Spring Boot 后端..."
sudo mkdir -p /opt/dorm
# 把你打包好的 JAR 上传到服务器后解注释下面这行：
# sudo cp /tmp/dorm-system-1.0.0.jar /opt/dorm/app.jar

# 创建启动配置文件
sudo tee /opt/dorm/app.env > /dev/null << 'ENV'
MYSQL_URL=jdbc:mysql://localhost:3306/dorm_system?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
MYSQL_USER=dorm
MYSQL_PASSWORD=Dorm@2024!
JWT_SECRET=dorm-system-super-secret-key-for-production-2024
ENV

# 注册为 systemd 服务（开机自启）
sudo tee /etc/systemd/system/dorm.service > /dev/null << 'SERVICE'
[Unit]
Description=Dorm Management System
After=network.target mysql.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/dorm
EnvironmentFile=/opt/dorm/app.env
ExecStart=/usr/bin/java -Xms256m -Xmx512m -jar /opt/dorm/app.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
SERVICE

sudo systemctl daemon-reload
sudo systemctl enable dorm
echo "  ✅ 后端服务注册完成（开机自启）"

# -------- 4. 配置 Nginx 反向代理 --------
echo "[4/6] 配置 Nginx..."
sudo tee /etc/nginx/sites-available/dorm > /dev/null << 'NGINX'
server {
    listen 80;
    server_name _;   # 替换为你的域名或IP

    # 后端 API 反向代理
    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_read_timeout 60s;
    }

    # 前端静态文件（如果要前后端一起部署）
    location / {
        root /var/www/dorm;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
}
NGINX

sudo ln -sf /etc/nginx/sites-available/dorm /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t && sudo systemctl restart nginx
sudo systemctl enable nginx
echo "  ✅ Nginx 配置完成"

# -------- 5. 开放防火墙端口 --------
echo "[5/6] 配置防火墙..."
# Ubuntu 自带 ufw
sudo ufw allow 22    # SSH
sudo ufw allow 80    # HTTP
sudo ufw allow 443   # HTTPS（备用）
sudo ufw --force enable
echo "  ✅ 防火墙已开放 22/80/443 端口"
echo "  ⚠️  记得在 Oracle 控制台的「安全列表」里也开放 80 端口！"

# -------- 6. 启动服务 --------
echo "[6/6] 启动服务..."
# 先确认 JAR 存在再启动
if [ -f "/opt/dorm/app.jar" ]; then
    sudo systemctl start dorm
    sleep 5
    if sudo systemctl is-active --quiet dorm; then
        echo "  ✅ 后端服务启动成功"
    else
        echo "  ❌ 后端服务启动失败，查看日志：sudo journalctl -u dorm -n 50"
    fi
else
    echo "  ⚠️  未找到 app.jar，请先上传 JAR 包到 /opt/dorm/app.jar 再执行："
    echo "     sudo systemctl start dorm"
fi

echo ""
echo "========================================="
echo "  部署完成！"
echo "  后端 API：http://$(curl -s ifconfig.me)/api"
echo "  查看日志：sudo journalctl -u dorm -f"
echo "  重启服务：sudo systemctl restart dorm"
echo "========================================="
