import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // 将根路径'/'指向新的系统自检页
      path: '/',
      name: 'init',
      component: () => import('@/views/systemCheck/InitView.vue')
    },
      {
      // 将根路径'/'指向新的系统自检页
    path: '/settings', // 设置页面的 URL 路径
    name: 'settings', 
      component: () => import('@/views/systemCheck/SystemSettings.vue')
    },
    {
      // 为任务列表页指定一个新的路径 '/tasks'
      path: '/tasks',
      name: 'task-list',
      component: () => import('@/views/task/TaskList.vue')
    },
    // ===================================================
    // **新增：任务执行页面的路由**
    // ===================================================
    {
      // 路径包含一个动态参数 :id，用于接收要执行的任务ID
      path: '/task/execute/:id',
      name: 'task-execute',
      
      component: () => import('@/views/task/TaskExecuteView.vue')
    },
    {
      path: '/task/review/:id',
      name: 'task-review',
      component: () => import('@/views/task/TaskReview.vue')
    },
    {
      path: '/task/upload/:id',
      name: 'task-upload',
      component: () => import('@/views/task/TaskUpload.vue')
    }
  ]
})

export default router