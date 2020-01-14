#!/bin/bash -eux
echo -n $FIREBASE_SERVICE_KEY | base64 --decode > $SAMPLE_GOOGLE_SERVICES_JSON