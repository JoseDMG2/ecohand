# MediaPipe Hand Landmarker Model

This folder should contain the MediaPipe Hand Landmarker model file.

## Download Instructions

1. Download the `hand_landmarker.task` file from MediaPipe Model Registry:
   https://developers.google.com/mediapipe/solutions/vision/hand_landmarker#models

2. Place the downloaded `hand_landmarker.task` file in this directory:
   `app/src/main/assets/hand_landmarker.task`

## Model Information

- **File name**: `hand_landmarker.task`
- **Size**: ~11MB
- **Provider**: Google MediaPipe
- **License**: Apache 2.0

## Alternative Download

You can also download directly from:
```
https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task
```

## Notes

- The model must NOT be compressed (noCompress is already configured in build.gradle.kts)
- The model works offline once downloaded
- Minimum Android SDK: 24
