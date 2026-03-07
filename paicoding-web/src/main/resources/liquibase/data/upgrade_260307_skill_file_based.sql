-- =====================================================
-- Changeset 00000000000003: Re-seed system tools (fix missing version column)
-- =====================================================
INSERT IGNORE INTO `tool_config`
  (`name`, `display_name`, `description`, `tool_type`, `input_schema`, `config`, `timeout_ms`, `retry_count`, `risk_level`, `visibility`, `status`, `is_system`, `creator_user_id`, `workspace_id`)
VALUES
('get_current_time', '获取当前时间', '返回服务器当前的日期和时间，适用于需要知道当前时间的场景', 'JAVA_FUNCTION',
 '{"type":"object","properties":{},"required":[]}',
 '{"bean_name":"builtinToolFunctions","method_name":"getCurrentTime"}',
 5000, 0, 1, 3, 1, 1, 0, 0),
('calculate', '数学计算', '计算数学表达式，支持加减乘除、括号、幂运算（用 ^ 表示）。示例：2+3*4, (10+5)/3', 'JAVA_FUNCTION',
 '{"type":"object","properties":{"expression":{"type":"string","description":"要计算的数学表达式，如 2+3*4"}},"required":["expression"]}',
 '{"bean_name":"builtinToolFunctions","method_name":"calculate"}',
 5000, 0, 1, 3, 1, 1, 0, 0),
('web_search', '网络搜索', '通过 DuckDuckGo 搜索互联网获取信息摘要。输入搜索关键词，返回摘要和相关链接。适合查询事实、获取最新信息', 'HTTP_API',
 '{"type":"object","properties":{"query":{"type":"string","description":"搜索关键词"}},"required":["query"]}',
 '{"url":"https://api.duckduckgo.com/?q={query}&format=json&no_html=1&skip_disambig=1","method":"GET"}',
 15000, 1, 1, 3, 1, 1, 0, 0),
('get_weather', '天气查询', '查询指定城市的当前天气情况，返回温度、天气状况等信息。输入城市名称（英文或中文拼音）', 'HTTP_API',
 '{"type":"object","properties":{"location":{"type":"string","description":"城市名称，如 Beijing、Shanghai、shenzhen"}},"required":["location"]}',
 '{"url":"https://wttr.in/{location}?format=j1","method":"GET"}',
 15000, 1, 1, 3, 1, 1, 0, 0),
('http_get', 'HTTP GET 请求', '向指定 URL 发起 HTTP GET 请求并返回响应内容。适合获取公开 API 数据', 'HTTP_API',
 '{"type":"object","properties":{"url":{"type":"string","description":"完整的 HTTP/HTTPS URL"}},"required":["url"]}',
 '{"url":"{url}","method":"GET"}',
 20000, 0, 2, 3, 1, 1, 0, 0);

-- =====================================================
-- Changeset 00000000000004: Extend skill_config for file-based skills
-- =====================================================
ALTER TABLE `skill_config`
  ADD COLUMN `oss_path`    VARCHAR(500)  NULL COMMENT 'OSS 中 skill zip 文件的存储路径',
  ADD COLUMN `oss_url`     VARCHAR(500)  NULL COMMENT 'skill zip 文件的可访问 URL',
  ADD COLUMN `skill_md`    LONGTEXT      NULL COMMENT '解析出的 SKILL.md 原始内容（FILE_BASED 类型时有值）';
