package com.ibm.elms.service;

import com.ibm.elms.domain.enums.EmployeeStatus;
import com.ibm.elms.domain.enums.LeaveRequestStatus;
import com.ibm.elms.dto.LeaveDtos.ReportSummary;
import com.ibm.elms.repository.LeaveRequestRepository;
import com.ibm.elms.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final UserAccountRepository userAccountRepository;

    public ReportService(
            LeaveRequestRepository leaveRequestRepository, UserAccountRepository userAccountRepository) {
        this.leaveRequestRepository = leaveRequestRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional(readOnly = true)
    public ReportSummary summary() {
        return new ReportSummary(
                userAccountRepository.findByStatus(EmployeeStatus.ACTIVE).size(),
                leaveRequestRepository.countByStatus(LeaveRequestStatus.PENDING),
                leaveRequestRepository.countByStatus(LeaveRequestStatus.APPROVED),
                leaveRequestRepository.countByStatus(LeaveRequestStatus.REJECTED),
                leaveRequestRepository.countByStatus(LeaveRequestStatus.CANCELLED));
    }
}
