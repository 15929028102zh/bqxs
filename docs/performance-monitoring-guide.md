# 性能优化与监控指南 - 全栈性能提升方案

## 🎯 性能优化目标

### 当前性能基线
📊 **响应时间**：API 平均响应时间 < 200ms  
📊 **吞吐量**：支持 1000+ 并发用户  
📊 **可用性**：系统可用性 > 99.9%  
📊 **资源利用率**：CPU < 70%, 内存 < 80%  

### 优化目标
🚀 **极致响应**：API 响应时间 < 100ms  
🚀 **高并发**：支持 5000+ 并发用户  
🚀 **高可用**：系统可用性 > 99.99%  
🚀 **资源优化**：CPU < 50%, 内存 < 60%  

## 📋 数据库性能优化

### 1. MySQL 查询优化

#### 1.1 索引优化策略

**复合索引设计**
```sql
-- 用户表索引优化
ALTER TABLE users 
ADD INDEX idx_username_status (username, status),
ADD INDEX idx_email_status (email, status),
ADD INDEX idx_phone_status (phone, status),
ADD INDEX idx_created_at (created_at),
ADD INDEX idx_last_login (last_login_at);

-- 商品表索引优化
ALTER TABLE products 
ADD INDEX idx_category_status_price (category_id, status, price),
ADD INDEX idx_status_created (status, created_at),
ADD INDEX idx_price_range (price, original_price),
ADD INDEX idx_name_fulltext (name, description) USING FULLTEXT;

-- 订单表索引优化
ALTER TABLE orders 
ADD INDEX idx_user_status_created (user_id, status, created_at),
ADD INDEX idx_order_no_status (order_no, status),
ADD INDEX idx_status_payment (status, payment_method),
ADD INDEX idx_created_final_amount (created_at, final_amount);

-- 订单明细表索引优化
ALTER TABLE order_items 
ADD INDEX idx_order_product (order_id, product_id),
ADD INDEX idx_product_created (product_id, created_at);
```

**索引使用分析**
```sql
-- 分析慢查询
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 0.1;
SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 查看索引使用情况
SELECT 
    TABLE_SCHEMA,
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    SUB_PART,
    PACKED,
    NULLABLE,
    INDEX_TYPE
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'fresh_delivery'
ORDER BY TABLE_NAME, SEQ_IN_INDEX;

-- 分析查询执行计划
EXPLAIN FORMAT=JSON
SELECT p.*, c.name as category_name, i.quantity
FROM products p
JOIN categories c ON p.category_id = c.id
JOIN inventory i ON p.id = i.product_id
WHERE p.status = 'ACTIVE' 
  AND p.price BETWEEN 10.00 AND 100.00
  AND c.status = 'ACTIVE'
ORDER BY p.created_at DESC
LIMIT 20;
```

#### 1.2 查询优化实践

**分页查询优化**
```java
// 传统分页（性能差）
@Query("SELECT p FROM Product p WHERE p.status = :status ORDER BY p.createdAt DESC")
Page<Product> findByStatusOrderByCreatedAtDesc(
    @Param("status") ProductStatus status, 
    Pageable pageable
);

// 游标分页优化（性能好）
@Repository
public class ProductRepositoryImpl {
    
    public List<ProductDto> findProductsWithCursor(
            ProductStatus status, 
            Long lastId, 
            int limit) {
        
        String sql = """
            SELECT p.id, p.name, p.price, p.created_at,
                   c.name as category_name,
                   i.quantity
            FROM products p
            JOIN categories c ON p.category_id = c.id
            JOIN inventory i ON p.id = i.product_id
            WHERE p.status = :status
              AND (:lastId IS NULL OR p.id < :lastId)
            ORDER BY p.id DESC
            LIMIT :limit
        """;
        
        return jdbcTemplate.query(sql, 
            Map.of("status", status.name(), "lastId", lastId, "limit", limit),
            (rs, rowNum) -> ProductDto.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .price(rs.getBigDecimal("price"))
                .createdAt(rs.getTimestamp("created_at").toLocalDateTime())
                .categoryName(rs.getString("category_name"))
                .stock(rs.getInt("quantity"))
                .build()
        );
    }
}

// 批量查询优化
@Repository
public class OrderRepositoryImpl {
    
    @Query(value = """
        SELECT o.*, 
               GROUP_CONCAT(oi.product_id) as product_ids,
               GROUP_CONCAT(oi.quantity) as quantities,
               GROUP_CONCAT(oi.product_name) as product_names
        FROM orders o
        LEFT JOIN order_items oi ON o.id = oi.order_id
        WHERE o.user_id = :userId
          AND o.created_at >= :startDate
        GROUP BY o.id
        ORDER BY o.created_at DESC
        LIMIT :limit
    """, nativeQuery = true)
    List<Object[]> findOrdersWithItemsByUserId(
        @Param("userId") Long userId,
        @Param("startDate") LocalDateTime startDate,
        @Param("limit") int limit
    );
}
```

