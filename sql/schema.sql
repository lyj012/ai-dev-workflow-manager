CREATE TABLE workflow_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  title VARCHAR(200) NOT NULL COMMENT '任务标题',
  description TEXT NULL COMMENT '任务描述',
  task_type VARCHAR(50) NOT NULL COMMENT '任务类型',
  complexity VARCHAR(30) NOT NULL COMMENT '复杂度：SIMPLE/MEDIUM/COMPLEX',
  risk_tags JSON NULL COMMENT '风险标签',
  status VARCHAR(30) NOT NULL DEFAULT 'DRAFT' COMMENT '任务状态',
  matched_template_id BIGINT NULL COMMENT '命中的 workflow 模板 ID',
  test_checklist_generated TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已生成测试清单',
  delivery_record_id BIGINT NULL COMMENT '交付记录 ID',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_workflow_task_status (status),
  INDEX idx_workflow_task_type_complexity (task_type, complexity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 开发任务';

CREATE TABLE workflow_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(100) NOT NULL COMMENT '模板名称',
  description VARCHAR(500) NULL COMMENT '模板说明',
  task_type VARCHAR(50) NOT NULL COMMENT '适用任务类型',
  complexity VARCHAR(30) NOT NULL COMMENT '适用复杂度',
  risk_tags JSON NULL COMMENT '适用风险标签',
  priority INT NOT NULL DEFAULT 0 COMMENT '优先级',
  version INT NOT NULL DEFAULT 1 COMMENT '版本号',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_workflow_template_match (task_type, complexity, enabled),
  INDEX idx_workflow_template_priority (priority, version)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Workflow 模板';

CREATE TABLE workflow_template_stage (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  template_id BIGINT NOT NULL COMMENT '模板 ID',
  stage_key VARCHAR(50) NOT NULL COMMENT '阶段标识',
  stage_name VARCHAR(100) NOT NULL COMMENT '阶段名称',
  stage_order INT NOT NULL COMMENT '阶段顺序',
  required TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必需',
  default_prompt_template_id BIGINT NULL COMMENT '默认 prompt 模板 ID',
  description VARCHAR(500) NULL COMMENT '阶段说明',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_template_stage_template (template_id),
  UNIQUE KEY uk_template_stage_order (template_id, stage_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Workflow 模板阶段';

CREATE TABLE workflow_stage (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT NOT NULL COMMENT '任务 ID',
  template_stage_id BIGINT NULL COMMENT '来源模板阶段 ID',
  stage_key VARCHAR(50) NOT NULL COMMENT '阶段标识',
  stage_name VARCHAR(100) NOT NULL COMMENT '阶段名称',
  stage_order INT NOT NULL COMMENT '阶段顺序',
  status VARCHAR(30) NOT NULL DEFAULT 'PENDING' COMMENT '阶段状态',
  input_summary TEXT NULL COMMENT '阶段输入摘要',
  output_summary TEXT NULL COMMENT '阶段输出摘要',
  started_at DATETIME NULL COMMENT '开始时间',
  completed_at DATETIME NULL COMMENT '完成时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_workflow_stage_task (task_id),
  INDEX idx_workflow_stage_status (status),
  UNIQUE KEY uk_task_stage_order (task_id, stage_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行阶段';

CREATE TABLE prompt_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  name VARCHAR(100) NOT NULL COMMENT '模板名称',
  prompt_type VARCHAR(50) NOT NULL COMMENT '模板类型',
  content TEXT NOT NULL COMMENT '模板内容',
  variables JSON NULL COMMENT '变量说明',
  enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_prompt_template_type (prompt_type, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Prompt 模板';

CREATE TABLE delivery_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
  task_id BIGINT NOT NULL COMMENT '任务 ID',
  summary TEXT NULL COMMENT '交付总结',
  test_checklist TEXT NULL COMMENT '测试清单',
  test_result TEXT NULL COMMENT '测试结果',
  risk_notes TEXT NULL COMMENT '风险说明',
  markdown_content MEDIUMTEXT NULL COMMENT 'Markdown 导出内容',
  delivered_at DATETIME NULL COMMENT '交付时间',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_delivery_record_task (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交付记录';
