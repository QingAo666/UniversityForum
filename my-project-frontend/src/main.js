import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";
import axios from "axios";
import { createPinia } from "pinia";


import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/assets/quill.css'

axios.defaults.baseURL = 'http://localhost:8080'

//3.引入路由到项目之中
const app = createApp(App)
app.use(router)
app.use(createPinia())

app.mount('#app')