**聚合查询优化**
```java
// 订单统计优化
@Repository
public class OrderStatisticsRepository {
    
    // 使用物化视图优化统计查询
    @Query(value = """
        CREATE OR REPLACE VIEW order_daily_stats AS
        SELECT 
            DATE(created_at) as order_date,
            COUNT(*) as order_count,
            SUM(final_amount) as total_amount,
            AVG(final_amount) as avg_amount,
            COUNT(DISTINCT user_id) as unique_users
        FROM orders
        WHERE status IN ('CONFIRMED', 'PAID', 'SHIPPED', 'DELIVERED')
        GROUP BY DATE(created_at)
    """, nativeQuery = true)
    void createOrderDailyStatsView();
    
    // 使用预计算表优化实时统计
    @Scheduled(fixedRate = 300000) // 每5分钟更新
    public void updateRealTimeStats() {
        String sql = """
            INSERT INTO order_realtime_stats (stat_time, order_count, total_amount)
            SELECT 
                DATE_FORMAT(NOW(), '%Y-%m-%d %H:%i:00') as stat_time,
                COUNT(*) as order_count,
                COALESCE(SUM(final_amount), 0) as total_amount
            FROM orders
            WHERE created_at >= DATE_SUB(NOW(), INTERVAL 5 MINUTE)
              AND status IN ('CONFIRMED', 'PAID', 'SHIPPED', 'DELIVERED')
            ON DUPLICATE KEY UPDATE
                order_count = VALUES(order_count),
                total_amount = VALUES(total_amount),
                updated_at = NOW()
        """;
        
        jdbcTemplate.update(sql);
    }
    
    // 分区表优化大数据量查询
    @Query(value = """
        SELECT 
            DATE_FORMAT(created_at, '%Y-%m') as month,
            COUNT(*) as order_count,
            SUM(final_amount) as total_amount
        FROM orders PARTITION (p202401, p202402, p202403)
        WHERE created_at >= :startDate
          AND created_at < :endDate
        GROUP BY DATE_FORMAT(created_at, '%Y-%m')
        ORDER BY month
    """, nativeQuery = true)
    List<Object[]> getMonthlyOrderStats(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
```

### 2. 连接池优化

**HikariCP 配置优化**
```yaml
# application.yml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    hikari:
      # 连接池配置
      minimum-idle: 10
      maximum-pool-size: 50
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
      validation-timeout: 5000
      
      # 性能优化
      auto-commit: false
      connection-test-query: SELECT 1
      pool-name: HikariPool-FreshDelivery
      
      # 数据库连接参数优化
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
        
  # JPA 优化配置
  jpa:
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        # 查询优化
        jdbc:
          batch_size: 50
          fetch_size: 50
        
        # 缓存配置
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory
        
        # 性能优化
        order_inserts: true
        order_updates: true
        batch_versioned_data: true
        generate_statistics: false
```

## 📋 缓存策略优化

### 1. Redis 缓存架构

#### 1.1 多级缓存设计

