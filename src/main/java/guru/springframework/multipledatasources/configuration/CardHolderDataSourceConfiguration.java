package guru.springframework.multipledatasources.configuration;

import guru.springframework.multipledatasources.model.cardholder.CardHolder;
import org.apache.commons.dbcp.BasicDataSource;
import org.eclipse.persistence.config.BatchWriting;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.eclipse.persistence.logging.SessionLog;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaBaseConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.InstrumentationLoadTimeWeaver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "guru.springframework.multipledatasources.repository.cardholder",
        entityManagerFactoryRef = "cardHolderEntityManagerFactory",
        transactionManagerRef= "cardHolderTransactionManager")
public class CardHolderDataSourceConfiguration extends JpaBaseConfiguration {
    
    protected CardHolderDataSourceConfiguration(DataSource dataSource, JpaProperties properties,
                                     ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
        super(dataSource, properties, jtaTransactionManager);
    }
    
    @Override
    protected AbstractJpaVendorAdapter createJpaVendorAdapter() {
        return new EclipseLinkJpaVendorAdapter();
    }
    
    @Override
    protected Map<String, Object> getVendorProperties() {
        final Map<String, Object> ret = new HashMap<>();
        ret.put(PersistenceUnitProperties.BATCH_WRITING, BatchWriting.JDBC);
        return ret;
    }
    
//    @Bean
//    @ConfigurationProperties("app.datasource.cardholder")
//    public DataSourceProperties cardHolderDataSourceProperties() {
//        return new DataSourceProperties();
//    }
//
//    @Bean
//    @ConfigurationProperties("app.datasource.cardholder.configuration")
//    public DataSource cardholderDataSource() {
//        return cardHolderDataSourceProperties().initializeDataSourceBuilder()
//                .type(BasicDataSource.class).build();
//    }
    
    @Bean
    public static DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        dataSource.setUrl("jdbc:sqlserver://localhost:1433;database=cardholderdb;SelectMethod=cursor");
        dataSource.setUsername("sa");
        dataSource.setPassword("password!23");
        return dataSource;
    }

    @Bean(name = "cardHolderEntityManagerFactory")
    @PersistenceUnit(unitName = "cardholder")
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactory(
            EntityManagerFactoryBuilder builder, DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(CardHolder.class)
                .persistenceUnit("cardholder")
                .properties(initJpaProperties())
                .build();
    }
//    public LocalContainerEntityManagerFactoryBean cardHolderEntityManagerFactory(
//            EntityManagerFactoryBuilder builder) {
//        return builder
//                .dataSource(cardholderDataSource())
//                .packages(CardHolder.class)
//                .build();
//    }

//    @Bean
//    public static PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
//        final JpaTransactionManager transactionManager = new JpaTransactionManager();
//        transactionManager.setEntityManagerFactory(emf);
//        return transactionManager;
//    }

    @Bean(name = "cardHolderTransactionManager")
    public PlatformTransactionManager cardHolderTransactionManager(
            final @Qualifier("cardHolderEntityManagerFactory") LocalContainerEntityManagerFactoryBean cardHolderEntityManagerFactory) {
        return new JpaTransactionManager(cardHolderEntityManagerFactory.getObject());
    }
    
    @Bean
    public static JpaProperties properties() {
        final JpaProperties jpaProperties = new JpaProperties();
//        jpaProperties.setShowSql(true);
        jpaProperties.setDatabasePlatform("org.eclipse.persistence.platform.database.SQLServerPlatform");
        return jpaProperties;
    }
    
    private static Map<String, ?> initJpaProperties() {
        final Map<String, Object> ret = new HashMap<>();
        // Add any JpaProperty you are interested in and is supported by your Database and JPA implementation
        ret.put(PersistenceUnitProperties.BATCH_WRITING, BatchWriting.JDBC);
        ret.put(PersistenceUnitProperties.LOGGING_LEVEL, SessionLog.FINEST_LABEL);
        ret.put(PersistenceUnitProperties.WEAVING, detectWeavingMode());
//        ret.put(PersistenceUnitProperties.DDL_GENERATION, PersistenceUnitProperties.CREATE_ONLY);
//        ret.put(PersistenceUnitProperties.DDL_GENERATION_MODE, PersistenceUnitProperties.DDL_DATABASE_GENERATION);
        return ret;
    }
    
    private static String detectWeavingMode() {
        return InstrumentationLoadTimeWeaver.isInstrumentationAvailable() ? "true" : "false";
    }
}
