<template>
  <q-page class="q-pa-lg flex flex-center">
    <div class="page-container">
      <q-card class="q-pa-lg shadow-4">
        <div class="row items-center q-mb-md">
          <div class="text-h5 text-weight-bold text-primary col">Lista de Inventario</div>
          <q-btn icon="refresh" flat round color="primary" @click="store.fetchAll()" />
        </div>

        <q-input v-model="search" placeholder="Buscar por nombre..." outlined dense
          clearable class="q-mb-md" @update:model-value="filterRows">
          <template #prepend><q-icon name="search" /></template>
        </q-input>

        <q-banner v-if="store.error" class="bg-negative text-white rounded-borders q-mb-md">
          {{ store.error }}
        </q-banner>

        <div v-if="store.loading" class="flex flex-center q-pa-xl">
          <q-spinner color="primary" size="48px" />
        </div>

        <q-table v-else :rows="filtered" :columns="columns" row-key="id"
          flat bordered separator="horizontal" class="rounded-borders"
          :no-data-label="'No hay registros de inventario'">
          <template #body-cell-stock="props">
            <q-td :props="props">
              <q-chip dense :color="props.value > 0 ? 'positive' : 'negative'" text-color="white">
                {{ props.value }}
              </q-chip>
            </q-td>
          </template>
        </q-table>
      </q-card>
    </div>
  </q-page>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useInventoryStore } from '../stores/useInventoryStore'

const store = useInventoryStore()
const search = ref('')

const columns = [
  { name: 'id', label: 'Producto ID', field: (r) => r.productId, align: 'left', sortable: true },
  { name: 'name', label: 'Nombre', field: (r) => r.product?.name, align: 'left', sortable: true },
  { name: 'price', label: 'Precio', field: (r) => `$${r.product?.price}`, align: 'left' },
  { name: 'stock', label: 'Stock', field: 'stock', align: 'center', sortable: true },
]

const filtered = computed(() => {
  if (!search.value) return store.inventoryList
  const q = search.value.toLowerCase()
  return store.inventoryList.filter((i) => i.product?.name?.toLowerCase().includes(q))
})

function filterRows() {}

onMounted(() => store.fetchAll())
</script>
