<template>
  <q-layout view="hHh lpR fFf">

    <q-header class="bg-primary">
      <q-toolbar>
        <q-btn flat dense round icon="menu" @click="drawer = !drawer" />
        <q-icon name="inventory_2" color="white" size="22px" class="q-ml-sm" />
        <q-toolbar-title class="text-weight-bold q-ml-xs">
          Linktic Inventory
        </q-toolbar-title>
        <q-chip dense color="white" text-color="primary" class="q-mr-sm">v1.0.0</q-chip>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="drawer" show-if-above bordered :width="240"
      class="column no-wrap drawer-container">

      <!-- Branding -->
      <div class="drawer-header">
        <div class="text-caption opacity-70 q-mb-xs">Sistema de gestión</div>
        <div class="text-subtitle2 text-weight-bold">Panel de control</div>
      </div>

      <q-scroll-area class="col">
        <div class="q-px-md q-pt-md q-pb-sm">

          <!-- Sección Productos -->
          <div class="nav-section-label">
            <q-icon name="storefront" size="14px" />
            <span>Productos</span>
          </div>

          <div class="nav-group q-mb-sm">
            <div v-for="item in productLinks" :key="item.to"
              class="nav-item" :class="{ 'nav-item--active': isActive(item.to) }"
              @click="navigate(item.to)">
              <div class="nav-item__indicator" />
              <q-icon :name="item.icon" size="18px" class="nav-item__icon" />
              <span class="nav-item__label">{{ item.label }}</span>
            </div>
          </div>

          <!-- Separador -->
          <div class="nav-divider q-my-md" />

          <!-- Sección Inventario -->
          <div class="nav-section-label">
            <q-icon name="warehouse" size="14px" />
            <span>Inventario</span>
          </div>

          <div class="nav-group">
            <div v-for="item in inventoryLinks" :key="item.to"
              class="nav-item" :class="{ 'nav-item--active': isActive(item.to) }"
              @click="navigate(item.to)">
              <div class="nav-item__indicator" />
              <q-icon :name="item.icon" size="18px" class="nav-item__icon" />
              <span class="nav-item__label">{{ item.label }}</span>
            </div>
          </div>

        </div>
      </q-scroll-area>

      <div class="drawer-footer">
        Linktic Technical Test · JSBB
      </div>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>

  </q-layout>
</template>

<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref } from 'vue'

const drawer = ref(true)
const router = useRouter()
const route = useRoute()

const productLinks = [
  { to: '/products',        icon: 'add_box',       label: 'Crear producto' },
  { to: '/products/list',   icon: 'list_alt',      label: 'Listar productos' },
  { to: '/products/search', icon: 'manage_search', label: 'Buscar por ID' },
]

const inventoryLinks = [
  { to: '/inventory',          icon: 'analytics',     label: 'Consultar stock' },
  { to: '/inventory/list',     icon: 'table_chart',   label: 'Lista inventario' },
  { to: '/inventory/stock',    icon: 'tune',          label: 'Actualizar stock' },
  { to: '/inventory/purchase', icon: 'shopping_cart', label: 'Realizar compra' },
]

function isActive(path) {
  return route.path === path
}

function navigate(path) {
  router.push(path)
}
</script>

<style scoped>
.drawer-container {
  background: #f4f7f0;
}

.drawer-header {
  background: #5C7A3E;
  color: white;
  padding: 20px 20px 16px;
}

.drawer-footer {
  text-align: center;
  font-size: 11px;
  color: #a0a8a0;
  padding: 10px;
  border-top: 1px solid #e0e8d8;
}

/* Etiqueta de sección */
.nav-section-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #8a9e7a;
  padding: 0 4px 8px;
}

/* Grupo de items */
.nav-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* Cada item de navegación */
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  cursor: pointer;
  position: relative;
  transition: background 0.2s ease, transform 0.15s ease;
  color: #4a5e4a;
  user-select: none;
  overflow: hidden;
}

.nav-item:hover {
  background: rgba(122, 158, 89, 0.1);
  transform: translateX(2px);
}

.nav-item:active {
  transform: translateX(0px) scale(0.98);
}

/* Indicador lateral (barra verde) */
.nav-item__indicator {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%) scaleY(0);
  width: 3px;
  height: 60%;
  border-radius: 0 4px 4px 0;
  background: #7A9E59;
  transition: transform 0.2s ease;
}

/* Ícono */
.nav-item__icon {
  color: #6a8a6a;
  transition: color 0.2s ease;
  flex-shrink: 0;
}

/* Texto */
.nav-item__label {
  font-size: 13.5px;
  font-weight: 500;
  transition: color 0.2s ease;
}

/* Estado activo */
.nav-item--active {
  background: rgba(122, 158, 89, 0.15);
  color: #3d6b2e;
}

.nav-item--active .nav-item__indicator {
  transform: translateY(-50%) scaleY(1);
}

.nav-item--active .nav-item__icon {
  color: #5C7A3E;
}

.nav-item--active .nav-item__label {
  color: #3d6b2e;
  font-weight: 700;
}

/* Divisor */
.nav-divider {
  height: 1px;
  background: linear-gradient(to right, transparent, #c8d8b8, transparent);
}
</style>
