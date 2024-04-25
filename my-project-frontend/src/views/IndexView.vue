<template>
  <div class="main-content">
    <el-container style="height: 100%">
      <el-header class="main-content-header">
        <el-image class="logo" src="https://element-plus.org/images/element-plus-logo.svg"/>
        <div style="flex: 1" class="user-info">
          <div class="profile">
            <div>用户名</div>
            <div>电子邮件</div>
          </div>
          <el-avatar src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png"/>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">Aside</el-aside>
        <el-main>Main</el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import {logout} from "@/net";
import router from "@/router";

function userLogout(){
  logout(() => router.push('/'))
}

</script>

<style lang="less" scoped>

.main-content {
  height: 100vh;
  width: 100vw;
}

.main-content-header {
  border-bottom: solid 1px var(--el-border-color);
  height: 55px;
  display: flex;
  align-items: center;
  box-sizing: border-box;

  .logo {
    height: 36px;
  }

  .user-info {
    display: flex;
    justify-content: flex-end ;
    align-items: center;

    .profile {
      text-align: right;
      margin-right: 20px;
    }

    :first-child  {
      font-size: 18px;
      font-weight: bold;
      line-height: 20px;
    }

    :last-child {
      font-size: 12px;
      color: grey;
    }
  }
}

</style>