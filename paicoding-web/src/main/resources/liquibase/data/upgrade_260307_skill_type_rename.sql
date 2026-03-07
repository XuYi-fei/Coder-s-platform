-- =====================================================
-- Changeset 00000000000005: 将旧 skill_type 值迁移到新类型体系
--
-- 新类型体系：
--   SIMPLE  — 仅含 SKILL.md，可在线创建或上传只含 SKILL.md 的 zip
--   FOLDER  — 含 SKILL.md + 额外资源目录（scripts/、references/、assets/）
--
-- 旧类型映射：
--   PROMPT_TEMPLATE → SIMPLE  （并用 CONCAT 生成 SKILL.md frontmatter）
--   FILE_BASED      → FOLDER  （zip 里有额外资源）
--   CODE_SNIPPET    → SIMPLE
--   HTTP_PIPELINE   → SIMPLE
-- =====================================================

-- 1. PROMPT_TEMPLATE → SIMPLE，同时生成 skill_md（如果尚未填充）
UPDATE `skill_config`
SET
    skill_type = 'SIMPLE',
    skill_md   = CONCAT(
        '---', CHAR(10),
        'name: ', `name`, CHAR(10),
        'display_name: ', `display_name`, CHAR(10),
        'description: ', `description`, CHAR(10),
        '---', CHAR(10), CHAR(10),
        IFNULL(`content`, '')
    )
WHERE skill_type = 'PROMPT_TEMPLATE'
  AND (skill_md IS NULL OR skill_md = '');

-- 2. FILE_BASED → FOLDER
UPDATE `skill_config`
SET skill_type = 'FOLDER'
WHERE skill_type = 'FILE_BASED';

-- 3. CODE_SNIPPET / HTTP_PIPELINE → SIMPLE（历史遗留类型）
UPDATE `skill_config`
SET skill_type = 'SIMPLE'
WHERE skill_type IN ('CODE_SNIPPET', 'HTTP_PIPELINE');
