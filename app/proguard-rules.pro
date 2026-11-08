# Room, Hilt, WorkManager and kotlinx.serialization ship their own consumer rules.

# Keep the backup file format stable: field names are the JSON keys.
-keep,includedescriptorclasses class com.rk.gharstock.feature.backup.data.**Dto { *; }
-keep,includedescriptorclasses class com.rk.gharstock.feature.backup.data.BackupFile { *; }

# Navigation routes are serialized by class name.
-keep @kotlinx.serialization.Serializable class com.rk.gharstock.**Route { *; }
