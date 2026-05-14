const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', redirect: '/products' },
      { path: 'products', component: () => import('pages/ProductsPage.vue') },
      { path: 'products/list', component: () => import('pages/ProductsListPage.vue') },
      { path: 'products/search', component: () => import('pages/ProductByIdPage.vue') },
      { path: 'inventory', component: () => import('pages/InventoryPage.vue') },
      { path: 'inventory/list', component: () => import('pages/InventoryListPage.vue') },
      { path: 'inventory/stock', component: () => import('pages/UpdateStockPage.vue') },
      { path: 'inventory/purchase', component: () => import('pages/PurchasePage.vue') },
    ],
  },
  { path: '/:catchAll(.*)*', component: () => import('pages/ErrorNotFound.vue') },
]

export default routes
