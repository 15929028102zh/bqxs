# 边墙鲜送系统 - 代码质量与可维护性全面提升方案

## 🎯 当前项目状态评估

### 项目优势
✅ **架构清晰**：前后端分离 + 微信小程序的现代化架构  
✅ **技术栈成熟**：Spring Boot + Vue 3 + MySQL 的稳定组合  
✅ **容器化部署**：完整的 Docker 部署方案  
✅ **文档完善**：详细的部署和使用文档  

### 待改进领域
🔧 **代码规范**：缺少统一的编码标准和自动化检查  
🔧 **测试覆盖**：单元测试和集成测试不足  
🔧 **错误处理**：异常处理机制需要标准化  
🔧 **安全加固**：需要加强输入验证和权限控制  
🔧 **性能优化**：数据库查询和缓存策略需要优化  

## 🚀 立即可实施的改进措施

### 1. 后端代码质量提升

#### A. 统一异常处理机制

**创建全局异常处理器**
```java
// backend/src/main/java/com/example/exception/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.ok(ApiResponse.error(e.getCode(), e.getMessage()));
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException e) {
        log.warn("参数验证失败: {}", e.getMessage());
        return ResponseEntity.badRequest()
            .body(ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception e) {
        log.error("系统异常", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("SYSTEM_ERROR", "系统繁忙，请稍后重试"));
    }
}
```

**标准化 API 响应格式**
```java
// backend/src/main/java/com/example/common/ApiResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    private Long timestamp;
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .code("SUCCESS")
            .message("操作成功")
            .data(data)
            .timestamp(System.currentTimeMillis())
            .build();
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .code(code)
            .message(message)
            .timestamp(System.currentTimeMillis())
            .build();
    }
}
```

#### B. 数据验证增强

**创建自定义验证注解**
```java
// backend/src/main/java/com/example/validation/Phone.java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PhoneValidator.class)
public @interface Phone {
    String message() default "手机号格式不正确";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

@Component
public class PhoneValidator implements ConstraintValidator<Phone, String> {
    private static final Pattern PHONE_PATTERN = 
        Pattern.compile("^1[3-9]\\d{9}$");
    
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && PHONE_PATTERN.matcher(value).matches();
    }
}
```

#### C. 服务层优化

**引入缓存机制**
```java
// backend/src/main/java/com/example/service/ProductService.java
@Service
@Slf4j
public class ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Cacheable(value = "products", key = "#categoryId", unless = "#result.isEmpty()")
    public List<Product> getProductsByCategory(Long categoryId) {
        log.info("从数据库查询分类商品: {}", categoryId);
        return productMapper.selectByCategory(categoryId);
    }
    
    @CacheEvict(value = "products", allEntries = true)
    public void updateProduct(Product product) {
        productMapper.updateById(product);
        log.info("商品更新完成，清除缓存: {}", product.getId());
    }
}
```

### 2. 前端代码质量提升

#### A. 组件标准化

**创建基础组件库**
```vue
<!-- admin-frontend/src/components/base/BaseTable.vue -->
<template>
  <div class="base-table">
    <el-table
      :data="data"
      :loading="loading"
      @selection-change="handleSelectionChange"
      v-bind="$attrs"
    >
      <el-table-column
        v-if="showSelection"
        type="selection"
        width="55"
      />
      <slot />
    </el-table>
    
    <el-pagination
      v-if="showPagination"
      :current-page="pagination.page"
      :page-size="pagination.size"
      :total="pagination.total"
      @current-change="handlePageChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>

<script setup>
defineProps({
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  showSelection: { type: Boolean, default: false },
  showPagination: { type: Boolean, default: true },
  pagination: {
    type: Object,
    default: () => ({ page: 1, size: 10, total: 0 })
  }
})

const emit = defineEmits(['selection-change', 'page-change', 'size-change'])

const handleSelectionChange = (selection) => {
  emit('selection-change', selection)
}

const handlePageChange = (page) => {
  emit('page-change', page)
}

const handleSizeChange = (size) => {
  emit('size-change', size)
}
</script>
```

#### B. 状态管理优化

