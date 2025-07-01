import { createRouter, createWebHistory } from 'vue-router'
// 1. Import your new component
import TaskExecuteView from '../views/TaskExecuteView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      // 2. Set this component as the main route
      path: '/',
      name: 'task-execute',
      component: TaskExecuteView
    },
   
  ]
})

export default router