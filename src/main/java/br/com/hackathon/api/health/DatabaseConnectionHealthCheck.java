package br.com.hackathon.api.health;

import io.quarkus.agroal.DataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;
import io.agroal.api.AgroalDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Readiness
@ApplicationScoped
public class DatabaseConnectionHealthCheck implements HealthCheck {

    @Inject
    @DataSource("mysql")
    AgroalDataSource dataSource;

    @Override
    public HealthCheckResponse call() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeQuery("SELECT 1");

            return HealthCheckResponse.named("Database Connection")
                    .withData("message", "Database connection is healthy.")
                    .up()
                    .build();
        } catch (SQLException e) {
            return HealthCheckResponse.named("Database Connection")
                    .withData("error", e.getMessage())
                    .down()
                    .build();
        }
    }
}