**Pinia Store 标准化**
```javascript
// admin-frontend/src/stores/modules/product.js
import { defineStore } from 'pinia'
import { productApi } from '@/api/product'

export const useProductStore = defineStore('product', {
  state: () => ({
    products: [],
    categories: [],
    loading: false,
    error: null
  }),
  
  getters: {
    getProductById: (state) => (id) => {
      return state.products.find(product => product.id === id)
    },
    
    getProductsByCategory: (state) => (categoryId) => {
      return state.products.filter(product => product.categoryId === categoryId)
    }
  },
  
  actions: {
    async fetchProducts(params = {}) {
      this.loading = true
      this.error = null
      
      try {
        const response = await productApi.getProducts(params)
        this.products = response.data
        return response
      } catch (error) {
        this.error = error.message
        throw error
      } finally {
        this.loading = false
      }
    },
    
    async createProduct(productData) {
      try {
        const response = await productApi.createProduct(productData)
        this.products.unshift(response.data)
        return response
      } catch (error) {
        this.error = error.message
        throw error
      }
    },
    
    async updateProduct(id, productData) {
      try {
        const response = await productApi.updateProduct(id, productData)
        const index = this.products.findIndex(p => p.id === id)
        if (index !== -1) {
          this.products[index] = response.data
        }
        return response
      } catch (error) {
        this.error = error.message
        throw error
      }
    }
  }
})
```

#### C. API 请求标准化

**统一请求拦截器**
```javascript
// admin-frontend/src/utils/request.js
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/modules/user'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    
    // 添加请求ID用于追踪
    config.headers['X-Request-ID'] = generateRequestId()
    
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    
    if (code === 'SUCCESS') {
      return { data, message }
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message || '请求失败'))
    }
  },
  error => {
    const { response } = error
    
    if (response?.status === 401) {
      ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const userStore = useUserStore()
        userStore.logout()
        router.push('/login')
      })
    } else {
      const message = response?.data?.message || error.message || '网络错误'
      ElMessage.error(message)
    }
    
    return Promise.reject(error)
  }
)

function generateRequestId() {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

export default service
```

### 3. 小程序代码优化

#### A. 组件化改进

**创建通用组件**
```javascript
// miniprogram/components/product-card/product-card.js
Component({
  properties: {
    product: {
      type: Object,
      value: {}
    },
    showAddButton: {
      type: Boolean,
      value: true
    }
  },
  
  data: {
    imageError: false
  },
  
  methods: {
    onImageError() {
      this.setData({ imageError: true })
    },
    
    onAddToCart() {
      this.triggerEvent('add-to-cart', {
        product: this.data.product
      })
    },
    
    onProductTap() {
      wx.navigateTo({
        url: `/pages/product/detail?id=${this.data.product.id}`
      })
    }
  }
})
```

#### B. 状态管理优化

**全局状态管理**
```javascript
// miniprogram/utils/store.js
class Store {
  constructor() {
    this.state = {
      user: null,
      cart: [],
      orders: []
    }
    this.listeners = []
  }
  
  getState() {
    return { ...this.state }
  }
  
  setState(newState) {
    this.state = { ...this.state, ...newState }
    this.notify()
  }
  
  subscribe(listener) {
    this.listeners.push(listener)
    return () => {
      const index = this.listeners.indexOf(listener)
      if (index > -1) {
        this.listeners.splice(index, 1)
      }
    }
  }
  
  notify() {
    this.listeners.forEach(listener => listener(this.state))
  }
  
  // 购物车操作
  addToCart(product) {
    const cart = [...this.state.cart]
    const existingItem = cart.find(item => item.id === product.id)
    
    if (existingItem) {
      existingItem.quantity += 1
    } else {
      cart.push({ ...product, quantity: 1 })
    }
    
    this.setState({ cart })
    wx.showToast({ title: '已添加到购物车' })
  }
  
  removeFromCart(productId) {
    const cart = this.state.cart.filter(item => item.id !== productId)
    this.setState({ cart })
  }
  
  updateCartQuantity(productId, quantity) {
    const cart = this.state.cart.map(item => 
      item.id === productId ? { ...item, quantity } : item
    )
    this.setState({ cart })
  }
}

export default new Store()
```

