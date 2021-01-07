package guru.springframework.multipledatasources.configuration;

import guru.springframework.multipledatasources.model.member.Member;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.jta.JtaTransactionManager;

import javax.persistence.PersistenceUnit;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "guru.springframework.multipledatasources.repository.member",
        entityManagerFactoryRef = "memberEntityManagerFactory",
        transactionManagerRef = "memberTransactionManager"
)
public class MemberDataSourceConfiguration extends JpaDataSourceConfiguration {
    
    protected MemberDataSourceConfiguration(final DataSource dataSource, final JpaProperties jpaProperties, final ObjectProvider<JtaTransactionManager> jtaTransactionManager) {
        super(dataSource, jpaProperties, jtaTransactionManager);
    }
    
    @Primary
    @Bean("memberDataSource")
    @ConfigurationProperties("app.datasource.member")
    public static DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    
    @Primary
    @Bean("memberEntityManagerFactory")
    @PersistenceUnit(unitName = "member")
    public LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactory(
            EntityManagerFactoryBuilder builder, @Qualifier("memberDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages(Member.class)
                .persistenceUnit("member")
                .jta(true)
                .properties(getVendorProperties())
                .build();
    }
    
    @Primary
    @Bean("memberTransactionManager")
    public PlatformTransactionManager memberTransactionManager(
            final @Qualifier("memberEntityManagerFactory") LocalContainerEntityManagerFactoryBean memberEntityManagerFactory) {
        return new JpaTransactionManager(memberEntityManagerFactory.getObject());
    }
    
}
