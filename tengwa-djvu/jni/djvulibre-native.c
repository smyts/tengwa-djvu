// android includes
#include <jni.h>
#include <android/log.h>

// djvulibre's includes
#include <djvulibre/libdjvu/ddjvuapi.h>


// single main context that will be used to work with
static ddjvu_context_t *main_context = NULL;

// for now, only one document per is supported
static ddjvu_document_t *main_document = NULL;

// logging resources
static const char *LOG_TAG = "Djvulibre native";

// toggle document cache usage
static int main_cache = 1;

jint Java_tengwa_djvu_Djvulibre_contextCreate (JNIEnv *env, jobject this) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG, 
                         "Attempt to create context...");

    if (main_context) {
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Context already exists. No actions were made.");
    } else {
        main_context = ddjvu_context_create ("djvulibre-native");

        if (main_context) {
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "Context has been successfully created.");
        } else {
            __android_log_write (ANDROID_LOG_FATAL, LOG_TAG,
                                 "Context creation failed for unknown reason.");
            return 1; // general failure
        }
    }

    return 0;
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
                             "Context does not exist. Done nothing.");
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
                             "Context does not exist. Done nothing");
    } else {
        ddjvu_cache_clear (main_context);

        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Cache has been cleared.");
    }
}

void Java_tengwa_djvu_Djvulibre_handleDjvuMessages (JNIEnv *env, jclass cls, jint wait) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                        "Handling ddjvu messages");
    const ddjvu_message_t *msg;
    jmethodID mid;
    jint status;

    if (wait)
        ddjvu_message_wait (main_context);

    while ((msg = ddjvu_message_peek (main_context))) {
        switch (msg->m_any.tag) {
        case DDJVU_ERROR:
            __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_ERROR:\n%s:%s:%d:\n%s",
                                 msg->m_error.filename, msg->m_error.function,
                                 msg->m_error.lineno, msg->m_error.message);
            break;
        case DDJVU_INFO:
            __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_INFO:\n%s", msg->m_info.message);
            break;
        case DDJVU_NEWSTREAM:
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_NEWSTREAM");
            break;
        case DDJVU_DOCINFO:
            mid = (*env)->GetStaticMethodID (env, cls, "handleDdjvuDocinfo", "(I)V");
            status = (jint)ddjvu_document_decoding_status (main_document);

            __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_DOCINFO:\nstatus: %d", status);

            
            (*env)->CallStaticVoidMethod (env, cls, mid, status);
                     
            break;
        case DDJVU_PAGEINFO:
            mid = (*env)->GetStaticMethodID (env, cls, "handleDdjvuPageinfo", "(I)V");
            status = (jint)ddjvu_document_decoding_status (main_document);

            __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_PAGEINFO:\nstatus %s", status);

            (*env)->CallStaticVoidMethod (env, cls, mid, status);


            break;
        case DDJVU_RELAYOUT:
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_RELAYOUT");
            break;
        case DDJVU_REDISPLAY:
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_REDISPLAY");
            break;
        case DDJVU_CHUNK:
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_CHUNK");
            break;
        case DDJVU_THUMBNAIL:
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_THUMBNAIL");
            break;
        case DDJVU_PROGRESS:
            __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                                 "DDJVU_PROGRESS");
            break;
        default:
            break;
        }

        ddjvu_message_pop (main_context);
    }

    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Ddjvu messages were handled");
}


jint Java_tengwa_djvu_Djvulibre_documentCreate \
(JNIEnv *env, jobject this, jstring jstring_filepath/* jbyteArray jbyte_filepath */) {
    __android_log_write(ANDROID_LOG_INFO, LOG_TAG,
                        "Attempt to create document...");

/* Check whether context exists */
    if (!main_context) {
        __android_log_write(ANDROID_LOG_ERROR, LOG_TAG,
                            "Context does not exist");
        return 1;
    }

/* Convert path */
    const char *cfilepath = (*env)->GetStringUTFChars (env, jstring_filepath, NULL);

/* Access new document */
    if (main_document != NULL) {
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Starting release of the previous document");
        ddjvu_document_release (main_document);
        main_document = NULL;
    }

    main_document = \
        ddjvu_document_create_by_filename (main_context, cfilepath,
                                           main_cache);
    (*env)->ReleaseStringUTFChars (env, jstring_filepath, cfilepath);

    if (main_document) {
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Document has been created.");
    } else {
        __android_log_write (ANDROID_LOG_FATAL, LOG_TAG,
                             "Document creation failed for unknown reason.");
        return 1;
    }

    return 0;
}


void Java_tengwa_djvu_Djvulibre_documentRelease (JNIEnv *env, jobject this) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Attempt to start releasing document...");

    if (!main_document) {
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "There is no document to release.");
    } else {
        ddjvu_document_release (main_document);
        main_document = NULL;
        
        __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                             "Document release has started.");
    }
}

jint Java_tengwa_djvu_Djvulibre_getPagenum (JNIEnv *env, jobject this) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Recieving number of pages...");
    return (jint) ddjvu_document_get_pagenum (main_document);
}

jlong Java_tengwa_djvu_Djvulibre_pageCreateByPagneno (JNIEnv *env, jobject this, 
                                                     jint pageno) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Creating page by pageno...");
    
    return (jlong) ddjvu_page_create_by_pageno (main_document, pageno);
}

void Java_tengwa_djvu_Djvulibre_pageRelease (JNIEnv *env, jobject this,
                                                 jlong pageobj) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Releasing page...");
    ddjvu_page_release ((ddjvu_page_t*) pageobj);
}
