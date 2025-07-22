package com.mycompany.exchange_rate_service.integration.testcontainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;

import java.time.Duration;
import java.util.Map;

public class PostgreSQLContainerSetup {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLContainerSetup.class);
    private static final String DEFAULT_BUILD_TAG = "default_build_tag";
    private static final String DATABASE_NAME = "petclinic";
    private static final String DATABASE_USER = "petclinic";
    private static final String DATABASE_PASSWORD = "petclinic";
    private static final String POSTGRES_IMAGE = "postgres:16.3";
    private static final String NETWORK_ALIAS = "db";
    private static final String CONTAINER_SUFFIX = "-db-1";

    private static String networkName;
    private static String containerName;
    private static PostgreSQLContainer<?> postgreSQLContainer;

    static {
        // Obtener todas las variables de entorno
        Map<String, String> env = System.getenv();

        // Imprimir las variables de entorno usando el logger
        LOGGER.info("Mostrando todas las variables de entorno del sistema:");
        for (Map.Entry<String, String> entry : env.entrySet()) {
            LOGGER.info(entry.getKey() + " = " + entry.getValue());
        }

        String buildTag = resolveBuildTag();
        networkName = buildTag + "_default";
        containerName = buildTag + CONTAINER_SUFFIX;
    }

    private static String resolveBuildTag() {
        String tag = System.getenv("BUILD_TAG");
        if (tag == null || tag.isEmpty()) {
            LOGGER.warn("Environment variable BUILD_TAG is not set. Using default value: {}", DEFAULT_BUILD_TAG);
            return DEFAULT_BUILD_TAG;
        }
        return tag;
    }

    public static void startContainer() {
        if (postgreSQLContainer != null && postgreSQLContainer.isRunning()) {
            LOGGER.info("PostgreSQL container is already running.");
            return;
        }

        LOGGER.info("PostgreSQL container parameters - Image: {}, Database Name: {}, User: {}, Password: {}, Network Mode: {}, Network Alias: {}, Container Name: {}",
                POSTGRES_IMAGE, DATABASE_NAME, DATABASE_USER, DATABASE_PASSWORD, networkName, NETWORK_ALIAS, containerName);

        postgreSQLContainer = new PostgreSQLContainer<>(POSTGRES_IMAGE)
                .withDatabaseName(DATABASE_NAME)
                .withUsername(DATABASE_USER)
                .withPassword(DATABASE_PASSWORD)
                .withNetworkMode(networkName)
                .withCreateContainerCmdModifier(cmd -> {
                    cmd.withName(containerName);
                    cmd.withAliases(NETWORK_ALIAS);
                })
                .withFileSystemBind("src/test/resources/db/postgres/schema.sql",
                        "/docker-entrypoint-initdb.d/01-schema.sql", BindMode.READ_ONLY)
                .withFileSystemBind("src/test/resources/db/postgres/data.sql",
                        "/docker-entrypoint-initdb.d/02-data.sql", BindMode.READ_ONLY)
                .waitingFor(new WaitAllStrategy()
                        .withStartupTimeout(Duration.ofMinutes(3))
                        .withStrategy(Wait.forListeningPort())
                        .withStrategy(Wait.forLogMessage(".*database system is ready to accept connections.*\\n", 1))
                        .withStrategy(Wait.forSuccessfulCommand("pg_isready -U petclinic"))
                )
                .withStartupTimeout(Duration.ofMinutes(3));

        postgreSQLContainer.start();

        LOGGER.info("PostgreSQL container started with name: {}", containerName);
        LOGGER.info("JDBC URL: {}", postgreSQLContainer.getJdbcUrl());
    }

    public static String getJdbcUrl() {
        if (postgreSQLContainer == null || !postgreSQLContainer.isRunning()) {
            throw new IllegalStateException("PostgreSQL container is not running. Start the container first.");
        }
        return postgreSQLContainer.getJdbcUrl();
    }
}
