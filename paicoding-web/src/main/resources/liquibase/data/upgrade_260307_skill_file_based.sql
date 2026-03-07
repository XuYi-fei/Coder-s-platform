-- =====================================================
-- Changeset 00000000000003: Extend skill_config for file-based skills
-- (tool seeds are now handled correctly by changeset 00000000000002)
-- =====================================================
ALTER TABLE `skill_config`
  ADD COLUMN `oss_path`    VARCHAR(500)  NULL COMMENT 'OSS 中 skill zip 文件的存储路径',
  ADD COLUMN `oss_url`     VARCHAR(500)  NULL COMMENT 'skill zip 文件的可访问 URL',
  ADD COLUMN `skill_md`    LONGTEXT      NULL COMMENT '解析出的 SKILL.md 原始内容（FILE_BASED 类型时有值）';
