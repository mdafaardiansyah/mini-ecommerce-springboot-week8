/**
 * Jenkins Pipeline for Order Management API - Docker + Heroku
 *
 * Deployment Strategy:
 * - develop branch → dev profile → week8-practice1-dev
 * - main/master branch → prod profile → week8-practice1-prod
 *
 * Requirements:
 * ✅ Use Docker for deployment
 * ✅ Auto-restart containers (Heroku handles this)
 * ✅ Reflect latest code (always build fresh image)
 * ✅ Use correct Spring profile
 * ✅ If unit tests failed, build failed
 *
 * Prerequisites:
 * - Docker MUST be pre-installed on Jenkins agent
 * - Docker Hub credentials configured
 * - Heroku API key configured
 * - Database credentials configured
 */

pipeline {
    agent any

    tools {
        maven 'Maven 3.9'
    }

    parameters {
        string(
            name: 'IMAGE_TAG',
            defaultValue: '',
            description: 'Docker image tag (leave empty to use build number)'
        )
        choice(
            name: 'DEPLOY_ENV',
            choices: ['development', 'production'],
            description: 'Environment to deploy'
        )
    }

    environment {
        // ==========================================
        // DOCKER IN DOCKER (DIND) CONFIGURATION
        // ==========================================
        DOCKER_HOST = 'tcp://docker:2376'
//         DOCKER_CERT_PATH = '/certs/client'
//         DOCKER_TLS_VERIFY = '1'
        // ==========================================

        // Docker Hub Configuration
        DOCKER_HUB_CREDENTIALS = credentials('docker-hub')
        DOCKER_REPO = 'ardidafa' // TODO: Change to your Docker Hub username
        IMAGE_NAME_DEV = 'week8-practice1-dev'
        IMAGE_NAME_PROD = 'week8-practice1-prod'

        // Heroku Configuration
        HEROKU_API_KEY = credentials('HEROKU_API_KEY')
        HEROKU_APP_NAME_DEV = 'week8-practice1-dev'
        HEROKU_APP_NAME_PROD = 'week8-practice1-prod'

        // Database Configuration
        DATABASE_URL = credentials('db-url')
        DATABASE_USERNAME = credentials('db-username')
        DATABASE_PASSWORD = credentials('db-password')
        DATABASE_DRIVER = credentials('db-driver')

        // Application
        APP_NAME = 'week8-practice1'
        IMAGE_TAG = "${params.IMAGE_TAG ? params.IMAGE_TAG : env.BUILD_NUMBER}"

        // Spring Profile (auto-detected from branch)
        SPRING_PROFILE = "${params.DEPLOY_ENV == 'production' ? 'prod' : 'dev'}"
    }

    stages {
        // ============================================================
        // STAGE 1: Checkout
        // ============================================================
        stage('Checkout') {
            steps {
                cleanWs()
                checkout scm

                script {
                    // Extract Git info
                    env.GIT_BRANCH = sh(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()

                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    // Auto-detect environment from branch
                    // Remove 'origin/' prefix if present
                    def cleanBranch = env.GIT_BRANCH.replace('origin/', '')

                    if (cleanBranch == 'main' || cleanBranch == 'master') {
                        env.SPRING_PROFILE = 'prod'
                        env.DEPLOY_ENV = 'production'
                        env.DEPLOY_APP_NAME = HEROKU_APP_NAME_PROD
                        env.IMAGE_NAME = IMAGE_NAME_PROD
                        echo "🚀 Branch: ${cleanBranch} → PRODUCTION"
                    } else if (cleanBranch == 'develop') {
                        env.SPRING_PROFILE = 'dev'
                        env.DEPLOY_ENV = 'development'
                        env.DEPLOY_APP_NAME = HEROKU_APP_NAME_DEV
                        env.IMAGE_NAME = IMAGE_NAME_DEV
                        echo "🔧 Branch: ${cleanBranch} → DEVELOPMENT"
                    } else {
                        // Use parameter for other branches
                        if (params.DEPLOY_ENV == 'production') {
                            env.DEPLOY_APP_NAME = HEROKU_APP_NAME_PROD
                            env.IMAGE_NAME = IMAGE_NAME_PROD
                        } else {
                            env.DEPLOY_APP_NAME = HEROKU_APP_NAME_DEV
                            env.IMAGE_NAME = IMAGE_NAME_DEV
                        }
                        echo "ℹ️ Branch: ${cleanBranch} → ${env.DEPLOY_ENV}"
                    }

                    echo "Spring Profile: ${env.SPRING_PROFILE}"
                    echo "Heroku App: ${DEPLOY_APP_NAME}"
                    echo "Docker Image: ${DOCKER_REPO}/${IMAGE_NAME}"
                }
            }
        }

        // ============================================================
        // STAGE 2: Build with Maven (Unit Tests MANDATORY)
        // ============================================================
        stage('Build') {
            steps {
                script {
                    echo "🔨 Building Spring Boot Application..."
                    echo "⚠️ Unit tests are MANDATORY. Build will FAIL if tests fail."
                    echo "ℹ️ Integration tests skipped for faster CI/CD"
                    echo "ℹ️ Unit tests use Mockito (NO database)"

                    withEnv([
                        "SPRING_PROFILES_ACTIVE=unit-test",
                        "MAVEN_OPTS=-Xmx256m -XX:MaxMetaspaceSize=128m"
                    ]) {
                        // Build with tests - NO SKIP, NO FALLBACK
                        // Tests MUST pass for build to succeed
                        timeout(time: 30, unit: 'MINUTES') {
                            sh '''
                                echo "🔍 Building with UNIT TESTS (mandatory)..."
                                echo ""

                                # Build with tests running
                                # If tests fail, this entire build FAILS
                                mvn clean package \
                                    -DskipITs \
                                    -Djacoco.skip=true \
                                    -Dspring.profiles.active=unit-test \
                                    -Dmaven.test.failure.ignore=false \
                                    -B
                            '''
                        }
                    }

                    // Verify JAR was created
                    sh '''
                        if [ -f target/Week8_Practice1-0.0.1-SNAPSHOT.jar ]; then
                            echo "✅ Build successful - JAR file created"
                            ls -lh target/Week8_Practice1-0.0.1-SNAPSHOT.jar
                        else
                            echo "❌ Build failed - JAR file not found"
                            exit 1
                        fi
                    '''
                }
            }
        }

        // ============================================================
        // STAGE 3: Verify Unit Tests Results
        // ============================================================
        stage('Verify Tests') {
            steps {
                script {
                    echo "🧪 Verifying test results..."

                    // Publish test results - FAIL BUILD if tests failed
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false

                    echo "✅ All unit tests passed! Build can proceed."
                }
            }
        }

        // ============================================================
        // STAGE 4: Verify Docker is Available
        // ============================================================
        stage('Verify Docker') {
            steps {
                script {
                    echo "🐳 Verifying Docker is available..."

                    sh '''
                        # Pengecekan posix standard yang sudah kamu perbaiki
                        if ! command -v docker > /dev/null 2>&1; then
                            echo "❌ Docker is NOT installed on this Jenkins agent"
                            echo ""
                            echo "PREREQUISITE: Docker must be pre-installed on Jenkins agent"
                            exit 1
                        fi

                        docker --version
                        echo "✅ Docker is available!"
                    '''
                }
            }
        }

        // ============================================================
        // STAGE 5: Build Docker Image
        // ============================================================
        stage('Build Docker Image') {
            steps {
                script {
                    echo "🐳 Building Docker image..."

                    // Get Docker Hub username from credentials
                    def dockerHubUsername = DOCKER_HUB_CREDENTIALS.split(':')[0]

                    // Full image name
                    def fullImageName = "${dockerHubUsername}/${IMAGE_NAME}"

                    // Build with metadata
                    sh """
                        echo "Building Docker image: ${fullImageName}:${IMAGE_TAG}"
                        echo "Spring Profile: ${env.SPRING_PROFILE}"
                        echo ""

                        # Build image
                        docker build \
                            -t ${fullImageName}:${IMAGE_TAG} \
                            -t ${fullImageName}:latest \
                            --build-arg SPRING_PROFILES_ACTIVE=${env.SPRING_PROFILE} \
                            --build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
                            --build-arg VCS_REF=${env.GIT_COMMIT_SHORT} \
                            --build-arg VERSION=${IMAGE_TAG} \
                            .

                        echo ""
                        echo "✅ Docker image built successfully!"
                        docker images ${fullImageName} --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}'
                    """

                    // Store for next stages
                    env.DOCKER_IMAGE = fullImageName

                    echo "✅ Docker image: ${fullImageName}:${IMAGE_TAG}"
                }
            }
        }

        // ============================================================
        // STAGE 6: Push to Docker Hub
        // ============================================================
        stage('Push to Docker Hub') {
            steps {
                script {
                    echo "📤 Pushing Docker image to Docker Hub..."

                    sh """
                        # Login to Docker Hub
                        echo "${DOCKER_HUB_CREDENTIALS}" | docker login --username-password-stdin

                        # Push both tagged images
                        echo "Pushing ${DOCKER_IMAGE}:${IMAGE_TAG}..."
                        docker push ${DOCKER_IMAGE}:${IMAGE_TAG}

                        echo "Pushing ${DOCKER_IMAGE}:latest..."
                        docker push ${DOCKER_IMAGE}:latest

                        echo ""
                        echo "✅ Docker image pushed to Docker Hub!"
                        echo "Image: ${DOCKER_IMAGE}:${IMAGE_TAG}"
                    """
                }
            }
        }

        // ============================================================
        // STAGE 7: Deploy to Heroku
        // ============================================================
        stage('Deploy to Heroku') {
            steps {
                script {
                    echo "🚀 Deploying to Heroku: ${DEPLOY_APP_NAME}"

                    // Install Heroku CLI
                    sh '''
                        cd ${WORKSPACE}
                        rm -rf heroku heroku.tar.gz

                        ARCH=$(uname -m)
                        if [ "$ARCH" = "x86_64" ]; then
                            DOWNLOAD_URL="https://cli-assets.heroku.com/heroku-linux-x64.tar.gz"
                        elif [ "$ARCH" = "aarch64" ] || [ "$ARCH" = "arm64" ]; then
                            DOWNLOAD_URL="https://cli-assets.heroku.com/heroku-linux-arm64.tar.gz"
                        else
                            echo "❌ Unsupported architecture: $ARCH"
                            exit 1
                        fi

                        curl -fsSL "$DOWNLOAD_URL" -o heroku.tar.gz
                        tar -xzf heroku.tar.gz
                        rm heroku.tar.gz
                    '''

                    env.PATH = "${env.WORKSPACE}/heroku/bin:${env.PATH}"

                    // Authenticate
                    withEnv(["HEROKU_API_KEY=${HEROKU_API_KEY}"]) {
                        sh '''
                            echo "${HEROKU_API_KEY}" | heroku auth:token
                            heroku auth:whoami
                        '''
                    }

                    // Create app if not exists
                    sh """
                        if ! heroku apps:info --app "${DEPLOY_APP_NAME}" 2>/dev/null; then
                            echo "Creating Heroku app: ${DEPLOY_APP_NAME}"
                            heroku create "${DEPLOY_APP_NAME}"
                        fi
                    """

                    // Set environment variables
                    sh """
                        heroku config:set SPRING_PROFILES_ACTIVE="${env.SPRING_PROFILE}" --app "${DEPLOY_APP_NAME}"

                        heroku config:set SPRING_DATASOURCE_URL="${DATABASE_URL}" --app "${DEPLOY_APP_NAME}"
                        heroku config:set SPRING_DATASOURCE_USERNAME="${DATABASE_USERNAME}" --app "${DEPLOY_APP_NAME}"
                        heroku config:set SPRING_DATASOURCE_PASSWORD="${DATABASE_PASSWORD}" --app "${DEPLOY_APP_NAME}"
                        heroku config:set SPRING_DATASOURCE_DRIVER_CLASS_NAME="${DATABASE_DRIVER}" --app "${DEPLOY_APP_NAME}"
                    """

                    // Deploy using Heroku Container Registry
                    // Pull from Docker Hub, tag for Heroku, push to Heroku, release
                    sh """
                        echo "📤 Deploying Docker image to Heroku..."
                        echo "Source: ${DOCKER_IMAGE}:${IMAGE_TAG}"
                        echo "Target: registry.heroku.com/${DEPLOY_APP_NAME}/web"
                        echo ""

                        # Pull image from Docker Hub
                        docker pull ${DOCKER_IMAGE}:${IMAGE_TAG}

                        # Tag for Heroku Container Registry
                        docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} registry.heroku.com/${DEPLOY_APP_NAME}/web

                        # Login to Heroku Container Registry
                        echo "${HEROKU_API_KEY}" | docker login --username=_ --password-stdin registry.heroku.com

                        # Push to Heroku Container Registry
                        docker push registry.heroku.com/${DEPLOY_APP_NAME}/web

                        # Release the container
                        heroku container:release web --app "${DEPLOY_APP_NAME}"

                        echo ""
                        echo "✅ Deployed to Heroku successfully!"
                    """
                }
            }
        }

        // ============================================================
        // STAGE 8: Health Check
        // ============================================================
        stage('Health Check') {
            steps {
                script {
                    echo "🏥 Checking application health..."

                    // Get app URL
                    def appUrl = sh(
                        script: """
                            heroku apps:info --app ${DEPLOY_APP_NAME} --json | grep -o '"web_url":"[^"]*"' | cut -d'"' -f4
                        """,
                        returnStdout: true
                    ).trim()

                    env.APP_URL = appUrl

                    // Wait for startup
                    echo "⏳ Waiting for application to start..."
                    sleep time: 60, unit: 'SECONDS'

                    // Health check
                    withEnv(["APP_URL=${appUrl}"]) {
                        sh """
                            set +e
                            MAX_RETRIES=10
                            RETRY_COUNT=0

                            while [ \${RETRY_COUNT} -lt \${MAX_RETRIES} ]; do
                                echo "Health check attempt \$((RETRY_COUNT + 1))"

                                HTTP_CODE=\$(curl -s -o /dev/null -w "%{http_code}" \${APP_URL}/actuator/health || echo "000")

                                if [ "\${HTTP_CODE}" = "200" ]; then
                                    echo "✅ Health check passed!"
                                    curl -s \${APP_URL}/actuator/health | jq . || true
                                    exit 0
                                fi

                                echo "⏳ Waiting... (HTTP: \${HTTP_CODE})"
                                sleep 10
                                RETRY_COUNT=\$((RETRY_COUNT + 1))
                            done

                            echo "❌ Health check failed"
                            exit 1
                        """
                    }
                }
            }
        }
    }

    // ============================================================
    // POST ACTIONS
    // ============================================================
    post {
        success {
            script {
                echo "✅ Pipeline SUCCESS!"

                try {
                    withCredentials([string(credentialsId: 'discord-notification', variable: 'DISCORD_WEBHOOK')]) {
                        discordSend(
                            webhookURL: DISCORD_WEBHOOK,
                            title: "✅ Deployment Success",
                            description: """**Environment:** `${env.DEPLOY_ENV}` (${env.SPRING_PROFILE})
**Branch:** `${env.GIT_BRANCH}`
**Build:** `#${env.BUILD_NUMBER}`
**Docker Image:** `${DOCKER_IMAGE}:${IMAGE_TAG}`
**Heroku App:** `${DEPLOY_APP_NAME}`

**✅ All tests passed**

**🔗 [App](${env.APP_URL}) | [Health](${env.APP_URL}/actuator/health)**""",
                            result: 'SUCCESS'
                        )
                    }
                } catch (Exception e) {
                    echo "Discord notification skipped: ${e.message}"
                }
            }
        }

        failure {
            script {
                echo "❌ Pipeline FAILED!"

                try {
                    withCredentials([string(credentialsId: 'discord-notification', variable: 'DISCORD_WEBHOOK')]) {
                        discordSend(
                            webhookURL: DISCORD_WEBHOOK,
                            title: "❌ Deployment Failed",
                            description: """**Environment:** `${env.DEPLOY_ENV}`
**Branch:** `${env.GIT_BRANCH}`
**Build:** `#${env.BUILD_NUMBER}`

**❌ Check test results or build logs**

**🔗 [Console Output](${env.BUILD_URL}console)**""",
                            result: 'FAILURE'
                        )
                    }
                } catch (Exception e) {
                    echo "Discord notification skipped: ${e.message}"
                }
            }
        }

        always {
            // Cleanup Docker images (diperbaiki agar aman dari error)
            sh '''
                if command -v docker > /dev/null 2>&1; then
                    docker system prune -f || true
                fi
            '''
            echo "Pipeline completed"
        }
    }
}