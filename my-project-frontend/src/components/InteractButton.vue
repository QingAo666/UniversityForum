<script setup>
defineProps({
  name: String,
  checkName: String,
  color: String,
  check: Boolean
})

const emit = defineEmits(['check'])
</script>

<template>
  <div class="interact-button">
    <span class="icon" :style="{'color': check ? color : 'unset'}" @click="emit('check')">
      <slot/>
    </span>
    <span class="name" :style="{'color': color }">
      {{ check ? checkName : name }}
    </span>
  </div>
</template>

<style scoped>
.interact-button{
  display: inline-block;
  height: 20px;

  .name{
    font-size: 15px;
    margin-left: 5px;
    opacity: 0.7;
  }
  .icon{
    vertical-align: middle;
    transition: 0.3s;

    &:hover{
      cursor: pointer;
      font-size: 18px;
    }
  }
}
</style>