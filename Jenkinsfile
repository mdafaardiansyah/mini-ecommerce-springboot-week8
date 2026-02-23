/**
 * Jenkins Pipeline for Order Management API - Docker + Docker Hub + Heroku
 *
 * Deployment Strategy:
 * - develop branch → dev profile → week8-practice1-dev
 * - main branch → prod profile → week8-practice1-prod
 *
 * Deployment Flow:
 * 1. Checkout code
 * 2. Maven build (NO skip tests)
 * 3. Run unit tests (FAIL BUILD if tests fail)
 * 4. Build Docker image
 * 5. Push to Docker Hub
 * 6. Deploy to Heroku (pull from Docker Hub)
 * 7. Health check
 *
 * Prerequisites:
 * - Docker installed on Jenkins agent
 * - Docker Hub credentials (docker-hub)
 * - Heroku API key
 * - Database credentials
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
                    if (env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'master') {
                        env.SPRING_PROFILE = 'prod'
                        env.DEPLOY_ENV = 'production'
                        env.DEPLOY_APP_NAME = HEROKU_APP_NAME_PROD
                        env.IMAGE_NAME = IMAGE_NAME_PROD
                        echo "🚀 Branch: main → PRODUCTION"
                    } else if (env.GIT_BRANCH == 'develop') {
                        env.SPRING_PROFILE = 'dev'
                        env.DEPLOY_ENV = 'development'
                        env.DEPLOY_APP_NAME = HEROKU_APP_NAME_DEV
                        env.IMAGE_NAME = IMAGE_NAME_DEV
                        echo "🔧 Branch: develop → DEVELOPMENT"
                    } else {
                        // Use parameter for other branches
                        if (params.DEPLOY_ENV == 'production') {
                            env.DEPLOY_APP_NAME = HEROKU_APP_NAME_PROD
                            env.IMAGE_NAME = IMAGE_NAME_PROD
                        } else {
                            env.DEPLOY_APP_NAME = HEROKU_APP_NAME_DEV
                            env.IMAGE_NAME = IMAGE_NAME_DEV
                        }
                        echo "ℹ️ Branch: ${env.GIT_BRANCH} → ${env.DEPLOY_ENV}"
                    }

                    echo "Spring Profile: ${env.SPRING_PROFILE}"
                    echo "Heroku App: ${env.DEPLOY_APP_NAME}"
                    echo "Docker Image: ${DOCKER_REPO}/${IMAGE_NAME}"
                }
            }
        }

        // ============================================================
        // STAGE 2: Build with Maven (Tests NOT Skipped)
        // ============================================================
        stage('Build') {
            steps {
                script {
                    echo "🔨 Building Spring Boot Application..."
                    echo "⚠️ Unit tests will run. Build will FAIL if tests fail."

                    withEnv(["SPRING_PROFILES_ACTIVE=${env.SPRING_PROFILE}"]) {
                        // DO NOT skip tests
                        sh 'mvn clean package'
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
        // STAGE 3: Run Unit Tests (MANDATORY)
        // ============================================================
        stage('Unit Tests') {
            steps {
                script {
                    echo "🧪 Running Unit Tests..."

                    // Run tests and publish results
                    sh 'mvn test'

                    // Publish test results to Jenkins
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: false

                    echo "✅ All tests passed!"
                }
            }
        }

        // ============================================================
        // STAGE 4: Build Docker Image
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
                        docker build \
                            -t ${fullImageName}:${IMAGE_TAG} \
                            -t ${fullImageName}:latest \
                            --build-arg SPRING_PROFILES_ACTIVE=${env.SPRING_PROFILE} \
                            --build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
                            --build-arg VCS_REF=${env.GIT_COMMIT_SHORT} \
                            --build-arg VERSION=${IMAGE_TAG} \
                            .
                    """

                    // Store for next stages
                    env.DOCKER_IMAGE = fullImageName

                    echo "✅ Docker image built: ${fullImageName}:${IMAGE_TAG}"
                    sh "docker images ${fullImageName} --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}'"
                }
            }
        }

        // ============================================================
        // STAGE 5: Push to Docker Hub
        // ============================================================
        stage('Push to Docker Hub') {
            steps {
                script {
                    echo "📤 Pushing Docker image to Docker Hub..."

                    // Login to Docker Hub using credentials
                    // docker-hub credential format: username:password
                    sh """
                        echo "${DOCKER_HUB_CREDENTIALS}" | docker login --username-password-stdin
                    """

                    // Push both tagged images
                    sh """
                        docker push ${DOCKER_IMAGE}:${IMAGE_TAG}
                        docker push ${DOCKER_IMAGE}:latest
                    """

                    echo "✅ Docker image pushed to Docker Hub"
                    echo "Image: ${DOCKER_IMAGE}:${IMAGE_TAG}"
                }
            }
        }

        // ============================================================
        // STAGE 6: Deploy to Heroku
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

                    // Deploy by creating Heroku.yml that pulls from Docker Hub
                    // Or we can use Heroku Container Registry with image from Docker Hub
                    sh """
                        echo "Setting Heroku to use Docker image from Docker Hub..."

                        # Set Docker image location for Heroku
                        heroku config:set DOCKER_IMAGE="${DOCKER_IMAGE}:${IMAGE_TAG}" --app "${DEPLOY_APP_NAME}"

                        # Use Heroku Container Registry to deploy
                        # Pull image from Docker Hub, tag it for Heroku, push to Heroku Registry, release
                        docker pull ${DOCKER_IMAGE}:${IMAGE_TAG}
                        docker tag ${DOCKER_IMAGE}:${IMAGE_TAG} registry.heroku.com/${DEPLOY_APP_NAME}/web
                        echo "${HEROKU_API_KEY}" | docker login --username=_ --password-stdin registry.heroku.com
                        docker push registry.heroku.com/${DEPLOY_APP_NAME}/web
                        heroku container:release web --app "${DEPLOY_APP_NAME}"
                    """

                    echo "✅ Deployed to Heroku successfully"
                }
            }
        }

        // ============================================================
        // STAGE 7: Health Check
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
                    echo "Discord notification skipped"
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
                    echo "Discord notification skipped"
                }
            }
        }

        always {
            // Cleanup Docker images
            sh 'docker system prune -f || true'
            echo "Pipeline completed"
        }
    }
}
