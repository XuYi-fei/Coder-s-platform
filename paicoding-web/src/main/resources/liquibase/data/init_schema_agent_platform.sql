-- =====================================================
-- Agent Platform 基础表结构
-- 创建时间：2026-03-06
-- 说明：全新的 Agent 基础设施调用平台数据库初始化脚本
-- =====================================================

-- =====================================================
-- 1. 工作区与权限层
-- =====================================================

-- 工作区主表
CREATE TABLE IF NOT EXISTS `workspace` (
    `workspace_id`    BIGINT       NOT NULL AUTO_INCREMENT,
    `name`            VARCHAR(100) NOT NULL                COMMENT '工作区名称',
    `description`     VARCHAR(500)                         COMMENT '描述',
    `owner_user_id`   BIGINT       NOT NULL                COMMENT '创建者/所有者 user_id',
    `avatar_url`      VARCHAR(255)                         COMMENT '工作区头像',
    `status`          TINYINT      NOT NULL DEFAULT 1      COMMENT '1=正常 2=禁用',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`workspace_id`),
    INDEX `idx_owner` (`owner_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作区';

-- 工作区成员
CREATE TABLE IF NOT EXISTS `workspace_member` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `workspace_id`    BIGINT       NOT NULL                COMMENT '工作区 ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '成员 user_id',
    `role`            TINYINT      NOT NULL                COMMENT '1=OWNER 2=ADMIN 3=MEMBER 4=VIEWER',
    `invited_by`      BIGINT                               COMMENT '邀请人 user_id',
    `joined_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_ws_user` (`workspace_id`, `user_id`),
    INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作区成员';

-- 资源细粒度权限（SPECIFIED 可见性时使用）
-- 适用于 AGENT / TOOL / SKILL / MODEL 四类资源
CREATE TABLE IF NOT EXISTS `resource_permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT,
    `resource_type`   VARCHAR(20)  NOT NULL                COMMENT 'AGENT/TOOL/SKILL/MODEL',
    `resource_id`     BIGINT       NOT NULL                COMMENT '资源 ID',
    `subject_type`    VARCHAR(20)  NOT NULL                COMMENT 'USER/WORKSPACE/ROLE',
    `subject_id`      BIGINT       NOT NULL                COMMENT '被授权对象 ID',
    `access_level`    TINYINT      NOT NULL                COMMENT '1=READ 2=USE 3=EDIT 4=MANAGE',
    `granted_by`      BIGINT       NOT NULL                COMMENT '授权操作人 user_id',
    `expire_at`       DATETIME                             COMMENT '到期时间，NULL=永不过期',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_resource_subject` (`resource_type`, `resource_id`, `subject_type`, `subject_id`),
    INDEX `idx_resource` (`resource_type`, `resource_id`),
    INDEX `idx_subject` (`subject_type`, `subject_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源细粒度权限';

-- =====================================================
-- 2. 模型配置层
-- =====================================================

-- 模型配置（支持多 Provider，API Key 加密存储）
CREATE TABLE IF NOT EXISTS `model_config` (
    `model_config_id` BIGINT        NOT NULL AUTO_INCREMENT,
    `workspace_id`    BIGINT                               COMMENT '所属工作区，NULL=系统级全局可见',
    `name`            VARCHAR(100)  NOT NULL                COMMENT '显示名称，如 通义千问-Max',
    `provider`        VARCHAR(50)   NOT NULL                COMMENT 'ALIBABA/OPENAI/ANTHROPIC/CUSTOM',
    `model_name`      VARCHAR(100)  NOT NULL                COMMENT '实际模型标识，如 qwen-max',
    `base_url`        VARCHAR(255)  NOT NULL                COMMENT 'API BaseURL',
    `api_key`         VARCHAR(512)  NOT NULL                COMMENT 'API Key，AES 加密存储',
    `max_tokens`      INT           NOT NULL DEFAULT 4096,
    `temperature`     DECIMAL(3,2)  NOT NULL DEFAULT 0.70,
    `extra_params`    JSON                                  COMMENT '扩展参数，如 top_p、stream 等',
    `visibility`      TINYINT       NOT NULL DEFAULT 1      COMMENT '1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED',
    `status`          TINYINT       NOT NULL DEFAULT 1      COMMENT '1=正常 2=禁用',
    `creator_user_id` BIGINT        NOT NULL,
    `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`model_config_id`),
    INDEX `idx_workspace` (`workspace_id`),
    INDEX `idx_creator` (`creator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型配置';

-- =====================================================
-- 3. Agent 配置层
-- =====================================================

-- Agent 主配置表
CREATE TABLE IF NOT EXISTS `agent_config` (
    `agent_id`                   BIGINT       NOT NULL AUTO_INCREMENT,
    `workspace_id`               BIGINT       NOT NULL                COMMENT '所属工作区',
    `name`                       VARCHAR(100) NOT NULL                COMMENT 'Agent 名称',
    `description`                VARCHAR(500)                         COMMENT 'Agent 描述',
    `avatar_url`                 VARCHAR(255)                         COMMENT 'Agent 头像',

    -- 执行模式
    `execution_mode`             VARCHAR(30)  NOT NULL                COMMENT 'REACT/CHAIN/ROUTE/PLANNER_CRITIC/SUPERVISOR_WORKER',

    -- 模型绑定
    `model_config_id`            BIGINT       NOT NULL                COMMENT '默认使用的模型',
    `critic_model_config_id`     BIGINT                               COMMENT 'Critic 专用模型，NULL 则同主模型',

    -- Prompt 配置
    `system_prompt`              TEXT         NOT NULL                COMMENT 'Agent 人设与职责描述',
    `planner_prompt`             TEXT                                 COMMENT 'Planner 专用指令（PLANNER_CRITIC 模式）',
    `critic_prompt`              TEXT                                 COMMENT 'Critic 专用指令（PLANNER_CRITIC 模式）',

    -- Critic 评分维度配置（JSON）
    -- 示例：{"feasibility":{"weight":30,"threshold":60},"completeness":{"weight":40,"threshold":70},...}
    `critic_score_config`        JSON                                 COMMENT 'Critic 各维度权重与通过阈值',
    `critic_pass_score`          INT          NOT NULL DEFAULT 70     COMMENT 'Critic 综合通过分数线（0-100）',

    -- 执行控制参数
    `max_iterations`             INT          NOT NULL DEFAULT 3      COMMENT 'Planner-Critic 最大循环轮次',
    `max_steps`                  INT          NOT NULL DEFAULT 10     COMMENT '单次执行最大步骤数',
    `timeout_seconds`            INT          NOT NULL DEFAULT 120    COMMENT '整体执行超时秒数',
    `parallel_enabled`           TINYINT      NOT NULL DEFAULT 0      COMMENT '是否允许步骤并行执行',

    -- 人工审核触发配置
    `human_review_on_mutation`   TINYINT      NOT NULL DEFAULT 1      COMMENT '写操作（MUTATION/DANGEROUS）是否强制人工确认',
    `human_review_on_uncertain`  TINYINT      NOT NULL DEFAULT 0      COMMENT '低置信度时是否转人工',
    `uncertainty_threshold`      DECIMAL(3,2)          DEFAULT 0.60   COMMENT '置信度阈值',

    -- 权限与生命周期
    `visibility`                 TINYINT      NOT NULL DEFAULT 1      COMMENT '1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED',
    `status`                     TINYINT      NOT NULL DEFAULT 1      COMMENT '1=DRAFT 2=ACTIVE 3=ARCHIVED',
    `is_system`                  TINYINT      NOT NULL DEFAULT 0      COMMENT '1=管理员内置，不可删除',
    `version`                    INT          NOT NULL DEFAULT 1      COMMENT '当前版本号',

    `creator_user_id`            BIGINT       NOT NULL,
    `created_at`                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`                 DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`agent_id`),
    INDEX `idx_workspace` (`workspace_id`),
    INDEX `idx_creator` (`creator_user_id`),
    INDEX `idx_status_visibility` (`status`, `visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 主配置';

-- Agent 版本快照（每次发布保存，支持回滚）
CREATE TABLE IF NOT EXISTS `agent_version` (
    `version_id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `agent_id`            BIGINT        NOT NULL                COMMENT '所属 Agent',
    `version`             INT           NOT NULL                COMMENT '版本号',
    `snapshot`            JSON          NOT NULL                COMMENT '该版本的完整配置快照',
    `change_note`         VARCHAR(500)                          COMMENT '本次变更说明',
    `publisher_user_id`   BIGINT        NOT NULL,
    `published_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`version_id`),
    UNIQUE KEY `uk_agent_version` (`agent_id`, `version`),
    INDEX `idx_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 版本快照';

-- =====================================================
-- 4. Tool 配置层
-- =====================================================

-- 工具主表（多态设计，tool_type 决定 config 结构）
-- tool_type 及对应 config 示例：
--   HTTP_API   : {"url":"...","method":"POST","headers":{},"body_template":{},"auth_type":"BEARER","response_jpath":"$.data"}
--   MCP        : {"mcp_server_id":123,"tool_name":"brave_search"}
--   AGENT_DELEGATE : {"delegate_agent_id":456,"pass_context":true}
--   JAVA_FUNCTION  : {"bean_name":"weatherService","method_name":"getCurrentWeather"}
--   SCRIPT     : {"script_type":"PYTHON","script_content":"def run(params): ..."}
--   DATABASE_QUERY : {"datasource":"read_replica","sql_template":"SELECT ...","allowed_tables":["article"]}
CREATE TABLE IF NOT EXISTS `tool_config` (
    `tool_id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `workspace_id`      BIGINT        NOT NULL                COMMENT '所属工作区',
    `name`              VARCHAR(100)  NOT NULL                COMMENT '工具名（供 LLM 识别，建议英文下划线）',
    `display_name`      VARCHAR(100)  NOT NULL                COMMENT '显示名称',
    `description`       TEXT          NOT NULL                COMMENT '工具功能描述，LLM 依赖此字段决定是否调用',
    `tool_type`         VARCHAR(30)   NOT NULL                COMMENT 'HTTP_API/MCP/AGENT_DELEGATE/JAVA_FUNCTION/SCRIPT/DATABASE_QUERY',

    -- 调用契约
    `input_schema`      JSON          NOT NULL                COMMENT '入参 JSON Schema',
    `output_schema`     JSON                                  COMMENT '出参 JSON Schema',

    -- 执行配置（根据 tool_type 结构不同）
    `config`            JSON          NOT NULL                COMMENT '各类型的执行配置，见注释',

    -- 执行控制
    `timeout_ms`        INT           NOT NULL DEFAULT 10000,
    `retry_count`       TINYINT       NOT NULL DEFAULT 0,
    `retry_interval_ms` INT           NOT NULL DEFAULT 1000,

    -- 风险分级（决定是否需要 Critic/人工审核）
    `risk_level`        TINYINT       NOT NULL DEFAULT 1      COMMENT '1=READ 2=MUTATION 3=DANGEROUS',

    -- 权限与状态
    `visibility`        TINYINT       NOT NULL DEFAULT 1      COMMENT '1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED',
    `status`            TINYINT       NOT NULL DEFAULT 1      COMMENT '1=正常 2=禁用',
    `is_system`         TINYINT       NOT NULL DEFAULT 0      COMMENT '1=管理员内置',
    `creator_user_id`   BIGINT        NOT NULL,
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`tool_id`),
    INDEX `idx_workspace` (`workspace_id`),
    INDEX `idx_type` (`tool_type`),
    INDEX `idx_creator` (`creator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工具配置（多态）';

-- MCP 服务器注册表
CREATE TABLE IF NOT EXISTS `mcp_server` (
    `mcp_server_id`     BIGINT        NOT NULL AUTO_INCREMENT,
    `workspace_id`      BIGINT        NOT NULL                COMMENT '所属工作区',
    `name`              VARCHAR(100)  NOT NULL                COMMENT 'MCP 服务器名称',
    `description`       VARCHAR(500)                          COMMENT '描述',
    `server_type`       VARCHAR(10)   NOT NULL                COMMENT 'STDIO/SSE/HTTP',
    `endpoint`          VARCHAR(255)                          COMMENT 'SSE 或 HTTP 的服务地址',
    `command`           VARCHAR(255)                          COMMENT 'STDIO 启动命令，如 npx -y @modelcontextprotocol/server-brave-search',
    `args`              JSON                                  COMMENT 'STDIO 命令参数列表',
    `env_vars`          JSON                                  COMMENT '环境变量（敏感值加密存储）',
    `health_check_url`  VARCHAR(255)                          COMMENT '健康检查地址',
    `last_ping_at`      DATETIME                              COMMENT '最近一次心跳时间',
    `status`            TINYINT       NOT NULL DEFAULT 1      COMMENT '1=ACTIVE 2=OFFLINE 3=ERROR',
    `visibility`        TINYINT       NOT NULL DEFAULT 2      COMMENT '1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED，默认工作区可见',
    `is_system`         TINYINT       NOT NULL DEFAULT 0,
    `creator_user_id`   BIGINT        NOT NULL,
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`mcp_server_id`),
    INDEX `idx_workspace` (`workspace_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MCP 服务器注册';

-- =====================================================
-- 5. Skill 配置层
-- =====================================================

-- Skill 主表
-- skill_type 及对应 config 示例：
--   PROMPT_TEMPLATE : content 字段存 Prompt 模板，config 为 NULL
--   RAG_QUERY       : config={"collection_id":1,"top_k":5,"score_threshold":0.7}
--   CODE_SNIPPET    : config={"language":"python","code":"def run(params): ..."}
--   HTTP_PIPELINE   : config={"steps":[{"url":"...","method":"GET"}]}
CREATE TABLE IF NOT EXISTS `skill_config` (
    `skill_id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `workspace_id`      BIGINT        NOT NULL                COMMENT '所属工作区',
    `name`              VARCHAR(100)  NOT NULL                COMMENT 'Skill 名称（供 LLM 识别，建议英文下划线）',
    `display_name`      VARCHAR(100)  NOT NULL                COMMENT '显示名称',
    `description`       TEXT          NOT NULL                COMMENT '技能描述，LLM 依赖此字段决定是否调用',
    `skill_type`        VARCHAR(30)   NOT NULL                COMMENT 'PROMPT_TEMPLATE/RAG_QUERY/CODE_SNIPPET/HTTP_PIPELINE',

    -- Skill 内容
    `content`           LONGTEXT                              COMMENT 'PROMPT_TEMPLATE 类型的提示词模板内容',
    `input_schema`      JSON                                  COMMENT '入参定义（JSON Schema）',
    `output_schema`     JSON                                  COMMENT '出参格式定义',
    `config`            JSON                                  COMMENT '其他类型的扩展配置，见注释',

    -- 标签（用于搜索和分类）
    `tags`              JSON                                  COMMENT '标签列表，如 ["搜索","数据处理"]',

    -- 权限与生命周期
    `visibility`        TINYINT       NOT NULL DEFAULT 1      COMMENT '1=PRIVATE 2=WORKSPACE 3=PUBLIC 4=SPECIFIED',
    `status`            TINYINT       NOT NULL DEFAULT 1      COMMENT '1=正常 2=禁用',
    `is_system`         TINYINT       NOT NULL DEFAULT 0      COMMENT '1=管理员内置',
    `version`           INT           NOT NULL DEFAULT 1,
    `creator_user_id`   BIGINT        NOT NULL,
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (`skill_id`),
    INDEX `idx_workspace` (`workspace_id`),
    INDEX `idx_type` (`skill_type`),
    INDEX `idx_creator` (`creator_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Skill 配置';

-- Skill 版本快照
CREATE TABLE IF NOT EXISTS `skill_version` (
    `version_id`          BIGINT        NOT NULL AUTO_INCREMENT,
    `skill_id`            BIGINT        NOT NULL,
    `version`             INT           NOT NULL,
    `snapshot`            JSON          NOT NULL                COMMENT '该版本的完整配置快照',
    `change_note`         VARCHAR(500)                          COMMENT '变更说明',
    `publisher_user_id`   BIGINT        NOT NULL,
    `published_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`version_id`),
    UNIQUE KEY `uk_skill_version` (`skill_id`, `version`),
    INDEX `idx_skill` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Skill 版本快照';

-- =====================================================
-- 6. 关联关系层
-- =====================================================

-- Agent 绑定 Tool
CREATE TABLE IF NOT EXISTS `agent_tool_rel` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `agent_id`          BIGINT        NOT NULL,
    `tool_id`           BIGINT        NOT NULL,
    `sort_order`        INT           NOT NULL DEFAULT 0      COMMENT '工具列表展示顺序',
    `is_enabled`        TINYINT       NOT NULL DEFAULT 1,
    `override_config`   JSON                                  COMMENT 'Agent 级别对 Tool 配置的局部覆盖（如超时、风险等级）',
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_tool` (`agent_id`, `tool_id`),
    INDEX `idx_agent` (`agent_id`),
    INDEX `idx_tool` (`tool_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 与 Tool 关联';

-- Agent 绑定 Skill
CREATE TABLE IF NOT EXISTS `agent_skill_rel` (
    `id`                BIGINT        NOT NULL AUTO_INCREMENT,
    `agent_id`          BIGINT        NOT NULL,
    `skill_id`          BIGINT        NOT NULL,
    `sort_order`        INT           NOT NULL DEFAULT 0,
    `is_enabled`        TINYINT       NOT NULL DEFAULT 1,
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_agent_skill` (`agent_id`, `skill_id`),
    INDEX `idx_agent` (`agent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 与 Skill 关联';

-- =====================================================
-- 7. 执行运行时层
-- =====================================================

-- 执行会话（一次用户提问 = 一个 Session）
CREATE TABLE IF NOT EXISTS `agent_session` (
    `session_id`        BIGINT        NOT NULL AUTO_INCREMENT,
    `workspace_id`      BIGINT        NOT NULL,
    `entry_agent_id`    BIGINT        NOT NULL                COMMENT '入口 Agent（负责路由/规划）',
    `user_id`           BIGINT        NOT NULL,
    `user_input`        TEXT          NOT NULL                COMMENT '用户原始输入',
    `final_output`      TEXT                                  COMMENT '最终返回给用户的结果',
    `status`            VARCHAR(20)   NOT NULL DEFAULT 'PLANNING'
                                                              COMMENT 'PLANNING/CRITIC_REVIEW/EXECUTING/HUMAN_PENDING/COMPLETED/FAILED',
    `tokens_total`      INT           NOT NULL DEFAULT 0      COMMENT '本次会话总 token 消耗',
    `cost_usd`          DECIMAL(10,6)                         COMMENT '估算费用（美元）',
    `started_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `completed_at`      DATETIME,
    PRIMARY KEY (`session_id`),
    INDEX `idx_workspace_user` (`workspace_id`, `user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_started` (`started_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent 执行会话';

-- 执行计划（Planner 生成，Critic 评审，支持多版本迭代）
CREATE TABLE IF NOT EXISTS `execution_plan` (
    `plan_id`             BIGINT        NOT NULL AUTO_INCREMENT,
    `session_id`          BIGINT        NOT NULL,
    `plan_version`        INT           NOT NULL DEFAULT 1    COMMENT '第几轮规划迭代',
    `planner_agent_id`    BIGINT        NOT NULL,
    `plan_content`        JSON          NOT NULL              COMMENT '结构化步骤列表，含 depends_on 依赖关系',
    -- Critic 评审结果
    `critic_agent_id`     BIGINT                              COMMENT '评测此计划的 Critic Agent',
    `critic_score`        INT                                 COMMENT 'Critic 综合评分（0-100）',
    `critic_scores_detail` JSON                               COMMENT '各维度分数，如 {"feasibility":80,"completeness":90}',
    `critic_feedback`     TEXT                                COMMENT 'Critic 修改意见',
    `decision`            VARCHAR(10)                         COMMENT 'APPROVE/REJECT/REVISE',
    -- 状态
    `status`              VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                                                              COMMENT 'PENDING/APPROVED/REJECTED/SUPERSEDED',
    `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `reviewed_at`         DATETIME,
    PRIMARY KEY (`plan_id`),
    INDEX `idx_session` (`session_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行计划（含 Critic 评审记录）';

-- 执行步骤（计划通过后逐步执行）
CREATE TABLE IF NOT EXISTS `execution_step` (
    `step_id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `plan_id`           BIGINT        NOT NULL,
    `session_id`        BIGINT        NOT NULL,
    `step_order`        INT           NOT NULL                COMMENT '步骤序号',
    `depends_on`        JSON                                  COMMENT '依赖的 step_id 列表，为空则可并行执行',
    -- 执行主体
    `agent_id`          BIGINT                                COMMENT '负责执行此步骤的 Agent',
    `tool_id`           BIGINT                                COMMENT '调用的 Tool（NULL 则为纯 LLM 推理）',
    `skill_id`          BIGINT                                COMMENT '使用的 Skill',
    -- 执行内容
    `step_desc`         VARCHAR(500)                          COMMENT '步骤描述（来自计划）',
    `input_params`      JSON                                  COMMENT '实际传入参数',
    `output_result`     JSON                                  COMMENT '执行结果',
    `error_message`     TEXT,
    -- 状态与耗时
    `status`            VARCHAR(20)   NOT NULL DEFAULT 'PENDING'
                                                              COMMENT 'PENDING/RUNNING/COMPLETED/FAILED/SKIPPED/HUMAN_PENDING',
    `tokens_used`       INT           NOT NULL DEFAULT 0,
    `duration_ms`       INT,
    `started_at`        DATETIME,
    `completed_at`      DATETIME,
    PRIMARY KEY (`step_id`),
    INDEX `idx_plan` (`plan_id`),
    INDEX `idx_session` (`session_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='执行步骤';

-- 人工审核队列（危险操作 / 低置信度时触发）
CREATE TABLE IF NOT EXISTS `human_review_queue` (
    `review_id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `session_id`          BIGINT        NOT NULL,
    `step_id`             BIGINT                              COMMENT '关联步骤，NULL 则为计划级审核',
    `review_type`         VARCHAR(30)   NOT NULL              COMMENT 'DANGEROUS_TOOL/LOW_CONFIDENCE/PLAN_APPROVAL',
    `context`             JSON          NOT NULL              COMMENT '供审核人查看的上下文信息',
    `trigger_reason`      VARCHAR(500)                        COMMENT '触发原因',
    -- 审核结果
    `reviewer_user_id`    BIGINT,
    `decision`            VARCHAR(10)                         COMMENT 'APPROVE/REJECT/MODIFY',
    `review_note`         VARCHAR(500),
    -- 超时兜底
    `expire_at`           DATETIME      NOT NULL              COMMENT '超时时间',
    `timeout_action`      VARCHAR(10)   NOT NULL DEFAULT 'REJECT' COMMENT '超时自动决策：APPROVE/REJECT/CANCEL',
    `status`              VARCHAR(10)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PROCESSED/EXPIRED',
    `created_at`          DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `reviewed_at`         DATETIME,
    PRIMARY KEY (`review_id`),
    INDEX `idx_session` (`session_id`),
    INDEX `idx_status_expire` (`status`, `expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人工审核队列';

-- =====================================================
-- 8. 审计与统计层
-- =====================================================

-- 资源使用统计（按天聚合，供配额控制和计费参考）
CREATE TABLE IF NOT EXISTS `resource_usage_stat` (
    `stat_id`           BIGINT        NOT NULL AUTO_INCREMENT,
    `workspace_id`      BIGINT        NOT NULL,
    `user_id`           BIGINT        NOT NULL,
    `stat_date`         DATE          NOT NULL,
    `agent_id`          BIGINT                                COMMENT '粒度到具体 Agent，NULL=工作区汇总',
    `model_config_id`   BIGINT,
    `session_count`     INT           NOT NULL DEFAULT 0,
    `tokens_input`      BIGINT        NOT NULL DEFAULT 0,
    `tokens_output`     BIGINT        NOT NULL DEFAULT 0,
    `tool_call_count`   INT           NOT NULL DEFAULT 0,
    `cost_usd`          DECIMAL(10,4) NOT NULL DEFAULT 0.0000,
    PRIMARY KEY (`stat_id`),
    UNIQUE KEY `uk_stat` (`workspace_id`, `user_id`, `stat_date`, `agent_id`),
    INDEX `idx_workspace_date` (`workspace_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源使用统计';

-- 操作审计日志（关键操作留痕，不可删除）
CREATE TABLE IF NOT EXISTS `audit_log` (
    `log_id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `workspace_id`      BIGINT        NOT NULL,
    `operator_user_id`  BIGINT        NOT NULL,
    `action`            VARCHAR(50)   NOT NULL                COMMENT 'CREATE_AGENT/PUBLISH_AGENT/DELETE_TOOL/GRANT_PERMISSION/...',
    `resource_type`     VARCHAR(20)                           COMMENT 'AGENT/TOOL/SKILL/MODEL/WORKSPACE',
    `resource_id`       BIGINT,
    `before_snapshot`   JSON                                  COMMENT '变更前快照',
    `after_snapshot`    JSON                                  COMMENT '变更后快照',
    `ip_address`        VARCHAR(50),
    `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`log_id`),
    INDEX `idx_workspace` (`workspace_id`),
    INDEX `idx_operator` (`operator_user_id`),
    INDEX `idx_resource` (`resource_type`, `resource_id`),
    INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作审计日志';

-- =====================================================
-- 9. 全局动态配置（DynamicConfigContainer 依赖）
-- =====================================================
CREATE TABLE IF NOT EXISTS `global_conf` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `key`         VARCHAR(128) NOT NULL                COMMENT '配置项名称',
    `value`       VARCHAR(256) NOT NULL                COMMENT '配置项值',
    `comment`     VARCHAR(256)          DEFAULT ''     COMMENT '配置描述',
    `deleted`     TINYINT      NOT NULL DEFAULT 0,
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_key` (`key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='全局动态配置';
