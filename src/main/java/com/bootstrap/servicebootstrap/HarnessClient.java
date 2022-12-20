package com.bootstrap.servicebootstrap;

import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarnessClient {

  private static final String accountId = "9S4tqOcyQkeFXybZvJt6fA";
  private static final String orgId = "default";
  private static final String projectId = "Default_Project_1671068007356";
  // private final static Logger LOGGER = Logger.getAnonymousLogger();
  Logger logger = LoggerFactory.getLogger(getClass());

  public static void main(String[] args) throws Exception {

    String serviceName = "delete-this-3";
    String envName = serviceName + "-env-1";
    String infraDefName = envName + "-infra-def-1";
    String pipelineName = serviceName + "-pipeline-2";
    HarnessClient harnessClient = new HarnessClient();
    harnessClient.createHarnessPipelineHelm(pipelineName,
        serviceName.replace("-", ""),
        envName.replace("-", ""),
        infraDefName.replace("-", ""),
        serviceName);

  }

  public String createHarnessEnvironment(String envName) throws IOException, InterruptedException {

    var httpClient = HttpClient.newBuilder().build();

    var payload = String.join("\n", "{", " \"orgIdentifier\": \"" + HarnessClient.orgId + "\",",
        " \"projectIdentifier\": \"" + HarnessClient.projectId + "\",",
        " \"identifier\": \"" + envName.replace("-", "") + "\",", " \"name\": \"" + envName + "\",",
        " \"type\": \"PreProduction\"", "}");

    logger.debug(payload);

    HashMap<String, String> params = new HashMap<>();
    params.put("accountIdentifier", HarnessClient.accountId);

    var query = params.keySet().stream()
        .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    var host = "https://app.harness.io";
    var pathname = "/gateway/ng/api/environmentsV2";
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .uri(URI.create(host + pathname + '?' + query))
        .header("Content-Type", "application/json")
        .header("x-api-key", System.getenv("HARNESS_API_TOKEN"))
        .build();

    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.debug(response.body());

    return response.body();
  }

  public String createHarnessInfraDefinition(String infraDefName, String envRef, String deploymentType,
      String nameSpace) throws IOException, InterruptedException {

    var httpClient = HttpClient.newBuilder().build();

    var payload = String.join("",
        "{", " \"identifier\": \"" + infraDefName.replace("-", "") + "\",",
        " \"orgIdentifier\": \"" + HarnessClient.orgId + "\",",
        " \"projectIdentifier\": \"" + HarnessClient.projectId + "\",", " \"environmentRef\": \"" + envRef + "\",",
        " \"name\": \"" + infraDefName + "\",", " \"type\": \"KubernetesDirect\",",
        " \"deploymentType\": \"" + deploymentType + "\",", " \"yaml\": \"infrastructureDefinition:\\n",
        " name: " + infraDefName + "\\n identifier: " + infraDefName.replace("-", "") + "\\n description: \\\"\\\"\\n",
        " tags: {}\\n orgIdentifier: default\\n projectIdentifier: Default_Project_1671068007356\\n",
        " environmentRef: " + envRef + "\\n", " deploymentType: " + deploymentType + "\\n",
        " type: KubernetesDirect\\n", " spec:\\n", "  connectorRef: k8skindconnector\\n",
        "  namespace: " + nameSpace + "\\n", "  releaseName: release-<+INFRA_KEY>\\n",
        " allowSimultaneousDeployments: false\\n\"", "}");

    logger.debug(payload);

    HashMap<String, String> params = new HashMap<>();
    params.put("accountIdentifier", HarnessClient.accountId);

    var query = params.keySet().stream()
        .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    var host = "https://app.harness.io";
    var pathname = "/gateway/ng/api/infrastructures";
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .uri(URI.create(host + pathname + '?' + query))
        .header("Content-Type", "application/json")
        .header("x-api-key", System.getenv("HARNESS_API_TOKEN"))
        .build();

    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.debug(response.body());
    return response.body();
  }

  public String createHarnessK8SConnector(String connectorName, String delegateSelector)
      throws IOException, InterruptedException {
    var httpClient = HttpClient.newBuilder().build();

    var payload = String.join("\n", "{", " \"connector\": {", "  \"name\": \"" + connectorName + "\",",
        "  \"identifier\": \"" + connectorName.replace("-", "") + "\",", "  \"description\": \"k8s-connctor\",",
        "  \"orgIdentifier\": \"" + HarnessClient.orgId + "\",",
        "  \"projectIdentifier\": \"" + HarnessClient.projectId + "\",", "  \"tags\": {",
        "   \"servicename\": \"autotest\"", "  },", "  \"type\": \"K8sCluster\",", "  \"spec\": {",
        "   \"connectorType\": {\"type\": \"KubernetesClusterConfig\"},",
        "   \"credential\": {\"type\": \"InheritFromDelegate\"},",
        "   \"delegateSelectors\": [\"" + delegateSelector + "\"]", "  }", " }", "}");

    logger.debug(payload);
    HashMap<String, String> params = new HashMap<>();
    params.put("accountIdentifier", HarnessClient.accountId);

    var query = params.keySet().stream()
        .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    var host = "https://app.harness.io";
    var pathname = "/gateway/ng/api/connectors";
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .uri(URI.create(host + pathname + '?' + query))
        .header("Content-Type", "application/json")
        .header("x-api-key", System.getenv("HARNESS_API_TOKEN"))
        .build();

    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.debug(response.body());

    return response.body();

  }

  public String createHarnessServiceHelm(String serviceName, String gitHubRepoName)
      throws IOException, InterruptedException {
    var httpClient = HttpClient.newBuilder().build();

    var payload = String.format("""
          identifier: %s
          orgIdentifier: default
          projectIdentifier: Default_Project_1671068007356
          name: %s
          yaml: |
            service:
              name: %s
              identifier: %s
              tags: {}
              serviceDefinition:
                spec:
                  manifests:
                    - manifest:
                        identifier: %s
                        type: HelmChart
                        spec:
                          store:
                            type: Github
                            spec:
                              connectorRef: githubhelmrepo
                              gitFetchType: Branch
                              folderPath: /
                              repoName: %s
                              branch: main
                          skipResourceVersioning: false
                          helmVersion: V3
                type: NativeHelm
        """, serviceName.replace("-", ""), serviceName, serviceName, serviceName.replace("-", ""),
        serviceName + "-helm-chart", gitHubRepoName);

    logger.debug(payload);

    HashMap<String, String> params = new HashMap<>();
    params.put("accountIdentifier", HarnessClient.accountId);

    var query = params.keySet().stream()
        .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    var host = "https://app.harness.io";
    var pathname = "/gateway/ng/api/servicesV2";
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .uri(URI.create(host + pathname + '?' + query))
        .header("Content-Type", "application/yaml")
        .header("x-api-key", System.getenv("HARNESS_API_TOKEN"))
        .build();

    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.debug(response.body());

    return response.body();

  }

  public String createHarnessServiceK8s(String serviceName, String gitHubRepoName)
      throws IOException, InterruptedException {
    var httpClient = HttpClient.newBuilder().build();

    var payload = String.format("""
          identifier: %s
          orgIdentifier: default
          projectIdentifier: Default_Project_1671068007356
          name: %s
          yaml: |
            service:
              name: %s
              identifier: %s
              tags: {}
              serviceDefinition:
                spec:
                  manifests:
                    - manifest:
                        identifier: %s
                        type: K8sManifest
                        spec:
                          store:
                            type: Github
                            spec:
                              connectorRef: githubhelmrepo
                              gitFetchType: Branch
                              paths: 
                                - /deployment.yaml
                              repoName: %s
                              branch: main
                type: Kubernetes
        """, serviceName.replace("-", ""), serviceName, serviceName, serviceName.replace("-", ""),
        serviceName + "-k8s-mf", gitHubRepoName);

    logger.debug(payload);

    HashMap<String, String> params = new HashMap<>();
    params.put("accountIdentifier", HarnessClient.accountId);

    var query = params.keySet().stream()
        .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    var host = "https://app.harness.io";
    var pathname = "/gateway/ng/api/servicesV2";
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .uri(URI.create(host + pathname + '?' + query))
        .header("Content-Type", "application/yaml")
        .header("x-api-key", System.getenv("HARNESS_API_TOKEN"))
        .build();

    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.debug(response.body());

    return response.body();

  }

  public String createHarnessPipelineHelm(String pipelineName, String serviceId, String environmentId, String infraDefId,
      String gitRepoName) throws IOException, InterruptedException {

    var httpClient = HttpClient.newBuilder().build();

    HashMap<String, String> params = new HashMap<>();
    params.put("accountIdentifier", HarnessClient.accountId);
    params.put("orgIdentifier", "default");
    params.put("projectIdentifier", HarnessClient.projectId);
    params.put("branch", "main");
    params.put("repoIdentifier", "");
    params.put("rootFolder", "/");
    params.put("filePath", ".harness/" + pipelineName + ".yaml");
    params.put("commitMsg", "Commit from app onboarding automation");
    params.put("isNewBranch", "false");
    params.put("baseBranch", "main");
    params.put("connectorRef", "githubhelmrepo");
    params.put("storeType", "REMOTE");
    params.put("repoName", gitRepoName);

    var payload = String.format("""
        pipeline:
          name: %s
          identifier: %s
          projectIdentifier: %s
          orgIdentifier: default
          tags: {}
          stages:
            - stage:
                name: helm-deploy
                identifier: helmdeploy
                description: ""
                type: Deployment
                spec:
                  deploymentType: NativeHelm
                  service:
                    serviceRef: %s
                  environment:
                    environmentRef: %s
                    deployToAll: false
                    infrastructureDefinitions:
                      - identifier: %s
                  execution:
                    steps:
                      - step:
                          name: Helm Deployment
                          identifier: helmDeployment
                          type: HelmDeploy
                          timeout: 10m
                          spec:
                            skipDryRun: false
                    rollbackSteps:
                      - step:
                          name: Helm Rollback
                          identifier: helmRollback
                          type: HelmRollback
                          timeout: 10m
                          spec: {}
                tags: {}
                failureStrategies:
                  - onFailure:
                      errors:
                        - AllErrors
                      action:
                        type: StageRollback

            """, pipelineName, pipelineName.replace("-", ""),
        HarnessClient.projectId, serviceId, environmentId,
        infraDefId);

    logger.debug(payload);
    var query = params.keySet().stream()
        .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    var host = "https://app.harness.io";
    var pathname = "/gateway/pipeline/api/pipelines/v2";
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .uri(URI.create(host + pathname + '?' + query))
        .header("Content-Type", "application/yaml")
        .header("x-api-key", System.getenv("HARNESS_API_TOKEN"))
        .build();

    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.debug(response.body());

    return response.body();

  }

  public String createHarnessPipelineK8s(String pipelineName, String serviceId, String environmentId, String infraDefId,
      String gitRepoName) throws IOException, InterruptedException {

    var httpClient = HttpClient.newBuilder().build();

    HashMap<String, String> params = new HashMap<>();
    params.put("accountIdentifier", HarnessClient.accountId);
    params.put("orgIdentifier", "default");
    params.put("projectIdentifier", HarnessClient.projectId);
    params.put("branch", "main");
    params.put("repoIdentifier", "");
    params.put("rootFolder", "/");
    params.put("filePath", ".harness/" + pipelineName + ".yaml");
    params.put("commitMsg", "Commit from app onboarding automation");
    params.put("isNewBranch", "false");
    params.put("baseBranch", "main");
    params.put("connectorRef", "githubhelmrepo");
    params.put("storeType", "REMOTE");
    params.put("repoName", gitRepoName);

    var payload = String.format("""
        pipeline:
          name: %s
          identifier: %s
          projectIdentifier: %s
          orgIdentifier: default
          tags: {}
          stages:
            - stage:
                name: k8s-deploy
                identifier: k8sdeploy
                description: ""
                type: Deployment
                spec:
                  deploymentType: Kubernetes
                  service:
                    serviceRef: %s
                  environment:
                    environmentRef: %s
                    deployToAll: false
                    infrastructureDefinitions:
                      - identifier: %s
                  execution:
                    steps:
                      - step:
                          name: Rollout Deployment
                          identifier: rollourDeployment
                          type: K8sRollingDeploy
                          timeout: 10m
                          spec:
                            skipDryRun: false
                            pruningEnabled: false
                    rollbackSteps:
                      - step:
                          name: Rollback Rollout Deployment
                          identifier: rollbackRolloutDeployment
                          type: K8sRollingRollback
                          timeout: 10m
                          spec: 
                            pruningEnabled: false
                tags: {}
                failureStrategies:
                  - onFailure:
                      errors:
                        - AllErrors
                      action:
                        type: StageRollback

            """, pipelineName, pipelineName.replace("-", ""),
        HarnessClient.projectId, serviceId, environmentId,
        infraDefId);

    logger.debug(payload);
    var query = params.keySet().stream()
        .map(key -> key + "=" + URLEncoder.encode(params.get(key), StandardCharsets.UTF_8))
        .collect(Collectors.joining("&"));

    var host = "https://app.harness.io";
    var pathname = "/gateway/pipeline/api/pipelines/v2";
    var request = HttpRequest.newBuilder()
        .POST(HttpRequest.BodyPublishers.ofString(payload))
        .uri(URI.create(host + pathname + '?' + query))
        .header("Content-Type", "application/yaml")
        .header("x-api-key", System.getenv("HARNESS_API_TOKEN"))
        .build();

    var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    logger.debug(response.body());

    return response.body();

  }
}