# Build

```
docker build -f docker-build/Dockerfile -t status-react-native-v2:latest .
```

# Run

## Bash

```
docker run -t -i -v $(pwd):/workspace -w /workspace --rm --cap-add SYS_ADMIN --name build status-react-native-v2 bash
```

## Build app

```
docker run -t -i \
  -v $(pwd):/workspace \
  -v ~/.gradle/:/home/user/.gradle/ \
  -w /workspace \
  -e LOCAL_USER_ID=`id -u $USER` \
  status-react-native-v2 \
  /bin/sh -c "cd android && ./gradlew --stacktrace assembleRelease"

```


