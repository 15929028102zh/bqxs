<template>
  <div class="app-container">
    <div class="page-header">
      <h1>系统设置</h1>
    </div>

    <el-tabs v-model="activeTab" class="settings-tabs">
      <!-- 基本设置 -->
      <el-tab-pane label="基本设置" name="basic">
        <el-card class="setting-card">
          <template #header>
            <div class="card-header">
              <span>网站基本信息</span>
            </div>
          </template>

          <el-form
            ref="basicFormRef"
            :model="basicForm"
            :rules="basicRules"
            label-width="120px"
            class="setting-form"
          >
            <el-form-item label="网站名称" prop="siteName">
              <el-input
                v-model="basicForm.siteName"
                placeholder="请输入网站名称"
              />
            </el-form-item>
            <el-form-item label="网站Logo">
              <ImageUpload
                v-model="basicForm.siteLogo"
                :multiple="false"
                :max-size="2"
                size="large"
                placeholder="点击上传Logo"
                tip="建议尺寸：200x60px，支持jpg、png格式，大小不超过2MB"
              />
            </el-form-item>
            <el-form-item label="网站描述">
              <el-input
                v-model="basicForm.siteDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入网站描述"
              />
            </el-form-item>
            <el-form-item label="联系电话" prop="contactPhone">
              <el-input
                v-model="basicForm.contactPhone"
                placeholder="请输入联系电话"
              />
            </el-form-item>
            <el-form-item label="联系邮箱" prop="contactEmail">
              <el-input
                v-model="basicForm.contactEmail"
                placeholder="请输入联系邮箱"
              />
            </el-form-item>
            <el-form-item label="公司地址">
              <el-input
                v-model="basicForm.companyAddress"
                placeholder="请输入公司地址"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="handleBasicSubmit"
                :loading="basicLoading"
              >
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 配送设置 -->
      <el-tab-pane label="配送设置" name="delivery">
        <el-card class="setting-card">
          <template #header>
            <div class="card-header">
              <span>配送相关设置</span>
            </div>
          </template>

          <el-form
            ref="deliveryFormRef"
            :model="deliveryForm"
            :rules="deliveryRules"
            label-width="120px"
            class="setting-form"
          >
            <el-form-item label="起送金额" prop="minOrderAmount">
              <el-input-number
                v-model="deliveryForm.minOrderAmount"
                :min="0"
                :precision="2"
                placeholder="起送金额"
                style="width: 200px"
              />
              <span class="unit">元</span>
            </el-form-item>
            <el-form-item label="配送费" prop="deliveryFee">
              <el-input-number
                v-model="deliveryForm.deliveryFee"
                :min="0"
                :precision="2"
                placeholder="配送费"
                style="width: 200px"
              />
              <span class="unit">元</span>
            </el-form-item>
            <el-form-item label="免配送费金额" prop="freeDeliveryAmount">
              <el-input-number
                v-model="deliveryForm.freeDeliveryAmount"
                :min="0"
                :precision="2"
                placeholder="免配送费金额"
                style="width: 200px"
              />
              <span class="unit">元</span>
            </el-form-item>
            <el-form-item label="配送范围" prop="deliveryRange">
              <el-input-number
                v-model="deliveryForm.deliveryRange"
                :min="1"
                :max="50"
                placeholder="配送范围"
                style="width: 200px"
              />
              <span class="unit">公里</span>
            </el-form-item>
            <el-form-item label="配送时间">
              <el-time-picker
                v-model="deliveryTimeRange"
                is-range
                range-separator="至"
                start-placeholder="开始时间"
                end-placeholder="结束时间"
                format="HH:mm"
                value-format="HH:mm"
                @change="handleDeliveryTimeChange"
              />
            </el-form-item>
            <el-form-item label="预计送达时间" prop="estimatedDeliveryTime">
              <el-input-number
                v-model="deliveryForm.estimatedDeliveryTime"
                :min="10"
                :max="120"
                placeholder="预计送达时间"
                style="width: 200px"
              />
              <span class="unit">分钟</span>
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="handleDeliverySubmit"
                :loading="deliveryLoading"
              >
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 支付设置 -->
      <el-tab-pane label="支付设置" name="payment">
        <el-card class="setting-card">
          <template #header>
            <div class="card-header">
              <span>支付方式设置</span>
            </div>
          </template>

          <el-form
            ref="paymentFormRef"
            :model="paymentForm"
            label-width="120px"
            class="setting-form"
          >
            <el-form-item label="微信支付">
              <el-switch v-model="paymentForm.wechatEnabled" />
              <div v-if="paymentForm.wechatEnabled" class="payment-config">
                <el-form-item label="商户号" prop="wechatMerchantId">
                  <el-input
                    v-model="paymentForm.wechatMerchantId"
                    placeholder="请输入微信支付商户号"
                  />
                </el-form-item>
                <el-form-item label="API密钥" prop="wechatApiKey">
                  <el-input
                    v-model="paymentForm.wechatApiKey"
                    type="password"
                    placeholder="请输入微信支付API密钥"
                    show-password
                  />
                </el-form-item>
              </div>
            </el-form-item>

            <el-form-item label="现金支付">
              <el-switch v-model="paymentForm.cashEnabled" />
              <div v-if="paymentForm.cashEnabled" class="payment-config">
                <el-form-item label="记账方式">
                  <el-radio-group v-model="paymentForm.cashAccountingType">
                    <el-radio label="auto">自动记账</el-radio>
                    <el-radio label="manual">手动记账</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="备注" prop="cashRemark">
                  <el-input
                    v-model="paymentForm.cashRemark"
                    type="textarea"
                    :rows="2"
                    placeholder="现金支付相关说明"
                  />
                </el-form-item>
              </div>
            </el-form-item>

            <el-form-item label="货到付款">
              <el-switch v-model="paymentForm.codEnabled" />
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                @click="handlePaymentSubmit"
                :loading="paymentLoading"
              >
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 短信设置 -->
      <el-tab-pane label="短信设置" name="sms">
        <el-card class="setting-card">
          <template #header>
            <div class="card-header">
              <span>短信服务设置</span>
            </div>
          </template>

          <el-form
            ref="smsFormRef"
            :model="smsForm"
            :rules="smsRules"
            label-width="120px"
            class="setting-form"
          >
            <el-form-item label="短信服务商">
              <el-select
                v-model="smsForm.provider"
                placeholder="请选择短信服务商"
                style="width: 200px"
              >
                <el-option label="阿里云" value="aliyun" />
                <el-option label="腾讯云" value="tencent" />
                <el-option label="华为云" value="huawei" />
              </el-select>
            </el-form-item>
            <el-form-item label="AccessKey" prop="accessKey">
              <el-input
                v-model="smsForm.accessKey"
                placeholder="请输入AccessKey"
              />
            </el-form-item>
            <el-form-item label="SecretKey" prop="secretKey">
              <el-input
                v-model="smsForm.secretKey"
                type="password"
                placeholder="请输入SecretKey"
                show-password
              />
            </el-form-item>
            <el-form-item label="签名" prop="signName">
              <el-input
                v-model="smsForm.signName"
                placeholder="请输入短信签名"
              />
            </el-form-item>
            <el-form-item label="验证码模板" prop="verifyTemplate">
              <el-input
                v-model="smsForm.verifyTemplate"
                placeholder="请输入验证码模板ID"
              />
            </el-form-item>
            <el-form-item label="通知模板" prop="notifyTemplate">
              <el-input
                v-model="smsForm.notifyTemplate"
                placeholder="请输入通知模板ID"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                @click="handleSmsSubmit"
                :loading="smsLoading"
              >
                保存设置
              </el-button>
              <el-button @click="handleSmsTest" :loading="testLoading">
                测试发送
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 图片管理 -->
      <el-tab-pane label="图片管理" name="images">
        <el-card class="setting-card">
          <template #header>
            <div class="card-header">
              <span>系统图片设置</span>
            </div>
          </template>

          <el-form
            ref="imageFormRef"
            :model="imageForm"
            label-width="120px"
            class="setting-form"
          >
            <el-form-item label="默认商品图片">
              <ImageUpload
                v-model="imageForm.defaultProductImage"
                :multiple="false"
                :max-size="5"
                placeholder="点击上传默认商品图片"
                tip="当商品没有图片时显示，建议尺寸：400x400px"
              />
            </el-form-item>
            
            <el-form-item label="默认分类图片">
              <ImageUpload
                v-model="imageForm.defaultCategoryImage"
                :multiple="false"
                :max-size="5"
                placeholder="点击上传默认分类图片"
                tip="当分类没有图片时显示，建议尺寸：200x200px"
              />
            </el-form-item>
            
            <el-form-item label="默认用户头像">
              <ImageUpload
                v-model="imageForm.defaultAvatarImage"
                :multiple="false"
                :max-size="2"
                placeholder="点击上传默认头像"
                tip="当用户没有头像时显示，建议尺寸：100x100px"
              />
            </el-form-item>
            
            <el-form-item label="轮播图">
              <ImageUpload
                v-model="imageForm.bannerImages"
                :multiple="true"
                :max-count="5"
                :max-size="10"
                placeholder="点击上传轮播图"
                tip="首页轮播图，建议尺寸：750x300px，最多5张"
              />
            </el-form-item>
            
            <el-form-item label="应用图标">
              <ImageUpload
                v-model="imageForm.appIcon"
                :multiple="false"
                :max-size="1"
                size="default"
                placeholder="点击上传应用图标"
                tip="小程序图标，建议尺寸：144x144px"
              />
            </el-form-item>
            
            <el-form-item>
              <el-button
                type="primary"
                @click="handleImageSubmit"
                :loading="imageLoading"
              >
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 系统维护 -->
      <el-tab-pane label="系统维护" name="maintenance">
        <el-card class="setting-card">
          <template #header>
            <div class="card-header">
              <span>系统维护工具</span>
            </div>
          </template>

          <div class="maintenance-tools">
            <div class="tool-item">
              <div class="tool-info">
                <h4>清理缓存</h4>
                <p>清理系统缓存，提高系统性能</p>
              </div>
              <el-button
                type="primary"
                @click="handleClearCache"
                :loading="clearCacheLoading"
              >
                清理缓存
              </el-button>
            </div>

            <div class="tool-item">
              <div class="tool-info">
                <h4>数据备份</h4>
                <p>备份系统数据，确保数据安全</p>
              </div>
              <el-button
                type="success"
                @click="handleBackupData"
                :loading="backupLoading"
              >
                立即备份
              </el-button>
            </div>

            <div class="tool-item">
              <div class="tool-info">
                <h4>系统日志</h4>
                <p>查看系统运行日志</p>
              </div>
              <el-button type="info" @click="handleViewLogs">
                查看日志
              </el-button>
            </div>

            <div class="tool-item">
              <div class="tool-info">
                <h4>系统信息</h4>
                <p>查看系统运行状态和信息</p>
              </div>
              <el-button type="warning" @click="handleSystemInfo">
                系统信息
              </el-button>
            </div>
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 系统信息对话框 -->
    <el-dialog v-model="systemInfoVisible" title="系统信息" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="系统版本">{{
          systemInfo.version
        }}</el-descriptions-item>
        <el-descriptions-item label="运行时间">{{
          systemInfo.uptime
        }}</el-descriptions-item>
        <el-descriptions-item label="CPU使用率"
          >{{ systemInfo.cpuUsage }}%</el-descriptions-item
        >
        <el-descriptions-item label="内存使用率"
          >{{ systemInfo.memoryUsage }}%</el-descriptions-item
        >
        <el-descriptions-item label="磁盘使用率"
          >{{ systemInfo.diskUsage }}%</el-descriptions-item
        >
        <el-descriptions-item label="数据库连接">{{
          systemInfo.dbStatus
        }}</el-descriptions-item>
        <el-descriptions-item label="Redis连接">{{
          systemInfo.redisStatus
        }}</el-descriptions-item>
        <el-descriptions-item label="最后更新">{{
          formatTime(systemInfo.lastUpdate)
        }}</el-descriptions-item>
      </el-descriptions>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="systemInfoVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleSystemInfo"> 刷新 </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { getSystemImages, updateSystemImage } from '@/api/upload';
