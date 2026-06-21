package officerretraining;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

public class OfficerRetrainingPlugin extends BaseModPlugin {
    @Override
    public void onApplicationLoad() throws Exception {
        Settings.loadSettings();
        
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            LunaWrapper.init();
        }
    }
    
    @Override
    public void onGameLoad(boolean newGame) {
        URL url;
        try {
            url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        } catch (SecurityException e) {
            try {
                url = Paths.get("../mods/officer_retraining/jars/officer_retraining.jar").toUri().toURL();
            } catch (Exception ex) {
                Global.getLogger(OfficerRetrainingPlugin.class).error("Could not convert jar path to URL; exiting", ex);
                return;
            }
        }
        
        ReflectionEnabledClassLoader cl = new ReflectionEnabledClassLoader(url, this.getClass().getClassLoader());
        try {
            Class<?> clazz = cl.loadClass("officerretraining.UIInjectorScript");
            EveryFrameScript script = (EveryFrameScript) clazz.newInstance();
            Global.getSector().addTransientScript(script);
            Global.getLogger(OfficerRetrainingPlugin.class).info("Successfully loaded UIInjectorScript via custom classloader.");
        } catch (Throwable e) {
            Global.getLogger(OfficerRetrainingPlugin.class).error("Failure to load core script class; exiting", e);
        }
    }

    public static class ReflectionEnabledClassLoader extends URLClassLoader {
        public ReflectionEnabledClassLoader(URL url, ClassLoader parent) {
            super(new URL[]{url}, parent);
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            if (name.startsWith("java.lang.reflect")) {
                return ClassLoader.getSystemClassLoader().loadClass(name);
            }
            return super.loadClass(name);
        }

        @Override
        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            Class<?> c = this.findLoadedClass(name);
            if (c != null) {
                return c;
            }
            if (name.startsWith("officerretraining.")) {
                return this.findClass(name);
            }
            return super.loadClass(name, resolve);
        }
    }
}
