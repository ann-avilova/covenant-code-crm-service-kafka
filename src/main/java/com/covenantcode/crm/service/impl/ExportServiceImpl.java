package com.covenantcode.crm.service.impl;

import com.covenantcode.crm.entity.Lead;
import com.covenantcode.crm.entity.Student;
import com.covenantcode.crm.repository.LeadRepository;
import com.covenantcode.crm.repository.LeadSpecifications;
import com.covenantcode.crm.repository.StudentRepository;
import com.covenantcode.crm.repository.StudentSpecifications;
import com.covenantcode.crm.service.ExportService;
import com.covenantcode.crm.service.LeadExportFilter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private static final byte[] UTF8_BOM = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final LeadRepository leadRepository;
    private final StudentRepository studentRepository;

    @Override
    @Transactional(readOnly = true)
    public void exportLeads(LeadExportFilter filter, OutputStream out) {
        Specification<Lead> spec = Specification
                .where(LeadSpecifications.hasStatus(filter.status()))
                .and(LeadSpecifications.searchByText(filter.search()))
                .and(LeadSpecifications.createdFrom(filter.dateFrom()))
                .and(LeadSpecifications.createdTo(filter.dateTo()));

        List<Lead> leads = leadRepository.findAll(spec);

        List<Object[]> rows = new ArrayList<>(leads.size());
        for (Lead lead : leads) {
            rows.add(new Object[]{
                    lead.getId(),
                    lead.getFirstName(),
                    nullToEmpty(lead.getLastName()),
                    lead.getPhone(),
                    nullToEmpty(lead.getEmail()),
                    lead.getStatus(),
                    lead.getInterestedCourse() != null ? lead.getInterestedCourse().getTitle() : "",
                    lead.getAssignedManager() != null
                            ? fullName(lead.getAssignedManager().getFirstName(), lead.getAssignedManager().getLastName())
                            : "",
                    lead.getCreatedAt().format(DATE_FORMAT)
            });
        }

        writeCsv(out, new String[]{"ID", "Имя", "Фамилия", "Телефон", "Email", "Статус", "Курс", "Менеджер", "Дата создания"}, rows);
    }

    @Override
    @Transactional(readOnly = true)
    public void exportStudents(String search, OutputStream out) {
        Specification<Student> spec = Specification.where(StudentSpecifications.searchByText(search));
        List<Student> students = studentRepository.findAll(spec);

        List<Object[]> rows = new ArrayList<>(students.size());
        for (Student student : students) {
            rows.add(new Object[]{
                    student.getId(),
                    student.getFirstName(),
                    student.getLastName(),
                    nullToEmpty(student.getPhone()),
                    nullToEmpty(student.getEmail()),
                    student.getBirthDate() != null ? student.getBirthDate().format(DATE_FORMAT) : "",
                    student.getCreatedAt().format(DATE_FORMAT)
            });
        }

        writeCsv(out, new String[]{"ID", "Имя", "Фамилия", "Телефон", "Email", "Дата рождения", "Дата создания"}, rows);
    }

    private void writeCsv(OutputStream out, String[] header, List<Object[]> rows) {
        try (Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(header).build())) {
            out.write(UTF8_BOM);
            out.flush();

            for (Object[] row : rows) {
                printer.printRecord(row);
            }
            printer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка форматирования CSV", e);
        }
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static String fullName(String firstName, String lastName) {
        return (nullToEmpty(firstName) + " " + nullToEmpty(lastName)).trim();
    }
}
