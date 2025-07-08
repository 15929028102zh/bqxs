# 微服务架构迁移指南 - 从单体到微服务的渐进式演进

## 🎯 迁移策略概览

### 当前架构分析
✅ **Spring Boot 单体应用**：成熟稳定的技术栈  
✅ **MySQL 数据库**：关系型数据存储  
✅ **Redis 缓存**：高性能缓存层  
✅ **Vue 3 前端**：现代化前端框架  

### 迁移目标
🚀 **高可用性**：服务可用性 > 99.9%  
🚀 **弹性扩展**：支持水平扩展，自动伸缩  
🚀 **独立部署**：服务独立发布，降低部署风险  
🚀 **技术多样性**：不同服务可选择最适合的技术栈  

## 📋 微服务拆分策略

### 1. 领域驱动设计 (DDD) 分析

#### 1.1 业务领域识别

**核心领域**
```
用户管理域 (User Domain)
├── 用户注册/登录
├── 用户信息管理
├── 权限控制
└── 用户行为分析

商品管理域 (Product Domain)
├── 商品信息管理
├── 库存管理
├── 价格管理
└── 商品分类

订单管理域 (Order Domain)
├── 订单创建
├── 订单状态管理
├── 订单支付
└── 订单履约

配送管理域 (Delivery Domain)
├── 配送路线规划
├── 配送员管理
├── 配送状态跟踪
└── 配送费用计算
```

**支撑领域**
```
通知服务域 (Notification Domain)
├── 短信通知
├── 邮件通知
├── 推送通知
└── 站内消息

支付服务域 (Payment Domain)
├── 支付处理
├── 退款处理
├── 支付方式管理
└── 财务对账

文件服务域 (File Domain)
├── 图片上传
├── 文件存储
├── CDN 管理
└── 文件安全
```

#### 1.2 微服务边界定义

**用户服务 (User Service)**
```yaml
服务职责:
  - 用户注册、登录、注销
  - 用户信息 CRUD
  - 用户权限管理
  - JWT Token 管理

数据模型:
  - users (用户基本信息)
  - user_roles (用户角色)
  - user_permissions (用户权限)
  - user_sessions (用户会话)

API 接口:
  - POST /api/users/register
  - POST /api/users/login
  - GET /api/users/profile
  - PUT /api/users/profile
  - GET /api/users/{id}/permissions

技术栈:
  - Spring Boot 3.x
  - Spring Security
  - MySQL
  - Redis (会话存储)
```

**商品服务 (Product Service)**
```yaml
服务职责:
  - 商品信息管理
  - 商品分类管理
  - 库存管理
  - 价格管理

数据模型:
  - products (商品信息)
  - categories (商品分类)
  - inventory (库存信息)
  - price_history (价格历史)

API 接口:
  - GET /api/products
  - POST /api/products
  - PUT /api/products/{id}
  - GET /api/products/{id}/inventory
  - PUT /api/products/{id}/inventory

技术栈:
  - Spring Boot 3.x
  - MySQL
  - Elasticsearch (商品搜索)
  - Redis (缓存)
```

**订单服务 (Order Service)**
```yaml
服务职责:
  - 订单创建和管理
  - 订单状态流转
  - 订单查询和统计
  - 购物车管理

数据模型:
  - orders (订单主表)
  - order_items (订单明细)
  - shopping_carts (购物车)
  - order_status_history (状态历史)

API 接口:
  - POST /api/orders
  - GET /api/orders/{id}
  - PUT /api/orders/{id}/status
  - GET /api/orders/user/{userId}
  - POST /api/carts/items

技术栈:
  - Spring Boot 3.x
  - MySQL
  - Redis (购物车)
  - RabbitMQ (事件发布)
```

### 2. 渐进式迁移路径

#### 2.1 阶段一：数据库拆分 (Database-per-Service)

