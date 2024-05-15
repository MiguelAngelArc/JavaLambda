aws_region = "us-east-1" # Update with your desired region

notes_table_name                              = "dev-notes"
users_table_name                              = "dev-users"
api_gateway_name                              = "dev-listener-api-gateway"
api_gateway_stage_name                        = "dev"
env                                           = "dev"
lambda_timeout                                = 45
# lambda_event_processor_vpc_security_group_ids = ["sg-058140e87076d16e7"]
# lambda_event_processor_vpc_subnet_ids         = ["subnet-0c852333211ede102"]
# event_processor_lambda_layers_arns            = ["arn:aws:lambda:us-east-1:958113053741:layer:AWS-AppConfig-Extension:141"]
