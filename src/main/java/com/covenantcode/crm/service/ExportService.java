package com.covenantcode.crm.service;

import java.io.OutputStream;

public interface ExportService {
    void exportLeads(LeadExportFilter filter, OutputStream out);

    void exportStudents(String search, OutputStream out);
}
