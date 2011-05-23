// android includes
#include <jni.h>
#include <android/log.h>

// djvulibre's includes
#include <djvulibre/libdjvu/ddjvuapi.h>


// single main context that will be used to work with
static ddjvu_context_t *main_context = NULL;

// logging resources
static const char *LOG_TAG = "Djvulibre native";

void Java_tengwa_djvu_Djvulibre_contextCreate (JNIEnv *env, jobject this) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG, 
                         "Attempt to create context...");

    if (main_context) {
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Context already exists. No actions were made.");
    } else {

        main_context = ddjvu_context_create ("djvulibre-native");

        if (main_context)
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "Context has been successfully created.");
        else
            __android_log_write (ANDROID_LOG_FATAL, LOG_TAG,
                                 "Context creation failed for unknown reason.");
    }
}

void Java_tengwa_djvu_Djvulibre_contextRelease (JNIEnv *env, jobject this) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Attempt to release context...");

    if (!main_context) {
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "There is no context to release.");
    } else {
        ddjvu_context_release (main_context);
        main_context = NULL;
        
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Context has been successfully released.");
    }
}

void Java_tengwa_djvu_Djvulibre_cacheSetSize (JNIEnv *env, jobject this, 
                                              jint cachesize) {
    if (!main_context) {
        __android_log_write (ANDROID_LOG_ERROR, LOG_TAG,
                             "Context is not created. Done nothing.");
    } else {
        ddjvu_cache_set_size (main_context, (unsigned long) cachesize);

        __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                             "Cache is now set to %d bytes", cachesize);
    }
}

jint Java_tengwa_djvu_Djvulibre_cacheGetSize (JNIEnv *env, jobject this) {
    if (!main_context) {
        __android_log_write (ANDROID_LOG_ERROR, LOG_TAG,
                             "Context is not created. Done nothing");
    } else {
        jint cachesize = ddjvu_cache_get_size (main_context);

        __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                             "Current cache size is %d bytes", cachesize);
    }
}

void Java_tengwa_djvu_Djvulibre_cacheClear (JNIEnv *env, jobject this) {
    if (!main_context) {
        __android_log_write (ANDROID_LOG_ERROR, LOG_TAG,
                             "Context is not created. Done nothing");
    } else {
        ddjvu_cache_clear (main_context);

        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Cache has been cleared.");
    }
}
