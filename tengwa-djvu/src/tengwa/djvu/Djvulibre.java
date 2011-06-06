/*
 *
 * Copyright and license info here
 *
 */

package tengwa.djvu;


import android.os.AsyncTask;

public class Djvulibre {
     /*
      * Initial size of the message arrays
      */
    public static final int MESSAGES_LENGTH = 100;


    static {
        System.loadLibrary("djvulibre-native");
        sMessageTypes = new int[MESSAGES_LENGTH];
        sMessageArguments = new Object[MESSAGES_LENGTH];
        sLastMessage = -1;
        sStartMessage = 0;
        errorCall = null;
        sLastPage = -1;
        sLastPageObj = -1;
    }

    /*
     * DDJVU message types (correspond to the ones
     * at the native implementation)
     */
    public static final int MESSAGE_TYPE_ERROR = 0;
    public static final int MESSAGE_TYPE_INFO = 1;
    public static final int MESSAGE_TYPE_NEWSTREAM = 2;
    public static final int MESSAGE_TYPE_DOCINFO = 3;
    public static final int MESSAGE_TYPE_PAGEINFO = 4;
    public static final int MESSAGE_TYPE_RELAYOUT = 5;
    public static final int MESSAGE_TYPE_REDISPLAY = 6;
    public static final int MESSAGE_TYPE_CHUNK = 7;
    public static final int MESSAGE_TYPE_THUMBNAIL = 8;
    public static final int MESSAGE_TYPE_PROGRESS = 9;

    /*
     * DDJVU job types (correspond to the ones at the native
     * implementation)
     */
    public static final int JOB_NOTSTARTED = 0;
    public static final int JOB_STARTED = 1;
    public static final int JOB_OK = 2;
    public static final int JOB_FAILED = 3;
    public static final int JOB_STOPPED = 4;


    /*
     * errorDescription, docinfoDescription and etc. types
     */
    // TODO: add these types explicitly

    /*
     * Two arrays, the first one contains DDJVU message type for a message
     * to handle,the second one contains argument(s) for that particular message.
     * lastMessage is and index of the latest unhandled message.
     */
    public static int[] sMessageTypes;
    public static Object[] sMessageArguments;
    public static int sLastMessage;
    public static int sStartMessage;

    /*
     * Application-level cache of pages
     * (currently consists only of the latest page)
     *
     */

    public static int sLastPage;
    public static long sLastPageObj;

    // TODO: decision on application-level cache
    //public static int sMaxPreservedPages = 10;
    //public static Queue<Integer> sOrderQueue;
    //public static HashMap<Integer, Long> sPagesCache;

    /*
     * Callback objects to notify, set it manually for now
     */
    public static DjvulibreErrorCallback errorCall;
    public static DjvulibreDocinfoCallback docinfoCall;
    public static DjvulibrePageinfoCallback pageinfoCall;
    public static DjvulibreRedisplayCallback redisplayCall;

    static native void contextCreate();
    static native void contextRelease();
    static native void cacheSetSize(int cachesize);
    static native int cacheGetSize();
    static native void cacheClear();
    static native int documentCreate(String filepath);
    static native void documentRelease();
    static native void handleDjvuMessages(int wait);
    static native int getPagenum();
    static native long pageCreateByPageno(int pageno);
    static native void pageRelease(long pageobj);
    static native void installCallback();
    static native int waitPage(long pageobj);
    static native int getPageHeight();
    static native int getPageWidth();
    static native int getPixelSize();
    static native int getRenderHeight();
    static native int getRenderWidth();
    static native int[] getPageImage(long pageobj);
    static native void setRenderHeight();
    static native void setRenderWidth();
    static native void setPixelDepth(int bits);

    public static void handleCallback() {
        installCallback();
        new HandleMessages().execute();
    }

    public static void handleMessages() {

        for (int curId = sStartMessage; curId <= sLastMessage; ++curId) {
            sStartMessage = curId + 1;

            switch (sMessageTypes[curId]) {
                case MESSAGE_TYPE_ERROR:
                    if (errorCall != null) {
                        errorCall.signalError(0);
                    }
                    break;
                case MESSAGE_TYPE_INFO:
                    break;
                case MESSAGE_TYPE_NEWSTREAM:
                    break;
                case MESSAGE_TYPE_DOCINFO:
                    if (docinfoCall != null) {
                        docinfoCall.signalDocinfo((Integer) sMessageArguments[curId]);
                    }
                    break;
                case MESSAGE_TYPE_PAGEINFO:
                    if (pageinfoCall != null) {
                        pageinfoCall.signalPageinfo((Integer) sMessageArguments[curId]);
                    }
                    break;
                case MESSAGE_TYPE_RELAYOUT:
                    break;
                case MESSAGE_TYPE_REDISPLAY:
                    break;
                case MESSAGE_TYPE_CHUNK:
                    break;
                case MESSAGE_TYPE_THUMBNAIL:
                    break;
                case MESSAGE_TYPE_PROGRESS:
                    break;
                default:
                    break;
            }
        }

        sLastMessage = -1;
        sStartMessage = 0;
    }

    static void getPage(int pageno) {
        if (sLastPageObj != -1)
            Djvulibre.pageRelease(sLastPageObj);
        sLastPage = pageno;
        sLastPageObj = Djvulibre.pageCreateByPageno(pageno);
        Djvulibre.handleDdjvuPageinfo(Djvulibre.waitPage(sLastPageObj));
        Djvulibre.handleDjvuMessages(0);
        Djvulibre.handleMessages();
    }

    static void checkResize(int lastMessage) {
        if (lastMessage == sMessageTypes.length) {
            int[] newIntArr = new int[lastMessage * 2];
            System.arraycopy(sMessageTypes, 0, newIntArr, 0, lastMessage * 2);
            sMessageTypes = newIntArr;

            Object[] newObjArr = new Object[lastMessage * 2];
            System.arraycopy(sMessageArguments, 0, newObjArr, 0, lastMessage * 2);
            sMessageArguments = newObjArr;
        }
    }

    public static void handleDdjvuDocinfo(int status) {
        ++sLastMessage;
        checkResize(sLastMessage);
        sMessageTypes[sLastMessage] = MESSAGE_TYPE_DOCINFO;
        sMessageArguments[sLastMessage] = (Object) status;
    }

    public static void handleDdjvuPageinfo(int status) {
        ++sLastMessage;
        checkResize(sLastMessage);
        sMessageTypes[sLastMessage] = MESSAGE_TYPE_PAGEINFO;
        sMessageArguments[sLastMessage] = (Object) status;
    }

    private static class HandleMessages extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            Djvulibre.handleDjvuMessages(0);
            Djvulibre.handleMessages();
        }
    }

    private static class WaitForPage extends AsyncTask<Void, Void, Void> {
        private long mPageObj;

        public WaitForPage(long pageobj) {
            mPageObj = pageobj;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}

interface DjvulibreHandleCallback {
    void signalHandle();
}

interface DjvulibreErrorCallback {
    void signalError(int errorDescription);
}

interface DjvulibreDocinfoCallback {
    void signalDocinfo(int docinfoDescription);
}

interface DjvulibrePageinfoCallback {
    void signalPageinfo(int pageinfoDescription);
}

// relayoutCallback necessary or not ?

interface  DjvulibreRedisplayCallback {
    void signalRedisplay(int redisplayDescription);
}