import ImageUpload from '@/components/ImageUpload.vue';
import dayjs from 'dayjs';

// 响应式数据
const activeTab = ref('basic');
const basicLoading = ref(false);
const deliveryLoading = ref(false);
const paymentLoading = ref(false);
const smsLoading = ref(false);
const testLoading = ref(false);
const clearCacheLoading = ref(false);
const backupLoading = ref(false);
const imageLoading = ref(false);
const systemInfoVisible = ref(false);
const deliveryTimeRange = ref([]);

// 表单引用
const basicFormRef = ref();
const deliveryFormRef = ref();
const paymentFormRef = ref();
const smsFormRef = ref();
const imageFormRef = ref();

// 上传地址
const uploadUrl = ref('/api/upload/image');

// 基本设置表单
const basicForm = reactive({
  siteName: '新鲜配送',
  siteLogo: '',
  siteDescription: '专业的生鲜配送平台',
  contactPhone: '400-123-4567',
  contactEmail: 'service@freshdelivery.com',
  companyAddress: '北京市朝阳区xxx街道xxx号',
});

// 配送设置表单
const deliveryForm = reactive({
  minOrderAmount: 20,
  deliveryFee: 5,
  freeDeliveryAmount: 50,
  deliveryRange: 10,
  deliveryStartTime: '08:00',
  deliveryEndTime: '22:00',
  estimatedDeliveryTime: 30,
});

