package ch.unil.doplab.bankease.service;

import java.util.List;
import java.util.Map;

public interface EmployeeService {
    Map<String, Object> approve(String employeeId, String txId);
    Map<String, Object> reject(String employeeId, String txId);
    List<Map<String, Object>> pendingApprovals();
}
