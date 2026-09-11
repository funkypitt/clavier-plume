/*
 * TypoLogger — local-only logging of (typed, committed) pairs to JSONL.
 *
 * Captures real-world typo→correction events from the user's normal typing.
 * Used to build a personal dataset for training a custom pt-BR FUTO model.
 *
 * Design:
 *  - Singleton, lazily initialised once the IME service has a Context.
 *  - Async writer thread + bounded BlockingQueue → never blocks the IME thread.
 *  - JSONL output to ${context.getExternalFilesDir(null)}/typo_log.jsonl
 *    On Android 11+ this is the per-app sandbox; user-visible but app-private.
 *  - Rotates when file exceeds MAX_BYTES (50 MB) → typo_log.jsonl.1
 *  - Filters: only logs locales starting with the configured prefix (default "pt").
 *  - No network, no analytics, no telemetry. Local file only.
 *
 * Privacy: all data stays on the device. Pull via:
 *   adb pull /sdcard/Android/data/<pkg>/files/typo_log.jsonl
 *
 * Add to: java/src/org/futo/inputmethod/latin/utils/TypoLogger.java
 * Instantiated from: InputLogic.java (see InputLogic.diff)
 */
package org.futo.inputmethod.latin.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class TypoLogger {
    private static final String TAG = "TypoLogger";
    private static final String FILENAME = "typo_log.jsonl";
    private static final long MAX_BYTES = 50L * 1024 * 1024;     // 50 MB
    private static final int QUEUE_CAPACITY = 256;
    private static final String LOCALE_PREFIX = "";               // Plume : toutes les langues (fr, en)

    private static volatile TypoLogger sInstance;

    private final File mLogFile;
    private final BlockingQueue<String> mQueue;
    private final Thread mWriterThread;
    private final SimpleDateFormat mTsFormat;

    private TypoLogger(final Context context) {
        final File dir = context.getExternalFilesDir(null);
        if (dir != null && !dir.exists()) dir.mkdirs();
        mLogFile = new File(dir, FILENAME);
        mQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        mTsFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        mTsFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        mWriterThread = new Thread(this::runWriterLoop, "TypoLoggerWriter");
        mWriterThread.setDaemon(true);
        mWriterThread.start();
        Log.i(TAG, "TypoLogger initialised: " + mLogFile.getAbsolutePath());
    }

    /** Idempotent init. Safe to call from any thread. */
    public static void init(final Context context) {
        if (sInstance == null) {
            synchronized (TypoLogger.class) {
                if (sInstance == null) {
                    sInstance = new TypoLogger(context.getApplicationContext());
                }
            }
        }
    }

    /**
     * Log an autocorrect or manual-pick event.
     *
     * @param typed the user's typed string (what they pressed)
     * @param committed the word that ended up in the text field
     * @param locale current IME locale (e.g. "pt-BR")
     * @param source either "auto_correct" or "manual_pick"
     */
    private static final java.util.concurrent.atomic.AtomicInteger sWords = new java.util.concurrent.atomic.AtomicInteger();
    private static final int STATS_EVERY = 200;

    /** Compte les mots validés ; toutes les STATS_EVERY validations, une ligne « stats »
     *  (avec l'état du hit-testing) sert de dénominateur à la mesure. */
    public static void countWord() {
        final TypoLogger inst = sInstance;
        if (inst == null) return;
        if (sWords.incrementAndGet() % STATS_EVERY != 0) return;
        try {
            final String line = "{\"ts\":\"" + inst.mTsFormat.format(new Date()) + "\",\"src\":\"stats\",\"words\":"
                    + STATS_EVERY + ",\"boost\":" + org.futo.inputmethod.latin.plume.PlumeExperiment.getLastBoostState() + "}\n";
            inst.mQueue.offer(line);
        } catch (final Exception e) {
            Log.w(TAG, "stats", e);
        }
    }

    public static void log(final String typed, final String committed,
                           final String locale, final String source) {
        final TypoLogger inst = sInstance;
        if (inst == null) return;                     // not initialised yet — drop
        if (typed == null || committed == null) return;
        if (typed.length() < 2) return;                // too short, noise
        if (typed.equals(committed)) return;           // no correction
        if (locale == null || !locale.startsWith(LOCALE_PREFIX)) return;
        inst.enqueue(typed, committed, locale, source);
    }

    private void enqueue(final String typed, final String committed,
                         final String locale, final String source) {
        try {
            final String line = buildJsonLine(typed, committed, locale, source);
            // offer() is non-blocking; drop on full queue (better than blocking IME)
            if (!mQueue.offer(line)) {
                Log.w(TAG, "queue full, dropping log entry");
            }
        } catch (final Exception e) {
            Log.w(TAG, "enqueue failed", e);
        }
    }

    private String buildJsonLine(final String typed, final String committed,
                                  final String locale, final String source) {
        final StringBuilder sb = new StringBuilder(192);
        sb.append('{');
        sb.append("\"ts\":\"").append(mTsFormat.format(new Date())).append("\",");
        sb.append("\"typed\":\"").append(escape(typed)).append("\",");
        sb.append("\"committed\":\"").append(escape(committed)).append("\",");
        sb.append("\"locale\":\"").append(escape(locale)).append("\",");
        sb.append("\"src\":\"").append(escape(source)).append("\",");
        sb.append("\"boost\":").append(org.futo.inputmethod.latin.plume.PlumeExperiment.getLastBoostState());
        sb.append('}').append('\n');
        return sb.toString();
    }

    private static String escape(final String s) {
        final StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            switch (c) {
                case '"':  out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        out.append(String.format(Locale.US, "\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
            }
        }
        return out.toString();
    }

    private void runWriterLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final String line = mQueue.poll(5, TimeUnit.SECONDS);
                if (line == null) continue;
                writeLine(line);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (final Exception e) {
                Log.w(TAG, "writer loop error", e);
            }
        }
    }

    private void writeLine(final String line) {
        try {
            if (mLogFile.exists() && mLogFile.length() > MAX_BYTES) {
                rotate();
            }
            try (final FileWriter fw = new FileWriter(mLogFile, true)) {
                fw.write(line);
            }
        } catch (final IOException e) {
            Log.w(TAG, "writeLine failed", e);
        }
    }

    private void rotate() {
        final File rotated = new File(mLogFile.getParentFile(), FILENAME + ".1");
        if (rotated.exists()) rotated.delete();
        if (!mLogFile.renameTo(rotated)) {
            Log.w(TAG, "rotate failed, truncating instead");
            mLogFile.delete();
        }
    }
}
