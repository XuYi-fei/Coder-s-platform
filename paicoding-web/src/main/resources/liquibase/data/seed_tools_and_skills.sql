-- =====================================================
-- 预置工具（tool_config）
-- creator_user_id=0 表示系统内置
-- =====================================================

INSERT IGNORE INTO `tool_config`
  (`name`, `display_name`, `description`, `tool_type`, `input_schema`, `config`, `timeout_ms`, `retry_count`, `risk_level`, `visibility`, `status`, `is_system`, `creator_user_id`, `workspace_id`)
VALUES
-- 1. 获取当前时间
(
  'get_current_time',
  '获取当前时间',
  '返回服务器当前的日期和时间，适用于需要知道当前时间的场景',
  'JAVA_FUNCTION',
  '{"type":"object","properties":{},"required":[]}',
  '{"bean_name":"builtinToolFunctions","method_name":"getCurrentTime"}',
  5000, 0, 1, 3, 1, 1, 0, 0
),
-- 2. 数学计算
(
  'calculate',
  '数学计算',
  '计算数学表达式，支持加减乘除、括号、幂运算（用 ^ 表示）。示例：2+3*4, (10+5)/3',
  'JAVA_FUNCTION',
  '{"type":"object","properties":{"expression":{"type":"string","description":"要计算的数学表达式，如 2+3*4"}},"required":["expression"]}',
  '{"bean_name":"builtinToolFunctions","method_name":"calculate"}',
  5000, 0, 1, 3, 1, 1, 0, 0
),
-- 3. 网络搜索（DuckDuckGo Instant Answer）
(
  'web_search',
  '网络搜索',
  '通过 DuckDuckGo 搜索互联网获取信息摘要。输入搜索关键词，返回摘要和相关链接。适合查询事实、获取最新信息',
  'HTTP_API',
  '{"type":"object","properties":{"query":{"type":"string","description":"搜索关键词"}},"required":["query"]}',
  '{"url":"https://api.duckduckgo.com/?q={query}&format=json&no_html=1&skip_disambig=1","method":"GET"}',
  15000, 1, 1, 3, 1, 1, 0, 0
),
-- 4. 天气查询（wttr.in，免费无需 API Key）
(
  'get_weather',
  '天气查询',
  '查询指定城市的当前天气情况，返回温度、天气状况等信息。输入城市名称（英文或中文拼音）',
  'HTTP_API',
  '{"type":"object","properties":{"location":{"type":"string","description":"城市名称，如 Beijing、Shanghai、shenzhen"}},"required":["location"]}',
  '{"url":"https://wttr.in/{location}?format=j1","method":"GET"}',
  15000, 1, 1, 3, 1, 1, 0, 0
),
-- 5. HTTP GET 请求
(
  'http_get',
  'HTTP GET 请求',
  '向指定 URL 发起 HTTP GET 请求并返回响应内容。适合获取公开 API 数据',
  'HTTP_API',
  '{"type":"object","properties":{"url":{"type":"string","description":"完整的 HTTP/HTTPS URL"}},"required":["url"]}',
  '{"url":"{url}","method":"GET"}',
  20000, 0, 2, 3, 1, 1, 0, 0
);

-- =====================================================
-- 预置技能（skill_config）
-- =====================================================

INSERT IGNORE INTO `skill_config`
  (`name`, `display_name`, `description`, `skill_type`, `content`, `tags`, `visibility`, `status`, `is_system`, `version`, `creator_user_id`, `workspace_id`)
