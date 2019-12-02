#!/bin/bash -eux
echo -n $GCLOUD_SERVICE_KEY | base64 --decode > $GCLOUD_CREDENTIAL_JSON_PATH