**数据库迁移脚本**
```sql
-- 用户服务数据库
CREATE DATABASE user_service;
USE user_service;

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
);

CREATE TABLE user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_role (user_id, role_name)
);

-- 商品服务数据库
CREATE DATABASE product_service;
USE product_service;

CREATE TABLE categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    sort_order INT DEFAULT 0,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_id) REFERENCES categories(id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
);

CREATE TABLE products (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    original_price DECIMAL(10,2),
    images JSON,
    specifications JSON,
    tags JSON,
    status ENUM('ACTIVE', 'INACTIVE', 'OUT_OF_STOCK') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_category_id (category_id),
    INDEX idx_price (price),
    INDEX idx_status (status),
    FULLTEXT idx_name_desc (name, description)
);

CREATE TABLE inventory (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT DEFAULT 10,
    max_stock_level INT DEFAULT 1000,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_product_inventory (product_id)
);

-- 订单服务数据库
CREATE DATABASE order_service;
USE order_service;

CREATE TABLE orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0,
    final_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'PAID', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    payment_method ENUM('ALIPAY', 'WECHAT', 'CREDIT_CARD', 'CASH') NOT NULL,
    delivery_address JSON NOT NULL,
    remark TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

CREATE TABLE order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
);
```

#### 2.2 阶段二：服务接口抽取

**用户服务实现**
```java
// UserService.java
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(
            @Valid @RequestBody UserRegistrationRequest request) {
        
        UserDto user = userService.registerUser(request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        
        User user = userService.authenticate(request.getUsername(), request.getPassword());
        String token = tokenProvider.generateToken(user);
        
        LoginResponse response = LoginResponse.builder()
            .token(token)
            .user(UserDto.fromEntity(user))
            .expiresIn(tokenProvider.getExpirationTime())
            .build();
            
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserDto>> getProfile(Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(
            @Valid @RequestBody UserUpdateRequest request,
            Authentication auth) {
        
        Long userId = Long.parseLong(auth.getName());
        UserDto user = userService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success(user));
    }
    
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.name")
    public ResponseEntity<ApiResponse<List<String>>> getUserPermissions(
            @PathVariable Long id) {
        
        List<String> permissions = userService.getUserPermissions(id);
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}

// UserService.java
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    
    public UserDto registerUser(UserRegistrationRequest request) {
        // 检查用户名和邮箱是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("邮箱已被注册");
        }
        
        // 创建用户
        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .phone(request.getPhone())
            .status(UserStatus.ACTIVE)
            .build();
            
        user = userRepository.save(user);
        
        // 分配默认角色
        assignDefaultRole(user.getId());
        
        // 发送欢迎邮件 (异步)
        publishUserRegisteredEvent(user);
        
        return UserDto.fromEntity(user);
    }
    
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new AuthenticationException("用户名或密码错误"));
            
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new AuthenticationException("用户名或密码错误");
        }
        
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AuthenticationException("账户已被禁用");
        }
        
        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        return user;
    }
    
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        return UserDto.fromEntity(user);
    }
    
    @CacheEvict(value = "users", key = "#id")
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
            
        // 更新用户信息
        if (StringUtils.hasText(request.getEmail()) && 
            !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BusinessException("邮箱已被使用");
            }
            user.setEmail(request.getEmail());
        }
        
        if (StringUtils.hasText(request.getPhone())) {
            user.setPhone(request.getPhone());
        }
        
        if (StringUtils.hasText(request.getAvatarUrl())) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        user = userRepository.save(user);
        return UserDto.fromEntity(user);
    }
    
    @Cacheable(value = "user_permissions", key = "#userId")
    public List<String> getUserPermissions(Long userId) {
        return userRepository.findUserPermissions(userId);
    }
    
    private void assignDefaultRole(Long userId) {
        UserRole userRole = UserRole.builder()
            .userId(userId)
            .roleName("USER")
            .build();
        userRoleRepository.save(userRole);
    }
    
    private void publishUserRegisteredEvent(User user) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .build();
            
        eventPublisher.publishEvent(event);
    }
}
```

