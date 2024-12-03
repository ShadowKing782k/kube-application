package io.phoenix.factory;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.phoenix.KubeApplication;


public class ResourceFactory {
    public KubernetesResource getResource(String resourceType, KubernetesClient client, KubeApplication resource) {
        switch (resourceType) {
            case "deployment":
                return new KubernetesDeployment(resource, client);
            case "service":
                return new KubernetesService(resource, client);
            case "hpa":
                return new KubernetesHPA(resource, client);
            default:
                throw new IllegalArgumentException("Unknown resource type: " + resourceType);
        }
    }
}