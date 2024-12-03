package io.phoenix.watcher;

import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.phoenix.KubeApplication;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;


public class KubeSubResourceWatcher implements AutoCloseable{
    private static final Logger log = Logger.getLogger(KubeSubResourceWatcher.class);
    private final KubernetesClient client;
    private final Reconciler<KubeApplication> reconciler;
    private final List<SharedIndexInformer<?>> informers = new ArrayList<>();

    public KubeSubResourceWatcher(KubernetesClient client, Reconciler<KubeApplication> reconciler) {
        this.client = client;
        this.reconciler = reconciler;
    }

    public void startWatchers() {
        watchDeployments();
        watchServices();
        watchHPAs();
    }

    private void watchDeployments() {
        informers.add(client.apps().deployments()
                .withLabel("app.kubernetes.io/managed-by", "kube-application-controller")
                .inform(new DeploymentHandler()));
    }

    private void watchServices() {
        informers.add(client.services()
                .withLabel("app.kubernetes.io/managed-by", "kube-application-controller")
                .inform(new ServiceHandler()));
    }

    private void watchHPAs() {
        informers.add(client.autoscaling().v2().horizontalPodAutoscalers()
                .withLabel("app.kubernetes.io/managed-by", "kube-application-controller")
                .inform(new HPAHandler()));
    }

    // Inner classes for handling events
    private class DeploymentHandler implements ResourceEventHandler<Deployment> {
        public DeploymentHandler () {
        }

        @Override
        public void onAdd(Deployment deployment) {
            String kubeAppResourceName = deployment.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName,  deployment.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating Deployment resource " +  deployment.getMetadata().getName());
            }
        }

        @Override
        public void onUpdate(Deployment oldDeployment, Deployment newDeployment) {
            String kubeAppResourceName = oldDeployment.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName, oldDeployment.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating Deployment resource " +  oldDeployment.getMetadata().getName());
            }
        }

        @Override
        public void onDelete(Deployment deployment, boolean deletedFinalStateUnknown) {
            String kubeAppResourceName = deployment.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName, deployment.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating Deployment resource " +  deployment.getMetadata().getName());
            }
        }
    }

    private class ServiceHandler implements ResourceEventHandler<Service> {
        @Override
        public void onAdd(Service service) {
            String kubeAppResourceName = service.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName, service.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating Service resource " +  service.getMetadata().getName());
            }
        }

        @Override
        public void onUpdate(Service oldService, Service newService) {
            String kubeAppResourceName = oldService.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName, oldService.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating Service resource " +  oldService.getMetadata().getName());
            }
        }

        @Override
        public void onDelete(Service service, boolean deletedFinalStateUnknown) {
            String kubeAppResourceName = service.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName, service.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating Service resource " +  service.getMetadata().getName());
            }
        }
    }

    private class HPAHandler implements ResourceEventHandler<HorizontalPodAutoscaler> {
        @Override
        public void onAdd(HorizontalPodAutoscaler hpa) {
            String kubeAppResourceName = hpa.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName,hpa.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating HPA resource " +  hpa.getMetadata().getName());
            }
        }

        @Override
        public void onUpdate(HorizontalPodAutoscaler oldHpa, HorizontalPodAutoscaler newHpa) {
            String kubeAppResourceName = oldHpa.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName, oldHpa.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating HPA resource " +  oldHpa.getMetadata().getName());
            }
        }

        @Override
        public void onDelete(HorizontalPodAutoscaler hpa, boolean deletedFinalStateUnknown) {
            String kubeAppResourceName = hpa.getMetadata().getLabels().get("phoenix.io/resource");
            try {
                triggerReconcile(kubeAppResourceName, hpa.getMetadata().getNamespace());
            } catch (Exception e) {
                log.warn("Error Updating HPA resource " +  hpa.getMetadata().getName());
            }
        }
    }

    private void triggerReconcile(String resourceName, String namespace) throws Exception {
        var resource = client.resources(KubeApplication.class).inNamespace(namespace)
                .withName(resourceName).get();

        if (resource == null) {
            log.info("No KubeApplication resources with name: " + resourceName);
            return;
        }

        try {
        reconciler.reconcile(resource, null);
        } catch (Exception e) {
            log.error("Failed to reconcile KubeApplication " + resource.getMetadata().getName(), e);
            // Optionally add retry logic here
        }

    }

    @Override
    public void close() {
        for (SharedIndexInformer<?> informer : informers) {
            try {
                informer.close();
            } catch (Exception e) {
                log.warn("Error while closing informer: " + informer.getClass().getSimpleName(), e);
            }
        }
    }
}
