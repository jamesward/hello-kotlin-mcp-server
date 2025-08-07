import aws_cdk

from aws_cdk import (
    aws_cognito
)

from constructs import Construct

from buildpack_image_asset import BuildpackImageAsset
from bedrock_agentcore_runtime import BedrockAgentCoreRuntime


class BedrockAgentCoreStack(aws_cdk.Stack):
    def __init__(self, scope: Construct, construct_id: str, **kwargs):
        super().__init__(scope, construct_id, **kwargs)

        # user_pool = aws_cognito.UserPool(self, "UserPool",
        #                                  password_policy=aws_cognito.PasswordPolicy(
        #                                      min_length=8
        #                                  ),
        #                                  removal_policy=aws_cdk.RemovalPolicy.DESTROY
        #                                  )
        #
        # user_pool_client = aws_cognito.UserPoolClient(self, "UserPoolClient",
        #                                               user_pool=user_pool,
        #                                               generate_secret=False,
        #                                               auth_flows=aws_cognito.AuthFlow(
        #                                                   user_password=True,
        #                                               ),
        #                                               )

        server_image = BuildpackImageAsset(self, "ServerImage",
                                                    source_path="../",
                                                    builder="public.ecr.aws/heroku/builder:24",
                                                    run_image="public.ecr.aws/heroku/heroku:24",
                                                    platform="linux/amd64",
                                                    default_process="web",
                                                    )

        server = BedrockAgentCoreRuntime(self, "ServerAgentCore",
                                                  repository=server_image.ecr_repo,
                                                  protocol="MCP",
                                                  # discovery_url=f"https://cognito-idp.{self.region}.amazonaws.com/{user_pool.user_pool_id}/.well-known/openid-configuration",
                                                  # client_id=user_pool_client.user_pool_client_id
                                                  )

        server.node.add_dependency(server_image)


        # aws_cdk.CfnOutput(self, "PoolId", value=user_pool.user_pool_id)
        # aws_cdk.CfnOutput(self, "ClientId", value=user_pool_client.user_pool_client_id)

        aws_cdk.CfnOutput(self, "HelloKotlinMCPServerAgentRuntimeArn", value=server.resource.ref)


app = aws_cdk.App()

BedrockAgentCoreStack(app, "HelloKotlinMCPServer")

app.synth()
