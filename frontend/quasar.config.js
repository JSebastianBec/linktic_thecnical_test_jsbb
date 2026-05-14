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
          primary: '#3A86FF',
          secondary: '#8338EC',
          accent: '#FF006E',
          positive: '#21BA45',
          negative: '#C10015',
          info: '#31CCEC',
          warning: '#F2C037',
        },
        notify: { position: 'top-right', timeout: 3000 },
      },
      plugins: ['Notify', 'Loading'],
    },
    animations: [],
  }
})