## 🔒 安全性增强

### 1. 输入验证和SQL注入防护

**MyBatis 参数化查询**
```xml
<!-- backend/src/main/resources/mapper/UserMapper.xml -->
<mapper namespace="com.example.mapper.UserMapper">
    <!-- 正确的参数化查询 -->
    <select id="findByPhone" resultType="User">
        SELECT * FROM users 
        WHERE phone = #{phone} 
        AND status = 'ACTIVE'
    </select>
    
    <!-- 动态查询条件 -->
    <select id="searchUsers" resultType="User">
        SELECT * FROM users
        <where>
            <if test="name != null and name != ''">
                AND name LIKE CONCAT('%', #{name}, '%')
            </if>
            <if test="phone != null and phone != ''">
                AND phone = #{phone}
            </if>
            <if test="status != null">
                AND status = #{status}
            </if>
        </where>
        ORDER BY created_at DESC
    </select>
</mapper>
```

### 2. 权限控制增强

**基于注解的权限控制**
```java
// backend/src/main/java/com/example/security/RequirePermission.java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String[] value() default {};
    boolean requireAll() default false;
}

@Aspect
@Component
@Slf4j
public class PermissionAspect {
    
    @Around("@annotation(requirePermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint, RequirePermission requirePermission) throws Throwable {
        String[] permissions = requirePermission.value();
        boolean requireAll = requirePermission.requireAll();
        
        // 获取当前用户
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new SecurityException("用户未登录");
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        
        // 检查权限
        boolean hasPermission = requireAll 
            ? hasAllPermissions(userDetails, permissions)
            : hasAnyPermission(userDetails, permissions);
            
        if (!hasPermission) {
            throw new SecurityException("权限不足");
        }
        
        return joinPoint.proceed();
    }
}
```

### 3. 敏感信息保护

**配置文件加密**
```yaml
# backend/src/main/resources/application-prod.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fresh_delivery?useSSL=true
    username: ENC(encrypted_username)
    password: ENC(encrypted_password)
  
  redis:
    password: ENC(encrypted_redis_password)

# 微信小程序配置
wechat:
  miniprogram:
    app-id: ENC(encrypted_app_id)
    app-secret: ENC(encrypted_app_secret)
```

## 📊 性能优化策略

### 1. 数据库优化

**索引优化建议**
```sql
-- 用户表索引
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_status_created ON users(status, created_at);

-- 订单表索引
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_orders_created_at ON orders(created_at);
CREATE INDEX idx_orders_status_updated ON orders(status, updated_at);

-- 商品表索引
CREATE INDEX idx_products_category_status ON products(category_id, status);
CREATE INDEX idx_products_name_fulltext ON products(name) USING FULLTEXT;

-- 订单详情表索引
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);
```

**查询优化**
```java
// 使用分页查询避免大量数据加载
@Service
public class OrderService {
    
    public PageResult<Order> getOrdersByUser(Long userId, int page, int size) {
        PageHelper.startPage(page, size);
        List<Order> orders = orderMapper.selectByUserId(userId);
        PageInfo<Order> pageInfo = new PageInfo<>(orders);
        
        return PageResult.<Order>builder()
            .data(pageInfo.getList())
            .total(pageInfo.getTotal())
            .page(page)
            .size(size)
            .build();
    }
    
    // 批量查询避免N+1问题
    public List<OrderWithItems> getOrdersWithItems(List<Long> orderIds) {
        List<Order> orders = orderMapper.selectByIds(orderIds);
        List<OrderItem> items = orderItemMapper.selectByOrderIds(orderIds);
        
        Map<Long, List<OrderItem>> itemsMap = items.stream()
            .collect(Collectors.groupingBy(OrderItem::getOrderId));
            
        return orders.stream()
            .map(order -> OrderWithItems.builder()
                .order(order)
                .items(itemsMap.getOrDefault(order.getId(), Collections.emptyList()))
                .build())
            .collect(Collectors.toList());
    }
}
```

### 2. 缓存策略

