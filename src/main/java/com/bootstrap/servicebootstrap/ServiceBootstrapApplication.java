package com.bootstrap.servicebootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServiceBootstrapApplication implements CommandLineRunner {

	Logger logger = LoggerFactory.getLogger(getClass());

	public static void main(String[] args) {
		SpringApplication.run(ServiceBootstrapApplication.class, args);
	}

	@Override
	public void run(String... args) {
		logger.info("Starting service bootstrap..");
		boolean logTest = false;
		if (logTest) {
			logger.trace("Trace Log");
			logger.debug("Debug Log");
			logger.info("Info Log");
			logger.error("Error Log");
			return;
		}

		String serviceName = "";
		String templateName = "";

		if (args == null || args.length < 2) {
			logger.error("Enter service name and template name");
			return;
		} else {
			serviceName = args[0];
			templateName = args[1];
		}

		String envName = serviceName + "-env-1";
		String infraDefName = envName + "-infra-def-1";
		String pipelineName = serviceName + "-pipeline-1";
		String deploymentType = args[1].equalsIgnoreCase("spring-boot-helm-template") ? "NativeHelm" : "Kubernetes";

		HarnessClient harnessClient = new HarnessClient();

		try {
			harnessClient.createHarnessEnvironment(envName);
			logger.info("Created Harness Environment: " + envName);

			harnessClient.createHarnessInfraDefinition(infraDefName,
					envName.replace("-", ""), deploymentType, "default");
			// harnessClient.createHarnessK8SConnector("auto-k8s-connector-kind","k8s-kind-mac-delegate");
			logger.info("Created Harness Infrastructre Defenition: " + infraDefName);

			GitHubClient ghClient = new GitHubClient();
			ghClient.createSpringBootHelmRepo(serviceName, templateName);
			logger.info("Created GitHub Repo : " + serviceName + ",sleeping for 20s..");
			// TODO: find a better way to identify when new repo is ready for new commit
			Thread.sleep(20000);

			if (args[1].equalsIgnoreCase("spring-boot-helm-template")) {
				harnessClient.createHarnessServiceHelm(serviceName, serviceName);
				logger.info("Created Harness Service: " + serviceName);

				harnessClient.createHarnessPipelineHelm(pipelineName,
						serviceName.replace("-", ""),
						envName.replace("-", ""),
						infraDefName.replace("-", ""),
						serviceName);
				logger.info(
						"Created Harness Pipeline: " + pipelineName + " and Commited .harness folder in github repo");
			}
			else if (args[1].equalsIgnoreCase("flask-k8s-mf-template")) {
				harnessClient.createHarnessServiceK8s(serviceName, serviceName);
				logger.info("Created Harness Service: " + serviceName);

				harnessClient.createHarnessPipelineK8s(pipelineName,
						serviceName.replace("-", ""),
						envName.replace("-", ""),
						infraDefName.replace("-", ""),
						serviceName);
				logger.info(
						"Created Harness Pipeline: " + pipelineName + " and Commited .harness folder in github repo");
			}
			else
				logger.error("This template is not available"+templateName);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Service bootsrap failed", e.getMessage());
		}
	}

}
