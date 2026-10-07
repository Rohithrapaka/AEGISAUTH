# Keep ML Kit & TensorFlow Lite models and classes
-keep class com.google.mlkit.** { *; }
-keep class org.tensorflow.lite.** { *; }

# Keep Room entities and DAOs
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }

# Keep data models
-keep class com.aegisauth.data.local.entity.** { *; }
-keep class com.aegisauth.domain.model.** { *; }
