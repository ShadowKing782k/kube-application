package io.phoenix;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1")
@Group("phoenix.io")
public class KubeApplication extends CustomResource<KubeApplicationSpec, KubeApplicationStatus> implements Namespaced { }