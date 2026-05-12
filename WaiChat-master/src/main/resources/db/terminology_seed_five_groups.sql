-- 术语库示例数据：5 个系统术语组；互联网与软件 20 条；其余组 3～12 条；每条术语至少 1 个别名
-- 使用前请备份。会清空 terminology 三表后重灌。
SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM `terminology_alias`;
DELETE FROM `terminology`;
DELETE FROM `terminology_group`;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO `terminology_group` (`id`, `name`, `code`, `sort_weight`, `enabled`, `create_time`, `update_time`)
VALUES
  (1, '医疗与健康', 'medical_health', 50, 1, NOW(), NOW()),
  (2, '电商与物流', 'ecommerce_logistics', 45, 1, NOW(), NOW()),
  (3, '互联网与软件', 'internet_software', 40, 1, NOW(), NOW()),
  (4, '法律与合规', 'legal_compliance', 35, 1, NOW(), NOW()),
  (5, '教育与学术', 'education_academic', 30, 1, NOW(), NOW());

-- 组 1：医疗与健康（5 条）id 1–5
INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`, `cloned_from_system_id`)
VALUES
  (1, NULL, 1, 'CT', '计算机断层扫描（Computed Tomography），利用 X 射线断层成像的医学检查手段。', 1, 90, NOW(), NOW(), NULL),
  (2, NULL, 1, 'MRI', '磁共振成像（Magnetic Resonance Imaging），利用磁场与射频的软组织成像技术。', 1, 88, NOW(), NOW(), NULL),
  (3, NULL, 1, 'ICU', '重症监护病房（Intensive Care Unit），集中救治危重患者的病区。', 1, 85, NOW(), NOW(), NULL),
  (4, NULL, 1, '心电图', '记录心脏电活动以评估心律与心肌供血的无创检查。', 1, 70, NOW(), NOW(), NULL),
  (5, NULL, 1, '血糖', '血液中葡萄糖浓度，是糖尿病筛查与管理的核心指标。', 1, 65, NOW(), NOW(), NULL);

-- 组 2：电商与物流（8 条）id 6–13
INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`, `cloned_from_system_id`)
VALUES
  (6, NULL, 2, 'SKU', '库存量单位（Stock Keeping Unit），最小可管理商品编码。', 1, 95, NOW(), NOW(), NULL),
  (7, NULL, 2, 'SPU', '标准产品单位（Standard Product Unit），聚合同款商品的抽象规格。', 1, 92, NOW(), NOW(), NULL),
  (8, NULL, 2, 'GMV', '成交总额（Gross Merchandise Volume），一定周期内订单金额总和。', 1, 80, NOW(), NOW(), NULL),
  (9, NULL, 2, '履约', '按订单约定完成备货、出库、配送与签收的全过程。', 1, 72, NOW(), NOW(), NULL),
  (10, NULL, 2, '冷链', '在低温环境下运输与储存生鲜、医药等温敏商品的物流体系。', 1, 70, NOW(), NOW(), NULL),
  (11, NULL, 2, '仓配一体', '仓储与配送由同一主体协同优化，以缩短时效、降低破损。', 1, 68, NOW(), NOW(), NULL),
  (12, NULL, 2, '逆向物流', '退货、换货、回收与再制造等从消费者返回供应链的货流。', 1, 66, NOW(), NOW(), NULL),
  (13, NULL, 2, 'OMS', '订单管理系统（Order Management System），负责接单、拆合单与状态跟踪。', 1, 75, NOW(), NOW(), NULL);

