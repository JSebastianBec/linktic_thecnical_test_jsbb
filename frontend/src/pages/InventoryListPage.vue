<template>
  <q-page class="q-pa-lg" style="background: #f4f7f0">

    <!-- Header -->
    <div class="row items-center q-mb-lg">
      <div class="col">
        <div class="text-h5 text-weight-bold text-secondary">Inventario General</div>
        <div class="text-caption text-grey-6">{{ store.inventoryList.length }} productos en inventario</div>
      </div>
      <q-btn icon="refresh" flat round color="primary" @click="store.fetchAll()"
        :loading="store.loading" />
      <q-btn label="Actualizar stock" icon="tune" color="secondary" unelevated rounded
        class="q-ml-sm" to="/inventory/stock" />
    </div>

    <!-- Stat chips -->
    <div class="row q-col-gutter-md q-mb-lg">
      <div class="col-12 col-sm-4">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="row items-center q-py-md">
            <q-icon name="inventory_2" color="primary" size="36px" class="q-mr-md" />
            <div>
              <div class="text-h5 text-weight-bold text-secondary">{{ store.inventoryList.length }}</div>
              <div class="text-caption text-grey-6">Total productos</div>
            </div>
          </q-card-section>
        </q-card>
      </div>
      <div class="col-12 col-sm-4">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="row items-center q-py-md">
            <q-icon name="check_circle" color="positive" size="36px" class="q-mr-md" />
            <div>
              <div class="text-h5 text-weight-bold text-positive">{{ withStock }}</div>
              <div class="text-caption text-grey-6">Con stock disponible</div>
            </div>
          </q-card-section>
        </q-card>
      </div>
      <div class="col-12 col-sm-4">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="row items-center q-py-md">
            <q-icon name="warning" color="negative" size="36px" class="q-mr-md" />
            <div>
              <div class="text-h5 text-weight-bold text-negative">{{ outOfStock }}</div>
              <div class="text-caption text-grey-6">Sin stock</div>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- Tabla -->
    <q-card flat bordered class="rounded-borders">
      <q-card-section class="bg-primary text-white q-py-sm q-px-md">
        <div class="row items-center">
          <div class="row items-center q-gutter-sm col">
            <q-icon name="table_chart" size="20px" />
            <span class="text-subtitle2">Estado del inventario</span>
          </div>
          <q-input v-model="search" placeholder="Buscar por nombre..." dark dense
            standout="bg-white text-secondary" class="col-auto" style="min-width: 200px">
            <template #append><q-icon name="search" /></template>
          </q-input>
        </div>
      </q-card-section>

      <q-card-section class="q-pa-md">
        <q-banner v-if="store.error" rounded class="bg-negative text-white q-mb-md">
          <template #avatar><q-icon name="error" /></template>
          {{ store.error }}
        </q-banner>

        <div v-if="store.loading" class="flex flex-center q-pa-xl">
          <q-spinner color="primary" size="48px" />
        </div>

        <q-table v-else :rows="filtered" :columns="columns" row-key="productId"
          flat separator="cell" class="rounded-borders"
          :no-data-label="'No hay registros de inventario'"
          :rows-per-page-options="[10, 20, 0]">

          <template #body-cell-stock="props">
            <q-td :props="props" class="text-center">
              <q-chip dense :color="props.value > 0 ? 'positive' : 'negative'"
                text-color="white" :icon="props.value > 0 ? 'check' : 'close'">
                {{ props.value }}
              </q-chip>
            </q-td>
          </template>

          <template #body-cell-price="props">
            <q-td :props="props">
              <span class="text-secondary text-weight-medium">${{ props.value }}</span>
            </q-td>
          </template>

        </q-table>
      </q-card-section>
    </q-card>

  </q-page>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useInventoryStore } from '../stores/useInventoryStore'

const store = useInventoryStore()
const search = ref('')

const columns = [
  { name: 'productId', label: 'ID',         field: r => r.productId,       align: 'left',   sortable: true, style: 'width: 60px' },
  { name: 'name',      label: 'Producto',    field: r => r.product?.name,   align: 'left',   sortable: true },
  { name: 'price',     label: 'Precio',      field: r => r.product?.price,  align: 'left',   sortable: true },
  { name: 'stock',     label: 'Stock',       field: 'stock',                align: 'center', sortable: true },
]

const filtered = computed(() => {
  if (!search.value) return store.inventoryList
  const q = search.value.toLowerCase()
  return store.inventoryList.filter(i => i.product?.name?.toLowerCase().includes(q))
})

const withStock   = computed(() => store.inventoryList.filter(i => i.stock > 0).length)
const outOfStock  = computed(() => store.inventoryList.filter(i => i.stock === 0).length)

onMounted(() => store.fetchAll())
</script>
