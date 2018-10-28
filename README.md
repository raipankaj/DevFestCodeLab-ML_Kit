# ML Kit Code lab
1. Create project in firebase
2. Download .json file from firebase after creating the project
3. Add .json file in app folder
4. Add following libraries in gradle to use face detection, text recognition and barcode scan
    
    *Text recognition & Barcode scanning*
    
    implementation 'com.google.firebase:firebase-ml-vision:18.0.1'


    *Face detection*
    
     implementation 'com.google.firebase:firebase-ml-vision-face-model:17.0.2'
    
5. Add meta data in manifest to download model when app is installed onto the user's device, without this when user try to use any 
of the ML Kit service at that time it will download model and till than it will not display any result
        <meta-data
                android:name="com.google.firebase.ml.vision.DEPENDENCIES"
                android:value="ocr,face,barcode"/>