// 支付设置表单
const paymentForm = reactive({
  wechatEnabled: true,
  wechatMerchantId: '',
  wechatApiKey: '',
  cashEnabled: true,
  cashAccountingType: 'auto',
  cashRemark: '现金支付，系统自动记账',
  codEnabled: true,
});

// 短信设置表单
const smsForm = reactive({
  provider: 'aliyun',
  accessKey: '',
  secretKey: '',
  signName: '',
  verifyTemplate: '',
  notifyTemplate: '',
});

// 图片设置表单
const imageForm = reactive({
  defaultProductImage: '',
  defaultCategoryImage: '',
  defaultAvatarImage: '',
  bannerImages: [],
  appIcon: ''
});

// 系统信息
const systemInfo = reactive({
  version: '1.0.0',
  uptime: '7天12小时',
  cpuUsage: 25,
  memoryUsage: 68,
  diskUsage: 45,
  dbStatus: '正常',
  redisStatus: '正常',
  lastUpdate: new Date(),
});

// 表单验证规则
const basicRules = {
  siteName: [{ required: true, message: '请输入网站名称', trigger: 'blur' }],
  contactPhone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '请输入正确的手机号码',
      trigger: 'blur',
    },
  ],
  contactEmail: [
    { required: true, message: '请输入联系邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' },
  ],
};

