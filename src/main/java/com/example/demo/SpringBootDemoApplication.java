package com.example.demo;

import com.example.demo.entity.CustomRevisionListener;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;

@SpringBootApplication
@ImportRuntimeHints(SpringBootDemoApplication.DemoRuntimeHints.class)
public class SpringBootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootDemoApplication.class, args);
	}

	static class DemoRuntimeHints implements RuntimeHintsRegistrar {
		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			// Register CustomRevisionListener for Envers
			hints.reflection().registerType(CustomRevisionListener.class,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

			// Register Envers classes for programmatic AuditReader access
			hints.reflection().registerType(AuditReader.class,
					MemberCategory.INVOKE_PUBLIC_METHODS);
			hints.reflection().registerType(DefaultRevisionEntity.class,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
					MemberCategory.INVOKE_PUBLIC_METHODS,
					MemberCategory.DECLARED_FIELDS);
		}
	}

}