**商品服务实现**
```java
// ProductController.java
@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {
    
    private final ProductService productService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductDto>>> getProducts(
            @Valid ProductSearchRequest request,
            Pageable pageable) {
        
        Page<ProductDto> products = productService.searchProducts(request, pageable);
        PageResponse<ProductDto> response = PageResponse.of(products);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductCreateRequest request) {
        
        ProductDto product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(product));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDetailDto>> getProduct(
            @PathVariable Long id) {
        
        ProductDetailDto product = productService.getProductDetail(id);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        
        ProductDto product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success(product));
    }
    
    @GetMapping("/{id}/inventory")
    public ResponseEntity<ApiResponse<InventoryDto>> getInventory(
            @PathVariable Long id) {
        
        InventoryDto inventory = productService.getInventory(id);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
    
    @PutMapping("/{id}/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryDto>> updateInventory(
            @PathVariable Long id,
            @Valid @RequestBody InventoryUpdateRequest request) {
        
        InventoryDto inventory = productService.updateInventory(id, request);
        return ResponseEntity.ok(ApiResponse.success(inventory));
    }
}

// ProductService.java
@Service
@Transactional
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    
    public Page<ProductDto> searchProducts(ProductSearchRequest request, Pageable pageable) {
        // 构建搜索条件
        Specification<Product> spec = Specification.where(null);
        
        if (StringUtils.hasText(request.getKeyword())) {
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(root.get("name"), "%" + request.getKeyword() + "%"),
                    cb.like(root.get("description"), "%" + request.getKeyword() + "%")
                )
            );
        }
        
        if (request.getCategoryId() != null) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("categoryId"), request.getCategoryId())
            );
        }
        
        if (request.getMinPrice() != null) {
            spec = spec.and((root, query, cb) -> 
                cb.greaterThanOrEqualTo(root.get("price"), request.getMinPrice())
            );
        }
        
        if (request.getMaxPrice() != null) {
            spec = spec.and((root, query, cb) -> 
                cb.lessThanOrEqualTo(root.get("price"), request.getMaxPrice())
            );
        }
        
        spec = spec.and((root, query, cb) -> 
            cb.equal(root.get("status"), ProductStatus.ACTIVE)
        );
        
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(ProductDto::fromEntity);
    }
    
    public ProductDto createProduct(ProductCreateRequest request) {
        // 验证分类是否存在
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("商品分类不存在"));
            
        // 创建商品
        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .categoryId(request.getCategoryId())
            .price(request.getPrice())
            .originalPrice(request.getOriginalPrice())
            .images(request.getImages())
            .specifications(request.getSpecifications())
            .tags(request.getTags())
            .status(ProductStatus.ACTIVE)
            .build();
            
        product = productRepository.save(product);
        
        // 创建库存记录
        Inventory inventory = Inventory.builder()
            .productId(product.getId())
            .quantity(request.getInitialStock())
            .minStockLevel(request.getMinStockLevel())
            .maxStockLevel(request.getMaxStockLevel())
            .build();
        inventoryRepository.save(inventory);
        
        // 同步到搜索引擎
        syncToElasticsearch(product);
        
        // 发布商品创建事件
        publishProductCreatedEvent(product);
        
        return ProductDto.fromEntity(product);
    }
    
    @Cacheable(value = "products", key = "#id")
    public ProductDetailDto getProductDetail(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
            
        Inventory inventory = inventoryRepository.findByProductId(id)
            .orElse(null);
            
        return ProductDetailDto.fromEntity(product, inventory);
    }
    
    @CacheEvict(value = "products", key = "#id")
    public ProductDto updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品不存在"));
            
        // 更新商品信息
        if (StringUtils.hasText(request.getName())) {
            product.setName(request.getName());
        }
        
        if (StringUtils.hasText(request.getDescription())) {
            product.setDescription(request.getDescription());
        }
        
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        
        if (request.getImages() != null) {
            product.setImages(request.getImages());
        }
        
        product = productRepository.save(product);
        
        // 同步到搜索引擎
        syncToElasticsearch(product);
        
        return ProductDto.fromEntity(product);
    }
    
    public InventoryDto getInventory(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("库存信息不存在"));
        return InventoryDto.fromEntity(inventory);
    }
    
    public InventoryDto updateInventory(Long productId, InventoryUpdateRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
            .orElseThrow(() -> new ResourceNotFoundException("库存信息不存在"));
            
        inventory.setQuantity(request.getQuantity());
        inventory.setMinStockLevel(request.getMinStockLevel());
        inventory.setMaxStockLevel(request.getMaxStockLevel());
        
        inventory = inventoryRepository.save(inventory);
        
        // 检查库存预警
        checkStockAlert(inventory);
        
        return InventoryDto.fromEntity(inventory);
    }
    
    private void syncToElasticsearch(Product product) {
        // 异步同步到 Elasticsearch
        CompletableFuture.runAsync(() -> {
            try {
                ProductDocument document = ProductDocument.fromEntity(product);
                elasticsearchTemplate.save(document);
            } catch (Exception e) {
                log.error("同步商品到搜索引擎失败: {}", e.getMessage(), e);
            }
        });
    }
    
    private void checkStockAlert(Inventory inventory) {
        if (inventory.getQuantity() <= inventory.getMinStockLevel()) {
            StockAlertEvent event = StockAlertEvent.builder()
                .productId(inventory.getProductId())
                .currentStock(inventory.getQuantity())
                .minStockLevel(inventory.getMinStockLevel())
                .build();
            eventPublisher.publishEvent(event);
        }
    }
}
```

