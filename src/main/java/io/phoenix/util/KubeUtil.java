package io.phoenix.util;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.phoenix.KubeApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class KubeUtil {
    static final String MANAGED_BY_KEY = "app.kubernetes.io/managed-by";
    static final String MANAGED_BY_VALUE = "kube-application-controller";
    static final String MANAGED_BY_RESOURCE = "phoenix.io/resource";
    public static boolean waitForReadiness(Supplier<Boolean> isReady, String message, KubeApplication resource, KubernetesClient client, long timeoutMs) {
        long startTime = System.currentTimeMillis();
        while (!isReady.get()) {
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                generateEvent(resource,client, message + " timed out","Warning", "SubResourceTimeout");
                return false;
            }
            generateEvent(resource,client, message,"Normal", "SubResourceSuccessful");

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    public static void generateEvent(KubeApplication resource, KubernetesClient client, String message, String type, String reason) {
        var refObject = new ObjectReference();
        refObject.setName(resource.getMetadata().getName());
        refObject.setNamespace(resource.getMetadata().getNamespace());
        refObject.setKind(resource.getKind());
        refObject.setApiVersion(resource.getApiVersion());
        Event event = new EventBuilder()
                .withNewMetadata()
                .withName("event-" + resource.getMetadata().getName() + "-" + System.currentTimeMillis())
                .withNamespace(resource.getMetadata().getNamespace())
                .endMetadata()
                .withMessage(message)
                .withReason(reason)
                .withInvolvedObject(refObject)
                .withType(type)  // Event type: "Normal" or "Warning"
                .build();

        client.v1().events().resource(event).serverSideApply();
    }

    public static ObjectMeta updateMetadata(ObjectMeta metaData, KubeApplication resource) {
        var labelMap = metaData.getLabels();
        labelMap.put(MANAGED_BY_KEY, MANAGED_BY_VALUE);
        labelMap.put(MANAGED_BY_RESOURCE, resource.getMetadata().getName());
        metaData.setLabels(labelMap);
        return metaData;
    }

    public static List<OwnerReference> getOwnerRef(KubeApplication resource) {
        var ownerReferences = new ArrayList<OwnerReference>();
        OwnerReference ownerRef = new OwnerReference();
        ownerRef.setApiVersion(resource.getApiVersion());
        ownerRef.setKind(resource.getKind());
        ownerRef.setName(resource.getMetadata().getName());
        ownerRef.setUid(resource.getMetadata().getUid());
        ownerReferences.add(ownerRef);
        return ownerReferences;
    }
}
