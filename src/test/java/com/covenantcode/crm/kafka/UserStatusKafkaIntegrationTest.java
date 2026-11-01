package com.covenantcode.crm.kafka;

import com.covenantcode.crm.BaseIntegrationTest;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.RoleRepository;
import com.covenantcode.crm.repository.UserRepository;
import com.covenantcode.crm.security.JwtService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserStatusKafkaIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    private String adminToken;
    private User adminUser;
    private User managerUser;

    @BeforeEach
    void setup() {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.ADMIN);
                    return roleRepository.save(role);
                });
        Role managerRole = roleRepository.findByName(RoleName.MANAGER)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(RoleName.MANAGER);
                    return roleRepository.save(role);
                });

        adminUser = User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@test.ru")
                .password(passwordEncoder.encode("password123"))
                .role(adminRole)
                .enabled(true)
                .build();

        managerUser = User.builder()
                .firstName("Manager")
                .lastName("User")
                .email("manager@test.ru")
                .password(passwordEncoder.encode("password123"))
                .role(managerRole)
                .enabled(true)
                .build();
        adminUser = userRepository.save(adminUser);
        managerUser = userRepository.save(managerUser);
        adminToken = jwtService.generateToken(adminUser);
    }


    @DisplayName("Проверка топика, что после блокировки туда приходят данные менеджера")
    @Test
    void topicCheck_afterBlockingTheManager_managerDataIsSentToTheTopic() throws Exception {
        Map<String, Object> props = Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (KafkaConsumer<String, UserStatusChangedEvent> consumer = new KafkaConsumer<>(
                props, new StringDeserializer(), new JsonDeserializer<>(UserStatusChangedEvent.class))) {
            // 1. Подключиться к топику, запомнить, где он заканчивается
            TopicPartition partition = new TopicPartition("user.status-changed", 0);
            consumer.assign(List.of(partition));
            consumer.seekToEnd(List.of(partition));
            long bookmark = consumer.position(partition);

            // 2. Запомнить время «до»
            Instant before = Instant.now();

            // 3. PATCH (твой код)
            mockMvc.perform(patch("/api/v1/users/{id}/enabled", managerUser.getId())
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"enabled\": false}"))
                    .andExpect(status().isOk());

            // 4. Прочитать письмо
            ConsumerRecords<String, UserStatusChangedEvent> records = consumer.poll(Duration.ofSeconds(10));
            assertThat(records.count()).isEqualTo(1);
            ConsumerRecord<String, UserStatusChangedEvent> message = records.iterator().next();

            // 5. Запомнить время «после»
            Instant after = Instant.now();

            // 6. Проверить ключ и поля письма
            UserStatusChangedEvent event = message.value();
            assertThat(message.key()).isEqualTo(managerUser.getId().toString());
            assertThat(message.offset()).isEqualTo(bookmark);
            assertThat(event.userId()).isEqualTo(managerUser.getId());
            assertThat(event.email()).isEqualTo(managerUser.getEmail());
            assertThat(event.changedAt()).isBetween(before, after);
            assertThat(event.enabled()).isFalse();
            assertThat(event.eventId()).isNotNull();

        }
    }
}