### 3. 服务间通信

#### 3.1 同步通信 - OpenFeign

**用户服务客户端**
```java
// UserServiceClient.java
@FeignClient(name = "user-service", url = "${services.user-service.url}")
public interface UserServiceClient {
    
    @GetMapping("/api/users/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable("id") Long id);
    
    @GetMapping("/api/users/{id}/permissions")
    ApiResponse<List<String>> getUserPermissions(@PathVariable("id") Long id);
    
    @PostMapping("/api/users/validate-token")
    ApiResponse<UserDto> validateToken(@RequestHeader("Authorization") String token);
}

// 在订单服务中使用
@Service
public class OrderService {
    
    private final UserServiceClient userServiceClient;
    
    public OrderDto createOrder(OrderCreateRequest request, Long userId) {
        // 验证用户是否存在
        ApiResponse<UserDto> userResponse = userServiceClient.getUserById(userId);
        if (!userResponse.isSuccess()) {
            throw new BusinessException("用户不存在");
        }
        
        UserDto user = userResponse.getData();
        
        // 创建订单逻辑...
    }
}
```

**商品服务客户端**
```java
// ProductServiceClient.java
@FeignClient(name = "product-service", url = "${services.product-service.url}")
public interface ProductServiceClient {
    
    @GetMapping("/api/products/{id}")
    ApiResponse<ProductDetailDto> getProduct(@PathVariable("id") Long id);
    
    @GetMapping("/api/products/{id}/inventory")
    ApiResponse<InventoryDto> getInventory(@PathVariable("id") Long id);
    
    @PostMapping("/api/products/batch")
    ApiResponse<List<ProductDto>> getProductsByIds(@RequestBody List<Long> ids);
    
    @PutMapping("/api/products/{id}/inventory/reserve")
    ApiResponse<Boolean> reserveInventory(
        @PathVariable("id") Long productId,
        @RequestParam("quantity") Integer quantity
    );
}
```

#### 3.2 异步通信 - RabbitMQ

