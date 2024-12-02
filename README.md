
---

# kube-application Helm Chart

This Helm chart installs the `kube-application` operator, a Kubernetes operator built with Java and Quarkus. The operator manages custom resources (`kubeapplications`) and automatically creates and manages related deployments, services, and horizontal pod autoscalers.

## Prerequisites

- Kubernetes 1.21+
- Helm 3.5+
- Cluster-wide permissions to install RBAC resources.

## Installation

### Add the Helm Repository

If hosting this Helm chart in a repository, you can add it to Helm:

```bash
helm repo add my-charts https://example.com/helm-charts
helm repo update
```

Replace `https://example.com/helm-charts` with the URL of your Helm chart repository.

### Install the Chart

Install the chart in the `default` namespace or specify a namespace:

```bash
helm install kube-application my-charts/kube-application --namespace kube-application --create-namespace
```

- `kube-application` is the release name.
- `my-charts/kube-application` refers to the chart.
- `--namespace kube-application` creates the namespace `kube-application`.

### Verify the Installation

Check the deployed resources:

```bash
kubectl get all -n kube-application
```

Confirm the operator pod is running:

```bash
kubectl logs -l app.kubernetes.io/name=kube-application -n kube-application
```

## Uninstallation

To uninstall the chart and all associated resources:

```bash
helm uninstall kube-application --namespace kube-application
```

If you created the namespace for this chart, delete it as well:

```bash
kubectl delete namespace kube-application
```

## Configuration

The chart provides minimal configuration. You can override the default values using the `--set` flag or a custom `values.yaml` file.

### Example: Custom Image Tag

```bash
helm install kube-application my-charts/kube-application --set image.tag=1.1.0
```

### Example: Custom Values File

```bash
helm install kube-application my-charts/kube-application -f custom-values.yaml
```

## Values

| Key                    | Default                | Description                                |
|------------------------|------------------------|--------------------------------------------|
| `replicaCount`         | `1`                    | Number of replicas for the operator        |
| `image.repository`     | `docker.io/tejeshavadanam/kube-application` | Operator image repository                  |
| `image.tag`            | `1.0.0`                | Operator image tag                         |
| `image.pullPolicy`     | `Always`               | Image pull policy                          |
| `serviceAccount.name`  | `kube-application`     | Name of the service account                |
| `rbac.create`          | `true`                 | Whether to create RBAC resources           |

## Notes

- The operator is implemented using Java and the Quarkus framework.
- The health endpoints `/q/health/live`, `/q/health/ready`, and `/q/health/started` are used for liveness, readiness, and startup probes.

--- 

This README provides installation instructions, customization examples, and an overview of the operator's setup. Let me know if you need further modifications!