<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen,
    Ubuntu, Cantarell, 'Fira Sans', 'Droid Sans', 'Helvetica Neue', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #333;
}

body {
  background-color: #fff;
  margin: 0;
  padding: 0;
}

/* Element Plus 主题色覆盖 */
:root {
  --el-color-primary: #4CAF50;
}

.el-button--primary {
  --el-button-bg-color: #4CAF50;
  --el-button-border-color: #4CAF50;
  --el-button-hover-bg-color: #45a049;
  --el-button-hover-border-color: #45a049;
}
</style>

