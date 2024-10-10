<script setup>
import Card from "@/components/Card.vue";
import {Lock, Switch} from "@element-plus/icons-vue";
import {reactive, ref} from "vue";
import {get, post} from "@/net/index.js";
import {ElMessage} from "element-plus";

const form = reactive({
  password: '',
  new_password: '',
  new_password_repeat: '',
})

const validatePassword = (rule,value,callback) =>{
  if(value === '')
    callback(new Error('请输入密码'))
  else if(value !== form.new_password)
    callback(new Error('两次输入的密码不一致'))
  else
    callback()

}

const rules = {
  password: [
    { required:true, message:'请输入原密码', trigger:'blur'},
  ],
  new_password: [
    { required:true, message:'请输入新密码', trigger:'blur'},
    { min:6, max:20, message:'密码的长度必须在6-20个字符之间',trigger: 'blur' }
  ],
  new_password_repeat: [
    { required:true, message:'请重新输入新密码', trigger:'blur'},
    { validator:validatePassword, trigger:['blur','change'] }
  ]
}

const formRef = ref()
const valid = ref(false)
const onValidate = (prop, isValid) => valid.value = isValid
function resetPassword(){
  formRef.value.validate(valid => {
    if(valid){
      post('api/user/change-password',form,() => {
        ElMessage.success('修改密码成功！')
        formRef.value.resetFields()
      })
    }
  })
}

const saving = ref(true)
const privacy = reactive({
  gender: false,
  phone: false,
  qq: false,
  wx: false,
  email: false,
})
get('api/user/privacy', data => {
  privacy.gender = data.gender;
  privacy.phone = data.phone;
  privacy.qq = data.qq;
  privacy.wx = data.wx;
  privacy.email = data.email;
  saving.value = false;
})

function savePrivacy(type,status){
  saving.value = true
  post('api/user/save-privacy', {
    type: type,
    status: status
  },() => {
    ElMessage.success('隐私设置修改成功！')
    saving.value = false;
  })
}


</script>

<template>
  <div style="max-width: 600px; margin: auto;">
    <div style="margin-top: 20px;">
      <card :icon="Lock" title="隐私设置" desc="在这里设置哪些内容可以被其他人看到，请各位小伙伴注重自己的隐私哦" v-loading="saving">
        <div class="checkbox-list">
          <el-checkbox @change="savePrivacy('gender',privacy.gender)" v-model="privacy.gender">公开展示我的性别</el-checkbox>
          <el-checkbox @change="savePrivacy('phone',privacy.phone)" v-model="privacy.phone">公开展示我的手机号</el-checkbox>
          <el-checkbox @change="savePrivacy('qq',privacy.qq)" v-model="privacy.qq">公开展示我的企鹅号</el-checkbox>
          <el-checkbox @change="savePrivacy('wx',privacy.wx)" v-model="privacy.wx">公开展示我的微信号</el-checkbox>
          <el-checkbox @change="savePrivacy('email',privacy.email)" v-model="privacy.email">公开展示我的电子邮件</el-checkbox>
        </div>
      </card>

      <card style="margin: 20px 0" :icon="Lock" title="修改密码" desc="修改密码请在这里操作，请务必牢记您的新密码">
        <el-form label-width="85" ref="formRef" @validate="onValidate" :rules="rules" :model="form" style="margin: 20px">
          <el-form-item label="原密码" prop="password">
            <el-input :prefix-icon="Lock" type="password" v-model="form.password" placeholder="当前密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="新密码" prop="new_password">
            <el-input :prefix-icon="Lock" type="password" v-model="form.new_password" placeholder="新密码" maxlength="16"/>
          </el-form-item>
          <el-form-item label="重复新密码" prop="new_password_repeat">
            <el-input :prefix-icon="Lock" type="password" v-model="form.new_password_repeat" placeholder="重复新密码" maxlength="16"/>
          </el-form-item>
          <div style="text-align: center">
            <el-button :icon="Switch" @click="resetPassword" type="success">立即重置密码</el-button>
          </div>
        </el-form>
      </card>
    </div>
  </div>
</template>

<style scoped>
.checkbox-list{
  margin: 10px 0 0 10px;
  display: flex;
  flex-direction: column;
}
</style>