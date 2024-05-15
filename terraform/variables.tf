variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1" # Update with your desired region
}

variable "env" {
  description = "AWS env name"
  type        = string
  default     = "dev" # Update with your desired region
}

variable "notes_table_name" {
  description = "Name of the DynamoDB table containing notes"
  type        = string
  default     = "notes"
}

variable "users_table_name" {
  description = "Name of the DynamoDB table containing users"
  type        = string
  default     = "users"
}

#Lambdas Variable Definition
variable "http_listener_function_name" {
  description = "Name of Lambda function 1"
  type        = string
  default     = "Listener"
}

variable "datamart_encora_connection_secret" {
  description = "Secret connection string for the datamart"
  type        = string
  default     = "Server=sqlserverencora,1433;Database=master;User Id=sa;Password=P455w0rd;TrustServerCertificate=True;"
}

variable "http_listener_function_handler" {
  description = "The name of the Http Listener Java handler function"
  type        = string
  default     = "com.zeus.Handler::handleRequest"
}

variable "lambda_timeout" {
  description = "The amount of time your Lambda Function has to run in seconds"
  type        = number
  default     = 10
}

variable "lambda_architecture" {
  description = "Lambda function architecture"
  type        = list(string)
  default     = ["arm64"]
}

variable "lambda_memory_size" {
  description = "The amount of memory in MB to allocate to your Lambda Function"
  type        = number
  default     = 256
}

variable "api_gateway_name" {
  description = "Name of the API Gateway"
  type        = string
  default     = "listener-api-geteway"
}

variable "api_gateway_stage_name" {
  description = "Name of the API Gateway"
  type        = string
  default     = "local"
}

variable "sns_alerts_emails" {
  description = "List of emails subscribed to SNS CLoudWatch alerts topic"
  type        = list(string)
  default     = ["miguel.arcos@encora.com"]
}


variable "api_gateway_http_methods" {
  description = "The list of HTTP methods working for Api Gateway Lambda integration"
  type        = list(string)
  default     = ["POST", "GET", "PUT", "DELETE"]
}

variable "app_version" {
  description = "The Deployed application version"
  type        = string
  default     = "<branch_name>:<commit_hash>"
}

# variable "api_gateway_whitelisted_ips" {
#   description = "The list of allowed IP CIDRs for API Gateway"
#   type        = list(string)
#   default     = [""]
# }


# variable "event_processor_sqs_deadletter_queue_name" {
#   description = "Name of the SQS Dead Letter Queue"
#   type        = string
#   default     = "event-processor-sqs-deadletter"
# }

# variable "sqs_deadletter_retention_seconds" {
#   description = "Name of the SQS queue"
#   type        = number
#   default     = 345600 # 4 days
# }

# #SQS Variable Definition
# variable "event_processor_sqs_queue_name" {
#   description = "Name of the SQS queue"
#   type        = string
#   default     = "event-processor-sqs"
# }


# variable "sqs_message_retention_seconds" {
#   description = "Number of seconds to retain messages in the SQS queue"
#   type        = number
#   default     = 345600 # 4 days
# }

# variable "sqs_max_message_size" {
#   description = "Maximum message size in bytes for the SQS queue"
#   type        = number
#   default     = 262144 # 1 MB
# }

# variable "sqs_visibility_timeout_seconds" {
#   description = "Duration for which the message is hidden from other consumers after being consumed by a single consumer"
#   type        = number
#   default     = 60
# }

# variable "sqs_delay_seconds" {
#   description = "Number of seconds to delay the message before it becomes available for processing"
#   type        = number
#   default     = 0
# }

# variable "sqs_receive_wait_time_seconds" {
#   description = "Duration to wait for a message to arrive in the SQS queue"
#   type        = number
#   default     = 20
# }

# variable "sqs_max_receive_count" {
#   description = "Maximum number of times a message can be received before being moved to the dead-letter queue"
#   type        = number
#   default     = 1
# }

# variable "dotnet_environment" {
#   description = "Dotnet Environment name"
#   type        = string
#   default     = "Local"
# }

# variable "webhook_auth_user" {
#   description = "The user for the webhook auth"
#   type        = string
#   default     = "aladdin"
# }

# variable "webhook_auth_password" {
#   description = "The password for the webhook auth"
#   type        = string
#   default     = "opensesame"
# }

# variable "lambda_event_processor_vpc_security_group_ids" {
#   description = "List of security groups IDs"
#   type        = list(string)
#   default     = [""]
# }

# variable "lambda_event_processor_vpc_subnet_ids" {
#   description = "List of VPC subnets IDs"
#   type        = list(string)
#   default     = [""]
# }

# variable "event_processor_lambda_layers_arns" {
#   description = "List of Layers ARNs to attach to EventProcessor function"
#   type        = list(string)
#   default     = [""]
# }

# variable "crystal_choice_appconfig_arn" {
#   description = "The ARN of the AppConfig application for Crystal Choice"
#   type        = string
#   default     = "arn:aws:appconfig:<region>:<account_id>:application/<app_id>"
# }
