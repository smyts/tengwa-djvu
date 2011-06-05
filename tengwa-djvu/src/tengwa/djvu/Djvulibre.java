/*
 *
 * Copyright and license info here
 *
 */

package tengwa.djvu;

public class Djvulibre {
     /*
      * Initial size of the message arrays
      */
    public static final int MESSAGES_LENGTH = 100;


    static {
        System.loadLibrary("djvulibre-native");
        messageTypes = new int[MESSAGES_LENGTH];
        messageArguments = new Object[MESSAGES_LENGTH];
        lastMessage = -1;
        errorCall = null;
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
     * Two arrays, the first one contains DDJVU message type for a message
     * to handle,the second one contains argument(s) for that particular message.
     * lastMessage is and index of the latest unhandled message.
     */
    public static int[] messageTypes;
    public static Object[] messageArguments;
    public static int lastMessage;

    /*
     * Callback objects to notify
     */
    public static DjvulibreErrorCallback errorCall;

    static native void contextCreate();
    static native void contextRelease();
    static native void cacheSetSize(int cachesize);
    static native int cacheGetSize();
    static native void cacheClear();
    static native int documentCreate(String filepath);
    static native void documentRelease();
    static native void handleDjvuMessages();

    public static void handleMessages() {
        handleDjvuMessages();

        for (int curId = 0; curId <= lastMessage; ++curId) {
            switch (messageTypes[curId]) {
                case MESSAGE_TYPE_ERROR:
                    if (errorCall != null)
                        errorCall.takeError(0);
                    break;
                case MESSAGE_TYPE_INFO:
                    break;
                case MESSAGE_TYPE_NEWSTREAM:
                    break;
                case MESSAGE_TYPE_DOCINFO:
                    break;
                case MESSAGE_TYPE_PAGEINFO:
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

        lastMessage = -1;
    }

    static void checkResize(int lastMessage) {
        if (lastMessage == messageTypes.length) {
            int[] newIntArr = new int[lastMessage * 2];
            System.arraycopy(messageTypes, 0, newIntArr, 0, lastMessage * 2);
            messageTypes = newIntArr;

            Object[] newObjArr = new Object[lastMessage * 2];
            System.arraycopy(messageArguments, 0, newObjArr, 0, lastMessage * 2);
            messageArguments = newObjArr;
        }
    }

    public static void handleDdjvuDocinfo(int status) {
        ++lastMessage;
        checkResize(lastMessage);
        messageTypes[lastMessage] = MESSAGE_TYPE_DOCINFO;
        messageArguments[lastMessage] = (Object) status;
    }
}

interface DjvulibreErrorCallback {
    void takeError(int errorDescription);
}
