package com.dorm.service;

import com.dorm.entity.*;
import com.dorm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final StudentRepository studentRepository;
    private final RoomRepository roomRepository;
    private final RepairRepository repairRepository;
    private final FeeRepository feeRepository;
    private final VisitorRepository visitorRepository;
    private final BuildingRepository buildingRepository;

    public Map<String, Object> overview() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalStudents",     studentRepository.countByStatus(Student.Status.ACTIVE));
        m.put("totalRooms",        roomRepository.count());
        m.put("availableRooms",    roomRepository.findAvailableRooms().size());
        m.put("totalBuildings",    buildingRepository.count());
        m.put("pendingRepairs",    repairRepository.countByStatus(Repair.Status.PENDING));
        m.put("processingRepairs", repairRepository.countByStatus(Repair.Status.PROCESSING));
        m.put("unpaidFees",        feeRepository.countByStatus(Fee.Status.UNPAID));
        m.put("visitingNow",       visitorRepository.countByStatus(Visitor.Status.VISITING));
        m.put("todayVisitors",     visitorRepository.countByVisitDateAndStatus(LocalDate.now(), Visitor.Status.VISITING)
                                 + visitorRepository.countByVisitDateAndStatus(LocalDate.now(), Visitor.Status.LEFT));
        return m;
    }
}