const deliveryRules = {
  minOrderAmount: [
    { required: true, message: '请输入起送金额', trigger: 'blur' },
  ],
  deliveryFee: [{ required: true, message: '请输入配送费', trigger: 'blur' }],
  freeDeliveryAmount: [
    { required: true, message: '请输入免配送费金额', trigger: 'blur' },
  ],
  deliveryRange: [
    { required: true, message: '请输入配送范围', trigger: 'blur' },
  ],
  estimatedDeliveryTime: [
    { required: true, message: '请输入预计送达时间', trigger: 'blur' },
  ],
};

const smsRules = {
  accessKey: [{ required: true, message: '请输入AccessKey', trigger: 'blur' }],
  secretKey: [{ required: true, message: '请输入SecretKey', trigger: 'blur' }],
  signName: [{ required: true, message: '请输入短信签名', trigger: 'blur' }],
  verifyTemplate: [
    { required: true, message: '请输入验证码模板ID', trigger: 'blur' },
  ],
};

// 配送时间改变
const handleDeliveryTimeChange = (times) => {
  if (times && times.length === 2) {
    deliveryForm.deliveryStartTime = times[0];
    deliveryForm.deliveryEndTime = times[1];
  }
};

// Logo上传成功
const handleLogoSuccess = (response) => {
  if (response.code === 200) {
    basicForm.siteLogo = response.data.url;
    ElMessage.success('Logo上传成功');
  } else {
    ElMessage.error('Logo上传失败');
  }
};

// Logo上传前验证
const beforeLogoUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('Logo只能是 JPG/PNG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('Logo大小不能超过 2MB!');
  }
  return isJPG && isLt2M;
};

// 保存基本设置
const handleBasicSubmit = async () => {
  try {
    const valid = await basicFormRef.value.validate();
    if (!valid) return;

    basicLoading.value = true;

    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success('基本设置保存成功');
  } catch (error) {
    ElMessage.error('保存失败');
  } finally {
    basicLoading.value = false;
  }
};

// 保存配送设置
const handleDeliverySubmit = async () => {
  try {
    const valid = await deliveryFormRef.value.validate();
    if (!valid) return;

    deliveryLoading.value = true;

    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success('配送设置保存成功');
  } catch (error) {
    ElMessage.error('保存失败');
  } finally {
    deliveryLoading.value = false;
  }
};

// 保存支付设置
const handlePaymentSubmit = async () => {
  try {
    paymentLoading.value = true;

    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success('支付设置保存成功');
  } catch (error) {
    ElMessage.error('保存失败');
  } finally {
    paymentLoading.value = false;
  }
};

// 保存短信设置
const handleSmsSubmit = async () => {
  try {
    const valid = await smsFormRef.value.validate();
    if (!valid) return;

    smsLoading.value = true;

    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 1000));

    ElMessage.success('短信设置保存成功');
  } catch (error) {
    ElMessage.error('保存失败');
  } finally {
    smsLoading.value = false;
  }
};

