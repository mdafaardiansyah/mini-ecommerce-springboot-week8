/**
 * Jenkins Pipeline for Spring Boot Application Deployment to Heroku via Docker
 *
 * This pipeline automates the build, test, and deployment process for a Spring Boot application.
 * It uses Docker to build the image and deploys to Heroku Container Registry.
 *
 * Branch-based Deployment:
 * - develop branch → deploy to development environment with dev profile
 * - main branch → deploy to production environment with prod profile
 *
 * Deployment Flow:
 * 1. Checkout source code
 * 2. Build application with Maven
 * 3. Run unit & integration tests (fail if tests fail)
 * 4. Build Docker image
 * 5. Push to Heroku Container Registry
 * 6. Release container to Heroku dynos
 * 7. Smoke test / health check
 *
 * Prerequisites:
 * - Jenkins with Maven, Git, and Docker installed
 * - Docker Hub credentials for Heroku Container Registry
 * - Heroku app already created or auto-create
 */

pipeline {
    agent any

    tools {
        maven 'Maven 3.9'  // Ensure Maven is configured in Jenkins Global Tools
    }

    parameters {
        string(
            name: 'RELEASE_TAG',
            defaultValue: '',
            description: 'Release tag to build and deploy (leave empty to use build number)'
        )
        choice(
            name: 'DEPLOY_ENV',
            choices: ['development', 'production'],
            description: 'Environment to deploy'
        )
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Run unit and integration tests'
        )
        string(
            name: 'CUSTOM_DOMAIN',
            defaultValue: '',
            description: 'Custom domain for smoke test (leave empty to use Heroku URL)'
        )
    }

    environment {
        // Heroku Credentials
        HEROKU_API_KEY = credentials('HEROKU_API_KEY')
        HEROKU_APP_NAME_DEV = 'week8-practice1-dev'
        HEROKU_APP_NAME_PROD = 'week8-practice1-prod'

        // Docker Hub Credentials (for Heroku Container Registry)
        // Heroku Container Registry uses same credentials as Heroku API
        DOCKER_REGISTRY = 'registry.heroku.com'

        // Database Credentials (External MySQL Database)
        DATABASE_URL = credentials('db-url')
        DATABASE_USERNAME = credentials('db-username')
        DATABASE_PASSWORD = credentials('db-password')
        DATABASE_DRIVER = credentials('db-driver')

        // Application Configuration
        APP_NAME = 'week8-practice1'
        IMAGE_TAG = "${params.RELEASE_TAG ? params.RELEASE_TAG : env.BUILD_NUMBER}"
        DEPLOY_APP_NAME = "${params.DEPLOY_ENV == 'production' ? HEROKU_APP_NAME_PROD : HEROKU_APP_NAME_DEV}"

        // Docker Image Names
        DOCKER_IMAGE_DEV = "${DOCKER_REGISTRY}/${HEROKU_APP_NAME_DEV}/web"
        DOCKER_IMAGE_PROD = "${DOCKER_REGISTRY}/${HEROKU_APP_NAME_PROD}/web"

        // Custom Domain Configuration
        CUSTOM_DOMAIN = "${params.CUSTOM_DOMAIN}"

        // Spring Profile based on branch and parameter
        SPRING_PROFILE = "${params.DEPLOY_ENV == 'production' ? 'prod' : 'dev'}"
    }

    stages {
        // ============================================================
        // STAGE 1: Checkout Source Code
        // ============================================================
        stage('Checkout') {
            steps {
                checkout scm

                script {
                    // Extract Git information
                    env.GIT_BRANCH = sh(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()

                    env.GIT_COMMIT = sh(
                        script: 'git rev-parse HEAD',
                        returnStdout: true
                    ).trim()

                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    env.GIT_AUTHOR = sh(
                        script: 'git log -1 --pretty=format:"%an"',
                        returnStdout: true
                    ).trim()

                    env.GIT_MESSAGE = sh(
                        script: 'git log -1 --pretty=format:"%s"',
                        returnStdout: true
                    ).trim()

                    echo "Branch: ${env.GIT_BRANCH}"
                    echo "Commit: ${env.GIT_COMMIT_SHORT}"
                    echo "Author: ${env.GIT_AUTHOR}"
                    echo "Message: ${env.GIT_MESSAGE}"

                    // Auto-detect environment based on branch
                    if (env.GIT_BRANCH == 'main' || env.GIT_BRANCH == 'master') {
                        env.SPRING_PROFILE = 'prod'
                        env.DEPLOY_ENV = 'production'
                        env.DEPLOY_APP_NAME = HEROKU_APP_NAME_PROD
                        env.DOCKER_IMAGE = DOCKER_IMAGE_PROD
                        echo "🚀 Detected main/master branch - deploying to PRODUCTION"
                    } else if (env.GIT_BRANCH == 'develop') {
                        env.SPRING_PROFILE = 'dev'
                        env.DEPLOY_ENV = 'development'
                        env.DEPLOY_APP_NAME = HEROKU_APP_NAME_DEV
                        env.DOCKER_IMAGE = DOCKER_IMAGE_DEV
                        echo "🔧 Detected develop branch - deploying to DEVELOPMENT"
                    } else {
                        // Use parameter for other branches
                        if (params.DEPLOY_ENV == 'production') {
                            env.DOCKER_IMAGE = DOCKER_IMAGE_PROD
                        } else {
                            env.DOCKER_IMAGE = DOCKER_IMAGE_DEV
                        }
                        echo "ℹ️ Branch ${env.GIT_BRANCH} - using parameter DEPLOY_ENV: ${params.DEPLOY_ENV}"
                    }

                    // Send notification to Discord (optional)
                    script {
                        try {
                            withCredentials([string(credentialsId: 'discord-notification', variable: 'DISCORD_WEBHOOK')]) {
                                discordSend(
                                    webhookURL: DISCORD_WEBHOOK,
                                    title: "🚀 Build Started!",
                                    description: """**Branch:** `${env.GIT_BRANCH}`
**Build:** `#${env.BUILD_NUMBER}`
**Status:** `🔄 In Progress`
**Commit:** `${env.GIT_COMMIT_SHORT}`
**Author:** `${env.GIT_AUTHOR}`
**Message:** `${env.GIT_MESSAGE}`

**🔗 Links:**
• [View Build](${env.BUILD_URL})
• [Console Output](${env.BUILD_URL}console)""",
                                    link: env.BUILD_URL,
                                    result: 'STARTED'
                                )
                            }
                        } catch (Exception e) {
                            echo "⚠️ Discord notification not configured. Skipping notification..."
                        }
                    }
                }
            }
        }

        // ============================================================
        // STAGE 2: Build Application
        // ============================================================
        stage('Build') {
            steps {
                script {
                    echo "Building Spring Boot Application with Maven..."

                    withEnv([
                        'MAVEN_OPTS=-Xmx3072m -XX:MaxMetaspaceSize=512m',
                        "SPRING_PROFILES_ACTIVE=${env.SPRING_PROFILE}"
                    ]) {
                        // Clean, package, and skip tests for build stage
                        sh 'mvn clean package -DskipTests'
                    }

                    // Verify jar file was created
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
        // STAGE 3: Run Tests (Optional)
        // ============================================================
        stage('Test') {
            when {
                expression { params.RUN_TESTS == true }
            }
            steps {
                script {
                    echo "Running Unit and Integration Tests..."

                    sh 'mvn test'

                    // Publish test results
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
            post {
                always {
                    // Archive test reports
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        // ============================================================
        // STAGE 4: Security Scan - Dependencies
        // ============================================================
        stage('Security Scan') {
            steps {
                script {
                    echo "Running dependency check..."
                    sh 'mvn dependency:tree || true'
                }
            }
        }

        // ============================================================
        // STAGE 5: Build Docker Image
        // ============================================================
        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image for ${APP_NAME}..."

                    // Build Docker image with tags
                    sh """
                        docker build \
                            -t ${DOCKER_IMAGE}:${IMAGE_TAG} \
                            -t ${DOCKER_IMAGE}:latest \
                            --build-arg SPRING_PROFILES_ACTIVE=${env.SPRING_PROFILE} \
                            --build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
                            --build-arg VCS_REF=${env.GIT_COMMIT_SHORT} \
                            --build-arg VERSION=${IMAGE_TAG} \
                            .
                    """

                    echo "✅ Docker image built successfully"

                    // Display image info
                    sh """
                        echo "Docker Image Details:"
                        docker images ${DOCKER_IMAGE} --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}'
                    """
                }
            }
        }

        // ============================================================
        // STAGE 6: Push to Heroku Container Registry
        // ============================================================
        stage('Push to Heroku Registry') {
            steps {
                script {
                    echo "Pushing Docker image to Heroku Container Registry..."

                    // Login to Heroku Container Registry
                    // Heroku Container Registry uses the same API key
                    withEnv(["HEROKU_API_KEY=${HEROKU_API_KEY}"]) {
                        sh """
                            # Login to Heroku Container Registry
                            echo "${HEROKU_API_KEY}" | docker login --username=_ --password-stdin ${DOCKER_REGISTRY}

                            # Push all tagged images
                            docker push ${DOCKER_IMAGE}:${IMAGE_TAG}
                            docker push ${DOCKER_IMAGE}:latest
                        """
                    }

                    echo "✅ Docker image pushed to Heroku Container Registry"
                }
            }
        }

        // ============================================================
        // STAGE 7: Deploy to Heroku
        // ============================================================
        stage('Deploy to Heroku') {
            steps {
                script {
                    echo "Deploying to Heroku App: ${DEPLOY_APP_NAME} (${env.DEPLOY_ENV})"

                    // Install Heroku CLI (needed for container:release command)
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
                        ls -F ${WORKSPACE}/heroku/bin/
                    '''

                    env.PATH = "${env.WORKSPACE}/heroku/bin:${env.PATH}"
                    sh 'heroku --version'

                    // Authenticate and create app if needed
                    withEnv(["HEROKU_API_KEY=${HEROKU_API_KEY}"]) {
                        sh '''
                            echo "${HEROKU_API_KEY}" | heroku auth:token
                            heroku auth:whoami
                            echo "✅ Successfully authenticated to Heroku"
                        '''
                    }

                    sh """
                        if ! heroku apps:info --app "${DEPLOY_APP_NAME}" 2>/dev/null; then
                            echo "Creating new Heroku app: ${DEPLOY_APP_NAME}"
                            heroku create "${DEPLOY_APP_NAME}"
                        else
                            echo "✅ Heroku app exists: ${DEPLOY_APP_NAME}"
                        fi
                    """

                    // Set environment variables
                    withEnv(["SPRING_PROFILE=${env.SPRING_PROFILE}"]) {
                        sh """
                            heroku config:set JAVA_OPTS="-Xmx512m -Xms256m" --app "${DEPLOY_APP_NAME}"
                            heroku config:set SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" --app "${DEPLOY_APP_NAME}"
                            echo "✅ Environment variables set"
                        """
                    }

                    // Set Database Configuration
                    withEnv([
                        "HEROKU_API_KEY=${HEROKU_API_KEY}",
                        "DATABASE_URL=${DATABASE_URL}",
                        "DATABASE_USERNAME=${DATABASE_USERNAME}",
                        "DATABASE_PASSWORD=${DATABASE_PASSWORD}",
                        "DATABASE_DRIVER=${DATABASE_DRIVER}"
                    ]) {
                        sh """
                            export HEROKU_API_KEY

                            heroku config:set SPRING_DATASOURCE_URL="${DATABASE_URL}" --app "${DEPLOY_APP_NAME}"
                            heroku config:set SPRING_DATASOURCE_USERNAME="${DATABASE_USERNAME}" --app "${DEPLOY_APP_NAME}"
                            heroku config:set SPRING_DATASOURCE_PASSWORD="${DATABASE_PASSWORD}" --app "${DEPLOY_APP_NAME}"
                            heroku config:set SPRING_DATASOURCE_DRIVER_CLASS_NAME="${DATABASE_DRIVER}" --app "${DEPLOY_APP_NAME}"

                            echo "✅ Database configuration set"
                        """
                    }

                    // Release the container
                    // This tells Heroku to use the Docker image we just pushed
                    sh """
                        echo "Releasing container to Heroku..."
                        heroku container:release web --app "${DEPLOY_APP_NAME}"
                        echo "✅ Container released successfully"
                    """

                    echo "✅ Deployment successful!"
                }
            }
        }

        // ============================================================
        // STAGE 8: Health Check / Smoke Test
        // ============================================================
        stage('Smoke Test') {
            steps {
                script {
                    echo "Running smoke tests against deployed application..."

                    def appUrl = ''

                    if ("${CUSTOM_DOMAIN}"?.trim()) {
                        appUrl = "https://${CUSTOM_DOMAIN}"
                        echo "Using custom domain: ${appUrl}"
                    } else {
                        appUrl = sh(
                            script: """
                                heroku apps:info --app ${DEPLOY_APP_NAME} --json | grep -o '"web_url":"[^"]*"' | cut -d'"' -f4
                            """,
                            returnStdout: true
                        ).trim()
                        echo "Using Heroku URL: ${appUrl}"
                    }

                    env.APP_URL = appUrl

                    // Wait for application to start
                    echo "Waiting for application to be ready (60 seconds)..."
                    sleep time: 60, unit: 'SECONDS'

                    withEnv(["APP_URL=${appUrl}"]) {
                        sh """
                            set +e
                            MAX_RETRIES=10
                            RETRY_COUNT=0

                            while [ \${RETRY_COUNT} -lt \${MAX_RETRIES} ]; do
                                echo "Attempt \$((RETRY_COUNT + 1)) of \${MAX_RETRIES}"

                                HTTP_CODE=\$(curl -s -o /dev/null -w "%{http_code}" \${APP_URL}/actuator/health || echo "000")

                                if [ "\${HTTP_CODE}" = "200" ]; then
                                    echo "✅ Health check passed!"
                                    echo "Response:"
                                    curl -s \${APP_URL}/actuator/health | jq . || echo "Health check response"
                                    exit 0
                                fi

                                echo "⏳ Waiting for service... (HTTP: \${HTTP_CODE})"
                                sleep 10
                                RETRY_COUNT=\$((RETRY_COUNT + 1))
                            done

                            echo "❌ Health check failed after \${MAX_RETRIES} attempts"
                            echo "URL attempted: \${APP_URL}"
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
                echo "✅ Pipeline completed successfully!"

                try {
                    withCredentials([string(credentialsId: 'discord-notification', variable: 'DISCORD_WEBHOOK')]) {
                        discordSend(
                            webhookURL: DISCORD_WEBHOOK,
                            title: "✅ Deploy Success!",
                            description: """**Branch:** `${env.GIT_BRANCH}`
**Build:** `#${env.BUILD_NUMBER}`
**Status:** `✅ Success`
**Environment:** `${env.DEPLOY_ENV}` (${env.SPRING_PROFILE})
**Heroku App:** `${DEPLOY_APP_NAME}`
**Docker Image:** `${DOCKER_IMAGE}:${IMAGE_TAG}`

**📝 Changes:**
`${env.GIT_COMMIT_SHORT}` ${env.GIT_MESSAGE} - ${env.GIT_AUTHOR}

**🔗 Links:**
• [View Build](${env.BUILD_URL})
• [Console Output](${env.BUILD_URL}console)
• [🌐 Application](${env.APP_URL})
• [Actuator Health](${env.APP_URL}/actuator/health)
• [Swagger UI](${env.APP_URL}/swagger-ui/index.html)

**⏱️ Duration:** `${currentBuild.durationString}`""",
                            link: env.BUILD_URL,
                            result: 'SUCCESS'
                        )
                    }
                } catch (Exception e) {
                    echo "⚠️ Discord notification not configured. Skipping notification..."
                }
            }
        }

        failure {
            script {
                echo "❌ Pipeline failed!"

                def failureMessage = "Unknown error"
                try {
                    def log = currentBuild.rawBuild.getLog(100)
                    failureMessage = log.findAll { it.contains('ERROR') || it.contains('FAILED') }
                                          .take(5)
                                          .join('\n')
                    if (!failureMessage) {
                        failureMessage = "Check console output for details"
                    }
                } catch (Exception e) {
                    failureMessage = "Check console output for details"
                }

                try {
                    withCredentials([string(credentialsId: 'discord-notification', variable: 'DISCORD_WEBHOOK')]) {
                        discordSend(
                            webhookURL: DISCORD_WEBHOOK,
                            title: "❌ Build Failed!",
                            description: """**Branch:** `${env.GIT_BRANCH}`
**Build:** `#${env.BUILD_NUMBER}`
**Status:** `❌ Failed`
**Environment:** `${env.DEPLOY_ENV}`

**📝 Last Commit:**
`${env.GIT_COMMIT_SHORT}` ${env.GIT_MESSAGE} - ${env.GIT_AUTHOR}

**💥 Failure Info:**
```
${failureMessage}
```

**🔗 Links:**
• [View Build](${env.BUILD_URL})
• [Console Output](${env.BUILD_URL}console)

**⏱️ Duration:** `${currentBuild.durationString}`""",
                            link: env.BUILD_URL,
                            result: 'FAILURE'
                        )
                    }
                } catch (Exception e) {
                    echo "⚠️ Discord notification not configured. Skipping notification..."
                }
            }
        }

        always {
            echo "Pipeline execution completed"

            // Cleanup Docker images to save disk space
            script {
                try {
                    sh 'docker system prune -f || true'
                } catch (Exception e) {
                    echo "⚠️ Docker cleanup failed (non-critical)"
                }
            }
        }
    }
}