-- 组 3：互联网与软件（20 条）id 14–33
INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`, `cloned_from_system_id`)
VALUES
  (14, NULL, 3, 'API', '应用程序接口（Application Programming Interface），供系统间调用的约定与端点。', 1, 95, NOW(), NOW(), NULL),
  (15, NULL, 3, 'REST', '表述性状态转移架构风格，常用 HTTP 动词与资源 URL 设计 Web 服务。', 1, 90, NOW(), NOW(), NULL),
  (16, NULL, 3, 'GraphQL', '由客户端声明所需字段结构的查询语言与运行时，用于聚合数据接口。', 1, 88, NOW(), NOW(), NULL),
  (17, NULL, 3, 'JWT', 'JSON Web Token，一种紧凑的自包含令牌，常用于无状态身份校验。', 1, 86, NOW(), NOW(), NULL),
  (18, NULL, 3, 'OAuth 2.0', '开放授权框架，允许第三方在资源所有者授权下访问受保护资源。', 1, 86, NOW(), NOW(), NULL),
  (19, NULL, 3, 'CI/CD', '持续集成与持续交付/部署，自动化构建、测试与发布流水线。', 1, 84, NOW(), NOW(), NULL),
  (20, NULL, 3, 'Kubernetes', '容器编排平台，用于部署、扩缩容与管理分布式应用。', 1, 88, NOW(), NOW(), NULL),
  (21, NULL, 3, 'Docker', '容器化平台，将应用与依赖打包为可移植镜像以便一致运行。', 1, 87, NOW(), NOW(), NULL),
  (22, NULL, 3, '微服务', '将系统拆分为小型、独立部署的服务，通过轻量协议协作。', 1, 82, NOW(), NOW(), NULL),
  (23, NULL, 3, 'RPC', '远程过程调用（Remote Procedure Call），像本地函数一样调用远端服务。', 1, 80, NOW(), NOW(), NULL),
  (24, NULL, 3, 'gRPC', '基于 HTTP/2 与 Protocol Buffers 的高性能 RPC 框架。', 1, 82, NOW(), NOW(), NULL),
  (25, NULL, 3, 'WebSocket', '全双工持久连接协议，适用于实时消息与推送场景。', 1, 78, NOW(), NOW(), NULL),
  (26, NULL, 3, 'CDN', '内容分发网络（Content Delivery Network），就近缓存静态资源以加速访问。', 1, 76, NOW(), NOW(), NULL),
  (27, NULL, 3, 'DNS', '域名系统（Domain Name System），将域名解析为 IP 地址的分布式目录。', 1, 76, NOW(), NOW(), NULL),
  (28, NULL, 3, 'TLS', '传输层安全协议（Transport Layer Security），为通信提供加密与完整性校验。', 1, 84, NOW(), NOW(), NULL),
  (29, NULL, 3, 'HTTPS', '在 HTTP 之上叠加 TLS 的安全超文本传输协议。', 1, 83, NOW(), NOW(), NULL),
  (30, NULL, 3, 'JSON', '轻量级数据交换格式，基于文本、易于人读与程序解析。', 1, 74, NOW(), NOW(), NULL),
  (31, NULL, 3, 'YAML', '人类可读的配置序列化语言，常用于编排与流水线定义。', 1, 72, NOW(), NOW(), NULL),
  (32, NULL, 3, 'Protocol Buffers', 'Google 推出的二进制序列化格式，常与 gRPC 搭配。', 1, 78, NOW(), NOW(), NULL),
  (33, NULL, 3, 'DevOps', '开发运维一体化文化与实践，强调协作、自动化与可观测性。', 1, 80, NOW(), NOW(), NULL);

-- 组 4：法律与合规（12 条）id 34–45
INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`, `cloned_from_system_id`)
VALUES
  (34, NULL, 4, 'GDPR', '欧盟《通用数据保护条例》，规范个人数据收集、处理与跨境传输。', 1, 88, NOW(), NOW(), NULL),
  (35, NULL, 4, 'PIPL', '《中华人民共和国个人信息保护法》，我国个人信息处理活动的基本法。', 1, 90, NOW(), NOW(), NULL),
  (36, NULL, 4, '告知同意', '处理个人信息前向个人告知处理目的、方式等事项并取得同意。', 1, 76, NOW(), NOW(), NULL),
  (37, NULL, 4, '数据出境', '将境内收集和产生的个人信息或重要数据向境外提供的行为。', 1, 78, NOW(), NOW(), NULL),
  (38, NULL, 4, '等保', '网络安全等级保护制度，按等级对网络与信息系统实施安全保护。', 1, 80, NOW(), NOW(), NULL),
  (39, NULL, 4, '隐私政策', '向用户说明个人信息处理规则、权利与联系方式的法律文件。', 1, 70, NOW(), NOW(), NULL),
  (40, NULL, 4, '最小必要', '处理个人信息限于实现处理目的的最小范围，不得过度收集。', 1, 72, NOW(), NOW(), NULL),
  (41, NULL, 4, '匿名化', '使个人信息主体无法被识别且不能复原的技术与过程。', 1, 68, NOW(), NOW(), NULL),
  (42, NULL, 4, '去标识化', '对个人信息技术处理使其在不借助额外信息时无法识别特定自然人。', 1, 68, NOW(), NOW(), NULL),
  (43, NULL, 4, 'DPA', '数据处理协议（Data Processing Agreement），委托处理场景下双方权利义务约定。', 1, 74, NOW(), NOW(), NULL),
  (44, NULL, 4, '审计追踪', '对关键操作留痕以便事后核查与举证，常见于合规与内控。', 1, 65, NOW(), NOW(), NULL),
  (45, NULL, 4, '敏感个人信息', '一旦泄露或非法使用易导致人格尊严侵害或人身、财产安全的个人信息。', 1, 84, NOW(), NOW(), NULL);

