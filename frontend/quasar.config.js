import { defineConfig } from '#q-app/wrappers'

export default defineConfig(() => {
  return {
    boot: ['pinia'],
    css: ['app.css'],
    extras: ['roboto-font', 'material-icons'],
    build: {
      target: { browser: 'es2022', node: 'node20' },
      vueRouterMode: 'hash',
    },
    devServer: {
      open: true,
      proxy: {
        '/api/products': {
          target: 'http://localhost:8080',
          rewrite: (path) => path.replace(/^\/api\/products/, '/api/v1/products'),
          changeOrigin: true,
        },
        '/api/inventory': {
          target: 'http://localhost:8081',
          rewrite: (path) => path.replace(/^\/api\/inventory/, '/api/v1/inventory'),
          changeOrigin: true,
        },
      },
    },
    framework: {
      config: {
        brand: {
          primary: '#7A9E59',
          secondary: '#5C7A3E',
          accent: '#C4A84F',
          positive: '#4CAF50',
          negative: '#C62828',
          info: '#5B9BAF',
          warning: '#E6A817',
        },
        notify: { position: 'top-right', timeout: 3000 },
      },
      plugins: ['Notify', 'Loading'],
    },
    animations: [],
  }
})
