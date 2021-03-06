package net.hockeyapp.android.metrics;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.support.v4.app.FrameMetricsAggregator;
import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;
import net.hockeyapp.android.utils.AsyncTaskUtils;
import net.hockeyapp.android.utils.HockeyLog;
import org.telegram.messenger.support.widget.helper.ItemTouchHelper.Callback;

public class Sender {
    static final String DEFAULT_ENDPOINT_URL = "https://gate.hockeyapp.net/v2/track";
    static final int DEFAULT_SENDER_CONNECT_TIMEOUT = 15000;
    static final int DEFAULT_SENDER_READ_TIMEOUT = 10000;
    static final int MAX_REQUEST_COUNT = 10;
    private static final String TAG = "HockeyApp-Metrics";
    private String mCustomServerURL;
    private AtomicInteger mRequestCount = new AtomicInteger(0);
    protected WeakReference<Persistence> mWeakPersistence;

    /* renamed from: net.hockeyapp.android.metrics.Sender$1 */
    class C06331 extends AsyncTask<Void, Void, Void> {
        C06331() {
        }

        protected Void doInBackground(Void... params) {
            Sender.this.sendAvailableFiles();
            return null;
        }
    }

    protected Sender() {
    }

    @SuppressLint({"StaticFieldLeak"})
    protected void triggerSending() {
        if (requestCount() < 10) {
            try {
                AsyncTaskUtils.execute(new C06331());
                return;
            } catch (Throwable e) {
                HockeyLog.error("Could not send events. Executor rejected async task.", e);
                return;
            }
        }
        HockeyLog.debug(TAG, "We have already 10 pending requests, not sending anything.");
    }

