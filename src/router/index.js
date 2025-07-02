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
      // 为任务列表页指定一个新的路径 '/tasks'
      path: '/tasks',
      name: 'task-list',
      component: () => import('@/views/task/TaskList.vue')
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
