package com.quanna.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration for JPA Auditing
 * This enables automatic population of @CreatedDate and @LastModifiedDate fields
 *
 * To use this configuration, import it in your service's main application class:
 * @Import(JpaAuditingConfig.class)
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}

