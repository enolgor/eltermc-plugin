package es.enolgor.eltermc.plugin;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class FeatureLoader {
	private final JavaPlugin plugin;
	private final ConfigurationSection config;
	private final Map<String, FeatureWrapper> featureInstances;
	
	public FeatureLoader(JavaPlugin plugin, ConfigurationSection config) {
		this.plugin = plugin;
		this.config = config;
		this.featureInstances = new HashMap<>();
	}
	
	public void loadFromInputStream(InputStream is) {
		new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)).lines().map(className -> {
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}).filter(className -> className != null)
		.forEach(cls -> {
			try {
				loadFeature(cls);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	
	public void loadFeature(Class<?> featureClass) throws IllegalClassFeatureException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, FeatureAlreadyRegisteredException, NoSuchFeatureException{
		EltermcAnnotations.Feature featureAnnotation = featureClass.getDeclaredAnnotation(EltermcAnnotations.Feature.class);
		if(featureAnnotation == null) throw new IllegalClassFeatureException(featureClass, "no feature annotation");
		String featureKey = featureAnnotation.value();
		if(featureKey == null || featureKey.trim().equals("")) throw new IllegalClassFeatureException(featureClass, "no feature key in annotation");
		if(this.featureInstances.containsKey(featureKey)) throw new FeatureAlreadyRegisteredException(featureKey);
		ConfigurationSection configSection = config.getConfigurationSection(featureKey);
		FeatureWrapper featureWrapper = new FeatureWrapper();
		featureWrapper.status = false;
		featureWrapper.instance = featureClass.getConstructor().newInstance();
		for(Field field : featureClass.getDeclaredFields()) {
			if(field.isAnnotationPresent(EltermcAnnotations.Inject.class)) {
				if(field.getType().isAssignableFrom(JavaPlugin.class)) {
					field.setAccessible(true);
					field.set(featureWrapper.instance, this.plugin);
				}
				if(field.getType().isAssignableFrom(ConfigurationSection.class)) {
					field.setAccessible(true);
					field.set(featureWrapper.instance, configSection);
				}
			}
		}
		for(Method method : featureClass.getDeclaredMethods()) {
			if(method.isAnnotationPresent(EltermcAnnotations.Feature.onEnable.class) && method.getParameterCount() == 0) {
				if(featureWrapper.onEnable != null) throw new IllegalClassFeatureException(featureClass, "only one enable annotated method allowed");
				featureWrapper.onEnable = method;
			}
			if(method.isAnnotationPresent(EltermcAnnotations.Feature.onDisable.class) && method.getParameterCount() == 0) {
				if(featureWrapper.onDisable != null) throw new IllegalClassFeatureException(featureClass, "only one disable annotated method allowed");
				featureWrapper.onDisable = method;
			}
			if(method.isAnnotationPresent(EltermcAnnotations.Feature.onCommand.class)) {
				if(featureWrapper.onCommand != null) throw new IllegalClassFeatureException(featureClass, "only one command annotated method allowed");
				if(method.getParameterCount() != 2) throw new IllegalClassFeatureException(featureClass, "command annotated method needs 2 parameters");
				if(method.getReturnType() != boolean.class) throw new IllegalClassFeatureException(featureClass, "command annotated method must return boolean value");
				Class<?> [] parameterTypes = method.getParameterTypes();
				if(!parameterTypes[0].isAssignableFrom(CommandSender.class)) throw new IllegalClassFeatureException(featureClass, "command annotated method needs CommandSender as 1st parameter");
				if(!parameterTypes[1].isAssignableFrom(String[].class)) throw new IllegalClassFeatureException(featureClass, "command annotated method needs String[] as 2nd parameter");
				featureWrapper.onCommand = method;
			}
		}
		if(featureWrapper.onEnable == null || featureWrapper.onDisable == null) throw new IllegalClassFeatureException(featureClass, "enable or disable annotated method without parameters not found");
		this.plugin.getLogger().info(String.format("loading feature %s", featureKey));
		this.featureInstances.put(featureKey, featureWrapper);
		if(configSection.getBoolean("enabled")) this.enable(featureKey);
	}
	
	public void enable(String featureKey) throws NoSuchFeatureException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(!this.featureInstances.containsKey(featureKey)) throw new NoSuchFeatureException(featureKey);
		FeatureWrapper featureWrapper = this.featureInstances.get(featureKey);
		if(featureWrapper.status) return;
		this.plugin.getLogger().info(String.format("enabling feature %s", featureKey));
		featureWrapper.status = true;
		featureWrapper.onEnable.invoke(featureWrapper.instance);
		this.plugin.getServer().broadcastMessage(String.format("eltermc:%s feature is enabled", featureKey));
	}
	
	public void disable(String featureKey) throws NoSuchFeatureException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(!this.featureInstances.containsKey(featureKey)) throw new NoSuchFeatureException(featureKey);
		FeatureWrapper featureWrapper = this.featureInstances.get(featureKey);
		if(!featureWrapper.status) return;
		this.plugin.getLogger().info(String.format("disabling feature %s", featureKey));
		featureWrapper.status = false;
		featureWrapper.onDisable.invoke(featureWrapper.instance);
		this.plugin.getServer().broadcastMessage(String.format("eltermc:%s feature is disabled", featureKey));
	}
	
	public boolean command(String featureKey, CommandSender sender, String [] args) throws NoSuchFeatureException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if(!this.featureInstances.containsKey(featureKey)) throw new NoSuchFeatureException(featureKey);
		FeatureWrapper featureWrapper = this.featureInstances.get(featureKey);
		if(!featureWrapper.status) return false;
		Object result = featureWrapper.onCommand.invoke(featureWrapper.instance, sender, args);
		return (boolean) result;
	}
	
	public boolean isEnabled(String featureKey) throws NoSuchFeatureException{
		if(!this.featureInstances.containsKey(featureKey)) throw new NoSuchFeatureException(featureKey);
		return this.featureInstances.get(featureKey).status;
	}
	
	public String [] loadedFeatures() {
		return this.featureInstances.keySet().toArray(new String[] {});
	}
	
	public static class FeatureWrapper {
		private Object instance;
		private boolean status;
		private Method onEnable, onDisable, onCommand;
	}
	
	@SuppressWarnings("serial")
	public static class IllegalClassFeatureException extends Exception {
		private String message;
		public IllegalClassFeatureException(Class<?> cls, String reason) {
			this.message = String.format("Illegal feature in class %s: %s", cls.getName(), reason);
		}
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	@SuppressWarnings("serial")
	public static class NoSuchFeatureException extends Exception {
		private String message;
		public NoSuchFeatureException(String feature) {
			this.message = String.format("Feature %s not found", feature);
		}
		@Override
		public String getMessage() {
			return message;
		}
	}
	
	@SuppressWarnings("serial")
	public static class FeatureAlreadyRegisteredException extends Exception {
		private String message;
		public FeatureAlreadyRegisteredException(String feature) {
			this.message = String.format("Feature %s already registered", feature);
		}
		@Override
		public String getMessage() {
			return message;
		}
	}
	
}
