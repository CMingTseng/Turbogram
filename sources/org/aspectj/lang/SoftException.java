package org.aspectj.lang;

import com.google.devtools.build.android.desugar.runtime.ThrowableExtension;
import java.io.PrintStream;
import java.io.PrintWriter;

public class SoftException extends RuntimeException {
    private static final boolean HAVE_JAVA_14;
    Throwable inner;

    static {
        boolean java14 = false;
        try {
            Class.forName("java.nio.Buffer");
            java14 = true;
        } catch (Throwable th) {
        }
        HAVE_JAVA_14 = java14;
    }

    public SoftException(Throwable inner) {
        this.inner = inner;
    }

    public Throwable getWrappedThrowable() {
        return this.inner;
    }

    public Throwable getCause() {
        return this.inner;
    }

    public void printStackTrace() {
        ThrowableExtension.printStackTrace((Throwable) this, System.err);
    }

    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);
        Throwable _inner = this.inner;
        if (!HAVE_JAVA_14 && _inner != null) {
            stream.print("Caused by: ");
            ThrowableExtension.printStackTrace(_inner, stream);
        }
    }

    public void printStackTrace(PrintWriter stream) {
        super.printStackTrace(stream);
        Throwable _inner = this.inner;
        if (!HAVE_JAVA_14 && _inner != null) {
            stream.print("Caused by: ");
            ThrowableExtension.printStackTrace(_inner, stream);
        }
    }
}