**多级缓存架构**
```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
        
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // 商品缓存 - 1小时
        cacheConfigurations.put("products", config.entryTtl(Duration.ofHours(1)));
        
        // 用户缓存 - 30分钟
        cacheConfigurations.put("users", config.entryTtl(Duration.ofMinutes(30)));
        
        // 分类缓存 - 2小时
        cacheConfigurations.put("categories", config.entryTtl(Duration.ofHours(2)));
        
        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build();
    }
}
```

### 3. 前端性能优化

**组件懒加载**
```javascript
// admin-frontend/src/router/index.js
const routes = [
  {
    path: '/products',
    name: 'Products',
    component: () => import('@/views/product/ProductList.vue'),
    meta: { title: '商品管理' }
  },
  {
    path: '/orders',
    name: 'Orders', 
    component: () => import('@/views/order/OrderList.vue'),
    meta: { title: '订单管理' }
  }
]
```

**图片懒加载和压缩**
```vue
<template>
  <div class="image-container">
    <img
      v-lazy="imageUrl"
      :alt="alt"
      @load="onImageLoad"
      @error="onImageError"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  src: String,
  alt: String,
  width: Number,
  height: Number
})

const imageLoaded = ref(false)
const imageError = ref(false)

const imageUrl = computed(() => {
  if (!props.src) return ''
  
  // 根据设备像素比和尺寸生成优化的图片URL
  const dpr = window.devicePixelRatio || 1
  const width = props.width ? Math.ceil(props.width * dpr) : 'auto'
  const height = props.height ? Math.ceil(props.height * dpr) : 'auto'
  
  return `${props.src}?w=${width}&h=${height}&q=80&f=webp`
})

const onImageLoad = () => {
  imageLoaded.value = true
}

const onImageError = () => {
  imageError.value = true
}
</script>
```

## 🧪 测试策略完善

### 1. 后端单元测试

**Service 层测试**
```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    
    @Mock
    private ProductMapper productMapper;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @InjectMocks
    private ProductService productService;
    
    @Test
    @DisplayName("根据分类ID查询商品 - 成功")
    void getProductsByCategory_Success() {
        // Given
        Long categoryId = 1L;
        List<Product> expectedProducts = Arrays.asList(
            Product.builder().id(1L).name("苹果").categoryId(categoryId).build(),
            Product.builder().id(2L).name("香蕉").categoryId(categoryId).build()
        );
        
        when(productMapper.selectByCategory(categoryId))
            .thenReturn(expectedProducts);
        
        // When
        List<Product> actualProducts = productService.getProductsByCategory(categoryId);
        
        // Then
        assertThat(actualProducts)
            .hasSize(2)
            .extracting(Product::getName)
            .containsExactly("苹果", "香蕉");
        
        verify(productMapper).selectByCategory(categoryId);
    }
    
    @Test
    @DisplayName("根据分类ID查询商品 - 分类不存在")
    void getProductsByCategory_CategoryNotFound() {
        // Given
        Long categoryId = 999L;
        when(productMapper.selectByCategory(categoryId))
            .thenReturn(Collections.emptyList());
        
        // When
        List<Product> actualProducts = productService.getProductsByCategory(categoryId);
        
        // Then
        assertThat(actualProducts).isEmpty();
    }
}
```

### 2. 前端单元测试

**组件测试**
```javascript
// admin-frontend/src/components/__tests__/ProductCard.spec.js
import { mount } from '@vue/test-utils'
import { describe, it, expect, vi } from 'vitest'
import ProductCard from '../ProductCard.vue'

describe('ProductCard', () => {
  const mockProduct = {
    id: 1,
    name: '新鲜苹果',
    price: 12.5,
    image: '/images/apple.jpg',
    description: '新鲜美味的苹果'
  }
  
  it('正确渲染商品信息', () => {
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct }
    })
    
    expect(wrapper.find('.product-name').text()).toBe('新鲜苹果')
    expect(wrapper.find('.product-price').text()).toContain('12.5')
    expect(wrapper.find('img').attributes('src')).toBe('/images/apple.jpg')
  })
  
  it('点击添加到购物车触发事件', async () => {
    const wrapper = mount(ProductCard, {
      props: { product: mockProduct }
    })
    
    await wrapper.find('.add-to-cart-btn').trigger('click')
    
    expect(wrapper.emitted('add-to-cart')).toBeTruthy()
    expect(wrapper.emitted('add-to-cart')[0]).toEqual([mockProduct])
  })
  
  it('图片加载失败显示默认图片', async () => {
    const wrapper = mount(ProductCard, {
      props: { product: { ...mockProduct, image: 'invalid-url' } }
    })
    
    await wrapper.find('img').trigger('error')
    
    expect(wrapper.find('img').attributes('src')).toBe('/images/default-product.png')
  })
})
```

