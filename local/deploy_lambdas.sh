#!/bin/sh
PROJECT_ROOT=/Users/miguel.arcos/Desktop/Work/Projects/JavaProject/my-notes

publishProject() {
    cd $PROJECT_ROOT
    rm -Rf target; mvn clean package shade:shade
}

if !(aws s3api head-bucket --bucket "my-notes" --endpoint-url http://localhost:4566 --region us-east-1 2>/dev/null); then
    echo "Creating new bucket for Terraform"

    # Add # --create-bucket-configuration LocationConstraint=us-east-1 if region is not us-east-1
    aws s3api create-bucket \
        --bucket my-notes \
        --endpoint-url http://localhost:4566 \
        --region us-east-1 \
        --no-cli-pager
fi

publishProject