**事件发布配置**
```java
// RabbitMQConfig.java
@Configuration
@EnableRabbit
public class RabbitMQConfig {
    
    // 用户相关交换机和队列
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange("user.exchange", true, false);
    }
    
    @Bean
    public Queue userRegisteredQueue() {
        return QueueBuilder.durable("user.registered.queue")
            .withArgument("x-dead-letter-exchange", "user.dlx")
            .withArgument("x-dead-letter-routing-key", "user.registered.dlq")
            .build();
    }
    
    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder.bind(userRegisteredQueue())
            .to(userExchange())
            .with("user.registered");
    }
    
    // 订单相关交换机和队列
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange", true, false);
    }
    
    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable("order.created.queue")
            .withArgument("x-dead-letter-exchange", "order.dlx")
            .withArgument("x-dead-letter-routing-key", "order.created.dlq")
            .build();
    }
    
    @Bean
    public Binding orderCreatedBinding() {
        return BindingBuilder.bind(orderCreatedQueue())
            .to(orderExchange())
            .with("order.created");
    }
    
    // 库存相关交换机和队列
    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange("inventory.exchange", true, false);
    }
    
    @Bean
    public Queue inventoryReservedQueue() {
        return QueueBuilder.durable("inventory.reserved.queue")
            .withArgument("x-dead-letter-exchange", "inventory.dlx")
            .withArgument("x-dead-letter-routing-key", "inventory.reserved.dlq")
            .build();
    }
    
    @Bean
    public Binding inventoryReservedBinding() {
        return BindingBuilder.bind(inventoryReservedQueue())
            .to(inventoryExchange())
            .with("inventory.reserved");
    }
}

// 事件发布者
@Component
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    public void publishUserRegisteredEvent(UserRegisteredEvent event) {
        rabbitTemplate.convertAndSend(
            "user.exchange",
            "user.registered",
            event,
            message -> {
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                message.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                return message;
            }
        );
    }
    
    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.created",
            event
        );
    }
    
    public void publishInventoryReservedEvent(InventoryReservedEvent event) {
        rabbitTemplate.convertAndSend(
            "inventory.exchange",
            "inventory.reserved",
            event
        );
    }
}

// 事件监听者
@Component
public class OrderEventListener {
    
    private final NotificationService notificationService;
    private final InventoryService inventoryService;
    
    @RabbitListener(queues = "user.registered.queue")
    public void handleUserRegistered(UserRegisteredEvent event) {
        log.info("处理用户注册事件: {}", event);
        
        // 发送欢迎邮件
        notificationService.sendWelcomeEmail(event.getEmail(), event.getUsername());
        
        // 赠送新用户优惠券
        couponService.grantNewUserCoupon(event.getUserId());
    }
    
    @RabbitListener(queues = "order.created.queue")
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("处理订单创建事件: {}", event);
        
        // 预留库存
        for (OrderItemDto item : event.getItems()) {
            inventoryService.reserveInventory(item.getProductId(), item.getQuantity());
        }
        
        // 发送订单确认通知
        notificationService.sendOrderConfirmation(event.getUserId(), event.getOrderNo());
    }
    
    @RabbitListener(queues = "inventory.reserved.queue")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("处理库存预留事件: {}", event);
        
        if (event.isSuccess()) {
            // 库存预留成功，继续订单处理流程
            orderService.confirmOrder(event.getOrderId());
        } else {
            // 库存不足，取消订单
            orderService.cancelOrder(event.getOrderId(), "库存不足");
        }
    }
}
```

### 4. 分布式事务处理

#### 4.1 Saga 模式实现

