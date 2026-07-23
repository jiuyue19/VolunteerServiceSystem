import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/theme.css'
import '@vueup/vue-quill/dist/vue-quill.snow.css'

import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

if (typeof window !== 'undefined') {
  window._AMapSecurityConfig = {
    // 高德 JSAPI 安全密钥，由用户在高德控制台申请
    securityJsCode: '4140336fee57d1e660379f91c7acdd5b'
  }
}

const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.use(createPinia())
app.mount('#app')