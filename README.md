# KubeApplication Operator

The **KubeApplication Operator** is a Kubernetes operator built with **Quarkus Java** and the Operator SDK. It manages custom resources of type `KubeApplication` for deploying and managing Kubernetes objects like Deployments, Services, and Horizontal Pod Autoscalers (HPAs).

---

## Features
- **CRD Management:** Simplified custom resource handling for `KubeApplication`.
- **Reconciliation Logic:** Automates resource creation, updates, and deletion based on `KubeApplication` specifications.
- **Horizontal Scaling:** Dynamic scaling using HPA configurations.
- **Namespace Scoping:** Operates at the namespace level for better resource isolation.

---

## Prerequisites
- Kubernetes cluster (v1.20+)
- Helm or kubectl installed
- Java 23.0.1 or higher
- Maven tool

---

## Deploying the Operator Using Helm

1. **Apply the CustomResourceDefinition (CRD):**
   ```bash
   kubectl apply -f kubeapplications.phoenix.io-v1.yml
   ```
   
2. **Deploy the operator using Helm**
   ```bash
   helm install kubeapplication-operator ./helm-chart --set image.repository=<docker-repo>/kubeapplication-operator --set image.tag=latest
   ```
## Usage
   