**订单创建 Saga**
```java
// OrderSaga.java
@Component
public class OrderSaga {
    
    private final UserServiceClient userServiceClient;
    private final ProductServiceClient productServiceClient;
    private final PaymentServiceClient paymentServiceClient;
    private final OrderRepository orderRepository;
    
    @SagaOrchestrationStart
    public void createOrder(OrderCreateRequest request) {
        SagaManager.start("order-creation-saga")
            .step("validate-user")
                .invoke(() -> validateUser(request.getUserId()))
                .compensate(() -> {/* 无需补偿 */})
            .step("validate-products")
                .invoke(() -> validateProducts(request.getItems()))
                .compensate(() -> {/* 无需补偿 */})
            .step("reserve-inventory")
                .invoke(() -> reserveInventory(request.getItems()))
                .compensate(() -> releaseInventory(request.getItems()))
            .step("create-order")
                .invoke(() -> createOrder(request))
                .compensate(() -> cancelOrder(request.getOrderId()))
            .step("process-payment")
                .invoke(() -> processPayment(request))
                .compensate(() -> refundPayment(request.getOrderId()))
            .step("confirm-order")
                .invoke(() -> confirmOrder(request.getOrderId()))
                .compensate(() -> {/* 最后一步，无需补偿 */})
            .execute();
    }
    
    private void validateUser(Long userId) {
        ApiResponse<UserDto> response = userServiceClient.getUserById(userId);
        if (!response.isSuccess()) {
            throw new SagaException("用户验证失败: " + response.getMessage());
        }
    }
    
    private void validateProducts(List<OrderItemRequest> items) {
        List<Long> productIds = items.stream()
            .map(OrderItemRequest::getProductId)
            .collect(Collectors.toList());
            
        ApiResponse<List<ProductDto>> response = 
            productServiceClient.getProductsByIds(productIds);
            
        if (!response.isSuccess() || response.getData().size() != productIds.size()) {
            throw new SagaException("商品验证失败");
        }
    }
    
    private void reserveInventory(List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            ApiResponse<Boolean> response = productServiceClient.reserveInventory(
                item.getProductId(), item.getQuantity());
                
            if (!response.isSuccess() || !response.getData()) {
                throw new SagaException("库存预留失败: 商品ID " + item.getProductId());
            }
        }
    }
    
    private void releaseInventory(List<OrderItemRequest> items) {
        for (OrderItemRequest item : items) {
            try {
                productServiceClient.releaseInventory(
                    item.getProductId(), item.getQuantity());
            } catch (Exception e) {
                log.error("释放库存失败: 商品ID {}, 数量 {}", 
                    item.getProductId(), item.getQuantity(), e);
            }
        }
    }
    
    private Order createOrder(OrderCreateRequest request) {
        Order order = Order.builder()
            .orderNo(generateOrderNo())
            .userId(request.getUserId())
            .totalAmount(calculateTotalAmount(request.getItems()))
            .status(OrderStatus.PENDING)
            .paymentMethod(request.getPaymentMethod())
            .deliveryAddress(request.getDeliveryAddress())
            .build();
            
        return orderRepository.save(order);
    }
    
    private void cancelOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });
    }
    
    private void processPayment(OrderCreateRequest request) {
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .orderId(request.getOrderId())
            .amount(request.getTotalAmount())
            .paymentMethod(request.getPaymentMethod())
            .build();
            
        ApiResponse<PaymentResult> response = 
            paymentServiceClient.processPayment(paymentRequest);
            
        if (!response.isSuccess() || !response.getData().isSuccess()) {
            throw new SagaException("支付处理失败: " + response.getMessage());
        }
    }
    
    private void refundPayment(Long orderId) {
        try {
            paymentServiceClient.refundPayment(orderId);
        } catch (Exception e) {
            log.error("退款失败: 订单ID {}", orderId, e);
        }
    }
    
    private void confirmOrder(Long orderId) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            
            // 发布订单确认事件
            publishOrderConfirmedEvent(order);
        });
    }
}
```

### 5. 服务发现和负载均衡

#### 5.1 Consul 服务注册

