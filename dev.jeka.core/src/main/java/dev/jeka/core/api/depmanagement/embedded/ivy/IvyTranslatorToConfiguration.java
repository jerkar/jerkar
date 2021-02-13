package dev.jeka.core.api.depmanagement.embedded.ivy;

import dev.jeka.core.api.depmanagement.JkQualifiedDependencies;
import dev.jeka.core.api.depmanagement.publication.JkIvyConfigurationMapping;
import org.apache.ivy.core.module.descriptor.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

class IvyTranslatorToConfiguration {

    static Set<Configuration> toMasterConfigurations(JkQualifiedDependencies dependencies) {
        return dependencies.getQualifiedDependencies().stream()
                .map(qDep -> qDep.getQualifier())
                .map(JkIvyConfigurationMapping::of)
                .flatMap(cm -> cm.getLeft().stream())
                .map(confName -> new Configuration(confName))
                .collect(Collectors.toSet());
    }
}