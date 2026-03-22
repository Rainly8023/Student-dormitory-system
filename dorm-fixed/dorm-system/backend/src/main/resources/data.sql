-- 创建数据库（如未手动创建）
-- CREATE DATABASE IF NOT EXISTS dorm_system DEFAULT CHARACTER SET utf8mb4;

-- 初始化管理员账号（密码 BCrypt 加密，明文 admin123）
INSERT IGNORE INTO users (username, password, role, real_name, phone, email, is_active, created_at)
VALUES
('admin',    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTIGp6PTFV6', 'ADMIN',   '系统管理员', '13800000001', 'admin@dorm.edu',    1, NOW()),
('manager1', '$2a$10$BkuFSrFDLNv02eMEv.ILTOJ.smTCpNK1V1/gVCDNdPq3JHU9GkXom', 'MANAGER', '李宿管',   '13800000002', 'manager1@dorm.edu', 1, NOW()),
('manager2', '$2a$10$BkuFSrFDLNv02eMEv.ILTOJ.smTCpNK1V1/gVCDNdPq3JHU9GkXom', 'MANAGER', '王宿管',   '13800000003', 'manager2@dorm.edu', 1, NOW()),
('S2021001', '$2a$10$Nf3yqpRFJG1/R8tJDf3J1O5q3p5JPQM4a6CqHHJXrB/QbWTgI.K4G', 'STUDENT', '张三',     '13800001001', 's001@dorm.edu',     1, NOW()),
('S2021002', '$2a$10$Nf3yqpRFJG1/R8tJDf3J1O5q3p5JPQM4a6CqHHJXrB/QbWTgI.K4G', 'STUDENT', '李四',     '13800001002', 's002@dorm.edu',     1, NOW()),
('S2021003', '$2a$10$Nf3yqpRFJG1/R8tJDf3J1O5q3p5JPQM4a6CqHHJXrB/QbWTgI.K4G', 'STUDENT', '王五',     '13800001003', 's003@dorm.edu',     1, NOW());

-- 楼栋
INSERT IGNORE INTO buildings (id, name, gender, floors, description, manager_id, created_at)
VALUES
(1, '男生一号楼', 'MALE',   6, '共120间标准宿舍', 2, NOW()),
(2, '男生二号楼', 'MALE',   6, '共110间标准宿舍', 2, NOW()),
(3, '女生一号楼', 'FEMALE', 8, '共160间宿舍，配备独立卫浴', 3, NOW()),
(4, '女生二号楼', 'FEMALE', 6, '共128间标准宿舍', 3, NOW());

-- 房间
INSERT IGNORE INTO rooms (id, building_id, floor, room_number, capacity, current_count, room_type, status, created_at)
VALUES
(1, 1, 2, '201', 4, 2, '标准间', 'AVAILABLE', NOW()),
(2, 1, 2, '202', 4, 0, '标准间', 'AVAILABLE', NOW()),
(3, 1, 3, '301', 4, 4, '标准间', 'FULL',      NOW()),
(4, 1, 3, '302', 4, 0, '标准间', 'MAINTENANCE',NOW()),
(5, 2, 2, '201', 4, 1, '标准间', 'AVAILABLE', NOW()),
(6, 3, 2, '201', 6, 3, '六人间', 'AVAILABLE', NOW()),
(7, 4, 2, '201', 4, 0, '标准间', 'AVAILABLE', NOW());

-- 学生
INSERT IGNORE INTO students (id, user_id, student_no, real_name, gender, major, class_name, phone, room_id, bed_number, check_in_date, status, created_at)
VALUES
(1, 4, 'S2021001', '张三', 'MALE', '软件工程', '软工2101', '13800001001', 1, 1, '2021-09-01', 'ACTIVE', NOW()),
(2, 5, 'S2021002', '李四', 'MALE', '计算机科学', '计科2101', '13800001002', 1, 2, '2021-09-01', 'ACTIVE', NOW()),
(3, 6, 'S2021003', '王五', 'MALE', '网络工程', '网工2101', '13800001003', 5, 1, '2021-09-01', 'ACTIVE', NOW());

-- 报修
INSERT IGNORE INTO repairs (id, room_id, student_id, title, description, repair_type, status, priority, created_at, updated_at)
VALUES
(1, 1, 1, '空调不制冷', '空调开启后只吹风，室温过高', 'AIR_CONDITIONER', 'PROCESSING', 'HIGH',   NOW(), NOW()),
(2, 1, 2, '宽带无法连接', '网络接口损坏，无法上网', 'NETWORK', 'RESOLVED', 'NORMAL', NOW(), NOW()),
(3, 5, 3, '水龙头漏水', '卫生间水龙头持续滴水', 'PLUMBING', 'PENDING', 'NORMAL', NOW(), NOW()),
(4, 3, NULL, '门锁损坏', '宿舍门锁无法正常上锁', 'DOOR', 'PENDING', 'URGENT', NOW(), NOW());

-- 访客
INSERT IGNORE INTO visitors (id, student_id, visitor_name, visitor_phone, visit_date, purpose, status, registered_by, created_at)
VALUES
(1, 1, '张父', '13900001001', '2024-03-10', '家长探访', 'LEFT',     2, NOW()),
(2, 2, '李母', '13900002001', CURDATE(),    '家长探访', 'VISITING', 2, NOW());

-- 公告
INSERT IGNORE INTO notices (id, title, content, category, author_id, is_pinned, created_at, updated_at)
VALUES
(1, '2024年春季学期宿舍管理通知',
   '各位同学：\n春季学期已正式开始，请注意：\n1. 晚归时间为22:30；\n2. 宿舍内禁止使用大功率电器；\n3. 每周五进行卫生检查。',
   'NOTICE', 1, 1, NOW(), NOW()),
(2, '2月水电费账单已生成',
   '2024年2月水电费账单已生成，请各宿舍于3月15日前完成缴纳，如有疑问联系宿管老师。',
   'FEE', 1, 0, NOW(), NOW()),
(3, '寒假返校时间通知',
   '寒假期间宿舍将于2月25日正式开放，请同学们提前做好返校准备。',
   'NOTICE', 1, 0, NOW(), NOW());

-- 费用
INSERT IGNORE INTO fees (id, room_id, billing_month, water_usage, water_fee, electricity_usage, electricity_fee, other_fee, total_fee, status, created_at)
VALUES
(1, 1, '2024-02', 5.20,  10.40, 120.50, 60.25, 0.00, 70.65, 'PAID',   NOW()),
(2, 5, '2024-02', 4.80,   9.60, 115.00, 57.50, 0.00, 67.10, 'UNPAID', NOW()),
(3, 1, '2024-03', 6.10,  12.20, 135.00, 67.50, 0.00, 79.70, 'UNPAID', NOW()),
(4, 6, '2024-03', 8.50,  17.00, 210.00, 105.00,0.00,122.00, 'UNPAID', NOW());
