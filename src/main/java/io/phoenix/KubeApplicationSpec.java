package io.phoenix;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;

public class KubeApplicationSpec {
    private Deployment deploymentConfig;
    private Service serviceConfig;
    private HorizontalPodAutoscaler hpaConfig;

    public Deployment getDeploymentConfig() {
        return deploymentConfig;
    }

    public void setDeploymentConfig(Deployment deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
    }

    public Service getServiceConfig() {
        return serviceConfig;
    }

    public void setServiceConfig(Service serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public HorizontalPodAutoscaler getHpaConfig() {
        return hpaConfig;
    }

    public void setHpaConfig(HorizontalPodAutoscaler HPAConfig) {
        this.hpaConfig = HPAConfig;
    }
}