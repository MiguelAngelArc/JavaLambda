terraform {
  backend "s3" {}
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.27.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  dynamic "endpoints" {
    for_each = terraform.workspace == "local" ? [1] : []
    content {
      apigateway     = "http://localhost:4566"
      apigatewayv2   = "http://localhost:4566"
      cloudformation = "http://localhost:4566"
      cloudwatch     = "http://localhost:4566"
      logs           = "http://localhost:4566"
      dynamodb       = "http://localhost:4566"
      ec2            = "http://localhost:4566"
      es             = "http://localhost:4566"
      elasticache    = "http://localhost:4566"
      firehose       = "http://localhost:4566"
      iam            = "http://localhost:4566"
      kinesis        = "http://localhost:4566"
      lambda         = "http://localhost:4566"
      rds            = "http://localhost:4566"
      redshift       = "http://localhost:4566"
      route53        = "http://localhost:4566"
      s3             = "http://s3.localhost.localstack.cloud:4566"
      secretsmanager = "http://localhost:4566"
      ses            = "http://localhost:4566"
      sns            = "http://localhost:4566"
      sqs            = "http://localhost:4566"
      ssm            = "http://localhost:4566"
      stepfunctions  = "http://localhost:4566"
      sts            = "http://localhost:4566"
    }
  }
}

resource "aws_ssm_parameter" "connection_encora" {
  name  = "/${var.env}/connections/encora"
  type  = "SecureString"
  value = var.datamart_encora_connection_secret
}

resource "aws_dynamodb_table" "notes_table" {
  name         = var.notes_table_name
  billing_mode = "PAY_PER_REQUEST"

  attribute {
    name = "Id"
    type = "S"
  }
  hash_key = "Id"
  # tags = {
  #   RoleArn   = aws_iam_role.dynamo_table_role.arn
  #   PolicyArn = aws_iam_policy.dynamodb_cloudwatch_policy.arn
  # }
}

resource "aws_dynamodb_table" "users_table" {
  name         = var.users_table_name
  billing_mode = "PAY_PER_REQUEST"

  attribute {
    name = "Id"
    type = "S"
  }
  hash_key = "Id"
  # tags = {
  #   RoleArn   = aws_iam_role.dynamo_table_role.arn
  #   PolicyArn = aws_iam_policy.dynamodb_cloudwatch_policy.arn
  # }
}

resource "aws_lambda_function" "http_listener" {
  function_name    = var.http_listener_function_name
  role             = aws_iam_role.http_listener_lambda_role.arn
  runtime          = "java21"
  handler          = var.http_listener_function_handler
  timeout          = var.lambda_timeout
  memory_size      = var.lambda_memory_size
  architectures    = var.lambda_architecture
  source_code_hash = filebase64sha256("../target/my-notes-1.0-SNAPSHOT-aws.jar")
  filename         = "../target/my-notes-1.0-SNAPSHOT-aws.jar"
  # environment {
  #   variables = {
  #     AWSSDK__SQS__QUEUES__EVENTPROCESSORQUEUE = aws_sqs_queue.event_processor_sqs_queue.id,
  #     AWSSDK__DYNAMODB__TABLES__EVENTSTABLE    = var.notes_table_name,
  #     DOTNET_ENVIRONMENT                       = var.dotnet_environment,
  #     WEBHOOKAUTH__USER                        = aws_ssm_parameter.webhook_auth_user.value,
  #     WEBHOOKAUTH__PASSWORD                    = aws_ssm_parameter.webhook_auth_password.value,
  #     APP_VERSION                              = var.app_version
  #   }
  # }
  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_rest_api" "api_gateway" {
  name        = var.api_gateway_name
  description = "The API Gateway for Listener Lambdas"
}

resource "aws_api_gateway_resource" "api_gateway_proxy_resource" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  parent_id   = aws_api_gateway_rest_api.api_gateway.root_resource_id
  path_part   = "{proxy+}"
}

resource "aws_api_gateway_method" "api_gateway_methods" {
  count         = length(var.api_gateway_http_methods)
  rest_api_id   = aws_api_gateway_rest_api.api_gateway.id
  resource_id   = aws_api_gateway_resource.api_gateway_proxy_resource.id
  http_method   = var.api_gateway_http_methods[count.index]
  authorization = "NONE"

  request_parameters = {
    "method.request.path.proxy" = true
  }
}

resource "aws_api_gateway_integration" "api_gateway_integration" {
  count                   = length(var.api_gateway_http_methods)
  rest_api_id             = aws_api_gateway_rest_api.api_gateway.id
  resource_id             = aws_api_gateway_method.api_gateway_methods[count.index].resource_id
  http_method             = aws_api_gateway_method.api_gateway_methods[count.index].http_method
  integration_http_method = "POST"
  type                    = "AWS_PROXY"
  uri                     = aws_lambda_function.http_listener.invoke_arn
}

