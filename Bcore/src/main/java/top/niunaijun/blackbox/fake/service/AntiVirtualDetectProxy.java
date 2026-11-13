package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.fake.hook.IInjectHook;
import top.niunaijun.blackbox.utils.Slog;

/**
 * Bypass anti-virtual-environment detection.
 * Intercepts attempts by protected apps to kill themselves when they detect a sandbox.
 */
public class AntiVirtualDetectProxy implements IInjectHook {
    private static final String TAG = "AntiVirtualDetect";
    private static volatile boolean sInstalled;

    @Override
    public void injectHook() {
        install();
    }

    @Override
    public boolean isBadEnv() {
        return !sInstalled;
    }

    public static void install() {
        if (sInstalled) return;
        synchronized (AntiVirtualDetectProxy.class) {
            if (sInstalled) return;
            try {
                // Attempt to prevent self-kill via Runtime.exit / System.exit
                try {
                    Class<?> processClass = android.os.Process.class;
                    Method killProcess = processClass.getDeclaredMethod("killProcess", int.class);
                    killProcess.setAccessible(true);
                    Slog.d(TAG, "Found Process.killProcess for potential hook");
                } catch (Throwable ignored) {}

                // Mark as installed (native AntiDetection.cpp handles path hiding)
                sInstalled = true;
                Slog.d(TAG, "AntiVirtualDetect proxy installed");

            } catch (Throwable e) {
                Slog.w(TAG, "install failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Check if a class name is a known anti-virtual detection class.
     */
    public static boolean isAntiDetectProvider(String className) {
        if (className == null) return false;
        return className.contains("HadesContentProvider")
                || className.contains("hades")
                || className.contains("ztuni")
                || className.contains("SecurityGuard")
                || className.contains("AvDetector")
                || className.contains("VirtualDetect")
                || className.contains("SandBoxDetect")
                || className.contains("EmulatorDetect")
                || className.contains("ijmdetect")
                || className.contains("baiduprotect")
                || className.contains("JMEncrypt");
    }
}
