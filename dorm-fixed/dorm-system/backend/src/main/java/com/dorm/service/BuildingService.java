package com.dorm.service;

import com.dorm.entity.Building;
import com.dorm.entity.User;
import com.dorm.repository.BuildingRepository;
import com.dorm.repository.RoomRepository;
import com.dorm.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public List<Map<String, Object>> listAll() {
        return buildingRepository.findAllWithManager().stream().map(b -> {
            long roomCount     = roomRepository.countByBuildingId(b.getId());
            long availableCount = roomRepository.countAvailableByBuilding(b.getId());
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id",            b.getId());
            m.put("name",          b.getName());
            m.put("gender",        b.getGender().name());
            m.put("floors",        b.getFloors());
            m.put("description",   b.getDescription());
            m.put("managerId",     b.getManager() != null ? b.getManager().getId() : null);
            m.put("managerName",   b.getManager() != null ? b.getManager().getRealName() : null);
            m.put("roomCount",     roomCount);
            m.put("availableCount",availableCount);
            return m;
        }).collect(Collectors.toList());
    }

    public Building save(BuildingDto dto) {
        Building b = dto.getId() != null
            ? buildingRepository.findById(dto.getId()).orElseThrow(() -> new IllegalArgumentException("楼栋不存在"))
            : new Building();
        b.setName(dto.getName());
        b.setGender(Building.Gender.valueOf(dto.getGender()));
        b.setFloors(dto.getFloors());
        b.setDescription(dto.getDescription());
        if (dto.getManagerId() != null) {
            b.setManager(userRepository.findById(dto.getManagerId()).orElse(null));
        }
        return buildingRepository.save(b);
    }

    public void delete(Long id) {
        if (roomRepository.countByBuildingId(id) > 0) {
            throw new IllegalArgumentException("该楼栋下存在房间，无法删除");
        }
        buildingRepository.deleteById(id);
    }

    @Data
    public static class BuildingDto {
        private Long id;
        private String name;
        private String gender;
        private Integer floors;
        private String description;
        private Long managerId;
    }
}
