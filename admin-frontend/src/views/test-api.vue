<template>
  <div class="test-api">
    <h2>API测试页面</h2>
    <el-button @click="testLogin" :loading="loading">测试登录API</el-button>
    <div v-if="result" class="result">
      <h3>结果:</h3>
      <pre>{{ result }}</pre>
    </div>
    <div v-if="error" class="error">
      <h3>错误:</h3>
      <pre>{{ error }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const loading = ref(false)
const result = ref('')
const error = ref('')

const testLogin = async () => {
  loading.value = true
  result.value = ''
  error.value = ''
  
  try {
    console.log('开始测试API调用...')
    
    // 直接调用API
    const response = await axios.post('/api/admin/login', {
      username: 'admin',
      password: '123456'
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('API调用成功:', response)
    result.value = JSON.stringify(response.data, null, 2)
  } catch (err) {
    console.error('API调用失败:', err)
    error.value = JSON.stringify({
      message: err.message,
      status: err.response?.status,
      statusText: err.response?.statusText,
      data: err.response?.data,
      config: {
        url: err.config?.url,
        method: err.config?.method,
        baseURL: err.config?.baseURL
      }
    }, null, 2)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.test-api {
  padding: 20px;
}

.result, .error {
  margin-top: 20px;
  padding: 10px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.error {
  border-color: #f56c6c;
  background-color: #fef0f0;
}

.result {
  border-color: #67c23a;
  background-color: #f0f9ff;
}

pre {
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>