resource "aws_api_gateway_deployment" "api_gateway_deployment" {
  depends_on = [
    aws_api_gateway_integration.api_gateway_integration
  ]
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id

  triggers = {
    redeployment = sha1(jsonencode([
      aws_api_gateway_resource.api_gateway_proxy_resource,
      aws_api_gateway_method.api_gateway_methods,
      aws_api_gateway_integration.api_gateway_integration
      # aws_api_gateway_rest_api_policy.whitelisted_ips_api_gateway_policy_attachment
    ]))
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "api_gateway_stage" {
  # depends_on = [aws_cloudwatch_log_group.api_gateway_logs]
  deployment_id = aws_api_gateway_deployment.api_gateway_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.api_gateway.id
  stage_name    = var.api_gateway_stage_name
}

resource "aws_api_gateway_method_settings" "gateway_settings" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  stage_name  = aws_api_gateway_stage.api_gateway_stage.stage_name
  method_path = "*/*"

  settings {
    metrics_enabled = true
    logging_level   = "INFO"
  }
}

# This role was created manually with these permissions: AmazonAPIGatewayPushToCloudWatchLogs
# It's shared across all stacks (dev, stg and prod), if not shared this way then we have the following problem
# https://github.com/aws/aws-cdk/discussions/22713
data "aws_iam_role" "cloudwatch" {
  count = terraform.workspace == "local" ? 0 : 1
  name  = "api_gateway_cloudwatch_global"
}

resource "aws_api_gateway_account" "api_gateway_account" {
  count               = terraform.workspace == "local" ? 0 : 1
  cloudwatch_role_arn = data.aws_iam_role.cloudwatch[0].arn
}

resource "aws_iam_role_policy_attachment" "api_gateways_logs_writer_attachment" {
  count      = terraform.workspace == "local" ? 0 : 1
  role       = data.aws_iam_role.cloudwatch[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonAPIGatewayPushToCloudWatchLogs"
}

resource "aws_sns_topic" "api_gateway_logging_error_topic" {
  name = "${var.env}_api_gateway_logging_error_topic"
}

# When using for_each if a set was provided, each.key and each.value is the same
resource "aws_sns_topic_subscription" "api_gateway_logging_error_subscription" {
  for_each  = toset(var.sns_alerts_emails)
  topic_arn = aws_sns_topic.api_gateway_logging_error_topic.arn
  protocol  = "email"
  endpoint  = each.value
}

resource "aws_cloudwatch_metric_alarm" "listener_api_gateway_4xx_errors_alarm" {
  alarm_name          = "${var.env}_listener_api_gateway_4xx_errors_alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  alarm_description   = "Gateway 4xx error rate has exceeded 5%"
  treat_missing_data  = "notBreaching"
  metric_name         = "4XXError"
  namespace           = "AWS/ApiGateway"
  period              = 180
  evaluation_periods  = 1
  threshold           = 0.05
  statistic           = "Average"
  unit                = "Count"

  dimensions = {
    ApiName = aws_api_gateway_rest_api.api_gateway.name
    Stage   = aws_api_gateway_stage.api_gateway_stage.stage_name
  }

  alarm_actions = [aws_sns_topic.api_gateway_logging_error_topic.arn]
}

resource "aws_cloudwatch_metric_alarm" "listener_api_gateway_5xx_errors_alarm" {
  alarm_name          = "${var.env}_listener_api_gateway_5xx_errors_alarm"
  comparison_operator = "GreaterThanOrEqualToThreshold"
  alarm_description   = "Gateway 5xx error rate has exceeded 5%"
  treat_missing_data  = "notBreaching"
  metric_name         = "5XXError"
  namespace           = "AWS/ApiGateway"
  period              = 180
  evaluation_periods  = 1
  threshold           = 0.05
  statistic           = "Average"
  unit                = "Count"

  dimensions = {
    ApiName = aws_api_gateway_rest_api.api_gateway.name
    Stage   = aws_api_gateway_stage.api_gateway_stage.stage_name
  }

  alarm_actions = [aws_sns_topic.api_gateway_logging_error_topic.arn]
}

resource "aws_lambda_permission" "api_gateway_invoke_permissions" {
  statement_id  = "AllowAPIGatewayInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.http_listener.function_name
  principal     = "apigateway.amazonaws.com"

  # The /*/* portion grants access from any method on any resource
  # within the API Gateway "REST API".
  source_arn = "${aws_api_gateway_rest_api.api_gateway.execution_arn}/*/*"
}

resource "aws_iam_policy" "lambda_ssm_policy" {
  name        = "${var.env}lambda-ssm-policy"
  description = "Permissions for Lambda to access SSM Parameter"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = "ssm:GetParameter",
        Resource = [
          aws_ssm_parameter.connection_encora.arn
        ]
      },
    ],
  })
}

