<template>
  <q-page class="q-pa-lg flex flex-center">
    <div class="page-container">
      <q-card class="q-pa-lg shadow-4">
        <div class="row items-center q-mb-lg">
          <div class="text-h5 text-weight-bold text-primary col">Productos</div>
          <q-btn icon="refresh" flat round color="primary" @click="store.fetchAll()" />
        </div>

        <q-banner v-if="store.error" class="bg-negative text-white rounded-borders q-mb-md">
          {{ store.error }}
        </q-banner>

        <div v-if="store.loading" class="flex flex-center q-pa-xl">
          <q-spinner color="primary" size="48px" />
        </div>

        <q-table v-else :rows="store.products" :columns="columns" row-key="id"
          flat bordered separator="horizontal" class="rounded-borders"
          :no-data-label="'No hay productos registrados'">
          <template #body-cell-price="props">
            <q-td :props="props">$ {{ props.value }}</q-td>
          </template>
        </q-table>
      </q-card>
    </div>
  </q-page>
</template>

<script setup>
import { onMounted } from 'vue'
import { useProductStore } from '../stores/useProductStore'

const store = useProductStore()

const columns = [
  { name: 'id', label: 'ID', field: 'id', align: 'left', sortable: true },
  { name: 'name', label: 'Nombre', field: 'name', align: 'left', sortable: true },
  { name: 'price', label: 'Precio', field: 'price', align: 'left', sortable: true },
  { name: 'description', label: 'Descripción', field: 'description', align: 'left' },
]

onMounted(() => store.fetchAll())
</script>
