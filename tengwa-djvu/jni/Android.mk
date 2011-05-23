#
#
# License info here
#
#
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := djvulibre-android
LOCAL_CFLAGS := -DANDROID_NDK -DHAVE_CONFIG_H
LOCAL_SRC_FILES := $(wildcard djvulibre/libdjvu/*.cpp)

include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := djvulibre-native
LOCAL_CFLAGS := -DANDROID_NDK
LOCAL_STATIC_LIBRARIES := djvulibre-android
LOCAL_SRC_FILES := djvulibre-native.c
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)