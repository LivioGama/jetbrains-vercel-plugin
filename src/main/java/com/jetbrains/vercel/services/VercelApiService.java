package com.jetbrains.vercel.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.diagnostic.Logger;
import com.jetbrains.vercel.models.VercelDeployment;
import com.jetbrains.vercel.models.VercelProject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public final class VercelApiService {
    private static final Logger LOG = Logger.getInstance(VercelApiService.class);
    private static final String VERCEL_API_BASE = "https://api.vercel.com";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public VercelApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(REQUEST_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public static VercelApiService getInstance() {
        return ApplicationManager.getApplication().getService(VercelApiService.class);
    }

    /**
     * Fetch all projects for the authenticated user
     */
    public CompletableFuture<List<VercelProject>> getProjects(String apiToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(VERCEL_API_BASE + "/v9/projects"))
                        .header("Authorization", "Bearer " + apiToken)
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode projectsNode = root.get("projects");

                    List<VercelProject> projects = new ArrayList<>();
                    if (projectsNode != null && projectsNode.isArray()) {
                        for (JsonNode projectNode : projectsNode) {
                            VercelProject project = objectMapper.treeToValue(projectNode, VercelProject.class);
                            projects.add(project);
                        }
                    }

                    LOG.info("Successfully fetched " + projects.size() + " projects");
                    return projects;
                } else {
                    LOG.warn("Failed to fetch projects: HTTP " + response.statusCode() + " - " + response.body());
                    throw new RuntimeException("Failed to fetch projects: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                LOG.error("Error fetching projects", e);
                throw new RuntimeException("Error fetching projects: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Fetch deployments for a specific project
     */
    public CompletableFuture<List<VercelDeployment>> getDeployments(String apiToken, String projectId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = VERCEL_API_BASE + "/v6/deployments?projectId=" + projectId + "&limit=20";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + apiToken)
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode deploymentsNode = root.get("deployments");

                    List<VercelDeployment> deployments = new ArrayList<>();
                    if (deploymentsNode != null && deploymentsNode.isArray()) {
                        for (JsonNode deploymentNode : deploymentsNode) {
                            VercelDeployment deployment = objectMapper.treeToValue(deploymentNode, VercelDeployment.class);
                            deployments.add(deployment);
                        }
                    }

                    LOG.info("Successfully fetched " + deployments.size() + " deployments for project " + projectId);
                    return deployments;
                } else {
                    LOG.warn("Failed to fetch deployments for project " + projectId + ": HTTP " + response.statusCode() + " - " + response.body());
                    throw new RuntimeException("Failed to fetch deployments: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                LOG.error("Error fetching deployments for project " + projectId, e);
                throw new RuntimeException("Error fetching deployments: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Fetch all deployments for all projects
     */
    public CompletableFuture<List<VercelDeployment>> getAllDeployments(String apiToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = VERCEL_API_BASE + "/v6/deployments?limit=50";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Authorization", "Bearer " + apiToken)
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    JsonNode deploymentsNode = root.get("deployments");

                    List<VercelDeployment> deployments = new ArrayList<>();
                    if (deploymentsNode != null && deploymentsNode.isArray()) {
                        for (JsonNode deploymentNode : deploymentsNode) {
                            VercelDeployment deployment = objectMapper.treeToValue(deploymentNode, VercelDeployment.class);
                            deployments.add(deployment);
                        }
                    }

                    LOG.info("Successfully fetched " + deployments.size() + " total deployments");
                    return deployments;
                } else {
                    LOG.warn("Failed to fetch all deployments: HTTP " + response.statusCode() + " - " + response.body());
                    throw new RuntimeException("Failed to fetch deployments: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                LOG.error("Error fetching all deployments", e);
                throw new RuntimeException("Error fetching deployments: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Test API token validity
     */
    public CompletableFuture<Boolean> testApiToken(String apiToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(VERCEL_API_BASE + "/v2/user"))
                        .header("Authorization", "Bearer " + apiToken)
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                boolean isValid = response.statusCode() == 200;
                LOG.info("API token validation result: " + isValid);
                return isValid;
            } catch (Exception e) {
                LOG.error("Error testing API token", e);
                return false;
            }
        });
    }

    /**
     * Get user information
     */
    public CompletableFuture<JsonNode> getUserInfo(String apiToken) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(VERCEL_API_BASE + "/v2/user"))
                        .header("Authorization", "Bearer " + apiToken)
                        .header("Content-Type", "application/json")
                        .timeout(REQUEST_TIMEOUT)
                        .GET()
                        .build();

                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonNode userInfo = objectMapper.readTree(response.body()).get("user");
                    LOG.info("Successfully fetched user info");
                    return userInfo;
                } else {
                    LOG.warn("Failed to fetch user info: HTTP " + response.statusCode());
                    throw new RuntimeException("Failed to fetch user info: HTTP " + response.statusCode());
                }
            } catch (Exception e) {
                LOG.error("Error fetching user info", e);
                throw new RuntimeException("Error fetching user info: " + e.getMessage(), e);
            }
        });
    }
}
