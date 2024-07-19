package kr.co.seoultel.message.mt.mms.direct.config;

import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisConnectionChecker;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import kr.co.seoultel.message.mt.mms.core_module.modules.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.annotation.PostConstruct;
import java.time.Duration;


/* REDIS-CONFIG 관련 상수 및 Bean */
@Slf4j
@Configuration
@ConfigurationProperties("spring.data.redis.cluster")
public class RedisConfig extends kr.co.seoultel.message.mt.mms.core_module.common.config.DefaultRedisConfig {

    @Override
    @PostConstruct
    public void check() {
        super.check();
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                                                                            .commandTimeout(Duration.ofSeconds(1))
                                                                            .shutdownTimeout(Duration.ZERO)
                                                                            .build();

        if (nodes.size() > 1) {
            RedisClusterConfiguration clusterConfig = new RedisClusterConfiguration(nodes);
            if (password != null) {
                clusterConfig.setPassword(RedisPassword.of(password));
            }

            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(clusterConfig, clientConfig);
            return lettuceConnectionFactory;

        } else if (nodes.size() == 1) {
            // Standalone configuration for single node
            String hostAndPort = nodes.get(0);
            String[] parts = hostAndPort.split(":");

            RedisStandaloneConfiguration standaloneConfig = new RedisStandaloneConfiguration(parts[0], Integer.parseInt(parts[1]));

            if (password != null) {
                standaloneConfig.setPassword(RedisPassword.of(password));
            }

            LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(standaloneConfig, clientConfig);
            // lettuceConnectionFactory.setShareNativeConnection(true); -> default : true

            return lettuceConnectionFactory;
        }

        throw new IllegalArgumentException("No valid redis nodes provided");
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean
    public RedisService redisService(RedisTemplate<String, Object> redisTemplate) {
        return new RedisService(redisTemplate);
    }

    @Bean
    public RedisConnectionChecker redisConnectionChecker(RedisConnectionFactory redisConnectionFactory) {
        return new RedisConnectionChecker(redisConnectionFactory);
    }

}