resource "aws_iam_role" "http_listener_lambda_role" {
  name = "${var.env}_http_listener_lambda_role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}


resource "aws_iam_role_policy_attachment" "http_listener_lambda_policy_attachment" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.http_listener_lambda_role.name
}

resource "aws_iam_policy" "lambda_dynamodb_policy" {
  name        = "${var.env}lambda-dynamodb-policy"
  description = "Permissions for Lambda functions to interact with DynamoDB"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "dynamodb:GetItem",
          "dynamodb:PutItem",
          "dynamodb:UpdateItem",
          "dynamodb:DeleteItem",
        ],
        Resource = [
          aws_dynamodb_table.notes_table.arn,
          aws_dynamodb_table.users_table.arn
        ]
      },
    ],
  })
}

resource "aws_iam_role_policy_attachment" "lambda_dynamodb_policy_attachment" {
  policy_arn = aws_iam_policy.lambda_dynamodb_policy.arn
  role       = aws_iam_role.http_listener_lambda_role.name
}


resource "aws_cloudwatch_log_group" "http_listener_lambda_log_group" {
  name              = "/aws/lambda/${aws_lambda_function.http_listener.function_name}"
  retention_in_days = 30
  lifecycle {
    prevent_destroy = false
  }
}

resource "aws_iam_policy" "function_logging_policy" {
  name = "${var.env}function-logging-policy_1"
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Action : [
          "logs:CreateLogStream",
          "logs:PutLogEvents",
          "logs:CreateLogGroup",
        ],
        Effect : "Allow",
        Resource : "arn:aws:logs:*:*:*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "function_logging_policy_attachment" {
  role       = aws_iam_role.http_listener_lambda_role.id
  policy_arn = aws_iam_policy.function_logging_policy.arn
}

output "api_gateway_test_url" {
  value = terraform.workspace == "local" ? "http://localhost:4566/restapis/${aws_api_gateway_rest_api.api_gateway.id}/${var.api_gateway_stage_name}/_user_request_" : null
}


# resource "aws_ssm_parameter" "webhook_auth_user" {
#   name  = "/${var.env}/webhook_auth/user"
#   type  = "SecureString"
#   value = var.webhook_auth_user
# }

# resource "aws_ssm_parameter" "webhook_auth_password" {
#   name  = "/${var.env}/webhook_auth/password"
#   type  = "SecureString"
#   value = var.webhook_auth_password
# }

# resource "aws_iam_policy" "dynamodb_cloudwatch_policy" {
#   name = "${var.env}DynamoDBCloudWatchPolicy"

#   policy = jsonencode({
#     Version = "2012-10-17",
#     Statement = [
#       {
#         Action = [
#           "logs:CreateLogStream",
#           "logs:PutLogEvents",
#         ],
#         Effect   = "Allow",
#         Resource = aws_cloudwatch_log_group.dynamodb_log_group.arn
#       }
#     ]
#   })
# }

# resource "aws_iam_role" "dynamo_table_role" {
#   name = "${var.env}DynamoTableRole"

#   assume_role_policy = jsonencode({
#     Version = "2012-10-17",
#     Statement = [
#       {
#         Action = "sts:AssumeRole",
#         Effect = "Allow",
#         Principal = {
#           Service = "dynamodb.amazonaws.com"
#         }
#       }
#     ]
#   })
# }

# resource "aws_iam_role_policy_attachment" "dynamodb_cloudwatch_attachment" {
#   policy_arn = aws_iam_policy.dynamodb_cloudwatch_policy.arn
#   role       = aws_iam_role.dynamo_table_role.name
# }


# resource "aws_sqs_queue" "event_processor_sqs_deadletter_queue" {
#   name                      = var.event_processor_sqs_deadletter_queue_name
#   max_message_size          = var.sqs_max_message_size
#   message_retention_seconds = var.sqs_deadletter_retention_seconds
# }


# resource "aws_sqs_queue" "event_processor_sqs_queue" {
#   name                       = var.event_processor_sqs_queue_name
#   message_retention_seconds  = var.sqs_message_retention_seconds
#   max_message_size           = var.sqs_max_message_size
#   visibility_timeout_seconds = var.sqs_visibility_timeout_seconds
#   delay_seconds              = var.sqs_delay_seconds
#   receive_wait_time_seconds  = var.sqs_receive_wait_time_seconds

