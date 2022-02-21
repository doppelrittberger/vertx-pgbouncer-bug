package org.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.quarkus.arc.profile.UnlessBuildProfile;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.postgres.PostgresPlugin;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class JdbiProducer {
    @ConfigProperty(name = "quarkus.datasource.jdbc.url")
    String jdbcUrl;
    @ConfigProperty(name = "quarkus.datasource.username")
    String dbUser;
    @ConfigProperty(name = "quarkus.datasource.password")
    String dbPass;

    @Produces
    @ApplicationScoped
    @UnlessBuildProfile("test")
    public JdbiWrapper jdbi() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPass);
        config.addDataSourceProperty("prepareThreshold", "0");
        final HikariDataSource dataSource = new HikariDataSource(config);
        return new JdbiWrapper(Jdbi.create(dataSource)
                .installPlugin(new PostgresPlugin())
                .configure(SqlStatements.class, stmt -> stmt.setQueryTimeout(5)));
    }
}
