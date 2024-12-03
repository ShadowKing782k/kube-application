package io.phoenix;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.phoenix.factory.KubernetesResource;
import io.phoenix.factory.ResourceFactory;
import io.phoenix.watcher.KubeSubResourceWatcher;
import org.jboss.logging.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@ControllerConfiguration(finalizerName = "phoenix.io/finalizer")
@MaxReconciliationInterval(interval = 300L, timeUnit = TimeUnit.SECONDS)
public class KubeApplicationReconciler implements Reconciler<KubeApplication> {

    private static final Logger log = Logger.getLogger(KubeApplicationReconciler.class);
    private final KubernetesClient client;
    private final KubeSubResourceWatcher subResourceWatcher;

    public KubeApplicationReconciler(KubernetesClient client) {
        this.client = client;
        this.subResourceWatcher = new KubeSubResourceWatcher(this.client, this);
        this.subResourceWatcher.startWatchers();
        Runtime.getRuntime().addShutdownHook(new Thread(subResourceWatcher::close));

    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(30);

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                log.info("Shutting down the shared executor...");
                executor.shutdown();
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    log.warn("Terminated all executors gracefully within time");
                }
            } catch (InterruptedException e) {
                log.warn("Could not terminate executors gracefully within time, forcefully shutting down");
                executor.shutdownNow();
            }
        }));
    }

    @Override
    public UpdateControl<KubeApplication> reconcile(KubeApplication resource, Context<KubeApplication> context) throws InterruptedException {
        ResourceFactory factory = new ResourceFactory();
        List<String> resourceTypes = Arrays.asList("deployment", "service", "hpa");
        log.info("Triggered Reconciliation for " + resource.getMetadata().getName());
        AtomicBoolean allResourcesReady = new AtomicBoolean(true);
        for (String resourceType : resourceTypes) {
            KubernetesResource resourceObj = factory.getResource(resourceType, client, resource);
            executor.submit(() -> {
                boolean resourceReady = resourceObj.apply();
                if (!resourceReady) {
                    allResourcesReady.set(false);
                }
            });
        }

        // Check and update status
        boolean allResourcesReadyStatus = allResourcesReady.get();
        if (!allResourcesReadyStatus) {
            resource.setStatus(new KubeApplicationStatus("All resources could not be created successfully"));
        } else {
            resource.setStatus(new KubeApplicationStatus("All resources created successfully"));
        }

        return UpdateControl.updateStatus(resource);
    }
}
