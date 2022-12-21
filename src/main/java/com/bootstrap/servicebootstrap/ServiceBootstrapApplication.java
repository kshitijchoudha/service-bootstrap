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

		String serviceName = "";
		String templateName = "";

		System.out.println("Enter Service Name: ");
		serviceName = System.console().readLine();

		System.out.println(String.join("\n", "Enter number for service type and deployment type template to use",
				"1. Spring Boot with Helm Deployment", "2. Python Flask with kuberetes manifest deployment"));

		templateName = System.console().readLine();

		if(templateName == null || (!templateName.trim().equalsIgnoreCase("1") && !templateName.trim().equalsIgnoreCase("2")))
		{
			System.out.println("Only options are 1 or 2, you entered: "+templateName+"END");
			return;
		}

		templateName = templateName.trim().equalsIgnoreCase("1") ? "spring-boot-helm-template" : "flask-k8s-mf-template";

		String envName = serviceName + "-env-1";
		String infraDefName = envName + "-infra-def-1";
		String pipelineName = serviceName + "-pipeline-1";
		
		HarnessClient harnessClient = new HarnessClient();

		System.out.println("Bootstraping service: "+serviceName +" ,with template: "+templateName + "\n" + "Continue?[Y/n]:");
		String proceed = System.console().readLine();

		if(proceed == null || (proceed!=null && proceed.trim().equalsIgnoreCase("Y")) || (proceed!=null && proceed.trim().equalsIgnoreCase("")))
		{
			System.out.println("Creating Github, Harness and Terrform resources..");
		}
        else
        {
            System.out.println("Aborting....");
            return;
        }
		

		try {
			harnessClient.createHarnessEnvironment(envName);
			logger.info("Created Harness Environment: " + envName);
			String deploymentType = templateName.trim().equalsIgnoreCase("spring-boot-helm-template") ? "NativeHelm" : "Kubernetes";
			harnessClient.createHarnessInfraDefinition(infraDefName,
					envName.replace("-", ""), deploymentType, "default");
			// harnessClient.createHarnessK8SConnector("auto-k8s-connector-kind","k8s-kind-mac-delegate");
			logger.info("Created Harness Infrastructre Defenition: " + infraDefName);

			GitHubClient ghClient = new GitHubClient();
			ghClient.createSpringBootHelmRepo(serviceName, templateName);
			logger.info("Created GitHub Repo : " + serviceName + ",sleeping for 20s..");
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
