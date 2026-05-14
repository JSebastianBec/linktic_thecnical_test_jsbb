<template>
  <q-page class="q-pa-lg" style="background: #f4f7f0">

    <!-- Header -->
    <div class="row items-center q-mb-lg">
      <div class="col">
        <div class="text-h5 text-weight-bold text-secondary">Catálogo de Productos</div>
        <div class="text-caption text-grey-6">{{ store.products.length }} productos registrados</div>
      </div>
      <q-btn icon="refresh" flat round color="primary" @click="store.fetchAll()"
        :loading="store.loading" />
      <q-btn label="Nuevo" icon="add" color="primary" unelevated rounded
        class="q-ml-sm" to="/products" />
    </div>

    <!-- Stat chips -->
    <div class="row q-col-gutter-md q-mb-lg">
      <div class="col-12 col-sm-4">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="row items-center q-py-md">
            <q-icon name="category" color="primary" size="36px" class="q-mr-md" />
            <div>
              <div class="text-h5 text-weight-bold text-secondary">{{ store.products.length }}</div>
              <div class="text-caption text-grey-6">Total productos</div>
            </div>
          </q-card-section>
        </q-card>
      </div>
      <div class="col-12 col-sm-4">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="row items-center q-py-md">
            <q-icon name="attach_money" color="positive" size="36px" class="q-mr-md" />
            <div>
              <div class="text-h5 text-weight-bold text-positive">
                ${{ avgPrice }}
              </div>
              <div class="text-caption text-grey-6">Precio promedio</div>
            </div>
          </q-card-section>
        </q-card>
      </div>
      <div class="col-12 col-sm-4">
        <q-card flat bordered class="rounded-borders">
          <q-card-section class="row items-center q-py-md">
            <q-icon name="price_check" color="secondary" size="36px" class="q-mr-md" />
            <div>
              <div class="text-h5 text-weight-bold text-secondary">
                ${{ maxPrice }}
              </div>
              <div class="text-caption text-grey-6">Precio más alto</div>
            </div>
          </q-card-section>
        </q-card>
      </div>
    </div>

    <!-- Tabla -->
    <q-card flat bordered class="rounded-borders">
      <q-card-section class="bg-primary text-white q-py-sm q-px-md">
        <div class="row items-center q-gutter-sm">
          <q-icon name="list_alt" size="20px" />
          <span class="text-subtitle2">Lista de productos</span>
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

        <q-table v-else :rows="store.products" :columns="columns" row-key="id"
          flat separator="cell" class="rounded-borders"
          :no-data-label="'No hay productos registrados'"
          :rows-per-page-options="[10, 20, 0]">

          <template #body-cell-price="props">
            <q-td :props="props">
              <q-chip dense color="green-2" text-color="secondary" icon="attach_money">
                {{ props.value }}
              </q-chip>
            </q-td>
          </template>

          <template #body-cell-description="props">
            <q-td :props="props" class="text-grey-7">
              {{ props.value || '—' }}
            </q-td>
          </template>

        </q-table>
      </q-card-section>
    </q-card>

  </q-page>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useProductStore } from '../stores/useProductStore'

const store = useProductStore()

const columns = [
  { name: 'id',          label: 'ID',          field: 'id',          align: 'left', sortable: true, style: 'width: 60px' },
  { name: 'name',        label: 'Nombre',       field: 'name',        align: 'left', sortable: true },
  { name: 'price',       label: 'Precio',       field: 'price',       align: 'left', sortable: true },
  { name: 'description', label: 'Descripción',  field: 'description', align: 'left' },
]

const avgPrice = computed(() => {
  if (!store.products.length) return '0.00'
  const avg = store.products.reduce((s, p) => s + p.price, 0) / store.products.length
  return avg.toFixed(2)
})

const maxPrice = computed(() => {
  if (!store.products.length) return '0.00'
  return Math.max(...store.products.map(p => p.price)).toFixed(2)
})

onMounted(() => store.fetchAll())
</script>
