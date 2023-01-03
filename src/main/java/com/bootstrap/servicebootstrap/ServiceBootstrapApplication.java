package com.bootstrap.servicebootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceBootstrapApplication implements CommandLineRunner {

	Logger logger = LoggerFactory.getLogger(getClass());
	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLACK = "\u001B[30m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";

	public static void main(String[] args) {
		SpringApplication.run(ServiceBootstrapApplication.class, args);
	}

	@Override
	public void run(String... args) {
		logger.info("Starting service bootstrap..");

		String serviceName = "";
		String templateName = "";

		System.out.print("Enter Service Name: ");
		serviceName = System.console().readLine();

		System.out.println(
				String.join("\n", ANSI_BLUE + "Enter number for service type and deployment type template to use",
						"1. Spring Boot with Helm Deployment",
						"2. Python Flask with kubernetes manifest deployment" + ANSI_BLUE));

		templateName = System.console().readLine();

		if (templateName == null
				|| (!templateName.trim().equalsIgnoreCase("1") && !templateName.trim().equalsIgnoreCase("2"))) {
			System.out.println(ANSI_RED + "Only options are 1 or 2, you entered: " + ANSI_RED + templateName);
			return;
		}

		templateName = templateName.trim().equalsIgnoreCase("1") ? "spring-boot-helm-template"
				: "flask-k8s-mf-template";

		String envName = serviceName + "-env-1";
		String infraDefName = envName + "-infra-def-1";
		String pipelineName = serviceName + "-pipeline-1";

		HarnessClient harnessClient = new HarnessClient();

		System.out.print(
				ANSI_BLUE + "Bootstraping service: " + serviceName + " ,with template: " + templateName + "\n"
						+ "Continue?[Y/n]:" + ANSI_BLUE);
		String proceed = System.console().readLine();

		if (proceed == null || (proceed != null && proceed.trim().equalsIgnoreCase("Y"))
				|| (proceed != null && proceed.trim().equalsIgnoreCase(""))) {
			System.out.println(ANSI_GREEN + "Creating Github, Harness and Terrform resources.." + ANSI_GREEN);

		} else {
			System.out.println(ANSI_RED + "Aborting...." + ANSI_RED);
			return;
		}

		try {
			harnessClient.createHarnessEnvironment(envName);
			logger.info("Created Harness Environment: " + envName);
			String deploymentType = templateName.trim().equalsIgnoreCase("spring-boot-helm-template") ? "NativeHelm"
					: "Kubernetes";
			harnessClient.createHarnessInfraDefinition(infraDefName,
					envName.replace("-", ""), deploymentType, "default");
			// harnessClient.createHarnessK8SConnector("auto-k8s-connector-kind","k8s-kind-mac-delegate");
			logger.info("Created Harness Infrastructre Defenition: " + infraDefName);

			GitHubClient ghClient = new GitHubClient();
			ghClient.createSpringBootHelmRepo(serviceName, templateName);
			logger.info("Created GitHub Repo : " + serviceName + ",waiting for resources to be ready in repository..");
			// TODO: find a better way to identify when new repo is ready for new commit
			Thread.sleep(20000);

			if (templateName.equalsIgnoreCase("spring-boot-helm-template")) {
				harnessClient.createHarnessServiceHelm(serviceName, serviceName);
				logger.info("Created Harness Service: " + serviceName);

				harnessClient.createHarnessPipelineHelm(pipelineName,
						serviceName.replace("-", ""),
						envName.replace("-", ""),
						infraDefName.replace("-", ""),
						serviceName);
				logger.info(
						"Created Harness Pipeline: " + pipelineName + " and Commited .harness folder in github repo");
			} else if (templateName.equalsIgnoreCase("flask-k8s-mf-template")) {
				harnessClient.createHarnessServiceK8s(serviceName, serviceName);
				logger.info("Created Harness Service: " + serviceName);

				harnessClient.createHarnessPipelineK8s(pipelineName,
						serviceName.replace("-", ""),
						envName.replace("-", ""),
						infraDefName.replace("-", ""),
						serviceName);
				logger.info(
						"Created Harness Pipeline: " + pipelineName + " and Commited .harness folder in github repo");
			} else
				logger.error("This template is not available" + templateName);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Service bootsrap failed", e.getMessage());
		}
	}

}
