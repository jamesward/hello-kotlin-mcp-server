# Hello Kotlin MCP Server

## Run Locally

```
./gradlew run
```

## Deploy on AgentCore

> Note: Requires Node & uv - Will work on switching to CloudFormation or SAM

```
cd infra
npx aws-cdk bootstrap
npx aws-cdk deploy
```
