#!/bin/bash
# Script untuk restart Jenkins container dengan Docker mount

echo "🛑 Stopping Jenkins container..."
docker stop jenkins-blueocean

echo "📦 Backing up Jenkins data..."
docker exec jenkins-blueocean tar czf /tmp/backup.tar.gz /var/jenkins_home 2>/dev/null || echo "Backup skipped (container may be stopped)"
docker cp jenkins-blueocean:/tmp/backup.tar.gz ./jenkins-backup-$(date +%Y%m%d-%H%M%S).tar.gz 2>/dev/null || echo "No backup to copy"

echo "🗑️ Removing Jenkins container..."
docker rm jenkins-blueocean

echo "🚀 Creating new Jenkins container with Docker mount..."
docker run -d \
  --name jenkins-blueocean \
  --restart unless-stopped \
  -p 9000:8080 \
  -p 50000:50000 \
  --link jenkins-docker:docker \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker:ro \
  jenkins/jenkins:lts-jdk17

echo ""
echo "✅ Jenkins restarted with Docker access!"
echo ""
echo "⏳ Waiting for Jenkins to start (30 seconds)..."
sleep 30

echo ""
echo "📊 Jenkins container status:"
docker ps | grep jenkins-blueocean

echo ""
echo "✅ Done! Jenkins should be available at: http://your-vps:9000"