**服务配置**
```yaml
# application.yml
spring:
  application:
    name: user-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        enabled: true
        register: true
        instance-id: ${spring.application.name}:${server.port}
        service-name: ${spring.application.name}
        port: ${server.port}
        prefer-ip-address: true
        ip-address: ${spring.cloud.client.ip-address}
        health-check-path: /actuator/health
        health-check-interval: 10s
        health-check-timeout: 3s
        health-check-critical-timeout: 30s
        tags:
          - version=1.0.0
          - environment=dev

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**负载均衡配置**
```java
// LoadBalancerConfig.java
@Configuration
public class LoadBalancerConfig {
    
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {
        
        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new RoundRobinLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name
        );
    }
}

// 使用服务发现
@Service
public class OrderService {
    
    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;
    
    public UserDto getUserById(Long userId) {
        String url = "http://user-service/api/users/" + userId;
        ApiResponse<UserDto> response = restTemplate.getForObject(
            url, 
            new ParameterizedTypeReference<ApiResponse<UserDto>>() {}
        );
        return response.getData();
    }
}
```

### 6. API 网关

#### 6.1 Spring Cloud Gateway 配置

**网关路由配置**
```yaml
# gateway-service/application.yml
spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # 用户服务路由
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
                redis-rate-limiter.requestedTokens: 1
            - name: CircuitBreaker
              args:
                name: user-service-cb
                fallbackUri: forward:/fallback/user
        
        # 商品服务路由
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 20
                redis-rate-limiter.burstCapacity: 40
                redis-rate-limiter.requestedTokens: 1
        
        # 订单服务路由
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - name: AuthenticationFilter
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 15
                redis-rate-limiter.burstCapacity: 30
                redis-rate-limiter.requestedTokens: 1
      
      default-filters:
        - name: GlobalLoggingFilter
        - name: GlobalCorsFilter
      
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "*"
            allowedMethods:
              - GET
              - POST
              - PUT
              - DELETE
              - OPTIONS
            allowedHeaders: "*"
            allowCredentials: true
            maxAge: 3600

resilience4j:
  circuitbreaker:
    instances:
      user-service-cb:
        registerHealthIndicator: true
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
```

**自定义过滤器**
```java
// AuthenticationFilter.java
@Component
public class AuthenticationFilter implements GatewayFilter, Ordered {
    
    private final JwtTokenProvider tokenProvider;
    private final UserServiceClient userServiceClient;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // 检查是否需要认证
        if (isPublicPath(request.getPath().value())) {
            return chain.filter(exchange);
        }
        
        // 提取 JWT Token
        String token = extractToken(request);
        if (token == null) {
            return handleUnauthorized(exchange);
        }
        
        // 验证 Token
        return validateToken(token)
            .flatMap(userDto -> {
                // 添加用户信息到请求头
                ServerHttpRequest mutatedRequest = request.mutate()
                    .header("X-User-Id", userDto.getId().toString())
                    .header("X-User-Name", userDto.getUsername())
                    .header("X-User-Roles", String.join(",", userDto.getRoles()))
                    .build();
                    
                ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();
                    
                return chain.filter(mutatedExchange);
            })
            .onErrorResume(throwable -> handleUnauthorized(exchange));
    }
    
    private boolean isPublicPath(String path) {
        List<String> publicPaths = Arrays.asList(
            "/api/users/login",
            "/api/users/register",
            "/api/products",
            "/actuator/health"
        );
        return publicPaths.stream().anyMatch(path::startsWith);
    }
    
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
    
    private Mono<UserDto> validateToken(String token) {
        return Mono.fromCallable(() -> {
            if (!tokenProvider.validateToken(token)) {
                throw new UnauthorizedException("Invalid token");
            }
            
            Long userId = tokenProvider.getUserIdFromToken(token);
            ApiResponse<UserDto> response = userServiceClient.getUserById(userId);
            
            if (!response.isSuccess()) {
                throw new UnauthorizedException("User not found");
            }
            
            return response.getData();
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        
        String body = "{\"success\":false,\"code\":\"UNAUTHORIZED\",\"message\":\"认证失败\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}

// GlobalLoggingFilter.java
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalLoggingFilter.class);
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String requestId = UUID.randomUUID().toString();
        
        // 记录请求信息
        log.info("[{}] Request: {} {} from {}", 
            requestId,
            request.getMethod(),
            request.getURI(),
            request.getRemoteAddress()
        );
        
        long startTime = System.currentTimeMillis();
        
        return chain.filter(exchange).doFinally(signalType -> {
            long duration = System.currentTimeMillis() - startTime;
            ServerHttpResponse response = exchange.getResponse();
            
            log.info("[{}] Response: {} in {}ms", 
                requestId,
                response.getStatusCode(),
                duration
            );
        });
    }
    
    @Override
    public int getOrder() {
        return -200;
    }
}
```

## 🎯 部署和监控

### Docker 容器化

**用户服务 Dockerfile**
```dockerfile
# user-service/Dockerfile
FROM openjdk:17-jdk-slim

