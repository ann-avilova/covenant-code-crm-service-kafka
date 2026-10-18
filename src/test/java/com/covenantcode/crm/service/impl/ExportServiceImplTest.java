package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.entity.Course;
import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.Role;
import com.covenantcode.crm.entity.User;
import com.covenantcode.crm.entity.enums.LeadStatus;
import com.covenantcode.crm.entity.enums.RoleName;
import com.covenantcode.crm.repository.LeadRepository;
import com.covenantcode.crm.service.LeadExportFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExportServiceImplTest {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

    @Mock
    private LeadRepository leadRepository;

    @InjectMocks
    private ExportServiceImpl exportService;

    @Test
    @DisplayName("Экспорт лидов — CSV содержит заголовок и строку данных")
    void exportLeads_shouldReturnCsvWithHeaderAndDataRow() {
        Course course = Course.builder()
                .id(1L)
                .title("Java Backend")
                .build();

        Role managerRole = Role.builder()
                .id(1L)
                .name(RoleName.MANAGER)
                .build();

        User manager = User.builder()
                .id(1L)
                .firstName("Мария")
                .lastName("Иванова")
                .email("maria@example.com")
                .password("hash")
                .role(managerRole)
                .enabled(true)
                .build();

        Lead lead = Lead.builder()
                .id(1L)
                .firstName("Иван")
                .lastName("Петров")
                .phone("+79161234567")
                .email("ivan@example.com")
                .status(LeadStatus.NEW)
                .interestedCourse(course)
                .assignedManager(manager)
                .createdAt(OffsetDateTime.parse("2026-06-25T10:00:00+03:00"))
                .build();

        when(leadRepository.findAll(any(Specification.class))).thenReturn(List.of(lead));

        OutputStream out = new ByteArrayOutputStream();

        exportService.exportLeads(
                new LeadExportFilter(null, null, null, null),
                out
        );

        byte[] bytes = ((ByteArrayOutputStream) out).toByteArray();

        assertThat(bytes).startsWith(UTF8_BOM);

        String csv = new String(bytes, UTF8_BOM.length, bytes.length - UTF8_BOM.length, StandardCharsets.UTF_8);

        assertThat(csv).contains("ID,Имя,Фамилия,Телефон,Email,Статус,Курс,Менеджер,Дата создания");
        assertThat(csv).contains("1,Иван,Петров,+79161234567,ivan@example.com,NEW,Java Backend,Мария Иванова,25.06.2026");
    }

    @Test
    @DisplayName("Экспорт пустого списка лидов — только заголовок, без строк данных")
    void exportLeads_emptyList_shouldReturnOnlyHeader() {
        when(leadRepository.findAll(any(Specification.class))).thenReturn(List.of());

        OutputStream out = new ByteArrayOutputStream();

        exportService.exportLeads(
                new LeadExportFilter(null, null, null, null),
                out
        );

        byte[] bytes = ((ByteArrayOutputStream) out).toByteArray();

        assertThat(bytes).startsWith(UTF8_BOM);

        String csv = new String(bytes, UTF8_BOM.length, bytes.length - UTF8_BOM.length, StandardCharsets.UTF_8);
        String[] lines = csv.split("\r\n|\n");

        assertThat(lines).hasSize(1);
        assertThat(lines[0]).isEqualTo("ID,Имя,Фамилия,Телефон,Email,Статус,Курс,Менеджер,Дата создания");
    }
}
