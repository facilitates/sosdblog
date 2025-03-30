# Sa-Token框架与MySQL数据库设计说明

## 数据库概述

本数据库设计主要围绕Sa-Token权限认证框架和博客系统的需求进行设计，包含用户认证、权限管理和博客内容管理三大模块。

## 权限认证模块（Sa-Token相关）

### 用户表（sys_user）

用户表存储系统用户的基本信息和登录凭证，是Sa-Token进行身份认证的基础。

- **主要字段**：
  - `id`：用户唯一标识
  - `username`：登录用户名，Sa-Token使用此字段作为登录ID
  - `password`：密码，存储加密后的密码
  - `status`：用户状态，用于控制用户是否可以登录
  - `deleted`：逻辑删除标记

### 角色表（sys_role）

角色表定义了系统中的各种角色，Sa-Token通过角色进行权限分组。

- **主要字段**：
  - `id`：角色ID
  - `role_name`：角色名称
  - `role_code`：角色编码，Sa-Token使用此字段进行角色判断
  - `status`：角色状态

### 权限表（sys_permission）

权限表定义了系统中的各种权限，是Sa-Token进行权限验证的基础。

- **主要字段**：
  - `id`：权限ID
  - `permission_name`：权限名称
  - `permission_code`：权限编码，Sa-Token使用此字段进行权限判断
  - `permission_type`：权限类型，区分菜单、按钮和接口权限
  - `parent_id`：父权限ID，用于构建权限树

### 用户角色关联表（sys_user_role）

用户和角色的多对多关系表，Sa-Token通过此表查询用户拥有的角色。

- **主要字段**：
  - `user_id`：用户ID
  - `role_id`：角色ID

### 角色权限关联表（sys_role_permission）

角色和权限的多对多关系表，Sa-Token通过此表查询角色拥有的权限。

- **主要字段**：
  - `role_id`：角色ID
  - `permission_id`：权限ID

## 博客内容模块

### 文章表（blog_article）

存储博客文章的主要内容。

- **主要字段**：
  - `id`：文章ID
  - `title`：文章标题
  - `content`：文章内容
  - `user_id`：作者ID，关联用户表
  - `category_id`：分类ID，关联分类表
  - `status`：文章状态

### 分类表（blog_category）

文章分类信息。

- **主要字段**：
  - `id`：分类ID
  - `category_name`：分类名称
  - `parent_id`：父分类ID，用于构建分类树

### 标签表（blog_tag）

文章标签信息。

- **主要字段**：
  - `id`：标签ID
  - `tag_name`：标签名称

### 文章标签关联表（blog_article_tag）

文章和标签的多对多关系表。

- **主要字段**：
  - `article_id`：文章ID
  - `tag_id`：标签ID

### 评论表（blog_comment）

文章评论信息。

- **主要字段**：
  - `id`：评论ID
  - `article_id`：文章ID
  - `user_id`：评论用户ID
  - `content`：评论内容
  - `parent_id`：父评论ID，用于构建评论树

## 日志模块

### 登录日志表（sys_login_log）

记录用户登录信息，用于安全审计和分析。

- **主要字段**：
  - `id`：日志ID
  - `user_id`：用户ID
  - `ip`：登录IP
  - `status`：登录状态
  - `login_time`：登录时间

### 操作日志表（sys_operation_log）

记录用户操作信息，用于系统审计和问题排查。

- **主要字段**：
  - `id`：日志ID
  - `user_id`：用户ID
  - `operation`：操作类型
  - `method`：方法名
  - `params`：请求参数
  - `operation_time`：操作时间

## Sa-Token集成说明

### 身份认证

1. 用户登录时，通过`username`和`password`进行身份验证
2. 验证成功后，调用`StpUtil.login(id)`进行登录，id为用户表的主键
3. Sa-Token会自动生成token并管理会话

### 权限验证

1. 基于角色的权限控制：
   - 通过`sys_user_role`表获取用户角色
   - 使用`StpUtil.hasRole("ROLE_ADMIN")`验证用户是否拥有某角色

2. 基于权限的细粒度控制：
   - 通过`sys_role_permission`和`sys_user_role`表获取用户权限
   - 使用`StpUtil.hasPermission("article:edit")`验证用户是否拥有某权限

### 会话管理

1. Sa-Token默认使用Redis存储会话数据，通过`spring-boot-starter-data-redis`依赖实现
2. 可以通过配置文件调整token有效期、Cookie设置等

### 注解鉴权

在Controller方法上使用Sa-Token提供的注解进行权限控制：

```java
// 登录验证
@SaCheckLogin
public String getUserInfo() { /* ... */ }

// 角色验证
@SaCheckRole("ROLE_ADMIN")
public String adminOperation() { /* ... */ }

// 权限验证
@SaCheckPermission("article:edit")
public String editArticle() { /* ... */ }
```

## 数据库索引说明

为提高查询性能，数据库设计中添加了以下关键索引：

1. 用户表：用户名和邮箱唯一索引
2. 角色表：角色编码唯一索引
3. 权限表：权限编码唯一索引
4. 文章表：分类ID、用户ID和创建时间索引
5. 评论表：文章ID、用户ID和父评论ID索引
6. 日志表：用户ID和操作时间索引

## 安全考虑

1. 密码存储使用BCrypt加密，不可逆
2. 使用逻辑删除保护用户数据
3. 记录登录日志和操作日志，便于安全审计
4. 状态字段控制资源可用性