VOLUME /tmp

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

**Docker Compose 配置**
```yaml
# docker-compose.yml
version: '3.8'

services:
  # 基础设施服务
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root123
      MYSQL_DATABASE: microservices
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./sql/init:/docker-entrypoint-initdb.d
  
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
  
  rabbitmq:
    image: rabbitmq:3-management
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
    ports:
      - "5672:5672"
      - "15672:15672"
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
  
  consul:
    image: consul:1.15
    command: consul agent -dev -client=0.0.0.0
    ports:
      - "8500:8500"
    volumes:
      - consul_data:/consul/data
  
  # 微服务
  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_CLOUD_CONSUL_HOST: consul
    depends_on:
      - consul
      - redis
  
  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/user_service
      SPRING_REDIS_HOST: redis
      SPRING_CLOUD_CONSUL_HOST: consul
      SPRING_RABBITMQ_HOST: rabbitmq
    depends_on:
      - mysql
      - redis
      - rabbitmq
      - consul
  
  product-service:
    build: ./product-service
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/product_service
      SPRING_REDIS_HOST: redis
      SPRING_CLOUD_CONSUL_HOST: consul
      SPRING_RABBITMQ_HOST: rabbitmq
    depends_on:
      - mysql
      - redis
      - rabbitmq
      - consul
  
  order-service:
    build: ./order-service
    ports:
      - "8083:8083"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/order_service
      SPRING_REDIS_HOST: redis
      SPRING_CLOUD_CONSUL_HOST: consul
      SPRING_RABBITMQ_HOST: rabbitmq
    depends_on:
      - mysql
      - redis
      - rabbitmq
      - consul

volumes:
  mysql_data:
  redis_data:
  rabbitmq_data:
  consul_data:

networks:
  default:
    driver: bridge
```

## 🎯 实施路线图

### 第1-2周：基础设施准备
- [ ] 搭建 Docker 开发环境
- [ ] 配置服务注册中心 (Consul)
- [ ] 设置消息队列 (RabbitMQ)
- [ ] 准备数据库分离方案

### 第3-4周：用户服务拆分
- [ ] 创建独立的用户服务
- [ ] 实现 JWT 认证机制
- [ ] 配置服务间通信
- [ ] 完成用户服务测试

### 第5-6周：商品服务拆分
- [ ] 拆分商品管理功能
- [ ] 集成 Elasticsearch 搜索
- [ ] 实现库存管理
- [ ] 配置缓存策略

### 第7-8周：订单服务拆分
- [ ] 创建订单服务
- [ ] 实现分布式事务 (Saga)
- [ ] 配置事件驱动架构
- [ ] 完成端到端测试

### 第9-10周：API 网关和监控
- [ ] 部署 Spring Cloud Gateway
- [ ] 配置限流和熔断
- [ ] 集成监控和日志
- [ ] 性能优化和调试

---

**立即开始**：从数据库拆分开始，为微服务架构迁移奠定坚实的数据隔离基础！