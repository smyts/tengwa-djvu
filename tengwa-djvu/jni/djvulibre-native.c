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

// save JNIEnv, class, method for callback
static JNIEnv *callback_env;
static const char *DJVULIBRE_CLASSNAME = "tengwa/djvu/Djvulibre";

// settings for images
static ddjvu_format_style_t image_pixel_style = DDJVU_FORMAT_RGBMASK32;
static int pixel_size = 4; // four bytes
static unsigned int masks[] = {0xFF0000, 0x00FF00, 0x0000FF};
static ddjvu_format_t *image_pixel_format = NULL;
static int render_width = 960, render_height = 640;




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

jlong Java_tengwa_djvu_Djvulibre_pageCreateByPageno (JNIEnv *env, jobject this, 
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

void messageHandleCallback (ddjvu_context_t *ctx, void *closure) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Executing callback...");

    static jclass cls = NULL;
    static jmethodID mid = NULL;

    if (cls == NULL) {
        cls = (*callback_env)->FindClass (callback_env, 
                                          DJVULIBRE_CLASSNAME);
    }

    if (mid == NULL) {
        mid =  (*callback_env)->GetStaticMethodID (callback_env, cls, 
                                                   "handleCallback", "()V");
    }

    (*callback_env)->CallStaticVoidMethod (callback_env, cls, mid);
}

void Java_tengwa_djvu_Djvulibre_installCallback (JNIEnv *env, jclass cls) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Installing callback...");

    callback_env = env;
    ddjvu_message_set_callback (main_context, 
                                (ddjvu_message_callback_t) messageHandleCallback,
                                NULL);

    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Callback installed...");

}

jint Java_tengwa_djvu_Djvulibre_waitPage (JNIEnv *env, jclass cls, jlong pageobj) {
    int status = ddjvu_page_decoding_status ((ddjvu_page_t*) pageobj);

    while ((status = ddjvu_page_decoding_status ((ddjvu_page_t*) pageobj)) < 
           DDJVU_JOB_OK) {

        __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                             "Waiting for page (status = %d)...", status);

        Java_tengwa_djvu_Djvulibre_handleDjvuMessages (env, cls, 1);
    }

    __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                         "Waiting for page (status = %d)...", status);

    return status;
}

jint Java_tengwa_djvu_Djvulibre_getPageHeight (JNIEnv *env, jclass cls, jlong pageobj) {
    return ddjvu_page_get_height ((ddjvu_page_t*) pageobj);
}

jint Java_tengwa_djvu_Djvulibre_getPageWidth (JNIEnv *env, jclass cls, jlong pageobj) {
    return ddjvu_page_get_width ((ddjvu_page_t*) pageobj);
}

jint Java_tengwa_djvu_Djvulibre_getPixelSize (JNIEnv *env, jclass cls, jlong pageobj) {
    return pixel_size;
}

static void prepare_image_format () {
    if (image_pixel_format == NULL) {
        image_pixel_format = ddjvu_format_create (image_pixel_style, 3, masks);
        ddjvu_format_set_row_order (image_pixel_format, 1);
        ddjvu_format_set_y_direction (image_pixel_format, 1);
    }
}

jint Java_tengwa_djvu_Djvulibre_getRenderHeight (JNIEnv *env, jclass cls) {
    return (jint)render_height;
}

jint Java_tengwa_djvu_Djvulibre_getRenderWidth (JNIEnv *env, jclass cls) {
    return (jint) render_width;
}


void Java_tengwa_djvu_Djvulibre_setRenderHeight (JNIEnv *env, jclass cls, int height) {
    render_height = height;
}

void Java_tengwa_djvu_Djvulibre_setRenderWidth (JNIEnv *env, jclass cls, int width) {
    render_width = width;
}


void Java_tengwa_djvu_Djvulibre_setPixelDepth (JNIEnv *env, jclass cls, int bits) {
    prepare_image_format ();
    ddjvu_format_set_ditherbits (image_pixel_format, bits);
}

jintArray Java_tengwa_djvu_Djvulibre_getPageImage (JNIEnv *env, jclass cls, 
                                                    jlong pageobj) {
    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Getting image for page...");
    prepare_image_format ();

    int width, height, size;
    height = render_height;
    width = render_width;
    size = height * width;

    __android_log_print (ANDROID_LOG_INFO, LOG_TAG,
                         "height = %d\nwidth = %d\nsize = %d",
                         height, width, size);


    int *imgbuf = malloc (size * sizeof(int));

    ddjvu_rect_t pagerect, renderrect;
    pagerect.x = pagerect.y = 0;
    pagerect.w = width;
    pagerect.h = height;
    renderrect = pagerect;

    ddjvu_page_render ((ddjvu_page_t*) pageobj, DDJVU_RENDER_COLOR,
                       &pagerect, &renderrect, image_pixel_format,
                       width * pixel_size, imgbuf);

    __android_log_write (ANDROID_LOG_INFO, LOG_TAG,
                         "Page has beed rendered");


    jintArray img = (*env)->NewIntArray (env, size);
    (*env)->SetIntArrayRegion (env, img, 0, size, imgbuf);

    free (imgbuf);
    return img;
}