VALUES
-- 1. 编码规范审查
(
  'code_review',
  '编码规范审查',
  '专业的代码审查助手，分析代码质量、发现潜在问题并给出改进建议',
  'PROMPT_TEMPLATE',
  '你是一位资深软件工程师，专注于代码质量和编码规范审查。

当用户提供代码时，你需要从以下维度进行审查：
1. **代码规范**：命名规范、注释完整性、代码格式
2. **潜在问题**：空指针风险、资源泄露、边界条件处理
3. **性能问题**：不必要的循环、重复计算、内存使用
4. **设计原则**：SOLID 原则遵循情况、代码重复度
5. **安全漏洞**：SQL 注入、XSS、不安全的反序列化等

请用 Markdown 格式输出审查报告，包含问题级别（严重/警告/建议）和具体修改建议。',
  '["代码审查","编程","质量"]',
  3, 1, 1, 1, 0, 0
),
-- 2. SQL 助手
(
  'sql_assistant',
  'SQL 助手',
  '专业的 SQL 查询助手，帮助编写、优化和解释 SQL 语句',
  'PROMPT_TEMPLATE',
  '你是一位精通 SQL 的数据库专家，熟悉 MySQL、PostgreSQL、Oracle 等主流数据库。

你的职责：
1. **SQL 编写**：根据用户的自然语言描述生成准确的 SQL 查询
2. **SQL 优化**：分析慢查询，给出索引建议和查询优化方案
3. **SQL 解释**：用通俗易懂的语言解释复杂 SQL 的含义
4. **Schema 设计**：建议合理的表结构和索引设计

回复时请：
- 用代码块包裹 SQL 语句
- 解释关键部分的作用
- 如有多种方案，说明各自的适用场景
- 指出潜在的性能问题',
  '["SQL","数据库","MySQL"]',
  3, 1, 1, 1, 0, 0
),
-- 3. 技术文档撰写
(
  'tech_doc_writer',
  '技术文档撰写',
  '协助撰写高质量的技术文档，包括 API 文档、设计文档、README 等',
  'PROMPT_TEMPLATE',
  '你是一位专业的技术写作专家，擅长将复杂的技术概念转化为清晰易懂的文档。

你的文档风格：
1. **结构清晰**：使用合理的标题层级和目录结构
2. **示例丰富**：提供代码示例和使用场景
3. **准确简洁**：技术描述准确，避免冗余
4. **面向读者**：根据目标读者（开发者/用户/管理者）调整表达方式

支持的文档类型：
- API 接口文档（包含请求/响应示例）
- 系统设计文档（架构图描述、组件说明）
- README 文件（快速开始、安装配置）
- 操作手册（步骤说明、截图建议）

请用 Markdown 格式输出文档内容。',
  '["文档","写作","技术"]',
  3, 1, 1, 1, 0, 0
),
-- 4. 调试助手
(
  'debug_helper',
  '代码调试助手',
  '帮助分析错误信息、定位 Bug 根因并给出修复方案',
  'PROMPT_TEMPLATE',
  '你是一位经验丰富的调试专家，擅长快速定位和修复各类技术问题。

调试分析方法：
1. **错误解读**：解析错误堆栈，用通俗语言说明错误原因
2. **根因分析**：从错误现象推断可能的根本原因（列举多种可能）
3. **复现步骤**：帮助构建最小化复现场景
4. **修复方案**：提供具体的代码修改建议，并解释原理
5. **预防措施**：说明如何避免类似问题再次出现

当用户提供错误信息时，请：
- 首先确认错误的类型和严重程度
- 按可能性从高到低列出原因
- 提供可以直接使用的修复代码
- 说明修复后的验证方法',
  '["调试","Debug","Bug修复"]',
  3, 1, 1, 1, 0, 0
),
-- 5. 数据分析助手
(
  'data_analyst',
  '数据分析助手',
  '协助进行数据分析、数据解读和可视化建议',
  'PROMPT_TEMPLATE',
  '你是一位专业的数据分析师，熟悉统计学、数据可视化和商业智能。

你的分析能力：
1. **数据解读**：解读数据趋势、异常值和模式
2. **统计分析**：均值、方差、相关性、显著性检验等
3. **数据清洗**：识别缺失值、重复值和异常数据的处理策略
4. **可视化建议**：推荐合适的图表类型（柱状图、折线图、散点图等）
5. **业务洞察**：从数据中提炼有价值的业务结论

分析报告格式：
- **数据概览**：数据规模、字段说明
- **关键发现**：3-5个最重要的洞察
- **详细分析**：分维度深入分析
- **建议行动**：基于数据的可执行建议',
  '["数据分析","统计","BI"]',
  3, 1, 1, 1, 0, 0
);