**缓存层次结构**
```java
// 缓存配置
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 用户信息缓存 - 长期缓存
        cacheConfigurations.put("users", config.entryTtl(Duration.ofHours(2)));
        
        // 商品信息缓存 - 中期缓存
        cacheConfigurations.put("products", config.entryTtl(Duration.ofMinutes(30)));
        
        // 库存信息缓存 - 短期缓存
        cacheConfigurations.put("inventory", config.entryTtl(Duration.ofMinutes(5)));
        
        // 热点数据缓存 - 超长期缓存
        cacheConfigurations.put("hotdata", config.entryTtl(Duration.ofHours(24)));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // 序列化配置
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(mapper);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);
        
        template.afterPropertiesSet();
        return template;
    }
}

// 智能缓存服务
@Service
public class SmartCacheService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final LoadingCache<String, Object> localCache;
    
    public SmartCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        
        // 本地缓存配置
        this.localCache = Caffeine.newBuilder()
            .maximumSize(10000)
            .expireAfterWrite(Duration.ofMinutes(5))
            .recordStats()
            .build(key -> loadFromRedis(key));
    }
    
    // L1: 本地缓存 -> L2: Redis缓存 -> L3: 数据库
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        try {
            // L1: 本地缓存
            Object cached = localCache.get(key);
            if (cached != null) {
                return type.cast(cached);
            }
        } catch (Exception e) {
            log.warn("本地缓存获取失败: {}", e.getMessage());
        }
        
        try {
            // L2: Redis缓存
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                localCache.put(key, cached);
                return type.cast(cached);
            }
        } catch (Exception e) {
            log.warn("Redis缓存获取失败: {}", e.getMessage());
        }
        
        // L3: 数据库加载
        T value = loader.get();
        if (value != null) {
            // 异步更新缓存
            CompletableFuture.runAsync(() -> {
                try {
                    redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(30));
                    localCache.put(key, value);
                } catch (Exception e) {
                    log.error("缓存更新失败: {}", e.getMessage(), e);
                }
            });
        }
        
        return value;
    }
    
    // 缓存预热
    @EventListener(ApplicationReadyEvent.class)
    public void warmUpCache() {
        log.info("开始缓存预热...");
        
        CompletableFuture.runAsync(() -> {
            // 预热热门商品
            warmUpHotProducts();
            
            // 预热商品分类
            warmUpCategories();
            
            // 预热系统配置
            warmUpSystemConfig();
            
            log.info("缓存预热完成");
        });
    }
    
    private void warmUpHotProducts() {
        try {
            List<Long> hotProductIds = productService.getHotProductIds(100);
            for (Long productId : hotProductIds) {
                String key = "product:" + productId;
                ProductDetailDto product = productService.getProductDetail(productId);
                redisTemplate.opsForValue().set(key, product, Duration.ofHours(2));
            }
            log.info("热门商品缓存预热完成: {} 个商品", hotProductIds.size());
        } catch (Exception e) {
            log.error("热门商品缓存预热失败", e);
        }
    }
}
```

#### 1.2 缓存一致性保证

**Canal + Redis 数据同步**
```java
// Canal 监听器
@Component
public class CanalDataSyncListener {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    
    @CanalTable("products")
    public void onProductChange(CanalEntry.EventType eventType, 
                               List<ProductEntity> products) {
        
        for (ProductEntity product : products) {
            String cacheKey = "product:" + product.getId();
            
            switch (eventType) {
                case INSERT:
                case UPDATE:
                    // 更新缓存
                    ProductDetailDto productDto = convertToDto(product);
                    redisTemplate.opsForValue().set(cacheKey, productDto, Duration.ofMinutes(30));
                    
                    // 发布缓存更新事件
                    publishCacheUpdateEvent("product", product.getId(), "update");
                    break;
                    
                case DELETE:
                    // 删除缓存
                    redisTemplate.delete(cacheKey);
                    
                    // 发布缓存删除事件
                    publishCacheUpdateEvent("product", product.getId(), "delete");
                    break;
            }
        }
    }
    
    @CanalTable("inventory")
    public void onInventoryChange(CanalEntry.EventType eventType, 
                                 List<InventoryEntity> inventories) {
        
        for (InventoryEntity inventory : inventories) {
            String cacheKey = "inventory:" + inventory.getProductId();
            
            if (eventType == CanalEntry.EventType.DELETE) {
                redisTemplate.delete(cacheKey);
            } else {
                InventoryDto inventoryDto = convertToDto(inventory);
                redisTemplate.opsForValue().set(cacheKey, inventoryDto, Duration.ofMinutes(5));
            }
            
            // 更新商品缓存中的库存信息
            updateProductInventoryCache(inventory.getProductId(), inventory.getQuantity());
        }
    }
    
    private void publishCacheUpdateEvent(String type, Long id, String action) {
        CacheUpdateEvent event = CacheUpdateEvent.builder()
            .type(type)
            .id(id)
            .action(action)
            .timestamp(System.currentTimeMillis())
            .build();
            
        rabbitTemplate.convertAndSend("cache.exchange", "cache.update", event);
    }
}

// 分布式缓存更新
@RabbitListener(queues = "cache.update.queue")
@Component
public class CacheUpdateEventListener {
    
    private final LoadingCache<String, Object> localCache;
    
    public void handleCacheUpdate(CacheUpdateEvent event) {
        String cacheKey = event.getType() + ":" + event.getId();
        
        if ("delete".equals(event.getAction())) {
            localCache.invalidate(cacheKey);
        } else {
            // 异步刷新本地缓存
            localCache.refresh(cacheKey);
        }
        
        log.debug("本地缓存更新: {} - {}", cacheKey, event.getAction());
    }
}
```