-- 组 5：教育与学术（3 条）id 46–48
INSERT INTO `terminology` (`id`, `owner_user_id`, `group_id`, `term`, `definition`, `enabled`, `sort_weight`, `create_time`, `update_time`, `cloned_from_system_id`)
VALUES
  (46, NULL, 5, 'MOOC', '大规模开放在线课程（Massive Open Online Course）。', 1, 75, NOW(), NOW(), NULL),
  (47, NULL, 5, '学分绩点', '将课程成绩映射为绩点并加权汇总，用于评价学业水平。', 1, 70, NOW(), NOW(), NULL),
  (48, NULL, 5, '开题报告', '学位论文工作启动阶段对选题意义、研究内容与技术路线的说明。', 1, 65, NOW(), NOW(), NULL);

-- 别名：每条术语至少 1 条（共 48 条）；部分词条附第二条常用说法
INSERT INTO `terminology_alias` (`terminology_id`, `alias`)
VALUES
  (1, '电子计算机断层扫描'),
  (2, '核磁共振'),
  (3, '重症监护室'),
  (4, 'ECG'),
  (5, '血葡萄糖'),
  (6, '单品编码'),
  (7, '标准品'),
  (8, '成交总额'),
  (9, '订单履行'),
  (10, '冷藏运输'),
  (11, '仓储配送一体化'),
  (12, '退货物流'),
  (13, '订单系统'),
  (14, '应用程序接口'),
  (15, 'RESTful'),
  (16, 'GQL'),
  (17, 'JSON Web Token'),
  (18, '开放授权'),
  (19, '持续集成持续部署'),
  (20, 'K8s'),
  (21, '容器'),
  (22, '微服务架构'),
  (23, '远程调用'),
  (24, 'GRPC'),
  (25, 'WS'),
  (26, '内容分发'),
  (27, '域名解析'),
  (28, '传输层安全'),
  (29, '安全 HTTP'),
  (30, 'JSON数据格式'),
  (31, 'YAML配置语法'),
  (32, 'Protobuf'),
  (33, '开发运维一体化'),
  (34, '欧盟数据保护条例'),
  (35, '个保法'),
  (36, '明示同意'),
  (37, '跨境提供数据'),
  (38, '等级保护'),
  (39, '隐私说明'),
  (40, '数据最小化'),
  (41, '不可识别化'),
  (42, '去标识处理'),
  (43, '数据处理合同'),
  (44, '操作留痕'),
  (45, '敏感个人数据'),
  (46, '慕课'),
  (47, 'GPA'),
  (48, '选题报告'),
  (1, '断层扫描'),
  (6, 'Stock Keeping Unit'),
  (14, '接口'),
  (20, '容器编排'),
  (34, '通用数据保护条例'),
  (46, '大规模在线开放课程');

ALTER TABLE `terminology_group` AUTO_INCREMENT = 6;
ALTER TABLE `terminology` AUTO_INCREMENT = 49;
ALTER TABLE `terminology_alias` AUTO_INCREMENT = 200;