    @SuppressLint({"StaticFieldLeak"})
    protected void triggerSendingForTesting(final HttpURLConnection connection, final File file, final String persistedData) {
        if (requestCount() < 10) {
            this.mRequestCount.getAndIncrement();
            AsyncTaskUtils.execute(new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    Sender.this.send(connection, file, persistedData);
                    return null;
                }
            });
        }
    }

    protected void sendAvailableFiles() {
        if (getPersistence() != null) {
            File fileToSend = getPersistence().nextAvailableFileInDirectory();
            String persistedData = loadData(fileToSend);
            HttpURLConnection connection = createConnection();
            if (!(persistedData == null || connection == null)) {
                send(connection, fileToSend, persistedData);
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    protected void send(HttpURLConnection connection, File file, String persistedData) {
        if (connection != null && file != null && persistedData != null) {
            try {
                this.mRequestCount.getAndIncrement();
                logRequest(connection, persistedData);
                connection.connect();
                onResponse(connection, connection.getResponseCode(), persistedData, file);
            } catch (IOException e) {
                HockeyLog.debug(TAG, "Couldn't send data with " + e.toString());
                this.mRequestCount.getAndDecrement();
                if (getPersistence() != null) {
                    HockeyLog.debug(TAG, "Persisting because of IOException: We're probably offline.");
                    getPersistence().makeAvailable(file);
                }
            } catch (SecurityException e2) {
                HockeyLog.debug(TAG, "Couldn't send data with " + e2.toString());
                this.mRequestCount.getAndDecrement();
                if (getPersistence() != null) {
                    HockeyLog.debug(TAG, "Persisting because of SecurityException: Missing INTERNET permission or the user might have removed the internet permission.");
                    getPersistence().makeAvailable(file);
                }
            } catch (Exception e3) {
                HockeyLog.debug(TAG, "Couldn't send data with " + e3.toString());
                this.mRequestCount.getAndDecrement();
                if (getPersistence() != null) {
                    HockeyLog.debug(TAG, "Persisting because of unknown exception.");
                    getPersistence().makeAvailable(file);
                }
            }
        }
    }

    protected String loadData(File file) {
        String persistedData = null;
        if (!(getPersistence() == null || file == null)) {
            persistedData = getPersistence().load(file);
            if (persistedData != null && persistedData.isEmpty()) {
                getPersistence().deleteFile(file);
            }
        }
        return persistedData;
    }

    protected HttpURLConnection createConnection() {
        try {
            URL url;
            if (getCustomServerURL() == null) {
                url = new URL(DEFAULT_ENDPOINT_URL);
            } else {
                url = new URL(this.mCustomServerURL);
                if (url == null) {
                    url = new URL(DEFAULT_ENDPOINT_URL);
                }
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-json-stream");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            return connection;
        } catch (IOException e) {
            HockeyLog.error(TAG, "Could not open connection for provided URL with exception: ", e);
            return null;
        }
    }

    protected void onResponse(HttpURLConnection connection, int responseCode, String payload, File fileToSend) {
        this.mRequestCount.getAndDecrement();
        HockeyLog.debug(TAG, "response code " + Integer.toString(responseCode));
        if (isRecoverableError(responseCode)) {
            HockeyLog.debug(TAG, "Recoverable error (probably a server error), persisting data:\n" + payload);
            if (getPersistence() != null) {
                getPersistence().makeAvailable(fileToSend);
                return;
            }
            return;
        }
        if (getPersistence() != null) {
            getPersistence().deleteFile(fileToSend);
        }
        StringBuilder builder = new StringBuilder();
        if (isExpected(responseCode)) {
            try {
                InputStream inputStream = connection.getInputStream();
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                HockeyLog.error(TAG, "Could not close input stream", e);
            }
            triggerSending();
            return;
        }
        onUnexpected(connection, responseCode, builder);
    }

    protected boolean isRecoverableError(int responseCode) {
        return Arrays.asList(new Integer[]{Integer.valueOf(408), Integer.valueOf(429), Integer.valueOf(500), Integer.valueOf(503), Integer.valueOf(FrameMetricsAggregator.EVERY_DURATION)}).contains(Integer.valueOf(responseCode));
    }

    protected boolean isExpected(int responseCode) {
        return Callback.DEFAULT_DRAG_ANIMATION_DURATION <= responseCode && responseCode <= 203;
    }

    protected void onUnexpected(HttpURLConnection connection, int responseCode, StringBuilder builder) {
        String message = String.format(Locale.ROOT, "Unexpected response code: %d", new Object[]{Integer.valueOf(responseCode)});
        builder.append(message);
        builder.append("\n");
        HockeyLog.error(TAG, message);
        readResponse(connection, builder);
    }

    private void logRequest(HttpURLConnection connection, String payload) throws IOException, SecurityException {
        Writer writer = null;
        if (!(connection == null || payload == null)) {
            try {
                HockeyLog.debug(TAG, "Sending payload:\n" + payload);
                HockeyLog.debug(TAG, "Using URL:" + connection.getURL().toString());
                writer = getWriter(connection);
                writer.write(payload);
                writer.flush();
            } catch (Throwable th) {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        HockeyLog.error(TAG, "Couldn't close writer with: " + e.toString());
                    }
                }
            }
        }
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e2) {
                HockeyLog.error(TAG, "Couldn't close writer with: " + e2.toString());
            }
        }
    }

    protected void readResponse(HttpURLConnection connection, StringBuilder builder) {
        StringBuilder buffer = new StringBuilder();
        InputStream inputStream = null;
        try {
            String result;
            inputStream = connection.getErrorStream();
            if (inputStream == null) {
                inputStream = connection.getInputStream();
            }
            if (inputStream != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                while (true) {
                    String inputLine = br.readLine();
                    if (inputLine == null) {
                        break;
                    }
                    buffer.append(inputLine);
                }
                result = buffer.toString();
            } else {
                result = connection.getResponseMessage();
            }
            if (TextUtils.isEmpty(result)) {
                HockeyLog.verbose(TAG, "Couldn't log response, result is null or empty string");
            } else {
                HockeyLog.verbose(result);
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    HockeyLog.error(TAG, e.toString());
                }
            }
        } catch (IOException e2) {
            HockeyLog.error(TAG, e2.toString());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22) {
                    HockeyLog.error(TAG, e22.toString());
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e222) {
                    HockeyLog.error(TAG, e222.toString());
                }
            }
        }
    }

    @TargetApi(19)
    protected Writer getWriter(HttpURLConnection connection) throws IOException {
        if (VERSION.SDK_INT < 19) {
            return new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
        }
        connection.addRequestProperty("Content-Encoding", "gzip");
        connection.setRequestProperty("Content-Type", "application/x-json-stream");
        return new OutputStreamWriter(new GZIPOutputStream(connection.getOutputStream(), true), "UTF-8");
    }

    protected Persistence getPersistence() {
        if (this.mWeakPersistence != null) {
            return (Persistence) this.mWeakPersistence.get();
        }
        return null;
    }

    protected void setPersistence(Persistence persistence) {
        this.mWeakPersistence = new WeakReference(persistence);
    }

    protected int requestCount() {
        return this.mRequestCount.get();
    }

    protected String getCustomServerURL() {
        return this.mCustomServerURL;
    }

    protected void setCustomServerURL(String customServerURL) {
        this.mCustomServerURL = customServerURL;
    }
}