### 2. 缓存穿透和雪崩防护

**布隆过滤器防穿透**
```java
@Component
public class BloomFilterService {
    
    private final BloomFilter<Long> productBloomFilter;
    private final BloomFilter<Long> userBloomFilter;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public BloomFilterService() {
        // 初始化布隆过滤器
        this.productBloomFilter = BloomFilter.create(
            Funnels.longFunnel(), 
            1000000, // 预期元素数量
            0.01     // 误判率
        );
        
        this.userBloomFilter = BloomFilter.create(
            Funnels.longFunnel(),
            100000,
            0.01
        );
        
        // 初始化数据
        initBloomFilters();
    }
    
    public boolean mightContainProduct(Long productId) {
        return productBloomFilter.mightContain(productId);
    }
    
    public boolean mightContainUser(Long userId) {
        return userBloomFilter.mightContain(userId);
    }
    
    public void addProduct(Long productId) {
        productBloomFilter.put(productId);
        // 同步到Redis
        redisTemplate.opsForSet().add("bloom:products", productId);
    }
    
    public void addUser(Long userId) {
        userBloomFilter.put(userId);
        redisTemplate.opsForSet().add("bloom:users", userId);
    }
    
    @Scheduled(fixedRate = 3600000) // 每小时重建
    public void rebuildBloomFilters() {
        log.info("开始重建布隆过滤器...");
        
        CompletableFuture.runAsync(() -> {
            try {
                // 重建商品布隆过滤器
                Set<Object> productIds = redisTemplate.opsForSet().members("bloom:products");
                BloomFilter<Long> newProductFilter = BloomFilter.create(
                    Funnels.longFunnel(), 
                    Math.max(productIds.size() * 2, 1000000), 
                    0.01
                );
                
                productIds.forEach(id -> newProductFilter.put((Long) id));
                
                // 原子替换
                synchronized (this) {
                    // 这里需要使用反射或其他方式替换过滤器
                    // 实际实现中可能需要重启应用或使用其他策略
                }
                
                log.info("布隆过滤器重建完成");
            } catch (Exception e) {
                log.error("布隆过滤器重建失败", e);
            }
        });
    }
    
    private void initBloomFilters() {
        // 从数据库加载现有数据
        CompletableFuture.runAsync(() -> {
            try {
                // 加载商品ID
                List<Long> productIds = productRepository.findAllIds();
                productIds.forEach(productBloomFilter::put);
                
                // 加载用户ID
                List<Long> userIds = userRepository.findAllIds();
                userIds.forEach(userBloomFilter::put);
                
                log.info("布隆过滤器初始化完成: 商品{}, 用户{}", 
                    productIds.size(), userIds.size());
            } catch (Exception e) {
                log.error("布隆过滤器初始化失败", e);
            }
        });
    }
}

// 缓存服务增强
@Service
public class EnhancedCacheService {
    
    private final BloomFilterService bloomFilterService;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public <T> T getProduct(Long productId, Class<T> type, Supplier<T> loader) {
        // 1. 布隆过滤器检查
        if (!bloomFilterService.mightContainProduct(productId)) {
            log.debug("布隆过滤器拦截: 商品ID {} 不存在", productId);
            return null;
        }
        
        String cacheKey = "product:" + productId;
        
        // 2. 缓存查询
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return type.cast(cached);
            }
        } catch (Exception e) {
            log.warn("缓存查询失败: {}", e.getMessage());
        }
        
        // 3. 防止缓存击穿 - 分布式锁
        String lockKey = "lock:" + cacheKey;
        Boolean lockAcquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, "1", Duration.ofSeconds(10));
            
        if (Boolean.TRUE.equals(lockAcquired)) {
            try {
                // 双重检查
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null) {
                    return type.cast(cached);
                }
                
                // 加载数据
                T value = loader.get();
                if (value != null) {
                    // 随机TTL防止雪崩
                    int randomTtl = 1800 + new Random().nextInt(600); // 30-40分钟
                    redisTemplate.opsForValue().set(cacheKey, value, Duration.ofSeconds(randomTtl));
                } else {
                    // 缓存空值防穿透
                    redisTemplate.opsForValue().set(cacheKey, "NULL", Duration.ofMinutes(5));
                }
                
                return value;
            } finally {
                redisTemplate.delete(lockKey);
            }
        } else {
            // 等待其他线程加载
            try {
                Thread.sleep(100);
                Object cached = redisTemplate.opsForValue().get(cacheKey);
                if (cached != null && !"NULL".equals(cached)) {
                    return type.cast(cached);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return null;
    }
}
```

