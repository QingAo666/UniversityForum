<script setup>

</script>

<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex;">
    <div style="flex: 1;background-color: black">
      <img alt="" style="width: 100%;height: 100%" fit="cover" src="../image/2.png"></img>

    </div>

    <div class="welcome-title">
      <div style="font-size: 30px;font-weight: bold">欢迎来到农产品产销对接和冷链物流平台</div>
      <div style="margin-top: 10px">在这里你既可以出售农产品，也可以购入农产品</div>
      <div style="margin-top: 5px">并且实现了农产品冷链物流运输车的监控,可以实时在此网站进行查看</div>
    </div>

    <div class="right-card">
      <router-view></router-view>
    </div>
  </div>
</template>

<style scoped>
.right-card{
  width: 400px;
  z-index: 1;
  background-color: white;
}
.welcome-title{
  position: absolute;
  bottom: 50px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
</style>