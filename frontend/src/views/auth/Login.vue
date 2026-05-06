<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="title">Bulls and Cows</h1>
      <h2 class="subtitle">猜数字游戏</h2>
      
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        class="login-form"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="用户名 / 邮箱"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="密码"
            size="large"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            :loading="loading" 
            class="login-button"
            native-type="submit"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="divider">
        <span>或</span>
      </div>
      
      <div class="oauth-buttons">
        <el-button 
          class="oauth-button wechat" 
          @click="handleWechatLogin"
        >
          <span class="oauth-icon">微</span>
          微信登录
        </el-button>
        <el-button 
          class="oauth-button github" 
          @click="handleGithubLogin"
        >
          <span class="oauth-icon">G</span>
          Github 登录
        </el-button>
      </div>
      
      <div class="register-link">
        还没有账号？ <router-link to="/register">去注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const userStore = useUserStore();

const formRef = ref(null);
const loading = ref(false);

const form = reactive({
  username: '',
  password: ''
});

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
};

const handleLogin = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    loading.value = true;
    
    const result = await userStore.login(form.username, form.password);
    
    if (result.success) {
      ElMessage.success('登录成功');
      router.push('/');
    } else {
      ElMessage.error(result.message);
    }
  } catch (error) {
    console.error('登录失败:', error);
  } finally {
    loading.value = false;
  }
};

const handleWechatLogin = () => {
  ElMessage.info('微信登录开发中...');
  // TODO: 实现微信登录
};

const handleGithubLogin = () => {
  ElMessage.info('Github 登录开发中...');
  // TODO: 实现 Github 登录
};
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-card {
  background: white;
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin: 0 0 8px 0;
}

.subtitle {
  text-align: center;
  font-size: 16px;
  color: #666;
  margin: 0 0 32px 0;
}

.login-form {
  margin-bottom: 24px;
}

.login-button {
  width: 100%;
}

.divider {
  text-align: center;
  margin: 24px 0;
  position: relative;
}

.divider::before,
.divider::after {
  content: '';
  position: absolute;
  top: 50%;
  width: calc(50% - 40px);
  height: 1px;
  background: #ddd;
}

.divider::before {
  left: 0;
}

.divider::after {
  right: 0;
}

.divider span {
  color: #999;
  font-size: 14px;
  background: white;
  padding: 0 12px;
  position: relative;
  z-index: 1;
}

.oauth-buttons {
  display: flex;
  gap: 12px;
}

.oauth-button {
  flex: 1;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #ddd;
}

.oauth-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: white;
}

.oauth-button.wechat .oauth-icon {
  background: #07c160;
}

.oauth-button.github .oauth-icon {
  background: #333;
}

.register-link {
  text-align: center;
  margin-top: 24px;
  color: #666;
}

.register-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>
