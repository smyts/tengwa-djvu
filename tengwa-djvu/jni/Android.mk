#
#
# License info here
#
#
#
LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := djvulibre-android
LOCAL_CFLAGS := -DANDROID_NDK
LOCAL_SRC_FILES := libdjvu/ddjvuapi.cpp

include $(BUILD_SHARED_LIBRARY)