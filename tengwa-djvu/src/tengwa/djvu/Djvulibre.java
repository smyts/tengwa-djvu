/*
 *
 * Copyright and license info here
 *
 */

package tengwa.djvu;


public class Djvulibre {
    static {
        System.loadLibrary("djvulibre-native");
    }

    static native void contextCreate();
    static native void contextRelease();
    static native void cacheSetSize(int cachesize);
    static native int cacheGetSize();
    static native void cacheClear();
}
