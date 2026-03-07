<template>
  <nav
    :data-islogin="global.isLogin? 'true' : 'false'"
    class="navbar navbar-expand-md bg-color-white fixed-top"
  >
    <div class="nav-body">
      <div class="nav-logo-wrap-lg">
        <a class="navbar-logo-wrap" href="/">
          <img class="logo hidden-when-screen-small" src="/src/assets/static/img/logo.png"/>
<!--          <img src="/src/assets/static/img/icon.png" class="logo-lg display-when-screen-small" alt="" />-->
        </a>

        <el-dropdown :hide-on-click="false" class="display-when-screen-small center-content">
          <a class="el-dropdown-link nav-link display-when-screen-small">
            首页
          </a>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item><a class="dropdown-item" href="/">首页</a></el-dropdown-item>
              <el-dropdown-item><a class="dropdown-item" href="/chat">LLM 对话</a></el-dropdown-item>
              <el-dropdown-item><a class="dropdown-item" href="/agent">Agent</a></el-dropdown-item>
              <el-dropdown-item v-if="global.isLogin"><a class="dropdown-item" href="/admin">管理后台</a></el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div class="navbar-collapse hidden-when-screen-small">
        <ul class="navbar-nav">
          <el-space size="small">
            <li :class="{'selected-domain': activeTab == '/'}">
              <a class="nav-link" href="/">首页</a>
            </li>
            <li :class="{'selected-domain': activeTab == '/chat'}">
              <a class="nav-link" href="/chat">LLM 对话</a>
            </li>
            <li :class="{'selected-domain': activeTab.startsWith('/agent')}">
              <a class="nav-link" href="/agent">Agent</a>
            </li>
            <li v-if="global.isLogin" :class="{'selected-domain': activeTab.startsWith('/admin')}">
              <a class="nav-link" href="/admin">管理后台</a>
            </li>
          </el-space>
        </ul>
      </div>
      <div class="nav-right">
        <ul v-if="!global.isLogin">
          <li class="nav-item">
            <el-button @click="loginButton">登录</el-button>
          </li>
        </ul>
        <ul v-if="global.isLogin" class="nav-right-user">
          <!-- 头像框 -->
          <li class="nav-right-user center-content">
            <el-dropdown :hide-on-click="false">
              <div style="display: flex">
                <img
                  class="nav-login-img"
                  style="border-radius: 50%"
                  :src="global.user.photo? global.user.photo : 'https://static.developers.pub/static/img/logo.b2ff606.jpeg'"
                  alt=""
                  loading="lazy"
                />
                <div class="center-content m-2"><el-icon size="15"><ArrowDownBold /></el-icon></div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item><span @click="personalPage"> 个人主页 </span></el-dropdown-item>
                  <el-dropdown-item><span @click="logout"> 登出 </span></el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </li>
        </ul>
      </div>
    </div>
  </nav>
<!--  登录对话框 -->
</template>

<script setup lang="ts">
import { inject, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { doGet } from '@/http/BackendRequests'
import type { CommonResponse } from '@/http/ResponseTypes/CommonResponseType'
import { useGlobalStore } from '@/stores/global'
import { messageTip, refreshPage, sleep } from '@/util/utils'
import { MESSAGE_TYPE } from '@/constants/MessageTipEnumConstant'
import { ArrowDownBold } from '@element-plus/icons-vue'
import { LOGOUT_URL } from '@/http/URL'

const router = useRouter()
const route = useRoute()
const globalStore = useGlobalStore()
const global = globalStore.global

const activeTab = ref('/')
onMounted(() => {
  activeTab.value = router.currentRoute.value.path
})

const showLoginDialog = inject<() => void>('loginDialogClicked')
const loginButton = () => {
  if (showLoginDialog) showLoginDialog()
}

const personalPage = () => {
  if (route.fullPath.includes('/user/' + global.user.userId)) {
    messageTip('已经在个人主页了', MESSAGE_TYPE.INFO)
    return
  }
  router.push(global.user.userId ? '/user/' + global.user.userId : '/')
    .then(() => { if (global.user.userId != route.params['userId']) window.location.reload() })
}

const logout = () => {
  doGet<CommonResponse>(LOGOUT_URL, {}).then((response) => {
    if (response.data.status.code === 0) {
      messageTip('退出登录成功', MESSAGE_TYPE.SUCCESS)
      sleep(1)
      refreshPage()
    }
  })
}
</script>


<style scoped>

.wx-login-span-info{
  font-weight: bold;
  font-size: small;
  line-height: 10px;
}

.dropdown-item{
  margin: 5px;
}

.other-login {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 20px;
}

.other-login span {
  margin-right: 10px;
}

.qr-login img {
  width: 150px;
  height: 150px;
}

.qr-login .code {
  color: red;
  font-weight: bold;
}

span.wx-login-span-info{
  margin: 10px;
}

#login-agreement-message{
  margin: 10px
}

.el-main{
  padding: 0;
}

</style>
