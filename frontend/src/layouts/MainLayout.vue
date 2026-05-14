<template>
  <q-layout view="hHh lpR fFf">
    <q-header elevated class="bg-primary text-white">
      <q-toolbar>
        <q-btn flat dense round icon="menu" @click="drawer = !drawer" />
        <q-toolbar-title>Linktic Inventory App</q-toolbar-title>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="drawer" show-if-above bordered :width="220">
      <q-list padding>
        <q-item-label header class="text-weight-bold text-grey-7">Productos</q-item-label>

        <q-item clickable v-ripple :to="'/products'" exact>
          <q-item-section avatar><q-icon name="add_box" /></q-item-section>
          <q-item-section>Crear producto</q-item-section>
        </q-item>

        <q-item clickable v-ripple :to="'/products/list'" exact>
          <q-item-section avatar><q-icon name="list" /></q-item-section>
          <q-item-section>Listar productos</q-item-section>
        </q-item>

        <q-item clickable v-ripple :to="'/products/search'" exact>
          <q-item-section avatar><q-icon name="search" /></q-item-section>
          <q-item-section>Buscar por ID</q-item-section>
        </q-item>

        <q-separator class="q-my-sm" />
        <q-item-label header class="text-weight-bold text-grey-7">Inventario</q-item-label>

        <q-item clickable v-ripple :to="'/inventory'" exact>
          <q-item-section avatar><q-icon name="analytics" /></q-item-section>
          <q-item-section>Consultar stock</q-item-section>
        </q-item>

        <q-item clickable v-ripple :to="'/inventory/list'" exact>
          <q-item-section avatar><q-icon name="table_chart" /></q-item-section>
          <q-item-section>Lista inventario</q-item-section>
        </q-item>

        <q-item clickable v-ripple :to="'/inventory/stock'" exact>
          <q-item-section avatar><q-icon name="inventory_2" /></q-item-section>
          <q-item-section>Actualizar stock</q-item-section>
        </q-item>

        <q-item clickable v-ripple :to="'/inventory/purchase'" exact>
          <q-item-section avatar><q-icon name="shopping_cart" /></q-item-section>
          <q-item-section>Realizar compra</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup>
import { ref } from 'vue'

const drawer = ref(true)
</script>
