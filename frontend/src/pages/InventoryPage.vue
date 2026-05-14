<template>
  <q-page class="q-pa-lg flex flex-center">
    <div class="page-container">
      <q-card class="q-pa-xl shadow-4">
        <div class="text-h5 text-weight-bold text-primary q-mb-lg">Consultar Stock</div>

        <q-form ref="formRef" @submit.prevent="search">
          <q-input v-model="productId" label="ID del producto *" outlined class="q-mb-md"
            :rules="[v => !!v || 'Requerido', v => /^\d+$/.test(v) || 'Solo números']" />

          <q-btn label="Consultar" type="submit" color="primary" icon="search"
            rounded class="full-width" :loading="store.loading" />
        </q-form>

        <q-banner v-if="store.error" class="bg-negative text-white rounded-borders q-mt-md">
          {{ store.error }}
        </q-banner>

        <q-card v-if="inventory" flat bordered class="q-mt-lg bg-grey-1">
          <q-card-section>
            <div class="text-subtitle1 text-weight-bold q-mb-sm">{{ inventory.product?.name }}</div>
            <div class="row q-gutter-md">
              <div><span class="text-grey-6">Precio:</span> ${{ inventory.product?.price }}</div>
              <q-chip :color="inventory.stock > 0 ? 'positive' : 'negative'" text-color="white">
                Stock: {{ inventory.stock }}
              </q-chip>
            </div>
          </q-card-section>
        </q-card>
      </q-card>
    </div>
  </q-page>
</template>

<script setup>
import { ref } from 'vue'
import { useInventoryStore } from '../stores/useInventoryStore'

const store = useInventoryStore()
const formRef = ref(null)
const productId = ref('')
const inventory = ref(null)

async function search() {
  const valid = await formRef.value.validate()
  if (!valid) return

  store.clearError()
  inventory.value = null
  inventory.value = await store.fetchByProductId(productId.value)
}
</script>
