package com.example.demo;

import com.example.demo.entity.CustomRevisionListener;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.data.envers.repository.support.EnversRevisionRepositoryImpl;
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation;
import org.springframework.data.repository.history.RevisionRepository;

@SpringBootApplication
@ImportRuntimeHints(SpringBootDemoApplication.DemoRuntimeHints.class)
public class SpringBootDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootDemoApplication.class, args);
	}

	static class DemoRuntimeHints implements RuntimeHintsRegistrar {
		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.reflection().registerType(CustomRevisionListener.class,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
			hints.reflection().registerType(EnversRevisionRepositoryImpl.class,
					MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
					MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
					MemberCategory.INVOKE_PUBLIC_METHODS);
			hints.reflection().registerType(JpaRepositoryImplementation.class,
					MemberCategory.INVOKE_PUBLIC_METHODS);
			hints.reflection().registerType(RevisionRepository.class,
					MemberCategory.INVOKE_PUBLIC_METHODS);
		}
	}

}
