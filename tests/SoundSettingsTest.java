import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

import engine.Core;
import engine.FileManager;
import engine.Frame;
import engine.soundEngine.AudioBackend;
import engine.soundEngine.Sound;
import engine.soundEngine.SoundManager;
import engine.soundEngine.SoundSettings;
import screen.Screen;
import screen.SettingsScreen;

/** HW-51 JUnit scenarios, each run with isolated classes and save files. */
public final class SoundSettingsTest {
    private static final FileManager FILES = Core.getFileManager();

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"firstRun", "corruptFiles", "invalidValues", "boundaries",
            "ioFailures", "runtimeVolumes", "settingsExitAndRestart"})
    void persistsSoundSettings(String scenario, @TempDir Path root) throws Exception {
        Path classes = root.resolve("classes");
        for (Class<?> type : new Class<?>[]{Core.class, SoundSettingsTest.class}) {
            Path source = Paths.get(type.getProtectionDomain().getCodeSource().getLocation().toURI());
            try (Stream<Path> files = Files.walk(source)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    Path target = classes.resolve(source.relativize(file));
                    Files.createDirectories(target.getParent());
                    Files.copy(file, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        assertEquals(0, runScenario(root, scenario), Files.readString(root.resolve(scenario + ".log")));
    }

    private static int runScenario(Path root, String scenario) throws Exception {
        Path resources = Paths.get(Core.class.getResource("/graphics").toURI()).getParent();
        Process child = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java")
                .toString(), "-Djava.awt.headless=true", "-cp",
                root.resolve("classes") + File.pathSeparator + resources,
                "SoundSettingsTest", root.toString(), scenario).directory(root.toFile())
                .redirectErrorStream(true).redirectOutput(root.resolve(scenario + ".log").toFile()).start();
        try {
            check(child.waitFor(30, TimeUnit.SECONDS), "Scenario timed out: " + scenario);
            return child.exitValue();
        } finally { child.destroyForcibly(); }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static Field field(Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static Object call(Object target, String name, Class<?>[] types,
                               Object... args) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name, types);
        method.setAccessible(true);
        return method.invoke(target, args);
    }

    private static void volumes(float bgm, float sfx) {
        SoundSettings loaded = FILES.loadSoundSettings();
        check(loaded.getBgmVolume() == bgm, "Loaded BGM " + bgm);
        check(loaded.getSfxVolume() == sfx, "Loaded SFX " + sfx);
    }

    private static void write(Path save, String text) throws IOException {
        Files.write(save, text.getBytes(StandardCharsets.ISO_8859_1));
    }

    /** Records the exact volumes supplied to audio playback without a device. */
    private static final class RecordingBackend implements AudioBackend {
        private float bgm = -1, sfx = -1;
        public void preload(Sound sound) { }
        public void playSfx(Sound sound, float volume) { sfx = volume; }
        public void playBgm(Sound sound, float volume) { bgm = volume; }
        public void stopBgm() { }
        public boolean isBgmPlaying() { return false; }
        public void setBgmVolume(float volume) { bgm = volume; }
        public void dispose() { }
    }

    /** Constructor is bypassed so no native window or display is needed. */
    @SuppressWarnings("serial")
    public static final class OffscreenFrame extends Frame {
        private OffscreenFrame() { super(448, 520); }
        @Override public Graphics getGraphics() {
            return new BufferedImage(448, 520, BufferedImage.TYPE_INT_RGB).getGraphics();
        }
        @Override public Insets getInsets() { return new Insets(0, 0, 0, 0); }
    }

    private static SettingsScreen screen() throws Exception {
        // Match the existing offscreen testing approach without changing production code.
        Class<?> unsafeType = Class.forName("sun.misc.Unsafe");
        Object unsafe = field(unsafeType, "theUnsafe").get(null);
        Frame frame = (Frame) unsafeType.getMethod("allocateInstance", Class.class)
                .invoke(unsafe, OffscreenFrame.class);
        Core.getDrawManager().setFrame(frame);
        return new SettingsScreen(448, 520, 60);
    }

    private static RecordingBackend record(SoundManager manager) throws Exception {
        RecordingBackend backend = new RecordingBackend();
        field(SoundManager.class, "backend").set(manager, backend);
        return backend;
    }

    private static void percent(SettingsScreen screen, Sound.Category category,
                                int expected) throws Exception {
        check((Integer) call(screen, "getVolumePercent",
                new Class<?>[]{Sound.Category.class}, category) == expected,
                "Settings percentage " + category + " = " + expected);
    }

    private static void exit(SettingsScreen screen) throws Exception {
        field(engine.Cooldown.class, "time").set(field(Screen.class, "inputDelay").get(screen), 0L);
        boolean[] keys = (boolean[]) field(engine.InputManager.class, "keys").get(null);
        keys[KeyEvent.VK_ESCAPE] = true;
        try { check(screen.run() == 1, "ESC leaves Settings even if saving fails"); }
        finally { keys[KeyEvent.VK_ESCAPE] = false; }
    }

    /** Subprocess entry point; assertions here propagate to the named JUnit case. */
    public static void main(String[] args) throws Exception {
        Core.getLogger().setUseParentHandlers(false);
        Path root = Paths.get(SoundSettingsTest.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).getParent().toRealPath();
        check(args.length > 0 && root.equals(Paths.get(args[0]).toRealPath()),
                "Explicit isolated save directory must match classes parent");
        Path save = root.resolve("sound-settings");
        Path temp = root.resolve("sound-settings.tmp");
        if (args.length > 1 && args[1].equals("restart")) {
            SoundManager manager = SoundManager.getInstance();
            RecordingBackend backend = record(manager);
            SettingsScreen settings = screen();
            percent(settings, Sound.Category.BGM, 60);
            percent(settings, Sound.Category.SFX, 25);
            manager.playBgm(Sound.BGM_TITLE);
            manager.playSfx(Sound.MENU_MOVE);
            check(backend.bgm == 0.6f && backend.sfx == 0.25f,
                    "Fresh JVM sends saved volumes to audio backend");
            return;
        }
        check(!Files.exists(save) && !Files.exists(temp), "Fresh save directory required");
        SoundManager manager = SoundManager.getInstance();
        switch (args[1]) {
        case "firstRun":
            volumes(0.5f, 0.7f);
            check(manager.getVolume(Sound.Category.BGM) == 0.5f
                    && manager.getVolume(Sound.Category.SFX) == 0.7f, "First-run manager defaults");
            check(!Files.exists(save), "Loading defaults does not create a save");
            break;
        case "corruptFiles":
            write(save, "bgmVolume=\\uZZZZ\n");
            volumes(0.5f, 0.7f);
            write(save, "garbage\n");
            volumes(0.5f, 0.7f);
            break;
        case "invalidValues":
            for (String bad : new String[]{"", "oops", "NaN", "Infinity", "-Infinity",
                    "1e100", "-0.01", "1.01"}) {
                write(save, "bgmVolume=" + bad + "\nsfxVolume=0.25\n");
                volumes(0.5f, 0.25f);
                write(save, "bgmVolume=0.2\nsfxVolume=" + bad + "\n");
                volumes(0.2f, 0.7f);
            }
            write(save, "sfxVolume=0.25\n");
            volumes(0.5f, 0.25f);
            write(save, "bgmVolume=0.2\n");
            volumes(0.2f, 0.7f);
            break;
        case "boundaries":
            FILES.saveSoundSettings(new SoundSettings(0, 1));
            volumes(0, 1);
            FILES.saveSoundSettings(new SoundSettings(1, 0));
            volumes(1, 0);
            check(!Files.exists(temp), "Successful save moves temporary file");
            break;
        case "ioFailures":
            FILES.saveSoundSettings(new SoundSettings(0.2f, 0.3f));
            byte[] previous = Files.readAllBytes(save);
            Files.createDirectory(temp);
            FILES.saveSoundSettings(new SoundSettings(0.8f, 0.9f));
            check(Arrays.equals(previous, Files.readAllBytes(save)), "Failed write preserves save");
            Files.delete(temp);
            Files.delete(save);
            Files.createDirectory(save);
            write(save.resolve("blocker"), "keep");
            volumes(0.5f, 0.7f);
            FILES.saveSoundSettings(new SoundSettings(0.2f, 0.3f));
            check(Files.exists(save.resolve("blocker")), "Failed replacement preserves target");
            break;
        case "runtimeVolumes":
            RecordingBackend backend = record(manager);
            for (Sound.Category category : Sound.Category.values()) {
                for (float input : new float[]{-10, 0, 1, 10, Float.NaN,
                        Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY}) {
                    manager.setVolume(category, input);
                    float actual = manager.getVolume(category);
                    check(Float.isFinite(actual) && actual >= 0 && actual <= 1,
                            "Safe runtime volume " + category + ": " + input);
                    if (category == Sound.Category.BGM) check(backend.bgm == actual, "Live BGM volume");
                    else {
                        manager.playSfx(Sound.MENU_MOVE);
                        check(backend.sfx == actual, "SFX playback volume");
                    }
                }
            }
            break;
        case "settingsExitAndRestart":
            record(manager);
            SettingsScreen settings = screen();
            call(settings, "adjustSelectedVolume", new Class<?>[]{int.class}, 10);
            field(Core.getInputManager().getClass(), "mouseX").set(Core.getInputManager(), 156);
            field(Core.getInputManager().getClass(), "mouseY").set(Core.getInputManager(), 253);
            call(settings, "updateMouseVolume", new Class<?>[]{boolean.class}, true);
            call(settings, "updateMouseVolume", new Class<?>[]{boolean.class}, false);
            percent(settings, Sound.Category.BGM, 60);
            percent(settings, Sound.Category.SFX, 25);
            exit(settings);
            volumes(0.6f, 0.25f);
            percent(screen(), Sound.Category.SFX, 25);
            check(runScenario(root, "restart") == 0,
                    "Independent JVM reloads UI and playback volumes: " + Files.readString(root.resolve("restart.log")));
            // Saving failures must not prevent ESC from leaving Settings.
            Files.createDirectory(temp);
            exit(screen());
            Files.delete(temp);
            break;
        default: throw new IllegalArgumentException(args[1]);
        }
    }
}