// 测试短信发送
const handleSmsTest = async () => {
  try {
    const { value: phone } = await ElMessageBox.prompt(
      '请输入测试手机号',
      '测试短信发送',
      {
        confirmButtonText: '发送',
        cancelButtonText: '取消',
        inputPattern: /^1[3-9]\d{9}$/,
        inputErrorMessage: '请输入正确的手机号码',
      }
    );

    testLoading.value = true;

    // 模拟API调用
    console.log('发送测试短信到:', phone);
    await new Promise((resolve) => setTimeout(resolve, 2000));

    ElMessage.success(`测试短信已发送到 ${phone}`)
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('测试短信发送失败');
    }
  } finally {
    testLoading.value = false;
  }
};

// 清理缓存
const handleClearCache = async () => {
  try {
    await ElMessageBox.confirm('确定要清理系统缓存吗？', '清理确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    });

    clearCacheLoading.value = true;

    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 2000));

    ElMessage.success('缓存清理成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('缓存清理失败');
    }
  } finally {
    clearCacheLoading.value = false;
  }
};

// 数据备份
const handleBackupData = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要进行数据备份吗？备份过程可能需要几分钟时间。',
      '备份确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info',
      }
    );

    backupLoading.value = true;

    // 模拟API调用
    await new Promise((resolve) => setTimeout(resolve, 3000));

    ElMessage.success('数据备份成功');
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('数据备份失败');
    }
  } finally {
    backupLoading.value = false;
  }
};

// 查看日志
const handleViewLogs = () => {
  ElMessage.info('日志查看功能开发中...');
};

// 系统信息
const handleSystemInfo = () => {
  // 模拟获取系统信息
  systemInfo.lastUpdate = new Date();
  systemInfo.cpuUsage = Math.floor(Math.random() * 50) + 20;
  systemInfo.memoryUsage = Math.floor(Math.random() * 40) + 50;
  systemInfo.diskUsage = Math.floor(Math.random() * 30) + 30;

  systemInfoVisible.value = true;
};

// 格式化时间
const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-';
};

// 保存图片设置
const saveImageSettings = async () => {
  try {
    imageLoading.value = true;
    
    // 调用API保存图片设置
    await updateSystemImage(imageForm);
    
    ElMessage.success('图片设置保存成功');
  } catch (error) {
    console.error('保存图片设置失败:', error);
    ElMessage.error('保存图片设置失败');
  } finally {
    imageLoading.value = false;
  }
};

// 加载图片设置
const loadImageSettings = async () => {
  try {
    const response = await getSystemImages();
    Object.assign(imageForm, response.data);
  } catch (error) {
    console.error('加载图片设置失败:', error);
  }
};

// 页面加载时初始化数据
onMounted(() => {
  // 初始化配送时间范围
  deliveryTimeRange.value = [
    deliveryForm.deliveryStartTime,
    deliveryForm.deliveryEndTime,
  ];
  
  // 加载图片设置
  loadImageSettings();
});
</script>

<style lang="scss" scoped>
.settings-tabs {
  :deep(.el-tabs__content) {
    padding: 0;
  }
}

.setting-card {
  margin-bottom: 20px;

  .card-header {
    font-size: 16px;
    font-weight: 600;
    color: #2c3e50;
  }
}

.setting-form {
  max-width: 600px;

  .unit {
    margin-left: 10px;
    color: #999;
  }
}

.upload-container {
  .logo-uploader {
    :deep(.el-upload) {
      border: 1px dashed #d9d9d9;
      border-radius: 6px;
      cursor: pointer;
      position: relative;
      overflow: hidden;
      transition: 0.2s;

      &:hover {
        border-color: #409eff;
      }
    }

    .logo {
      width: 200px;
      height: 60px;
      display: block;
    }

    .logo-uploader-icon {
      font-size: 28px;
      color: #8c939d;
      width: 200px;
      height: 60px;
      text-align: center;
      line-height: 60px;
    }
  }

  .upload-tip {
    margin-top: 8px;
    font-size: 12px;
    color: #999;
  }
}

.payment-config {
  margin-left: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-top: 10px;
}

.maintenance-tools {
  .tool-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    border: 1px solid #e4e7ed;
    border-radius: 8px;
    margin-bottom: 15px;

    &:last-child {
      margin-bottom: 0;
    }

    .tool-info {
      h4 {
        margin: 0 0 5px 0;
        font-size: 16px;
        color: #2c3e50;
      }

      p {
        margin: 0;
        font-size: 14px;
        color: #7f8c8d;
      }
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .setting-form {
    max-width: 100%;

    .el-form-item {
      .el-input,
      .el-select,
      .el-input-number {
        width: 100% !important;
      }
    }
  }

  .maintenance-tools {
    .tool-item {
      flex-direction: column;
      align-items: flex-start;

      .tool-info {
        margin-bottom: 15px;
      }
    }
  }
}
</style>