## 📋 应用性能监控

### 1. Micrometer + Prometheus 监控

**监控配置**
```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
        spring.data.repository.invocations: true
      percentiles:
        http.server.requests: 0.5, 0.9, 0.95, 0.99
        spring.data.repository.invocations: 0.5, 0.9, 0.95, 0.99
    tags:
      application: fresh-delivery
      environment: ${spring.profiles.active:dev}
```

**自定义监控指标**
```java
@Component
public class BusinessMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTimer;
    private final Gauge activeUsersGauge;
    private final DistributionSummary orderAmountSummary;
    
    public BusinessMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 订单创建计数器
        this.orderCreatedCounter = Counter.builder("orders.created.total")
            .description("Total number of orders created")
            .tag("type", "business")
            .register(meterRegistry);
            
        // 订单处理时间
        this.orderProcessingTimer = Timer.builder("orders.processing.duration")
            .description("Order processing duration")
            .register(meterRegistry);
            
        // 活跃用户数
        this.activeUsersGauge = Gauge.builder("users.active.count")
            .description("Number of active users")
            .register(meterRegistry, this, BusinessMetrics::getActiveUserCount);
            
        // 订单金额分布
        this.orderAmountSummary = DistributionSummary.builder("orders.amount")
            .description("Order amount distribution")
            .baseUnit("yuan")
            .register(meterRegistry);
    }
    
    public void recordOrderCreated(String paymentMethod) {
        orderCreatedCounter.increment(
            Tags.of("payment_method", paymentMethod)
        );
    }
    
    public Timer.Sample startOrderProcessing() {
        return Timer.start(meterRegistry);
    }
    
    public void recordOrderProcessed(Timer.Sample sample, String status) {
        sample.stop(Timer.builder("orders.processing.duration")
            .tag("status", status)
            .register(meterRegistry));
    }
    
    public void recordOrderAmount(BigDecimal amount) {
        orderAmountSummary.record(amount.doubleValue());
    }
    
    private double getActiveUserCount() {
        // 从Redis或数据库获取活跃用户数
        try {
            return redisTemplate.opsForHyperLogLog().size("active_users");
        } catch (Exception e) {
            return 0;
        }
    }
    
    // 自定义业务指标
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        recordOrderCreated(event.getPaymentMethod());
        recordOrderAmount(event.getFinalAmount());
        
        // 记录用户活跃度
        redisTemplate.opsForHyperLogLog().add("active_users", event.getUserId());
    }
    
    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        meterRegistry.counter("orders.status.changed",
            "from", event.getOldStatus(),
            "to", event.getNewStatus()
        ).increment();
    }
}

// 性能监控切面
@Aspect
@Component
public class PerformanceMonitoringAspect {
    
    private final MeterRegistry meterRegistry;
    
    @Around("@annotation(Monitored)")
    public Object monitorPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            Object result = joinPoint.proceed();
            
            sample.stop(Timer.builder("method.execution.duration")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "success")
                .register(meterRegistry));
                
            return result;
        } catch (Exception e) {
            sample.stop(Timer.builder("method.execution.duration")
                .tag("class", className)
                .tag("method", methodName)
                .tag("status", "error")
                .register(meterRegistry));
                
            meterRegistry.counter("method.execution.errors",
                "class", className,
                "method", methodName,
                "exception", e.getClass().getSimpleName()
            ).increment();
            
            throw e;
        }
    }
}

// 监控注解
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {
    String value() default "";
}
```

### 2. APM 集成 - SkyWalking

**SkyWalking 配置**
```yaml
# skywalking配置
skywalking:
  agent:
    service_name: fresh-delivery-backend
    collector:
      backend_service: skywalking-oap:11800
    logging:
      level: INFO
    plugin:
      toolkit:
        log:
          transmit_formatted: true
```

