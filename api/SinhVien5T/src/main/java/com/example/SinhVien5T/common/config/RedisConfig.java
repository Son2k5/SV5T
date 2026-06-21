package com.example.SinhVien5T.common.config;

import io.lettuce.core.api.StatefulConnection;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.redis.cache")
    public RedisInstanceProperties cacheRedisProperties() {
        return new RedisInstanceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.redis.token")
    public RedisInstanceProperties tokenRedisProperties() {
        return new RedisInstanceProperties();
    }

    @Bean(name = "cacheRedisConnectionFactory")
    @Primary
    public RedisConnectionFactory cacheRedisConnectionFactory(
            @Qualifier("cacheRedisProperties") RedisInstanceProperties properties
    ) {
        return createConnectionFactory(properties);
    }

    @Bean(name = "tokenRedisConnectionFactory")
    public RedisConnectionFactory tokenRedisConnectionFactory(
            @Qualifier("tokenRedisProperties") RedisInstanceProperties properties
    ) {
        return createConnectionFactory(properties);
    }

    @Bean(name = "tokenRedisHealthIndicator")
    @ConditionalOnProperty(
            value = "app.redis.token.enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public HealthIndicator tokenRedisHealthIndicator(
            @Qualifier("tokenRedisConnectionFactory") RedisConnectionFactory connectionFactory
    ) {
        return () -> {
            RedisConnection connection = null;
            try {
                connection = connectionFactory.getConnection();
                String response = connection.ping();
                return Health.up()
                        .withDetail("role", "token-session")
                        .withDetail("response", response)
                        .build();
            } catch (Exception e) {
                return Health.down(e)
                        .withDetail("role", "token-session")
                        .build();
            } finally {
                if (connection != null) {
                    connection.close();
                }
            }
        };
    }

    private LettuceConnectionFactory createConnectionFactory(RedisInstanceProperties properties) {
        String mode = properties.getMode().toLowerCase(Locale.ROOT);
        LettuceConnectionFactory factory = switch (mode) {
            case "cluster" -> new LettuceConnectionFactory(clusterConfiguration(properties), clientConfiguration(properties));
            case "sentinel" -> new LettuceConnectionFactory(sentinelConfiguration(properties), clientConfiguration(properties));
            default -> new LettuceConnectionFactory(standaloneConfiguration(properties), clientConfiguration(properties));
        };

        factory.setValidateConnection(true);
        return factory;
    }

    private RedisStandaloneConfiguration standaloneConfiguration(RedisInstanceProperties properties) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration(
                properties.getHost(),
                properties.getPort()
        );
        configuration.setDatabase(properties.getDatabase());
        setPassword(configuration, properties.getPassword());
        return configuration;
    }

    private RedisClusterConfiguration clusterConfiguration(RedisInstanceProperties properties) {
        List<String> nodes = properties.getClusterNodes().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (nodes.isEmpty()) {
            throw new IllegalStateException("Redis cluster mode requires app.redis.*.cluster-nodes");
        }

        RedisClusterConfiguration configuration = new RedisClusterConfiguration(nodes);
        configuration.setMaxRedirects(properties.getClusterMaxRedirects());
        setPassword(configuration, properties.getPassword());
        return configuration;
    }

    private RedisSentinelConfiguration sentinelConfiguration(RedisInstanceProperties properties) {
        if (!StringUtils.hasText(properties.getSentinelMaster())) {
            throw new IllegalStateException("Redis sentinel mode requires app.redis.*.sentinel-master");
        }

        List<String> nodes = properties.getSentinelNodes().stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (nodes.isEmpty()) {
            throw new IllegalStateException("Redis sentinel mode requires app.redis.*.sentinel-nodes");
        }

        RedisSentinelConfiguration configuration = new RedisSentinelConfiguration();
        configuration.master(properties.getSentinelMaster());
        nodes.forEach(node -> {
            String[] parts = node.split(":", 2);
            configuration.sentinel(parts[0].trim(), parts.length == 2 ? Integer.parseInt(parts[1].trim()) : 26379);
        });
        configuration.setDatabase(properties.getDatabase());
        setPassword(configuration, properties.getPassword());
        return configuration;
    }

    private LettuceClientConfiguration clientConfiguration(RedisInstanceProperties properties) {
        GenericObjectPoolConfig<StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(properties.getPool().getMaxActive());
        poolConfig.setMaxIdle(properties.getPool().getMaxIdle());
        poolConfig.setMinIdle(properties.getPool().getMinIdle());
        poolConfig.setMaxWait(properties.getPool().getMaxWait());

        return LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(properties.getTimeout())
                .shutdownTimeout(properties.getShutdownTimeout())
                .build();
    }

    private void setPassword(RedisStandaloneConfiguration configuration, String password) {
        if (StringUtils.hasText(password)) {
            configuration.setPassword(RedisPassword.of(password));
        }
    }

    private void setPassword(RedisClusterConfiguration configuration, String password) {
        if (StringUtils.hasText(password)) {
            configuration.setPassword(RedisPassword.of(password));
        }
    }

    private void setPassword(RedisSentinelConfiguration configuration, String password) {
        if (StringUtils.hasText(password)) {
            configuration.setPassword(RedisPassword.of(password));
        }
    }

    public static class RedisInstanceProperties {
        private String mode = "standalone";
        private String host = "localhost";
        private int port = 6379;
        private int database = 0;
        private String password;
        private Duration timeout = Duration.ofSeconds(2);
        private Duration shutdownTimeout = Duration.ofMillis(100);
        private List<String> clusterNodes = new ArrayList<>();
        private int clusterMaxRedirects = 3;
        private String sentinelMaster = "mymaster";
        private List<String> sentinelNodes = new ArrayList<>();
        private Pool pool = new Pool();

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }

        public Duration getShutdownTimeout() {
            return shutdownTimeout;
        }

        public void setShutdownTimeout(Duration shutdownTimeout) {
            this.shutdownTimeout = shutdownTimeout;
        }

        public List<String> getClusterNodes() {
            return clusterNodes;
        }

        public void setClusterNodes(List<String> clusterNodes) {
            this.clusterNodes = clusterNodes;
        }

        public int getClusterMaxRedirects() {
            return clusterMaxRedirects;
        }

        public void setClusterMaxRedirects(int clusterMaxRedirects) {
            this.clusterMaxRedirects = clusterMaxRedirects;
        }

        public String getSentinelMaster() {
            return sentinelMaster;
        }

        public void setSentinelMaster(String sentinelMaster) {
            this.sentinelMaster = sentinelMaster;
        }

        public List<String> getSentinelNodes() {
            return sentinelNodes;
        }

        public void setSentinelNodes(List<String> sentinelNodes) {
            this.sentinelNodes = sentinelNodes;
        }

        public Pool getPool() {
            return pool;
        }

        public void setPool(Pool pool) {
            this.pool = pool;
        }

        public static class Pool {
            private int maxActive = 32;
            private int maxIdle = 16;
            private int minIdle = 4;
            private Duration maxWait = Duration.ofMillis(500);

            public int getMaxActive() {
                return maxActive;
            }

            public void setMaxActive(int maxActive) {
                this.maxActive = maxActive;
            }

            public int getMaxIdle() {
                return maxIdle;
            }

            public void setMaxIdle(int maxIdle) {
                this.maxIdle = maxIdle;
            }

            public int getMinIdle() {
                return minIdle;
            }

            public void setMinIdle(int minIdle) {
                this.minIdle = minIdle;
            }

            public Duration getMaxWait() {
                return maxWait;
            }

            public void setMaxWait(Duration maxWait) {
                this.maxWait = maxWait;
            }
        }
    }
}
