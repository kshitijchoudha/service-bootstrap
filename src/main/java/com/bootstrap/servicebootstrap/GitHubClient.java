package com.bootstrap.servicebootstrap;

import java.io.IOException;


import org.kohsuke.github.GHCreateRepositoryBuilder;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitHubClient {
    Logger logger = LoggerFactory.getLogger(getClass());
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Github Client...");
    }

    public void createSpringBootHelmRepo(String applicationName, String templateName) throws IOException
    {
        logger.debug("GH API Tonek: "+System.getenv("GH_API_TOKEN"));
        GitHub github = new GitHubBuilder().withOAuthToken(System.getenv("GH_API_TOKEN")).build();
        GHCreateRepositoryBuilder ghRepoBuilder = github.createRepository(applicationName);
        ghRepoBuilder.fromTemplateRepository("kshitijchoudha", templateName);
        ghRepoBuilder.create();
    }
}
