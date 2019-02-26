package es.enolgor.eltermc.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class EltermcAnnotations {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface Feature {
		public String value();
		
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.METHOD)
		public static @interface onEnable {
		}
		
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.METHOD)
		public static @interface onDisable {
		}
		
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.METHOD)
		public static @interface onCommand {
		}
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public static @interface Inject{
	}
}
