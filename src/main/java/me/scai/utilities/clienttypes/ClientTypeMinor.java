package me.scai.utilities.clienttypes;

public abstract class ClientTypeMinor {
    public static final int None = 0;

    public class API {
        public static final int UnitTest = 11;
        public static final int IntegTest = 12;
    }

    public class DesktopBrowser {
        public static final int CHROME = 21;
        public static final int FIREFOX = 22;
        public static final int SAFARI = 23;
        public static final int IE = 24;
        public static final int OPERA = 25;
    }

    public class MobileBrowser {
        public static final int CHROME = 31;
        public static final int FIREFOX = 32;
        public static final int SAFARI = 33;
        public static final int OPERA = 34;
    }
}
