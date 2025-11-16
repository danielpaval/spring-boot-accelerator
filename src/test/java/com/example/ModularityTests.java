package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.ApplicationModule;
import static org.assertj.core.api.Assertions.assertThat;

public class ModularityTests {

    @Test
    void onlyDemoDependsOnCommon() {
        ApplicationModules modules = ApplicationModules.of(SpringBootDemoApplication.class);
        ApplicationModule demoModule = modules.getModuleByName("demo").orElseThrow();
        ApplicationModule commonModule = modules.getModuleByName("common").orElseThrow();

        var demoDepStrings = demoModule.getDependencies(modules).stream().map(Object::toString).toList();
        var commonDepStrings = commonModule.getDependencies(modules).stream().map(Object::toString).toList();

        assertThat(demoDepStrings).anyMatch(s -> s.contains("common"));
        assertThat(commonDepStrings).noneMatch(s -> s.contains("demo"));
    }

}