**自定义链路追踪**
```java
@Component
public class TraceService {
    
    @Trace(operationName = "createOrder")
    @Tag(key = "userId", value = "arg[0].userId")
    public OrderDto createOrder(OrderCreateRequest request) {
        // 创建子Span
        AbstractSpan span = ContextManager.createLocalSpan("validateUser");
        span.tag("userId", request.getUserId().toString());
        
        try {
            // 用户验证逻辑
            validateUser(request.getUserId());
            span.tag("validation", "success");
        } catch (Exception e) {
            span.tag("validation", "failed");
            span.log(e);
            throw e;
        } finally {
            ContextManager.stopSpan();
        }
        
        // 继续订单创建流程...
        return processOrder(request);
    }
    
    @Trace(operationName = "processPayment")
    public PaymentResult processPayment(PaymentRequest request) {
        // 添加自定义标签
        Tags.of(ContextManager.activeSpan())
            .tag("payment.method", request.getPaymentMethod())
            .tag("payment.amount", request.getAmount().toString());
            
        // 记录关键事件
        ContextManager.activeSpan().log("开始处理支付");
        
        try {
            PaymentResult result = paymentService.process(request);
            
            ContextManager.activeSpan().log("支付处理完成");
            Tags.of(ContextManager.activeSpan())
                .tag("payment.status", result.getStatus());
                
            return result;
        } catch (Exception e) {
            ContextManager.activeSpan().log(e);
            throw e;
        }
    }
}
```

### 3. 日志监控和分析

**结构化日志配置**
```xml
<!-- logback-spring.xml -->
<configuration>
    <springProfile name="!local">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <arguments/>
                    <stackTrace/>
                    <pattern>
                        <pattern>
                            {
                                "traceId": "%X{traceId:-}",
                                "spanId": "%X{spanId:-}",
                                "service": "fresh-delivery",
                                "environment": "${spring.profiles.active}",
                                "host": "${HOSTNAME:-localhost}"
                            }
                        </pattern>
                    </pattern>
                </providers>
            </encoder>
        </appender>
    </springProfile>
    
    <!-- 异步日志 -->
    <appender name="ASYNC" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="STDOUT"/>
        <queueSize>1024</queueSize>
        <discardingThreshold>0</discardingThreshold>
        <includeCallerData>true</includeCallerData>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="ASYNC"/>
    </root>
</configuration>
```

**业务日志记录**
```java
@Component
public class BusinessLogger {
    
    private static final Logger businessLog = LoggerFactory.getLogger("BUSINESS");
    private static final Logger performanceLog = LoggerFactory.getLogger("PERFORMANCE");
    private static final Logger securityLog = LoggerFactory.getLogger("SECURITY");
    
    public void logOrderCreated(OrderDto order) {
        MDC.put("userId", order.getUserId().toString());
        MDC.put("orderId", order.getId().toString());
        MDC.put("orderAmount", order.getFinalAmount().toString());
        
        businessLog.info("订单创建成功: 用户={}, 订单号={}, 金额={}",
            order.getUserId(), order.getOrderNo(), order.getFinalAmount());
            
        MDC.clear();
    }
    
    public void logSlowQuery(String sql, long duration, Object... params) {
        if (duration > 1000) { // 超过1秒的查询
            MDC.put("queryDuration", String.valueOf(duration));
            MDC.put("queryType", "slow");
            
            performanceLog.warn("慢查询检测: SQL={}, 耗时={}ms, 参数={}",
                sql, duration, Arrays.toString(params));
                
            MDC.clear();
        }
    }
    
    public void logSecurityEvent(String event, String userId, String details) {
        MDC.put("securityEvent", event);
        MDC.put("userId", userId);
        MDC.put("clientIp", getCurrentClientIp());
        
        securityLog.warn("安全事件: 事件={}, 用户={}, 详情={}",
            event, userId, details);
            
        MDC.clear();
    }
    
    private String getCurrentClientIp() {
        // 获取当前请求的客户端IP
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes) {
            HttpServletRequest request = ((ServletRequestAttributes) attrs).getRequest();
            return getClientIpAddress(request);
        }
        return "unknown";
    }
}
```

## 🎯 实施计划

### 第1周：数据库优化
- [ ] 分析慢查询日志
- [ ] 优化索引设计
- [ ] 配置连接池参数
- [ ] 实施查询优化

### 第2周：缓存架构
- [ ] 部署Redis集群
- [ ] 实现多级缓存
- [ ] 配置缓存一致性
- [ ] 添加防护机制

### 第3周：监控体系
- [ ] 集成Prometheus监控
- [ ] 配置SkyWalking链路追踪
- [ ] 实现业务指标监控
- [ ] 建立告警机制

### 第4周：性能调优
- [ ] JVM参数优化
- [ ] 应用性能调优
- [ ] 压力测试验证
- [ ] 性能报告输出

---

**立即开始**：从数据库索引优化开始，这是性能提升最直接有效的方式！