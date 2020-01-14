#!/bin/bash -eux
echo -n $GCLOUD_SERVICE_KEY | base64 --decode > $GOOGLE_APPLICATION_CREDENTIALS