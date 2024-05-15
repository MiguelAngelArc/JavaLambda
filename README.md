# How to run the project?

The easiest way to run this project is to install ***docker*** and ***docker compose***, then  run the command:
```bash
docker compose up -d
```

This way we will have a local environment of AWS running in our machine.

Then create an S3 bucket to place the .tfstate files, running the follwing command:
```bash
aws s3api create-bucket \
    --bucket my-notes \
    --endpoint-url http://localhost:4566 \
    --region us-east-1 \
    --no-cli-pager
```

Then initialize the terraform backend (This only has to be done once, if you destroy and restart the localstack containers this files are preserved), to initialize the backend run the following command:
```bash
terraform -chdir=./terraform init --backend-config=./environments/local.conf
```

Finally, there is a script under ***./local/deploy_lambdas.sh***, in it change the **PROJECT_ROOT** to the route of this project in your local machine, and finally run the script alongside with the following commands to run the project (Make the script executable if needed by running ```chmod +x ./local/deploy_lambdas.sh```):
```bash
./local/deploy_lambdas.sh && \
    terraform -chdir=./terraform workspace new local; \
    terraform -chdir=./terraform workspace select local; \
    terraform -chdir=./terraform plan && \
    terraform -chdir=./terraform apply --auto-approve
```

Finally, there is a postman collection in this project that you can import to test the APIs, all the notes operations are protected with JWTs, but you can use the ***sign-up and sign-in*** methods to create your users and JWTs, if you run any of those endpoints the **access token** is stored automatically in the collection variables, so, you don't need to edit headers manually.

**Note**: Every time you restart the **localstack** container, the url of the ***Api Gateway*** changes, so, you must edit the ***baseUrl*** collection variable of the **Postman** colllection. The ***baseUrl*** is the output of the terraform command when it finishes the ***apply*** command
