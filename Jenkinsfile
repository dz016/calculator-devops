// Declarative CI/CD pipeline: checkout -> build -> test -> docker build -> push -> deploy.
pipeline {
    agent any

    triggers {
        githubPush()                 // GitHub webhook (via ngrok) - preferred
        pollSCM('H/2 * * * *')       // fallback if the webhook can't reach Jenkins
    }

    options {
        disableConcurrentBuilds()    // two deploys must never race each other
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        // Homebrew-launched Jenkins on macOS doesn't inherit the login shell PATH.
        JAVA_HOME = '/opt/homebrew/opt/openjdk@21'
        PATH = "/opt/homebrew/opt/openjdk@21/bin:/usr/local/bin:/opt/homebrew/bin:/Applications/Docker.app/Contents/Resources/bin:${env.PATH}"

        // Username + token from the Jenkins Credentials store; exposes _USR and _PSW.
        DOCKERHUB_CREDS = credentials('dockerhub-creds')
        DOCKER_IMAGE = "${DOCKERHUB_CREDS_USR}/calculator"
        IMAGE_TAG = "${BUILD_NUMBER}"
        APP_URL = 'http://localhost:8088'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw -B clean package -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t "$DOCKER_IMAGE:$IMAGE_TAG" -t "$DOCKER_IMAGE:latest" .'
            }
        }

        stage('Push') {
            steps {
                // Single quotes: the shell expands the secret, not Groovy, so it never lands in the build log.
                sh '''
                    echo "$DOCKERHUB_CREDS_PSW" | docker login -u "$DOCKERHUB_CREDS_USR" --password-stdin
                    docker push "$DOCKER_IMAGE:$IMAGE_TAG"
                    docker push "$DOCKER_IMAGE:latest"
                '''
            }
            post {
                always {
                    sh 'docker logout || true'
                }
            }
        }

        stage('Deploy') {
            steps {
                // Fixed project name + `up -d` = idempotent: only containers whose image/config changed are recreated.
                sh 'docker compose up -d --wait --remove-orphans'
                sh '''
                    for i in $(seq 1 20); do
                        if curl -sf "$APP_URL/actuator/health" | grep -q UP; then
                            echo "Health check passed"
                            curl -s "$APP_URL/api/v1/add?a=2&b=3"; echo
                            exit 0
                        fi
                        sleep 3
                    done
                    echo "Deployment did not become healthy"; docker compose ps; exit 1
                '''
                sh 'docker compose ps'
            }
        }
    }

    post {
        // Recipient comes from "Default Recipients" in Jenkins' Extended E-mail config, so no address is committed.
        success {
            emailext(
                to: '$DEFAULT_RECIPIENTS',
                subject: "SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """<b>Build succeeded and was deployed.</b><br>
                         Job: ${env.JOB_NAME}<br>
                         Build: #${env.BUILD_NUMBER}<br>
                         Image: ${env.DOCKER_IMAGE}:${env.IMAGE_TAG}<br>
                         URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a>"""
            )
        }
        failure {
            emailext(
                to: '$DEFAULT_RECIPIENTS',
                subject: "FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                mimeType: 'text/html',
                body: """<b>Build failed.</b><br>
                         Job: ${env.JOB_NAME}<br>
                         Build: #${env.BUILD_NUMBER}<br>
                         Console: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a>"""
            )
        }
    }
}
