// 全局错误处理
export function setupErrorHandler(app) {
  app.config.errorHandler = (err, instance, info) => {
    console.error('Vue Error:', err)
    console.error('Component:', instance)
    console.error('Info:', info)
  }

  window.addEventListener('error', (event) => {
    console.error('Global Error:', event.error)
  })

  window.addEventListener('unhandledrejection', (event) => {
    console.error('Unhandled Promise Rejection:', event.reason)
  })
}