### 3. 集成测试

**API 集成测试**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ProductControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
        .withDatabaseName("test_db")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Test
    @DisplayName("创建商品 - 成功")
    void createProduct_Success() {
        // Given
        CreateProductRequest request = CreateProductRequest.builder()
            .name("测试商品")
            .price(new BigDecimal("19.99"))
            .categoryId(1L)
            .description("测试商品描述")
            .build();
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.postForEntity(
            "/api/products", request, ApiResponse.class);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isSuccess()).isTrue();
        
        // 验证数据库中的数据
        List<Product> products = productRepository.findByName("测试商品");
        assertThat(products).hasSize(1);
        assertThat(products.get(0).getPrice()).isEqualTo(new BigDecimal("19.99"));
    }
}
```

## 📈 监控和日志改进

### 1. 结构化日志

**日志配置**
```xml
<!-- backend/src/main/resources/logback-spring.xml -->
<configuration>
    <springProfile name="!prod">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
    </springProfile>
    
    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>logs/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>100MB</maxFileSize>
                <maxHistory>30</maxHistory>
                <totalSizeCap>3GB</totalSizeCap>
            </rollingPolicy>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
        </appender>
    </springProfile>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

### 2. 应用监控

**Micrometer 指标配置**
```java
@Configuration
public class MetricsConfig {
    
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
    
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags(
            "application", "fresh-delivery-system",
            "environment", environment
        );
    }
}

@Service
public class OrderService {
    
    private final Counter orderCreatedCounter;
    private final Timer orderProcessingTimer;
    
    public OrderService(MeterRegistry meterRegistry) {
        this.orderCreatedCounter = Counter.builder("orders.created")
            .description("订单创建数量")
            .register(meterRegistry);
            
        this.orderProcessingTimer = Timer.builder("orders.processing.time")
            .description("订单处理时间")
            .register(meterRegistry);
    }
    
    @Timed(name = "orders.create", description = "创建订单耗时")
    public Order createOrder(CreateOrderRequest request) {
        return orderProcessingTimer.recordCallable(() -> {
            Order order = processOrder(request);
            orderCreatedCounter.increment();
            return order;
        });
    }
}
```

## 🔄 持续改进流程

### 实施时间表

**第1周：基础设施**
- [ ] 配置代码质量工具（ESLint, Checkstyle）
- [ ] 设置 Git hooks
- [ ] 创建 CI/CD 流水线
- [ ] 配置测试环境

**第2-3周：代码重构**
- [ ] 统一异常处理
- [ ] 标准化 API 响应格式
- [ ] 优化数据库查询
- [ ] 添加缓存机制

**第4周：测试和文档**
- [ ] 编写单元测试
- [ ] 添加集成测试
- [ ] 完善 API 文档
- [ ] 更新部署文档

**第5周：监控和优化**
- [ ] 配置应用监控
- [ ] 优化性能瓶颈
- [ ] 安全加固
- [ ] 生产环境验证

### 成功指标

- **代码质量**：代码覆盖率 > 80%，静态分析无严重问题
- **性能**：API 响应时间 < 200ms，页面加载时间 < 2s
- **稳定性**：系统可用性 > 99.9%，错误率 < 0.1%
- **安全性**：无已知安全漏洞，通过安全扫描
- **可维护性**：新功能开发周期缩短 30%，Bug 修复时间 < 2小时

---

**立即开始**：选择一个改进点开始实施，建议从 Git 用户配置和代码规范工具开始！