#   redrive_policy = jsonencode({
#     deadLetterTargetArn = aws_sqs_queue.event_processor_sqs_deadletter_queue.arn
#     maxReceiveCount     = var.sqs_max_receive_count
#   })

#   tags = {
#     Environment = var.dotnet_environment
#   }

#   # lifecycle {
#   #   ignore_changes = [redrive_policy]
#   # }
# }

# resource "aws_sqs_queue_redrive_allow_policy" "event_processor_sqs_redrive_policy" {
#   queue_url = aws_sqs_queue.event_processor_sqs_deadletter_queue.id

#   redrive_allow_policy = jsonencode({
#     redrivePermission = "byQueue",
#     sourceQueueArns   = [aws_sqs_queue.event_processor_sqs_queue.arn]
#   })
# }


# resource "aws_sqs_queue_policy" "event_processor_sqs_queue_policy" {
#   queue_url = aws_sqs_queue.event_processor_sqs_queue.url
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect    = "Allow"
#         Principal = "*"
#         Action    = "SQS:SendMessage"
#         Resource  = aws_sqs_queue.event_processor_sqs_queue.arn
#       }
#     ]
#   })
# }

# data "aws_iam_policy_document" "whitelisted_ips_api_gateway_policy" {
#   count = var.env == "prod" ? 1 : 0
#   statement {
#     effect = "Allow"

#     principals {
#       type        = "*"
#       identifiers = ["*"]
#     }

#     actions   = ["execute-api:Invoke"]
#     resources = ["${aws_api_gateway_rest_api.api_gateway.execution_arn}/*"]

#     condition {
#       test     = "IpAddress"
#       variable = "aws:SourceIp"
#       values   = var.api_gateway_whitelisted_ips
#     }
#   }
# }

# resource "aws_api_gateway_rest_api_policy" "whitelisted_ips_api_gateway_policy_attachment" {
#   count       = var.env == "prod" ? 1 : 0
#   rest_api_id = aws_api_gateway_rest_api.api_gateway.id
#   policy      = data.aws_iam_policy_document.whitelisted_ips_api_gateway_policy[0].json
# }

# resource "aws_iam_policy" "lambda_sqs_policy" {
#   name        = "${var.env}lambda-sqs-policy"
#   description = "Permissions for Lambda functions to interact with SQS"
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect = "Allow"
#         Action = [
#           "sqs:ReceiveMessage",
#           "sqs:DeleteMessage",
#           "sqs:GetQueueAttributes",
#           "sqs:GetQueueUrl",
#         ]
#         Resource = [
#           aws_sqs_queue.event_processor_sqs_queue.arn
#         ]
#       }
#     ]
#   })
# }

# resource "aws_iam_policy" "lambda_appconfig_policy" {
#   name        = "${var.env}-lambda-appconfig-policy"
#   description = "Permissions for Lambda functions to interact with AppConfig"
#   policy = jsonencode({
#     Version = "2012-10-17"
#     Statement = [
#       {
#         Effect = "Allow"
#         Action = [
#           "appconfig:StartConfigurationSession",
#           "appconfig:GetLatestConfiguration"
#         ]
#         Resource = "${var.crystal_choice_appconfig_arn}/*"
#       }
#     ]
#   })
# }

## THIS policy is only attached when the environment is not local because this allows the Lambda function
## to listen the SigTerm signal allowing to do a graceful shutdown, we also must include the needed Lambda
## layer ARN to allow it (this can only be included in the real environments because localstack does not 
## support calling those Layers in the free version)
# resource "aws_iam_role_policy_attachment" "event_processor_insights_extensions_policy" {
#   count      = terraform.workspace == "local" ? 0 : 1
#   policy_arn = "arn:aws:iam::aws:policy/CloudWatchLambdaInsightsExecutionRolePolicy"
#   role       = aws_iam_role.lambda_role_2.name
# }

# resource "aws_iam_role_policy_attachment" "lambda_sqs_policy_attachment" {
#   policy_arn = aws_iam_policy.lambda_sqs_policy.arn
#   role       = aws_iam_role.http_listener_lambda_role.name
# }


# resource "aws_iam_role_policy_attachment" "event_processor_policy_attachment_appconfig" {
#   policy_arn = aws_iam_policy.lambda_appconfig_policy.arn
#   role       = aws_iam_role.lambda_role_2.name
# }

# data "aws_iam_policy" "lambda_vpc_networking_policy" {
#   arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
# }

# resource "aws_iam_role_policy_attachment" "lambda_vpc_networking_policy_attachment" {
#   policy_arn = data.aws_iam_policy.lambda_vpc_networking_policy.arn
#   role       = aws_iam_role.lambda_role_2.name
# }