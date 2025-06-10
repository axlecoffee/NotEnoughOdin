package io.github.moulberry.notenoughupdates;

import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ModuleReloaderold {

	private static ClassLoader ctLoader;
	private static Object modCoreInstance;
	private static Class<?> modCoreClass;

	/**
	 * Loads Odin and calls init. Only called once.
	 */
	public static void reloadModules(FMLInitializationEvent preEvent) {
		try {
			ensureLoader();
			Thread.currentThread().setContextClassLoader(ctLoader);

			if (modCoreInstance == null) {
				modCoreClass = ctLoader.loadClass("io.github.moulberry.notenoughupdates.odinclient.odinclient.ModCore");
				modCoreInstance = modCoreClass.getDeclaredConstructor().newInstance();
				System.out.println("[AXLE] Odin Injection preInit: " + modCoreInstance);
			}

			Method preInit = modCoreClass.getMethod("init", FMLInitializationEvent.class);
			System.out.println("[AXLE] Odin Injection preInit method: " + preInit);
			preInit.invoke(modCoreInstance, preEvent);

			// Debug: show which loader loaded kotlinx.coroutines.SupervisorKt
			try {
				Class<?> supervisorKt = ctLoader.loadClass("kotlinx.coroutines.SupervisorKt");
				System.out.println("[AXLE] kotlinx.coroutines.SupervisorKt loaded by: " + supervisorKt.getClassLoader());
			} catch (Throwable t) {
				System.err.println("[AXLE] Could not load kotlinx.coroutines.SupervisorKt: " + t);
			}

			System.out.println("[AXLE] Invoked.");
		} catch (Exception e) {
			throw new RuntimeException("[AXLE] odin injection Init failed", e);
		}
	}

	/**
	 * Calls postInit on the already loaded Odin instance.
	 */
	public static void reloadModules(FMLPostInitializationEvent initEvent) {
		try {
			ensureLoader();
			Thread.currentThread().setContextClassLoader(ctLoader);
			if (modCoreInstance == null) return;
			Method postInit = modCoreClass.getMethod("postInit", FMLPostInitializationEvent.class);
			postInit.invoke(modCoreInstance, initEvent);
		} catch (Exception e) {
			throw new RuntimeException("[AXLE] Odin Injection postinit failed", e);
		}
	}

	public static void reloadModules(FMLLoadCompleteEvent loadCompleteEvent) {
		try {
			ensureLoader();
			Thread.currentThread().setContextClassLoader(ctLoader);
			if (modCoreInstance == null) return;
			Method loadComplete = modCoreClass.getMethod("loadComplete", FMLLoadCompleteEvent.class);
			loadComplete.invoke(modCoreInstance, loadCompleteEvent);
		} catch (Exception e) {
			throw new RuntimeException("[AXLE] Odin Injection load complete failed", e);
		}
	}

	public static void reloadModules(TickEvent.ClientTickEvent tickEvent) {
		try {
			ensureLoader();
			Thread.currentThread().setContextClassLoader(ctLoader);
			if (modCoreInstance == null) return;
			Method tick = modCoreClass.getMethod("onTick", TickEvent.ClientTickEvent.class);
			tick.invoke(modCoreInstance, tickEvent);
		} catch (Exception e) {
			throw new RuntimeException("[AXLE] Odin Injection client tick failed", e);
		}
	}

	private static void ensureLoader() throws Exception {
		if (ctLoader != null) return;

		InputStream jarStream = ModuleReloaderold.class.getResourceAsStream("/chattriggers-all.jar");
		if (jarStream == null)
			throw new IllegalStateException("'/chattriggers-all.jar' not found in resources!");

		Map<String, byte[]> classBytes = new HashMap<>();
		try (JarInputStream jis = new JarInputStream(jarStream)) {
			JarEntry entry;
			ByteArrayOutputStream buf = new ByteArrayOutputStream(4096);
			byte[] tmp = new byte[4096];
			while ((entry = jis.getNextJarEntry()) != null) {
				if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
					buf.reset();
					int r;
					while ((r = jis.read(tmp)) != -1) buf.write(tmp, 0, r);
					String name = entry.getName()
														 .replace('/', '.')
														 .replaceAll("\\.class$", "");
					classBytes.put(name, buf.toByteArray());
				}
			}
		}

		ClassLoader parent = ModuleReloaderold.class.getClassLoader();
		ctLoader = new ClassLoader(parent) {
			@Override
			protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				if (
					name.startsWith("net.minecraft.") ||
						name.startsWith("net.minecraftforge.") ||
						name.startsWith("kotlin.") ||
						name.startsWith("org.jetbrains.") ||
						name.startsWith("kotlinx.")
				) {
					System.out.println("[AXLE] Loading class " + name + " from parent ClassLoader");
					return super.loadClass(name, resolve);
				}
				try {
					return findClass(name);
				} catch (ClassNotFoundException e) {
					return super.loadClass(name, resolve);
				}
			}

			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				try {
					return getParent().loadClass(name);
				} catch (ClassNotFoundException e) {
					byte[] b = classBytes.get(name);
					if (b != null) {
						return defineClass(name, b, 0, b.length);
					}
					throw new ClassNotFoundException(name);
				}
			}
		};
	}
}
