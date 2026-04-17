-- 为人员管理模块补充字段：证书审核状态 & 服务人员工作状态

ALTER TABLE `staff_service_skill`
    ADD COLUMN IF NOT EXISTS `certificate_status` TINYINT NOT NULL DEFAULT 0 COMMENT '证书审核状态：0-待审核，1-通过，2-驳回' AFTER `certificate_url`;

ALTER TABLE `staff`
    ADD COLUMN IF NOT EXISTS `work_status` TINYINT NOT NULL DEFAULT 1 COMMENT '工作状态：0-不可服务，1-正常，2-警告，3-黑名单' AFTER `rating`;

