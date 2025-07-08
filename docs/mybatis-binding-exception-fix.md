# MyBatis绑定异常修复指南

## 问题描述

在微信登录功能中遇到以下错误：

```
org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): com.biangqiang.freshdelivery.mapper.UserMapper.selectByOpenId
```

## 错误分析

### 根本原因

1. **缺少SQL映射**：`UserMapper` 接口定义了 `selectByOpenId` 方法，但没有对应的SQL实现
2. **映射方式不一致**：项目中 `AdminMapper` 使用注解方式（`@Select`），而 `UserMapper` 没有使用任何映射方式
3. **缺少XML映射文件**：在 `resources` 目录下没有找到 `UserMapper.xml` 文件

### 错误日志分析

```
INFO  c.b.freshdelivery.util.WeChatUtil - 微信登录响应: {"session_key":"xfvMVJrXWVKoi0MQA4DmCg==","openid":"orn1B4-DftowsbgwOgW7Ei97uXg4"}
ERROR o.a.c.c.C.[.[.[.[dispatcherServlet] - Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed; nested exception is org.apache.ibatis.binding.BindingException: Invalid bound statement (not found): com.biangqiang.freshdelivery.mapper.UserMapper.selectByOpenId]
```

**分析**：
- 微信API调用成功，获取到了 `openid`
- 在尝试通过 `openid` 查询用户时，MyBatis无法找到对应的SQL语句
- 导致整个登录流程失败

## 解决方案

### 方案1：使用注解方式（已实施）

为 `UserMapper` 接口的方法添加相应的MyBatis注解：

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    @Select("SELECT * FROM tb_user WHERE open_id = #{openId} AND deleted = 0")
    User selectByOpenId(@Param("openId") String openId);
    
    @Select("SELECT * FROM tb_user WHERE phone = #{phone} AND deleted = 0")
    User selectByPhone(@Param("phone") String phone);
    
    @Update("UPDATE tb_user SET last_login_time = #{lastLoginTime} WHERE id = #{userId}")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
```

**优点**：
- 简单直接，SQL语句与方法定义在一起
- 与项目中 `AdminMapper` 的实现方式保持一致
- 不需要额外的XML文件

**缺点**：
- 复杂SQL语句可读性较差
- 动态SQL支持有限

### 方案2：使用XML映射文件（备选）

创建 `src/main/resources/mapper/UserMapper.xml`：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.biangqiang.freshdelivery.mapper.UserMapper">
    
    <select id="selectByOpenId" resultType="com.biangqiang.freshdelivery.entity.User">
        SELECT * FROM tb_user 
        WHERE open_id = #{openId} AND deleted = 0
    </select>
    
    <select id="selectByPhone" resultType="com.biangqiang.freshdelivery.entity.User">
        SELECT * FROM tb_user 
        WHERE phone = #{phone} AND deleted = 0
    </select>
    
    <update id="updateLastLoginTime">
        UPDATE tb_user 
        SET last_login_time = #{lastLoginTime} 
        WHERE id = #{userId}
    </update>
    
</mapper>
```

**优点**：
- 支持复杂的动态SQL
- SQL语句可读性更好
- 便于维护复杂查询

**缺点**：
- 需要额外的XML文件
- 方法定义与SQL实现分离

## 已实施的修复

### 1. 添加必要的导入

```java
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.time.LocalDateTime;
```

### 2. 为方法添加SQL注解

- `selectByOpenId`：添加 `@Select` 注解，查询指定 `open_id` 的用户
- `selectByPhone`：添加 `@Select` 注解，查询指定手机号的用户
- `updateLastLoginTime`：添加 `@Update` 注解，更新用户最后登录时间

### 3. 修正方法签名

将 `updateLastLoginTime` 方法的参数从：
```java
void updateLastLoginTime(@Param("userId") Long userId);
```

修改为：
```java
void updateLastLoginTime(@Param("userId") Long userId, @Param("lastLoginTime") LocalDateTime lastLoginTime);
```

## 验证修复效果

### 1. 重启后端服务

```bash
cd f:\code\backend
mvn clean package
java -jar target/fresh-delivery-backend-1.0.0.jar
```

### 2. 测试微信登录

使用小程序或API工具测试登录功能：

```bash
curl -X POST http://localhost:8081/api/user/login \
  -H "Content-Type: application/json" \
  -d '{"code":"test-code"}'
```

### 3. 检查日志

查看后端日志，确认不再出现MyBatis绑定异常：

```bash
tail -f f:\code\backend\logs\fresh-delivery.log
```

## 预防措施

### 1. 代码规范

- **统一映射方式**：项目中所有Mapper接口应使用相同的映射方式（注解或XML）
- **完整实现**：定义Mapper方法时，必须同时提供SQL实现
- **命名规范**：确保方法名与SQL映射ID完全一致

### 2. 开发流程

- **编译检查**：每次添加新的Mapper方法后，立即编译并测试
- **单元测试**：为每个Mapper方法编写单元测试
- **集成测试**：在完整的Spring Boot环境中测试Mapper功能

### 3. 项目配置

确保 `application.yml` 中正确配置了MyBatis：

```yaml
mybatis-plus:
  mapper-locations: classpath*:mapper/*.xml
  type-aliases-package: com.biangqiang.freshdelivery.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

## 常见MyBatis绑定异常

### 1. 方法名不匹配

**错误**：接口方法名与XML中的id不一致

**解决**：确保方法名与映射ID完全相同

### 2. 命名空间错误

**错误**：XML文件中的namespace与接口全限定名不匹配

**解决**：检查namespace是否正确

### 3. 缺少映射文件

**错误**：定义了接口方法但没有对应的SQL实现

**解决**：添加注解或XML映射

### 4. 扫描路径问题

**错误**：MyBatis无法扫描到Mapper接口或XML文件

**解决**：检查 `@MapperScan` 注解和配置文件中的扫描路径

## 总结

通过为 `UserMapper` 接口添加MyBatis注解，成功解决了绑定异常问题。这种修复方式：

1. **保持了项目的一致性**：与 `AdminMapper` 使用相同的注解方式
2. **解决了根本问题**：为每个方法提供了具体的SQL实现
3. **提高了代码可维护性**：SQL语句与方法定义紧密结合

建议在后续开发中：
- 统一使用注解方式进行简单SQL映射
- 对于复杂查询，考虑使用XML映射文件
- 建立完善的测试机制，及早发现此类问题