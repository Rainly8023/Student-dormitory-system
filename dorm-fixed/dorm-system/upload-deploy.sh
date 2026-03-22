#!/bin/bash
# 本地执行：把 JAR 包上传到 Oracle 服务器并重启
# 使用前修改下面三个变量

SERVER_IP="你的Oracle服务器IP"
SSH_KEY="~/.ssh/oracle_key.pem"    # Oracle 下载的私钥路径
JAR_PATH="./backend/target/dorm-system-1.0.0.jar"

echo "📦 打包后端..."
cd backend && mvn clean package -DskipTests -q && cd ..

echo "📤 上传 JAR 到服务器..."
scp -i $SSH_KEY $JAR_PATH ubuntu@$SERVER_IP:/tmp/app.jar

echo "🚀 部署到 /opt/dorm 并重启服务..."
ssh -i $SSH_KEY ubuntu@$SERVER_IP << 'REMOTE'
sudo mv /tmp/app.jar /opt/dorm/app.jar
sudo systemctl restart dorm
sleep 5
sudo systemctl is-active dorm && echo "✅ 部署成功！" || echo "❌ 启动失败，查看：sudo journalctl -u dorm -n 30"
REMOTE
