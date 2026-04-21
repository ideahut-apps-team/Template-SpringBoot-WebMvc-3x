package net.ideahut.springboot.template.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;
import net.ideahut.springboot.helper.HibernateHelper;
import net.ideahut.springboot.helper.ObjectHelper;
import net.ideahut.springboot.template.Application;
import net.ideahut.springboot.template.app.AppProperties;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
(
	entityManagerFactoryRef = TrxManagerPrimaryConfig.PREFIX + "EntityManagerFactory",
	transactionManagerRef = TrxManagerPrimaryConfig.PREFIX + "TransactionManager",
	basePackages = {
		Application.Package.APPLICATION + ".repo"
	}
)
class TrxManagerPrimaryConfig {
	
	static final String PREFIX = "primary";
	
	@Primary
	@Bean(PREFIX + "EntityManagerFactory")
	EntityManagerFactory entityManagerFactory(
		AppProperties appProperties
	) throws Exception {
		AppProperties.TrxMain mainDefinition = appProperties.getTrxManager().getPrimary();
		AppProperties.TrxAudit auditDefinition = ObjectHelper.useOrDefault(mainDefinition.getAudit(), AppProperties.TrxAudit::new);
		return HibernateHelper.createEntityManagerFactory(
			AppProperties.TrxDatasource.getDefinition(mainDefinition.getDatasource()), 
			mainDefinition, 
			auditDefinition.getId(), 
			AppProperties.TrxDatasource.getDefinition(auditDefinition.getDatasource()), 
			auditDefinition
		);
	}

	@Primary
	@Bean(PREFIX + "TransactionManager")
	PlatformTransactionManager transactionManager(
		@Qualifier(PREFIX + "EntityManagerFactory")	
		EntityManagerFactory entityManagerFactory	
	) {
		return new JpaTransactionManager(entityManagerFactory);
	}
	
}
